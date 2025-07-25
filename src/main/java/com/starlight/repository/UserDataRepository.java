package com.starlight.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.starlight.model.User;
import com.starlight.util.FileSystemManager;

/**
 * Repository for persisting {@link User} objects to an XML file.
 */
public class UserDataRepository {
    private static final String DEFAULT_XML_PATH = FileSystemManager.getDatabaseDirectory() + File.separator + "UserData.xml";
    private static final String DUMMY_XML_PATH = "src/main/java/com/starlight/data/UserDataDummy.xml";

    private final String xmlPath;
    private final XStream xstream;

    /**
     * Creates a repository that reads/writes the default user XML file.
     */
    public UserDataRepository() {
        this(DEFAULT_XML_PATH);
    }

    /**
     * Creates a repository storing users in the specified path.
     */
    public UserDataRepository(String xmlPath) {
        this.xmlPath = xmlPath;
        File parent = new File(xmlPath).getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        xstream = new XStream(new DomDriver());
        xstream.allowTypesByWildcard(new String[] {"com.starlight.model.*", "java.util.*"});
        xstream.alias("users", List.class);
        xstream.alias("user", User.class);
    }

    /**
     * Loads users including dummy data.
     */
    public List<User> loadUsers() {
        return loadUsers(true);
    }

    /**
     * @param includeDummy whether to also include dummy users
     * @return the list of users from the main file combined with dummy users if requested
     */
    public List<User> loadUsers(boolean includeDummy) {
        List<User> users = readUsersFromFile(new File(xmlPath));

        if (includeDummy || users.isEmpty()) {
            List<User> dummy = readUsersFromFile(new File(DUMMY_XML_PATH));
            if (includeDummy) {
                users.addAll(dummy);
            } else if (users.isEmpty()) {
                users = dummy;
            }
        }

        return users;
    }

    @SuppressWarnings("unchecked")
    private List<User> readUsersFromFile(File xmlFile) {
        if (!xmlFile.exists() || xmlFile.length() == 0) {
            return new ArrayList<>();
        }

        try (FileInputStream fis = new FileInputStream(xmlFile)) {
            Object obj = xstream.fromXML(fis);
            if (obj instanceof List) {
                return (List<User>) obj;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ArrayList<>();
    }

    /**
     * Ensures the main XML file contains at least the dummy users.
     */
    public void ensureDummyData() {
        File xmlFile = new File(xmlPath);
        boolean copy = false;

        if (!xmlFile.exists() || xmlFile.length() == 0) {
            copy = true;
        } else {
            try (FileInputStream fis = new FileInputStream(xmlFile)) {
                Object obj = xstream.fromXML(fis);
                if (obj instanceof List && ((List<?>) obj).isEmpty()) {
                    copy = true;
                }
            } catch (Exception e) {
                // if parsing fails, fall back to dummy
                copy = true;
            }
        }

        if (copy) {
            File dummy = new File(DUMMY_XML_PATH);
            if (dummy.exists()) {
                try (FileInputStream fis = new FileInputStream(dummy);
                     FileOutputStream fos = new FileOutputStream(xmlFile)) {
                    fos.write(fis.readAllBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Saves the provided list of users.
     */
    public void saveUsers(List<User> users) {
        // Prevent duplicate users before saving
        List<User> distinctUsers = users.stream()
                .distinct()
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));

        try (FileOutputStream fos = new FileOutputStream(xmlPath)) {
            if (distinctUsers.isEmpty()) {
                fos.write("<users></users>".getBytes(java.nio.charset.StandardCharsets.UTF_8));
            } else {
                xstream.toXML(distinctUsers, fos);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save users", e);
        }
    }

    /**
     * Deletes a user by username and saves the updated user list
     * @param username the username of the user to delete
     * @return true if user was found and deleted, false otherwise
     */
    public boolean deleteUser(String username) {
        List<User> users = loadUsers(false); // Load only real users, not dummy ones
        int initialSize = users.size();
        
        // Remove all instances of the user (in case of duplicates)
        users.removeIf(user -> user.username.equals(username));
        
        // Check if any users were removed
        if (users.size() < initialSize) {
            // Save the updated list
            saveUsers(users);
            return true;
        }
        
        return false;
    }
}

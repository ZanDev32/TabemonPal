package com.starlight.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.starlight.model.User;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Repository for managing local user data from LocalUserData.xml.
 * This provides admin and user accounts that are always available.
 */
public class LocalUserDataRepository {
    private static final Logger logger = Logger.getLogger(LocalUserDataRepository.class.getName());
    private static final String LOCAL_XML_PATH = "src/main/java/com/starlight/data/LocalUserData.xml";
    
    private final XStream xstream;
    private List<User> cachedUsers;

    /**
     * Creates a new LocalUserDataRepository instance.
     */
    public LocalUserDataRepository() {
        xstream = new XStream(new DomDriver());
        xstream.allowTypesByWildcard(new String[] {"com.starlight.model.*", "java.util.*"});
        xstream.alias("users", List.class);
        xstream.alias("user", User.class);
        loadUsers();
    }

    /**
     * Loads users from the local XML file.
     * @return List of local users (admin and user)
     */
    @SuppressWarnings("unchecked")
    public List<User> loadUsers() {
        if (cachedUsers != null) {
            return new ArrayList<>(cachedUsers);
        }

        try {
            File xmlFile = new File(LOCAL_XML_PATH);
            if (!xmlFile.exists()) {
                // Try to load from resources if file doesn't exist in src
                InputStream resourceStream = getClass().getResourceAsStream("/com/starlight/model/LocalUserData.xml");
                if (resourceStream == null) {
                    logger.warning("LocalUserData.xml not found in resources, creating default users");
                    return createDefaultUsers();
                }
                
                try (InputStream is = resourceStream) {
                    Object obj = xstream.fromXML(is);
                    if (obj instanceof List) {
                        cachedUsers = (List<User>) obj;
                        return new ArrayList<>(cachedUsers);
                    }
                }
            } else {
                try (FileInputStream fis = new FileInputStream(xmlFile)) {
                    Object obj = xstream.fromXML(fis);
                    if (obj instanceof List) {
                        cachedUsers = (List<User>) obj;
                        return new ArrayList<>(cachedUsers);
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to load LocalUserData.xml, creating default users", e);
        }

        return createDefaultUsers();
    }

    /**
     * Creates default admin and user accounts if XML loading fails.
     */
    private List<User> createDefaultUsers() {
        List<User> defaultUsers = new ArrayList<>();
        
        // Create admin user
        User admin = new User();
        admin.username = "admin";
        admin.email = "admin@tabemonpal.local";
        admin.fullname = "Administrator";
        admin.password = "admin123";
        admin.birthDay = "1990-01-01";
        admin.profilepicture = "src/main/resources/com/starlight/images/dummy/profileman.jpg";
        defaultUsers.add(admin);
        
        // Create default user
        User user = new User();
        user.username = "user";
        user.email = "user@tabemonpal.local";
        user.fullname = "Default User";
        user.password = "user123";
        user.birthDay = "1995-01-01";
        user.profilepicture = "src/main/resources/com/starlight/images/dummy/profiledefault.png";
        defaultUsers.add(user);
        
        cachedUsers = defaultUsers;
        return new ArrayList<>(defaultUsers);
    }

    /**
     * Saves users to the local XML file.
     * @param users List of users to save
     */
    public void saveUsers(List<User> users) {
        try {
            File xmlFile = new File(LOCAL_XML_PATH);
            xmlFile.getParentFile().mkdirs();
            
            try (FileOutputStream fos = new FileOutputStream(xmlFile)) {
                xstream.toXML(users, fos);
                cachedUsers = new ArrayList<>(users);
                logger.info("Successfully saved " + users.size() + " users to LocalUserData.xml");
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to save users to LocalUserData.xml", e);
        }
    }

    /**
     * Finds a user by username or email.
     * @param usernameOrEmail Username or email to search for
     * @return User if found, null otherwise
     */
    public User findUser(String usernameOrEmail) {
        List<User> users = loadUsers();
        return users.stream()
                .filter(u -> (u.username != null && u.username.equals(usernameOrEmail)) ||
                           (u.email != null && u.email.equals(usernameOrEmail)))
                .findFirst()
                .orElse(null);
    }

    /**
     * Validates user credentials.
     * @param usernameOrEmail Username or email
     * @param password Password
     * @return User if credentials are valid, null otherwise
     */
    public User validateCredentials(String usernameOrEmail, String password) {
        List<User> users = loadUsers();
        return users.stream()
                .filter(u -> ((u.username != null && u.username.equals(usernameOrEmail)) ||
                            (u.email != null && u.email.equals(usernameOrEmail))) &&
                            u.password != null && u.password.equals(password))
                .findFirst()
                .orElse(null);
    }

    /**
     * Checks if a user is an admin.
     * @param user User to check
     * @return true if user is admin, false otherwise
     */
    public boolean isAdmin(User user) {
        return user != null && user.username != null && user.username.toLowerCase().equals("admin");
    }

    /**
     * Gets the admin user.
     * @return Admin user or null if not found
     */
    public User getAdminUser() {
        return findUser("admin");
    }

    /**
     * Gets the default user.
     * @return Default user or null if not found
     */
    public User getDefaultUser() {
        return findUser("user");
    }

    /**
     * Resets cached users to force reload from file.
     */
    public void clearCache() {
        cachedUsers = null;
    }
}

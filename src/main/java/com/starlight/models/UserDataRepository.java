package com.starlight.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class UserDataRepository {
    private static final String DEFAULT_XML_PATH = "src/main/java/com/starlight/models/UserData.xml";
    private static final String DUMMY_XML_PATH = "src/main/java/com/starlight/models/UserDataDummy.xml";

    private final String xmlPath;
    private final XStream xstream;

    public UserDataRepository() {
        this(DEFAULT_XML_PATH);
    }

    public UserDataRepository(String xmlPath) {
        this.xmlPath = xmlPath;
        xstream = new XStream(new DomDriver());
        xstream.allowTypesByWildcard(new String[] {"com.starlight.models.*", "java.util.*"});
        xstream.alias("users", List.class);
        xstream.alias("user", User.class);
    }

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

    public void saveUsers(List<User> users) {
        try (FileOutputStream fos = new FileOutputStream(xmlPath)) {
            xstream.toXML(users, fos);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save users", e);
        }
    }
}

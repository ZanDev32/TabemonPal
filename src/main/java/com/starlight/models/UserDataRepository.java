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
        return loadUsers(false, true);
    }

    /**
     * @param useDummy If true, loads from dummy file. If false, loads from main file.
     * @return The list of users.
     */
    public List<User> loadUsers(boolean useDummy) {
        return loadUsers(useDummy, false);
    }

    @SuppressWarnings("unchecked")
    private List<User> loadUsers(boolean useDummy, boolean fallbackToDummyIfMissing) {
        File xmlFile = new File(useDummy ? DUMMY_XML_PATH : xmlPath);
        if (!xmlFile.exists() || xmlFile.length() == 0) {
            if (!useDummy && fallbackToDummyIfMissing) {
                xmlFile = new File(DUMMY_XML_PATH);
                if (!xmlFile.exists()) {
                    return new ArrayList<>();
                }
            } else {
                return new ArrayList<>();
            }
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
        if (!xmlFile.exists() || xmlFile.length() == 0) {
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

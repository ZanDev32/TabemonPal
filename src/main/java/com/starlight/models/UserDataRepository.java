package com.starlight.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class UserDataRepository {
    private static final String XML_PATH = "src/main/java/com/starlight/models/UserData.xml";

    private final XStream xstream;

    public UserDataRepository() {
        xstream = new XStream(new DomDriver());
        xstream.allowTypesByWildcard(new String[] {"com.starlight.models.*", "java.util.*"});
        xstream.alias("users", List.class);
        xstream.alias("user", User.class);
    }

    @SuppressWarnings("unchecked")
    public List<User> loadUsers() {
        File xmlFile = new File(XML_PATH);
        if (!xmlFile.exists()) return new ArrayList<>();
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

    public void saveUsers(List<User> users) {
        try (FileOutputStream fos = new FileOutputStream(XML_PATH)) {
            xstream.toXML(users, fos);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save users", e);
        }
    }
}

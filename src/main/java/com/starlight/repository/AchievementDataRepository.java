package com.starlight.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import com.starlight.model.Post;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class AchievementDataRepository {
    private static final String DEFAULT_XML_PATH = "src/main/java/com/starlight/model/PostData.xml";
    private static final String DUMMY_XML_PATH = "src/main/java/com/starlight/model/PostDataDummy.xml";

    private final String xmlPath;
    private final XStream xstream;

    /**
     * Creates a repository using the default XML file location.
     */
    public AchievementDataRepository() {
        this(DEFAULT_XML_PATH);
    }

    /**
     * Creates a repository storing data at the specified path.
     */
    public AchievementDataRepository(String xmlPath) {
        this.xmlPath = xmlPath;
        xstream = new XStream(new DomDriver());
        xstream.allowTypesByWildcard(new String[] {"com.starlight.model.*", "java.util.*"});
        xstream.alias("posts", List.class);
        xstream.alias("post", Post.class);
    }

    /**
     * Loads posts from the default XML file.
     */
    public List<Post> loadPosts() {
        return loadPosts(false, true);
    }

    /**
     * @param useDummy If true, loads from dummy file. If false, loads from main file.
     * @return The list of posts.
     */
    public List<Post> loadPosts(boolean useDummy) {
        return loadPosts(useDummy, false);
    }

    @SuppressWarnings("unchecked")
    private List<Post> loadPosts(boolean useDummy, boolean fallbackToDummyIfMissing) {
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
                return (List<Post>) obj;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     * Copies the dummy data file to the main XML file if the main file is
     * missing or empty.
     */
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

    /**
     * Saves the given list of posts to disk.
     */
    public void savePosts(List<Post> posts) {
        try (FileOutputStream fos = new FileOutputStream(xmlPath)) {
            xstream.toXML(posts, fos);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save posts", e);
        }
    }
}

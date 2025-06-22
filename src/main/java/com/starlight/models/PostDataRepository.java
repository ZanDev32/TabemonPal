package com.starlight.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class PostDataRepository {
    private static final String DEFAULT_XML_PATH = "src/main/java/com/starlight/models/PostData.xml";

    private final String xmlPath;
    private final XStream xstream;

    public PostDataRepository() {
        this(DEFAULT_XML_PATH);
    }

    public PostDataRepository(String xmlPath) {
        this.xmlPath = xmlPath;
        xstream = new XStream(new DomDriver());
        xstream.allowTypesByWildcard(new String[] {"com.starlight.models.*", "java.util.*"});
        xstream.alias("posts", List.class);
        xstream.alias("post", Post.class);
    }

    @SuppressWarnings("unchecked")

    public List<Post> loadPosts() {
        File xmlFile = new File(xmlPath);
        if (!xmlFile.exists()) return new ArrayList<>();
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

    public void savePosts(List<Post> posts) {
        try (FileOutputStream fos = new FileOutputStream(xmlPath)) {
            xstream.toXML(posts, fos);;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save posts", e);
        }
    }
}

package com.starlight.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

public class PostDataRepository {
    private static final String XML_PATH = "src/main/java/com/starlight/models/PostData.xml";

        private final XStream xstream;

    public PostDataRepository() {
        xstream = new XStream(new DomDriver());
        xstream.allowTypesByWildcard(new String[] {"com.starlight.models.*", "java.util.*"});
        xstream.alias("posts", List.class);
        xstream.alias("post", Post.class);
    }

    @SuppressWarnings("unchecked")

    public List<Post> loadPosts() {
        File xmlFile = new File(XML_PATH);
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
        try (FileOutputStream fos = new FileOutputStream(XML_PATH)) {
            xstream.toXML(posts, fos);;
        } catch (Exception e) {
            throw new RuntimeException("Failed to save posts", e);
        }
    }
}

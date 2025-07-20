package com.starlight.repository;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.starlight.models.Post;
import com.starlight.util.FileSystemManager;

/**
 * Repository for loading and saving {@link Post} objects to an XML file.
 */
public class PostDataRepository {
    private static final String DEFAULT_XML_PATH = FileSystemManager.getDatabaseDirectory() + File.separator + "PostData.xml";
    private static final String DUMMY_XML_PATH = "src/main/java/com/starlight/models/PostDataDummy.xml";

    private final String xmlPath;
    private final XStream xstream;

    /**
     * Creates a repository using the default XML file location.
     */
    public PostDataRepository() {
        this(DEFAULT_XML_PATH);
    }

    /**
     * Creates a repository storing data at the specified path.
     */
    public PostDataRepository(String xmlPath) {
        this.xmlPath = xmlPath;
        File parent = new File(xmlPath).getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        xstream = new XStream(new DomDriver());
        xstream.allowTypesByWildcard(new String[] {"com.starlight.models.*", "java.util.*"});
        xstream.alias("posts", List.class);
        xstream.alias("post", Post.class);
        xstream.alias("nutrition", com.starlight.models.Nutrition.class);
        xstream.alias("ingredient", com.starlight.models.Nutrition.NutritionIngredient.class);
        xstream.alias("calories", com.starlight.models.Nutrition.Calories.class);
        xstream.alias("protein", com.starlight.models.Nutrition.Protein.class);
        xstream.alias("fat", com.starlight.models.Nutrition.Fat.class);
        xstream.alias("carbohydrates", com.starlight.models.Nutrition.Carbohydrates.class);
        xstream.alias("fiber", com.starlight.models.Nutrition.Fiber.class);
        xstream.alias("sugar", com.starlight.models.Nutrition.Sugar.class);
        xstream.alias("salt", com.starlight.models.Nutrition.Salt.class);
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
                List<Post> posts = (List<Post>) obj;
                // Initialize missing fields for existing posts
                for (Post post : posts) {
                    if (post.commentcount == null) {
                        post.commentcount = "0";
                    }
                    if (post.isLiked == null) {
                        post.isLiked = "false";
                    }
                }
                return posts;
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
            // Ensure all posts have the required fields before saving
            for (Post post : posts) {
                if (post.commentcount == null) {
                    post.commentcount = "0";
                }
                if (post.isLiked == null) {
                    post.isLiked = "false";
                }
            }
            xstream.toXML(posts, fos);
        } catch (Exception e) {
            throw new RuntimeException("Failed to save posts to file: " + xmlPath + ". Error: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes a post by UUID and saves the updated post list
     * @param uuid the UUID of the post to delete
     * @return true if post was found and deleted, false otherwise
     */
    public boolean deletePost(String uuid) {
        List<Post> posts = loadPosts();
        int initialSize = posts.size();
        
        // Remove the post with the specified UUID
        posts.removeIf(post -> post.uuid != null && post.uuid.equals(uuid));
        
        // Check if any posts were removed
        if (posts.size() < initialSize) {
            // Save the updated list
            savePosts(posts);
            return true;
        }
        
        return false;
    }
}

package com.starlight.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

public class PostDataRepository {
    private static final String XML_PATH = "src/main/java/com/starlight/models/PostData.xml";

    public List<Post> loadPosts() {
        List<Post> list = new ArrayList<>();
        File xmlFile = new File(XML_PATH);
        if (!xmlFile.exists()) return list;
        try (FileInputStream fis = new FileInputStream(xmlFile)) {
            XMLInputFactory factory = XMLInputFactory.newInstance();
            XMLStreamReader reader = factory.createXMLStreamReader(fis);
            Post current = null;
            String currentTag = null;
            StringBuilder buffer = new StringBuilder();
            while (reader.hasNext()) {
                int event = reader.next();
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        currentTag = reader.getLocalName();
                        if ("post".equals(currentTag)) {
                            current = new Post();
                        } else {
                            buffer.setLength(0);
                        }
                        break;
                    case XMLStreamConstants.CHARACTERS:
                        if (currentTag != null) {
                            buffer.append(reader.getText());
                        }
                        break;
                    case XMLStreamConstants.END_ELEMENT:
                        String end = reader.getLocalName();
                        if (current != null) {
                            String text = buffer.toString().trim();
                            switch (end) {
                                case "title":
                                    current.title = text;
                                    break;
                                case "description":
                                    current.description = text;
                                    break;
                                case "ingredients":
                                    current.ingredients = text;
                                    break;
                                case "directions":
                                    current.directions = text;
                                    break;
                                case "image":
                                    current.image = text;
                                    break;
                                case "rating":
                                    current.rating = text;
                                    break;
                                case "uploadtime":
                                    current.uploadtime = text;
                                    break;
                                case "likecount":
                                    current.likecount = text;
                                    break;
                                case "post":
                                    list.add(current);
                                    current = null;
                                    break;
                            }
                        }
                        currentTag = null;
                        break;
                }
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public void savePosts(List<Post> posts) {
        try {
            XStream xstream = new XStream(new StaxDriver());
            xstream.alias("posts", List.class);
            xstream.alias("post", Post.class);

            StringWriter sw = new StringWriter();
            xstream.toXML(posts, sw);

            String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + sw.toString();

            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new InputSource(new StringReader(xml)));

            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(XML_PATH), StandardCharsets.UTF_8)) {
                transformer.transform(new DOMSource(doc), new StreamResult(osw));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save posts", e);
        }
    }
}

package com.starlight.models;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

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
            XMLOutputFactory outFactory = XMLOutputFactory.newInstance();
            XMLStreamWriter writer = outFactory.createXMLStreamWriter(new FileOutputStream(XML_PATH), "UTF-8");
            writer.writeStartDocument("UTF-8", "1.0");
            writer.writeStartElement("posts");
            for (Post p : posts) {
                writer.writeStartElement("post");

                writer.writeStartElement("title");
                writer.writeCharacters(p.title != null ? p.title : "");
                writer.writeEndElement();

                writer.writeStartElement("description");
                writer.writeCharacters(p.description != null ? p.description : "");
                writer.writeEndElement();

                writer.writeStartElement("ingredients");
                writer.writeCharacters(p.ingredients != null ? p.ingredients : "");
                writer.writeEndElement();

                writer.writeStartElement("directions");
                writer.writeCharacters(p.directions != null ? p.directions : "");
                writer.writeEndElement();

                writer.writeStartElement("image");
                writer.writeCharacters(p.image != null ? p.image : "");
                writer.writeEndElement();

                writer.writeStartElement("rating");
                writer.writeCharacters(p.rating != null ? p.rating : "");
                writer.writeEndElement();

                writer.writeStartElement("uploadtime");
                writer.writeCharacters(p.uploadtime != null ? p.uploadtime : "");
                writer.writeEndElement();

                writer.writeStartElement("likecount");
                writer.writeCharacters(p.likecount != null ? p.likecount : "");
                writer.writeEndElement();

                writer.writeEndElement();
            }
            writer.writeEndElement();
            writer.writeEndDocument();
            writer.close();
        } catch (Exception e) {
            throw new RuntimeException("Failed to save posts", e);
        }
    }
}

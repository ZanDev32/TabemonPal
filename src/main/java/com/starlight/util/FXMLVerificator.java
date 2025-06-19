package com.starlight.util;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.stream.Stream;

public class FXMLVerificator {

    // You can customize these
    private static final String EXPECTED_NAMESPACE = "http://javafx.com/javafx/19";
    private static final String FXML_DIR = "src/main/resources";
    private static final List<String> ILLEGAL_ATTRIBUTES = List.of("fx:factory", "fx:constant");
    private static final List<String> UNSUPPORTED_TAGS = List.of("<TextFlow", "<FlowPane", "<ColorPicker", "<TreeTableView");

    public static void verifyAll() {
        Path root = Paths.get(FXML_DIR);

        try (Stream<Path> files = Files.walk(root)) {
            files.filter(path -> path.toString().endsWith(".fxml"))
                 .forEach(FXMLVerificator::verifyOneFile);
        } catch (IOException e) {
            throw new RuntimeException("Failed to walk FXML directory: " + FXML_DIR, e);
        }
    }

    private static void verifyOneFile(Path fxmlPath) {
        try {
            String content = Files.readString(fxmlPath);
            String original = content;

            // Downgrade xmlns version
            content = content.replaceAll("http://javafx.com/javafx/\\d+", EXPECTED_NAMESPACE);

            // Detect illegal fx attributes
            for (String attr : ILLEGAL_ATTRIBUTES) {
                if (content.contains(attr)) {
                    throw new RuntimeException("FXML contains unsupported attribute: " + attr + " in " + fxmlPath);
                }
            }

            // Detect unsupported tags
            for (String tag : UNSUPPORTED_TAGS) {
                if (content.contains(tag)) {
                    throw new RuntimeException("FXML uses possibly unsupported tag: " + tag + " in " + fxmlPath);
                }
            }

            // Save changes only if content was modified
            if (!original.equals(content)) {
                Files.writeString(fxmlPath, content);
                System.out.println("Downgraded version in: " + fxmlPath.getFileName());
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to verify FXML file: " + fxmlPath, e);
        }
    }
}

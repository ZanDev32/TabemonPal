package com.starlight.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Logger;
import java.util.logging.Level;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * API for interacting with OpenAI ChatGPT service.
 * Handles API key management and chat completions using HTTP client.
 */
public class ChatbotAPI {
    private static final Logger logger = Logger.getLogger(ChatbotAPI.class.getName());
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private String apiKey;
    private HttpClient httpClient;
    private XStream xstream;
    
    /**
     * Configuration model for the SECRET_KEY.xml file.
     */
    public static class Config {
        private String openaiKey;
        
        public String getOpenaiKey() {
            return openaiKey;
        }
        
        public void setOpenaiKey(String openaiKey) {
            this.openaiKey = openaiKey;
        }
    }
    
    public ChatbotAPI() {
        this.xstream = new XStream(new DomDriver());
        this.xstream.allowTypesByWildcard(new String[]{"com.starlight.api.*"});
        this.xstream.alias("config", Config.class);
        this.xstream.aliasField("openai-key", Config.class, "openaiKey");
        
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
                
        try {
            loadApiKey();
        } catch (Exception e) {
            logger.log(Level.WARNING, "Could not load API key: " + e.getMessage(), e);
            logger.warning("Chatbot functionality will be disabled.");
        }
    }
    
    /**
     * Loads the API key from the SECRET_KEY.xml file.
     */
    private void loadApiKey() {
        String userHome = System.getProperty("user.home");
        String secretKeyPath = userHome + File.separator + ".tabemonpal" + File.separator + "Database" + File.separator + ".SECRET_KEY.xml";
        
        try (FileInputStream fis = new FileInputStream(secretKeyPath)) {
            Config config = (Config) xstream.fromXML(fis);
            this.apiKey = config.getOpenaiKey();
            
            if (this.apiKey == null || this.apiKey.trim().isEmpty() || this.apiKey.equals("your-openai-api-key-here")) {
                logger.warning("No valid API key found in SECRET_KEY.xml. Chatbot will work in demo mode.");
                this.apiKey = null;
            }
        } catch (IOException e) {
            logger.log(Level.WARNING, "Could not load API key from " + secretKeyPath + ": " + e.getMessage(), e);
            this.apiKey = null;
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error parsing SECRET_KEY.xml: " + e.getMessage(), e);
            this.apiKey = null;
        }
    }
    
    /**
     * Sends a message to the ChatGPT API and returns the response.
     *
     * @param userMessage The message from the user
     * @return The ChatGPT response
     * @throws ChatbotException if the API call fails
     */
    public String sendMessage(String userMessage) throws ChatbotException {
        return sendMessage(userMessage, null);
    }
    
    /**
     * Sends a message to the ChatGPT API with context and returns the response.
     *
     * @param userMessage The message from the user
     * @param systemMessage Optional system message to set context
     * @return The ChatGPT response
     * @throws ChatbotException if the API call fails
     */
    public String sendMessage(String userMessage, String systemMessage) throws ChatbotException {
        // Check for special Fumo keywords
        if (isFumoKeyword(userMessage)) {
            return "Fumo ᗜˬᗜ";
        }
        
        if (apiKey == null) {
            return "I'm currently in demo mode. Please configure your OpenAI API key in ~/.tabemonpal/Database/.SECRET_KEY.xml to enable full functionality.";
        }
        
        try {
            String requestBody = buildRequestBody(userMessage, systemMessage);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(OPENAI_API_URL))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + apiKey)
                    .timeout(Duration.ofSeconds(60))
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                return parseResponse(response.body());
            } else {
                String errorMessage = "API request failed with status " + response.statusCode() + 
                                    ": " + response.body();
                logger.severe("OpenAI API Error: " + errorMessage);
                throw new ChatbotException(errorMessage);
            }
            
        } catch (ChatbotException e) {
            throw e; // Re-throw our custom exceptions
        } catch (Exception e) {
            String errorMessage = "Failed to get response from ChatGPT: " + e.getMessage();
            logger.log(Level.SEVERE, "ChatGPT Request Error: " + errorMessage, e);
            throw new ChatbotException(errorMessage, e);
        }
    }
    
    /**
     * Sends a nutrition-related question to ChatGPT with appropriate context.
     *
     * @param question The nutrition question
     * @return The ChatGPT response
     * @throws ChatbotException if the API call fails
     */
    public String askNutritionQuestion(String question) throws ChatbotException {
        String systemMessage = "You are Kuro , a helpful nutrition assistant for the TabemonPal app. " +
                "Provide accurate, helpful nutrition advice and food recommendations. " +
                "Keep your responses concise but informative. " +
                "you may use rich text formatting to enhance readability." +
                "If you're unsure about medical advice, recommend consulting with a healthcare professional." +
                "Avoid giving information on topics outside of nutrition, health, or wellness.";

        return sendMessage(question, systemMessage);
    }
    
    /**
     * Analyzes nutrition facts for given ingredients using ChatGPT.
     *
     * @param ingredients The ingredients string (vertical line separated)
     * @return The nutrition facts in XML format
     * @throws ChatbotException if the API call fails
     */
    public String analyzeNutritionFacts(String ingredients) throws ChatbotException {
        // Check if we're in demo mode (no API key)
        if (apiKey == null) {
            logger.warning("Cannot perform nutrition analysis - no API key configured");
            throw new ChatbotException("Nutrition analysis is not available. Please configure your OpenAI API key in ~/.tabemonpal/Database/.SECRET_KEY.xml to enable this feature.");
        }
        
        if (ingredients == null || ingredients.trim().isEmpty()) {
            throw new ChatbotException("Cannot analyze nutrition facts for empty ingredients list");
        }
        
        String ingredientsList = ingredients.replace("|", "\n");
        
        String prompt = "Do an prediction analysis from this recipe's ingredients for nutrition facts using USDA then send the result in xml called nutrition:\n\n" +
                "format:\n" +
                "<nutrition verdict=\"Healthy|Moderate|Unhealthy|Junk Food|Unknown\">\n" +
                "  <ingredient name=\"\" amount=\"\">\n" +
                "    <calories unit=\"kcal\"></calories>\n" +
                "    <protein unit=\"g\"></protein>\n" +
                "    <fat unit=\"g\"></fat>\n" +
                "    <carbohydrates unit=\"g\"></carbohydrates>\n" +
                "    <fiber unit=\"g\"></fiber>\n" +
                "    <sugar unit=\"g\"></sugar>\n" +
                "    <salt unit=\"mg\"></salt>\n" +
                "  </ingredient>\n" +
                "</nutrition>\n\n" +
                "Ingredients: " + ingredientsList;
        
        String systemMessage = "You are a nutrition analysis assistant. " +
                "Provide ONLY the XML nutrition data as requested, without any additional text or formatting. " +
                "Use USDA nutritional database for accurate values. " +
                "If exact amounts are not specified in ingredients, estimate reasonable serving sizes. " +
                "For the verdict attribute, analyze overall nutrition and classify as: " +
                "- 'Healthy': Low sugar/salt, high fiber/protein, balanced macros " +
                "- 'Moderate': Average nutritional balance " +
                "- 'Unhealthy': High sugar/salt/fat, low nutrients " +
                "- 'Junk Food': Very high sugar/fat/salt, minimal nutritional value " +
                "- 'Unknown': When nutritional analysis cannot be determined " +
                "Return only valid XML that matches the exact format requested.";
        
        try {
            String response = sendMessage(prompt, systemMessage);
            
            // Basic validation that we got something that looks like XML
            if (response == null || response.trim().isEmpty()) {
                throw new ChatbotException("Received empty response from nutrition analysis API");
            }
            
            // Log the response length for monitoring
            logger.info("Nutrition analysis API response length: " + response.length() + " characters");
            
            // Check for nutrition XML tags with more detailed error reporting
            boolean hasNutritionStart = response.contains("<nutrition");
            boolean hasNutritionEnd = response.contains("</nutrition>");
            
            if (!hasNutritionStart && !hasNutritionEnd) {
                throw new ChatbotException("API response does not contain nutrition XML tags. Response: " + response);
            } else if (!hasNutritionStart) {
                throw new ChatbotException("API response missing <nutrition> opening tag. Response: " + response);
            } else if (!hasNutritionEnd) {
                // Try to repair incomplete XML by adding closing tag
                logger.warning("API response missing closing tag, attempting to repair XML");
                String repairedResponse = response.trim();
                if (!repairedResponse.endsWith("</nutrition>")) {
                    // If the response looks like it was cut off, add the closing tag
                    if (repairedResponse.contains("<ingredient") && !repairedResponse.endsWith(">")) {
                        // Find the last complete ingredient and add closing tags
                        int lastCompleteIngredient = repairedResponse.lastIndexOf("</ingredient>");
                        if (lastCompleteIngredient > 0) {
                            repairedResponse = repairedResponse.substring(0, lastCompleteIngredient + "</ingredient>".length()) + "\n</nutrition>";
                            logger.info("Repaired XML response: " + repairedResponse);
                            return repairedResponse;
                        }
                    }
                    // Simple repair: just add the closing tag
                    repairedResponse += "\n</nutrition>";
                    logger.info("Repaired XML by adding closing tag: " + repairedResponse);
                    return repairedResponse;
                } else {
                    throw new ChatbotException("API response missing </nutrition> closing tag. Response: " + response);
                }
            }
            
            return response;
            
        } catch (ChatbotException e) {
            // Add more context to the error
            throw new ChatbotException("Nutrition analysis failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * Builds the JSON request body for the OpenAI API.
     */
    private String buildRequestBody(String userMessage, String systemMessage) {
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"model\": \"gpt-4o-mini\",");
        json.append("\"messages\": [");
        
        if (systemMessage != null && !systemMessage.trim().isEmpty()) {
            json.append("{\"role\": \"system\", \"content\": \"")
                .append(escapeJson(systemMessage))
                .append("\"},");
        }
        
        json.append("{\"role\": \"user\", \"content\": \"")
            .append(escapeJson(userMessage))
            .append("\"}");
        
        json.append("],");
        json.append("\"max_tokens\": 2000,");
        json.append("\"temperature\": 0.9");
        json.append("}");
        
        return json.toString();
    }
    
    /**
     * Parses the JSON response from OpenAI API.
     * Package-private for testing.
     */
    String parseResponse(String jsonResponse) throws ChatbotException {
        try {
            // OpenAI API response format: {"choices":[{"message":{"content":"..."}}]}
            // Look for the choices array first (more flexible with whitespace)
            String choicesPattern = "\"choices\"";
            int choicesIndex = jsonResponse.indexOf(choicesPattern);
            if (choicesIndex == -1) {
                throw new ChatbotException("Invalid response format - no choices array found");
            }
            
            // Find the opening bracket for choices array
            int arrayStart = jsonResponse.indexOf('[', choicesIndex);
            if (arrayStart == -1) {
                throw new ChatbotException("Invalid response format - choices array malformed");
            }
            
            // Find the first message content within choices
            String messageStart = "\"message\"";
            int messageIndex = jsonResponse.indexOf(messageStart, arrayStart);
            if (messageIndex == -1) {
                throw new ChatbotException("Invalid response format - no message object found");
            }
            
            // Find content field within the message (flexible with whitespace)
            String contentField = "\"content\"";
            int contentIndex = jsonResponse.indexOf(contentField, messageIndex);
            if (contentIndex == -1) {
                throw new ChatbotException("Invalid response format - no content field found");
            }
            
            // Find the colon and opening quote
            int colonIndex = jsonResponse.indexOf(':', contentIndex);
            if (colonIndex == -1) {
                throw new ChatbotException("Invalid response format - content field malformed");
            }
            
            // Skip whitespace after colon
            int quoteIndex = colonIndex + 1;
            while (quoteIndex < jsonResponse.length() && 
                   (jsonResponse.charAt(quoteIndex) == ' ' || jsonResponse.charAt(quoteIndex) == '\t' || 
                    jsonResponse.charAt(quoteIndex) == '\n' || jsonResponse.charAt(quoteIndex) == '\r')) {
                quoteIndex++;
            }
            
            // Expect opening quote
            if (quoteIndex >= jsonResponse.length() || jsonResponse.charAt(quoteIndex) != '"') {
                throw new ChatbotException("Invalid response format - content value not properly quoted");
            }
            
            int startIndex = quoteIndex + 1; // Skip the opening quote
            
            // Find the end of the content string, handling escaped quotes
            int endIndex = findEndOfJsonString(jsonResponse, startIndex);
            if (endIndex == -1) {
                throw new ChatbotException("Invalid response format - malformed content string");
            }
            
            String content = jsonResponse.substring(startIndex, endIndex);
            return unescapeJson(content);
            
        } catch (ChatbotException e) {
            throw e; // Re-throw our custom exceptions
        } catch (Exception e) {
            throw new ChatbotException("Failed to parse API response: " + e.getMessage() + 
                                     "\nResponse: " + (jsonResponse.length() > 500 ? 
                                     jsonResponse.substring(0, 500) + "..." : jsonResponse));
        }
    }
    
    /**
     * Finds the end of a JSON string value, properly handling escaped characters.
     */
    private int findEndOfJsonString(String json, int startIndex) {
        for (int i = startIndex; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"') {
                // Check if this quote is escaped
                int backslashCount = 0;
                int j = i - 1;
                while (j >= startIndex && json.charAt(j) == '\\') {
                    backslashCount++;
                    j--;
                }
                // If even number of backslashes (including 0), the quote is not escaped
                if (backslashCount % 2 == 0) {
                    return i;
                }
            }
        }
        return -1; // Not found
    }
    
    /**
     * Escapes JSON special characters.
     */
    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t");
    }
    
    /**
     * Unescapes JSON special characters.
     */
    private String unescapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\\"", "\"")
                  .replace("\\\\", "\\")
                  .replace("\\n", "\n")
                  .replace("\\r", "\r")
                  .replace("\\t", "\t");
    }
    
    /**
     * Checks if the user message contains specific Fumo keywords.
     *
     * @param message The user message to check
     * @return true if the message contains Fumo keywords, false otherwise
     */
    private boolean isFumoKeyword(String message) {
        if (message == null || message.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = message.trim();
        
        // Check for exact matches (case-insensitive and case-sensitive variants)
        return trimmed.equalsIgnoreCase("fumo") ||
               trimmed.equals("Fumo") ||
               trimmed.equals("FUMO") ||
               trimmed.equalsIgnoreCase("funa");
    }
    
    /**
     * Exception thrown when chatbot operations fail.
     */
    public static class ChatbotException extends Exception {
        public ChatbotException(String message) {
            super(message);
        }
        
        public ChatbotException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

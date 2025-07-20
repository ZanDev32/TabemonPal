package com.starlight.util;

import com.starlight.models.Nutrition;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Utility class for parsing nutrition XML data from AI responses.
 * Uses regex parsing instead of XStream due to the complexity of the XML structure.
 */
public class NutritionParser {
    private static final Logger logger = Logger.getLogger(NutritionParser.class.getName());
    
    /**
     * Parses nutrition XML from AI response and returns a Nutrition object.
     * 
     * @param aiResponse The full AI response containing XML
     * @return Parsed Nutrition object or fallback if parsing fails
     */
    public Nutrition parseNutritionFromResponse(String aiResponse) {
        if (aiResponse == null || aiResponse.trim().isEmpty()) {
            logger.warning("AI response is empty or null");
            return createFallbackNutrition();
        }
        
        try {
            // Extract XML from the response (AI might include extra text)
            String xmlContent = extractXmlFromResponse(aiResponse);
            if (xmlContent == null) {
                logger.warning("No valid XML found in AI response");
                return createFallbackNutrition();
            }
            
            // Parse the XML using regex
            Nutrition nutrition = parseXmlWithRegex(xmlContent);
            
            return nutrition != null ? nutrition : createFallbackNutrition();
            
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to parse nutrition XML: " + e.getMessage(), e);
            return createFallbackNutrition();
        }
    }
    
    /**
     * Extracts XML content from AI response.
     */
    private String extractXmlFromResponse(String response) {
        // Look for nutrition XML tags (with or without attributes)
        int startIndex = response.indexOf("<nutrition");
        int endIndex = response.lastIndexOf("</nutrition>");
        
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return response.substring(startIndex, endIndex + "</nutrition>".length());
        }
        
        // If no proper XML found, try to find any XML-like content
        startIndex = response.indexOf("<");
        endIndex = response.lastIndexOf(">");
        
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            String potentialXml = response.substring(startIndex, endIndex + 1);
            if (potentialXml.contains("<nutrition") && potentialXml.contains("</nutrition>")) {
                return potentialXml;
            }
        }
        
        return null;
    }
    
    /**
     * Parses nutrition XML using regex patterns.
     */
    private Nutrition parseXmlWithRegex(String xmlContent) {
        Nutrition nutrition = new Nutrition();
        
        // Parse verdict attribute from nutrition tag
        Pattern verdictPattern = Pattern.compile("<nutrition\\s+verdict=\"([^\"]*?)\"", Pattern.DOTALL);
        Matcher verdictMatcher = verdictPattern.matcher(xmlContent);
        if (verdictMatcher.find()) {
            String verdict = verdictMatcher.group(1).trim();
            // Validate verdict value
            if (verdict.equals("Healthy") || verdict.equals("Moderate") || 
                verdict.equals("Unhealthy") || verdict.equals("Junk Food")) {
                nutrition.verdict = verdict;
            } else {
                nutrition.verdict = "Moderate"; // Default fallback
            }
        }
        
        // Pattern to match ingredient blocks
        Pattern ingredientPattern = Pattern.compile(
            "<ingredient\\s+name=\"([^\"]*?)\"\\s+amount=\"([^\"]*?)\">(.*?)</ingredient>", 
            Pattern.DOTALL
        );
        
        Matcher ingredientMatcher = ingredientPattern.matcher(xmlContent);
        
        while (ingredientMatcher.find()) {
            String name = ingredientMatcher.group(1);
            String amount = ingredientMatcher.group(2);
            String content = ingredientMatcher.group(3);
            
            Nutrition.NutritionIngredient ingredient = new Nutrition.NutritionIngredient();
            ingredient.name = name;
            ingredient.amount = amount;
            
            // Parse nutrition values
            ingredient.calories = parseCaloriesValue(content, "calories", "kcal");
            ingredient.protein = parseProteinValue(content, "protein", "g");
            ingredient.fat = parseFatValue(content, "fat", "g");
            ingredient.carbohydrates = parseCarbohydratesValue(content, "carbohydrates", "g");
            ingredient.fiber = parseFiberValue(content, "fiber", "g");
            ingredient.sugar = parseSugarValue(content, "sugar", "g");
            ingredient.salt = parseSaltValue(content, "salt", "mg");
            
            nutrition.ingredient.add(ingredient);
        }
        
        return nutrition.ingredient.isEmpty() ? null : nutrition;
    }
    
    /**
     * Parses calories values from XML content.
     */
    private Nutrition.Calories parseCaloriesValue(String content, String tagName, String defaultUnit) {
        Pattern pattern = Pattern.compile("<" + tagName + "\\s+unit=\"([^\"]*?)\"[^>]*>([^<]*)</" + tagName + ">");
        Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            String unit = matcher.group(1);
            String value = matcher.group(2).trim();
            
            Nutrition.Calories calories = new Nutrition.Calories(validateNumericValue(value));
            calories.unit = unit;
            return calories;
        }
        
        return new Nutrition.Calories("0");
    }
    
    /**
     * Parses protein values from XML content.
     */
    private Nutrition.Protein parseProteinValue(String content, String tagName, String defaultUnit) {
        Pattern pattern = Pattern.compile("<" + tagName + "\\s+unit=\"([^\"]*?)\"[^>]*>([^<]*)</" + tagName + ">");
        Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            String unit = matcher.group(1);
            String value = matcher.group(2).trim();
            
            Nutrition.Protein protein = new Nutrition.Protein(validateNumericValue(value));
            protein.unit = unit;
            return protein;
        }
        
        return new Nutrition.Protein("0");
    }
    
    /**
     * Parses fat values from XML content.
     */
    private Nutrition.Fat parseFatValue(String content, String tagName, String defaultUnit) {
        Pattern pattern = Pattern.compile("<" + tagName + "\\s+unit=\"([^\"]*?)\"[^>]*>([^<]*)</" + tagName + ">");
        Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            String unit = matcher.group(1);
            String value = matcher.group(2).trim();
            
            Nutrition.Fat fat = new Nutrition.Fat(validateNumericValue(value));
            fat.unit = unit;
            return fat;
        }
        
        return new Nutrition.Fat("0");
    }
    
    /**
     * Parses carbohydrates values from XML content.
     */
    private Nutrition.Carbohydrates parseCarbohydratesValue(String content, String tagName, String defaultUnit) {
        Pattern pattern = Pattern.compile("<" + tagName + "\\s+unit=\"([^\"]*?)\"[^>]*>([^<]*)</" + tagName + ">");
        Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            String unit = matcher.group(1);
            String value = matcher.group(2).trim();
            
            Nutrition.Carbohydrates carbohydrates = new Nutrition.Carbohydrates(validateNumericValue(value));
            carbohydrates.unit = unit;
            return carbohydrates;
        }
        
        return new Nutrition.Carbohydrates("0");
    }
    
    /**
     * Parses fiber values from XML content.
     */
    private Nutrition.Fiber parseFiberValue(String content, String tagName, String defaultUnit) {
        Pattern pattern = Pattern.compile("<" + tagName + "\\s+unit=\"([^\"]*?)\"[^>]*>([^<]*)</" + tagName + ">");
        Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            String unit = matcher.group(1);
            String value = matcher.group(2).trim();
            
            Nutrition.Fiber fiber = new Nutrition.Fiber(validateNumericValue(value));
            fiber.unit = unit;
            return fiber;
        }
        
        return new Nutrition.Fiber("0");
    }
    
    /**
     * Parses sugar values from XML content.
     */
    private Nutrition.Sugar parseSugarValue(String content, String tagName, String defaultUnit) {
        Pattern pattern = Pattern.compile("<" + tagName + "\\s+unit=\"([^\"]*?)\"[^>]*>([^<]*)</" + tagName + ">");
        Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            String unit = matcher.group(1);
            String value = matcher.group(2).trim();
            
            Nutrition.Sugar sugar = new Nutrition.Sugar(validateNumericValue(value));
            sugar.unit = unit;
            return sugar;
        }
        
        return new Nutrition.Sugar("0");
    }
    
    /**
     * Parses salt values from XML content.
     */
    private Nutrition.Salt parseSaltValue(String content, String tagName, String defaultUnit) {
        Pattern pattern = Pattern.compile("<" + tagName + "\\s+unit=\"([^\"]*?)\"[^>]*>([^<]*)</" + tagName + ">");
        Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            String unit = matcher.group(1);
            String value = matcher.group(2).trim();
            
            Nutrition.Salt salt = new Nutrition.Salt(validateNumericValue(value));
            salt.unit = unit;
            return salt;
        }
        
        return new Nutrition.Salt("0");
    }
    
    /**
     * Validates and cleans numeric values.
     */
    private String validateNumericValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "0";
        }
        
        try {
            // Try to parse as double to validate
            Double.parseDouble(value.trim());
            return value.trim();
        } catch (NumberFormatException e) {
            return "0";
        }
    }
    
    /**
     * Creates a fallback nutrition object when parsing fails.
     */
    private Nutrition createFallbackNutrition() {
        Nutrition nutrition = new Nutrition();
        nutrition.verdict = "Moderate"; // Default verdict
        
        Nutrition.NutritionIngredient fallback = new Nutrition.NutritionIngredient();
        fallback.name = "Recipe";
        fallback.amount = "1 serving";
        fallback.calories = new Nutrition.Calories("0");
        fallback.protein = new Nutrition.Protein("0");
        fallback.fat = new Nutrition.Fat("0");
        fallback.carbohydrates = new Nutrition.Carbohydrates("0");
        fallback.fiber = new Nutrition.Fiber("0");
        fallback.sugar = new Nutrition.Sugar("0");
        fallback.salt = new Nutrition.Salt("0");
        
        nutrition.ingredient.add(fallback);
        return nutrition;
    }
}

package com.starlight.util;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.regex.Pattern;

import com.starlight.model.Nutrition;

import java.util.regex.Matcher;

/**
 * Utility class for parsing nutrition XML data from AI responses.
 * Uses regex parsing instead of XStream due to the complexity of the XML structure.
 */
public class NutritionParser {
    private static final Logger logger = Logger.getLogger(NutritionParser.class.getName());
    private static final String NUTRITION_END_TAG = "</nutrition>";
    private static final String UNKNOWN_VERDICT = "Unknown";
    // Reused fragment for matching unit and value inside a tag
    private static final String UNIT_VALUE_FRAGMENT = "\\s+unit=\"([^\"]*?)\"[^>]*>([^<]*)</";
    
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
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, () -> "Failed to parse nutrition XML: " + e.getMessage());
            }
            return createFallbackNutrition();
        }
    }
    
    /**
     * Extracts XML content from AI response.
     */
    private String extractXmlFromResponse(String response) {
        // Look for nutrition XML tags (with or without attributes)
        int startIndex = response.indexOf("<nutrition");
    int endIndex = response.lastIndexOf(NUTRITION_END_TAG);
        
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            return response.substring(startIndex, endIndex + NUTRITION_END_TAG.length());
        }
        
        // If no proper XML found, try to find any XML-like content
        startIndex = response.indexOf("<");
        endIndex = response.lastIndexOf(">");
        
        if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
            String potentialXml = response.substring(startIndex, endIndex + 1);
            if (potentialXml.contains("<nutrition") && potentialXml.contains(NUTRITION_END_TAG)) {
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
                verdict.equals("Unhealthy") || verdict.equals("Junk Food") || verdict.equals(UNKNOWN_VERDICT)) {
                nutrition.setVerdict(verdict);
            } else {
                nutrition.setVerdict(UNKNOWN_VERDICT); // Default fallback
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
            ingredient.setName(name);
            ingredient.setAmount(amount);

            // Parse nutrition values and copy into the ingredient's nested objects
            Nutrition.Calories cals = parseCaloriesValue(content, "calories");
            ingredient.getCalories().setValue(cals.getValue());
            ingredient.getCalories().setUnit(cals.getUnit());

            Nutrition.Protein prot = parseProteinValue(content, "protein");
            ingredient.getProtein().setValue(prot.getValue());
            ingredient.getProtein().setUnit(prot.getUnit());

            Nutrition.Fat fat = parseFatValue(content, "fat");
            ingredient.getFat().setValue(fat.getValue());
            ingredient.getFat().setUnit(fat.getUnit());

            Nutrition.Carbohydrates carbs = parseCarbohydratesValue(content, "carbohydrates");
            ingredient.getCarbohydrates().setValue(carbs.getValue());
            ingredient.getCarbohydrates().setUnit(carbs.getUnit());

            Nutrition.Fiber fiber = parseFiberValue(content, "fiber");
            ingredient.getFiber().setValue(fiber.getValue());
            ingredient.getFiber().setUnit(fiber.getUnit());

            Nutrition.Sugar sugar = parseSugarValue(content, "sugar");
            ingredient.getSugar().setValue(sugar.getValue());
            ingredient.getSugar().setUnit(sugar.getUnit());

            Nutrition.Salt salt = parseSaltValue(content, "salt");
            ingredient.getSalt().setValue(salt.getValue());
            ingredient.getSalt().setUnit(salt.getUnit());

            nutrition.getIngredient().add(ingredient);
        }
        
        return nutrition.getIngredient().isEmpty() ? null : nutrition;
    }
    
    /**
     * Parses calories values from XML content.
     */
    private Nutrition.Calories parseCaloriesValue(String content, String tagName) {
        Pattern pattern = Pattern.compile("<" + tagName + UNIT_VALUE_FRAGMENT + tagName + ">");
        Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            String unit = matcher.group(1);
            String value = matcher.group(2).trim();
            
            Nutrition.Calories calories = new Nutrition.Calories(validateNumericValue(value));
            calories.setUnit(unit);
            return calories;
        }
        
    return new Nutrition.Calories("0");
    }
    
    /**
     * Parses protein values from XML content.
     */
    private Nutrition.Protein parseProteinValue(String content, String tagName) {
        Pattern pattern = Pattern.compile("<" + tagName + UNIT_VALUE_FRAGMENT + tagName + ">");
        Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            String unit = matcher.group(1);
            String value = matcher.group(2).trim();
            
            Nutrition.Protein protein = new Nutrition.Protein(validateNumericValue(value));
            protein.setUnit(unit);
            return protein;
        }
        
    return new Nutrition.Protein("0");
    }
    
    /**
     * Parses fat values from XML content.
     */
    private Nutrition.Fat parseFatValue(String content, String tagName) {
        Pattern pattern = Pattern.compile("<" + tagName + UNIT_VALUE_FRAGMENT + tagName + ">");
        Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            String unit = matcher.group(1);
            String value = matcher.group(2).trim();
            
            Nutrition.Fat fat = new Nutrition.Fat(validateNumericValue(value));
            fat.setUnit(unit);
            return fat;
        }
        
    return new Nutrition.Fat("0");
    }
    
    /**
     * Parses carbohydrates values from XML content.
     */
    private Nutrition.Carbohydrates parseCarbohydratesValue(String content, String tagName) {
        Pattern pattern = Pattern.compile("<" + tagName + UNIT_VALUE_FRAGMENT + tagName + ">");
        Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            String unit = matcher.group(1);
            String value = matcher.group(2).trim();
            
            Nutrition.Carbohydrates carbohydrates = new Nutrition.Carbohydrates(validateNumericValue(value));
            carbohydrates.setUnit(unit);
            return carbohydrates;
        }
        
    return new Nutrition.Carbohydrates("0");
    }
    
    /**
     * Parses fiber values from XML content.
     */
    private Nutrition.Fiber parseFiberValue(String content, String tagName) {
        Pattern pattern = Pattern.compile("<" + tagName + UNIT_VALUE_FRAGMENT + tagName + ">");
        Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            String unit = matcher.group(1);
            String value = matcher.group(2).trim();
            
            Nutrition.Fiber fiber = new Nutrition.Fiber(validateNumericValue(value));
            fiber.setUnit(unit);
            return fiber;
        }
        
    return new Nutrition.Fiber("0");
    }
    
    /**
     * Parses sugar values from XML content.
     */
    private Nutrition.Sugar parseSugarValue(String content, String tagName) {
        Pattern pattern = Pattern.compile("<" + tagName + UNIT_VALUE_FRAGMENT + tagName + ">");
        Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            String unit = matcher.group(1);
            String value = matcher.group(2).trim();
            
            Nutrition.Sugar sugar = new Nutrition.Sugar(validateNumericValue(value));
            sugar.setUnit(unit);
            return sugar;
        }
        
    return new Nutrition.Sugar("0");
    }
    
    /**
     * Parses salt values from XML content.
     */
    private Nutrition.Salt parseSaltValue(String content, String tagName) {
        Pattern pattern = Pattern.compile("<" + tagName + UNIT_VALUE_FRAGMENT + tagName + ">");
        Matcher matcher = pattern.matcher(content);
        
        if (matcher.find()) {
            String unit = matcher.group(1);
            String value = matcher.group(2).trim();
            
            Nutrition.Salt salt = new Nutrition.Salt(validateNumericValue(value));
            salt.setUnit(unit);
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
    nutrition.setVerdict(UNKNOWN_VERDICT); // Default verdict for unanalyzed recipes

    Nutrition.NutritionIngredient fallback = new Nutrition.NutritionIngredient();
    fallback.setName("Recipe");
    fallback.setAmount("1 serving");
    // Set nested numeric defaults
    fallback.getCalories().setValue("0");
    fallback.getProtein().setValue("0");
    fallback.getFat().setValue("0");
    fallback.getCarbohydrates().setValue("0");
    fallback.getFiber().setValue("0");
    fallback.getSugar().setValue("0");
    fallback.getSalt().setValue("0");

    nutrition.getIngredient().add(fallback);
    return nutrition;
    }
}

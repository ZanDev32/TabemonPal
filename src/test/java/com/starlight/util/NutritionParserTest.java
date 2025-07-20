package com.starlight.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.starlight.models.Nutrition;

/**
 * Unit tests for the NutritionParser utility class.
 */
public class NutritionParserTest {

    private NutritionParser parser;

    @BeforeEach
    void setUp() {
        parser = new NutritionParser();
    }

    @Test
    void testParseValidNutritionXML() {
        String aiResponse = "Here's the nutrition analysis:\n\n" +
                "<nutrition verdict=\"Healthy\">\n" +
                "  <ingredient name=\"Chicken Breast\" amount=\"200g\">\n" +
                "    <calories unit=\"kcal\">331</calories>\n" +
                "    <protein unit=\"g\">62.2</protein>\n" +
                "    <fat unit=\"g\">7.2</fat>\n" +
                "    <carbohydrates unit=\"g\">0</carbohydrates>\n" +
                "    <fiber unit=\"g\">0</fiber>\n" +
                "    <sugar unit=\"g\">0</sugar>\n" +
                "    <salt unit=\"mg\">146</salt>\n" +
                "  </ingredient>\n" +
                "  <ingredient name=\"Rice\" amount=\"1 cup cooked\">\n" +
                "    <calories unit=\"kcal\">205</calories>\n" +
                "    <protein unit=\"g\">4.2</protein>\n" +
                "    <fat unit=\"g\">0.4</fat>\n" +
                "    <carbohydrates unit=\"g\">45</carbohydrates>\n" +
                "    <fiber unit=\"g\">0.6</fiber>\n" +
                "    <sugar unit=\"g\">0.1</sugar>\n" +
                "    <salt unit=\"mg\">2</salt>\n" +
                "  </ingredient>\n" +
                "</nutrition>\n\n" +
                "This provides a balanced meal with good protein content.";

        Nutrition nutrition = parser.parseNutritionFromResponse(aiResponse);

        assertNotNull(nutrition, "Nutrition object should not be null");
        assertNotNull(nutrition.ingredient, "Ingredient list should not be null");
        assertEquals(2, nutrition.ingredient.size(), "Should have 2 ingredients");
        assertEquals("Healthy", nutrition.verdict, "Should have correct verdict");

        // Check first ingredient
        Nutrition.NutritionIngredient chicken = nutrition.ingredient.get(0);
        assertEquals("Chicken Breast", chicken.name);
        assertEquals("200g", chicken.amount);
        assertEquals("331", chicken.calories.value);
        assertEquals("62.2", chicken.protein.value);
        assertEquals("7.2", chicken.fat.value);
        assertEquals("0", chicken.carbohydrates.value);
        assertEquals("0", chicken.fiber.value);
        assertEquals("0", chicken.sugar.value);
        assertEquals("146", chicken.salt.value);

        // Check second ingredient
        Nutrition.NutritionIngredient rice = nutrition.ingredient.get(1);
        assertEquals("Rice", rice.name);
        assertEquals("1 cup cooked", rice.amount);
        assertEquals("205", rice.calories.value);
        assertEquals("4.2", rice.protein.value);
        assertEquals("0.4", rice.fat.value);
        assertEquals("45", rice.carbohydrates.value);
        assertEquals("0.6", rice.fiber.value);
        assertEquals("0.1", rice.sugar.value);
        assertEquals("2", rice.salt.value);

        // Test totals
        assertEquals(536.0, nutrition.getTotalCalories(), 0.1);
        assertEquals(66.4, nutrition.getTotalProtein(), 0.1);
        assertEquals(7.6, nutrition.getTotalFat(), 0.1);
        assertEquals(45.0, nutrition.getTotalCarbohydrates(), 0.1);
        assertEquals(0.6, nutrition.getTotalFiber(), 0.1);
        assertEquals(0.1, nutrition.getTotalSugar(), 0.1);
        assertEquals(148.0, nutrition.getTotalSalt(), 0.1);
    }

    @Test
    void testParseInvalidResponse() {
        String invalidResponse = "Sorry, I couldn't analyze the nutrition facts.";

        Nutrition nutrition = parser.parseNutritionFromResponse(invalidResponse);

        assertNotNull(nutrition, "Should return fallback nutrition object");
        assertNotNull(nutrition.ingredient, "Should have fallback ingredient");
        assertEquals(1, nutrition.ingredient.size(), "Should have one fallback ingredient");
        assertEquals("Recipe", nutrition.ingredient.get(0).name);
        assertEquals("Unknown", nutrition.verdict, "Should have Unknown verdict for fallback");
    }

    @Test
    void testParseNullResponse() {
        Nutrition nutrition = parser.parseNutritionFromResponse(null);

        assertNotNull(nutrition, "Should return fallback nutrition object");
        assertNotNull(nutrition.ingredient, "Should have fallback ingredient");
        assertEquals(1, nutrition.ingredient.size(), "Should have one fallback ingredient");
    }

    @Test
    void testParseEmptyResponse() {
        Nutrition nutrition = parser.parseNutritionFromResponse("");

        assertNotNull(nutrition, "Should return fallback nutrition object");
        assertNotNull(nutrition.ingredient, "Should have fallback ingredient");
        assertEquals(1, nutrition.ingredient.size(), "Should have one fallback ingredient");
    }

    @Test
    void testParseMalformedXML() {
        String malformedResponse = "<nutrition>\n" +
                "  <ingredient name=\"Apple\">\n" +
                "    <calories>52</calories>\n" +
                "    <!-- Missing closing tag for ingredient -->\n" +
                "</nutrition>";

        Nutrition nutrition = parser.parseNutritionFromResponse(malformedResponse);

        assertNotNull(nutrition, "Should return fallback nutrition object on parsing error");
    }

    @Test
    void testParseUnknownVerdict() {
        String aiResponse = "Here's the nutrition analysis:\n\n" +
                "<nutrition verdict=\"Unknown\">\n" +
                "  <ingredient name=\"Mystery Ingredient\" amount=\"100g\">\n" +
                "    <calories unit=\"kcal\">100</calories>\n" +
                "    <protein unit=\"g\">5</protein>\n" +
                "    <fat unit=\"g\">3</fat>\n" +
                "    <carbohydrates unit=\"g\">15</carbohydrates>\n" +
                "    <fiber unit=\"g\">2</fiber>\n" +
                "    <sugar unit=\"g\">8</sugar>\n" +
                "    <salt unit=\"mg\">100</salt>\n" +
                "  </ingredient>\n" +
                "</nutrition>\n\n" +
                "Nutritional analysis could not be fully determined.";

        Nutrition nutrition = parser.parseNutritionFromResponse(aiResponse);

        assertNotNull(nutrition, "Nutrition object should not be null");
        assertEquals("Unknown", nutrition.verdict, "Should have Unknown verdict");
        assertEquals(1, nutrition.ingredient.size(), "Should have 1 ingredient");
        assertEquals("Mystery Ingredient", nutrition.ingredient.get(0).name);
    }
}

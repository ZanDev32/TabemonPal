package com.starlight.util;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.starlight.model.Nutrition;

/**
 * Unit tests for the NutritionParser utility class.
 */
class NutritionParserTest {

    private NutritionParser parser;

    @BeforeEach
    void setUp() {
        parser = new NutritionParser();
    }

    @Test
    void testParseValidNutritionXML() {
                String aiResponse = """
                                Here's the nutrition analysis:

                                <nutrition verdict="Healthy">
                                    <ingredient name="Chicken Breast" amount="200g">
                                        <calories unit="kcal">331</calories>
                                        <protein unit="g">62.2</protein>
                                        <fat unit="g">7.2</fat>
                                        <carbohydrates unit="g">0</carbohydrates>
                                        <fiber unit="g">0</fiber>
                                        <sugar unit="g">0</sugar>
                                        <salt unit="mg">146</salt>
                                    </ingredient>
                                    <ingredient name="Rice" amount="1 cup cooked">
                                        <calories unit="kcal">205</calories>
                                        <protein unit="g">4.2</protein>
                                        <fat unit="g">0.4</fat>
                                        <carbohydrates unit="g">45</carbohydrates>
                                        <fiber unit="g">0.6</fiber>
                                        <sugar unit="g">0.1</sugar>
                                        <salt unit="mg">2</salt>
                                    </ingredient>
                                </nutrition>

                                This provides a balanced meal with good protein content.
                                """;

        Nutrition nutrition = parser.parseNutritionFromResponse(aiResponse);
    assertNotNull(nutrition, "Nutrition object should not be null");
    assertNotNull(nutrition.getIngredient(), "Ingredient list should not be null");
    assertEquals(2, nutrition.getIngredient().size(), "Should have 2 ingredients");
    assertEquals("Healthy", nutrition.getVerdict(), "Should have correct verdict");

    // Ingredient assertions moved to helper methods to reduce assertion count in this test method
    assertIngredient(
        nutrition.getIngredient().get(0),
        "Chicken Breast", "200g", "331", "62.2", "7.2", "0", "0", "0", "146");
    assertIngredient(
        nutrition.getIngredient().get(1),
        "Rice", "1 cup cooked", "205", "4.2", "0.4", "45", "0.6", "0.1", "2");

    assertTotals(nutrition, 536.0, 66.4, 7.6, 45.0, 0.6, 0.1, 148.0);
    }

    @Test
    void testParseInvalidResponse() {
        String invalidResponse = "Sorry, I couldn't analyze the nutrition facts.";

        Nutrition nutrition = parser.parseNutritionFromResponse(invalidResponse);

    assertNotNull(nutrition, "Should return fallback nutrition object");
    assertNotNull(nutrition.getIngredient(), "Should have fallback ingredient");
    assertEquals(1, nutrition.getIngredient().size(), "Should have one fallback ingredient");
    assertEquals("Recipe", nutrition.getIngredient().get(0).getName());
    assertEquals("Unknown", nutrition.getVerdict(), "Should have Unknown verdict for fallback");
    }

    @Test
    void testParseNullResponse() {
        Nutrition nutrition = parser.parseNutritionFromResponse(null);

    assertNotNull(nutrition, "Should return fallback nutrition object");
    assertNotNull(nutrition.getIngredient(), "Should have fallback ingredient");
    assertEquals(1, nutrition.getIngredient().size(), "Should have one fallback ingredient");
    }

    @Test
    void testParseEmptyResponse() {
        Nutrition nutrition = parser.parseNutritionFromResponse("");

        assertNotNull(nutrition, "Should return fallback nutrition object");
    assertNotNull(nutrition.getIngredient(), "Should have fallback ingredient");
    assertEquals(1, nutrition.getIngredient().size(), "Should have one fallback ingredient");
    }

    @Test
    void testParseMalformedXML() {
                String malformedResponse = """
                                <nutrition>
                                    <ingredient name="Apple">
                                        <calories>52</calories>
                                        <!-- Missing closing tag for ingredient -->
                                </nutrition>
                                """;

        Nutrition nutrition = parser.parseNutritionFromResponse(malformedResponse);

        assertNotNull(nutrition, "Should return fallback nutrition object on parsing error");
    }

    @Test
    void testParseUnknownVerdict() {
                String aiResponse = """
                                Here's the nutrition analysis:

                                <nutrition verdict="Unknown">
                                    <ingredient name="Mystery Ingredient" amount="100g">
                                        <calories unit="kcal">100</calories>
                                        <protein unit="g">5</protein>
                                        <fat unit="g">3</fat>
                                        <carbohydrates unit="g">15</carbohydrates>
                                        <fiber unit="g">2</fiber>
                                        <sugar unit="g">8</sugar>
                                        <salt unit="mg">100</salt>
                                    </ingredient>
                                </nutrition>

                                Nutritional analysis could not be fully determined.
                                """;

        Nutrition nutrition = parser.parseNutritionFromResponse(aiResponse);

    assertNotNull(nutrition, "Nutrition object should not be null");
    assertEquals("Unknown", nutrition.getVerdict(), "Should have Unknown verdict");
    assertEquals(1, nutrition.getIngredient().size(), "Should have 1 ingredient");
    assertEquals("Mystery Ingredient", nutrition.getIngredient().get(0).getName());
    }

    // Helper methods to keep individual test readable while reducing per-method assertion counts
    private static void assertIngredient(Nutrition.NutritionIngredient ingredient,
                                         String expectedName,
                                         String expectedAmount,
                                         String expectedCalories,
                                         String expectedProtein,
                                         String expectedFat,
                                         String expectedCarbs,
                                         String expectedFiber,
                                         String expectedSugar,
                                         String expectedSalt) {
    assertEquals(expectedName, ingredient.getName());
    assertEquals(expectedAmount, ingredient.getAmount());
    assertEquals(expectedCalories, ingredient.getCalories().getValue());
    assertEquals(expectedProtein, ingredient.getProtein().getValue());
    assertEquals(expectedFat, ingredient.getFat().getValue());
    assertEquals(expectedCarbs, ingredient.getCarbohydrates().getValue());
    assertEquals(expectedFiber, ingredient.getFiber().getValue());
    assertEquals(expectedSugar, ingredient.getSugar().getValue());
    assertEquals(expectedSalt, ingredient.getSalt().getValue());
    }

    private static void assertTotals(Nutrition nutrition,
                                     double calories,
                                     double protein,
                                     double fat,
                                     double carbs,
                                     double fiber,
                                     double sugar,
                                     double salt) {
        assertEquals(calories, nutrition.getTotalCalories(), 0.1);
        assertEquals(protein, nutrition.getTotalProtein(), 0.1);
        assertEquals(fat, nutrition.getTotalFat(), 0.1);
        assertEquals(carbs, nutrition.getTotalCarbohydrates(), 0.1);
        assertEquals(fiber, nutrition.getTotalFiber(), 0.1);
        assertEquals(sugar, nutrition.getTotalSugar(), 0.1);
        assertEquals(salt, nutrition.getTotalSalt(), 0.1);
    }
}

package com.starlight.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Model for nutrition facts data from AI analysis.
 */
public class Nutrition {
    public static final String VERDICT_UNKNOWN = "Unknown";
    public static final String UNIT_CALORIES = "kcal";
    public static final String UNIT_GRAMS = "g";
    public static final String UNIT_MILLIGRAMS = "mg";
    public static final String DEFAULT_NUMERIC = "0";

    private final List<NutritionIngredient> ingredient = new ArrayList<>();
    /** Recipe health verdict: Healthy, Moderate, Unhealthy, Junk Food, or Unknown */
    private String verdict = VERDICT_UNKNOWN;

    public List<NutritionIngredient> getIngredient() { return ingredient; }
    public String getVerdict() { return verdict; }
    public void setVerdict(String verdict) { this.verdict = verdict; }
    
    /**
     * Individual ingredient nutrition information.
     */
    public static class NutritionIngredient {
    private String name;
    private String amount;
    private Calories calories = new Calories();
    private Protein protein = new Protein();
    private Fat fat = new Fat();
    private Carbohydrates carbohydrates = new Carbohydrates();
    private Fiber fiber = new Fiber();
    private Sugar sugar = new Sugar();
    private Salt salt = new Salt();

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAmount() { return amount; }
    public void setAmount(String amount) { this.amount = amount; }
    public Calories getCalories() { return calories; }
    public Protein getProtein() { return protein; }
    public Fat getFat() { return fat; }
    public Carbohydrates getCarbohydrates() { return carbohydrates; }
    public Fiber getFiber() { return fiber; }
    public Sugar getSugar() { return sugar; }
    public Salt getSalt() { return salt; }
    }
    
    /**
     * Calories information with unit.
     */
    public static class Calories {
    private String unit = UNIT_CALORIES;
    private String value = DEFAULT_NUMERIC;
        
        public Calories() {}
        
        public Calories(String value) {
            this.value = value;
        }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    }
    
    /**
     * Protein information with unit.
     */
    public static class Protein {
    private String unit = UNIT_GRAMS;
    private String value = DEFAULT_NUMERIC;
        
        public Protein() {}
        
        public Protein(String value) {
            this.value = value;
        }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    }
    
    /**
     * Fat information with unit.
     */
    public static class Fat {
    private String unit = UNIT_GRAMS;
    private String value = DEFAULT_NUMERIC;
        
        public Fat() {}
        
        public Fat(String value) {
            this.value = value;
        }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    }
    
    /**
     * Carbohydrates information with unit.
     */
    public static class Carbohydrates {
    private String unit = UNIT_GRAMS;
    private String value = DEFAULT_NUMERIC;
        
        public Carbohydrates() {}
        
        public Carbohydrates(String value) {
            this.value = value;
        }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    }
    
    /**
     * Fiber information with unit.
     */
    public static class Fiber {
    private String unit = UNIT_GRAMS;
    private String value = DEFAULT_NUMERIC;
        
        public Fiber() {}
        
        public Fiber(String value) {
            this.value = value;
        }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    }
    
    /**
     * Sugar information with unit.
     */
    public static class Sugar {
    private String unit = UNIT_GRAMS;
    private String value = DEFAULT_NUMERIC;
        
        public Sugar() {}
        
        public Sugar(String value) {
            this.value = value;
        }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    }
    
    /**
     * Salt information with unit.
     */
    public static class Salt {
    private String unit = UNIT_MILLIGRAMS;
    private String value = DEFAULT_NUMERIC;
        
        public Salt() {}
        
        public Salt(String value) {
            this.value = value;
        }
    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    }
    
    /**
     * Gets total calories from all ingredients.
     */
    public double getTotalCalories() {
        return ingredient.stream()
                .mapToDouble(i -> {
                    try {
                        return Double.parseDouble(i.calories.value);
                    } catch (NumberFormatException e) {
                        return 0.0;
                    }
                })
                .sum();
    }
    
    /**
     * Gets total protein from all ingredients.
     */
    public double getTotalProtein() {
        return ingredient.stream()
                .mapToDouble(i -> {
                    try {
                        return Double.parseDouble(i.protein.value);
                    } catch (NumberFormatException e) {
                        return 0.0;
                    }
                })
                .sum();
    }
    
    /**
     * Gets total fat from all ingredients.
     */
    public double getTotalFat() {
        return ingredient.stream()
                .mapToDouble(i -> {
                    try {
                        return Double.parseDouble(i.fat.value);
                    } catch (NumberFormatException e) {
                        return 0.0;
                    }
                })
                .sum();
    }
    
    /**
     * Gets total carbohydrates from all ingredients.
     */
    public double getTotalCarbohydrates() {
        return ingredient.stream()
                .mapToDouble(i -> {
                    try {
                        return Double.parseDouble(i.carbohydrates.value);
                    } catch (NumberFormatException e) {
                        return 0.0;
                    }
                })
                .sum();
    }
    
    /**
     * Gets total fiber from all ingredients.
     */
    public double getTotalFiber() {
        return ingredient.stream()
                .mapToDouble(i -> {
                    try {
                        return Double.parseDouble(i.fiber.value);
                    } catch (NumberFormatException e) {
                        return 0.0;
                    }
                })
                .sum();
    }
    
    /**
     * Gets total sugar from all ingredients.
     */
    public double getTotalSugar() {
        return ingredient.stream()
                .mapToDouble(i -> {
                    try {
                        return Double.parseDouble(i.sugar.value);
                    } catch (NumberFormatException e) {
                        return 0.0;
                    }
                })
                .sum();
    }
    
    /**
     * Gets total salt from all ingredients.
     */
    public double getTotalSalt() {
        return ingredient.stream()
                .mapToDouble(i -> {
                    try {
                        return Double.parseDouble(i.salt.value);
                    } catch (NumberFormatException e) {
                        return 0.0;
                    }
                })
                .sum();
    }
}

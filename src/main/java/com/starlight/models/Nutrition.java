package com.starlight.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Model for nutrition facts data from AI analysis.
 */
public class Nutrition {
    public List<NutritionIngredient> ingredient = new ArrayList<>();
    /** Recipe health verdict: Healthy, Moderate, Unhealthy, Junk Food, or Unknown */
    public String verdict = "Unknown";
    
    /**
     * Individual ingredient nutrition information.
     */
    public static class NutritionIngredient {
        public String name;
        public String amount;
        public Calories calories = new Calories();
        public Protein protein = new Protein();
        public Fat fat = new Fat();
        public Carbohydrates carbohydrates = new Carbohydrates();
        public Fiber fiber = new Fiber();
        public Sugar sugar = new Sugar();
        public Salt salt = new Salt();
    }
    
    /**
     * Calories information with unit.
     */
    public static class Calories {
        public String unit = "kcal";
        public String value = "0";
        
        public Calories() {}
        
        public Calories(String value) {
            this.value = value;
        }
    }
    
    /**
     * Protein information with unit.
     */
    public static class Protein {
        public String unit = "g";
        public String value = "0";
        
        public Protein() {}
        
        public Protein(String value) {
            this.value = value;
        }
    }
    
    /**
     * Fat information with unit.
     */
    public static class Fat {
        public String unit = "g";
        public String value = "0";
        
        public Fat() {}
        
        public Fat(String value) {
            this.value = value;
        }
    }
    
    /**
     * Carbohydrates information with unit.
     */
    public static class Carbohydrates {
        public String unit = "g";
        public String value = "0";
        
        public Carbohydrates() {}
        
        public Carbohydrates(String value) {
            this.value = value;
        }
    }
    
    /**
     * Fiber information with unit.
     */
    public static class Fiber {
        public String unit = "g";
        public String value = "0";
        
        public Fiber() {}
        
        public Fiber(String value) {
            this.value = value;
        }
    }
    
    /**
     * Sugar information with unit.
     */
    public static class Sugar {
        public String unit = "g";
        public String value = "0";
        
        public Sugar() {}
        
        public Sugar(String value) {
            this.value = value;
        }
    }
    
    /**
     * Salt information with unit.
     */
    public static class Salt {
        public String unit = "mg";
        public String value = "0";
        
        public Salt() {}
        
        public Salt(String value) {
            this.value = value;
        }
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

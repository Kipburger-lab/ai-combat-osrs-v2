package database.models;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

/**
 * Represents a consumable item in OSRS (food, potions, etc.)
 */
public class Consumable {
    // Basic item information
    private int itemId;
    private String name;
    private String description;
    private ConsumableType type;
    
    // Economic properties
    private int value; // GE value
    private boolean tradeable;
    private boolean stackable;
    private boolean noted;
    
    // Consumption properties
    private int healAmount; // HP restored
    private int consumeTime; // ticks to consume
    private boolean edible;
    private boolean drinkable;
    
    // Effects and bonuses
    private List<Effect> effects;
    private Map<String, Integer> statBoosts; // stat name -> boost amount
    private Map<String, Integer> statReductions; // stat name -> reduction amount
    
    // Special properties
    private boolean poisonCure;
    private boolean diseaseImmunity;
    private boolean prayerRestore;
    private int prayerPoints;
    private boolean energyRestore;
    private int energyPoints;
    private boolean runEnergyRestore;
    private int runEnergyPoints;
    
    // Combat related
    private boolean combatPotion;
    private boolean antifire;
    private boolean antipoison;
    private boolean superPotion;
    private int duration; // effect duration in ticks
    
    // Requirements
    private Map<String, Integer> levelRequirements;
    private List<String> questRequirements;
    
    // Cooking/Herblore properties
    private boolean cookable;
    private int cookingLevel;
    private int cookingXp;
    private boolean brewable;
    private int herbloreLevel;
    private int herbloreXp;
    private List<Integer> ingredients; // item IDs
    
    public Consumable() {
        this.effects = new ArrayList<>();
        this.statBoosts = new HashMap<>();
        this.statReductions = new HashMap<>();
        this.levelRequirements = new HashMap<>();
        this.questRequirements = new ArrayList<>();
        this.ingredients = new ArrayList<>();
    }
    
    public Consumable(int itemId, String name, ConsumableType type) {
        this();
        this.itemId = itemId;
        this.name = name;
        this.type = type;
    }
    
    // Getters and setters
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public ConsumableType getType() { return type; }
    public void setType(ConsumableType type) { this.type = type; }
    
    public int getValue() { return value; }
    public void setValue(int value) { this.value = Math.max(0, value); }
    
    public boolean isTradeable() { return tradeable; }
    public void setTradeable(boolean tradeable) { this.tradeable = tradeable; }
    
    public boolean isStackable() { return stackable; }
    public void setStackable(boolean stackable) { this.stackable = stackable; }
    
    public boolean isNoted() { return noted; }
    public void setNoted(boolean noted) { this.noted = noted; }
    
    public int getHealAmount() { return healAmount; }
    public void setHealAmount(int healAmount) { this.healAmount = Math.max(0, healAmount); }
    
    public int getConsumeTime() { return consumeTime; }
    public void setConsumeTime(int consumeTime) { this.consumeTime = Math.max(1, consumeTime); }
    
    public boolean isEdible() { return edible; }
    public void setEdible(boolean edible) { this.edible = edible; }
    
    public boolean isDrinkable() { return drinkable; }
    public void setDrinkable(boolean drinkable) { this.drinkable = drinkable; }
    
    public List<Effect> getEffects() { return effects; }
    public void setEffects(List<Effect> effects) { 
        this.effects = effects != null ? effects : new ArrayList<>(); 
    }
    
    public Map<String, Integer> getStatBoosts() { return statBoosts; }
    public void setStatBoosts(Map<String, Integer> statBoosts) { 
        this.statBoosts = statBoosts != null ? statBoosts : new HashMap<>(); 
    }
    
    public Map<String, Integer> getStatReductions() { return statReductions; }
    public void setStatReductions(Map<String, Integer> statReductions) { 
        this.statReductions = statReductions != null ? statReductions : new HashMap<>(); 
    }
    
    public boolean isPoisonCure() { return poisonCure; }
    public void setPoisonCure(boolean poisonCure) { this.poisonCure = poisonCure; }
    
    public boolean isDiseaseImmunity() { return diseaseImmunity; }
    public void setDiseaseImmunity(boolean diseaseImmunity) { this.diseaseImmunity = diseaseImmunity; }
    
    public boolean isPrayerRestore() { return prayerRestore; }
    public void setPrayerRestore(boolean prayerRestore) { this.prayerRestore = prayerRestore; }
    
    public int getPrayerPoints() { return prayerPoints; }
    public void setPrayerPoints(int prayerPoints) { this.prayerPoints = Math.max(0, prayerPoints); }
    
    public boolean isEnergyRestore() { return energyRestore; }
    public void setEnergyRestore(boolean energyRestore) { this.energyRestore = energyRestore; }
    
    public int getEnergyPoints() { return energyPoints; }
    public void setEnergyPoints(int energyPoints) { this.energyPoints = Math.max(0, energyPoints); }
    
    public boolean isRunEnergyRestore() { return runEnergyRestore; }
    public void setRunEnergyRestore(boolean runEnergyRestore) { this.runEnergyRestore = runEnergyRestore; }
    
    public int getRunEnergyPoints() { return runEnergyPoints; }
    public void setRunEnergyPoints(int runEnergyPoints) { this.runEnergyPoints = Math.max(0, runEnergyPoints); }
    
    public boolean isCombatPotion() { return combatPotion; }
    public void setCombatPotion(boolean combatPotion) { this.combatPotion = combatPotion; }
    
    public boolean isAntifire() { return antifire; }
    public void setAntifire(boolean antifire) { this.antifire = antifire; }
    
    public boolean isAntipoison() { return antipoison; }
    public void setAntipoison(boolean antipoison) { this.antipoison = antipoison; }
    
    public boolean isSuperPotion() { return superPotion; }
    public void setSuperPotion(boolean superPotion) { this.superPotion = superPotion; }
    
    public int getDuration() { return duration; }
    public void setDuration(int duration) { this.duration = Math.max(0, duration); }
    
    public Map<String, Integer> getLevelRequirements() { return levelRequirements; }
    public void setLevelRequirements(Map<String, Integer> levelRequirements) { 
        this.levelRequirements = levelRequirements != null ? levelRequirements : new HashMap<>(); 
    }
    
    public List<String> getQuestRequirements() { return questRequirements; }
    public void setQuestRequirements(List<String> questRequirements) { 
        this.questRequirements = questRequirements != null ? questRequirements : new ArrayList<>(); 
    }
    
    public boolean isCookable() { return cookable; }
    public void setCookable(boolean cookable) { this.cookable = cookable; }
    
    public int getCookingLevel() { return cookingLevel; }
    public void setCookingLevel(int cookingLevel) { this.cookingLevel = Math.max(0, cookingLevel); }
    
    public int getCookingXp() { return cookingXp; }
    public void setCookingXp(int cookingXp) { this.cookingXp = Math.max(0, cookingXp); }
    
    public boolean isBrewable() { return brewable; }
    public void setBrewable(boolean brewable) { this.brewable = brewable; }
    
    public int getHerbloreLevel() { return herbloreLevel; }
    public void setHerbloreLevel(int herbloreLevel) { this.herbloreLevel = Math.max(0, herbloreLevel); }
    
    public int getHerbloreXp() { return herbloreXp; }
    public void setHerbloreXp(int herbloreXp) { this.herbloreXp = Math.max(0, herbloreXp); }
    
    public List<Integer> getIngredients() { return ingredients; }
    public void setIngredients(List<Integer> ingredients) { 
        this.ingredients = ingredients != null ? ingredients : new ArrayList<>(); 
    }
    
    // Utility methods
    public boolean canConsume(Map<String, Integer> playerStats) {
        if (levelRequirements.isEmpty()) return true;
        
        for (Map.Entry<String, Integer> requirement : levelRequirements.entrySet()) {
            String skill = requirement.getKey();
            int requiredLevel = requirement.getValue();
            int playerLevel = playerStats.getOrDefault(skill, 1);
            
            if (playerLevel < requiredLevel) {
                return false;
            }
        }
        return true;
    }
    
    public boolean hasStatBoost(String stat) {
        return statBoosts.containsKey(stat.toLowerCase());
    }
    
    public int getStatBoost(String stat) {
        return statBoosts.getOrDefault(stat.toLowerCase(), 0);
    }
    
    public boolean hasStatReduction(String stat) {
        return statReductions.containsKey(stat.toLowerCase());
    }
    
    public int getStatReduction(String stat) {
        return statReductions.getOrDefault(stat.toLowerCase(), 0);
    }
    
    public void addEffect(Effect effect) {
        if (effect != null && !effects.contains(effect)) {
            effects.add(effect);
        }
    }
    
    public void addStatBoost(String stat, int boost) {
        if (stat != null && boost > 0) {
            statBoosts.put(stat.toLowerCase(), boost);
        }
    }
    
    public void addStatReduction(String stat, int reduction) {
        if (stat != null && reduction > 0) {
            statReductions.put(stat.toLowerCase(), reduction);
        }
    }
    
    public void addLevelRequirement(String skill, int level) {
        if (skill != null && level > 0) {
            levelRequirements.put(skill.toLowerCase(), level);
        }
    }
    
    public void addQuestRequirement(String quest) {
        if (quest != null && !questRequirements.contains(quest)) {
            questRequirements.add(quest);
        }
    }
    
    public void addIngredient(int itemId) {
        if (itemId > 0 && !ingredients.contains(itemId)) {
            ingredients.add(itemId);
        }
    }
    
    public boolean isFood() {
        return type == ConsumableType.FOOD && edible && healAmount > 0;
    }
    
    public boolean isPotion() {
        return type == ConsumableType.POTION && drinkable;
    }
    
    public boolean isCombatFood() {
        return isFood() && healAmount >= 10; // Decent healing for combat
    }
    
    public boolean isCombatPotion() {
        return isPotion() && (combatPotion || !statBoosts.isEmpty());
    }
    
    public double getHealingEfficiency() {
        if (!isFood() || value <= 0) return 0.0;
        return (double) healAmount / value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Consumable that = (Consumable) o;
        return itemId == that.itemId;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(itemId);
    }
    
    @Override
    public String toString() {
        return String.format("Consumable{id=%d, name='%s', type=%s, heal=%d}", 
                           itemId, name, type, healAmount);
    }
    
    // Enums
    public enum ConsumableType {
        FOOD, POTION, DRINK, BARBARIAN_HERBLORE, COMBO_FOOD, OTHER
    }
    
    // Inner classes
    public static class Effect {
        private String name;
        private String description;
        private EffectType type;
        private int duration; // in ticks
        private int magnitude;
        private boolean beneficial;
        
        public Effect() {}
        
        public Effect(String name, EffectType type, int duration, int magnitude) {
            this.name = name;
            this.type = type;
            this.duration = duration;
            this.magnitude = magnitude;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public EffectType getType() { return type; }
        public void setType(EffectType type) { this.type = type; }
        
        public int getDuration() { return duration; }
        public void setDuration(int duration) { this.duration = Math.max(0, duration); }
        
        public int getMagnitude() { return magnitude; }
        public void setMagnitude(int magnitude) { this.magnitude = magnitude; }
        
        public boolean isBeneficial() { return beneficial; }
        public void setBeneficial(boolean beneficial) { this.beneficial = beneficial; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Effect effect = (Effect) o;
            return Objects.equals(name, effect.name) && type == effect.type;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(name, type);
        }
        
        @Override
        public String toString() {
            return String.format("Effect{name='%s', type=%s, duration=%d, magnitude=%d}", 
                               name, type, duration, magnitude);
        }
    }
    
    public enum EffectType {
        STAT_BOOST, STAT_REDUCTION, POISON_IMMUNITY, DISEASE_IMMUNITY, 
        ANTIFIRE, PRAYER_BONUS, ENERGY_RESTORE, SPECIAL_ATTACK_RESTORE,
        DAMAGE_REDUCTION, ACCURACY_BOOST, OTHER
    }
}
package database.models;

import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

/**
 * Represents an equipment item in OSRS with all its properties and requirements
 */
public class Equipment {
    // Basic item information
    private int itemId;
    private String name;
    private String description;
    private EquipmentSlot slot;
    private EquipmentType type;
    private int value; // Grand Exchange value
    
    // Level requirements
    private Map<String, Integer> levelRequirements;
    
    // Combat bonuses
    private CombatBonuses bonuses;
    
    // Special properties
    private boolean tradeable;
    private boolean degradable;
    private boolean stackable;
    private int weight; // Weight in grams
    
    // Weapon-specific properties
    private WeaponProperties weaponProperties;
    
    // Armor-specific properties
    private ArmorProperties armorProperties;
    
    public Equipment() {
        this.levelRequirements = new HashMap<>();
        this.bonuses = new CombatBonuses();
    }
    
    public Equipment(int itemId, String name, EquipmentSlot slot) {
        this();
        this.itemId = itemId;
        this.name = name;
        this.slot = slot;
        this.type = EquipmentType.TOOL; // Default type
    }
    
    public Equipment(int itemId, String name, EquipmentSlot slot, EquipmentType type) {
        this();
        this.itemId = itemId;
        this.name = name;
        this.slot = slot;
        this.type = type;
    }
    
    // Getters and setters
    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public EquipmentSlot getSlot() { return slot; }
    public void setSlot(EquipmentSlot slot) { this.slot = slot; }
    
    public EquipmentType getType() { return type; }
    public void setType(EquipmentType type) { this.type = type; }
    
    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
    
    public Map<String, Integer> getLevelRequirements() { return levelRequirements; }
    public void setLevelRequirements(Map<String, Integer> levelRequirements) { 
        this.levelRequirements = levelRequirements != null ? levelRequirements : new HashMap<>(); 
    }
    
    public CombatBonuses getBonuses() { return bonuses; }
    public void setBonuses(CombatBonuses bonuses) { 
        this.bonuses = bonuses != null ? bonuses : new CombatBonuses(); 
    }
    
    public boolean isTradeable() { return tradeable; }
    public void setTradeable(boolean tradeable) { this.tradeable = tradeable; }
    
    public boolean isDegradable() { return degradable; }
    public void setDegradable(boolean degradable) { this.degradable = degradable; }
    
    public boolean isStackable() { return stackable; }
    public void setStackable(boolean stackable) { this.stackable = stackable; }
    
    public int getWeight() { return weight; }
    public void setWeight(int weight) { this.weight = weight; }
    
    public WeaponProperties getWeaponProperties() { return weaponProperties; }
    public void setWeaponProperties(WeaponProperties weaponProperties) { this.weaponProperties = weaponProperties; }
    
    public ArmorProperties getArmorProperties() { return armorProperties; }
    public void setArmorProperties(ArmorProperties armorProperties) { this.armorProperties = armorProperties; }
    
    // Convenience methods for weapon and armor types
    public void setWeaponType(WeaponType weaponType) {
        if (weaponProperties == null) {
            weaponProperties = new WeaponProperties();
        }
        weaponProperties.setWeaponType(weaponType);
    }
    
    public WeaponType getWeaponType() {
        return weaponProperties != null ? weaponProperties.getWeaponType() : WeaponType.UNKNOWN;
    }
    
    public void setArmorType(ArmorType armorType) {
        if (armorProperties == null) {
            armorProperties = new ArmorProperties();
        }
        armorProperties.setArmorType(armorType);
    }
    
    public ArmorType getArmorType() {
        return armorProperties != null ? armorProperties.getArmorType() : ArmorType.UNKNOWN;
    }
    
    // Utility methods
    public boolean isWeapon() {
        return slot == EquipmentSlot.WEAPON || slot == EquipmentSlot.SHIELD;
    }
    
    public boolean isArmor() {
        return slot == EquipmentSlot.HEAD || slot == EquipmentSlot.BODY || 
               slot == EquipmentSlot.LEGS || slot == EquipmentSlot.FEET ||
               slot == EquipmentSlot.HANDS || slot == EquipmentSlot.CAPE;
    }
    
    public boolean isAccessory() {
        return slot == EquipmentSlot.RING || slot == EquipmentSlot.AMULET;
    }
    
    public boolean hasLevelRequirement(String skill) {
        return levelRequirements.containsKey(skill.toLowerCase());
    }
    
    public boolean hasLevelRequirement(String skill, int level) {
        return getLevelRequirement(skill) <= level;
    }
    
    public boolean canEquip(Map<String, Integer> playerStats) {
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
    
    public boolean supportsCombatStyle(String combatStyle) {
        if (weaponProperties == null) {
            return false;
        }
        
        WeaponType weaponType = weaponProperties.getWeaponType();
        if (weaponType == null) {
            return false;
        }
        
        // Check if the weapon type supports the combat style
        switch (combatStyle.toLowerCase()) {
            case "melee":
            case "attack":
            case "strength":
            case "defence":
                return weaponType.isMelee();
            case "ranged":
                return weaponType.isRanged();
            case "magic":
                return weaponType.isMagic();
            default:
                return false;
        }
    }
    
    public int getLevelRequirement(String skill) {
        return levelRequirements.getOrDefault(skill.toLowerCase(), 1);
    }
    
    public void addLevelRequirement(String skill, int level) {
        levelRequirements.put(skill.toLowerCase(), level);
    }
    
    public int getTotalCombatBonus() {
        if (bonuses == null) return 0;
        return bonuses.getAttackStab() + bonuses.getAttackSlash() + bonuses.getAttackCrush() +
               bonuses.getAttackMagic() + bonuses.getAttackRanged() + bonuses.getStrengthBonus() +
               bonuses.getRangedStrength() + bonuses.getMagicDamage();
    }
    
    public int getTotalDefenceBonus() {
        if (bonuses == null) return 0;
        return bonuses.getDefenceStab() + bonuses.getDefenceSlash() + bonuses.getDefenceCrush() +
               bonuses.getDefenceMagic() + bonuses.getDefenceRanged();
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Equipment equipment = (Equipment) o;
        return itemId == equipment.itemId;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(itemId);
    }
    
    @Override
    public String toString() {
        return String.format("Equipment{id=%d, name='%s', slot=%s, type=%s}", 
                           itemId, name, slot, type);
    }
    
    // Enums
    public enum EquipmentSlot {
        HEAD, CAPE, AMULET, WEAPON, BODY, SHIELD, LEGS, HANDS, FEET, RING, AMMO
    }
    
    public enum EquipmentType {
        MELEE_WEAPON, RANGED_WEAPON, MAGIC_WEAPON, ARMOR, ACCESSORY, AMMUNITION, TOOL
    }
    
    // Inner classes for complex properties
    public static class CombatBonuses {
        private int attackStab = 0;
        private int attackSlash = 0;
        private int attackCrush = 0;
        private int attackMagic = 0;
        private int attackRanged = 0;
        
        private int defenceStab = 0;
        private int defenceSlash = 0;
        private int defenceCrush = 0;
        private int defenceMagic = 0;
        private int defenceRanged = 0;
        
        private int strengthBonus = 0;
        private int rangedStrength = 0;
        private int magicDamage = 0;
        private int prayer = 0;
        
        // Getters and setters
        public int getAttackStab() { return attackStab; }
        public void setAttackStab(int attackStab) { this.attackStab = attackStab; }
        
        public int getAttackSlash() { return attackSlash; }
        public void setAttackSlash(int attackSlash) { this.attackSlash = attackSlash; }
        
        public int getAttackCrush() { return attackCrush; }
        public void setAttackCrush(int attackCrush) { this.attackCrush = attackCrush; }
        
        public int getAttackMagic() { return attackMagic; }
        public void setAttackMagic(int attackMagic) { this.attackMagic = attackMagic; }
        
        public int getAttackRanged() { return attackRanged; }
        public void setAttackRanged(int attackRanged) { this.attackRanged = attackRanged; }
        
        public int getDefenceStab() { return defenceStab; }
        public void setDefenceStab(int defenceStab) { this.defenceStab = defenceStab; }
        
        public int getDefenceSlash() { return defenceSlash; }
        public void setDefenceSlash(int defenceSlash) { this.defenceSlash = defenceSlash; }
        
        public int getDefenceCrush() { return defenceCrush; }
        public void setDefenceCrush(int defenceCrush) { this.defenceCrush = defenceCrush; }
        
        public int getDefenceMagic() { return defenceMagic; }
        public void setDefenceMagic(int defenceMagic) { this.defenceMagic = defenceMagic; }
        
        public int getDefenceRanged() { return defenceRanged; }
        public void setDefenceRanged(int defenceRanged) { this.defenceRanged = defenceRanged; }
        
        public int getStrengthBonus() { return strengthBonus; }
        public void setStrengthBonus(int strengthBonus) { this.strengthBonus = strengthBonus; }
        
        public int getRangedStrength() { return rangedStrength; }
        public void setRangedStrength(int rangedStrength) { this.rangedStrength = rangedStrength; }
        
        public int getMagicDamage() { return magicDamage; }
        public void setMagicDamage(int magicDamage) { this.magicDamage = magicDamage; }
        
        public int getPrayer() { return prayer; }
        public void setPrayer(int prayer) { this.prayer = prayer; }
    }
    
    public static class WeaponProperties {
        private WeaponType weaponType;
        private int attackSpeed;
        private String[] attackStyles;
        private boolean hasSpecialAttack;
        private int specialAttackCost;
        private String weaponCategory; // "sword", "axe", "bow", etc.
        
        public int getAttackSpeed() { return attackSpeed; }
        public void setAttackSpeed(int attackSpeed) { this.attackSpeed = attackSpeed; }
        
        public String[] getAttackStyles() { return attackStyles; }
        public void setAttackStyles(String[] attackStyles) { this.attackStyles = attackStyles; }
        
        public boolean isHasSpecialAttack() { return hasSpecialAttack; }
        public void setHasSpecialAttack(boolean hasSpecialAttack) { this.hasSpecialAttack = hasSpecialAttack; }
        
        public int getSpecialAttackCost() { return specialAttackCost; }
        public void setSpecialAttackCost(int specialAttackCost) { this.specialAttackCost = specialAttackCost; }
        
        public String getWeaponCategory() { return weaponCategory; }
        public void setWeaponCategory(String weaponCategory) { this.weaponCategory = weaponCategory; }
        
        public WeaponType getWeaponType() { return weaponType; }
        public void setWeaponType(WeaponType weaponType) { this.weaponType = weaponType; }
    }
    
    public static class ArmorProperties {
        private ArmorType armorType;
        private String armorSet; // "iron", "steel", "rune", etc.
        private int defenceLevel;
        private boolean fullHelm;
        private boolean platebody;
        
        public String getArmorSet() { return armorSet; }
        public void setArmorSet(String armorSet) { this.armorSet = armorSet; }
        
        public int getDefenceLevel() { return defenceLevel; }
        public void setDefenceLevel(int defenceLevel) { this.defenceLevel = defenceLevel; }
        
        public boolean isFullHelm() { return fullHelm; }
        public void setFullHelm(boolean fullHelm) { this.fullHelm = fullHelm; }
        
        public boolean isPlatebody() { return platebody; }
        public void setPlatebody(boolean platebody) { this.platebody = platebody; }
        
        public ArmorType getArmorType() { return armorType; }
        public void setArmorType(ArmorType armorType) { this.armorType = armorType; }
    }
}
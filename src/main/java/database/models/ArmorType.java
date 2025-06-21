package database.models;

/**
 * Enumeration of armor types in Old School RuneScape
 * Used for categorizing defensive equipment and determining combat effectiveness
 */
public enum ArmorType {
    // Combat Style Based
    MELEE("Melee Armor", true, false, false, "Provides defense against melee attacks"),
    RANGED("Ranged Armor", false, true, false, "Provides defense against ranged attacks"),
    MAGIC("Magic Armor", false, false, true, "Provides defense against magic attacks"),
    
    // Hybrid Armors
    HYBRID("Hybrid Armor", true, true, true, "Provides balanced defense against all combat styles"),
    MELEE_RANGED("Melee/Ranged Armor", true, true, false, "Effective against melee and ranged"),
    MELEE_MAGIC("Melee/Magic Armor", true, false, true, "Effective against melee and magic"),
    RANGED_MAGIC("Ranged/Magic Armor", false, true, true, "Effective against ranged and magic"),
    
    // Special Categories
    VOID("Void Armor", true, true, true, "Special set with damage bonuses"),
    GRACEFUL("Graceful Armor", false, false, false, "Weight-reducing armor for agility"),
    PRAYER("Prayer Armor", false, false, false, "Provides prayer bonuses"),
    SKILLING("Skilling Armor", false, false, false, "Provides bonuses to non-combat skills"),
    
    // Material Based
    LEATHER("Leather Armor", false, true, false, "Light ranged armor"),
    DRAGONHIDE("Dragonhide Armor", false, true, false, "High-level ranged armor"),
    CHAINMAIL("Chainmail Armor", true, false, false, "Medium melee armor"),
    PLATEBODY("Plate Armor", true, false, false, "Heavy melee armor"),
    ROBES("Robes", false, false, true, "Magic armor providing spell bonuses"),
    
    // Defensive Only
    TANK("Tank Armor", true, true, true, "High defensive bonuses, low offensive"),
    DEFENSIVE("Defensive Armor", true, true, true, "Focused on damage reduction"),
    
    // Unknown/Other
    COSMETIC("Cosmetic Armor", false, false, false, "Provides no combat bonuses"),
    UNKNOWN("Unknown Armor", false, false, false, "Unclassified armor type");
    
    private final String displayName;
    private final boolean effectiveAgainstMelee;
    private final boolean effectiveAgainstRanged;
    private final boolean effectiveAgainstMagic;
    private final String description;
    
    ArmorType(String displayName, boolean effectiveAgainstMelee, 
              boolean effectiveAgainstRanged, boolean effectiveAgainstMagic, 
              String description) {
        this.displayName = displayName;
        this.effectiveAgainstMelee = effectiveAgainstMelee;
        this.effectiveAgainstRanged = effectiveAgainstRanged;
        this.effectiveAgainstMagic = effectiveAgainstMagic;
        this.description = description;
    }
    
    /**
     * Get the display name of this armor type
     * @return Human-readable armor type name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Get the description of this armor type
     * @return Armor type description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if this armor type is effective against melee attacks
     * @return true if effective against melee
     */
    public boolean isEffectiveAgainstMelee() {
        return effectiveAgainstMelee;
    }
    
    /**
     * Check if this armor type is effective against ranged attacks
     * @return true if effective against ranged
     */
    public boolean isEffectiveAgainstRanged() {
        return effectiveAgainstRanged;
    }
    
    /**
     * Check if this armor type is effective against magic attacks
     * @return true if effective against magic
     */
    public boolean isEffectiveAgainstMagic() {
        return effectiveAgainstMagic;
    }
    
    /**
     * Check if this armor type is hybrid (effective against multiple combat styles)
     * @return true if hybrid armor
     */
    public boolean isHybrid() {
        int effectiveCount = 0;
        if (effectiveAgainstMelee) effectiveCount++;
        if (effectiveAgainstRanged) effectiveCount++;
        if (effectiveAgainstMagic) effectiveCount++;
        return effectiveCount > 1;
    }
    
    /**
     * Check if this armor provides any combat effectiveness
     * @return true if provides combat bonuses
     */
    public boolean isCombatArmor() {
        return effectiveAgainstMelee || effectiveAgainstRanged || effectiveAgainstMagic;
    }
    
    /**
     * Get armor type from string name (case-insensitive)
     * @param name Armor type name
     * @return ArmorType enum or UNKNOWN if not found
     */
    public static ArmorType fromString(String name) {
        if (name == null || name.trim().isEmpty()) {
            return UNKNOWN;
        }
        
        String cleanName = name.trim().toUpperCase().replace(" ", "_").replace("/", "_");
        
        try {
            return ArmorType.valueOf(cleanName);
        } catch (IllegalArgumentException e) {
            // Try partial matching
            for (ArmorType type : values()) {
                if (type.displayName.equalsIgnoreCase(name) || 
                    type.name().equalsIgnoreCase(cleanName)) {
                    return type;
                }
            }
            
            // Try keyword matching
            String lowerName = name.toLowerCase();
            if (lowerName.contains("melee")) return MELEE;
            if (lowerName.contains("ranged") || lowerName.contains("range")) return RANGED;
            if (lowerName.contains("magic") || lowerName.contains("mage")) return MAGIC;
            if (lowerName.contains("leather")) return LEATHER;
            if (lowerName.contains("dragonhide") || lowerName.contains("d'hide")) return DRAGONHIDE;
            if (lowerName.contains("plate")) return PLATEBODY;
            if (lowerName.contains("robe")) return ROBES;
            if (lowerName.contains("void")) return VOID;
            if (lowerName.contains("graceful")) return GRACEFUL;
            
            return UNKNOWN;
        }
    }
    
    /**
     * Get all armor types effective against a specific combat style
     * @param combatStyle The combat style ("melee", "ranged", "magic")
     * @return Array of effective armor types
     */
    public static ArmorType[] getEffectiveAgainst(String combatStyle) {
        if (combatStyle == null) return new ArmorType[0];
        
        switch (combatStyle.toLowerCase()) {
            case "melee":
                return java.util.Arrays.stream(values())
                        .filter(ArmorType::isEffectiveAgainstMelee)
                        .toArray(ArmorType[]::new);
            case "ranged":
            case "range":
                return java.util.Arrays.stream(values())
                        .filter(ArmorType::isEffectiveAgainstRanged)
                        .toArray(ArmorType[]::new);
            case "magic":
            case "mage":
                return java.util.Arrays.stream(values())
                        .filter(ArmorType::isEffectiveAgainstMagic)
                        .toArray(ArmorType[]::new);
            default:
                return new ArmorType[0];
        }
    }
    
    /**
     * Get all hybrid armor types
     * @return Array of hybrid armor types
     */
    public static ArmorType[] getHybridTypes() {
        return java.util.Arrays.stream(values())
                .filter(ArmorType::isHybrid)
                .toArray(ArmorType[]::new);
    }
    
    /**
     * Get all combat armor types (excluding cosmetic/skilling)
     * @return Array of combat armor types
     */
    public static ArmorType[] getCombatTypes() {
        return java.util.Arrays.stream(values())
                .filter(ArmorType::isCombatArmor)
                .toArray(ArmorType[]::new);
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
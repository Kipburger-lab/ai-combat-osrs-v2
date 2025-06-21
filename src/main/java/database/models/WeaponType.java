package database.models;

/**
 * Enumeration of weapon types in Old School RuneScape
 * Used for categorizing equipment and determining combat effectiveness
 */
public enum WeaponType {
    // Melee Weapons
    SWORD("Sword", true, false, false),
    SCIMITAR("Scimitar", true, false, false),
    LONGSWORD("Longsword", true, false, false),
    DAGGER("Dagger", true, false, false),
    MACE("Mace", true, false, false),
    BATTLEAXE("Battleaxe", true, false, false),
    WARHAMMER("Warhammer", true, false, false),
    SPEAR("Spear", true, false, false),
    HALBERD("Halberd", true, false, false),
    WHIP("Whip", true, false, false),
    CLAWS("Claws", true, false, false),
    MAUL("Maul", true, false, false),
    
    // Ranged Weapons
    SHORTBOW("Shortbow", false, true, false),
    LONGBOW("Longbow", false, true, false),
    CROSSBOW("Crossbow", false, true, false),
    THROWING_KNIFE("Throwing Knife", false, true, false),
    DART("Dart", false, true, false),
    JAVELIN("Javelin", false, true, false),
    CHINCHOMPA("Chinchompa", false, true, false),
    BLOWPIPE("Blowpipe", false, true, false),
    
    // Magic Weapons
    STAFF("Staff", false, false, true),
    WAND("Wand", false, false, true),
    ORB("Orb", false, false, true),
    TOME("Tome", false, false, true),
    
    // Special/Hybrid
    SALAMANDER("Salamander", true, true, true),
    TRIDENT("Trident", false, false, true),
    POWERED_STAFF("Powered Staff", false, false, true),
    
    // Defensive
    SHIELD("Shield", false, false, false),
    
    // Unknown/Other
    UNKNOWN("Unknown", false, false, false);
    
    private final String displayName;
    private final boolean isMelee;
    private final boolean isRanged;
    private final boolean isMagic;
    
    WeaponType(String displayName, boolean isMelee, boolean isRanged, boolean isMagic) {
        this.displayName = displayName;
        this.isMelee = isMelee;
        this.isRanged = isRanged;
        this.isMagic = isMagic;
    }
    
    /**
     * Get the display name of this weapon type
     * @return Human-readable weapon type name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Check if this weapon type is used for melee combat
     * @return true if melee weapon
     */
    public boolean isMelee() {
        return isMelee;
    }
    
    /**
     * Check if this weapon type is used for ranged combat
     * @return true if ranged weapon
     */
    public boolean isRanged() {
        return isRanged;
    }
    
    /**
     * Check if this weapon type is used for magic combat
     * @return true if magic weapon
     */
    public boolean isMagic() {
        return isMagic;
    }
    
    /**
     * Check if this weapon type supports multiple combat styles
     * @return true if hybrid weapon
     */
    public boolean isHybrid() {
        int styleCount = 0;
        if (isMelee) styleCount++;
        if (isRanged) styleCount++;
        if (isMagic) styleCount++;
        return styleCount > 1;
    }
    
    /**
     * Get weapon type from string name (case-insensitive)
     * @param name Weapon type name
     * @return WeaponType enum or UNKNOWN if not found
     */
    public static WeaponType fromString(String name) {
        if (name == null || name.trim().isEmpty()) {
            return UNKNOWN;
        }
        
        String cleanName = name.trim().toUpperCase().replace(" ", "_");
        
        try {
            return WeaponType.valueOf(cleanName);
        } catch (IllegalArgumentException e) {
            // Try partial matching
            for (WeaponType type : values()) {
                if (type.displayName.equalsIgnoreCase(name) || 
                    type.name().equalsIgnoreCase(cleanName)) {
                    return type;
                }
            }
            return UNKNOWN;
        }
    }
    
    /**
     * Get all melee weapon types
     * @return Array of melee weapon types
     */
    public static WeaponType[] getMeleeTypes() {
        return java.util.Arrays.stream(values())
                .filter(WeaponType::isMelee)
                .toArray(WeaponType[]::new);
    }
    
    /**
     * Get all ranged weapon types
     * @return Array of ranged weapon types
     */
    public static WeaponType[] getRangedTypes() {
        return java.util.Arrays.stream(values())
                .filter(WeaponType::isRanged)
                .toArray(WeaponType[]::new);
    }
    
    /**
     * Get all magic weapon types
     * @return Array of magic weapon types
     */
    public static WeaponType[] getMagicTypes() {
        return java.util.Arrays.stream(values())
                .filter(WeaponType::isMagic)
                .toArray(WeaponType[]::new);
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
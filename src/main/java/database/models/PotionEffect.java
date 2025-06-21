package database.models;

/**
 * Enumeration of potion effects in Old School RuneScape
 * Used for categorizing consumable items and their effects
 */
public enum PotionEffect {
    // Combat Stat Boosts
    ATTACK_BOOST("Attack Boost", "attack", true, false, false, 5, "Increases Attack level"),
    STRENGTH_BOOST("Strength Boost", "strength", true, false, false, 5, "Increases Strength level"),
    DEFENCE_BOOST("Defence Boost", "defence", true, false, false, 5, "Increases Defence level"),
    RANGED_BOOST("Ranged Boost", "ranged", true, false, false, 5, "Increases Ranged level"),
    MAGIC_BOOST("Magic Boost", "magic", true, false, false, 5, "Increases Magic level"),
    
    // Combined Combat Boosts
    COMBAT_BOOST("Combat Boost", "combat", true, false, false, 5, "Increases multiple combat stats"),
    SUPER_COMBAT("Super Combat", "super_combat", true, false, false, 5, "Increases Attack, Strength, and Defence"),
    RANGING_POTION("Ranging Potion", "ranging", true, false, false, 5, "Increases Ranged level significantly"),
    MAGIC_POTION("Magic Potion", "magic_potion", true, false, false, 5, "Increases Magic level significantly"),
    
    // Prayer and Special
    PRAYER_RESTORE("Prayer Restore", "prayer", false, true, false, 0, "Restores Prayer points"),
    STAT_RESTORE("Stat Restore", "restore", false, true, false, 0, "Restores lowered stats"),
    SUPER_RESTORE("Super Restore", "super_restore", false, true, false, 0, "Restores Prayer and stats"),
    
    // Energy and Stamina
    ENERGY_RESTORE("Energy Restore", "energy", false, true, false, 0, "Restores run energy"),
    STAMINA_BOOST("Stamina Boost", "stamina", true, false, false, 2, "Reduces run energy drain"),
    SUPER_ENERGY("Super Energy", "super_energy", false, true, false, 0, "Fully restores run energy"),
    
    // Poison and Antidotes
    ANTIPOISON("Antipoison", "antipoison", false, false, true, 0, "Cures and prevents poison"),
    SUPER_ANTIPOISON("Super Antipoison", "super_antipoison", false, false, true, 0, "Long-lasting poison protection"),
    ANTIDOTE_PLUS("Antidote+", "antidote_plus", false, false, true, 0, "Protects against poison and venom"),
    ANTIDOTE_PLUS_PLUS("Antidote++", "antidote_plus_plus", false, false, true, 0, "Superior poison and venom protection"),
    
    // Special Potions
    SARADOMIN_BREW("Saradomin Brew", "sara_brew", true, true, false, 0, "Heals and boosts Defence, lowers other stats"),
    ZAMORAK_BREW("Zamorak Brew", "zam_brew", true, false, false, 5, "Boosts Attack and Strength, lowers Defence"),
    OVERLOAD("Overload", "overload", true, false, false, 5, "Massive combat stat boost with damage over time"),
    
    // Barbarian Herblore
    BARBARIAN_MIX("Barbarian Mix", "barbarian", true, true, false, 5, "Combined potion effects"),
    RELICYM_BALM("Relicym's Balm", "relicym", false, false, true, 0, "Cures disease"),
    
    // Skill Boosts (Non-Combat)
    FISHING_BOOST("Fishing Boost", "fishing", true, false, false, 5, "Increases Fishing level"),
    HUNTER_BOOST("Hunter Boost", "hunter", true, false, false, 5, "Increases Hunter level"),
    WOODCUTTING_BOOST("Woodcutting Boost", "woodcutting", true, false, false, 5, "Increases Woodcutting level"),
    MINING_BOOST("Mining Boost", "mining", true, false, false, 5, "Increases Mining level"),
    AGILITY_BOOST("Agility Boost", "agility", true, false, false, 5, "Increases Agility level"),
    THIEVING_BOOST("Thieving Boost", "thieving", true, false, false, 5, "Increases Thieving level"),
    
    // Special Effects
    POISON_IMMUNITY("Poison Immunity", "poison_immune", false, false, true, 0, "Temporary immunity to poison"),
    VENOM_IMMUNITY("Venom Immunity", "venom_immune", false, false, true, 0, "Temporary immunity to venom"),
    DISEASE_IMMUNITY("Disease Immunity", "disease_immune", false, false, true, 0, "Temporary immunity to disease"),
    
    // Food Effects (for consistency)
    HEALING("Healing", "heal", false, true, false, 0, "Restores hitpoints"),
    
    // Unknown/Other
    UNKNOWN("Unknown Effect", "unknown", false, false, false, 0, "Unidentified effect");
    
    private final String displayName;
    private final String identifier;
    private final boolean isBoost;
    private final boolean isRestore;
    private final boolean isProtection;
    private final int boostAmount;
    private final String description;
    
    PotionEffect(String displayName, String identifier, boolean isBoost, 
                boolean isRestore, boolean isProtection, int boostAmount, 
                String description) {
        this.displayName = displayName;
        this.identifier = identifier;
        this.isBoost = isBoost;
        this.isRestore = isRestore;
        this.isProtection = isProtection;
        this.boostAmount = boostAmount;
        this.description = description;
    }
    
    /**
     * Get the display name of this potion effect
     * @return Human-readable effect name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Get the identifier string for this effect
     * @return Effect identifier
     */
    public String getIdentifier() {
        return identifier;
    }
    
    /**
     * Get the description of this effect
     * @return Effect description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Check if this effect provides a stat boost
     * @return true if boost effect
     */
    public boolean isBoost() {
        return isBoost;
    }
    
    /**
     * Check if this effect restores something
     * @return true if restore effect
     */
    public boolean isRestore() {
        return isRestore;
    }
    
    /**
     * Check if this effect provides protection
     * @return true if protection effect
     */
    public boolean isProtection() {
        return isProtection;
    }
    
    /**
     * Get the boost amount for this effect
     * @return Boost amount (0 if not a boost)
     */
    public int getBoostAmount() {
        return boostAmount;
    }
    
    /**
     * Check if this is a combat-related effect
     * @return true if combat effect
     */
    public boolean isCombatEffect() {
        return this == ATTACK_BOOST || this == STRENGTH_BOOST || this == DEFENCE_BOOST ||
               this == RANGED_BOOST || this == MAGIC_BOOST || this == COMBAT_BOOST ||
               this == SUPER_COMBAT || this == RANGING_POTION || this == MAGIC_POTION ||
               this == SARADOMIN_BREW || this == ZAMORAK_BREW || this == OVERLOAD;
    }
    
    /**
     * Check if this is a skill-related effect (non-combat)
     * @return true if skill effect
     */
    public boolean isSkillEffect() {
        return this == FISHING_BOOST || this == HUNTER_BOOST || this == WOODCUTTING_BOOST ||
               this == MINING_BOOST || this == AGILITY_BOOST || this == THIEVING_BOOST;
    }
    
    /**
     * Get potion effect from string name (case-insensitive)
     * @param name Effect name
     * @return PotionEffect enum or UNKNOWN if not found
     */
    public static PotionEffect fromString(String name) {
        if (name == null || name.trim().isEmpty()) {
            return UNKNOWN;
        }
        
        String cleanName = name.trim().toUpperCase().replace(" ", "_").replace("+", "_PLUS");
        
        try {
            return PotionEffect.valueOf(cleanName);
        } catch (IllegalArgumentException e) {
            // Try partial matching
            for (PotionEffect effect : values()) {
                if (effect.displayName.equalsIgnoreCase(name) || 
                    effect.identifier.equalsIgnoreCase(name) ||
                    effect.name().equalsIgnoreCase(cleanName)) {
                    return effect;
                }
            }
            
            // Try keyword matching
            String lowerName = name.toLowerCase();
            if (lowerName.contains("attack")) return ATTACK_BOOST;
            if (lowerName.contains("strength")) return STRENGTH_BOOST;
            if (lowerName.contains("defence") || lowerName.contains("defense")) return DEFENCE_BOOST;
            if (lowerName.contains("ranged") || lowerName.contains("range")) return RANGED_BOOST;
            if (lowerName.contains("magic")) return MAGIC_BOOST;
            if (lowerName.contains("prayer")) return PRAYER_RESTORE;
            if (lowerName.contains("energy")) return ENERGY_RESTORE;
            if (lowerName.contains("stamina")) return STAMINA_BOOST;
            if (lowerName.contains("antipoison")) return ANTIPOISON;
            if (lowerName.contains("sara")) return SARADOMIN_BREW;
            if (lowerName.contains("zam")) return ZAMORAK_BREW;
            
            return UNKNOWN;
        }
    }
    
    /**
     * Get all combat-related effects
     * @return Array of combat effects
     */
    public static PotionEffect[] getCombatEffects() {
        return java.util.Arrays.stream(values())
                .filter(PotionEffect::isCombatEffect)
                .toArray(PotionEffect[]::new);
    }
    
    /**
     * Get all skill-related effects
     * @return Array of skill effects
     */
    public static PotionEffect[] getSkillEffects() {
        return java.util.Arrays.stream(values())
                .filter(PotionEffect::isSkillEffect)
                .toArray(PotionEffect[]::new);
    }
    
    /**
     * Get all boost effects
     * @return Array of boost effects
     */
    public static PotionEffect[] getBoostEffects() {
        return java.util.Arrays.stream(values())
                .filter(PotionEffect::isBoost)
                .toArray(PotionEffect[]::new);
    }
    
    /**
     * Get all restore effects
     * @return Array of restore effects
     */
    public static PotionEffect[] getRestoreEffects() {
        return java.util.Arrays.stream(values())
                .filter(PotionEffect::isRestore)
                .toArray(PotionEffect[]::new);
    }
    
    /**
     * Get all protection effects
     * @return Array of protection effects
     */
    public static PotionEffect[] getProtectionEffects() {
        return java.util.Arrays.stream(values())
                .filter(PotionEffect::isProtection)
                .toArray(PotionEffect[]::new);
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
package combat;

import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Advanced Equipment Manager for AI Combat OSRS
 * Handles automatic equipment switching, weapon management,
 * and equipment optimization based on combat scenarios
 * 
 * @author TraeAI
 * @version 2.0
 */
public class EquipmentManager {
    
    // Equipment categories
    public enum WeaponType {
        MELEE_SLASH, MELEE_STAB, MELEE_CRUSH,
        RANGED_BOW, RANGED_CROSSBOW, RANGED_THROWN,
        MAGIC_STAFF, MAGIC_WAND, MAGIC_TOME,
        SPECIAL_WEAPON, UNKNOWN
    }
    
    public enum ArmorType {
        MELEE_ARMOR, RANGED_ARMOR, MAGIC_ARMOR,
        HYBRID_ARMOR, PRAYER_ARMOR, UNKNOWN
    }
    
    // Equipment sets configuration
    private final Map<CombatStyleManager.CombatStyle, EquipmentSet> equipmentSets = new ConcurrentHashMap<>();
    private final Map<String, WeaponType> weaponDatabase = new ConcurrentHashMap<>();
    private final Map<String, ArmorType> armorDatabase = new ConcurrentHashMap<>();
    
    // Current equipment state
    private WeaponType currentWeaponType = WeaponType.UNKNOWN;
    private CombatStyleManager.CombatStyle currentCombatStyle = CombatStyleManager.CombatStyle.AGGRESSIVE;
    private EquipmentSet currentEquipmentSet;
    
    // Equipment switching settings
    private boolean autoSwitchEnabled = true;
    private boolean degradationMonitoring = true;
    private int equipmentSwitchDelay = 600; // milliseconds
    
    // Statistics
    private int totalEquipmentSwitches = 0;
    private int successfulSwitches = 0;
    private long totalSwitchTime = 0;
    
    public EquipmentManager() {
        initializeWeaponDatabase();
        initializeArmorDatabase();
        initializeDefaultEquipmentSets();
        updateCurrentEquipment();
        Logger.log("[EquipmentManager] Initialized with advanced equipment management");
    }
    
    /**
     * Equipment Set class to define complete equipment configurations
     */
    public static class EquipmentSet {
        private final String name;
        private final Map<EquipmentSlot, String> equipment = new HashMap<>();
        private final Set<String> requiredItems = new HashSet<>();
        private final int minCombatLevel;
        private final Map<Skill, Integer> skillRequirements = new HashMap<>();
        
        public EquipmentSet(String name, int minCombatLevel) {
            this.name = name;
            this.minCombatLevel = minCombatLevel;
        }
        
        public EquipmentSet addEquipment(EquipmentSlot slot, String itemName) {
            equipment.put(slot, itemName);
            requiredItems.add(itemName);
            return this;
        }
        
        public EquipmentSet addSkillRequirement(Skill skill, int level) {
            skillRequirements.put(skill, level);
            return this;
        }
        
        public String getName() { return name; }
        public Map<EquipmentSlot, String> getEquipment() { return equipment; }
        public Set<String> getRequiredItems() { return requiredItems; }
        public int getMinCombatLevel() { return minCombatLevel; }
        public Map<Skill, Integer> getSkillRequirements() { return skillRequirements; }
    }
    
    /**
     * Initialize weapon type database
     */
    private void initializeWeaponDatabase() {
        // Melee weapons - Slash
        String[] slashWeapons = {"Scimitar", "Longsword", "Sword", "Whip", "Sabre", "Cutlass", "Machete"};
        for (String weapon : slashWeapons) {
            weaponDatabase.put(weapon.toLowerCase(), WeaponType.MELEE_SLASH);
        }
        
        // Melee weapons - Stab
        String[] stabWeapons = {"Dagger", "Rapier", "Spear", "Hasta", "Pickaxe"};
        for (String weapon : stabWeapons) {
            weaponDatabase.put(weapon.toLowerCase(), WeaponType.MELEE_STAB);
        }
        
        // Melee weapons - Crush
        String[] crushWeapons = {"Mace", "Hammer", "Maul", "Club", "Flail", "Axe"};
        for (String weapon : crushWeapons) {
            weaponDatabase.put(weapon.toLowerCase(), WeaponType.MELEE_CRUSH);
        }
        
        // Ranged weapons - Bows
        String[] bows = {"Shortbow", "Longbow", "Composite", "Seercull", "Magic bow", "Crystal bow"};
        for (String bow : bows) {
            weaponDatabase.put(bow.toLowerCase(), WeaponType.RANGED_BOW);
        }
        
        // Ranged weapons - Crossbows
        String[] crossbows = {"Crossbow", "Ballista"};
        for (String crossbow : crossbows) {
            weaponDatabase.put(crossbow.toLowerCase(), WeaponType.RANGED_CROSSBOW);
        }
        
        // Ranged weapons - Thrown
        String[] thrown = {"Dart", "Knife", "Javelin", "Thrownaxe", "Chinchompa"};
        for (String throwWeapon : thrown) {
            weaponDatabase.put(throwWeapon.toLowerCase(), WeaponType.RANGED_THROWN);
        }
        
        // Magic weapons - Staves
        String[] staves = {"Staff", "Battlestaff", "Mystic staff", "Ancient staff", "Trident"};
        for (String staff : staves) {
            weaponDatabase.put(staff.toLowerCase(), WeaponType.MAGIC_STAFF);
        }
        
        // Magic weapons - Wands
        String[] wands = {"Wand", "Beginner wand"};
        for (String wand : wands) {
            weaponDatabase.put(wand.toLowerCase(), WeaponType.MAGIC_WAND);
        }
        
        // Magic weapons - Tomes
        weaponDatabase.put("tome", WeaponType.MAGIC_TOME);
        
        Logger.log("[EquipmentManager] Weapon database initialized with " + weaponDatabase.size() + " entries");
    }
    
    /**
     * Initialize armor type database
     */
    private void initializeArmorDatabase() {
        // Melee armor
        String[] meleeArmor = {"Platebody", "Chainbody", "Platelegs", "Plateskirt", "Full helm", 
                              "Med helm", "Square shield", "Kiteshield", "Defender"};
        for (String armor : meleeArmor) {
            armorDatabase.put(armor.toLowerCase(), ArmorType.MELEE_ARMOR);
        }
        
        // Ranged armor
        String[] rangedArmor = {"Leather", "Studded", "Dragonhide", "Coif", "Chaps", "Vambraces"};
        for (String armor : rangedArmor) {
            armorDatabase.put(armor.toLowerCase(), ArmorType.RANGED_ARMOR);
        }
        
        // Magic armor
        String[] magicArmor = {"Robe", "Hat", "Wizard", "Mystic", "Enchanted", "Splitbark"};
        for (String armor : magicArmor) {
            armorDatabase.put(armor.toLowerCase(), ArmorType.MAGIC_ARMOR);
        }
        
        // Hybrid armor
        String[] hybridArmor = {"Void", "Elite void", "Barrows", "Bandos", "Armadyl", "Ancestral"};
        for (String armor : hybridArmor) {
            armorDatabase.put(armor.toLowerCase(), ArmorType.HYBRID_ARMOR);
        }
        
        // Prayer armor
        String[] prayerArmor = {"Monk", "Proselyte", "Initiate", "Vestment"};
        for (String armor : prayerArmor) {
            armorDatabase.put(armor.toLowerCase(), ArmorType.PRAYER_ARMOR);
        }
        
        Logger.log("[EquipmentManager] Armor database initialized with " + armorDatabase.size() + " entries");
    }
    
    /**
     * Initialize default equipment sets
     */
    private void initializeDefaultEquipmentSets() {
        // Basic Melee equipment set (low level, no defense requirements)
        EquipmentSet basicMeleeSet = new EquipmentSet("Basic Melee", 1)
            .addEquipment(EquipmentSlot.WEAPON, "Iron scimitar")
            .addEquipment(EquipmentSlot.CHEST, "Leather body")
            .addEquipment(EquipmentSlot.LEGS, "Leather chaps")
            .addSkillRequirement(Skill.ATTACK, 1);
        
        // Standard Melee equipment set (higher level with defense requirements)
        EquipmentSet meleeSet = new EquipmentSet("Standard Melee", 40)
            .addEquipment(EquipmentSlot.WEAPON, "Rune scimitar")
            .addEquipment(EquipmentSlot.HAT, "Rune full helm")
            .addEquipment(EquipmentSlot.CHEST, "Rune platebody")
            .addEquipment(EquipmentSlot.LEGS, "Rune platelegs")
            .addEquipment(EquipmentSlot.SHIELD, "Rune kiteshield")
            .addEquipment(EquipmentSlot.FEET, "Rune boots")
            .addEquipment(EquipmentSlot.HANDS, "Combat bracelet")
            .addSkillRequirement(Skill.ATTACK, 40)
            .addSkillRequirement(Skill.DEFENCE, 40);
        
        // Basic Ranged equipment set (low level, no defense requirements)
        EquipmentSet basicRangedSet = new EquipmentSet("Basic Ranged", 1)
            .addEquipment(EquipmentSlot.WEAPON, "Shortbow")
            .addEquipment(EquipmentSlot.CHEST, "Leather body")
            .addEquipment(EquipmentSlot.LEGS, "Leather chaps")
            .addEquipment(EquipmentSlot.ARROWS, "Bronze arrow")
            .addSkillRequirement(Skill.RANGED, 1);
        
        // Standard Ranged equipment set (higher level with defense requirements)
        EquipmentSet rangedSet = new EquipmentSet("Standard Ranged", 40)
            .addEquipment(EquipmentSlot.WEAPON, "Magic shortbow")
            .addEquipment(EquipmentSlot.HAT, "Green d'hide coif")
            .addEquipment(EquipmentSlot.CHEST, "Green d'hide body")
            .addEquipment(EquipmentSlot.LEGS, "Green d'hide chaps")
            .addEquipment(EquipmentSlot.FEET, "Snakeskin boots")
            .addEquipment(EquipmentSlot.HANDS, "Green d'hide vambraces")
            .addEquipment(EquipmentSlot.ARROWS, "Rune arrow")
            .addSkillRequirement(Skill.RANGED, 40)
            .addSkillRequirement(Skill.DEFENCE, 40);
        
        // Basic Magic equipment set (low level, no defense requirements)
        EquipmentSet basicMagicSet = new EquipmentSet("Basic Magic", 1)
            .addEquipment(EquipmentSlot.WEAPON, "Staff of air")
            .addEquipment(EquipmentSlot.CHEST, "Wizard robe")
            .addEquipment(EquipmentSlot.LEGS, "Wizard robe skirt")
            .addSkillRequirement(Skill.MAGIC, 1);
        
        // Standard Magic equipment set (higher level with defense requirements)
        EquipmentSet magicSet = new EquipmentSet("Standard Magic", 40)
            .addEquipment(EquipmentSlot.WEAPON, "Staff of fire")
            .addEquipment(EquipmentSlot.HAT, "Mystic hat")
            .addEquipment(EquipmentSlot.CHEST, "Mystic robe top")
            .addEquipment(EquipmentSlot.LEGS, "Mystic robe bottom")
            .addEquipment(EquipmentSlot.FEET, "Mystic boots")
            .addEquipment(EquipmentSlot.HANDS, "Mystic gloves")
            .addSkillRequirement(Skill.MAGIC, 40)
            .addSkillRequirement(Skill.DEFENCE, 40);
        
        // Assign equipment sets based on player's defense level
        int defenseLevel = Skills.getRealLevel(Skill.DEFENCE);
        
        // Use basic sets for low defense, standard sets for higher defense
        EquipmentSet selectedMeleeSet = defenseLevel >= 40 ? meleeSet : basicMeleeSet;
        EquipmentSet selectedRangedSet = defenseLevel >= 40 ? rangedSet : basicRangedSet;
        EquipmentSet selectedMagicSet = defenseLevel >= 40 ? magicSet : basicMagicSet;
        
        equipmentSets.put(CombatStyleManager.CombatStyle.AGGRESSIVE, selectedMeleeSet);
        equipmentSets.put(CombatStyleManager.CombatStyle.ACCURATE, selectedMeleeSet);
        equipmentSets.put(CombatStyleManager.CombatStyle.DEFENSIVE, selectedMeleeSet);
        equipmentSets.put(CombatStyleManager.CombatStyle.CONTROLLED, selectedMeleeSet);
        
        equipmentSets.put(CombatStyleManager.CombatStyle.RANGED_ACCURATE, selectedRangedSet);
        equipmentSets.put(CombatStyleManager.CombatStyle.RANGED_RAPID, selectedRangedSet);
        equipmentSets.put(CombatStyleManager.CombatStyle.RANGED_LONGRANGE, selectedRangedSet);
        
        equipmentSets.put(CombatStyleManager.CombatStyle.MAGIC_ACCURATE, selectedMagicSet);
        equipmentSets.put(CombatStyleManager.CombatStyle.MAGIC_DEFENSIVE, selectedMagicSet);
        
        Logger.log("[EquipmentManager] Default equipment sets initialized: " + equipmentSets.size() + " sets");
    }
    
    /**
     * Switch to equipment set for specified combat style
     * @param combatStyle target combat style
     * @return true if switch was successful
     */
    public boolean switchToEquipmentSet(CombatStyleManager.CombatStyle combatStyle) {
        if (!autoSwitchEnabled) {
            Logger.log("[EquipmentManager] Auto-switch disabled, skipping equipment change");
            return false;
        }
        
        if (combatStyle == currentCombatStyle && isCurrentEquipmentOptimal()) {
            Logger.log("[EquipmentManager] Already using optimal equipment for " + combatStyle);
            return true;
        }
        
        long startTime = System.currentTimeMillis();
        totalEquipmentSwitches++;
        
        Logger.log("[EquipmentManager] Switching to equipment set for: " + combatStyle);
        
        EquipmentSet targetSet = equipmentSets.get(combatStyle);
        if (targetSet == null) {
            Logger.error("[EquipmentManager] No equipment set defined for: " + combatStyle);
            return false;
        }
        
        // Check skill requirements
        if (!meetsSkillRequirements(targetSet)) {
            Logger.error("[EquipmentManager] Skill requirements not met for: " + targetSet.getName());
            return false;
        }
        
        // Check item availability
        if (!hasRequiredItems(targetSet)) {
            Logger.error("[EquipmentManager] Required items not available for: " + targetSet.getName());
            return false;
        }
        
        // Perform equipment switch
        if (performEquipmentSwitch(targetSet)) {
            currentCombatStyle = combatStyle;
            currentEquipmentSet = targetSet;
            updateCurrentEquipment();
            
            successfulSwitches++;
            long switchTime = System.currentTimeMillis() - startTime;
            totalSwitchTime += switchTime;
            
            Logger.log(String.format("[EquipmentManager] Equipment switch completed in %dms (Success rate: %.1f%%)", 
                switchTime, getSuccessRate()));
            
            return true;
        }
        
        return false;
    }
    
    /**
     * Perform the actual equipment switching
     * @param equipmentSet target equipment set
     * @return true if successful
     */
    private boolean performEquipmentSwitch(EquipmentSet equipmentSet) {
        Logger.log("[EquipmentManager] Performing equipment switch to: " + equipmentSet.getName());
        
        for (Map.Entry<EquipmentSlot, String> entry : equipmentSet.getEquipment().entrySet()) {
            EquipmentSlot slot = entry.getKey();
            String itemName = entry.getValue();
            
            // Check if item is already equipped
            Item currentItem = Equipment.getItemInSlot(slot);
            if (currentItem != null && currentItem.getName().equals(itemName)) {
                Logger.log("[EquipmentManager] " + itemName + " already equipped in " + slot);
                continue;
            }
            
            // Unequip current item if necessary
            if (currentItem != null) {
                if (!Equipment.unequip(slot)) {
                    Logger.error("[EquipmentManager] Failed to unequip " + currentItem.getName());
                    return false;
                }
                Sleep.sleep(equipmentSwitchDelay / 2);
            }
            
            // Equip new item
            if (Inventory.contains(itemName)) {
                if (!Equipment.equip(EquipmentSlot.WEAPON, itemName)) {
                    Logger.error("[EquipmentManager] Failed to equip " + itemName);
                    return false;
                }
                Sleep.sleep(equipmentSwitchDelay);
                Logger.log("[EquipmentManager] Equipped " + itemName + " in " + slot);
            } else {
                Logger.warn("[EquipmentManager] Item not found in inventory: " + itemName);
            }
        }
        
        return true;
    }
    
    /**
     * Check if player meets skill requirements for equipment set
     * @param equipmentSet equipment set to check
     * @return true if requirements are met
     */
    private boolean meetsSkillRequirements(EquipmentSet equipmentSet) {
        for (Map.Entry<Skill, Integer> requirement : equipmentSet.getSkillRequirements().entrySet()) {
            Skill skill = requirement.getKey();
            int requiredLevel = requirement.getValue();
            int currentLevel = Skills.getRealLevel(skill);
            
            if (currentLevel < requiredLevel) {
                Logger.warn(String.format("[EquipmentManager] Skill requirement not met: %s %d (have %d)", 
                    skill.name(), requiredLevel, currentLevel));
                return false;
            }
        }
        return true;
    }
    
    /**
     * Check if required items are available
     * @param equipmentSet equipment set to check
     * @return true if all items are available
     */
    private boolean hasRequiredItems(EquipmentSet equipmentSet) {
        for (String itemName : equipmentSet.getRequiredItems()) {
            if (!Inventory.contains(itemName) && !Equipment.contains(itemName)) {
                Logger.warn("[EquipmentManager] Required item not available: " + itemName);
                return false;
            }
        }
        return true;
    }
    
    /**
     * Check if current equipment is optimal for current combat style
     * @return true if equipment is optimal
     */
    private boolean isCurrentEquipmentOptimal() {
        if (currentEquipmentSet == null) return false;
        
        for (Map.Entry<EquipmentSlot, String> entry : currentEquipmentSet.getEquipment().entrySet()) {
            EquipmentSlot slot = entry.getKey();
            String expectedItem = entry.getValue();
            
            Item currentItem = Equipment.getItemInSlot(slot);
            if (currentItem == null || !currentItem.getName().equals(expectedItem)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Update current equipment information
     */
    private void updateCurrentEquipment() {
        Item weapon = Equipment.getItemInSlot(EquipmentSlot.WEAPON);
        if (weapon != null) {
            currentWeaponType = determineWeaponType(weapon.getName());
            Logger.log("[EquipmentManager] Current weapon: " + weapon.getName() + " (Type: " + currentWeaponType + ")");
        } else {
            currentWeaponType = WeaponType.UNKNOWN;
            Logger.log("[EquipmentManager] No weapon equipped");
        }
    }
    
    /**
     * Determine weapon type from item name
     * @param weaponName name of the weapon
     * @return weapon type
     */
    private WeaponType determineWeaponType(String weaponName) {
        String lowerName = weaponName.toLowerCase();
        
        for (Map.Entry<String, WeaponType> entry : weaponDatabase.entrySet()) {
            if (lowerName.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        
        return WeaponType.UNKNOWN;
    }
    
    /**
     * Monitor equipment degradation and condition
     * @return true if equipment needs attention
     */
    public boolean checkEquipmentCondition() {
        if (!degradationMonitoring) return false;
        
        // Check for degraded items (simplified - can be expanded)
        for (Item item : Equipment.all()) {
            if (item != null && isDegradedItem(item)) {
                Logger.warn("[EquipmentManager] Degraded equipment detected: " + item.getName());
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Check if item is degraded
     * @param item item to check
     * @return true if item is degraded
     */
    private boolean isDegradedItem(Item item) {
        String itemName = item.getName().toLowerCase();
        return itemName.contains("(broken)") || itemName.contains("(degraded)") || 
               itemName.contains("(0)") || itemName.contains("(1)");
    }
    
    /**
     * Get recommended equipment upgrade
     * @return upgrade recommendation or null
     */
    /**
     * Check if an equipment change is needed based on the target NPC.
     * @param targetNpc The NPC being targeted.
     * @return true if an equipment change is recommended.
     */
    public boolean needsEquipmentChange(org.dreambot.api.wrappers.interactive.NPC targetNpc) {
        if (!autoSwitchEnabled) {
            return false;
        }

        // Check for degraded equipment first
        if (checkEquipmentCondition()) {
            Logger.log("[EquipmentManager] Equipment change needed due to item degradation.");
            return true;
        }

        // Determine the optimal combat style for the target
        CombatStyleManager.CombatStyle optimalStyle = getOptimalCombatStyle(targetNpc);

        // If the current style is not optimal, a change is needed
        if (optimalStyle != currentCombatStyle) {
            Logger.log("[EquipmentManager] Equipment change needed for optimal combat style.");
            return true;
        }

        // If the style is correct, check if the equipped items match the set
        if (!isCurrentEquipmentOptimal()) {
            Logger.log("[EquipmentManager] Equipment change needed to match the current set.");
            return true;
        }

        return false;
    }

    /**
     * Determines the optimal combat style for a given NPC.
     * @param targetNpc The NPC to evaluate.
     * @return The optimal combat style.
     */
    private CombatStyleManager.CombatStyle getOptimalCombatStyle(org.dreambot.api.wrappers.interactive.NPC targetNpc) {
        if (targetNpc == null || targetNpc.getName() == null) {
            return CombatStyleManager.CombatStyle.AGGRESSIVE; // Default
        }

        String npcName = targetNpc.getName().toLowerCase();

        // Placeholder for a more advanced weakness detection system
        if (npcName.contains("dragon")) {
            return CombatStyleManager.CombatStyle.ACCURATE;
        } else if (npcName.contains("demon")) {
            return CombatStyleManager.CombatStyle.AGGRESSIVE;
        } else if (npcName.contains("golem")) {
            return CombatStyleManager.CombatStyle.DEFENSIVE;
        }

        // Default to the current style if no specific weakness is found
        return currentCombatStyle;
    }

    public String getUpgradeRecommendation() {
        int combatLevel = Skills.getRealLevel(Skill.ATTACK) + Skills.getRealLevel(Skill.STRENGTH) + Skills.getRealLevel(Skill.DEFENCE);
        
        // Simple upgrade recommendations based on combat level
        if (combatLevel >= 60 && currentWeaponType == WeaponType.MELEE_SLASH) {
            return "Consider upgrading to Dragon scimitar or higher";
        }
        
        if (combatLevel >= 70 && currentWeaponType == WeaponType.RANGED_BOW) {
            return "Consider upgrading to Magic longbow or higher";
        }
        
        if (combatLevel >= 50 && currentWeaponType == WeaponType.MAGIC_STAFF) {
            return "Consider upgrading to Mystic staff or higher";
        }
        
        return null;
    }
    
    // Getters and setters
    public WeaponType getCurrentWeaponType() { return currentWeaponType; }
    public CombatStyleManager.CombatStyle getCurrentCombatStyle() { return currentCombatStyle; }

    /**
     * Switch to the optimal equipment set based on the target's weakness.
     * @param targetNpc The NPC being targeted.
     * @return true if the switch was successful.
     */
    public boolean switchToOptimalEquipment(org.dreambot.api.wrappers.interactive.NPC targetNpc) {
        if (targetNpc == null) {
            Logger.error("[EquipmentManager] Target NPC is null, cannot determine optimal equipment.");
            return false;
        }

        CombatStyleManager.CombatStyle optimalStyle = getOptimalCombatStyle(targetNpc);

        Logger.log("[EquipmentManager] Optimal combat style for " + targetNpc.getName() + " is " + optimalStyle);
        return switchToEquipmentSet(optimalStyle);
    }
    public boolean isAutoSwitchEnabled() { return autoSwitchEnabled; }
    public void setAutoSwitchEnabled(boolean enabled) { this.autoSwitchEnabled = enabled; }
    public boolean isDegradationMonitoring() { return degradationMonitoring; }
    public void setDegradationMonitoring(boolean enabled) { this.degradationMonitoring = enabled; }
    public int getEquipmentSwitchDelay() { return equipmentSwitchDelay; }
    public void setEquipmentSwitchDelay(int delay) { this.equipmentSwitchDelay = delay; }
    
    /**
     * Get equipment switching success rate
     * @return success rate as percentage
     */
    public double getSuccessRate() {
        if (totalEquipmentSwitches == 0) return 0.0;
        return (double) successfulSwitches / totalEquipmentSwitches * 100.0;
    }
    
    /**
     * Get average equipment switch time
     * @return average time in milliseconds
     */
    public long getAverageSwitchTime() {
        if (successfulSwitches == 0) return 0;
        return totalSwitchTime / successfulSwitches;
    }
    
    /**
     * Get equipment management statistics
     * @return formatted statistics string
     */
    public String getStatistics() {
        return String.format("Equipment Stats - Switches: %d, Success Rate: %.1f%%, Avg Time: %dms",
            totalEquipmentSwitches, getSuccessRate(), getAverageSwitchTime());
    }
    
    /**
     * Reset equipment statistics
     */
    public void resetStatistics() {
        totalEquipmentSwitches = 0;
        successfulSwitches = 0;
        totalSwitchTime = 0;
        Logger.log("[EquipmentManager] Statistics reset");
    }
}
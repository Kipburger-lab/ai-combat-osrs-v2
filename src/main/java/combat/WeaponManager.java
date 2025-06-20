package combat;

import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Advanced Weapon Manager for AI Combat OSRS
 * Handles weapon-specific mechanics, special attacks,
 * and combat optimization for all weapon types
 * 
 * @author TraeAI
 * @version 2.0
 */
public class WeaponManager {
    
    // Weapon categories and properties
    public enum WeaponCategory {
        MELEE, RANGED, MAGIC, SPECIAL
    }
    
    public enum AttackStyle {
        ACCURATE, AGGRESSIVE, DEFENSIVE, CONTROLLED,
        RAPID, LONGRANGE, SPELL, DEFENSIVE_CASTING
    }
    
    // Weapon data structure
    public static class WeaponData {
        private final String name;
        private final WeaponCategory category;
        private final EquipmentManager.WeaponType type;
        private final int attackSpeed;
        private final boolean hasSpecialAttack;
        private final int specialAttackCost;
        private final Map<AttackStyle, Integer> styleBonus = new HashMap<>();
        private final Set<String> requiredAmmo = new HashSet<>();
        
        public WeaponData(String name, WeaponCategory category, EquipmentManager.WeaponType type, int attackSpeed) {
            this.name = name;
            this.category = category;
            this.type = type;
            this.attackSpeed = attackSpeed;
            this.hasSpecialAttack = false;
            this.specialAttackCost = 0;
        }
        
        public WeaponData(String name, WeaponCategory category, EquipmentManager.WeaponType type, 
                         int attackSpeed, int specialAttackCost) {
            this.name = name;
            this.category = category;
            this.type = type;
            this.attackSpeed = attackSpeed;
            this.hasSpecialAttack = true;
            this.specialAttackCost = specialAttackCost;
        }
        
        // Getters
        public String getName() { return name; }
        public WeaponCategory getCategory() { return category; }
        public EquipmentManager.WeaponType getType() { return type; }
        public int getAttackSpeed() { return attackSpeed; }
        public boolean hasSpecialAttack() { return hasSpecialAttack; }
        public int getSpecialAttackCost() { return specialAttackCost; }
        public Map<AttackStyle, Integer> getStyleBonus() { return styleBonus; }
        public Set<String> getRequiredAmmo() { return requiredAmmo; }
        
        public WeaponData addStyleBonus(AttackStyle style, int bonus) {
            styleBonus.put(style, bonus);
            return this;
        }
        
        public WeaponData addRequiredAmmo(String ammoType) {
            requiredAmmo.add(ammoType);
            return this;
        }
    }
    
    // Weapon database and current state
    private final Map<String, WeaponData> weaponDatabase = new ConcurrentHashMap<>();
    private WeaponData currentWeapon;
    private AttackStyle currentAttackStyle = AttackStyle.ACCURATE;
    
    // Special attack management
    private boolean specialAttackEnabled = true;
    private int minSpecialAttackEnergy = 25;
    private int maxSpecialAttackEnergy = 100;
    private final Set<String> specialAttackTargets = new HashSet<>();
    
// Ammunition tracking
    private final Map<String, Integer> ammunitionCount = new ConcurrentHashMap<>();
    private int minAmmunitionThreshold = 50;
    
    // Combat optimization
    private boolean autoStyleSwitching = true;
    private boolean ammunitionManagement = true;
    private int weaponSwitchDelay = 800;
    
    // Statistics
    private int totalSpecialAttacks = 0;
    private int successfulSpecialAttacks = 0;
    private int totalWeaponSwitches = 0;
    private long totalCombatTime = 0;
    
    public WeaponManager() {
        initializeWeaponDatabase();
        initializeSpecialAttackTargets();
        updateCurrentWeapon();
        Logger.log("[WeaponManager] Initialized with advanced weapon management");
    }
    
    /**
     * Initialize comprehensive weapon database
     */
    private void initializeWeaponDatabase() {
        // Melee weapons with special attacks
        weaponDatabase.put("Dragon dagger", new WeaponData("Dragon dagger", WeaponCategory.MELEE, 
            EquipmentManager.WeaponType.MELEE_STAB, 4, 25)
            .addStyleBonus(AttackStyle.ACCURATE, 3)
            .addStyleBonus(AttackStyle.AGGRESSIVE, 1));
            
        weaponDatabase.put("Dragon longsword", new WeaponData("Dragon longsword", WeaponCategory.MELEE, 
            EquipmentManager.WeaponType.MELEE_SLASH, 5, 25)
            .addStyleBonus(AttackStyle.ACCURATE, 2)
            .addStyleBonus(AttackStyle.AGGRESSIVE, 2));
            
        weaponDatabase.put("Dragon scimitar", new WeaponData("Dragon scimitar", WeaponCategory.MELEE, 
            EquipmentManager.WeaponType.MELEE_SLASH, 4, 55)
            .addStyleBonus(AttackStyle.ACCURATE, 2)
            .addStyleBonus(AttackStyle.AGGRESSIVE, 1));
            
        weaponDatabase.put("Abyssal whip", new WeaponData("Abyssal whip", WeaponCategory.MELEE, 
            EquipmentManager.WeaponType.MELEE_SLASH, 4)
            .addStyleBonus(AttackStyle.ACCURATE, 3)
            .addStyleBonus(AttackStyle.CONTROLLED, 1));
            
        weaponDatabase.put("Dragon 2h sword", new WeaponData("Dragon 2h sword", WeaponCategory.MELEE, 
            EquipmentManager.WeaponType.MELEE_SLASH, 7, 60)
            .addStyleBonus(AttackStyle.ACCURATE, 3)
            .addStyleBonus(AttackStyle.AGGRESSIVE, 3));
            
        // Ranged weapons
        weaponDatabase.put("Magic shortbow", new WeaponData("Magic shortbow", WeaponCategory.RANGED, 
            EquipmentManager.WeaponType.RANGED_BOW, 5, 55)
            .addRequiredAmmo("arrow")
            .addStyleBonus(AttackStyle.ACCURATE, 3)
            .addStyleBonus(AttackStyle.RAPID, 1));
            
        weaponDatabase.put("Magic longbow", new WeaponData("Magic longbow", WeaponCategory.RANGED, 
            EquipmentManager.WeaponType.RANGED_BOW, 6)
            .addRequiredAmmo("arrow")
            .addStyleBonus(AttackStyle.ACCURATE, 3)
            .addStyleBonus(AttackStyle.LONGRANGE, 3));
            
        weaponDatabase.put("Rune crossbow", new WeaponData("Rune crossbow", WeaponCategory.RANGED, 
            EquipmentManager.WeaponType.RANGED_CROSSBOW, 5)
            .addRequiredAmmo("bolt")
            .addStyleBonus(AttackStyle.ACCURATE, 3)
            .addStyleBonus(AttackStyle.RAPID, 1));
            
        weaponDatabase.put("Dragon crossbow", new WeaponData("Dragon crossbow", WeaponCategory.RANGED, 
            EquipmentManager.WeaponType.RANGED_CROSSBOW, 5, 20)
            .addRequiredAmmo("bolt")
            .addStyleBonus(AttackStyle.ACCURATE, 3)
            .addStyleBonus(AttackStyle.RAPID, 1));
            
        // Magic weapons
        weaponDatabase.put("Staff of fire", new WeaponData("Staff of fire", WeaponCategory.MAGIC, 
            EquipmentManager.WeaponType.MAGIC_STAFF, 5)
            .addStyleBonus(AttackStyle.SPELL, 2)
            .addStyleBonus(AttackStyle.DEFENSIVE_CASTING, 3));
            
        weaponDatabase.put("Ancient staff", new WeaponData("Ancient staff", WeaponCategory.MAGIC, 
            EquipmentManager.WeaponType.MAGIC_STAFF, 5)
            .addStyleBonus(AttackStyle.SPELL, 3)
            .addStyleBonus(AttackStyle.DEFENSIVE_CASTING, 3));
            
        weaponDatabase.put("Trident of the seas", new WeaponData("Trident of the seas", WeaponCategory.MAGIC, 
            EquipmentManager.WeaponType.MAGIC_STAFF, 4, 100)
            .addStyleBonus(AttackStyle.SPELL, 4)
            .addStyleBonus(AttackStyle.DEFENSIVE_CASTING, 2));
            
        Logger.log("[WeaponManager] Weapon database initialized with " + weaponDatabase.size() + " weapons");
    }
    
    /**
     * Initialize special attack target preferences
     */
    private void initializeSpecialAttackTargets() {
        // High-value targets for special attacks
        specialAttackTargets.add("Dragon");
        specialAttackTargets.add("Demon");
        specialAttackTargets.add("Boss");
        specialAttackTargets.add("Giant");
        specialAttackTargets.add("King");
        specialAttackTargets.add("Queen");
        
        Logger.log("[WeaponManager] Special attack targets initialized: " + specialAttackTargets.size() + " types");
    }
    
    /**
     * Update current weapon information
     */
    public void updateCurrentWeapon() {
        Item weapon = Equipment.getItemInSlot(EquipmentSlot.WEAPON);
        if (weapon != null) {
            currentWeapon = weaponDatabase.get(weapon.getName());
            if (currentWeapon != null) {
                Logger.log("[WeaponManager] Current weapon updated: " + currentWeapon.getName() + 
                          " (Category: " + currentWeapon.getCategory() + ", Speed: " + currentWeapon.getAttackSpeed() + ")");
                updateAmmunitionCount();
            } else {
                Logger.warn("[WeaponManager] Unknown weapon equipped: " + weapon.getName());
            }
        } else {
            currentWeapon = null;
            Logger.log("[WeaponManager] No weapon equipped");
        }
    }
    
    /**
     * Update ammunition count for ranged weapons
     */
    private void updateAmmunitionCount() {
        if (currentWeapon != null && currentWeapon.getCategory() == WeaponCategory.RANGED) {
            ammunitionCount.clear();
            
            for (String ammoType : currentWeapon.getRequiredAmmo()) {
                int count = 0;
                
                // Count ammunition in inventory
                for (Item item : Inventory.all()) {
                    if (item != null && item.getName().toLowerCase().contains(ammoType)) {
                        count += item.getAmount();
                    }
                }
                
                // Count equipped ammunition
                Item equippedAmmo = Equipment.getItemInSlot(EquipmentSlot.ARROWS);
                if (equippedAmmo != null && equippedAmmo.getName().toLowerCase().contains(ammoType)) {
                    count += equippedAmmo.getAmount();
                }
                
                ammunitionCount.put(ammoType, count);
                Logger.log("[WeaponManager] " + ammoType + " count: " + count);
            }
        }
    }
    
    /**
     * Optimize attack style based on target and situation
     * @param target target NPC
     * @return optimal attack style
     */
    public AttackStyle getOptimalAttackStyle(NPC target) {
        if (currentWeapon == null || !autoStyleSwitching) {
            return currentAttackStyle;
        }
        
        // Consider target's combat level and type
        int targetLevel = target.getLevel();
        String targetName = target.getName().toLowerCase();
        
        switch (currentWeapon.getCategory()) {
            case MELEE:
                return getOptimalMeleeStyle(target, targetLevel, targetName);
            case RANGED:
                return getOptimalRangedStyle(target, targetLevel, targetName);
            case MAGIC:
                return getOptimalMagicStyle(target, targetLevel, targetName);
            default:
                return AttackStyle.ACCURATE;
        }
    }
    
    /**
     * Get optimal melee attack style
     */
    private AttackStyle getOptimalMeleeStyle(NPC target, int targetLevel, String targetName) {
        int combatLevel = Skills.getRealLevel(Skill.ATTACK) + Skills.getRealLevel(Skill.STRENGTH) + Skills.getRealLevel(Skill.DEFENCE);
        
        // Use aggressive for faster kills on weak targets
        if (targetLevel < combatLevel - 20) {
            return AttackStyle.AGGRESSIVE;
        }
        
        // Use defensive against strong targets
        if (targetLevel > combatLevel + 10) {
            return AttackStyle.DEFENSIVE;
        }
        
        // Use controlled for balanced training
        if (Skills.getRealLevel(Skill.ATTACK) < Skills.getRealLevel(Skill.STRENGTH) - 5) {
            return AttackStyle.CONTROLLED;
        }
        
        return AttackStyle.ACCURATE;
    }
    
    /**
     * Get optimal ranged attack style
     */
    private AttackStyle getOptimalRangedStyle(NPC target, int targetLevel, String targetName) {
        // Use rapid for DPS
        int combatLevel = Skills.getRealLevel(Skill.ATTACK) + Skills.getRealLevel(Skill.STRENGTH) + Skills.getRealLevel(Skill.DEFENCE);
        if (targetLevel < combatLevel) {
            return AttackStyle.RAPID;
        }
        
        // Use longrange for safety
        if (targetLevel > combatLevel + 15) {
            return AttackStyle.LONGRANGE;
        }
        
        return AttackStyle.ACCURATE;
    }
    
    /**
     * Get optimal magic attack style
     */
    private AttackStyle getOptimalMagicStyle(NPC target, int targetLevel, String targetName) {
        // Use defensive casting against strong melee targets
        int combatLevel = Skills.getRealLevel(Skill.ATTACK) + Skills.getRealLevel(Skill.STRENGTH) + Skills.getRealLevel(Skill.DEFENCE);
        if (targetLevel > combatLevel + 10 && !targetName.contains("mage")) {
            return AttackStyle.DEFENSIVE_CASTING;
        }
        
        return AttackStyle.SPELL;
    }
    
    /**
     * Execute special attack if conditions are met
     * @param target target for special attack
     * @return true if special attack was executed
     */
    public boolean executeSpecialAttack(NPC target) {
        if (!specialAttackEnabled || currentWeapon == null || !currentWeapon.hasSpecialAttack()) {
            return false;
        }
        
        int currentSpecEnergy = Combat.getSpecialPercentage();
        if (currentSpecEnergy < currentWeapon.getSpecialAttackCost()) {
            return false;
        }
        
        // Check if target is worth using special attack on
        if (!isSpecialAttackTarget(target)) {
            return false;
        }
        
        // Check energy thresholds
        if (currentSpecEnergy < minSpecialAttackEnergy || 
            (currentSpecEnergy < maxSpecialAttackEnergy && !isHighPriorityTarget(target))) {
            return false;
        }
        
        Logger.log("[WeaponManager] Executing special attack with " + currentWeapon.getName() + 
                  " on " + target.getName() + " (Energy: " + currentSpecEnergy + "%)");
        
        totalSpecialAttacks++;
        
        if (Combat.toggleSpecialAttack(true)) {
            Sleep.sleep(100, 300);
            
            if (target.interact("Attack")) {
                successfulSpecialAttacks++;
                Logger.log("[WeaponManager] Special attack executed successfully");
                
                // Wait for special attack to complete
                Sleep.sleep(currentWeapon.getAttackSpeed() * 600);
                
                // Turn off special attack
                Combat.toggleSpecialAttack(false);
                return true;
            }
        }
        
        Logger.error("[WeaponManager] Failed to execute special attack");
        return false;
    }
    
    /**
     * Check if target is suitable for special attack
     * @param target target NPC
     * @return true if target is suitable
     */
    private boolean isSpecialAttackTarget(NPC target) {
        if (target == null) return false;
        
        String targetName = target.getName().toLowerCase();
        
        // Check against special attack target list
        for (String specialTarget : specialAttackTargets) {
            if (targetName.contains(specialTarget.toLowerCase())) {
                return true;
            }
        }
        
        // High-level targets are worth special attacks
        int combatLevel = Skills.getRealLevel(Skill.ATTACK) + Skills.getRealLevel(Skill.STRENGTH) + Skills.getRealLevel(Skill.DEFENCE);
        return target.getLevel() > combatLevel;
    }
    
    /**
     * Check if target is high priority for special attacks
     * @param target target NPC
     * @return true if high priority
     */
    private boolean isHighPriorityTarget(NPC target) {
        if (target == null) return false;
        
        String targetName = target.getName().toLowerCase();
        return targetName.contains("boss") || targetName.contains("king") || 
               targetName.contains("queen") || target.getLevel() > Skills.getRealLevel(Skill.ATTACK) + Skills.getRealLevel(Skill.STRENGTH) + Skills.getRealLevel(Skill.DEFENCE) + 20;
    }
    
    /**
     * Check if ammunition is sufficient for ranged combat
     * @return true if ammunition is sufficient
     */
    public boolean hasAdequateAmmunition() {
        if (currentWeapon == null || currentWeapon.getCategory() != WeaponCategory.RANGED) {
            return true; // Not applicable for non-ranged weapons
        }
        
        for (Map.Entry<String, Integer> ammo : ammunitionCount.entrySet()) {
            if (ammo.getValue() < minAmmunitionThreshold) {
                Logger.warn("[WeaponManager] Low ammunition: " + ammo.getKey() + " (" + ammo.getValue() + ")");
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Switch to optimal weapon for target
     * @param target target NPC
     * @return true if weapon switch was successful
     */
    public boolean switchToOptimalWeapon(NPC target) {
        if (target == null) return false;
        
        String optimalWeapon = getOptimalWeaponForTarget(target);
        if (optimalWeapon == null || (currentWeapon != null && currentWeapon.getName().equals(optimalWeapon))) {
            return true; // Already using optimal weapon
        }
        
        if (Inventory.contains(optimalWeapon)) {
            Logger.log("[WeaponManager] Switching to optimal weapon: " + optimalWeapon + " for " + target.getName());
            
            totalWeaponSwitches++;
            
            if (Equipment.equip(EquipmentSlot.WEAPON, optimalWeapon)) {
                Sleep.sleep(weaponSwitchDelay);
                updateCurrentWeapon();
                Logger.log("[WeaponManager] Weapon switch successful");
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get optimal weapon name for target
     * @param target target NPC
     * @return optimal weapon name or null
     */
    private String getOptimalWeaponForTarget(NPC target) {
        // Simplified weapon selection logic
        // This can be expanded based on target weaknesses, combat style preferences, etc.
        
        String targetName = target.getName().toLowerCase();
        int targetLevel = target.getLevel();
        
        // Example logic - can be greatly expanded
        if (targetName.contains("dragon") && hasWeapon("Dragon dagger")) {
            return "Dragon dagger"; // Fast special attacks
        }
        
        int combatLevel = Skills.getRealLevel(Skill.ATTACK) + Skills.getRealLevel(Skill.STRENGTH) + Skills.getRealLevel(Skill.DEFENCE);
        if (targetLevel > combatLevel + 20 && hasWeapon("Magic longbow")) {
            return "Magic longbow"; // Safe ranging
        }
        
        return null;
    }
    
    /**
     * Check if player has specific weapon
     * @param weaponName weapon name
     * @return true if weapon is available
     */
    private boolean hasWeapon(String weaponName) {
        return Inventory.contains(weaponName) || 
               (Equipment.getItemInSlot(EquipmentSlot.WEAPON) != null && 
                Equipment.getItemInSlot(EquipmentSlot.WEAPON).getName().equals(weaponName));
    }
    
    /**
     * Get weapon effectiveness against target
     * @param weaponName weapon name
     * @param target target NPC
     * @return effectiveness score (higher is better)
     */
    public int getWeaponEffectiveness(String weaponName, NPC target) {
        WeaponData weapon = weaponDatabase.get(weaponName);
        if (weapon == null || target == null) return 0;
        
        int effectiveness = 0;
        
        // Base effectiveness from weapon stats
        effectiveness += (10 - weapon.getAttackSpeed()) * 2; // Faster weapons get bonus
        
        // Special attack bonus
        if (weapon.hasSpecialAttack() && isSpecialAttackTarget(target)) {
            effectiveness += 5;
        }
        
        // Category-specific bonuses
        switch (weapon.getCategory()) {
            case MELEE:
                effectiveness += getMeleeEffectiveness(target);
                break;
            case RANGED:
                effectiveness += getRangedEffectiveness(target);
                break;
            case MAGIC:
                effectiveness += getMagicEffectiveness(target);
                break;
        }
        
        return effectiveness;
    }
    
    private int getMeleeEffectiveness(NPC target) {
        // Melee is effective against most targets but risky against ranged/magic
        return target.getName().toLowerCase().contains("mage") ? -2 : 3;
    }
    
    private int getRangedEffectiveness(NPC target) {
        // Ranged is safe and effective against most targets
        return 2;
    }
    
    private int getMagicEffectiveness(NPC target) {
        // Magic is effective against armored targets
        return target.getLevel() > 50 ? 4 : 1;
    }
    
    // Getters and setters
    public WeaponData getCurrentWeapon() { return currentWeapon; }
    public AttackStyle getCurrentAttackStyle() { return currentAttackStyle; }
    public void setCurrentAttackStyle(AttackStyle style) { this.currentAttackStyle = style; }
    public boolean isSpecialAttackEnabled() { return specialAttackEnabled; }
    public void setSpecialAttackEnabled(boolean enabled) { this.specialAttackEnabled = enabled; }
    public int getMinSpecialAttackEnergy() { return minSpecialAttackEnergy; }
    public void setMinSpecialAttackEnergy(int energy) { this.minSpecialAttackEnergy = energy; }
    public int getMaxSpecialAttackEnergy() { return maxSpecialAttackEnergy; }
    public void setMaxSpecialAttackEnergy(int energy) { this.maxSpecialAttackEnergy = energy; }
    public boolean isAutoStyleSwitching() { return autoStyleSwitching; }
    public void setAutoStyleSwitching(boolean enabled) { this.autoStyleSwitching = enabled; }
    public boolean isAmmunitionManagement() { return ammunitionManagement; }
    public void setAmmunitionManagement(boolean enabled) { this.ammunitionManagement = enabled; }
    public int getMinAmmunitionThreshold() { return minAmmunitionThreshold; }
    public void setMinAmmunitionThreshold(int threshold) { this.minAmmunitionThreshold = threshold; }
    
    /**
     * Get special attack success rate
     * @return success rate as percentage
     */
    public double getSpecialAttackSuccessRate() {
        if (totalSpecialAttacks == 0) return 0.0;
        return (double) successfulSpecialAttacks / totalSpecialAttacks * 100.0;
    }
    
    /**
     * Get weapon management statistics
     * @return formatted statistics string
     */
    public String getStatistics() {
        return String.format("Weapon Stats - Special Attacks: %d (%.1f%% success), Weapon Switches: %d",
            totalSpecialAttacks, getSpecialAttackSuccessRate(), totalWeaponSwitches);
    }
    
    /**
     * Reset weapon statistics
     */
    public void resetStatistics() {
        totalSpecialAttacks = 0;
        successfulSpecialAttacks = 0;
        totalWeaponSwitches = 0;
        totalCombatTime = 0;
        Logger.log("[WeaponManager] Statistics reset");
    }
}
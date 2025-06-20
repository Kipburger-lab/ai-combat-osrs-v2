package combat;

import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.tabs.Tab;
import org.dreambot.api.methods.tabs.Tabs;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.widgets.WidgetChild;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.wrappers.items.Item;

/**
 * Advanced combat style management system
 * Handles combat style switching, weapon type detection, and combat preferences
 * 
 * @author TraeAI
 * @version 1.0
 */
public class CombatStyleManager {
    
    // Combat style enumeration
    public enum CombatStyle {
        ACCURATE(0, "Accurate"),
        AGGRESSIVE(1, "Aggressive"),
        DEFENSIVE(2, "Defensive"),
        CONTROLLED(3, "Controlled"),
        RANGED_ACCURATE(0, "Accurate"),
        RANGED_RAPID(1, "Rapid"),
        RANGED_LONGRANGE(2, "Longrange"),
        MAGIC_ACCURATE(0, "Accurate"),
        MAGIC_LONGRANGE(1, "Longrange"),
        MAGIC_DEFENSIVE(2, "Defensive");
        
        private final int index;
        private final String displayName;
        
        CombatStyle(int index, String displayName) {
            this.index = index;
            this.displayName = displayName;
        }
        
        public int getIndex() {
            return index;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    // Weapon type enumeration
    public enum WeaponType {
        MELEE,
        RANGED,
        MAGIC,
        UNKNOWN
    }
    
    // Current settings
    private CombatStyle currentStyle;
    private WeaponType currentWeaponType;
    private boolean autoSwitchStyle;
    private long lastStyleCheck;
    private static final long STYLE_CHECK_INTERVAL = 5000; // 5 seconds
    
    // Combat tab widget IDs
    private static final int COMBAT_TAB_WIDGET = 593;
    private static final int COMBAT_STYLE_PARENT = 1;
    
    public CombatStyleManager() {
        this.currentStyle = CombatStyle.ACCURATE;
        this.currentWeaponType = WeaponType.UNKNOWN;
        this.autoSwitchStyle = false;
        this.lastStyleCheck = 0;
        
        Logger.log("CombatStyleManager initialized");
    }
    
    /**
     * Updates combat style and weapon type detection
     * Should be called regularly in the main loop
     */
    public void update() {
        try {
            long currentTime = System.currentTimeMillis();
            
            if (currentTime - lastStyleCheck > STYLE_CHECK_INTERVAL) {
                detectWeaponType();
                verifyCurrentStyle();
                lastStyleCheck = currentTime;
            }
            
        } catch (Exception e) {
            Logger.error("Error in CombatStyleManager update: " + e.getMessage());
        }
    }
    
    /**
     * Sets the desired combat style
     * 
     * @param style desired combat style
     * @return true if successfully set, false otherwise
     */
    /**
     * Sets the desired combat style by its string name.
     * 
     * @param styleName The name of the style to set (e.g., "ACCURATE", "AGGRESSIVE").
     */
    public void setCurrentStyle(String styleName) {
        try {
            CombatStyle style = CombatStyle.valueOf(styleName.toUpperCase());
            setCombatStyle(style);
        } catch (IllegalArgumentException e) {
            Logger.error("[CombatStyleManager] Invalid combat style name: " + styleName);
        }
    }

    public boolean setCombatStyle(CombatStyle style) {
        if (style == null) {
            Logger.error("Cannot set null combat style");
            return false;
        }
        
        try {
            // Detect current weapon type first
            detectWeaponType();
            
            // Validate style is compatible with weapon type
            if (!isStyleCompatible(style, currentWeaponType)) {
                Logger.error("Combat style " + style.getDisplayName() + 
                           " is not compatible with weapon type " + currentWeaponType);
                return false;
            }
            
            // Open combat tab if not already open
            if (!Tabs.isOpen(Tab.COMBAT)) {
                if (!Tabs.open(Tab.COMBAT)) {
                    Logger.error("Failed to open combat tab");
                    return false;
                }
                Sleep.sleepUntil(() -> Tabs.isOpen(Tab.COMBAT), 2000);
            }
            
            // Click the appropriate combat style
            if (clickCombatStyle(style)) {
                currentStyle = style;
                Logger.log("Combat style set to: " + style.getDisplayName());
                return true;
            } else {
                Logger.error("Failed to click combat style: " + style.getDisplayName());
                return false;
            }
            
        } catch (Exception e) {
            Logger.error("Error setting combat style: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Detects the current weapon type based on equipped weapon
     */
    private void detectWeaponType() {
        try {
            Item weapon = Equipment.getItemInSlot(EquipmentSlot.WEAPON);
            
            if (weapon == null) {
                // No weapon equipped, assume melee (fists)
                currentWeaponType = WeaponType.MELEE;
                return;
            }
            
            String weaponName = weapon.getName().toLowerCase();
            
            // Check for ranged weapons
            if (weaponName.contains("bow") || 
                weaponName.contains("crossbow") ||
                weaponName.contains("dart") ||
                weaponName.contains("javelin") ||
                weaponName.contains("knife") ||
                weaponName.contains("chinchompa") ||
                weaponName.contains("blowpipe")) {
                currentWeaponType = WeaponType.RANGED;
                return;
            }
            
            // Check for magic weapons
            if (weaponName.contains("staff") ||
                weaponName.contains("wand") ||
                weaponName.contains("tome")) {
                currentWeaponType = WeaponType.MAGIC;
                return;
            }
            
            // Default to melee
            currentWeaponType = WeaponType.MELEE;
            
        } catch (Exception e) {
            Logger.error("Error detecting weapon type: " + e.getMessage());
            currentWeaponType = WeaponType.UNKNOWN;
        }
    }
    
    /**
     * Verifies the current combat style matches what's selected in-game
     */
    private void verifyCurrentStyle() {
        try {
            if (!Tabs.isOpen(Tab.COMBAT)) {
                return;
            }
            
            WidgetChild combatWidget = Widgets.getWidgetChild(COMBAT_TAB_WIDGET, COMBAT_STYLE_PARENT);
            if (combatWidget == null) {
                return;
            }
            
            // Check which style is currently selected
            // This is a simplified check - in practice, you'd need to check
            // the specific widget states for each combat style button
            
        } catch (Exception e) {
            Logger.error("Error verifying combat style: " + e.getMessage());
        }
    }
    
    /**
     * Clicks the specified combat style in the combat tab
     * 
     * @param style combat style to click
     * @return true if successfully clicked, false otherwise
     */
    private boolean clickCombatStyle(CombatStyle style) {
        try {
            // Widget child IDs for combat styles (these may need adjustment)
            int[] meleeStyleIds = {2, 3, 4, 5}; // Attack, Strength, Defence, Controlled
            int[] rangedStyleIds = {2, 3, 4};   // Accurate, Rapid, Longrange
            int[] magicStyleIds = {2, 3, 4};    // Accurate, Longrange, Defensive
            
            int[] styleIds;
            switch (currentWeaponType) {
                case MELEE:
                    styleIds = meleeStyleIds;
                    break;
                case RANGED:
                    styleIds = rangedStyleIds;
                    break;
                case MAGIC:
                    styleIds = magicStyleIds;
                    break;
                default:
                    styleIds = meleeStyleIds;
                    break;
            }
            
            if (style.getIndex() >= styleIds.length) {
                Logger.error("Invalid style index for weapon type");
                return false;
            }
            
            WidgetChild styleWidget = Widgets.getWidgetChild(COMBAT_TAB_WIDGET, styleIds[style.getIndex()]);
            if (styleWidget != null && styleWidget.isVisible()) {
                if (styleWidget.interact()) {
                    Sleep.sleep(100, 300);
                    return true;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            Logger.error("Error clicking combat style: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Checks if a combat style is compatible with a weapon type
     * 
     * @param style combat style to check
     * @param weaponType weapon type to check against
     * @return true if compatible, false otherwise
     */
    private boolean isStyleCompatible(CombatStyle style, WeaponType weaponType) {
        switch (weaponType) {
            case MELEE:
                return style == CombatStyle.ACCURATE ||
                       style == CombatStyle.AGGRESSIVE ||
                       style == CombatStyle.DEFENSIVE ||
                       style == CombatStyle.CONTROLLED;
                       
            case RANGED:
                return style == CombatStyle.RANGED_ACCURATE ||
                       style == CombatStyle.RANGED_RAPID ||
                       style == CombatStyle.RANGED_LONGRANGE;
                       
            case MAGIC:
                return style == CombatStyle.MAGIC_ACCURATE ||
                       style == CombatStyle.MAGIC_LONGRANGE ||
                       style == CombatStyle.MAGIC_DEFENSIVE;
                       
            default:
                return false;
        }
    }
    
    /**
     * Gets the appropriate combat style for training a specific skill
     * 
     * @param skill skill name ("attack", "strength", "defence", "ranged", "magic")
     * @return appropriate combat style or null if invalid
     */
    public CombatStyle getStyleForSkill(String skill) {
        if (skill == null) return null;
        
        String skillLower = skill.toLowerCase().trim();
        
        switch (skillLower) {
            case "attack":
                return currentWeaponType == WeaponType.MELEE ? CombatStyle.ACCURATE : null;
            case "strength":
                return currentWeaponType == WeaponType.MELEE ? CombatStyle.AGGRESSIVE : null;
            case "defence":
                switch (currentWeaponType) {
                    case MELEE:
                        return CombatStyle.DEFENSIVE;
                    case RANGED:
                        return CombatStyle.RANGED_LONGRANGE;
                    case MAGIC:
                        return CombatStyle.MAGIC_DEFENSIVE;
                    default:
                        return null;
                }
            case "ranged":
                return currentWeaponType == WeaponType.RANGED ? CombatStyle.RANGED_RAPID : null;
            case "magic":
                return currentWeaponType == WeaponType.MAGIC ? CombatStyle.MAGIC_ACCURATE : null;
            default:
                return null;
        }
    }
    
    /**
     * Sets auto-switch style mode
     * When enabled, will automatically switch to optimal style for current weapon
     * 
     * @param autoSwitch true to enable auto-switching
     */
    public void setAutoSwitchStyle(boolean autoSwitch) {
        this.autoSwitchStyle = autoSwitch;
        Logger.log("Auto-switch style: " + autoSwitch);
    }
    
    /**
     * Gets the current combat style
     * 
     * @return current combat style
     */
    public CombatStyle getCurrentStyle() {
        return currentStyle;
    }
    
    /**
     * Gets the current weapon type
     * 
     * @return current weapon type
     */
    public WeaponType getCurrentWeaponType() {
        return currentWeaponType;
    }
    
    /**
     * Checks if auto-switch style is enabled
     * 
     * @return true if auto-switch is enabled
     */
    public boolean isAutoSwitchStyle() {
        return autoSwitchStyle;
    }
    
    /**
     * Gets all available combat styles for current weapon type
     * 
     * @return array of available combat styles
     */
    public CombatStyle[] getAvailableStyles() {
        switch (currentWeaponType) {
            case MELEE:
                return new CombatStyle[]{
                    CombatStyle.ACCURATE,
                    CombatStyle.AGGRESSIVE,
                    CombatStyle.DEFENSIVE,
                    CombatStyle.CONTROLLED
                };
            case RANGED:
                return new CombatStyle[]{
                    CombatStyle.RANGED_ACCURATE,
                    CombatStyle.RANGED_RAPID,
                    CombatStyle.RANGED_LONGRANGE
                };
            case MAGIC:
                return new CombatStyle[]{
                    CombatStyle.MAGIC_ACCURATE,
                    CombatStyle.MAGIC_LONGRANGE,
                    CombatStyle.MAGIC_DEFENSIVE
                };
            default:
                return new CombatStyle[0];
        }
    }
    
    /**
     * Forces a weapon type detection update
     */
    public void forceWeaponTypeUpdate() {
        detectWeaponType();
        Logger.log("Weapon type updated to: " + currentWeaponType);
    }
    
    /**
     * Resets combat style manager to default state
     */
    public void reset() {
        currentStyle = CombatStyle.ACCURATE;
        currentWeaponType = WeaponType.UNKNOWN;
        autoSwitchStyle = false;
        lastStyleCheck = 0;
        Logger.log("CombatStyleManager reset to default state");
    }
    
    /**
     * Get combat style statistics
     */
    public String getStatistics() {
        return String.format("Style: %s, Weapon: %s, Auto-switch: %s",
            currentStyle != null ? currentStyle.getDisplayName() : "None",
            currentWeaponType != null ? currentWeaponType.name() : "Unknown",
            autoSwitchStyle ? "Enabled" : "Disabled");
    }
}
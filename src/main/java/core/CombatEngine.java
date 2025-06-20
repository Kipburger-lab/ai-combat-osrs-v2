package core;

import org.dreambot.api.methods.combat.Combat;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.methods.Calculations;

import combat.TargetSelector;
import combat.CombatStyleManager;
import combat.CombatStyleManager.CombatStyle;

import antiban.AntiBanManager;

/**
 * Core combat engine responsible for managing all combat operations
 * Handles target selection, combat execution, and combat state management
 * 
 * @author TraeAI
 * @version 1.0
 */
public class CombatEngine {
    
    private TargetSelector targetSelector;
    private CombatStyleManager styleManager;
    private CombatStyle currentCombatStyle;
    private NPC currentTarget;
    private boolean isInCombat;
    private long lastAttackTime;
    private int consecutiveMisses;
    private AntiBanManager antiBanManager;
    
    // Combat statistics
    private int totalKills;
    private long combatStartTime;
    private double totalXpGained;
    
    // Configuration
    private static final int MAX_CONSECUTIVE_MISSES = 5;
    private static final int COMBAT_TIMEOUT = 10000; // 10 seconds
    private static final int MIN_HEALTH_THRESHOLD = 20; // Minimum health percentage
    
    public CombatEngine() {
        this.targetSelector = new TargetSelector();
        this.currentCombatStyle = CombatStyle.CONTROLLED;
        this.isInCombat = false;
        this.lastAttackTime = 0;
        this.consecutiveMisses = 0;
        this.totalKills = 0;
        this.combatStartTime = System.currentTimeMillis();
        this.totalXpGained = 0;
        
        Logger.log("CombatEngine initialized successfully");
    }
    
    /**
     * Main combat execution method
     * Handles the complete combat cycle
     * 
     * @return sleep time in milliseconds
     */
    public int execute() {
        try {
            // Check if we need to eat food
            if (shouldEat()) {
                return eatFood();
            }
            
            // Check if we're already in combat
            if (isInCombat()) {
                return handleCombat();
            }
            
            // Find and attack new target
            return findAndAttackTarget();
            
        } catch (Exception e) {
            Logger.error("Error in CombatEngine.execute(): " + e.getMessage());
            return Calculations.random(1000, 2000);
        }
    }
    
    /**
     * Checks if player should eat food based on health
     * 
     * @return true if should eat, false otherwise
     */
    private boolean shouldEat() {
        Player localPlayer = Players.getLocal();
        if (localPlayer == null) return false;
        
        int currentHealth = Skills.getBoostedLevel(Skill.HITPOINTS);
        int maxHealth = Skills.getRealLevel(Skill.HITPOINTS);
        
        double healthPercentage = (double) currentHealth / maxHealth * 100;
        
        return healthPercentage <= MIN_HEALTH_THRESHOLD;
    }
    
    /**
     * Checks if player needs food based on health
     * 
     * @return true if needs food, false otherwise
     */
    public boolean needsFood() {
        return shouldEat();
    }
    
    /**
     * Attempts to eat food from inventory
     * 
     * @return sleep time in milliseconds
     */
    public int eatFood() {
        // TODO: Implement food consumption logic
        Logger.log("Health is low, attempting to eat food...");
        
        // For now, just log and return
        return Calculations.random(800, 1200);
    }
    
    /**
     * Checks if player is currently in combat
     * 
     * @return true if in combat, false otherwise
     */
    private boolean isInCombat() {
        Player localPlayer = Players.getLocal();
        if (localPlayer == null) return false;
        
        // Check if player is being attacked or attacking
        boolean playerInCombat = localPlayer.isInCombat();
        
        // Check if we have a valid target that's still alive and reachable
        boolean hasValidTarget = currentTarget != null && 
                                currentTarget.exists() && 
                                currentTarget.getHealthPercent() > 0 &&
                                currentTarget.isOnScreen();
        
        // Update combat state
        this.isInCombat = playerInCombat || hasValidTarget;
        
        return this.isInCombat;
    }
    
    /**
     * Handles combat when already engaged with a target
     * 
     * @return sleep time in milliseconds
     */
    private int handleCombat() {
        if (currentTarget == null || !currentTarget.exists() || currentTarget.getHealthPercent() <= 0) {
            Logger.log("Current target is no longer valid, ending combat");
            endCombat();
            return Calculations.random(500, 800);
        }
        
        // Check for combat timeout
        if (System.currentTimeMillis() - lastAttackTime > COMBAT_TIMEOUT) {
            Logger.log("Combat timeout reached, finding new target");
            endCombat();
            return Calculations.random(500, 800);
        }
        
        // Monitor combat progress
        if (!Players.getLocal().isInCombat() && !currentTarget.isInCombat()) {
            consecutiveMisses++;
            if (consecutiveMisses >= MAX_CONSECUTIVE_MISSES) {
                Logger.log("Too many consecutive misses, finding new target");
                endCombat();
                return Calculations.random(500, 800);
            }
        } else {
            consecutiveMisses = 0;
        }
        
        // Wait for combat to continue
        return Calculations.random(600, 1000);
    }
    
    /**
     * Finds and attacks a new target
     * 
     * @return sleep time in milliseconds
     */
    private int findAndAttackTarget() {
        NPC target = targetSelector.selectBestTarget();
        
        if (target == null) {
            Logger.log("No suitable targets found");
            return Calculations.random(1000, 2000);
        }
        
        return attackTarget(target);
    }
    
    /**
     * Attacks the specified target
     * 
     * @param target the NPC to attack
     * @return sleep time in milliseconds
     */
    private int attackTarget(NPC target) {
        if (target == null || !target.exists() || target.getHealthPercent() <= 0) {
            return Calculations.random(500, 800);
        }
        
        // Check if target is reachable
        if (!target.isOnScreen()) {
            Logger.log("Target not on screen, walking closer");
            if (Walking.shouldWalk()) {
                Walking.walk(target.getTile());
                return Calculations.random(1000, 1500);
            }
        }
        
        // Attempt to attack
        if (target.interact("Attack")) {
            Logger.log("Attacking: " + target.getName());
            currentTarget = target;
            lastAttackTime = System.currentTimeMillis();
            consecutiveMisses = 0;
            
            // Wait for attack to register
            Sleep.sleepUntil(() -> Players.getLocal().isInCombat() || 
                                  target.isInCombat(), 
                           Calculations.random(2000, 3000));
            
            return Calculations.random(600, 1000);
        }
        
        Logger.log("Failed to attack target: " + target.getName());
        return Calculations.random(800, 1200);
    }
    
    /**
     * Ends current combat session and resets state
     */
    private void endCombat() {
        if (currentTarget != null && currentTarget.getHealthPercent() <= 0) {
            totalKills++;
            Logger.log("Kill count: " + totalKills);
        }
        
        currentTarget = null;
        isInCombat = false;
        consecutiveMisses = 0;
        lastAttackTime = 0;
    }
    
    /**
     * Sets the combat style for training
     * 
     * @param style the combat style to use
     */
    public void setCombatStyle(CombatStyle style) {
        if (style != null && style != currentCombatStyle) {
            this.currentCombatStyle = style;
            // TODO: Implement combat style switching logic
            Logger.log("Combat style changed to: " + style.name());
        }
    }
    
    /**
     * Gets the current combat style
     * 
     * @return current combat style
     */
    public CombatStyle getCombatStyle() {
        return currentCombatStyle;
    }
    
    /**
     * Gets the current target
     * 
     * @return current target NPC or null
     */
    public NPC getCurrentTarget() {
        return currentTarget;
    }
    
    /**
     * Gets total kills this session
     * 
     * @return total kills
     */
    public int getTotalKills() {
        return totalKills;
    }
    
    /**
     * Gets combat session duration in milliseconds
     * 
     * @return session duration
     */
    public long getSessionDuration() {
        return System.currentTimeMillis() - combatStartTime;
    }
    
    /**
     * Calculates kills per hour
     * 
     * @return kills per hour
     */
    public double getKillsPerHour() {
        long sessionDuration = getSessionDuration();
        if (sessionDuration <= 0) return 0;
        
        return (double) totalKills / (sessionDuration / 3600000.0);
    }
    
    /**
     * Updates target selector configuration
     * 
     * @param selector new target selector
     */
    public void setTargetSelector(TargetSelector selector) {
        if (selector != null) {
            this.targetSelector = selector;
            Logger.log("Target selector updated");
        }
    }
    
    /**
     * Gets current target selector
     * 
     * @return target selector
     */
    public TargetSelector getTargetSelector() {
        return targetSelector;
    }
    
    /**
     * Sets the anti-ban manager
     * 
     * @param antiBanManager the anti-ban manager to use
     */
    public void setAntiBanManager(AntiBanManager antiBanManager) {
        this.antiBanManager = antiBanManager;
        Logger.log("Anti-ban manager set for combat engine");
    }
    
    /**
     * Sets the combat style manager
     * 
     * @param styleManager the combat style manager to use
     */
    public void setStyleManager(CombatStyleManager styleManager) {
        this.styleManager = styleManager;
        Logger.log("Combat style manager set for combat engine");
    }
    
    /**
     * Executes combat operations
     * This is the main method called by the script
     * 
     * @return sleep time in milliseconds
     */
    public int executeCombat() {
        return execute();
    }
    
    /**
     * Updates combat statistics
     */
    public void updateStatistics() {
        // Update any real-time statistics here
        // This method is called periodically to update stats
    }
    
    /**
     * Gets a summary of combat statistics
     * 
     * @return statistics summary string
     */
    public String getStatisticsSummary() {
        long sessionDuration = getSessionDuration();
        double killsPerHour = getKillsPerHour();
        
        return String.format("Kills: %d | Session: %s | KPH: %.1f", 
                           totalKills, 
                           formatDuration(sessionDuration), 
                           killsPerHour);
    }
    
    /**
     * Formats duration in milliseconds to readable string
     * 
     * @param duration duration in milliseconds
     * @return formatted duration string
     */
    private String formatDuration(long duration) {
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        if (hours > 0) {
            return String.format("%dh %dm", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, seconds % 60);
        } else {
            return String.format("%ds", seconds);
        }
    }
}
package combat;

import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.methods.Calculations;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Advanced target selection system for intelligent NPC targeting
 * Supports multiple targeting strategies and filtering options
 * 
 * @author TraeAI
 * @version 1.0
 */
public class TargetSelector {
    
    // Target configuration
    private Set<String> targetNames;
    private Set<Integer> targetIds;
    private Set<String> blacklistedNames;
    private Set<Integer> blacklistedIds;
    
    // Targeting preferences
    private TargetPriority priority;
    private int maxDistance;
    private boolean requireLineOfSight;
    private boolean avoidCombatNpcs;
    private boolean preferSafespots;
    
    // Area restrictions
    private Area allowedArea;
    private boolean stayInArea;
    
    // Combat level restrictions
    private int minCombatLevel;
    private int maxCombatLevel;
    
    /**
     * Target priority enumeration
     */
    public enum TargetPriority {
        CLOSEST,        // Closest target
        HIGHEST_LEVEL,  // Highest combat level
        LOWEST_LEVEL,   // Lowest combat level
        LOWEST_HP,      // Lowest current HP
        HIGHEST_HP,     // Highest current HP
        RANDOM          // Random selection
    }
    
    public TargetSelector() {
        this.targetNames = new HashSet<>();
        this.targetIds = new HashSet<>();
        this.blacklistedNames = new HashSet<>();
        this.blacklistedIds = new HashSet<>();
        
        // Default settings
        this.priority = TargetPriority.CLOSEST;
        this.maxDistance = 10;
        this.requireLineOfSight = true;
        this.avoidCombatNpcs = true;
        this.preferSafespots = false;
        this.stayInArea = false;
        this.minCombatLevel = 1;
        this.maxCombatLevel = 999;
        
        Logger.log("TargetSelector initialized with default settings");
    }
    
    /**
     * Selects the best target based on current configuration
     * 
     * @return best target NPC or null if none found
     */
    public NPC selectNextTarget() {
        return selectBestTarget();
    }

    public NPC selectBestTarget() {
        try {
            List<NPC> candidates = getCandidateTargets();
            
            if (candidates.isEmpty()) {
                return null;
            }
            
            // Filter candidates based on criteria
            candidates = filterCandidates(candidates);
            
            if (candidates.isEmpty()) {
                return null;
            }
            
            // Sort by priority and select best
            return selectByPriority(candidates);
            
        } catch (Exception e) {
            Logger.error("Error in selectBestTarget(): " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Gets initial list of candidate targets
     * 
     * @return list of potential targets
     */
    private List<NPC> getCandidateTargets() {
        List<NPC> candidates = new ArrayList<>();
        
        // Get NPCs by name if specified
        if (!targetNames.isEmpty()) {
            for (String name : targetNames) {
                candidates.addAll(NPCs.all(npc -> npc != null && 
                                          name.equalsIgnoreCase(npc.getName())));
            }
        }
        
        // Get NPCs by ID if specified
        if (!targetIds.isEmpty()) {
            for (Integer id : targetIds) {
                candidates.addAll(NPCs.all(npc -> npc != null && 
                                          npc.getID() == id));
            }
        }
        
        // If no specific targets, get all attackable NPCs
        if (targetNames.isEmpty() && targetIds.isEmpty()) {
            candidates.addAll(NPCs.all(npc -> npc != null && 
                                     npc.hasAction("Attack") &&
                                     npc.getHealthPercent() > 0));
        }
        
        return candidates;
    }
    
    /**
     * Filters candidate targets based on various criteria
     * 
     * @param candidates list of candidate NPCs
     * @return filtered list of NPCs
     */
    private List<NPC> filterCandidates(List<NPC> candidates) {
        Player localPlayer = Players.getLocal();
        if (localPlayer == null) return new ArrayList<>();
        
        return candidates.stream()
            .filter(this::isValidTarget)
            .filter(npc -> !isBlacklisted(npc))
            .filter(npc -> isWithinDistance(npc, localPlayer))
            .filter(npc -> isWithinCombatLevelRange(npc))
            .filter(npc -> !avoidCombatNpcs || !npc.isInCombat())
            .filter(npc -> !stayInArea || isInAllowedArea(npc))
            .filter(npc -> !requireLineOfSight || hasLineOfSight(npc, localPlayer))
            .collect(Collectors.toList());
    }
    
    /**
     * Checks if NPC is a valid target
     * 
     * @param npc NPC to check
     * @return true if valid, false otherwise
     */
    private boolean isValidTarget(NPC npc) {
        return npc != null && 
               npc.exists() && 
               npc.getHealthPercent() > 0 &&
               npc.hasAction("Attack") &&
               npc.isOnScreen();
    }
    
    /**
     * Checks if NPC is blacklisted
     * 
     * @param npc NPC to check
     * @return true if blacklisted, false otherwise
     */
    private boolean isBlacklisted(NPC npc) {
        return blacklistedIds.contains(npc.getID()) ||
               blacklistedNames.contains(npc.getName());
    }
    
    /**
     * Checks if NPC is within maximum distance
     * 
     * @param npc NPC to check
     * @param player local player
     * @return true if within distance, false otherwise
     */
    private boolean isWithinDistance(NPC npc, Player player) {
        return npc.distance(player) <= maxDistance;
    }
    
    /**
     * Checks if NPC is within combat level range
     * 
     * @param npc NPC to check
     * @return true if within range, false otherwise
     */
    private boolean isWithinCombatLevelRange(NPC npc) {
        int level = npc.getLevel();
        return level >= minCombatLevel && level <= maxCombatLevel;
    }
    
    /**
     * Checks if NPC is in allowed area
     * 
     * @param npc NPC to check
     * @return true if in area, false otherwise
     */
    private boolean isInAllowedArea(NPC npc) {
        return allowedArea == null || allowedArea.contains(npc.getTile());
    }
    
    /**
     * Checks if there's line of sight to NPC
     * 
     * @param npc NPC to check
     * @param player local player
     * @return true if line of sight exists, false otherwise
     */
    private boolean hasLineOfSight(NPC npc, Player player) {
        // TODO: Implement proper line of sight checking
        // For now, assume line of sight if NPC is on screen
        return npc.isOnScreen();
    }
    
    /**
     * Selects target based on priority setting
     * 
     * @param candidates filtered list of candidates
     * @return selected target NPC
     */
    private NPC selectByPriority(List<NPC> candidates) {
        Player localPlayer = Players.getLocal();
        if (localPlayer == null || candidates.isEmpty()) {
            return null;
        }
        
        switch (priority) {
            case CLOSEST:
                return candidates.stream()
                    .min(Comparator.comparingDouble(npc -> npc.distance(localPlayer)))
                    .orElse(null);
                    
            case HIGHEST_LEVEL:
                return candidates.stream()
                    .max(Comparator.comparingInt(NPC::getLevel))
                    .orElse(null);
                    
            case LOWEST_LEVEL:
                return candidates.stream()
                    .min(Comparator.comparingInt(NPC::getLevel))
                    .orElse(null);
                    
            case LOWEST_HP:
                return candidates.stream()
                    .min(Comparator.comparingInt(npc -> npc.getHealthPercent()))
                    .orElse(null);
                    
            case HIGHEST_HP:
                return candidates.stream()
                    .max(Comparator.comparingInt(npc -> npc.getHealthPercent()))
                    .orElse(null);
                    
            case RANDOM:
                return candidates.get(Calculations.random(0, candidates.size() - 1));
                
            default:
                return candidates.get(0);
        }
    }
    
    // Configuration methods
    
    /**
     * Adds target name to selection criteria
     * 
     * @param name NPC name to target
     */
    public void addTargetName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            targetNames.add(name.trim());
            Logger.log("Added target name: " + name);
        }
    }
    
    /**
     * Adds target ID to selection criteria
     * 
     * @param id NPC ID to target
     */
    public void addTargetId(int id) {
        targetIds.add(id);
        Logger.log("Added target ID: " + id);
    }
    
    /**
     * Adds name to blacklist
     * 
     * @param name NPC name to blacklist
     */
    public void addBlacklistedName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            blacklistedNames.add(name.trim());
            Logger.log("Blacklisted name: " + name);
        }
    }
    
    /**
     * Adds ID to blacklist
     * 
     * @param id NPC ID to blacklist
     */
    public void addBlacklistedId(int id) {
        blacklistedIds.add(id);
        Logger.log("Blacklisted ID: " + id);
    }
    
    /**
     * Sets target priority
     * 
     * @param priority target selection priority
     */
    public void setPriority(TargetPriority priority) {
        if (priority != null) {
            this.priority = priority;
            Logger.log("Target priority set to: " + priority.name());
        }
    }
    
    /**
     * Sets maximum targeting distance
     * 
     * @param distance maximum distance in tiles
     */
    public void setMaxDistance(int distance) {
        this.maxDistance = Math.max(1, distance);
        Logger.log("Max targeting distance set to: " + this.maxDistance);
    }
    
    /**
     * Sets combat level range
     * 
     * @param min minimum combat level
     * @param max maximum combat level
     */
    public void setCombatLevelRange(int min, int max) {
        this.minCombatLevel = Math.max(1, min);
        this.maxCombatLevel = Math.max(min, max);
        Logger.log("Combat level range set to: " + minCombatLevel + "-" + maxCombatLevel);
    }
    
    /**
     * Sets allowed area for targeting
     * 
     * @param area area to restrict targeting to
     */
    public void setAllowedArea(Area area) {
        this.allowedArea = area;
        this.stayInArea = (area != null);
        Logger.log("Allowed area " + (area != null ? "set" : "cleared"));
    }
    
    /**
     * Sets whether to require line of sight
     * 
     * @param require true to require line of sight
     */
    public void setRequireLineOfSight(boolean require) {
        this.requireLineOfSight = require;
        Logger.log("Require line of sight: " + require);
    }
    
    /**
     * Sets whether to avoid NPCs already in combat
     * 
     * @param avoid true to avoid combat NPCs
     */
    public void setAvoidCombatNpcs(boolean avoid) {
        this.avoidCombatNpcs = avoid;
        Logger.log("Avoid combat NPCs: " + avoid);
    }
    
    /**
     * Clears all target names
     */
    public void clearTargetNames() {
        targetNames.clear();
        Logger.log("Target names cleared");
    }
    
    /**
     * Clears all target IDs
     */
    public void clearTargetIds() {
        targetIds.clear();
        Logger.log("Target IDs cleared");
    }
    
    /**
     * Clears all blacklisted names
     */
    public void clearBlacklistedNames() {
        blacklistedNames.clear();
        Logger.log("Blacklisted names cleared");
    }
    
    /**
     * Clears all blacklisted IDs
     */
    public void clearBlacklistedIds() {
        blacklistedIds.clear();
        Logger.log("Blacklisted IDs cleared");
    }
    
    // Getter methods
    
    public Set<String> getTargetNames() {
        return new HashSet<>(targetNames);
    }
    
    public Set<Integer> getTargetIds() {
        return new HashSet<>(targetIds);
    }
    
    public TargetPriority getPriority() {
        return priority;
    }
    
    public int getMaxDistance() {
        return maxDistance;
    }
    
    public boolean isRequireLineOfSight() {
        return requireLineOfSight;
    }
    
    public boolean isAvoidCombatNpcs() {
        return avoidCombatNpcs;
    }
    
    public int getMinCombatLevel() {
        return minCombatLevel;
    }
    
    public int getMaxCombatLevel() {
        return maxCombatLevel;
    }
}
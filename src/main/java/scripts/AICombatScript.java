package scripts;

import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.Category;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.methods.combat.Combat;

// Import our custom components
import core.CombatEngine;
import combat.TargetSelector;
import combat.CombatStyleManager;
import tasks.TaskManager;
import antiban.AntiBanManager;

@ScriptManifest(
        name = "AI Combat Script",
        description = "Advanced AI-powered combat automation for OSRS with task management, anti-ban, and intelligent targeting",
        author = "TraeAI",
        version = 2.0,
        category = Category.COMBAT
)
public class AICombatScript extends AbstractScript {
    
    // Core components
    private CombatEngine combatEngine;
    private TargetSelector targetSelector;
    private CombatStyleManager styleManager;
    private TaskManager taskManager;
    private AntiBanManager antiBanManager;
    
    // Script state
    private boolean isInitialized;
    private long scriptStartTime;
    private long lastStatusUpdate;
    private int loopCount;
    
    // Configuration (these would normally come from GUI)
    private String targetNpcName = "Cow"; // Default target for testing
    private Skill trainingSkill = Skill.ATTACK;
    private int targetLevel = 10;
    private String combatStyle = "attack";
    
    @Override
    public void onStart() {
        Logger.log("=== AI Combat Script v2.0 Starting ===");
        
        try {
            initializeComponents();
            setupInitialTasks();
            
            scriptStartTime = System.currentTimeMillis();
            lastStatusUpdate = 0;
            loopCount = 0;
            isInitialized = true;
            
            Logger.log("AI Combat Script successfully initialized!");
            Logger.log("Target: " + targetNpcName);
            Logger.log("Training: " + trainingSkill.getName() + " to level " + targetLevel);
            Logger.log("Combat Style: " + combatStyle);
            
        } catch (Exception e) {
            Logger.error("Failed to initialize script: " + e.getMessage());
            e.printStackTrace();
            stop();
        }
    }
    
    /**
     * Initializes all script components
     */
    private void initializeComponents() {
        Logger.log("Initializing script components...");
        
        // Initialize core components
        combatEngine = new CombatEngine();
        targetSelector = new TargetSelector();
        styleManager = new CombatStyleManager();
        taskManager = new TaskManager();
        antiBanManager = new AntiBanManager();
        
        // Configure target selector
        targetSelector.addTargetName(targetNpcName);
        targetSelector.setPriority(TargetSelector.TargetPriority.CLOSEST);
        targetSelector.setMaxDistance(8);
        targetSelector.setAvoidCombatNpcs(true);
        
        // Configure anti-ban
        antiBanManager.setBehaviorProfile(AntiBanManager.BehaviorProfile.NORMAL);
        antiBanManager.setEnabled(true);
        
        // Set up dependencies
        combatEngine.setTargetSelector(targetSelector);
        combatEngine.setStyleManager(styleManager);
        combatEngine.setAntiBanManager(antiBanManager);
        taskManager.setStyleManager(styleManager);
        
        Logger.log("All components initialized successfully");
    }
    
    /**
     * Sets up initial training tasks
     */
    private void setupInitialTasks() {
        Logger.log("Setting up initial tasks...");
        
        // Add a simple training task
        taskManager.addSimpleTask(
            "Train " + trainingSkill.getName() + " to level " + targetLevel,
            targetNpcName,
            trainingSkill,
            targetLevel,
            combatStyle
        );
        
        // Start task manager
        taskManager.start();
        
        Logger.log("Tasks configured and started");
    }

    @Override
    public int onLoop() {
        if (!isInitialized) {
            return 5000; // Wait 5 seconds if not initialized
        }
        
        try {
            loopCount++;
            
            // Update all components
            updateComponents();
            
            // Main combat logic
            if (taskManager.isRunning() && taskManager.getCurrentTask() != null) {
                executeCombatLoop();
            } else {
                Logger.log("No active tasks - script idle");
                return 5000; // Wait 5 seconds when no tasks
            }
            
            // Periodic status updates
            updateStatus();
            
            // Return dynamic sleep time based on current state
            return getDynamicSleepTime();
            
        } catch (Exception e) {
            Logger.error("Error in main loop: " + e.getMessage());
            e.printStackTrace();
            return 2000; // Wait 2 seconds on error
        }
    }
    
    /**
     * Updates all script components
     */
    private void updateComponents() {
        // Update managers
        taskManager.update();
        styleManager.update();
        antiBanManager.update();
        
        // Record action for anti-ban
        if (loopCount % 10 == 0) { // Every 10 loops
            antiBanManager.recordAction("main_loop");
        }
    }
    
    /**
     * Executes the main combat loop
     */
    private void executeCombatLoop() {
        Player localPlayer = Players.getLocal();
        if (localPlayer == null) {
            return;
        }
        
        // Check if we need to eat or handle combat state
        if (combatEngine.needsFood()) {
            combatEngine.eatFood();
            return;
        }
        
        // Execute combat if not already in combat or if we need a new target
        if (!localPlayer.isInCombat() || combatEngine.getCurrentTarget() == null) {
            combatEngine.executeCombat();
        }
        
        // Update combat statistics
        combatEngine.updateStatistics();
    }
    
    /**
     * Updates and logs status information
     */
    private void updateStatus() {
        long currentTime = System.currentTimeMillis();
        
        // Update status every 30 seconds
        if (currentTime - lastStatusUpdate > 30000) {
            logStatusUpdate();
            lastStatusUpdate = currentTime;
        }
    }
    
    /**
     * Logs comprehensive status information
     */
    private void logStatusUpdate() {
        Logger.log("=== Status Update ===");
        
        // Runtime information
        long runtime = System.currentTimeMillis() - scriptStartTime;
        Logger.log("Runtime: " + (runtime / 60000) + " minutes");
        Logger.log("Loop count: " + loopCount);
        
        // Current skill levels
        if (trainingSkill != null) {
            int currentLevel = Skills.getRealLevel(trainingSkill);
            Logger.log("Current " + trainingSkill.getName() + " level: " + currentLevel);
        }
        
        // Task status
        TaskManager.CombatTask currentTask = taskManager.getCurrentTask();
        if (currentTask != null) {
            Logger.log("Current task: " + currentTask.getDescription());
            Logger.log("Task progress: " + String.format("%.1f%%", currentTask.getProgressPercentage()));
        }
        
        // Combat statistics
        Logger.log("Combat stats: " + combatEngine.getStatisticsSummary());
        
        // Anti-ban status
        Logger.log("Anti-ban: Fatigue " + antiBanManager.getFatigueLevel() + "%, Actions " + 
                  antiBanManager.getAntiBanActionCount());
        
        Logger.log("===================");
    }
    
    /**
     * Calculates dynamic sleep time based on current state
     * 
     * @return sleep time in milliseconds
     */
    private int getDynamicSleepTime() {
        Player localPlayer = Players.getLocal();
        
        // Faster loops during combat
        if (localPlayer != null && localPlayer.isInCombat()) {
            return 600; // 0.6 seconds during combat
        }
        
        // Slower loops when idle
        if (combatEngine.getCurrentTarget() == null) {
            return 1500; // 1.5 seconds when no target
        }
        
        // Normal loop speed
        return 1000; // 1 second default
    }

    @Override
    public void onExit() {
        Logger.log("=== AI Combat Script Stopping ===");
        
        try {
            // Stop all managers
            if (taskManager != null) {
                taskManager.stop();
            }
            
            // Log final statistics
            logFinalStatistics();
            
            Logger.log("AI Combat Script stopped successfully!");
            
        } catch (Exception e) {
            Logger.error("Error during script shutdown: " + e.getMessage());
        }
    }
    
    /**
     * Logs final script statistics
     */
    private void logFinalStatistics() {
        long totalRuntime = System.currentTimeMillis() - scriptStartTime;
        
        Logger.log("=== Final Statistics ===");
        Logger.log("Total runtime: " + (totalRuntime / 60000) + " minutes");
        Logger.log("Total loops: " + loopCount);
        
        if (taskManager != null) {
            Logger.log("Completed tasks: " + taskManager.getCompletedTaskCount());
        }
        
        if (combatEngine != null) {
            Logger.log("Final combat stats: " + combatEngine.getStatisticsSummary());
        }
        
        if (antiBanManager != null) {
            Logger.log("Anti-ban summary: " + antiBanManager.getStatusSummary());
        }
        
        Logger.log("========================");
    }
    
    // Public methods for external configuration (GUI integration)
    
    /**
     * Sets the target NPC name
     * 
     * @param npcName name of NPC to target
     */
    public void setTargetNpc(String npcName) {
        if (npcName != null && !npcName.trim().isEmpty()) {
            this.targetNpcName = npcName.trim();
            
            if (targetSelector != null) {
                targetSelector.clearTargetNames();
                targetSelector.addTargetName(npcName);
            }
            
            Logger.log("Target NPC set to: " + npcName);
        }
    }
    
    /**
     * Sets the target NPC ID
     * 
     * @param npcId ID of NPC to target
     */
    public void setTargetNpcId(int npcId) {
        if (targetSelector != null) {
            targetSelector.clearTargetIds();
            targetSelector.addTargetId(npcId);
        }
        
        Logger.log("Target NPC ID set to: " + npcId);
    }
    
    /**
     * Sets the training skill and target level
     * 
     * @param skill skill to train
     * @param level target level
     */
    public void setTrainingGoal(Skill skill, int level) {
        if (skill != null && level > 0) {
            this.trainingSkill = skill;
            this.targetLevel = level;
            
            Logger.log("Training goal set: " + skill.getName() + " to level " + level);
        }
    }
    
    /**
     * Sets the combat style
     * 
     * @param style combat style name
     */
    public void setCombatStyle(String style) {
        if (style != null && !style.trim().isEmpty()) {
            this.combatStyle = style.trim().toLowerCase();
            
            Logger.log("Combat style set to: " + style);
        }
    }
    
    /**
     * Adds a new training task
     * 
     * @param description task description
     * @param npcName target NPC name
     * @param skill skill to train
     * @param level target level
     * @param style combat style
     */
    public void addTrainingTask(String description, String npcName, Skill skill, int level, String style) {
        if (taskManager != null) {
            taskManager.addSimpleTask(description, npcName, skill, level, style);
            Logger.log("Added training task: " + description);
        }
    }
    
    // Getter methods for status monitoring
    
    public boolean isInitialized() {
        return isInitialized;
    }
    
    public TaskManager getTaskManager() {
        return taskManager;
    }
    
    public CombatEngine getCombatEngine() {
        return combatEngine;
    }
    
    public AntiBanManager getAntiBanManager() {
        return antiBanManager;
    }
}
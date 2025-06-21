package scripts;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.skills.Skill;
import org.dreambot.api.methods.skills.Skills;
import org.dreambot.api.script.AbstractScript;
import org.dreambot.api.script.ScriptManifest;
import org.dreambot.api.script.Category;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;
import org.dreambot.api.wrappers.interactive.Player;
import org.dreambot.api.methods.interactive.Players;

import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javax.swing.Timer;
import java.awt.Window;
import java.util.Map;
import java.util.HashMap;

// Import Phase 1 components
import core.CombatEngine;
import combat.TargetSelector;
import combat.CombatStyleManager;
import tasks.TaskManager;
import antiban.AntiBanManager;

// Import Phase 2 components
import combat.EquipmentManager;
import combat.WeaponManager;
import economy.BankManager;
import gui.CombatGUI;

/**
 * AI Combat Script for Old School RuneScape
 * Advanced combat automation with anti-detection and GUI
 * 
 * @author TraeAI
 * @version 2.0
 */
@ScriptManifest(
    author = "TraeAI",
    description = "Advanced AI Combat Script with GUI and Banking",
    category = Category.COMBAT,
    version = 1.2,
    name = "AI Combat OSRS v2"
)
public class AICombatScript extends AbstractScript {
    
    // Core Components
    private CombatEngine combatEngine;
    private TaskManager taskManager;
    private AntiBanManager antiBanManager;
    private CombatStyleManager combatStyleManager;
    private TargetSelector targetSelector;
    
    // Phase 2 Components
    private EquipmentManager equipmentManager;
    private WeaponManager weaponManager;
    private BankManager bankManager;
    private CombatGUI gui;
    
    // Script State
    private boolean isRunning = false;
    private boolean isPaused = false;
    private long startTime;
    private Map<String, Object> configuration = new HashMap<>();
    
    @Override
    public void onStart() {
        Logger.log("[AICombatScript] Starting AI Combat Script v2.0");
        
        try {
            // Initialize GUI first
            SwingUtilities.invokeLater(() -> {
                try {
                    gui = new CombatGUI(this); // Pass script reference to GUI
                    gui.logMessage("AI Combat Script v2.0 initialized");
                    Logger.log("[AICombatScript] GUI window created and displayed with script reference");
                    
                } catch (Exception e) {
                    Logger.error("[AICombatScript] Failed to create GUI: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
            // Wait for GUI to be ready
            Sleep.sleep(1000);
            
            // Initialize core components
            initializeComponents();
            
            // Set script state - start paused, waiting for GUI to start it
            isRunning = true;
            isPaused = true; // Start paused until GUI starts it
            startTime = System.currentTimeMillis();
            
            Logger.log("[AICombatScript] Script started successfully with GUI");
            
        } catch (Exception e) {
            Logger.error("[AICombatScript] Failed to start script: " + e.getMessage());
            stop();
        }
    }
    
    /**
     * Initialize all script components
     */
    private void initializeComponents() {
        Logger.log("[AICombatScript] Initializing components...");
        
        // Initialize Phase 1 managers
        combatStyleManager = new CombatStyleManager();
        targetSelector = new TargetSelector();
        antiBanManager = new AntiBanManager();
        taskManager = new TaskManager();
        
        // Initialize Phase 2 managers
        equipmentManager = new EquipmentManager();
        weaponManager = new WeaponManager();
        bankManager = new BankManager();
        
        // Initialize combat engine
        combatEngine = new CombatEngine();
        
        Logger.log("[AICombatScript] All components initialized successfully");
        logToGUI("All script components initialized");
    }
    
    /**
     * Helper method to log messages to GUI
     */
    private void logToGUI(String message) {
        if (gui != null) {
            SwingUtilities.invokeLater(() -> gui.logMessage(message));
        }
    }
    
    /**
     * Update configuration from GUI
     */
    private void updateConfigurationFromGUI() {
        if (gui != null) {
            configuration = gui.getConfiguration();
            
            // Apply configuration to managers
            if (configuration.containsKey("targetNpc")) {
                targetSelector.addTargetName((String) configuration.get("targetNpc"));
            }
            
            if (configuration.containsKey("combatStyle")) {
                combatStyleManager.setCurrentStyle((String) configuration.get("combatStyle"));
            }
            
            if (configuration.containsKey("bankingEnabled")) {
                bankManager.setEnabled((Boolean) configuration.get("bankingEnabled"));
            }
        }
    }

    @Override
    public int onLoop() {
        if (!isRunning || isPaused) {
            return 1000;
        }
        
        try {
            // Update configuration from GUI
            updateConfigurationFromGUI();
            
            // Check if banking is needed
            if (bankManager.needsToBank()) {
                logToGUI("Banking required - heading to bank");
                if (bankManager.handleBanking()) {
                    logToGUI("Banking completed successfully");
                } else {
                    logToGUI("Banking failed - retrying");
                    return 2000;
                }
            }
            
            // Select a target before engaging
            org.dreambot.api.wrappers.interactive.NPC target = targetSelector.selectNextTarget();
            if (target == null) {
                logToGUI("No valid targets found, searching...");
                return 1000; // Wait before trying again
            }

            // Check equipment and weapon switching
            if (equipmentManager.needsEquipmentChange(target)) { // Pass target to check
                logToGUI("Equipment change required for " + target.getName());
                if (!equipmentManager.switchToOptimalEquipment(target)) {
                    logToGUI("Failed to switch to optimal equipment.");
                    return 1500; // Wait before retrying
                }
            }
            
            // Execute combat
            int combatSleep = combatEngine.executeCombat();
            if (combatSleep > 0) {
                return combatSleep;
            }
            
            // Anti-ban activities
            antiBanManager.performRandomAction();
            
            // Update GUI statistics
            updateGUIStatistics();
            
            return antiBanManager.getRandomSleep(600, 1200);
            
        } catch (Exception e) {
            Logger.error("[AICombatScript] Error in main loop: " + e.getMessage());
            logToGUI("Error: " + e.getMessage());
            return 2000;
        }
    }
    
    /**
     * Update GUI statistics display
     */
    private void updateGUIStatistics() {
        if (gui != null) {
            SwingUtilities.invokeLater(() -> {
                // Update runtime
                long runtime = System.currentTimeMillis() - startTime;
                gui.updateRuntime(runtime);
                
                // Update combat statistics
                if (combatEngine != null) {
                    gui.updateCombatStats(combatEngine.getTotalKills(), 0); // TODO: Add death tracking
                }
                
                // Update banking statistics
                if (bankManager != null) {
                    gui.updateBankingStats(0, bankManager.getAverageBankingTime()); // TODO: Add banking count tracking
                }
                
                // Update equipment status
                if (equipmentManager != null) {
                    gui.updateEquipmentStatus("Current Equipment"); // TODO: Add getCurrentEquipmentSet() method to EquipmentManager
                }
            });
        }
    }
    
    /**
     * Pause the script
     */
    public void pauseScript() {
        isPaused = true;
        logToGUI("Script paused");
        Logger.log("[AICombatScript] Script paused by user");
    }
    
    /**
     * Resume the script
     */
    public void resumeScript() {
        isPaused = false;
        logToGUI("Script resumed");
        Logger.log("[AICombatScript] Script resumed by user");
    }
    
    /**
     * Stop the script
     */
    public void stopScript() {
        isRunning = false;
        logToGUI("Script stopping...");
        Logger.log("[AICombatScript] Script stopped by user");
        stop();
    }

    @Override
    public void onExit() {
        Logger.log("[AICombatScript] AI Combat Script v2.0 stopping...");
        
        try {
            // Set script state
            isRunning = false;
            
            // Close GUI
            if (gui != null) {
                SwingUtilities.invokeLater(() -> {
                    gui.logMessage("Script stopped - GUI closing");
                    gui.dispose();
                });
            }
            
            // Log final statistics
            logFinalStatistics();
            
            Logger.log("[AICombatScript] Script stopped successfully!");
            
        } catch (Exception e) {
            Logger.error("[AICombatScript] Error during shutdown: " + e.getMessage());
        }
    }
    
    /**
     * Log final script statistics
     */
    private void logFinalStatistics() {
        long totalRuntime = System.currentTimeMillis() - startTime;
        
        Logger.log("[AICombatScript] === Final Statistics ===");
        Logger.log("[AICombatScript] Total runtime: " + (totalRuntime / 60000) + " minutes");
        
        if (combatEngine != null) {
            Logger.log("[AICombatScript] Combat stats: " + combatEngine.getTotalKills() + " kills");
        }
        
        if (bankManager != null) {
            Logger.log("[AICombatScript] Banking stats: " + bankManager.getStatistics());
        }
        
        if (equipmentManager != null) {
            Logger.log("[AICombatScript] Equipment statistics: " + equipmentManager.getStatistics());
        }
        
        Logger.log("[AICombatScript] ========================");
    }
    
    // Getter methods for GUI integration
    
    /**
     * Get current script status
     */
    public boolean isRunning() {
        return isRunning;
    }
    
    /**
     * Get current pause status
     */
    public boolean isPaused() {
        return isPaused;
    }
    
    /**
     * Get script runtime
     */
    public long getRuntime() {
        return System.currentTimeMillis() - startTime;
    }
    
    /**
     * Get combat engine reference
     */
    public CombatEngine getCombatEngine() {
        return combatEngine;
    }
    
    /**
     * Get bank manager reference
     */
    public BankManager getBankManager() {
        return bankManager;
    }
    
    /**
     * Get equipment manager reference
     */
    public EquipmentManager getEquipmentManager() {
        return equipmentManager;
    }
    
    /**
     * Get weapon manager reference
     */
    public WeaponManager getWeaponManager() {
        return weaponManager;
    }
    
    /**
     * Get task manager reference
     */
    public TaskManager getTaskManager() {
        return taskManager;
    }
    
    /**
     * Get anti-ban manager reference
     */
    public AntiBanManager getAntiBanManager() {
        return antiBanManager;
    }
}
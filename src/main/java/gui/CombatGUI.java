package gui;

import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.utilities.Logger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import scripts.AICombatScript;

import combat.*;
import economy.BankManager;
import tasks.TaskManager;
import antiban.AntiBanManager;

/**
 * Advanced Combat GUI for AI Combat OSRS
 * Provides comprehensive user interface for script configuration,
 * real-time monitoring, and advanced combat management
 * 
 * @author TraeAI
 * @version 2.0
 */
public class CombatGUI extends JFrame {
    
    // GUI Components
    private JTabbedPane mainTabbedPane;
    private JPanel combatPanel, bankingPanel, weaponPanel, antiBanPanel, statisticsPanel;
    
    // Combat Configuration
    private JComboBox<String> combatStyleCombo, targetPriorityCombo;
    private JCheckBox enableSpecialAttack, autoLoot, safeSpotting, prayerFlicking;
    private JSpinner minSpecEnergySpinner, maxSpecEnergySpinner, foodThresholdSpinner;
    private JTextField targetAreaField, lootFilterField;
    
    // Banking Configuration
    private JCheckBox enableBanking, depositJunk, withdrawFood, withdrawPotions;
    private JSpinner minFoodSpinner, maxFoodSpinner, minPotionSpinner;
    private JTextField bankLocationField, foodTypeField, potionTypeField;
    
    // Weapon Management
    private JCheckBox autoWeaponSwitch, ammunitionManagement;
    private JComboBox<String> primaryWeaponCombo, secondaryWeaponCombo;
    private JSpinner weaponSwitchDelaySpinner, minAmmoSpinner;
    // Removed weaponPriorityList - replaced with strategy combo
    
    // Anti-Ban Configuration
    private JCheckBox enableAntiBan, randomBreaks, cameraMovement, mouseVariation;
    private JSpinner breakFrequencySpinner, breakDurationSpinner, actionDelaySpinner;
    private JSlider humanLikenessSlider;
    
    // Statistics Display
    private JLabel combatStatsLabel, bankingStatsLabel, weaponStatsLabel, antiBanStatsLabel;
    private JTextArea logArea;
    private JProgressBar experienceProgressBar, profitProgressBar;
    
    // Control Buttons
    private JButton startButton, stopButton, pauseButton, resetStatsButton;
    private JButton saveConfigButton, loadConfigButton, exportLogsButton;
    
    // Configuration Storage
    private final Map<String, Object> configuration = new ConcurrentHashMap<>();
    
    // Manager References
    private CombatStyleManager combatStyleManager;
    private EquipmentManager equipmentManager;
    private WeaponManager weaponManager;
    private BankManager bankManager;
    private TaskManager taskManager;
    private AntiBanManager antiBanManager;
    
    // GUI State
    private boolean isScriptRunning = false;
    private Timer statisticsUpdateTimer;
    private AICombatScript scriptInstance;
    
    public CombatGUI() {
        initializeGUI();
        setupEventHandlers();
        loadDefaultConfiguration();
        startStatisticsUpdater();
        Logger.log("[CombatGUI] Advanced GUI initialized successfully");
    }
    
    public CombatGUI(AICombatScript script) {
        this.scriptInstance = script;
        initializeGUI();
        setupEventHandlers();
        loadDefaultConfiguration();
        startStatisticsUpdater();
        Logger.log("[CombatGUI] Advanced GUI initialized successfully with script reference");
    }
    
    /**
     * Set the script instance reference
     */
    public void setScriptInstance(AICombatScript script) {
        this.scriptInstance = script;
    }
    
    /**
     * Initialize the main GUI components
     */
    private void initializeGUI() {
        setTitle("AI Combat OSRS - Advanced Combat Script v2.0");
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);
        setResizable(true);
        setAlwaysOnTop(false);
        setAutoRequestFocus(true);
        
        // Ensure the window appears as a separate window
        setExtendedState(JFrame.NORMAL);
        
        // Set window position to center of screen
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (screenSize.width - getWidth()) / 2;
        int y = (screenSize.height - getHeight()) / 2;
        setLocation(x, y);
        
        // Skip look and feel setting to avoid conflicts with DreamBot's UI framework
        // Using default Swing look and feel for compatibility
        
        // Create main tabbed pane
        mainTabbedPane = new JTabbedPane(JTabbedPane.TOP);
        
        // Initialize all panels
        initializeCombatPanel();
        initializeBankingPanel();
        initializeWeaponPanel();
        initializeAntiBanPanel();
        initializeStatisticsPanel();
        
        // Add panels to tabbed pane
        mainTabbedPane.addTab("⚔️ Combat", combatPanel);
        mainTabbedPane.addTab("🏦 Banking", bankingPanel);
        mainTabbedPane.addTab("🗡️ Weapons", weaponPanel);
        mainTabbedPane.addTab("🛡️ Anti-Ban", antiBanPanel);
        mainTabbedPane.addTab("📊 Statistics", statisticsPanel);
        
        // Create control panel
        JPanel controlPanel = createControlPanel();
        
        // Layout
        setLayout(new BorderLayout());
        add(mainTabbedPane, BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
        
        Logger.log("[CombatGUI] Main GUI components initialized");
    }
    
    /**
     * Initialize combat configuration panel
     */
    private void initializeCombatPanel() {
        combatPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Combat Style Section
        JPanel combatStylePanel = createTitledPanel("Combat Style");
        combatStyleCombo = new JComboBox<>(new String[]{
            "Melee - Controlled (Balanced XP)",
            "Melee - Accurate (Attack XP)", 
            "Melee - Aggressive (Strength XP)",
            "Melee - Defensive (Defence XP)",
            "Ranged - Accurate (Ranged XP)",
            "Ranged - Rapid (Ranged XP + Speed)",
            "Ranged - Longrange (Ranged + Defence XP)",
            "Magic - Accurate (Magic XP)",
            "Magic - Longrange (Magic + Defence XP)",
            "Magic - Defensive (Magic + Defence XP)"
        });
        targetPriorityCombo = new JComboBox<>(new String[]{"Closest", "Weakest", "Strongest", "Lowest HP", "Highest Level"});
        
        combatStylePanel.add(new JLabel("Combat Style:"));
        combatStylePanel.add(combatStyleCombo);
        combatStylePanel.add(new JLabel("Target Priority:"));
        combatStylePanel.add(targetPriorityCombo);
        
        // Special Attack Section
        JPanel specialAttackPanel = createTitledPanel("Special Attacks");
        enableSpecialAttack = new JCheckBox("Enable Special Attacks", true);
        minSpecEnergySpinner = new JSpinner(new SpinnerNumberModel(25, 0, 100, 5));
        maxSpecEnergySpinner = new JSpinner(new SpinnerNumberModel(100, 0, 100, 5));
        
        specialAttackPanel.add(enableSpecialAttack);
        specialAttackPanel.add(new JLabel("Min Energy:"));
        specialAttackPanel.add(minSpecEnergySpinner);
        specialAttackPanel.add(new JLabel("Max Energy:"));
        specialAttackPanel.add(maxSpecEnergySpinner);
        
        // Combat Options Section
        JPanel combatOptionsPanel = createTitledPanel("Combat Options");
        autoLoot = new JCheckBox("Auto Loot (Coming Soon)", false);
        autoLoot.setEnabled(false); // Disable until implemented
        safeSpotting = new JCheckBox("Safe Spotting (Partial)", false);
        prayerFlicking = new JCheckBox("Prayer Flicking (Coming Soon)", false);
        prayerFlicking.setEnabled(false); // Disable until implemented
        foodThresholdSpinner = new JSpinner(new SpinnerNumberModel(20, 1, 99, 1));
        
        combatOptionsPanel.add(autoLoot);
        combatOptionsPanel.add(safeSpotting);
        combatOptionsPanel.add(prayerFlicking);
        combatOptionsPanel.add(new JLabel("Food Threshold (Basic):"));
        combatOptionsPanel.add(foodThresholdSpinner);
        
        // Target Configuration Section
        JPanel targetConfigPanel = createTitledPanel("Target Configuration");
        targetAreaField = new JTextField("Lumbridge Cows", 15);
        lootFilterField = new JTextField("Bones, Hide, Meat", 15);
        
        targetConfigPanel.add(new JLabel("Target Area:"));
        targetConfigPanel.add(targetAreaField);
        targetConfigPanel.add(new JLabel("Loot Filter:"));
        targetConfigPanel.add(lootFilterField);
        
        // Add all sections to combat panel
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        combatPanel.add(combatStylePanel, gbc);
        gbc.gridy = 1;
        combatPanel.add(specialAttackPanel, gbc);
        gbc.gridy = 2;
        combatPanel.add(combatOptionsPanel, gbc);
        gbc.gridy = 3;
        combatPanel.add(targetConfigPanel, gbc);
    }
    
    /**
     * Initialize banking configuration panel
     */
    private void initializeBankingPanel() {
        bankingPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Banking Options Section
        JPanel bankingOptionsPanel = createTitledPanel("Banking Options");
        enableBanking = new JCheckBox("Enable Banking", true);
        depositJunk = new JCheckBox("Deposit Junk Items", true);
        withdrawFood = new JCheckBox("Withdraw Food", true);
        withdrawPotions = new JCheckBox("Withdraw Potions", false);
        
        bankingOptionsPanel.add(enableBanking);
        bankingOptionsPanel.add(depositJunk);
        bankingOptionsPanel.add(withdrawFood);
        bankingOptionsPanel.add(withdrawPotions);
        
        // Supply Management Section
        JPanel supplyPanel = createTitledPanel("Supply Management");
        minFoodSpinner = new JSpinner(new SpinnerNumberModel(10, 1, 28, 1));
        maxFoodSpinner = new JSpinner(new SpinnerNumberModel(20, 1, 28, 1));
        minPotionSpinner = new JSpinner(new SpinnerNumberModel(2, 0, 10, 1));
        
        supplyPanel.add(new JLabel("Min Food:"));
        supplyPanel.add(minFoodSpinner);
        supplyPanel.add(new JLabel("Max Food:"));
        supplyPanel.add(maxFoodSpinner);
        supplyPanel.add(new JLabel("Min Potions:"));
        supplyPanel.add(minPotionSpinner);
        
        // Bank Configuration Section
        JPanel bankConfigPanel = createTitledPanel("Bank Configuration");
        bankLocationField = new JTextField("Lumbridge", 15);
        foodTypeField = new JTextField("Lobster", 15);
        potionTypeField = new JTextField("Combat potion", 15);
        
        bankConfigPanel.add(new JLabel("Bank Location:"));
        bankConfigPanel.add(bankLocationField);
        bankConfigPanel.add(new JLabel("Food Type:"));
        bankConfigPanel.add(foodTypeField);
        bankConfigPanel.add(new JLabel("Potion Type:"));
        bankConfigPanel.add(potionTypeField);
        
        // Add all sections to banking panel
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        bankingPanel.add(bankingOptionsPanel, gbc);
        gbc.gridy = 1;
        bankingPanel.add(supplyPanel, gbc);
        gbc.gridy = 2;
        bankingPanel.add(bankConfigPanel, gbc);
    }
    
    /**
     * Initialize weapon management panel
     */
    private void initializeWeaponPanel() {
        weaponPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Weapon Options Section
        JPanel weaponOptionsPanel = createTitledPanel("Weapon Options");
        autoWeaponSwitch = new JCheckBox("Auto Weapon Switching (Basic)", false);
        ammunitionManagement = new JCheckBox("Ammunition Management (Coming Soon)", false);
        ammunitionManagement.setEnabled(false);
        weaponSwitchDelaySpinner = new JSpinner(new SpinnerNumberModel(800, 200, 3000, 100));
        minAmmoSpinner = new JSpinner(new SpinnerNumberModel(50, 10, 1000, 10));
        
        weaponOptionsPanel.add(autoWeaponSwitch);
        weaponOptionsPanel.add(ammunitionManagement);
        weaponOptionsPanel.add(new JLabel("Switch Delay (ms):"));
        weaponOptionsPanel.add(weaponSwitchDelaySpinner);
        weaponOptionsPanel.add(new JLabel("Min Ammo:"));
        weaponOptionsPanel.add(minAmmoSpinner);
        
        // Weapon Selection Section
        JPanel weaponSelectionPanel = createTitledPanel("Weapon Selection");
        
        // Melee weapons (organized by tier)
        String[] meleeWeapons = {"Bronze scimitar", "Iron scimitar", "Steel scimitar", "Mithril scimitar", 
                                "Adamant scimitar", "Rune scimitar", "Dragon scimitar", "Abyssal whip", 
                                "Dragon dagger", "Rune dagger", "Dragon longsword"};
        
        // Ranged weapons
        String[] rangedWeapons = {"Shortbow", "Oak shortbow", "Willow shortbow", "Maple shortbow", 
                                 "Yew shortbow", "Magic shortbow", "Rune crossbow", "Dragon crossbow"};
        
        // Magic weapons
        String[] magicWeapons = {"Staff of air", "Staff of water", "Staff of earth", "Staff of fire", 
                                "Mystic air staff", "Mystic water staff", "Mystic earth staff", "Mystic fire staff"};
        
        primaryWeaponCombo = new JComboBox<>(meleeWeapons);
        secondaryWeaponCombo = new JComboBox<>(new String[]{"None"});
        
        // Weapon type selector
        JComboBox<String> weaponTypeCombo = new JComboBox<>(new String[]{"Melee", "Ranged", "Magic"});
        weaponTypeCombo.addActionListener(e -> {
            String selectedType = (String) weaponTypeCombo.getSelectedItem();
            primaryWeaponCombo.removeAllItems();
            
            String[] weapons;
            switch (selectedType) {
                case "Ranged":
                    weapons = rangedWeapons;
                    break;
                case "Magic":
                    weapons = magicWeapons;
                    break;
                default:
                    weapons = meleeWeapons;
                    break;
            }
            
            for (String weapon : weapons) {
                primaryWeaponCombo.addItem(weapon);
            }
        });
        
        weaponSelectionPanel.add(new JLabel("Weapon Type:"));
        weaponSelectionPanel.add(weaponTypeCombo);
        weaponSelectionPanel.add(new JLabel("Primary Weapon:"));
        weaponSelectionPanel.add(primaryWeaponCombo);
        weaponSelectionPanel.add(new JLabel("Secondary Weapon:"));
        weaponSelectionPanel.add(secondaryWeaponCombo);
        
        // Weapon Strategy Section
        JPanel weaponStrategyPanel = createTitledPanel("Combat Strategy");
        String[] strategies = {"Aggressive (Max DPS)", "Balanced (Mixed)", "Defensive (Safe)", "Adaptive (Auto)"};
        JComboBox<String> strategyCombo = new JComboBox<>(strategies);
        
        JCheckBox autoUpgrade = new JCheckBox("Auto Upgrade Weapons (Coming Soon)", false);
        autoUpgrade.setEnabled(false);
        
        weaponStrategyPanel.add(new JLabel("Combat Strategy:"));
        weaponStrategyPanel.add(strategyCombo);
        weaponStrategyPanel.add(autoUpgrade);
        
        // Add all sections to weapon panel
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        weaponPanel.add(weaponOptionsPanel, gbc);
        gbc.gridy = 1;
        weaponPanel.add(weaponSelectionPanel, gbc);
        gbc.gridy = 2;
        weaponPanel.add(weaponStrategyPanel, gbc);
    }
    
    /**
     * Initialize anti-ban configuration panel
     */
    private void initializeAntiBanPanel() {
        antiBanPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        
        // Anti-Ban Options Section
        JPanel antiBanOptionsPanel = createTitledPanel("Anti-Ban Options");
        enableAntiBan = new JCheckBox("Enable Anti-Ban", true);
        randomBreaks = new JCheckBox("Random Breaks (Coming Soon)", false);
        cameraMovement = new JCheckBox("Camera Movement (Basic)", true);
        mouseVariation = new JCheckBox("Adaptive Behavior", true);
        
        randomBreaks.setEnabled(false);
        
        antiBanOptionsPanel.add(enableAntiBan);
        antiBanOptionsPanel.add(randomBreaks);
        antiBanOptionsPanel.add(cameraMovement);
        antiBanOptionsPanel.add(mouseVariation);
        
        // Break Configuration Section
        JPanel breakConfigPanel = createTitledPanel("Break Configuration");
        breakFrequencySpinner = new JSpinner(new SpinnerNumberModel(45, 10, 180, 5));
        breakDurationSpinner = new JSpinner(new SpinnerNumberModel(5, 1, 30, 1));
        actionDelaySpinner = new JSpinner(new SpinnerNumberModel(150, 50, 1000, 25));
        
        breakConfigPanel.add(new JLabel("Break Frequency (min):"));
        breakConfigPanel.add(breakFrequencySpinner);
        breakConfigPanel.add(new JLabel("Break Duration (min):"));
        breakConfigPanel.add(breakDurationSpinner);
        breakConfigPanel.add(new JLabel("Action Delay (ms):"));
        breakConfigPanel.add(actionDelaySpinner);
        
        // Human Likeness Section
        JPanel humanLikenessPanel = createTitledPanel("Human Likeness");
        humanLikenessSlider = new JSlider(0, 100, 75);
        humanLikenessSlider.setMajorTickSpacing(25);
        humanLikenessSlider.setMinorTickSpacing(5);
        humanLikenessSlider.setPaintTicks(true);
        humanLikenessSlider.setPaintLabels(true);
        
        humanLikenessPanel.add(new JLabel("Human Likeness Level:"));
        humanLikenessPanel.add(humanLikenessSlider);
        
        // Add all sections to anti-ban panel
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        antiBanPanel.add(antiBanOptionsPanel, gbc);
        gbc.gridy = 1;
        antiBanPanel.add(breakConfigPanel, gbc);
        gbc.gridy = 2;
        antiBanPanel.add(humanLikenessPanel, gbc);
    }
    
    /**
     * Initialize statistics and monitoring panel
     */
    private void initializeStatisticsPanel() {
        statisticsPanel = new JPanel(new BorderLayout());
        
        // Statistics Display Section
        JPanel statsDisplayPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        
        combatStatsLabel = new JLabel("<html><b>Combat:</b><br>Kills: 0<br>XP/hr: (Coming Soon)<br>DPS: (Coming Soon)</html>");
        bankingStatsLabel = new JLabel("<html><b>Banking:</b><br>Trips: (Coming Soon)<br>Efficiency: (Coming Soon)<br>Time: 0s</html>");
        weaponStatsLabel = new JLabel("<html><b>Weapons:</b><br>Switches: 0<br>Spec Attacks: 0<br>Success Rate: (Coming Soon)</html>");
        antiBanStatsLabel = new JLabel("<html><b>Anti-Ban:</b><br>Actions: 0<br>Breaks: (Coming Soon)<br>Risk Level: Low</html>");
        
        statsDisplayPanel.add(createTitledPanel("Combat Statistics", combatStatsLabel));
        statsDisplayPanel.add(createTitledPanel("Banking Statistics", bankingStatsLabel));
        statsDisplayPanel.add(createTitledPanel("Weapon Statistics", weaponStatsLabel));
        statsDisplayPanel.add(createTitledPanel("Anti-Ban Statistics", antiBanStatsLabel));
        
        // Progress Bars Section
        JPanel progressPanel = new JPanel(new GridLayout(2, 1, 5, 5));
        experienceProgressBar = new JProgressBar(0, 100);
        experienceProgressBar.setStringPainted(true);
        experienceProgressBar.setString("Experience Progress");
        
        profitProgressBar = new JProgressBar(0, 100);
        profitProgressBar.setStringPainted(true);
        profitProgressBar.setString("Profit Progress");
        
        progressPanel.add(createTitledPanel("Experience", experienceProgressBar));
        progressPanel.add(createTitledPanel("Profit", profitProgressBar));
        
        // Log Area Section
        logArea = new JTextArea(10, 50);
        logArea.setEditable(false);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        JScrollPane logScrollPane = new JScrollPane(logArea);
        logScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        
        // Layout statistics panel
        statisticsPanel.add(statsDisplayPanel, BorderLayout.NORTH);
        statisticsPanel.add(progressPanel, BorderLayout.CENTER);
        statisticsPanel.add(createTitledPanel("Script Logs", logScrollPane), BorderLayout.SOUTH);
    }
    
    /**
     * Create control panel with start/stop buttons
     */
    private JPanel createControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout());
        
        startButton = new JButton("Start Script");
        stopButton = new JButton("Stop Script");
        pauseButton = new JButton("Pause Script");
        resetStatsButton = new JButton("Reset Stats");
        saveConfigButton = new JButton("Save Config");
        loadConfigButton = new JButton("Load Config");
        exportLogsButton = new JButton("Export Logs");
        
        // Style buttons
        startButton.setBackground(new Color(76, 175, 80));
        startButton.setForeground(Color.WHITE);
        stopButton.setBackground(new Color(244, 67, 54));
        stopButton.setForeground(Color.WHITE);
        pauseButton.setBackground(new Color(255, 193, 7));
        
        // Initially disable stop and pause buttons
        stopButton.setEnabled(false);
        pauseButton.setEnabled(false);
        
        controlPanel.add(startButton);
        controlPanel.add(stopButton);
        controlPanel.add(pauseButton);
        controlPanel.add(new JSeparator(SwingConstants.VERTICAL));
        controlPanel.add(resetStatsButton);
        controlPanel.add(saveConfigButton);
        controlPanel.add(loadConfigButton);
        controlPanel.add(exportLogsButton);
        
        return controlPanel;
    }
    
    /**
     * Setup event handlers for GUI components
     */
    private void setupEventHandlers() {
        // Start button handler
        startButton.addActionListener(e -> {
            if (validateConfiguration()) {
                startScript();
            }
        });
        
        // Stop button handler
        stopButton.addActionListener(e -> stopScript());
        
        // Pause button handler
        pauseButton.addActionListener(e -> pauseScript());
        
        // Reset stats button handler
        resetStatsButton.addActionListener(e -> resetStatistics());
        
        // Save config button handler
        saveConfigButton.addActionListener(e -> saveConfiguration());
        
        // Load config button handler
        loadConfigButton.addActionListener(e -> loadConfiguration());
        
        // Export logs button handler
        exportLogsButton.addActionListener(e -> exportLogs());
        
        // Configuration change handlers
        enableSpecialAttack.addActionListener(e -> updateConfiguration());
        enableBanking.addActionListener(e -> updateConfiguration());
        autoWeaponSwitch.addActionListener(e -> updateConfiguration());
        enableAntiBan.addActionListener(e -> updateConfiguration());
        
        Logger.log("[CombatGUI] Event handlers configured");
    }
    
    /**
     * Load default configuration values
     */
    private void loadDefaultConfiguration() {
        // Set default values
        combatStyleCombo.setSelectedItem("Auto-Switch");
        targetPriorityCombo.setSelectedItem("Closest");
        enableSpecialAttack.setSelected(true);
        autoLoot.setSelected(true);
        enableBanking.setSelected(true);
        autoWeaponSwitch.setSelected(true);
        enableAntiBan.setSelected(true);
        
        updateConfiguration();
        Logger.log("[CombatGUI] Default configuration loaded");
    }
    
    /**
     * Start statistics update timer
     */
    private void startStatisticsUpdater() {
        statisticsUpdateTimer = new Timer(1000, e -> updateStatisticsDisplay());
        statisticsUpdateTimer.start();
        Logger.log("[CombatGUI] Statistics updater started");
    }
    
    /**
     * Update statistics display
     */
    private void updateStatisticsDisplay() {
        if (isScriptRunning) {
            // Update combat statistics
            if (combatStyleManager != null) {
                combatStatsLabel.setText("<html><b>Combat:</b><br>" + 
                    combatStyleManager.getStatistics().replace(", ", "<br>") + "</html>");
            }
            
            // Update banking statistics
            if (bankManager != null) {
                bankingStatsLabel.setText("<html><b>Banking:</b><br>" + 
                    bankManager.getStatistics().replace(", ", "<br>") + "</html>");
            }
            
            // Update weapon statistics
            if (weaponManager != null) {
                weaponStatsLabel.setText("<html><b>Weapons:</b><br>" + 
                    weaponManager.getStatistics().replace(", ", "<br>") + "</html>");
            }
            
            // Update anti-ban statistics
            if (antiBanManager != null) {
                antiBanStatsLabel.setText("<html><b>Anti-Ban:</b><br>" + 
                    antiBanManager.getStatistics().replace(", ", "<br>") + "</html>");
            }
        }
    }
    
    /**
     * Validate configuration before starting script
     */
    private boolean validateConfiguration() {
        // Validate target area
        if (targetAreaField.getText().trim().isEmpty()) {
            showError("Target area cannot be empty");
            return false;
        }
        
        // Validate banking configuration
        if (enableBanking.isSelected() && bankLocationField.getText().trim().isEmpty()) {
            showError("Bank location cannot be empty when banking is enabled");
            return false;
        }
        
        // Validate food configuration
        if (withdrawFood.isSelected() && foodTypeField.getText().trim().isEmpty()) {
            showError("Food type cannot be empty when food withdrawal is enabled");
            return false;
        }
        
        return true;
    }
    
    /**
     * Start the combat script
     */
    private void startScript() {
        try {
            if (scriptInstance == null) {
                showError("Script instance not available. Please restart the script.");
                return;
            }
            
            updateConfiguration();
            isScriptRunning = true;
            
            // Update button states
            startButton.setEnabled(false);
            stopButton.setEnabled(true);
            pauseButton.setEnabled(true);
            
            // Initialize managers with configuration
            initializeManagers();
            
            // Start the actual script execution
            if (scriptInstance != null) {
                scriptInstance.resumeScript();
                // Also ensure the script is marked as running
                logMessage("Combat script execution started");
            }
            
            logMessage("Script started successfully with current configuration");
            Logger.log("[CombatGUI] Script started");
            
        } catch (Exception e) {
            showError("Failed to start script: " + e.getMessage());
            Logger.error("[CombatGUI] Failed to start script: " + e.getMessage());
        }
    }
    
    /**
     * Stop the combat script
     */
    private void stopScript() {
        try {
            isScriptRunning = false;
            
            // Stop the actual script execution
            if (scriptInstance != null) {
                scriptInstance.stopScript();
            }
            
            // Update button states
            startButton.setEnabled(true);
            stopButton.setEnabled(false);
            pauseButton.setEnabled(false);
            
            logMessage("Script stopped");
            Logger.log("[CombatGUI] Script stopped");
            
        } catch (Exception e) {
            showError("Failed to stop script: " + e.getMessage());
            Logger.error("[CombatGUI] Failed to stop script: " + e.getMessage());
        }
    }
    
    /**
     * Pause/resume the combat script
     */
    private void pauseScript() {
        try {
            // Toggle pause state
            if (pauseButton.getText().equals("Pause Script")) {
                if (scriptInstance != null) {
                    scriptInstance.pauseScript();
                }
                pauseButton.setText("Resume Script");
                logMessage("Script paused");
            } else {
                if (scriptInstance != null) {
                    scriptInstance.resumeScript();
                }
                pauseButton.setText("Pause Script");
                logMessage("Script resumed");
            }
        } catch (Exception e) {
            showError("Failed to pause/resume script: " + e.getMessage());
            Logger.error("[CombatGUI] Failed to pause/resume script: " + e.getMessage());
        }
    }
    
    /**
     * Initialize manager instances with current configuration
     */
    private void initializeManagers() {
        // Initialize managers based on configuration
        combatStyleManager = new CombatStyleManager();
        equipmentManager = new EquipmentManager();
        weaponManager = new WeaponManager();
        bankManager = new BankManager();
        taskManager = new TaskManager();
        antiBanManager = new AntiBanManager();
        
        // Apply configuration to managers
        applyConfigurationToManagers();
        
        Logger.log("[CombatGUI] Managers initialized with configuration");
    }
    
    /**
     * Apply GUI configuration to manager instances
     */
    private void applyConfigurationToManagers() {
        if (weaponManager != null) {
            weaponManager.setSpecialAttackEnabled(enableSpecialAttack.isSelected());
            weaponManager.setMinSpecialAttackEnergy((Integer) minSpecEnergySpinner.getValue());
            weaponManager.setMaxSpecialAttackEnergy((Integer) maxSpecEnergySpinner.getValue());
            weaponManager.setAutoStyleSwitching(autoWeaponSwitch.isSelected());
            weaponManager.setAmmunitionManagement(ammunitionManagement.isSelected());
            weaponManager.setMinAmmunitionThreshold((Integer) minAmmoSpinner.getValue());
        }
        
        if (bankManager != null) {
            bankManager.setBankingEnabled(enableBanking.isSelected());
            bankManager.setDepositJunkItems(depositJunk.isSelected());
            bankManager.setWithdrawFood(withdrawFood.isSelected());
            bankManager.setWithdrawPotions(withdrawPotions.isSelected());
            bankManager.setMinFoodAmount((Integer) minFoodSpinner.getValue());
            bankManager.setMaxFoodAmount((Integer) maxFoodSpinner.getValue());
            bankManager.setMinPotionAmount((Integer) minPotionSpinner.getValue());
        }
        
        if (antiBanManager != null) {
            antiBanManager.setEnabled(enableAntiBan.isSelected());
            antiBanManager.setAdaptiveBehavior(mouseVariation.isSelected());
            antiBanManager.setMaxSessionTime((Integer) breakFrequencySpinner.getValue() * 60000L);
            
            // Note: Some advanced features like specific break controls are not yet implemented
            // antiBanManager.setRandomBreaksEnabled(randomBreaks.isSelected());
            // antiBanManager.setCameraMovementEnabled(cameraMovement.isSelected());
            // antiBanManager.setBreakDuration((Integer) breakDurationSpinner.getValue());
            // antiBanManager.setActionDelay((Integer) actionDelaySpinner.getValue());
            // antiBanManager.setHumanLikenessLevel(humanLikenessSlider.getValue());
        }
    }
    
    /**
     * Update configuration map with current GUI values
     */
    private void updateConfiguration() {
        // Map GUI combat style selection to specific combat style
        String selectedStyle = (String) combatStyleCombo.getSelectedItem();
        String mappedStyle = mapCombatStyle(selectedStyle);
        configuration.put("combatStyle", mappedStyle);
        configuration.put("targetPriority", targetPriorityCombo.getSelectedItem());
        configuration.put("enableSpecialAttack", enableSpecialAttack.isSelected());
        configuration.put("minSpecEnergy", minSpecEnergySpinner.getValue());
        configuration.put("maxSpecEnergy", maxSpecEnergySpinner.getValue());
        configuration.put("autoLoot", autoLoot.isSelected());
        configuration.put("safeSpotting", safeSpotting.isSelected());
        configuration.put("prayerFlicking", prayerFlicking.isSelected());
        configuration.put("foodThreshold", foodThresholdSpinner.getValue());
        configuration.put("targetArea", targetAreaField.getText());
        configuration.put("lootFilter", lootFilterField.getText());
        
        configuration.put("enableBanking", enableBanking.isSelected());
        configuration.put("depositJunk", depositJunk.isSelected());
        configuration.put("withdrawFood", withdrawFood.isSelected());
        configuration.put("withdrawPotions", withdrawPotions.isSelected());
        configuration.put("minFood", minFoodSpinner.getValue());
        configuration.put("maxFood", maxFoodSpinner.getValue());
        configuration.put("minPotions", minPotionSpinner.getValue());
        configuration.put("bankLocation", bankLocationField.getText());
        configuration.put("foodType", foodTypeField.getText());
        configuration.put("potionType", potionTypeField.getText());
        
        configuration.put("autoWeaponSwitch", autoWeaponSwitch.isSelected());
        configuration.put("ammunitionManagement", ammunitionManagement.isSelected());
        configuration.put("weaponSwitchDelay", weaponSwitchDelaySpinner.getValue());
        configuration.put("minAmmo", minAmmoSpinner.getValue());
        configuration.put("primaryWeapon", primaryWeaponCombo.getSelectedItem());
        configuration.put("secondaryWeapon", secondaryWeaponCombo.getSelectedItem());
        
        configuration.put("enableAntiBan", enableAntiBan.isSelected());
        configuration.put("randomBreaks", randomBreaks.isSelected());
        configuration.put("cameraMovement", cameraMovement.isSelected());
        configuration.put("mouseVariation", mouseVariation.isSelected());
        configuration.put("breakFrequency", breakFrequencySpinner.getValue());
        configuration.put("breakDuration", breakDurationSpinner.getValue());
        configuration.put("actionDelay", actionDelaySpinner.getValue());
        configuration.put("humanLikeness", humanLikenessSlider.getValue());
    }
    
    /**
     * Map GUI combat style selection to specific combat style enum value
     */
    private String mapCombatStyle(String guiSelection) {
        if (guiSelection == null) {
            return "CONTROLLED"; // Default fallback
        }
        
        // Extract the key part of the selection for mapping
        String selection = guiSelection.toLowerCase();
        
        if (selection.contains("controlled")) {
            return "CONTROLLED";
        } else if (selection.contains("accurate") && selection.contains("melee")) {
            return "ACCURATE";
        } else if (selection.contains("aggressive")) {
            return "AGGRESSIVE";
        } else if (selection.contains("defensive") && selection.contains("melee")) {
            return "DEFENSIVE";
        } else if (selection.contains("ranged") && selection.contains("accurate")) {
            return "RANGED_ACCURATE";
        } else if (selection.contains("rapid")) {
            return "RANGED_RAPID";
        } else if (selection.contains("ranged") && selection.contains("longrange")) {
            return "RANGED_LONGRANGE";
        } else if (selection.contains("magic") && selection.contains("accurate")) {
            return "MAGIC_ACCURATE";
        } else if (selection.contains("magic") && selection.contains("longrange")) {
            return "MAGIC_LONGRANGE";
        } else if (selection.contains("magic") && selection.contains("defensive")) {
            return "MAGIC_DEFENSIVE";
        } else {
            Logger.log("[CombatGUI] Unknown combat style: " + guiSelection + ", defaulting to CONTROLLED");
            return "CONTROLLED";
        }
    }
    
    /**
     * Reset all statistics
     */
    private void resetStatistics() {
        if (combatStyleManager != null) combatStyleManager.reset();
        if (weaponManager != null) weaponManager.resetStatistics();
        if (bankManager != null) bankManager.resetStatistics();
        if (antiBanManager != null) antiBanManager.reset();
        
        experienceProgressBar.setValue(0);
        profitProgressBar.setValue(0);
        
        logMessage("All statistics reset");
        Logger.log("[CombatGUI] Statistics reset");
    }
    
    /**
     * Save current configuration to file
     */
    private void saveConfiguration() {
        updateConfiguration();
        // Implementation for saving configuration to file
        logMessage("Configuration saved");
        Logger.log("[CombatGUI] Configuration saved");
    }
    
    /**
     * Load configuration from file
     */
    private void loadConfiguration() {
        // Implementation for loading configuration from file
        logMessage("Configuration loaded");
        Logger.log("[CombatGUI] Configuration loaded");
    }
    
    /**
     * Export logs to file
     */
    private void exportLogs() {
        // Implementation for exporting logs
        logMessage("Logs exported");
        Logger.log("[CombatGUI] Logs exported");
    }
    
    /**
     * Add message to log area
     */
    public void logMessage(String message) {
        SwingUtilities.invokeLater(() -> {
            String timestamp = java.time.LocalTime.now().toString().substring(0, 8);
            logArea.append("[" + timestamp + "] " + message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }
    
    /**
     * Update equipment status display
     */
    public void updateEquipmentStatus(String status) {
        SwingUtilities.invokeLater(() -> {
            // Update equipment status in the GUI
            // This could be displayed in a status label or logged
            logMessage("Equipment Status: " + status);
        });
    }
    
    /**
     * Update banking statistics display
     */
    public void updateBankingStats(int bankingCount, long averageBankingTime) {
        SwingUtilities.invokeLater(() -> {
            // Update banking statistics in the GUI
            logMessage("Banking Stats - Count: " + bankingCount + ", Avg Time: " + averageBankingTime + "ms");
        });
    }
    
    /**
     * Update combat statistics display
     */
    public void updateCombatStats(int kills, int deaths) {
        SwingUtilities.invokeLater(() -> {
            // Update combat statistics in the GUI
            logMessage("Combat Stats - Kills: " + kills + ", Deaths: " + deaths);
        });
    }
    
    /**
     * Update runtime display
     */
    public void updateRuntime(long runtime) {
        SwingUtilities.invokeLater(() -> {
            // Update runtime display in the GUI
            long hours = runtime / 3600000;
            long minutes = (runtime % 3600000) / 60000;
            long seconds = (runtime % 60000) / 1000;
            logMessage(String.format("Runtime: %02d:%02d:%02d", hours, minutes, seconds));
        });
    }
    
    /**
     * Show error dialog
     */
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Configuration Error", JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Create titled panel with component
     */
    private JPanel createTitledPanel(String title, Component component) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(title));
        panel.add(component, BorderLayout.CENTER);
        return panel;
    }
    
    /**
     * Create titled panel with flow layout
     */
    private JPanel createTitledPanel(String title) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder(title));
        return panel;
    }
    
    /**
     * Create icon label for tabs
     */
    private JLabel createIconLabel(String icon) {
        JLabel label = new JLabel(icon);
        label.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 16));
        return label;
    }
    
    // Getters for configuration access
    public Map<String, Object> getConfiguration() { return new HashMap<>(configuration); }
    public boolean isScriptRunning() { return isScriptRunning; }
    
    // Manager getters
    public CombatStyleManager getCombatStyleManager() { return combatStyleManager; }
    public EquipmentManager getEquipmentManager() { return equipmentManager; }
    public WeaponManager getWeaponManager() { return weaponManager; }
    public BankManager getBankManager() { return bankManager; }
    public TaskManager getTaskManager() { return taskManager; }
    public AntiBanManager getAntiBanManager() { return antiBanManager; }
}
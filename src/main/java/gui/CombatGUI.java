package gui;

import org.dreambot.api.script.ScriptManager;
import org.dreambot.api.utilities.Logger;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import javax.swing.BorderFactory;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;

import scripts.AICombatScript;

import combat.*;
import economy.BankManager;
import tasks.TaskManager;
import antiban.AntiBanManager;
import database.repositories.*;
import database.models.*;
import database.models.Equipment.EquipmentSlot;
import database.models.WeaponType;
import database.models.ArmorType;

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
    private JComboBox<String> npcComboBox, locationComboBox, lootComboBox;
    private JCheckBox enableSpecialAttack, autoLoot, safeSpotting, prayerFlicking;
    private JSpinner minSpecEnergySpinner, maxSpecEnergySpinner, foodThresholdSpinner;
    // Removed unused lootFilterField - using lootComboBox instead
    
    // Banking Configuration
    private JCheckBox enableBanking, depositJunk, withdrawFood, withdrawPotions;
    private JSpinner minFoodSpinner, maxFoodSpinner, minPotionSpinner;
    private JComboBox<String> bankLocationCombo, foodComboBox, potionComboBox;
    
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
    private JComboBox<String> profileComboBox;
    private JTextField profileNameField;
    
    // Gear and Inventory Loadout Components (planned for future implementation)
    // private JComboBox<String> gearProfileComboBox;
    // private JComboBox<String> inventoryProfileComboBox;
    
    // Configuration Storage
    private final Map<String, Object> configuration = new ConcurrentHashMap<>();
    
    // Manager References
    private CombatStyleManager combatStyleManager;
    private EquipmentManager equipmentManager;
    private WeaponManager weaponManager;
    private BankManager bankManager;
    private TaskManager taskManager;
    private AntiBanManager antiBanManager;
    
    // Database Repositories
    private NPCRepository npcRepository;
    private EquipmentRepository equipmentRepository;
    private ConsumableRepository consumableRepository;
    private BankLocationRepository locationRepository;
    
    // GUI State
    private boolean isScriptRunning = false;
    private Timer statisticsUpdateTimer;
    private AICombatScript scriptInstance;
    
    public CombatGUI() {
        initializeDatabaseRepositories();
        initializeGUI();
        setupEventHandlers();
        loadDefaultConfiguration();
        startStatisticsUpdater();
        Logger.log("[CombatGUI] Advanced GUI initialized successfully");
    }
    
    public CombatGUI(AICombatScript script) {
        this.scriptInstance = script;
        initializeDatabaseRepositories();
        initializeGUI();
        setupEventHandlers();
        loadDefaultConfiguration();
        startStatisticsUpdater();
        Logger.log("[CombatGUI] Advanced GUI initialized successfully with script reference");
    }
    
    /**
     * Initialize database repositories
     */
    private void initializeDatabaseRepositories() {
        try {
            Logger.log("[CombatGUI] Initializing database repositories...");
            
            this.npcRepository = new NPCRepository();
            this.npcRepository.initialize();
            Logger.log("[CombatGUI] NPC repository initialized with " + npcRepository.getCount() + " NPCs");
            
            this.equipmentRepository = new EquipmentRepository();
            this.equipmentRepository.initialize();
            Logger.log("[CombatGUI] Equipment repository initialized with " + equipmentRepository.getCount() + " items");
            
            this.consumableRepository = new ConsumableRepository();
            this.consumableRepository.initialize();
            Logger.log("[CombatGUI] Consumable repository initialized with " + consumableRepository.getCount() + " items");
            
            this.locationRepository = new BankLocationRepository();
            this.locationRepository.initialize();
            Logger.log("[CombatGUI] Bank location repository initialized with " + locationRepository.getCount() + " locations");
            
            Logger.log("[CombatGUI] All database repositories initialized successfully");
        } catch (Exception e) {
            Logger.error("[CombatGUI] Failed to initialize database repositories: " + e.getMessage());
            e.printStackTrace();
            
            // Show error to user
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, 
                    "Failed to initialize database repositories. Some dropdown lists may be empty.\n" +
                    "Error: " + e.getMessage(), 
                    "Database Initialization Error", 
                    JOptionPane.WARNING_MESSAGE);
            });
        }
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
        try {
            setTitle("AI Combat OSRS - Advanced Combat Script v2.0");
            setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
            setSize(800, 600);
            setResizable(true);
            
            // Force window to be visible and on top initially
            setAlwaysOnTop(true);
            setAutoRequestFocus(true);
            setFocusableWindowState(true);
            
            // Ensure the window appears as a separate window
            setExtendedState(JFrame.NORMAL);
            setType(Window.Type.NORMAL);
            
            // Set window position to center of screen
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int x = (screenSize.width - getWidth()) / 2;
            int y = (screenSize.height - getHeight()) / 2;
            setLocation(x, y);
            
            // Create main tabbed pane
            mainTabbedPane = new JTabbedPane(JTabbedPane.TOP);
            
            // Initialize all panels
            initializeCombatPanel();
            initializeBankingPanel();
            initializeWeaponPanel();
            initializeAntiBanPanel();
            initializeStatisticsPanel();
            
            // Add panels to tabbed pane
            mainTabbedPane.addTab("Combat", combatPanel);
            mainTabbedPane.addTab("Banking", bankingPanel);
            mainTabbedPane.addTab("Weapons", weaponPanel);
            mainTabbedPane.addTab("Anti-Ban", antiBanPanel);
            mainTabbedPane.addTab("Statistics", statisticsPanel);
            
            // Create control panel
            JPanel controlPanel = createControlPanel();
            
            // Layout
            setLayout(new BorderLayout());
            add(mainTabbedPane, BorderLayout.CENTER);
            add(controlPanel, BorderLayout.SOUTH);
            
            // Force visibility
            pack();
            setVisible(true);
            toFront();
            requestFocus();
            
            // Reset always on top after 3 seconds
            Timer resetTimer = new Timer(3000, e -> {
                setAlwaysOnTop(false);
                ((Timer) e.getSource()).stop();
            });
            resetTimer.start();
            
            Logger.log("[CombatGUI] Main GUI components initialized and made visible");
            
        } catch (Exception e) {
            Logger.error("[CombatGUI] Error initializing GUI: " + e.getMessage());
            e.printStackTrace();
        }
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
        
        // NPC Selection with database integration
        JPanel npcSelectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        npcSelectionPanel.add(new JLabel("Target NPC:"));
        
        // Create searchable NPC combo box
        npcComboBox = createSearchableNPCComboBox();
        npcComboBox.addActionListener(e -> {
            String selectedNPC = (String) npcComboBox.getSelectedItem();
            updateNPCLocationComboBox(selectedNPC);
        });
        npcSelectionPanel.add(npcComboBox);
        
        // NPC location selection with database integration
        JPanel locationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        locationPanel.add(new JLabel("Location:"));
        locationComboBox = createSearchableNPCLocationComboBox();
        locationPanel.add(locationComboBox);
        
        // Loot filter with consumable database integration
        JPanel lootPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lootPanel.add(new JLabel("Loot Filter:"));
        
        lootComboBox = createSearchableLootComboBox();
        lootPanel.add(lootComboBox);
        
        targetConfigPanel.setLayout(new BoxLayout(targetConfigPanel, BoxLayout.Y_AXIS));
        targetConfigPanel.add(npcSelectionPanel);
        targetConfigPanel.add(locationPanel);
        targetConfigPanel.add(lootPanel);
        
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
        
        // Bank location selection
        JPanel bankLocationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bankLocationPanel.add(new JLabel("Bank Location:"));
        bankLocationCombo = createSearchableLocationComboBox();
        bankLocationPanel.add(bankLocationCombo);
        
        // Food type selection with database integration
        JPanel foodPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        foodPanel.add(new JLabel("Food Type:"));
        foodComboBox = createSearchableFoodComboBox();
        foodPanel.add(foodComboBox);
        
        // Potion type selection with database integration
        JPanel potionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        potionPanel.add(new JLabel("Potion Type:"));
        potionComboBox = createSearchablePotionComboBox();
        potionPanel.add(potionComboBox);
        
        // Inventory Setup Section
        JPanel inventorySetupPanel = createTitledPanel("Inventory Setup");
        inventorySetupPanel.setLayout(new BorderLayout());
        
        // Left side - fetch button only
        JPanel inventoryControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton fetchInventoryButton = new JButton("Fetch Inventory");
        
        inventoryControlPanel.add(fetchInventoryButton);
        
        // Right side - current inventory list
        JPanel currentInventoryPanel = new JPanel();
        currentInventoryPanel.setLayout(new BoxLayout(currentInventoryPanel, BoxLayout.Y_AXIS));
        currentInventoryPanel.setBorder(BorderFactory.createTitledBorder("Current Inventory"));
        currentInventoryPanel.setPreferredSize(new Dimension(200, 100));
        
        JScrollPane inventoryScrollPane = new JScrollPane(currentInventoryPanel);
        inventoryScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        inventoryScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        fetchInventoryButton.addActionListener(e -> {
            fetchCurrentInventory(currentInventoryPanel);
        });
        
        inventorySetupPanel.add(inventoryControlPanel, BorderLayout.WEST);
        inventorySetupPanel.add(inventoryScrollPane, BorderLayout.EAST);
        
        bankConfigPanel.setLayout(new BoxLayout(bankConfigPanel, BoxLayout.Y_AXIS));
        bankConfigPanel.add(bankLocationPanel);
        bankConfigPanel.add(foodPanel);
        bankConfigPanel.add(potionPanel);
        bankConfigPanel.add(inventorySetupPanel);
        
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
        weaponSelectionPanel.setLayout(new GridBagLayout());
        GridBagConstraints weaponGbc = new GridBagConstraints();
        weaponGbc.insets = new Insets(2, 2, 2, 2);
        weaponGbc.anchor = GridBagConstraints.WEST;
        
        // Weapon type selector
        JComboBox<String> weaponTypeCombo = new JComboBox<>(new String[]{"Melee", "Ranged", "Magic"});
        
        // Create database-driven weapon combo boxes
        primaryWeaponCombo = createSearchableWeaponComboBox(WeaponType.SWORD);
        secondaryWeaponCombo = createSearchableWeaponComboBox(WeaponType.SWORD);
        
        weaponTypeCombo.addActionListener(e -> {
            String selectedType = (String) weaponTypeCombo.getSelectedItem();
            WeaponType weaponType;
            switch (selectedType) {
                case "Ranged":
                    weaponType = WeaponType.SHORTBOW;
                    break;
                case "Magic":
                    weaponType = WeaponType.STAFF;
                    break;
                default:
                    weaponType = WeaponType.SWORD;
                    break;
            }
            updateWeaponComboBoxes(primaryWeaponCombo, weaponType);
            updateWeaponComboBoxes(secondaryWeaponCombo, weaponType);
        });
        
        weaponGbc.gridx = 0; weaponGbc.gridy = 0;
        weaponSelectionPanel.add(new JLabel("Type:"), weaponGbc);
        weaponGbc.gridx = 1;
        weaponSelectionPanel.add(weaponTypeCombo, weaponGbc);
        
        weaponGbc.gridx = 0; weaponGbc.gridy = 1;
        weaponSelectionPanel.add(new JLabel("Primary:"), weaponGbc);
        weaponGbc.gridx = 1;
        weaponSelectionPanel.add(primaryWeaponCombo, weaponGbc);
        
        weaponGbc.gridx = 0; weaponGbc.gridy = 2;
        weaponSelectionPanel.add(new JLabel("Secondary:"), weaponGbc);
        weaponGbc.gridx = 1;
        weaponSelectionPanel.add(secondaryWeaponCombo, weaponGbc);
        
        // Weapon Strategy Section
        JPanel weaponStrategyPanel = createTitledPanel("Combat Strategy");
        weaponStrategyPanel.setLayout(new GridBagLayout());
        GridBagConstraints strategyGbc = new GridBagConstraints();
        strategyGbc.insets = new Insets(2, 2, 2, 2);
        strategyGbc.anchor = GridBagConstraints.WEST;
        
        String[] strategies = {"Aggressive (Max DPS)", "Balanced (Mixed)", "Defensive (Safe)", "Adaptive (Auto)"};
        JComboBox<String> strategyCombo = new JComboBox<>(strategies);
        
        JCheckBox autoUpgrade = new JCheckBox("Auto Upgrade Weapons (Coming Soon)", false);
        autoUpgrade.setEnabled(false);
        
        strategyGbc.gridx = 0; strategyGbc.gridy = 0;
        weaponStrategyPanel.add(new JLabel("Strategy:"), strategyGbc);
        strategyGbc.gridx = 1;
        weaponStrategyPanel.add(strategyCombo, strategyGbc);
        
        strategyGbc.gridx = 0; strategyGbc.gridy = 1; strategyGbc.gridwidth = 2;
        weaponStrategyPanel.add(autoUpgrade, strategyGbc);
        
        // Gear Loadout Section
        JPanel gearLoadoutPanel = createTitledPanel("Gear Loadout");
        gearLoadoutPanel.setLayout(new BorderLayout());
        
        // Left side - fetch button only
        JPanel gearControlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton fetchGearButton = new JButton("Fetch Gear");
        
        gearControlPanel.add(fetchGearButton);
        
        // Right side - current gear list
        JPanel currentGearPanel = new JPanel();
        currentGearPanel.setLayout(new BoxLayout(currentGearPanel, BoxLayout.Y_AXIS));
        currentGearPanel.setBorder(BorderFactory.createTitledBorder("Current Gear"));
        currentGearPanel.setPreferredSize(new Dimension(200, 100));
        
        JScrollPane gearScrollPane = new JScrollPane(currentGearPanel);
        gearScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        gearScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        
        fetchGearButton.addActionListener(e -> {
            fetchCurrentGear(currentGearPanel);
        });
        
        gearLoadoutPanel.add(gearControlPanel, BorderLayout.WEST);
        gearLoadoutPanel.add(gearScrollPane, BorderLayout.EAST);
        
        // Add all sections to weapon panel with compact layout
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 1; gbc.fill = GridBagConstraints.HORIZONTAL;
        weaponPanel.add(weaponOptionsPanel, gbc);
        gbc.gridy = 1;
        weaponPanel.add(weaponSelectionPanel, gbc);
        gbc.gridy = 2;
        weaponPanel.add(weaponStrategyPanel, gbc);
        gbc.gridy = 3;
        weaponPanel.add(gearLoadoutPanel, gbc);
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
        saveConfigButton = new JButton("Save Profile");
        loadConfigButton = new JButton("Load Profile");
        exportLogsButton = new JButton("Export Logs");
        
        // Profile management components
        profileNameField = new JTextField(15);
        profileNameField.setToolTipText("Enter profile name to save");
        profileComboBox = new JComboBox<>();
        updateProfileList();
        
        JButton deleteProfileButton = new JButton("Delete");
        deleteProfileButton.addActionListener(e -> deleteProfile((String) profileComboBox.getSelectedItem()));
        
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
        controlPanel.add(new JSeparator(SwingConstants.VERTICAL));
        controlPanel.add(new JLabel("Profile:"));
        controlPanel.add(profileNameField);
        controlPanel.add(saveConfigButton);
        controlPanel.add(profileComboBox);
        controlPanel.add(loadConfigButton);
        controlPanel.add(deleteProfileButton);
        controlPanel.add(new JSeparator(SwingConstants.VERTICAL));
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
        // autoLoot.setSelected(true); // Removed - should be unchecked by default
        enableBanking.setSelected(true);
        autoWeaponSwitch.setSelected(true);
        enableAntiBan.setSelected(true);
        
        // Set default selections for new combo boxes
        if (npcComboBox != null && npcComboBox.getItemCount() > 0) {
            npcComboBox.setSelectedIndex(0);
        }
        if (locationComboBox != null && locationComboBox.getItemCount() > 0) {
            locationComboBox.setSelectedIndex(0);
        }
        if (lootComboBox != null && lootComboBox.getItemCount() > 0) {
            lootComboBox.setSelectedIndex(0);
        }
        
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
        // Validate target NPC selection
        if (npcComboBox.getSelectedItem() == null || npcComboBox.getSelectedItem().toString().trim().isEmpty()) {
            showError("Target NPC must be selected");
            return false;
        }
        
        // Validate location selection
        if (locationComboBox.getSelectedItem() == null || locationComboBox.getSelectedItem().toString().trim().isEmpty()) {
            showError("Location must be selected");
            return false;
        }
        
        // Validate banking configuration
        if (enableBanking.isSelected() && (bankLocationCombo.getSelectedItem() == null || bankLocationCombo.getSelectedItem().toString().trim().isEmpty())) {
            showError("Bank location cannot be empty when banking is enabled");
            return false;
        }
        
        // Validate food configuration
        if (withdrawFood.isSelected() && (foodComboBox.getSelectedItem() == null || foodComboBox.getSelectedItem().toString().trim().isEmpty())) {
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
        String profileName = profileNameField.getText().trim();
        if (profileName.isEmpty()) {
            profileName = "Default_" + System.currentTimeMillis();
        }
        
        updateConfiguration();
        saveProfileToFile(profileName, configuration);
        updateProfileList();
        logMessage("Profile '" + profileName + "' saved");
        Logger.log("[CombatGUI] Profile saved: " + profileName);
    }
    
    /**
     * Load configuration from file
     */
    private void loadConfiguration() {
        String selectedProfile = (String) profileComboBox.getSelectedItem();
        if (selectedProfile != null && !selectedProfile.isEmpty()) {
            Map<String, Object> loadedConfig = loadProfileFromFile(selectedProfile);
            if (loadedConfig != null) {
                applyConfigurationToGUI(loadedConfig);
                logMessage("Profile '" + selectedProfile + "' loaded");
                Logger.log("[CombatGUI] Profile loaded: " + selectedProfile);
            } else {
                logMessage("Failed to load profile: " + selectedProfile);
            }
        }
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
    
    // Database-driven combo box creation methods
    
    /**
     * Create searchable NPC combo box with database integration
     */
    private JComboBox<String> createSearchableNPCComboBox() {
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.setEditable(true);
        
        // Populate with all NPCs from database
        for (NPC npc : npcRepository.getAll()) {
            comboBox.addItem(npc.getName());
        }
        
        // Add search functionality
        JTextField textField = (JTextField) comboBox.getEditor().getEditorComponent();
        textField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String searchText = textField.getText().toLowerCase();
                comboBox.removeAllItems();
                
                for (NPC npc : npcRepository.findByName(searchText)) {
                    comboBox.addItem(npc.getName());
                }
                
                if (comboBox.getItemCount() > 0) {
                    comboBox.showPopup();
                }
            }
        });
        
        return comboBox;
    }
    
    /**
     * Create searchable bank location combo box with database integration
     */
    private JComboBox<String> createSearchableLocationComboBox() {
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.setEditable(true);
        
        // Initialize bank location repository if not already done
        if (locationRepository != null) {
            locationRepository.initialize();
            
            // Populate with all bank locations from database
            java.util.Set<String> uniqueLocations = new java.util.HashSet<>();
            for (BankLocation bank : locationRepository.getAll()) {
                if (bank.getName() != null && !bank.getName().trim().isEmpty()) {
                    uniqueLocations.add(bank.getName());
                }
            }
            
            // Add unique bank locations to combo box
            for (String location : uniqueLocations) {
                comboBox.addItem(location);
            }
        }
        
        // Add search functionality
        JTextField textField = (JTextField) comboBox.getEditor().getEditorComponent();
        textField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String searchText = textField.getText().toLowerCase();
                comboBox.removeAllItems();
                
                if (locationRepository != null) {
                    java.util.Set<String> filteredLocations = new java.util.HashSet<>();
                    for (BankLocation bank : locationRepository.getAll()) {
                        if (bank.getName() != null && 
                            bank.getName().toLowerCase().contains(searchText)) {
                            filteredLocations.add(bank.getName());
                        }
                    }
                    
                    for (String location : filteredLocations) {
                        comboBox.addItem(location);
                    }
                }
                
                if (comboBox.getItemCount() > 0) {
                    comboBox.showPopup();
                }
            }
        });
        
        return comboBox;
    }
    
    /**
     * Create searchable NPC location combo box with filtering by selected NPC
     */
    private JComboBox<String> createSearchableNPCLocationComboBox() {
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.setEditable(true);
        
        // Populate with all NPC locations from database
        java.util.Set<String> uniqueLocations = new java.util.HashSet<>();
        for (NPC npc : npcRepository.getAll()) {
            if (npc.getLocation() != null && !npc.getLocation().trim().isEmpty()) {
                uniqueLocations.add(npc.getLocation());
            }
        }
        
        // Add unique locations to combo box
        for (String location : uniqueLocations) {
            comboBox.addItem(location);
        }
        
        // Add search functionality
        JTextField textField = (JTextField) comboBox.getEditor().getEditorComponent();
        textField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String searchText = textField.getText().toLowerCase();
                comboBox.removeAllItems();
                
                java.util.Set<String> filteredLocations = new java.util.HashSet<>();
                for (NPC npc : npcRepository.getAll()) {
                    if (npc.getLocation() != null && 
                        npc.getLocation().toLowerCase().contains(searchText)) {
                        filteredLocations.add(npc.getLocation());
                    }
                }
                
                for (String location : filteredLocations) {
                    comboBox.addItem(location);
                }
                
                if (comboBox.getItemCount() > 0) {
                    comboBox.showPopup();
                }
            }
        });
        
        return comboBox;
    }
    
    /**
     * Update NPC location combo box based on selected NPC
     */
    private void updateNPCLocationComboBox(String selectedNPC) {
        if (locationComboBox != null && selectedNPC != null) {
            locationComboBox.removeAllItems();
            
            // Find locations for the selected NPC
            java.util.Set<String> npcLocations = new java.util.HashSet<>();
            for (NPC npc : npcRepository.getAll()) {
                if (npc.getName().equals(selectedNPC) && 
                    npc.getLocation() != null && !npc.getLocation().trim().isEmpty()) {
                    npcLocations.add(npc.getLocation());
                }
            }
            
            // Add filtered locations to combo box
            for (String location : npcLocations) {
                locationComboBox.addItem(location);
            }
        }
    }
    
    /**
     * Create searchable loot combo box with database integration
     */
    private JComboBox<String> createSearchableLootComboBox() {
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.setEditable(true);
        
        // Populate with all equipment and consumables from database
        for (Equipment equipment : equipmentRepository.getAll()) {
            comboBox.addItem(equipment.getName());
        }
        for (Consumable consumable : consumableRepository.getAll()) {
            comboBox.addItem(consumable.getName());
        }
        
        // Add search functionality
        JTextField textField = (JTextField) comboBox.getEditor().getEditorComponent();
        textField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String searchText = textField.getText().toLowerCase();
                comboBox.removeAllItems();
                
                // Search equipment
                for (Equipment equipment : equipmentRepository.findByName(searchText)) {
                    comboBox.addItem(equipment.getName());
                }
                // Search consumables
                for (Consumable consumable : consumableRepository.findByName(searchText)) {
                    comboBox.addItem(consumable.getName());
                }
                
                if (comboBox.getItemCount() > 0) {
                    comboBox.showPopup();
                }
            }
        });
        
        return comboBox;
    }
    
    /**
     * Create searchable food combo box with database integration
     */
    private JComboBox<String> createSearchableFoodComboBox() {
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.setEditable(true);
        
        // Populate with food items from database
        for (Consumable consumable : consumableRepository.getByType(Consumable.ConsumableType.FOOD)) {
            comboBox.addItem(consumable.getName());
        }
        
        // Add search functionality
        JTextField textField = (JTextField) comboBox.getEditor().getEditorComponent();
        textField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String searchText = textField.getText().toLowerCase();
                comboBox.removeAllItems();
                
                for (Consumable consumable : consumableRepository.getByType(Consumable.ConsumableType.FOOD)) {
                    if (consumable.getName().toLowerCase().contains(searchText)) {
                        comboBox.addItem(consumable.getName());
                    }
                }
                
                if (comboBox.getItemCount() > 0) {
                    comboBox.showPopup();
                }
            }
        });
        
        return comboBox;
    }
    
    /**
     * Create searchable potion combo box with database integration
     */
    private JComboBox<String> createSearchablePotionComboBox() {
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.setEditable(true);
        
        // Populate with potion items from database
        for (Consumable consumable : consumableRepository.getByType(Consumable.ConsumableType.POTION)) {
            comboBox.addItem(consumable.getName());
        }
        
        // Add search functionality
        JTextField textField = (JTextField) comboBox.getEditor().getEditorComponent();
        textField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String searchText = textField.getText().toLowerCase();
                comboBox.removeAllItems();
                
                for (Consumable consumable : consumableRepository.getByType(Consumable.ConsumableType.POTION)) {
                    if (consumable.getName().toLowerCase().contains(searchText)) {
                        comboBox.addItem(consumable.getName());
                    }
                }
                
                if (comboBox.getItemCount() > 0) {
                    comboBox.showPopup();
                }
            }
        });
        
        return comboBox;
    }
    
    /**
     * Create searchable weapon combo box with database integration
     */
    private JComboBox<String> createSearchableWeaponComboBox(WeaponType weaponType) {
        JComboBox<String> comboBox = new JComboBox<>();
        comboBox.setEditable(true);
        
        // Populate with weapons of specified type from database
        for (Equipment equipment : equipmentRepository.getByWeaponType(weaponType)) {
            comboBox.addItem(equipment.getName());
        }
        
        // Add search functionality
        JTextField textField = (JTextField) comboBox.getEditor().getEditorComponent();
        textField.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyReleased(java.awt.event.KeyEvent e) {
                String searchText = textField.getText().toLowerCase();
                comboBox.removeAllItems();
                
                for (Equipment equipment : equipmentRepository.getByWeaponType(weaponType)) {
                    if (equipment.getName().toLowerCase().contains(searchText)) {
                        comboBox.addItem(equipment.getName());
                    }
                }
                
                if (comboBox.getItemCount() > 0) {
                    comboBox.showPopup();
                }
            }
        });
        
        return comboBox;
    }
    
    /**
     * Update weapon combo boxes based on weapon type
     */
    private void updateWeaponComboBoxes(JComboBox<String> comboBox, WeaponType weaponType) {
        comboBox.removeAllItems();
        for (Equipment equipment : equipmentRepository.getByWeaponType(weaponType)) {
            comboBox.addItem(equipment.getName());
        }
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
    
    // Profile Management Methods
    private void updateProfileList() {
        profileComboBox.removeAllItems();
        File profileDir = getProfileDirectory();
        if (profileDir.exists() && profileDir.isDirectory()) {
            File[] profileFiles = profileDir.listFiles((dir, name) -> name.endsWith(".json"));
            if (profileFiles != null) {
                for (File file : profileFiles) {
                    String profileName = file.getName().replace(".json", "");
                    profileComboBox.addItem(profileName);
                }
            }
        }
    }
    
    private void deleteProfile(String profileName) {
        if (profileName != null && !profileName.isEmpty()) {
            File profileFile = new File(getProfileDirectory(), profileName + ".json");
            if (profileFile.exists() && profileFile.delete()) {
                updateProfileList();
                logMessage("Profile '" + profileName + "' deleted");
            } else {
                logMessage("Failed to delete profile: " + profileName);
            }
        }
    }
    
    private File getProfileDirectory() {
        String userHome = System.getProperty("user.home");
        File dreamBotDir = new File(userHome, ".dreambot");
        File scriptsDir = new File(dreamBotDir, "Scripts");
        File profileDir = new File(scriptsDir, "AICombatScript_Profiles");
        if (!profileDir.exists()) {
            profileDir.mkdirs();
        }
        return profileDir;
    }
    
    private void saveProfileToFile(String profileName, Map<String, Object> config) {
        try {
            File profileFile = new File(getProfileDirectory(), profileName + ".json");
            // Convert config to JSON and save
            // For now, using a simple approach - in production, use a proper JSON library
            StringBuilder json = new StringBuilder();
            json.append("{");
            boolean first = true;
            for (Map.Entry<String, Object> entry : config.entrySet()) {
                if (!first) json.append(",");
                json.append("\"").append(entry.getKey()).append("\":\"").append(entry.getValue()).append("\"");
                first = false;
            }
            json.append("}");
            
            try (FileWriter writer = new FileWriter(profileFile)) {
                writer.write(json.toString());
            }
        } catch (Exception e) {
            Logger.log("[CombatGUI] Error saving profile: " + e.getMessage());
        }
    }
    
    private Map<String, Object> loadProfileFromFile(String profileName) {
        try {
            File profileFile = new File(getProfileDirectory(), profileName + ".json");
            if (!profileFile.exists()) return null;
            
            // Simple JSON parsing
            Map<String, Object> config = new HashMap<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(profileFile))) {
                String content = reader.readLine();
                if (content != null && content.startsWith("{") && content.endsWith("}")) {
                    content = content.substring(1, content.length() - 1); // Remove { }
                    String[] pairs = content.split(",");
                    for (String pair : pairs) {
                        String[] keyValue = pair.split(":", 2);
                        if (keyValue.length == 2) {
                            String key = keyValue[0].trim().replaceAll("\"", "");
                            String value = keyValue[1].trim().replaceAll("\"", "");
                            
                            // Convert string values to appropriate types
                            if (value.equals("true") || value.equals("false")) {
                                config.put(key, Boolean.parseBoolean(value));
                            } else if (value.matches("\\d+")) {
                                config.put(key, Integer.parseInt(value));
                            } else {
                                config.put(key, value);
                            }
                        }
                    }
                }
            }
            return config;
        } catch (Exception e) {
            Logger.log("[CombatGUI] Error loading profile: " + e.getMessage());
            return null;
        }
    }
    
    private void applyConfigurationToGUI(Map<String, Object> config) {
        try {
            // Combat Options
            if (config.containsKey("combatStyle")) {
                combatStyleCombo.setSelectedItem(config.get("combatStyle"));
            }
            if (config.containsKey("targetPriority")) {
                targetPriorityCombo.setSelectedItem(config.get("targetPriority"));
            }
            if (config.containsKey("enableSpecialAttack")) {
                enableSpecialAttack.setSelected((Boolean) config.get("enableSpecialAttack"));
            }
            if (config.containsKey("autoLoot")) {
                autoLoot.setSelected((Boolean) config.get("autoLoot"));
            }
            if (config.containsKey("safeSpotting")) {
                safeSpotting.setSelected((Boolean) config.get("safeSpotting"));
            }
            if (config.containsKey("prayerFlicking")) {
                prayerFlicking.setSelected((Boolean) config.get("prayerFlicking"));
            }
            if (config.containsKey("minSpecEnergy")) {
                minSpecEnergySpinner.setValue(config.get("minSpecEnergy"));
            }
            if (config.containsKey("maxSpecEnergy")) {
                maxSpecEnergySpinner.setValue(config.get("maxSpecEnergy"));
            }
            
            // Food and Health
            if (config.containsKey("foodThreshold")) {
                foodThresholdSpinner.setValue(config.get("foodThreshold"));
            }
            
            // Target Selection
            if (config.containsKey("targetNPC")) {
                npcComboBox.setSelectedItem(config.get("targetNPC"));
            }
            if (config.containsKey("targetLocation")) {
                locationComboBox.setSelectedItem(config.get("targetLocation"));
            }
            
            // Target Selection - using lootFilterField as placeholder since target fields don't exist
            if (config.containsKey("targetNPC")) {
                // targetNPCField.setText((String) config.get("targetNPC"));
                // Note: targetNPCField not found, skipping
            }
            if (config.containsKey("targetLocation")) {
                // targetLocationField.setText((String) config.get("targetLocation"));
                // Note: targetLocationField not found, skipping
            }
            
            // Loot Filter
            if (config.containsKey("lootFilter")) {
                lootComboBox.setSelectedItem(config.get("lootFilter"));
            }
            
            // Banking Options
            if (config.containsKey("enableBanking")) {
                enableBanking.setSelected((Boolean) config.get("enableBanking"));
            }
            if (config.containsKey("depositJunk")) {
                depositJunk.setSelected((Boolean) config.get("depositJunk"));
            }
            if (config.containsKey("withdrawFood")) {
                withdrawFood.setSelected((Boolean) config.get("withdrawFood"));
            }
            if (config.containsKey("withdrawPotions")) {
                withdrawPotions.setSelected((Boolean) config.get("withdrawPotions"));
            }
            if (config.containsKey("minFood")) {
                minFoodSpinner.setValue(config.get("minFood"));
            }
            if (config.containsKey("maxFood")) {
                maxFoodSpinner.setValue(config.get("maxFood"));
            }
            if (config.containsKey("minPotions")) {
                minPotionSpinner.setValue(config.get("minPotions"));
            }
            if (config.containsKey("bankLocation")) {
                bankLocationCombo.setSelectedItem(config.get("bankLocation"));
            }
            if (config.containsKey("foodType")) {
                foodComboBox.setSelectedItem(config.get("foodType"));
            }
            if (config.containsKey("potionType")) {
                potionComboBox.setSelectedItem(config.get("potionType"));
            }
            
            // Weapon Options
            if (config.containsKey("autoWeaponSwitch")) {
                autoWeaponSwitch.setSelected((Boolean) config.get("autoWeaponSwitch"));
            }
            if (config.containsKey("ammunitionManagement")) {
                ammunitionManagement.setSelected((Boolean) config.get("ammunitionManagement"));
            }
            if (config.containsKey("weaponSwitchDelay")) {
                weaponSwitchDelaySpinner.setValue(config.get("weaponSwitchDelay"));
            }
            if (config.containsKey("minAmmo")) {
                minAmmoSpinner.setValue(config.get("minAmmo"));
            }
            if (config.containsKey("primaryWeapon")) {
                primaryWeaponCombo.setSelectedItem(config.get("primaryWeapon"));
            }
            if (config.containsKey("secondaryWeapon")) {
                secondaryWeaponCombo.setSelectedItem(config.get("secondaryWeapon"));
            }
            
            // Anti-Ban Options
            if (config.containsKey("enableAntiBan")) {
                enableAntiBan.setSelected((Boolean) config.get("enableAntiBan"));
            }
            if (config.containsKey("randomBreaks")) {
                randomBreaks.setSelected((Boolean) config.get("randomBreaks"));
            }
            if (config.containsKey("cameraMovement")) {
                cameraMovement.setSelected((Boolean) config.get("cameraMovement"));
            }
            if (config.containsKey("mouseVariation")) {
                mouseVariation.setSelected((Boolean) config.get("mouseVariation"));
            }
            if (config.containsKey("breakFrequency")) {
                breakFrequencySpinner.setValue(config.get("breakFrequency"));
            }
            if (config.containsKey("breakDuration")) {
                breakDurationSpinner.setValue(config.get("breakDuration"));
            }
            if (config.containsKey("actionDelay")) {
                actionDelaySpinner.setValue(config.get("actionDelay"));
            }
            if (config.containsKey("humanLikeness")) {
                humanLikenessSlider.setValue((Integer) config.get("humanLikeness"));
            }
            
            Logger.log("[CombatGUI] Configuration applied to GUI successfully");
            logMessage("Profile configuration loaded successfully");
            
        } catch (Exception e) {
            Logger.log("[CombatGUI] Error applying configuration to GUI: " + e.getMessage());
            logMessage("Error loading profile configuration: " + e.getMessage());
        }
    }
    
    // Gear and Inventory Fetching Methods
    private void fetchCurrentGear(JPanel gearPanel) {
        gearPanel.removeAll();
        try {
            if (org.dreambot.api.methods.container.impl.equipment.Equipment.isEmpty()) {
                logMessage("No items equipped.");
                return;
            }
            
            String[] slotNames = {"Helmet", "Cape", "Amulet", "Weapon", "Body", "Shield", "Legs", "Gloves", "Boots", "Ring"};
            for (int i = 0; i < slotNames.length; i++) {
                org.dreambot.api.wrappers.items.Item item = org.dreambot.api.methods.container.impl.equipment.Equipment.getItemInSlot(i);
                if (item != null) {
                    addItemToPanel(gearPanel, item.getName(), slotNames[i]);
                }
            }
            logMessage("Current gear fetched successfully.");
        } catch (Exception e) {
            Logger.log("Error fetching current gear: " + e.getMessage());
            logMessage("Error fetching gear - check console for details.");
        }
        gearPanel.revalidate();
        gearPanel.repaint();
    }
    
    private void fetchCurrentInventory(JPanel inventoryPanel) {
        inventoryPanel.removeAll();
        try {
            for (org.dreambot.api.wrappers.items.Item item : org.dreambot.api.methods.container.impl.Inventory.all()) {
                if (item != null && item.getName() != null) {
                    String itemName = item.getName();
                    if (item.getAmount() > 1) {
                        itemName += " (" + item.getAmount() + ")";
                    }
                    addItemToPanel(inventoryPanel, itemName, "Inventory");
                }
            }
            logMessage("Current inventory fetched successfully.");
        } catch (Exception e) {
            Logger.log("Error fetching current inventory: " + e.getMessage());
            logMessage("Error fetching inventory - check console for details.");
        }
        inventoryPanel.revalidate();
        inventoryPanel.repaint();
    }
    
    private void addItemToPanel(JPanel panel, String itemName, String category) {
        JPanel itemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
        
        JLabel itemLabel = new JLabel(itemName);
        JButton removeButton = new JButton("-");
        removeButton.setPreferredSize(new Dimension(20, 20));
        removeButton.setMargin(new Insets(0, 0, 0, 0));
        removeButton.setToolTipText("Remove " + itemName + " from " + category.toLowerCase());
        
        removeButton.addActionListener(e -> {
            panel.remove(itemPanel);
            panel.revalidate();
            panel.repaint();
            logMessage("Removed " + itemName + " from " + category.toLowerCase() + " display");
        });
        
        itemPanel.add(itemLabel);
        itemPanel.add(removeButton);
        panel.add(itemPanel);
    }
    
    // Enhanced configuration management
    private void updateConfiguration() {
        // Combat options
        configuration.put("combatStyle", mapCombatStyle((String) combatStyleCombo.getSelectedItem()));
        configuration.put("targetPriority", targetPriorityCombo.getSelectedItem());
        configuration.put("enableSpecialAttack", enableSpecialAttack.isSelected());
        configuration.put("minSpecEnergy", minSpecEnergySpinner.getValue());
        configuration.put("maxSpecEnergy", maxSpecEnergySpinner.getValue());
        configuration.put("autoLoot", autoLoot.isSelected());
        configuration.put("safeSpotting", safeSpotting.isSelected());
        configuration.put("prayerFlicking", prayerFlicking.isSelected());
        configuration.put("foodThreshold", foodThresholdSpinner.getValue());
        configuration.put("targetNPC", npcComboBox.getSelectedItem());
        configuration.put("targetLocation", locationComboBox.getSelectedItem());
        configuration.put("lootFilter", lootComboBox.getSelectedItem());
        
        // Banking options
        configuration.put("enableBanking", enableBanking.isSelected());
        configuration.put("depositJunk", depositJunk.isSelected());
        configuration.put("withdrawFood", withdrawFood.isSelected());
        configuration.put("withdrawPotions", withdrawPotions.isSelected());
        configuration.put("minFood", minFoodSpinner.getValue());
        configuration.put("maxFood", maxFoodSpinner.getValue());
        configuration.put("minPotions", minPotionSpinner.getValue());
        configuration.put("bankLocation", bankLocationCombo.getSelectedItem() != null ? bankLocationCombo.getSelectedItem().toString() : "");
        configuration.put("foodType", foodComboBox.getSelectedItem() != null ? foodComboBox.getSelectedItem().toString() : "");
        configuration.put("potionType", potionComboBox.getSelectedItem() != null ? potionComboBox.getSelectedItem().toString() : "");
        
        // Weapon options
        configuration.put("autoWeaponSwitch", autoWeaponSwitch.isSelected());
        configuration.put("ammunitionManagement", ammunitionManagement.isSelected());
        configuration.put("weaponSwitchDelay", weaponSwitchDelaySpinner.getValue());
        configuration.put("minAmmo", minAmmoSpinner.getValue());
        configuration.put("primaryWeapon", primaryWeaponCombo.getSelectedItem());
        configuration.put("secondaryWeapon", secondaryWeaponCombo.getSelectedItem());
        
        // Anti-ban options
        configuration.put("enableAntiBan", enableAntiBan.isSelected());
        configuration.put("randomBreaks", randomBreaks.isSelected());
        configuration.put("cameraMovement", cameraMovement.isSelected());
        configuration.put("mouseVariation", mouseVariation.isSelected());
        configuration.put("breakFrequency", breakFrequencySpinner.getValue());
        configuration.put("breakDuration", breakDurationSpinner.getValue());
        configuration.put("actionDelay", actionDelaySpinner.getValue());
        configuration.put("humanLikeness", humanLikenessSlider.getValue());
        
        Logger.log("[CombatGUI] Configuration updated with all current settings");
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
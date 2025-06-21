package database.repositories;

import database.models.Equipment;
import database.models.WeaponType;
import database.models.ArmorType;
import database.core.DataLoader;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.*;
import java.util.stream.Collectors;
import java.lang.reflect.Type;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

/**
 * Repository for managing equipment data with efficient querying and filtering
 */
public class EquipmentRepository {
    private static final Logger logger = Logger.getLogger(EquipmentRepository.class.getName());
    private static final String EQUIPMENT_DATA_FILE = "data/equipment.json";
    
    private final Map<Integer, Equipment> equipmentById;
    private final Map<String, List<Equipment>> equipmentByName;
    private final Map<Equipment.EquipmentSlot, List<Equipment>> equipmentBySlot;
    private final Map<WeaponType, List<Equipment>> equipmentByWeaponType;
    private final Map<ArmorType, List<Equipment>> equipmentByArmorType;
    private final DataLoader dataLoader;
    private final Gson gson;
    
    private boolean initialized = false;
    
    public EquipmentRepository() {
        this.equipmentById = new ConcurrentHashMap<>();
        this.equipmentByName = new ConcurrentHashMap<>();
        this.equipmentBySlot = new ConcurrentHashMap<>();
        this.equipmentByWeaponType = new ConcurrentHashMap<>();
        this.equipmentByArmorType = new ConcurrentHashMap<>();
        this.dataLoader = new DataLoader();
        this.gson = new Gson();
        
        // Initialize slot maps
        for (Equipment.EquipmentSlot slot : Equipment.EquipmentSlot.values()) {
            equipmentBySlot.put(slot, new ArrayList<>());
        }
        
        // Initialize weapon type maps
        for (WeaponType type : WeaponType.values()) {
            equipmentByWeaponType.put(type, new ArrayList<>());
        }
        
        // Initialize armor type maps
        for (ArmorType type : ArmorType.values()) {
            equipmentByArmorType.put(type, new ArrayList<>());
        }
    }
    
    /**
     * Initialize the repository by loading equipment data
     */
    public void initialize() {
        if (initialized) {
            logger.info("Equipment repository already initialized");
            return;
        }
        
        try {
            logger.info("Initializing equipment repository...");
            loadEquipmentData();
            buildIndices();
            initialized = true;
            logger.info(String.format("Equipment repository initialized with %d items", equipmentById.size()));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize equipment repository", e);
            throw new RuntimeException("Equipment repository initialization failed", e);
        }
    }
    
    /**
     * Load equipment data from JSON file
     */
    private void loadEquipmentData() {
        // Load hardcoded equipment data since JSON resources aren't being packaged properly
        loadDefaultEquipment();
    }
    
    /**
     * Load default equipment data if file loading fails
     */
    private void loadDefaultEquipment() {
        logger.info("Loading default equipment data...");
        
        // Basic weapons
        addDefaultWeapon(1205, "Bronze dagger", WeaponType.DAGGER, 1, 1);
        addDefaultWeapon(1277, "Iron sword", WeaponType.SWORD, 1, 1);
        addDefaultWeapon(1323, "Steel scimitar", WeaponType.SCIMITAR, 5, 5);
        addDefaultWeapon(1333, "Mithril scimitar", WeaponType.SCIMITAR, 20, 20);
        addDefaultWeapon(1343, "Adamant scimitar", WeaponType.SCIMITAR, 30, 30);
        addDefaultWeapon(1353, "Rune scimitar", WeaponType.SCIMITAR, 40, 40);
        addDefaultWeapon(4587, "Dragon scimitar", WeaponType.SCIMITAR, 60, 60);
        
        // Ranged weapons
        addDefaultRangedWeapon(841, "Shortbow", WeaponType.SHORTBOW, 1, 1);
        addDefaultRangedWeapon(843, "Oak shortbow", WeaponType.SHORTBOW, 5, 5);
        addDefaultRangedWeapon(845, "Willow shortbow", WeaponType.SHORTBOW, 20, 20);
        addDefaultRangedWeapon(847, "Maple shortbow", WeaponType.SHORTBOW, 30, 30);
        addDefaultRangedWeapon(849, "Yew shortbow", WeaponType.SHORTBOW, 40, 40);
        addDefaultRangedWeapon(851, "Magic shortbow", WeaponType.SHORTBOW, 50, 50);
        
        // Basic armor sets
        addDefaultArmorSet("Bronze", 1, 1);
        addDefaultArmorSet("Iron", 1, 1);
        addDefaultArmorSet("Steel", 5, 5);
        addDefaultArmorSet("Mithril", 20, 20);
        addDefaultArmorSet("Adamant", 30, 30);
        addDefaultArmorSet("Rune", 40, 40);
        
        logger.info(String.format("Loaded %d default equipment items", equipmentById.size()));
    }
    
    private void addDefaultWeapon(int id, String name, WeaponType type, int attackReq, int defenseReq) {
        Equipment weapon = new Equipment(id, name, Equipment.EquipmentSlot.WEAPON);
        weapon.setWeaponType(type);
        weapon.addLevelRequirement("attack", attackReq);
        weapon.addLevelRequirement("defence", defenseReq);
        weapon.setTradeable(true);
        weapon.setValue(1000 * attackReq); // Basic value calculation
        equipmentById.put(id, weapon);
    }
    
    private void addDefaultRangedWeapon(int id, String name, WeaponType type, int rangedReq, int defenseReq) {
        Equipment weapon = new Equipment(id, name, Equipment.EquipmentSlot.WEAPON);
        weapon.setWeaponType(type);
        weapon.addLevelRequirement("ranged", rangedReq);
        weapon.addLevelRequirement("defence", defenseReq);
        weapon.setTradeable(true);
        weapon.setValue(1000 * rangedReq);
        equipmentById.put(id, weapon);
    }
    
    private void addDefaultArmorSet(String material, int attackReq, int defenseReq) {
        int baseId = getBaseIdForMaterial(material);
        
        // Helmet
        Equipment helmet = new Equipment(baseId + 1, material + " full helm", Equipment.EquipmentSlot.HEAD);
        helmet.setArmorType(ArmorType.MELEE);
        helmet.addLevelRequirement("defence", defenseReq);
        helmet.setTradeable(true);
        helmet.setValue(500 * defenseReq);
        equipmentById.put(helmet.getItemId(), helmet);
        
        // Body
        Equipment body = new Equipment(baseId + 2, material + " platebody", Equipment.EquipmentSlot.BODY);
        body.setArmorType(ArmorType.MELEE);
        body.addLevelRequirement("defence", defenseReq);
        body.setTradeable(true);
        body.setValue(1000 * defenseReq);
        equipmentById.put(body.getItemId(), body);
        
        // Legs
        Equipment legs = new Equipment(baseId + 3, material + " platelegs", Equipment.EquipmentSlot.LEGS);
        legs.setArmorType(ArmorType.MELEE);
        legs.addLevelRequirement("defence", defenseReq);
        legs.setTradeable(true);
        legs.setValue(750 * defenseReq);
        equipmentById.put(legs.getItemId(), legs);
    }
    
    private int getBaseIdForMaterial(String material) {
        switch (material.toLowerCase()) {
            case "bronze": return 1100;
            case "iron": return 1200;
            case "steel": return 1300;
            case "mithril": return 1400;
            case "adamant": return 1500;
            case "rune": return 1600;
            default: return 1000;
        }
    }
    
    /**
     * Build search indices for efficient querying
     */
    private void buildIndices() {
        logger.info("Building equipment search indices...");
        
        // Clear existing indices
        equipmentByName.clear();
        equipmentBySlot.values().forEach(List::clear);
        equipmentByWeaponType.values().forEach(List::clear);
        equipmentByArmorType.values().forEach(List::clear);
        
        for (Equipment equipment : equipmentById.values()) {
            // Index by name
            String nameLower = equipment.getName().toLowerCase();
            equipmentByName.computeIfAbsent(nameLower, k -> new ArrayList<>()).add(equipment);
            
            // Index by slot
            if (equipment.getSlot() != null) {
                equipmentBySlot.get(equipment.getSlot()).add(equipment);
            }
            
            // Index by weapon type
            if (equipment.getWeaponType() != null) {
                equipmentByWeaponType.get(equipment.getWeaponType()).add(equipment);
            }
            
            // Index by armor type
            if (equipment.getArmorType() != null) {
                equipmentByArmorType.get(equipment.getArmorType()).add(equipment);
            }
        }
        
        logger.info("Equipment search indices built successfully");
    }
    
    // Query methods
    
    /**
     * Get equipment by item ID
     */
    public Equipment getById(int itemId) {
        ensureInitialized();
        return equipmentById.get(itemId);
    }
    
    /**
     * Get all equipment
     */
    public List<Equipment> getAll() {
        ensureInitialized();
        return new ArrayList<>(equipmentById.values());
    }
    
    /**
     * Find equipment by name (case-insensitive)
     */
    public List<Equipment> findByName(String name) {
        ensureInitialized();
        return equipmentByName.getOrDefault(name.toLowerCase(), new ArrayList<>());
    }
    
    /**
     * Get equipment by slot
     */
    public List<Equipment> getBySlot(Equipment.EquipmentSlot slot) {
        ensureInitialized();
        return new ArrayList<>(equipmentBySlot.getOrDefault(slot, new ArrayList<>()));
    }
    
    /**
     * Get equipment by weapon type
     */
    public List<Equipment> getByWeaponType(WeaponType weaponType) {
        ensureInitialized();
        return new ArrayList<>(equipmentByWeaponType.getOrDefault(weaponType, new ArrayList<>()));
    }
    
    /**
     * Get equipment by armor type
     */
    public List<Equipment> getByArmorType(ArmorType armorType) {
        ensureInitialized();
        return new ArrayList<>(equipmentByArmorType.getOrDefault(armorType, new ArrayList<>()));
    }
    
    /**
     * Find equipment suitable for player level
     */
    public List<Equipment> findSuitableEquipment(Map<String, Integer> playerStats) {
        ensureInitialized();
        return equipmentById.values().stream()
                .filter(equipment -> equipment.canEquip(playerStats))
                .collect(Collectors.toList());
    }
    
    /**
     * Find best equipment for slot and player level
     */
    public Equipment findBestForSlot(Equipment.EquipmentSlot slot, Map<String, Integer> playerStats) {
        ensureInitialized();
        return getBySlot(slot).stream()
                .filter(equipment -> equipment.canEquip(playerStats))
                .max(Comparator.comparingInt(Equipment::getValue))
                .orElse(null);
    }
    
    /**
     * Find weapons by combat style
     */
    public List<Equipment> findWeaponsByCombatStyle(String combatStyle) {
        ensureInitialized();
        return equipmentById.values().stream()
                .filter(equipment -> equipment.getSlot() == Equipment.EquipmentSlot.WEAPON)
                .filter(equipment -> equipment.supportsCombatStyle(combatStyle))
                .collect(Collectors.toList());
    }
    
    /**
     * Search equipment by partial name match
     */
    public List<Equipment> searchByName(String partialName) {
        ensureInitialized();
        String searchTerm = partialName.toLowerCase();
        return equipmentById.values().stream()
                .filter(equipment -> equipment.getName().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
    }
    
    /**
     * Get equipment within value range
     */
    public List<Equipment> getByValueRange(int minValue, int maxValue) {
        ensureInitialized();
        return equipmentById.values().stream()
                .filter(equipment -> equipment.getValue() >= minValue && equipment.getValue() <= maxValue)
                .collect(Collectors.toList());
    }
    
    /**
     * Get tradeable equipment only
     */
    public List<Equipment> getTradeableEquipment() {
        ensureInitialized();
        return equipmentById.values().stream()
                .filter(Equipment::isTradeable)
                .collect(Collectors.toList());
    }
    
    /**
     * Get equipment with specific level requirement
     */
    public List<Equipment> getByLevelRequirement(String skill, int level) {
        ensureInitialized();
        return equipmentById.values().stream()
                .filter(equipment -> equipment.hasLevelRequirement(skill, level))
                .collect(Collectors.toList());
    }
    
    /**
     * Get equipment count
     */
    public int getCount() {
        ensureInitialized();
        return equipmentById.size();
    }
    
    /**
     * Check if repository contains equipment with given ID
     */
    public boolean contains(int itemId) {
        ensureInitialized();
        return equipmentById.containsKey(itemId);
    }
    
    /**
     * Add or update equipment
     */
    public void addOrUpdate(Equipment equipment) {
        if (equipment == null || equipment.getItemId() <= 0) {
            throw new IllegalArgumentException("Invalid equipment data");
        }
        
        ensureInitialized();
        equipmentById.put(equipment.getItemId(), equipment);
        buildIndices(); // Rebuild indices after modification
        
        logger.info(String.format("Added/updated equipment: %s (ID: %d)", equipment.getName(), equipment.getItemId()));
    }
    
    /**
     * Remove equipment by ID
     */
    public boolean remove(int itemId) {
        ensureInitialized();
        Equipment removed = equipmentById.remove(itemId);
        if (removed != null) {
            buildIndices(); // Rebuild indices after modification
            logger.info(String.format("Removed equipment: %s (ID: %d)", removed.getName(), itemId));
            return true;
        }
        return false;
    }
    
    /**
     * Clear all equipment data
     */
    public void clear() {
        equipmentById.clear();
        equipmentByName.clear();
        equipmentBySlot.values().forEach(List::clear);
        equipmentByWeaponType.values().forEach(List::clear);
        equipmentByArmorType.values().forEach(List::clear);
        initialized = false;
        logger.info("Equipment repository cleared");
    }
    
    /**
     * Reload equipment data
     */
    public void reload() {
        logger.info("Reloading equipment repository...");
        clear();
        initialize();
    }
    
    private void ensureInitialized() {
        if (!initialized) {
            initialize();
        }
    }
    
    /**
     * Get repository statistics
     */
    public Map<String, Object> getStatistics() {
        ensureInitialized();
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalEquipment", equipmentById.size());
        stats.put("weaponCount", equipmentBySlot.get(Equipment.EquipmentSlot.WEAPON).size());
        stats.put("armorCount", equipmentById.size() - equipmentBySlot.get(Equipment.EquipmentSlot.WEAPON).size());
        stats.put("tradeableCount", getTradeableEquipment().size());
        
        // Count by weapon types
        Map<String, Integer> weaponTypeCounts = new HashMap<>();
        for (WeaponType type : WeaponType.values()) {
            weaponTypeCounts.put(type.name(), equipmentByWeaponType.get(type).size());
        }
        stats.put("weaponTypes", weaponTypeCounts);
        
        // Count by armor types
        Map<String, Integer> armorTypeCounts = new HashMap<>();
        for (ArmorType type : ArmorType.values()) {
            armorTypeCounts.put(type.name(), equipmentByArmorType.get(type).size());
        }
        stats.put("armorTypes", armorTypeCounts);
        
        return stats;
    }
}
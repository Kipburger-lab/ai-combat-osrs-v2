package database.repositories;

import database.models.NPC;
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
 * Repository for managing NPC data with efficient querying and filtering
 */
public class NPCRepository {
    private static final Logger logger = Logger.getLogger(NPCRepository.class.getName());
    private static final String NPC_DATA_FILE = "data/npcs.json";
    
    private final Map<Integer, NPC> npcsById;
    private final Map<String, List<NPC>> npcsByName;
    private final Map<String, List<NPC>> npcsByRegion;
    private final Map<String, List<NPC>> npcsByLocation;
    private final Map<NPC.NPCType, List<NPC>> npcsByType;
    private final Map<Integer, List<NPC>> npcsByCombatLevel;
    private final Map<String, List<NPC>> npcsBySlayerCategory;
    private final DataLoader dataLoader;
    private final Gson gson;
    
    private boolean initialized = false;
    
    public NPCRepository() {
        this.npcsById = new ConcurrentHashMap<>();
        this.npcsByName = new ConcurrentHashMap<>();
        this.npcsByRegion = new ConcurrentHashMap<>();
        this.npcsByLocation = new ConcurrentHashMap<>();
        this.npcsByType = new ConcurrentHashMap<>();
        this.npcsByCombatLevel = new ConcurrentHashMap<>();
        this.npcsBySlayerCategory = new ConcurrentHashMap<>();
        this.dataLoader = new DataLoader();
        this.gson = new Gson();
        
        // Initialize type maps
        for (NPC.NPCType type : NPC.NPCType.values()) {
            npcsByType.put(type, new ArrayList<>());
        }
    }
    
    /**
     * Initialize the repository by loading NPC data
     */
    public void initialize() {
        if (initialized) {
            logger.info("NPC repository already initialized");
            return;
        }
        
        try {
            logger.info("Initializing NPC repository...");
            loadNPCData();
            buildIndices();
            initialized = true;
            logger.info(String.format("NPC repository initialized with %d NPCs", npcsById.size()));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize NPC repository", e);
            throw new RuntimeException("NPC repository initialization failed", e);
        }
    }
    
    /**
     * Load NPC data from JSON file
     */
    private void loadNPCData() {
        // Load hardcoded NPC data since JSON resources aren't being packaged properly
        loadDefaultNPCs();
    }
    
    /**
     * Load default NPC data with accurate location information
     */
    private void loadDefaultNPCs() {
        logger.info("Loading default NPC data with accurate locations...");
        
        // Chickens - Multiple locations
        addNPCWithMultipleLocations(41, "Chicken", NPC.NPCType.MONSTER, 1, new String[]{
            "Lumbridge Farm (east of castle)|Lumbridge|3235|3295|0",
            "Fred's Farm (north of Lumbridge)|Lumbridge|3190|3273|0", 
            "South of Falador Farm|Falador|3026|3291|0",
            "Champions' Guild|Varrock|3191|3362|0",
            "Entrana|Entrana|2834|3338|0",
            "Rellekka|Rellekka|2670|3693|0"
        });
        
        // Cows - Multiple locations
        addNPCWithMultipleLocations(81, "Cow", NPC.NPCType.MONSTER, 2, new String[]{
            "Lumbridge Cow Field (east)|Lumbridge|3253|3266|0",
            "Lumbridge Cow Field (north of windmill)|Lumbridge|3162|3315|0",
            "South Falador Farm|Falador|3026|3291|0",
            "Champions' Guild Field|Varrock|3191|3362|0",
            "Crafting Guild|Falador|2905|3443|0",
            "Ardougne Farm|Ardougne|2627|3302|0",
            "Hosidius (north of town square)|Hosidius|1778|3517|0"
        });
        
        // Goblins - Multiple locations
        addNPCWithMultipleLocations(101, "Goblin", NPC.NPCType.MONSTER, 2, new String[]{
            "Lumbridge (around castle)|Lumbridge|3226|3261|0",
            "Lumbridge (north-west of castle)|Lumbridge|3248|3261|0",
            "Between Lumbridge and Varrock|Misthalin|3270|3310|0",
            "West of Varrock|Varrock|3150|3400|0",
            "Outside Port Sarim|Port Sarim|3024|3264|0",
            "South-east of Draynor Village|Draynor|3118|3244|0",
            "Goblin Cave (west of Fishing Guild)|Kandarin|2616|3393|0",
            "West of Fishing Guild|Kandarin|2624|3393|0"
        });
        
        // Guards - Multiple locations  
        addNPCWithMultipleLocations(368, "Guard", NPC.NPCType.MONSTER, 21, new String[]{
            "Varrock Palace|Varrock|3210|3462|0",
            "Varrock (around city)|Varrock|3174|3427|0",
            "Falador (White Knights' Castle)|Falador|2967|3343|0",
            "Al Kharid Palace|Al Kharid|3288|3169|0",
            "Ardougne (East)|Ardougne|2662|3305|0"
        });
        
        // Al Kharid Warriors
        addDefaultNPC(3103, "Al Kharid warrior", NPC.NPCType.MONSTER, 9, "Al Kharid", "Al Kharid Palace", 3288, 3169, 0);
        addDefaultNPC(3104, "Al Kharid warrior", NPC.NPCType.MONSTER, 9, "Al Kharid", "Al Kharid Palace", 3295, 3170, 0);
        
        // Men and Women
        addDefaultNPC(1, "Man", NPC.NPCType.MONSTER, 2, "Lumbridge", "Lumbridge", 3222, 3218, 0);
        addDefaultNPC(2, "Woman", NPC.NPCType.MONSTER, 2, "Lumbridge", "Lumbridge", 3222, 3218, 0);
        
        // Rats
        addDefaultNPC(2856, "Giant rat", NPC.NPCType.MONSTER, 6, "Lumbridge", "Lumbridge Basement", 3192, 3209, 0);
        
        // Dark Wizards
        addDefaultNPC(2870, "Dark wizard", NPC.NPCType.MONSTER, 7, "Draynor", "South of Draynor Village", 3223, 3369, 0);
        
        // Rock Crabs (Members)
        addDefaultNPC(1265, "Rock Crab", NPC.NPCType.MONSTER, 13, "Rellekka", "Rellekka Coast", 2673, 3710, 0);
        
        // Sand Crabs (Members)
        addDefaultNPC(1772, "Sand Crab", NPC.NPCType.MONSTER, 15, "Hosidius", "Hosidius Coast", 1778, 3517, 0);
        
        // Ammonite Crabs (Members)
        addDefaultNPC(1773, "Ammonite Crab", NPC.NPCType.MONSTER, 25, "Fossil Island", "Fossil Island Beach", 3715, 3882, 0);
        
        // Hill Giants
        addDefaultNPC(117, "Hill Giant", NPC.NPCType.MONSTER, 28, "Edgeville", "Hill Giant Dungeon", 3117, 9856, 0);
        
        // Moss Giants
        addDefaultNPC(112, "Moss giant", NPC.NPCType.MONSTER, 42, "Varrock", "Varrock Sewers", 3156, 9901, 0);
        
        // Dragons
        addDefaultNPC(941, "Green dragon", NPC.NPCType.MONSTER, 79, "Wilderness", "Green Dragon Isle", 2977, 3615, 0);
        addDefaultNPC(55, "Blue dragon", NPC.NPCType.MONSTER, 111, "Taverley", "Taverley Dungeon", 2892, 9799, 0);
        addDefaultNPC(53, "Red dragon", NPC.NPCType.MONSTER, 152, "Brimhaven", "Brimhaven Dungeon", 2701, 9564, 0);
        addDefaultNPC(54, "Black dragon", NPC.NPCType.MONSTER, 227, "Taverley", "Taverley Dungeon", 2861, 9825, 0);
        
        // Slayer monsters
        addDefaultSlayerNPC(1612, "Banshee", 23, "Slayer Tower", "Slayer Tower", 3440, 3543, 0, "Banshees", 15);
        addDefaultSlayerNPC(1613, "Crawling Hand", 8, "Slayer Tower", "Slayer Tower", 3421, 3550, 0, "Crawling Hands", 5);
        addDefaultSlayerNPC(1648, "Rock slug", 29, "Fremennik", "Rellekka Slayer Dungeon", 2792, 10014, 0, "Rock Slugs", 20);
        addDefaultSlayerNPC(1624, "Turoth", 83, "Fremennik", "Fremennik Slayer Dungeon", 2715, 10008, 0, "Turoths", 55);
        addDefaultSlayerNPC(1610, "Gargoyle", 111, "Slayer Tower", "Slayer Tower", 3440, 3535, 2, "Gargoyles", 75);
        addDefaultSlayerNPC(1615, "Abyssal demon", 124, "Slayer Tower", "Slayer Tower", 3415, 3564, 1, "Abyssal Demons", 85);
        
        // Bosses
        addDefaultBoss(2042, "King Black Dragon", 276, "Wilderness", "King Black Dragon Lair", 2271, 4680, 0);
        addDefaultBoss(2043, "TzTok-Jad", 702, "Karamja", "TzHaar Fight Cave", 2480, 5175, 0);
        
        logger.info(String.format("Loaded %d default NPCs with accurate locations", npcsById.size()));
    }
    
    private void addDefaultNPC(int id, String name, NPC.NPCType type, int combatLevel, String region, String location, int x, int y, int z) {
        NPC npc = new NPC(id, name, type);
        
        // Set combat stats based on combat level
        NPC.CombatStats stats = new NPC.CombatStats();
        int baseLevel = Math.max(1, combatLevel - 5);
        stats.setHitpoints(baseLevel + 5);
        stats.setAttack(baseLevel);
        stats.setStrength(baseLevel);
        stats.setDefence(baseLevel);
        stats.setRanged(baseLevel);
        stats.setMagic(baseLevel);
        npc.setCombatStats(stats);
        
        // Add location
        NPC.Location npcLocation = new NPC.Location(location, region, x, y, z);
        npc.addLocation(npcLocation);
        
        // Set basic properties
        npc.setAggressive(combatLevel > 10);
        npc.setMaxHit(Math.max(1, combatLevel / 10));
        npc.setRespawnTime(25); // 25 ticks = ~15 seconds
        
        npcsById.put(id, npc);
    }
    
    private void addDefaultSlayerNPC(int id, String name, int combatLevel, String region, String location, int x, int y, int z, String category, int slayerLevel) {
        addDefaultNPC(id, name, NPC.NPCType.SLAYER_MONSTER, combatLevel, region, location, x, y, z);
        
        NPC npc = npcsById.get(id);
        npc.setSlayerMonster(true);
        npc.setSlayerCategory(category);
        npc.setSlayerLevel(slayerLevel);
        npc.getSlayerMasters().add("Turael");
        npc.getSlayerMasters().add("Mazchna");
        if (slayerLevel >= 20) npc.getSlayerMasters().add("Vannaka");
        if (slayerLevel >= 40) npc.getSlayerMasters().add("Chaeldar");
        if (slayerLevel >= 70) npc.getSlayerMasters().add("Nieve");
        if (slayerLevel >= 85) npc.getSlayerMasters().add("Duradel");
    }
    
    private void addDefaultBoss(int id, String name, int combatLevel, String region, String location, int x, int y, int z) {
        addDefaultNPC(id, name, NPC.NPCType.BOSS, combatLevel, region, location, x, y, z);
        
        NPC npc = npcsById.get(id);
        npc.setAggressive(true);
        npc.setMaxHit(combatLevel / 5);
        npc.setRespawnTime(100); // Longer respawn for bosses
    }
    
    /**
     * Add an NPC with multiple spawn locations
     */
    private void addNPCWithMultipleLocations(int baseId, String name, NPC.NPCType type, int combatLevel, String[] locations) {
        for (int i = 0; i < locations.length; i++) {
            String[] parts = locations[i].split("\\|");
            if (parts.length == 5) {
                String locationName = parts[0];
                String region = parts[1];
                int x = Integer.parseInt(parts[2]);
                int y = Integer.parseInt(parts[3]);
                int z = Integer.parseInt(parts[4]);
                
                // Use unique ID for each location (baseId + location index)
                int uniqueId = baseId + (i * 10000);
                
                addDefaultNPC(uniqueId, name, type, combatLevel, region, locationName, x, y, z);
            }
        }
    }
    
    /**
     * Build search indices for efficient querying
     */
    private void buildIndices() {
        logger.info("Building NPC search indices...");
        
        // Clear existing indices
        npcsByName.clear();
        npcsByRegion.clear();
        npcsByLocation.clear();
        npcsByType.values().forEach(List::clear);
        npcsByCombatLevel.clear();
        npcsBySlayerCategory.clear();
        
        for (NPC npc : npcsById.values()) {
            // Index by name
            String nameLower = npc.getName().toLowerCase();
            npcsByName.computeIfAbsent(nameLower, k -> new ArrayList<>()).add(npc);
            
            // Index by type
            if (npc.getType() != null) {
                npcsByType.get(npc.getType()).add(npc);
            }
            
            // Index by combat level
            int combatLevel = npc.getCombatLevel();
            npcsByCombatLevel.computeIfAbsent(combatLevel, k -> new ArrayList<>()).add(npc);
            
            // Index by locations
            for (NPC.Location location : npc.getLocations()) {
                String regionLower = location.getRegion().toLowerCase();
                String locationLower = location.getName().toLowerCase();
                
                npcsByRegion.computeIfAbsent(regionLower, k -> new ArrayList<>()).add(npc);
                npcsByLocation.computeIfAbsent(locationLower, k -> new ArrayList<>()).add(npc);
            }
            
            // Index by slayer category
            if (npc.isSlayerMonster() && npc.getSlayerCategory() != null) {
                String categoryLower = npc.getSlayerCategory().toLowerCase();
                npcsBySlayerCategory.computeIfAbsent(categoryLower, k -> new ArrayList<>()).add(npc);
            }
        }
        
        logger.info("NPC search indices built successfully");
    }
    
    // Query methods
    
    /**
     * Get NPC by ID
     */
    public NPC getById(int npcId) {
        ensureInitialized();
        return npcsById.get(npcId);
    }
    
    /**
     * Get all NPCs
     */
    public List<NPC> getAll() {
        ensureInitialized();
        return new ArrayList<>(npcsById.values());
    }
    
    /**
     * Find NPCs by name (case-insensitive)
     */
    public List<NPC> findByName(String name) {
        ensureInitialized();
        return npcsByName.getOrDefault(name.toLowerCase(), new ArrayList<>());
    }
    
    /**
     * Get NPCs by region
     */
    public List<NPC> getByRegion(String region) {
        ensureInitialized();
        return new ArrayList<>(npcsByRegion.getOrDefault(region.toLowerCase(), new ArrayList<>()));
    }
    
    /**
     * Get NPCs by location
     */
    public List<NPC> getByLocation(String location) {
        ensureInitialized();
        return new ArrayList<>(npcsByLocation.getOrDefault(location.toLowerCase(), new ArrayList<>()));
    }
    
    /**
     * Get NPCs by type
     */
    public List<NPC> getByType(NPC.NPCType type) {
        ensureInitialized();
        return new ArrayList<>(npcsByType.getOrDefault(type, new ArrayList<>()));
    }
    
    /**
     * Get NPCs by combat level
     */
    public List<NPC> getByCombatLevel(int combatLevel) {
        ensureInitialized();
        return new ArrayList<>(npcsByCombatLevel.getOrDefault(combatLevel, new ArrayList<>()));
    }
    
    /**
     * Get NPCs within combat level range
     */
    public List<NPC> getByCombatLevelRange(int minLevel, int maxLevel) {
        ensureInitialized();
        return npcsById.values().stream()
                .filter(npc -> {
                    int level = npc.getCombatLevel();
                    return level >= minLevel && level <= maxLevel;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get slayer monsters by category
     */
    public List<NPC> getBySlayerCategory(String category) {
        ensureInitialized();
        return new ArrayList<>(npcsBySlayerCategory.getOrDefault(category.toLowerCase(), new ArrayList<>()));
    }
    
    /**
     * Get slayer monsters by required level
     */
    public List<NPC> getBySlayerLevel(int slayerLevel) {
        ensureInitialized();
        return npcsById.values().stream()
                .filter(NPC::isSlayerMonster)
                .filter(npc -> npc.getSlayerLevel() <= slayerLevel)
                .collect(Collectors.toList());
    }
    
    /**
     * Get aggressive NPCs
     */
    public List<NPC> getAggressiveNPCs() {
        ensureInitialized();
        return npcsById.values().stream()
                .filter(NPC::isAggressive)
                .collect(Collectors.toList());
    }
    
    /**
     * Get NPCs suitable for training at player's combat level
     */
    public List<NPC> getSuitableForTraining(int playerCombatLevel) {
        ensureInitialized();
        int minLevel = Math.max(1, playerCombatLevel - 10);
        int maxLevel = playerCombatLevel + 20;
        
        return getByCombatLevelRange(minLevel, maxLevel).stream()
                .filter(npc -> npc.getType() == NPC.NPCType.MONSTER || npc.getType() == NPC.NPCType.SLAYER_MONSTER)
                .collect(Collectors.toList());
    }
    
    /**
     * Search NPCs by partial name match
     */
    public List<NPC> searchByName(String partialName) {
        ensureInitialized();
        String searchTerm = partialName.toLowerCase();
        return npcsById.values().stream()
                .filter(npc -> npc.getName().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
    }
    
    /**
     * Get NPCs near coordinates
     */
    public List<NPC> getNearCoordinates(int x, int y, int z, int radius) {
        ensureInitialized();
        return npcsById.values().stream()
                .filter(npc -> npc.getLocations().stream()
                        .anyMatch(loc -> {
                            double distance = Math.sqrt(
                                Math.pow(loc.getX() - x, 2) + 
                                Math.pow(loc.getY() - y, 2)
                            );
                            return loc.getZ() == z && distance <= radius;
                        }))
                .collect(Collectors.toList());
    }
    
    /**
     * Get NPCs with specific weakness
     */
    public List<NPC> getByWeakness(String attackStyle) {
        ensureInitialized();
        return npcsById.values().stream()
                .filter(npc -> npc.hasWeakness(attackStyle))
                .collect(Collectors.toList());
    }
    
    /**
     * Get safe spottable NPCs
     */
    public List<NPC> getSafeSpottableNPCs() {
        ensureInitialized();
        return npcsById.values().stream()
                .filter(NPC::isSafeSpottable)
                .collect(Collectors.toList());
    }
    
    /**
     * Get NPCs by slayer master
     */
    public List<NPC> getBySlayerMaster(String masterName) {
        ensureInitialized();
        return npcsById.values().stream()
                .filter(NPC::isSlayerMonster)
                .filter(npc -> npc.getSlayerMasters().contains(masterName))
                .collect(Collectors.toList());
    }
    
    /**
     * Get NPC count
     */
    public int getCount() {
        ensureInitialized();
        return npcsById.size();
    }
    
    /**
     * Check if repository contains NPC with given ID
     */
    public boolean contains(int npcId) {
        ensureInitialized();
        return npcsById.containsKey(npcId);
    }
    
    /**
     * Add or update NPC
     */
    public void addOrUpdate(NPC npc) {
        if (npc == null || npc.getNpcId() <= 0) {
            throw new IllegalArgumentException("Invalid NPC data");
        }
        
        ensureInitialized();
        npcsById.put(npc.getNpcId(), npc);
        buildIndices(); // Rebuild indices after modification
        
        logger.info(String.format("Added/updated NPC: %s (ID: %d)", npc.getName(), npc.getNpcId()));
    }
    
    /**
     * Remove NPC by ID
     */
    public boolean remove(int npcId) {
        ensureInitialized();
        NPC removed = npcsById.remove(npcId);
        if (removed != null) {
            buildIndices(); // Rebuild indices after modification
            logger.info(String.format("Removed NPC: %s (ID: %d)", removed.getName(), npcId));
            return true;
        }
        return false;
    }
    
    /**
     * Clear all NPC data
     */
    public void clear() {
        npcsById.clear();
        npcsByName.clear();
        npcsByRegion.clear();
        npcsByLocation.clear();
        npcsByType.values().forEach(List::clear);
        npcsByCombatLevel.clear();
        npcsBySlayerCategory.clear();
        initialized = false;
        logger.info("NPC repository cleared");
    }
    
    /**
     * Reload NPC data
     */
    public void reload() {
        logger.info("Reloading NPC repository...");
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
        
        stats.put("totalNPCs", npcsById.size());
        stats.put("uniqueRegions", npcsByRegion.size());
        stats.put("uniqueLocations", npcsByLocation.size());
        stats.put("slayerMonsters", getByType(NPC.NPCType.SLAYER_MONSTER).size());
        stats.put("bosses", getByType(NPC.NPCType.BOSS).size());
        stats.put("aggressiveNPCs", getAggressiveNPCs().size());
        
        // Count by types
        Map<String, Integer> typeCounts = new HashMap<>();
        for (NPC.NPCType type : NPC.NPCType.values()) {
            typeCounts.put(type.name(), npcsByType.get(type).size());
        }
        stats.put("npcTypes", typeCounts);
        
        // Combat level distribution
        Map<String, Integer> levelRanges = new HashMap<>();
        levelRanges.put("1-10", getByCombatLevelRange(1, 10).size());
        levelRanges.put("11-50", getByCombatLevelRange(11, 50).size());
        levelRanges.put("51-100", getByCombatLevelRange(51, 100).size());
        levelRanges.put("101-200", getByCombatLevelRange(101, 200).size());
        levelRanges.put("200+", getByCombatLevelRange(201, 1000).size());
        stats.put("combatLevelRanges", levelRanges);
        
        return stats;
    }
}
package database.repositories;

import database.models.BankLocation;
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
 * Repository for managing bank location data with efficient querying
 */
public class BankLocationRepository {
    private static final Logger logger = Logger.getLogger(BankLocationRepository.class.getName());
    private static final String BANK_DATA_FILE = "data/bank_locations.json";
    
    private final Map<Integer, BankLocation> banksById;
    private final Map<String, List<BankLocation>> banksByName;
    private final Map<String, List<BankLocation>> banksByRegion;
    private final Map<String, List<BankLocation>> banksByCity;
    private final Map<BankLocation.BankType, List<BankLocation>> banksByType;
    private final Map<String, List<BankLocation>> banksByService;
    private final DataLoader dataLoader;
    private final Gson gson;
    
    private boolean initialized = false;
    
    public BankLocationRepository() {
        this.banksById = new ConcurrentHashMap<>();
        this.banksByName = new ConcurrentHashMap<>();
        this.banksByRegion = new ConcurrentHashMap<>();
        this.banksByCity = new ConcurrentHashMap<>();
        this.banksByType = new ConcurrentHashMap<>();
        this.banksByService = new ConcurrentHashMap<>();
        this.dataLoader = new DataLoader();
        this.gson = new Gson();
        
        // Initialize type maps
        for (BankLocation.BankType type : BankLocation.BankType.values()) {
            banksByType.put(type, new ArrayList<>());
        }
    }
    
    /**
     * Initialize the repository by loading bank location data
     */
    public void initialize() {
        if (initialized) {
            logger.info("Bank location repository already initialized");
            return;
        }
        
        try {
            logger.info("Initializing bank location repository...");
            loadBankData();
            buildIndices();
            initialized = true;
            logger.info(String.format("Bank location repository initialized with %d banks", banksById.size()));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize bank location repository", e);
            throw new RuntimeException("Bank location repository initialization failed", e);
        }
    }
    
    /**
     * Load bank location data from JSON file
     */
    private void loadBankData() {
        // Load hardcoded bank data since JSON resources aren't being packaged properly
        loadDefaultBanks();
    }
    
    /**
     * Load default bank location data if file loading fails
     */
    private void loadDefaultBanks() {
        logger.info("Loading default bank location data...");
        
        // Major city banks
        addDefaultBank(1, "Lumbridge Bank", "Lumbridge", "Misthalin", BankLocation.BankType.BANK_BOOTH, 3208, 3220, 2, true, true, false, false);
        addDefaultBank(2, "Varrock West Bank", "Varrock", "Misthalin", BankLocation.BankType.BANK_BOOTH, 3185, 3441, 0, true, true, false, false);
        addDefaultBank(3, "Varrock East Bank", "Varrock", "Misthalin", BankLocation.BankType.BANK_BOOTH, 3253, 3420, 0, true, true, false, false);
        addDefaultBank(4, "Falador West Bank", "Falador", "Asgarnia", BankLocation.BankType.BANK_BOOTH, 2946, 3368, 0, true, true, false, false);
        addDefaultBank(5, "Falador East Bank", "Falador", "Asgarnia", BankLocation.BankType.BANK_BOOTH, 3013, 3355, 0, true, true, false, false);
        addDefaultBank(6, "Draynor Bank", "Draynor Village", "Misthalin", BankLocation.BankType.BANK_BOOTH, 3092, 3243, 0, true, true, false, false);
        addDefaultBank(7, "Al Kharid Bank", "Al Kharid", "Kharidian Desert", BankLocation.BankType.BANK_BOOTH, 3269, 3167, 0, true, true, false, false);
        addDefaultBank(8, "Edgeville Bank", "Edgeville", "Misthalin", BankLocation.BankType.BANK_BOOTH, 3094, 3491, 0, true, true, false, false);
        addDefaultBank(9, "Barbarian Village Bank", "Barbarian Village", "Misthalin", BankLocation.BankType.BANK_BOOTH, 3013, 3355, 0, true, true, false, false);
        
        // Camelot and surrounding areas
        addDefaultBank(10, "Camelot Bank", "Camelot", "Kandarin", BankLocation.BankType.BANK_BOOTH, 2725, 3493, 0, true, true, false, false);
        addDefaultBank(11, "Catherby Bank", "Catherby", "Kandarin", BankLocation.BankType.BANK_BOOTH, 2808, 3441, 0, true, true, false, false);
        addDefaultBank(12, "Seers' Village Bank", "Seers' Village", "Kandarin", BankLocation.BankType.BANK_BOOTH, 2722, 3493, 0, true, true, false, false);
        
        // Ardougne
        addDefaultBank(13, "Ardougne North Bank", "East Ardougne", "Kandarin", BankLocation.BankType.BANK_BOOTH, 2615, 3332, 0, true, true, false, false);
        addDefaultBank(14, "Ardougne South Bank", "East Ardougne", "Kandarin", BankLocation.BankType.BANK_BOOTH, 2655, 3283, 0, true, true, false, false);
        
        // Yanille
        addDefaultBank(15, "Yanille Bank", "Yanille", "Kandarin", BankLocation.BankType.BANK_BOOTH, 2612, 3094, 0, true, true, false, false);
        
        // Port Sarim
        addDefaultBank(16, "Port Sarim Deposit Box", "Port Sarim", "Asgarnia", BankLocation.BankType.DEPOSIT_BOX, 3045, 3234, 0, true, true, false, false);
        
        // Gnome Stronghold
        addDefaultBank(17, "Gnome Stronghold Bank", "Tree Gnome Stronghold", "Kandarin", BankLocation.BankType.BANK_BOOTH, 2445, 3424, 1, true, true, false, false);
        
        // Grand Exchange
        addDefaultBank(18, "Grand Exchange Bank", "Grand Exchange", "Misthalin", BankLocation.BankType.GRAND_EXCHANGE, 3164, 3487, 0, true, true, false, true);
        
        // Canifis
        addDefaultBank(19, "Canifis Bank", "Canifis", "Morytania", BankLocation.BankType.BANK_BOOTH, 3512, 3480, 0, true, true, false, false);
        
        // Burgh de Rott
        addDefaultBank(20, "Burgh de Rott Bank", "Burgh de Rott", "Morytania", BankLocation.BankType.BANK_BOOTH, 3496, 3211, 0, true, true, false, false);
        
        // Shilo Village
        addDefaultBank(21, "Shilo Village Bank", "Shilo Village", "Karamja", BankLocation.BankType.BANK_BOOTH, 2852, 2954, 0, true, true, false, false);
        
        // Brimhaven
        addDefaultBank(22, "Brimhaven Deposit Box", "Brimhaven", "Karamja", BankLocation.BankType.DEPOSIT_BOX, 2808, 3143, 0, true, true, false, false);
        
        // Fishing Guild
        addDefaultBank(23, "Fishing Guild Bank", "Fishing Guild", "Kandarin", BankLocation.BankType.BANK_BOOTH, 2586, 3420, 0, true, true, false, false);
        
        // Cooking Guild
        addDefaultBank(24, "Cooking Guild Bank", "Cooking Guild", "Asgarnia", BankLocation.BankType.BANK_BOOTH, 3143, 3443, 0, true, true, false, false);
        
        // Crafting Guild
        addDefaultBank(25, "Crafting Guild Bank", "Crafting Guild", "Asgarnia", BankLocation.BankType.BANK_BOOTH, 2933, 3282, 0, true, true, false, false);
        
        // Mining Guild
        addDefaultBank(26, "Mining Guild Bank", "Mining Guild", "Asgarnia", BankLocation.BankType.BANK_BOOTH, 3012, 3355, 0, true, true, false, false);
        
        // Warriors' Guild
        addDefaultBank(27, "Warriors' Guild Bank", "Warriors' Guild", "Asgarnia", BankLocation.BankType.BANK_BOOTH, 2843, 3543, 0, true, true, false, false);
        
        // Duel Arena
        addDefaultBank(28, "Duel Arena Bank", "Duel Arena", "Kharidian Desert", BankLocation.BankType.BANK_BOOTH, 3381, 3268, 0, true, true, false, false);
        
        // Castle Wars
        addDefaultBank(29, "Castle Wars Bank", "Castle Wars", "Kandarin", BankLocation.BankType.CASTLE_WARS, 2442, 3083, 0, true, true, false, false);
        
        // Pest Control
        addDefaultBank(30, "Pest Control Bank", "Pest Control", "Void Knights' Outpost", BankLocation.BankType.BANK_BOOTH, 2667, 2653, 0, true, true, false, false);
        
        // Barbarian Outpost
        addDefaultBank(31, "Barbarian Outpost Bank", "Barbarian Outpost", "Kandarin", BankLocation.BankType.BARBARIAN_OUTPOST, 2536, 3573, 0, true, true, false, false);
        
        // TzHaar
        addDefaultBank(32, "TzHaar Bank", "TzHaar City", "Karamja", BankLocation.BankType.BANK_BOOTH, 2446, 5178, 0, true, true, false, false);
        
        // Neitiznot
        addDefaultBank(33, "Neitiznot Bank", "Neitiznot", "Fremennik Province", BankLocation.BankType.BANK_BOOTH, 2337, 3807, 0, true, true, false, false);
        
        // Jatizso
        addDefaultBank(34, "Jatizso Bank", "Jatizso", "Fremennik Province", BankLocation.BankType.BANK_BOOTH, 2416, 3801, 0, true, true, false, false);
        
        // Rellekka
        addDefaultBank(35, "Rellekka Bank", "Rellekka", "Fremennik Province", BankLocation.BankType.BANK_BOOTH, 2648, 3644, 0, true, true, false, false);
        
        // Etceteria
        addDefaultBank(36, "Etceteria Bank", "Etceteria", "Fremennik Province", BankLocation.BankType.BANK_BOOTH, 2618, 3895, 0, true, true, false, false);
        
        // Miscellania
        addDefaultBank(37, "Miscellania Bank", "Miscellania", "Fremennik Province", BankLocation.BankType.BANK_BOOTH, 2618, 3895, 0, true, true, false, false);
        
        // Lunar Isle
        addDefaultBank(38, "Lunar Isle Bank", "Lunar Isle", "Fremennik Province", BankLocation.BankType.BANK_BOOTH, 2099, 3919, 0, true, true, false, false);
        
        // Waterbirth Island
        addDefaultBank(39, "Waterbirth Island Bank", "Waterbirth Island", "Fremennik Province", BankLocation.BankType.BANK_BOOTH, 2544, 3740, 0, true, true, false, false);
        
        // Keldagrim
        addDefaultBank(40, "Keldagrim Bank", "Keldagrim", "Fremennik Province", BankLocation.BankType.BANK_BOOTH, 2827, 10207, 0, true, true, false, false);
        
        // Lletya
        addDefaultBank(41, "Lletya Bank", "Lletya", "Tirannwn", BankLocation.BankType.BANK_BOOTH, 2352, 3163, 0, true, true, false, false);
        
        // Prifddinas
        addDefaultBank(42, "Prifddinas Bank", "Prifddinas", "Tirannwn", BankLocation.BankType.BANK_BOOTH, 2283, 3139, 0, true, true, false, false);
        
        // Sophanem
        addDefaultBank(43, "Sophanem Bank", "Sophanem", "Kharidian Desert", BankLocation.BankType.BANK_BOOTH, 3288, 2812, 0, true, true, false, false);
        
        // Nardah
        addDefaultBank(44, "Nardah Bank", "Nardah", "Kharidian Desert", BankLocation.BankType.BANK_BOOTH, 3428, 2892, 0, true, true, false, false);
        
        // Pollnivneach
        addDefaultBank(45, "Pollnivneach Bank", "Pollnivneach", "Kharidian Desert", BankLocation.BankType.BANK_BOOTH, 3359, 2974, 0, true, true, false, false);
        
        // Menaphos
        addDefaultBank(46, "Menaphos Bank", "Menaphos", "Kharidian Desert", BankLocation.BankType.BANK_BOOTH, 3217, 2793, 0, true, true, false, false);
        
        // Zanaris
        addDefaultBank(47, "Zanaris Bank", "Zanaris", "Lost City", BankLocation.BankType.BANK_BOOTH, 2383, 4458, 0, true, true, false, false);
        
        // Motherlode Mine
        addDefaultBank(48, "Motherlode Mine Bank", "Motherlode Mine", "Asgarnia", BankLocation.BankType.BANK_BOOTH, 3760, 5666, 0, true, true, false, false);
        
        // Blast Furnace
        addDefaultBank(49, "Blast Furnace Bank", "Blast Furnace", "Fremennik Province", BankLocation.BankType.BANK_BOOTH, 1948, 4957, 0, true, true, false, false);
        
        // Wintertodt
        addDefaultBank(50, "Wintertodt Bank", "Wintertodt Camp", "Great Kourend", BankLocation.BankType.BANK_BOOTH, 1640, 3944, 0, true, true, false, false);
        
        addDefaultBank(51, "Shayzien Bank", "Shayzien", "Great Kourend", BankLocation.BankType.BANK_BOOTH, 1504, 3615, 0, true, true, false, false);
        addDefaultBank(52, "Hosidius Bank", "Hosidius", "Great Kourend", BankLocation.BankType.BANK_BOOTH, 1676, 3615, 0, true, true, false, false);
        addDefaultBank(53, "Lovakengj Bank", "Lovakengj", "Great Kourend", BankLocation.BankType.BANK_BOOTH, 1526, 3739, 0, true, true, false, false);
        addDefaultBank(54, "Piscarilius Bank", "Piscarilius", "Great Kourend", BankLocation.BankType.BANK_BOOTH, 1803, 3790, 0, true, true, false, false);
        addDefaultBank(55, "Arceuus Bank", "Arceuus", "Great Kourend", BankLocation.BankType.BANK_BOOTH, 1633, 3745, 0, true, true, false, false);
        
        // Fossil Island
        addDefaultBank(56, "Fossil Island Bank", "Fossil Island", "Fossil Island", BankLocation.BankType.BANK_BOOTH, 3739, 3804, 0, true, true, false, false);
        
        // Myths' Guild
        addDefaultBank(57, "Myths' Guild Bank", "Myths' Guild", "Feldip Hills", BankLocation.BankType.BANK_BOOTH, 2456, 2847, 0, true, true, false, false);
        
        // Woodcutting Guild
        addDefaultBank(58, "Woodcutting Guild Bank", "Woodcutting Guild", "Great Kourend", BankLocation.BankType.BANK_BOOTH, 1591, 3479, 0, true, true, false, false);
        
        // Farming Guild
        addDefaultBank(59, "Farming Guild Bank", "Farming Guild", "Great Kourend", BankLocation.BankType.BANK_BOOTH, 1249, 3718, 0, true, true, false, false);
        
        // Chambers of Xeric
        addDefaultBank(60, "Chambers of Xeric Bank", "Chambers of Xeric", "Great Kourend", BankLocation.BankType.BANK_BOOTH, 1640, 3943, 0, true, true, false, false);
        
        logger.info(String.format("Loaded %d default bank locations", banksById.size()));
    }
    
    private void addDefaultBank(int id, String name, String city, String region, BankLocation.BankType type, 
                               int x, int y, int z, boolean hasWithdraw, boolean hasDeposit, 
                               boolean depositOnly, boolean hasGrandExchange) {
        BankLocation bank = new BankLocation(id, name, city, region, x, y, z);
        bank.setType(type);
        bank.setAccessible(true);
        
        // Set services
        List<String> services = new ArrayList<>();
        if (hasWithdraw) services.add("withdraw");
        if (hasDeposit) services.add("deposit");
        if (depositOnly) services.add("deposit_only");
        if (hasGrandExchange) services.add("grand_exchange");
        
        bank.setServices(services);
        
        // Set requirements based on location
        Map<String, Integer> requirements = new HashMap<>();
        
        // Add quest/skill requirements for specific banks
        switch (name) {
            case "Shilo Village Bank":
                requirements.put("quest_shilo_village", 1);
                break;
            case "Lunar Isle Bank":
                requirements.put("quest_lunar_diplomacy", 1);
                break;
            case "Lletya Bank":
                requirements.put("quest_mournings_end_part_i", 1);
                break;
            case "Ape Atoll Deposit Box":
                requirements.put("quest_monkey_madness", 1);
                break;
            case "Sophanem Bank":
                requirements.put("quest_icthlarin_little_helper", 1);
                break;
            case "Zanaris Bank":
                requirements.put("quest_lost_city", 1);
                break;
            case "Fishing Guild Bank":
                requirements.put("fishing", 68);
                break;
            case "Cooking Guild Bank":
                requirements.put("cooking", 32);
                break;
            case "Crafting Guild Bank":
                requirements.put("crafting", 40);
                break;
            case "Mining Guild Bank":
                requirements.put("mining", 60);
                break;
            case "Warriors' Guild Bank":
                requirements.put("attack", 99); // Combined attack + strength = 130
                requirements.put("strength", 99);
                break;
            case "Myths' Guild Bank":
                requirements.put("quest_dragon_slayer_ii", 1);
                break;
            case "Woodcutting Guild Bank":
                requirements.put("woodcutting", 60);
                break;
            case "Farming Guild Bank":
                requirements.put("farming", 45);
                break;
        }
        
        bank.setRequirements(requirements);
        
        // Set walking time estimates (in seconds)
        Map<String, Integer> walkingTimes = new HashMap<>();
        
        // Add walking times to major teleport locations
        if (city.equals("Lumbridge")) {
            walkingTimes.put("lumbridge_teleport", 30);
            walkingTimes.put("home_teleport", 30);
        } else if (city.equals("Varrock")) {
            walkingTimes.put("varrock_teleport", 45);
        } else if (city.equals("Falador")) {
            walkingTimes.put("falador_teleport", 60);
        } else if (city.equals("Camelot")) {
            walkingTimes.put("camelot_teleport", 20);
        } else if (city.equals("East Ardougne")) {
            walkingTimes.put("ardougne_teleport", 45);
        }
        
        bank.setWalkingTimes(walkingTimes);
        
        banksById.put(id, bank);
    }
    
    /**
     * Build search indices for efficient querying
     */
    private void buildIndices() {
        logger.info("Building bank location search indices...");
        
        // Clear existing indices
        banksByName.clear();
        banksByRegion.clear();
        banksByCity.clear();
        banksByType.values().forEach(List::clear);
        banksByService.clear();
        
        for (BankLocation bank : banksById.values()) {
            // Index by name
            String nameLower = bank.getName().toLowerCase();
            banksByName.computeIfAbsent(nameLower, k -> new ArrayList<>()).add(bank);
            
            // Index by region
            String regionLower = bank.getRegion().toLowerCase();
            banksByRegion.computeIfAbsent(regionLower, k -> new ArrayList<>()).add(bank);
            
            // Index by city
            String cityLower = bank.getCity().toLowerCase();
            banksByCity.computeIfAbsent(cityLower, k -> new ArrayList<>()).add(bank);
            
            // Index by type
            if (bank.getType() != null) {
                banksByType.get(bank.getType()).add(bank);
            }
            
            // Index by services
            for (String service : bank.getServices()) {
                String serviceLower = service.toLowerCase();
                banksByService.computeIfAbsent(serviceLower, k -> new ArrayList<>()).add(bank);
            }
        }
        
        logger.info("Bank location search indices built successfully");
    }
    
    // Query methods
    
    /**
     * Get bank by ID
     */
    public BankLocation getById(int bankId) {
        ensureInitialized();
        return banksById.get(bankId);
    }
    
    /**
     * Get all banks
     */
    public List<BankLocation> getAll() {
        ensureInitialized();
        return new ArrayList<>(banksById.values());
    }
    
    /**
     * Find banks by name (case-insensitive)
     */
    public List<BankLocation> findByName(String name) {
        ensureInitialized();
        return banksByName.getOrDefault(name.toLowerCase(), new ArrayList<>());
    }
    
    /**
     * Get banks by region
     */
    public List<BankLocation> getByRegion(String region) {
        ensureInitialized();
        return new ArrayList<>(banksByRegion.getOrDefault(region.toLowerCase(), new ArrayList<>()));
    }
    
    /**
     * Get banks by city
     */
    public List<BankLocation> getByCity(String city) {
        ensureInitialized();
        return new ArrayList<>(banksByCity.getOrDefault(city.toLowerCase(), new ArrayList<>()));
    }
    
    /**
     * Get banks by type
     */
    public List<BankLocation> getByType(BankLocation.BankType type) {
        ensureInitialized();
        return new ArrayList<>(banksByType.getOrDefault(type, new ArrayList<>()));
    }
    
    /**
     * Get banks with specific service
     */
    public List<BankLocation> getByService(String service) {
        ensureInitialized();
        return new ArrayList<>(banksByService.getOrDefault(service.toLowerCase(), new ArrayList<>()));
    }
    
    /**
     * Get accessible banks only
     */
    public List<BankLocation> getAccessible() {
        ensureInitialized();
        return banksById.values().stream()
                .filter(BankLocation::isAccessible)
                .collect(Collectors.toList());
    }
    
    /**
     * Get banks near coordinates
     */
    public List<BankLocation> getNearCoordinates(int x, int y, int z, int radius) {
        ensureInitialized();
        return banksById.values().stream()
                .filter(bank -> {
                    double distance = Math.sqrt(
                        Math.pow(bank.getX() - x, 2) + 
                        Math.pow(bank.getY() - y, 2)
                    );
                    return bank.getZ() == z && distance <= radius;
                })
                .sorted((a, b) -> {
                    double distA = Math.sqrt(Math.pow(a.getX() - x, 2) + Math.pow(a.getY() - y, 2));
                    double distB = Math.sqrt(Math.pow(b.getX() - x, 2) + Math.pow(b.getY() - y, 2));
                    return Double.compare(distA, distB);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get closest bank to coordinates
     */
    public BankLocation getClosest(int x, int y, int z) {
        ensureInitialized();
        return banksById.values().stream()
                .filter(bank -> bank.getZ() == z && bank.isAccessible())
                .min((a, b) -> {
                    double distA = Math.sqrt(Math.pow(a.getX() - x, 2) + Math.pow(a.getY() - y, 2));
                    double distB = Math.sqrt(Math.pow(b.getX() - x, 2) + Math.pow(b.getY() - y, 2));
                    return Double.compare(distA, distB);
                })
                .orElse(null);
    }
    
    /**
     * Get banks usable with player's requirements
     */
    public List<BankLocation> getUsableByPlayer(Map<String, Integer> playerStats, Set<String> completedQuests) {
        ensureInitialized();
        return banksById.values().stream()
                .filter(bank -> {
                    if (!bank.isAccessible()) return false;
                    
                    // Check skill requirements
                    for (Map.Entry<String, Integer> req : bank.getRequirements().entrySet()) {
                        String skill = req.getKey();
                        int requiredLevel = req.getValue();
                        
                        if (skill.startsWith("quest_")) {
                            // Quest requirement
                            if (!completedQuests.contains(skill)) {
                                return false;
                            }
                        } else {
                            // Skill requirement
                            Integer playerLevel = playerStats.get(skill);
                            if (playerLevel == null || playerLevel < requiredLevel) {
                                return false;
                            }
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get banks with withdraw service
     */
    public List<BankLocation> getWithWithdraw() {
        return getByService("withdraw");
    }
    
    /**
     * Get banks with deposit service
     */
    public List<BankLocation> getWithDeposit() {
        return getByService("deposit");
    }
    
    /**
     * Get deposit boxes only
     */
    public List<BankLocation> getDepositBoxes() {
        return getByType(BankLocation.BankType.DEPOSIT_BOX);
    }
    
    /**
     * Get banks with Grand Exchange access
     */
    public List<BankLocation> getWithGrandExchange() {
        return getByService("grand_exchange");
    }
    
    /**
     * Search banks by partial name match
     */
    public List<BankLocation> searchByName(String partialName) {
        ensureInitialized();
        String searchTerm = partialName.toLowerCase();
        return banksById.values().stream()
                .filter(bank -> bank.getName().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
    }
    
    /**
     * Get banks with no requirements
     */
    public List<BankLocation> getWithoutRequirements() {
        ensureInitialized();
        return banksById.values().stream()
                .filter(bank -> bank.getRequirements().isEmpty())
                .collect(Collectors.toList());
    }
    
    /**
     * Get bank count
     */
    public int getCount() {
        ensureInitialized();
        return banksById.size();
    }
    
    /**
     * Check if repository contains bank with given ID
     */
    public boolean contains(int bankId) {
        ensureInitialized();
        return banksById.containsKey(bankId);
    }
    
    /**
     * Add or update bank
     */
    public void addOrUpdate(BankLocation bank) {
        if (bank == null || bank.getId() <= 0) {
            throw new IllegalArgumentException("Invalid bank data");
        }
        
        ensureInitialized();
        banksById.put(bank.getId(), bank);
        buildIndices(); // Rebuild indices after modification
        
        logger.info(String.format("Added/updated bank: %s (ID: %d)", bank.getName(), bank.getId()));
    }
    
    /**
     * Remove bank by ID
     */
    public boolean remove(int bankId) {
        ensureInitialized();
        BankLocation removed = banksById.remove(bankId);
        if (removed != null) {
            buildIndices(); // Rebuild indices after modification
            logger.info(String.format("Removed bank: %s (ID: %d)", removed.getName(), bankId));
            return true;
        }
        return false;
    }
    
    /**
     * Clear all bank data
     */
    public void clear() {
        banksById.clear();
        banksByName.clear();
        banksByRegion.clear();
        banksByCity.clear();
        banksByType.values().forEach(List::clear);
        banksByService.clear();
        initialized = false;
        logger.info("Bank location repository cleared");
    }
    
    /**
     * Reload bank data
     */
    public void reload() {
        logger.info("Reloading bank location repository...");
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
        
        stats.put("totalBanks", banksById.size());
        stats.put("uniqueRegions", banksByRegion.size());
        stats.put("uniqueCities", banksByCity.size());
        stats.put("accessibleBanks", getAccessible().size());
        stats.put("banksWithRequirements", banksById.values().stream()
                .mapToInt(bank -> bank.getRequirements().isEmpty() ? 0 : 1).sum());
        
        // Count by types
        Map<String, Integer> typeCounts = new HashMap<>();
        for (BankLocation.BankType type : BankLocation.BankType.values()) {
            typeCounts.put(type.name(), banksByType.get(type).size());
        }
        stats.put("bankTypes", typeCounts);
        
        // Count by services
        Map<String, Integer> serviceCounts = new HashMap<>();
        serviceCounts.put("withdraw", getWithWithdraw().size());
        serviceCounts.put("deposit", getWithDeposit().size());
        serviceCounts.put("grandExchange", getWithGrandExchange().size());
        stats.put("services", serviceCounts);
        
        // Regional distribution
        Map<String, Integer> regionCounts = new HashMap<>();
        for (String region : banksByRegion.keySet()) {
            regionCounts.put(region, banksByRegion.get(region).size());
        }
        stats.put("regionDistribution", regionCounts);
        
        return stats;
    }
}
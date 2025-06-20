package database.repositories;

import database.models.Consumable;
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
 * Repository for managing consumable items (food, potions) with efficient querying
 */
public class ConsumableRepository {
    private static final Logger logger = Logger.getLogger(ConsumableRepository.class.getName());
    private static final String CONSUMABLE_DATA_FILE = "data/consumables.json";
    
    private final Map<Integer, Consumable> consumablesById;
    private final Map<String, List<Consumable>> consumablesByName;
    private final Map<Consumable.ConsumableType, List<Consumable>> consumablesByType;
    private final Map<Integer, List<Consumable>> consumablesByHealAmount;
    private final Map<String, List<Consumable>> consumablesByEffect;
    private final Map<String, List<Consumable>> consumablesByStatBoost;
    private final DataLoader dataLoader;
    private final Gson gson;
    
    private boolean initialized = false;
    
    public ConsumableRepository() {
        this.consumablesById = new ConcurrentHashMap<>();
        this.consumablesByName = new ConcurrentHashMap<>();
        this.consumablesByType = new ConcurrentHashMap<>();
        this.consumablesByHealAmount = new ConcurrentHashMap<>();
        this.consumablesByEffect = new ConcurrentHashMap<>();
        this.consumablesByStatBoost = new ConcurrentHashMap<>();
        this.dataLoader = new DataLoader();
        this.gson = new Gson();
        
        // Initialize type maps
        for (Consumable.ConsumableType type : Consumable.ConsumableType.values()) {
            consumablesByType.put(type, new ArrayList<>());
        }
    }
    
    /**
     * Initialize the repository by loading consumable data
     */
    public void initialize() {
        if (initialized) {
            logger.info("Consumable repository already initialized");
            return;
        }
        
        try {
            logger.info("Initializing consumable repository...");
            loadConsumableData();
            buildIndices();
            initialized = true;
            logger.info(String.format("Consumable repository initialized with %d items", consumablesById.size()));
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to initialize consumable repository", e);
            throw new RuntimeException("Consumable repository initialization failed", e);
        }
    }
    
    /**
     * Load consumable data from JSON file
     */
    private void loadConsumableData() {
        try {
            String jsonData = dataLoader.loadJsonData(CONSUMABLE_DATA_FILE);
            Type listType = new TypeToken<List<Consumable>>(){}.getType();
            List<Consumable> consumableList = gson.fromJson(jsonData, listType);
            
            if (consumableList != null) {
                for (Consumable consumable : consumableList) {
                    if (consumable != null && consumable.getItemId() > 0) {
                        consumablesById.put(consumable.getItemId(), consumable);
                    }
                }
                logger.info(String.format("Loaded %d consumables from %s", consumableList.size(), CONSUMABLE_DATA_FILE));
            } else {
                logger.warning("No consumable data found in " + CONSUMABLE_DATA_FILE);
                loadDefaultConsumables();
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to load consumable data from file, using defaults", e);
            loadDefaultConsumables();
        }
    }
    
    /**
     * Load default consumable data if file loading fails
     */
    private void loadDefaultConsumables() {
        logger.info("Loading default consumable data...");
        
        // Basic food items
        addDefaultFood(315, "Shrimps", 3, 1, 5, false);
        addDefaultFood(319, "Anchovies", 1, 1, 5, false);
        addDefaultFood(317, "Sardine", 4, 1, 5, false);
        addDefaultFood(321, "Herring", 5, 5, 10, false);
        addDefaultFood(325, "Mackerel", 6, 10, 15, false);
        addDefaultFood(327, "Trout", 7, 15, 20, false);
        addDefaultFood(329, "Cod", 7, 18, 25, false);
        addDefaultFood(331, "Pike", 8, 25, 30, false);
        addDefaultFood(333, "Salmon", 9, 25, 35, false);
        addDefaultFood(335, "Tuna", 10, 30, 40, false);
        addDefaultFood(347, "Swordfish", 14, 35, 100, false);
        addDefaultFood(349, "Lobster", 12, 40, 220, false);
        addDefaultFood(361, "Monkfish", 16, 62, 358, false);
        addDefaultFood(373, "Shark", 20, 76, 1000, false);
        addDefaultFood(385, "Dark crab", 22, 85, 1500, false);
        
        // Bread and baked goods
        addDefaultFood(2309, "Bread", 5, 1, 12, false);
        addDefaultFood(1891, "Cake", 4, 1, 50, false); // 3 bites
        addDefaultFood(1897, "Chocolate cake", 5, 1, 60, false); // 3 bites
        addDefaultFood(7223, "Pineapple pizza", 11, 65, 1100, false); // 2 slices
        
        // Meat
        addDefaultFood(2142, "Cooked chicken", 3, 1, 5, false);
        addDefaultFood(2146, "Cooked meat", 3, 1, 5, false);
        addDefaultFood(9980, "Cooked rabbit", 5, 1, 10, false);
        
        // Pies
        addDefaultFood(2327, "Meat pie", 6, 20, 58, false); // 2 bites
        addDefaultFood(2331, "Apple pie", 7, 30, 78, false); // 2 bites
        addDefaultFood(7178, "Garden pie", 6, 34, 84, false); // 2 bites
        addDefaultFood(7188, "Fish pie", 6, 47, 164, false); // 2 bites
        addDefaultFood(7198, "Admiral pie", 8, 70, 500, false); // 2 bites
        addDefaultFood(7208, "Wild pie", 11, 85, 1200, false); // 2 bites
        
        // Stews and soups
        addDefaultFood(2003, "Stew", 11, 34, 117, false);
        addDefaultFood(7566, "Curry", 19, 60, 600, false);
        
        // Special foods
        addDefaultFood(6297, "Karambwan", 18, 65, 1200, false); // Can combo eat
        addDefaultFood(6701, "Tuna potato", 22, 68, 1500, false);
        addDefaultFood(7946, "Manta ray", 22, 81, 1600, false);
        addDefaultFood(15272, "Anglerfish", 22, 82, 1000, true); // Can boost above max HP
        
        // Combat potions
        addDefaultPotion(2428, "Attack potion", Consumable.PotionEffect.ATTACK_BOOST, 3, 1, 37);
        addDefaultPotion(113, "Strength potion", Consumable.PotionEffect.STRENGTH_BOOST, 3, 1, 37);
        addDefaultPotion(2442, "Defence potion", Consumable.PotionEffect.DEFENCE_BOOST, 3, 1, 37);
        addDefaultPotion(2436, "Super attack", Consumable.PotionEffect.ATTACK_BOOST, 5, 45, 188);
        addDefaultPotion(2440, "Super strength", Consumable.PotionEffect.STRENGTH_BOOST, 5, 55, 313);
        addDefaultPotion(2444, "Super defence", Consumable.PotionEffect.DEFENCE_BOOST, 5, 66, 469);
        addDefaultPotion(2432, "Combat potion", Consumable.PotionEffect.COMBAT_BOOST, 4, 48, 225);
        addDefaultPotion(12695, "Super combat potion", Consumable.PotionEffect.SUPER_COMBAT, 4, 90, 5000);
        
        // Ranging potions
        addDefaultPotion(2444, "Ranging potion", Consumable.PotionEffect.RANGED_BOOST, 4, 43, 163);
        addDefaultPotion(2444, "Super ranging", Consumable.PotionEffect.RANGED_BOOST, 5, 72, 938);
        
        // Magic potions
        addDefaultPotion(3040, "Magic potion", Consumable.PotionEffect.MAGIC_BOOST, 4, 76, 1000);
        
        // Prayer potions
        addDefaultPotion(2434, "Prayer potion", Consumable.PotionEffect.PRAYER_RESTORE, 7, 38, 100);
        addDefaultPotion(10925, "Sanfew serum", Consumable.PotionEffect.PRAYER_RESTORE, 4, 65, 1500);
        addDefaultPotion(6685, "Saradomin brew", Consumable.PotionEffect.SARADOMIN_BREW, 15, 81, 1000);
        
        // Restore potions
        addDefaultPotion(2430, "Restore potion", Consumable.PotionEffect.STAT_RESTORE, 10, 22, 62);
        addDefaultPotion(3024, "Super restore", Consumable.PotionEffect.STAT_RESTORE, 8, 63, 500);
        
        // Energy and run potions
        addDefaultPotion(3008, "Energy potion", Consumable.PotionEffect.ENERGY_RESTORE, 10, 26, 72);
        addDefaultPotion(3010, "Super energy", Consumable.PotionEffect.ENERGY_RESTORE, 20, 52, 281);
        addDefaultPotion(3012, "Stamina potion", Consumable.PotionEffect.STAMINA_BOOST, 20, 77, 2000);
        
        // Antipoison
        addDefaultPotion(2446, "Antipoison", Consumable.PotionEffect.ANTIPOISON, 0, 37, 100);
        addDefaultPotion(2448, "Super antipoison", Consumable.PotionEffect.ANTIPOISON, 0, 48, 200);
        addDefaultPotion(2452, "Antidote+", Consumable.PotionEffect.ANTIPOISON, 0, 68, 1000);
        addDefaultPotion(2454, "Antidote++", Consumable.PotionEffect.ANTIPOISON, 0, 79, 1500);
        
        // Special potions
        addDefaultPotion(6687, "Zamorak brew", Consumable.PotionEffect.ZAMORAK_BREW, 12, 78, 1000);
        addDefaultPotion(12140, "Overload", Consumable.PotionEffect.OVERLOAD, 50, 96, 10000);
        addDefaultPotion(12625, "Imbued heart", Consumable.PotionEffect.MAGIC_BOOST, 1, 75, 5000);
        
        // Barbarian herblore
        addDefaultPotion(175, "Barbarian herblore", Consumable.PotionEffect.BARBARIAN_MIX, 6, 17, 50);
        
        logger.info(String.format("Loaded %d default consumables", consumablesById.size()));
    }
    
    private void addDefaultFood(int id, String name, int healAmount, int levelReq, int value, boolean special) {
        Consumable food = new Consumable(id, name, Consumable.ConsumableType.FOOD);
        food.setValue(value);
        food.setTradeable(true);
        food.setStackable(false);
        food.setHealAmount(healAmount);
        food.setConsumeTime(3); // 3 ticks for most food
        
        if (levelReq > 1) {
            food.getRequirements().put("cooking", levelReq);
        }
        
        if (special) {
            food.getEffects().add("Special healing properties");
        }
        
        consumablesById.put(id, food);
    }
    
    private void addDefaultPotion(int id, String name, Consumable.PotionEffect effect, int boost, int levelReq, int value) {
        Consumable potion = new Consumable(id, name, Consumable.ConsumableType.POTION);
        potion.setValue(value);
        potion.setTradeable(true);
        potion.setStackable(false);
        potion.setConsumeTime(3); // 3 ticks for potions
        
        if (levelReq > 1) {
            potion.getRequirements().put("herblore", levelReq);
        }
        
        // Set potion effects based on type
        switch (effect) {
            case ATTACK_BOOST:
                potion.getStatBoosts().put("attack", boost);
                potion.getEffects().add("Boosts Attack level");
                break;
            case STRENGTH_BOOST:
                potion.getStatBoosts().put("strength", boost);
                potion.getEffects().add("Boosts Strength level");
                break;
            case DEFENCE_BOOST:
                potion.getStatBoosts().put("defence", boost);
                potion.getEffects().add("Boosts Defence level");
                break;
            case RANGED_BOOST:
                potion.getStatBoosts().put("ranged", boost);
                potion.getEffects().add("Boosts Ranged level");
                break;
            case MAGIC_BOOST:
                potion.getStatBoosts().put("magic", boost);
                potion.getEffects().add("Boosts Magic level");
                break;
            case COMBAT_BOOST:
                potion.getStatBoosts().put("attack", boost);
                potion.getStatBoosts().put("strength", boost);
                potion.getEffects().add("Boosts Attack and Strength levels");
                break;
            case SUPER_COMBAT:
                potion.getStatBoosts().put("attack", boost);
                potion.getStatBoosts().put("strength", boost);
                potion.getStatBoosts().put("defence", boost);
                potion.getEffects().add("Boosts Attack, Strength, and Defence levels");
                break;
            case PRAYER_RESTORE:
                potion.getStatBoosts().put("prayer", boost);
                potion.getEffects().add("Restores Prayer points");
                break;
            case STAT_RESTORE:
                potion.getEffects().add("Restores lowered stats");
                break;
            case ENERGY_RESTORE:
                potion.getEffects().add("Restores run energy");
                break;
            case STAMINA_BOOST:
                potion.getEffects().add("Reduces run energy drain");
                break;
            case ANTIPOISON:
                potion.getEffects().add("Cures and prevents poison");
                break;
            case SARADOMIN_BREW:
                potion.getStatBoosts().put("hitpoints", boost);
                potion.getStatBoosts().put("defence", boost / 5);
                potion.getStatReductions().put("attack", boost / 10);
                potion.getStatReductions().put("strength", boost / 10);
                potion.getStatReductions().put("magic", boost / 10);
                potion.getStatReductions().put("ranged", boost / 10);
                potion.getEffects().add("Heals and boosts Defence, reduces combat stats");
                break;
            case ZAMORAK_BREW:
                potion.getStatBoosts().put("attack", boost / 5);
                potion.getStatBoosts().put("strength", boost / 5);
                potion.getStatReductions().put("defence", boost / 10);
                potion.getStatReductions().put("hitpoints", boost / 10);
                potion.getEffects().add("Boosts Attack and Strength, reduces Defence and HP");
                break;
            case OVERLOAD:
                potion.getStatBoosts().put("attack", boost / 10);
                potion.getStatBoosts().put("strength", boost / 10);
                potion.getStatBoosts().put("defence", boost / 10);
                potion.getStatBoosts().put("ranged", boost / 10);
                potion.getStatBoosts().put("magic", boost / 10);
                potion.getEffects().add("Massively boosts all combat stats");
                break;
            case BARBARIAN_MIX:
                potion.getEffects().add("Barbarian herblore mix");
                break;
        }
        
        consumablesById.put(id, potion);
    }
    
    /**
     * Build search indices for efficient querying
     */
    private void buildIndices() {
        logger.info("Building consumable search indices...");
        
        // Clear existing indices
        consumablesByName.clear();
        consumablesByType.values().forEach(List::clear);
        consumablesByHealAmount.clear();
        consumablesByEffect.clear();
        consumablesByStatBoost.clear();
        
        for (Consumable consumable : consumablesById.values()) {
            // Index by name
            String nameLower = consumable.getName().toLowerCase();
            consumablesByName.computeIfAbsent(nameLower, k -> new ArrayList<>()).add(consumable);
            
            // Index by type
            if (consumable.getType() != null) {
                consumablesByType.get(consumable.getType()).add(consumable);
            }
            
            // Index by heal amount
            if (consumable.getHealAmount() > 0) {
                consumablesByHealAmount.computeIfAbsent(consumable.getHealAmount(), k -> new ArrayList<>()).add(consumable);
            }
            
            // Index by effects
            for (String effect : consumable.getEffects()) {
                String effectLower = effect.toLowerCase();
                consumablesByEffect.computeIfAbsent(effectLower, k -> new ArrayList<>()).add(consumable);
            }
            
            // Index by stat boosts
            for (String stat : consumable.getStatBoosts().keySet()) {
                String statLower = stat.toLowerCase();
                consumablesByStatBoost.computeIfAbsent(statLower, k -> new ArrayList<>()).add(consumable);
            }
        }
        
        logger.info("Consumable search indices built successfully");
    }
    
    // Query methods
    
    /**
     * Get consumable by ID
     */
    public Consumable getById(int itemId) {
        ensureInitialized();
        return consumablesById.get(itemId);
    }
    
    /**
     * Get all consumables
     */
    public List<Consumable> getAll() {
        ensureInitialized();
        return new ArrayList<>(consumablesById.values());
    }
    
    /**
     * Find consumables by name (case-insensitive)
     */
    public List<Consumable> findByName(String name) {
        ensureInitialized();
        return consumablesByName.getOrDefault(name.toLowerCase(), new ArrayList<>());
    }
    
    /**
     * Get consumables by type
     */
    public List<Consumable> getByType(Consumable.ConsumableType type) {
        ensureInitialized();
        return new ArrayList<>(consumablesByType.getOrDefault(type, new ArrayList<>()));
    }
    
    /**
     * Get food items only
     */
    public List<Consumable> getFood() {
        return getByType(Consumable.ConsumableType.FOOD);
    }
    
    /**
     * Get potions only
     */
    public List<Consumable> getPotions() {
        return getByType(Consumable.ConsumableType.POTION);
    }
    
    /**
     * Get consumables by heal amount
     */
    public List<Consumable> getByHealAmount(int healAmount) {
        ensureInitialized();
        return new ArrayList<>(consumablesByHealAmount.getOrDefault(healAmount, new ArrayList<>()));
    }
    
    /**
     * Get consumables within heal amount range
     */
    public List<Consumable> getByHealAmountRange(int minHeal, int maxHeal) {
        ensureInitialized();
        return consumablesById.values().stream()
                .filter(consumable -> {
                    int heal = consumable.getHealAmount();
                    return heal >= minHeal && heal <= maxHeal;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get consumables by effect
     */
    public List<Consumable> getByEffect(String effect) {
        ensureInitialized();
        return new ArrayList<>(consumablesByEffect.getOrDefault(effect.toLowerCase(), new ArrayList<>()));
    }
    
    /**
     * Get consumables that boost specific stat
     */
    public List<Consumable> getByStatBoost(String stat) {
        ensureInitialized();
        return new ArrayList<>(consumablesByStatBoost.getOrDefault(stat.toLowerCase(), new ArrayList<>()));
    }
    
    /**
     * Get consumables suitable for combat level
     */
    public List<Consumable> getSuitableForCombatLevel(int combatLevel) {
        ensureInitialized();
        int minHeal = Math.max(3, combatLevel / 10);
        int maxHeal = Math.min(22, combatLevel / 3);
        
        return getByHealAmountRange(minHeal, maxHeal).stream()
                .filter(consumable -> consumable.getType() == Consumable.ConsumableType.FOOD)
                .collect(Collectors.toList());
    }
    
    /**
     * Get affordable consumables within budget
     */
    public List<Consumable> getAffordable(int maxValue) {
        ensureInitialized();
        return consumablesById.values().stream()
                .filter(consumable -> consumable.getValue() <= maxValue)
                .collect(Collectors.toList());
    }
    
    /**
     * Get tradeable consumables
     */
    public List<Consumable> getTradeable() {
        ensureInitialized();
        return consumablesById.values().stream()
                .filter(Consumable::isTradeable)
                .collect(Collectors.toList());
    }
    
    /**
     * Get stackable consumables
     */
    public List<Consumable> getStackable() {
        ensureInitialized();
        return consumablesById.values().stream()
                .filter(Consumable::isStackable)
                .collect(Collectors.toList());
    }
    
    /**
     * Search consumables by partial name match
     */
    public List<Consumable> searchByName(String partialName) {
        ensureInitialized();
        String searchTerm = partialName.toLowerCase();
        return consumablesById.values().stream()
                .filter(consumable -> consumable.getName().toLowerCase().contains(searchTerm))
                .collect(Collectors.toList());
    }
    
    /**
     * Get consumables with requirements
     */
    public List<Consumable> getWithRequirements() {
        ensureInitialized();
        return consumablesById.values().stream()
                .filter(consumable -> !consumable.getRequirements().isEmpty())
                .collect(Collectors.toList());
    }
    
    /**
     * Get consumables usable at skill level
     */
    public List<Consumable> getUsableAtLevel(String skill, int level) {
        ensureInitialized();
        return consumablesById.values().stream()
                .filter(consumable -> {
                    Integer reqLevel = consumable.getRequirements().get(skill.toLowerCase());
                    return reqLevel == null || reqLevel <= level;
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get best healing food for budget
     */
    public List<Consumable> getBestHealingFood(int maxValue, int minHeal) {
        ensureInitialized();
        return getFood().stream()
                .filter(food -> food.getValue() <= maxValue && food.getHealAmount() >= minHeal)
                .sorted((a, b) -> {
                    // Sort by heal per gp ratio (descending)
                    double ratioA = (double) a.getHealAmount() / Math.max(1, a.getValue());
                    double ratioB = (double) b.getHealAmount() / Math.max(1, b.getValue());
                    return Double.compare(ratioB, ratioA);
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get combat potions
     */
    public List<Consumable> getCombatPotions() {
        ensureInitialized();
        return getPotions().stream()
                .filter(potion -> {
                    Map<String, Integer> boosts = potion.getStatBoosts();
                    return boosts.containsKey("attack") || boosts.containsKey("strength") || 
                           boosts.containsKey("defence") || boosts.containsKey("ranged") || 
                           boosts.containsKey("magic");
                })
                .collect(Collectors.toList());
    }
    
    /**
     * Get prayer potions
     */
    public List<Consumable> getPrayerPotions() {
        ensureInitialized();
        return getPotions().stream()
                .filter(potion -> potion.getStatBoosts().containsKey("prayer"))
                .collect(Collectors.toList());
    }
    
    /**
     * Get restore potions
     */
    public List<Consumable> getRestorePotions() {
        ensureInitialized();
        return getByEffect("restores lowered stats");
    }
    
    /**
     * Get antipoison items
     */
    public List<Consumable> getAntipoisonItems() {
        ensureInitialized();
        return getByEffect("cures and prevents poison");
    }
    
    /**
     * Get consumable count
     */
    public int getCount() {
        ensureInitialized();
        return consumablesById.size();
    }
    
    /**
     * Check if repository contains consumable with given ID
     */
    public boolean contains(int itemId) {
        ensureInitialized();
        return consumablesById.containsKey(itemId);
    }
    
    /**
     * Add or update consumable
     */
    public void addOrUpdate(Consumable consumable) {
        if (consumable == null || consumable.getItemId() <= 0) {
            throw new IllegalArgumentException("Invalid consumable data");
        }
        
        ensureInitialized();
        consumablesById.put(consumable.getItemId(), consumable);
        buildIndices(); // Rebuild indices after modification
        
        logger.info(String.format("Added/updated consumable: %s (ID: %d)", consumable.getName(), consumable.getItemId()));
    }
    
    /**
     * Remove consumable by ID
     */
    public boolean remove(int itemId) {
        ensureInitialized();
        Consumable removed = consumablesById.remove(itemId);
        if (removed != null) {
            buildIndices(); // Rebuild indices after modification
            logger.info(String.format("Removed consumable: %s (ID: %d)", removed.getName(), itemId));
            return true;
        }
        return false;
    }
    
    /**
     * Clear all consumable data
     */
    public void clear() {
        consumablesById.clear();
        consumablesByName.clear();
        consumablesByType.values().forEach(List::clear);
        consumablesByHealAmount.clear();
        consumablesByEffect.clear();
        consumablesByStatBoost.clear();
        initialized = false;
        logger.info("Consumable repository cleared");
    }
    
    /**
     * Reload consumable data
     */
    public void reload() {
        logger.info("Reloading consumable repository...");
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
        
        stats.put("totalConsumables", consumablesById.size());
        stats.put("foodItems", getFood().size());
        stats.put("potions", getPotions().size());
        stats.put("tradeableItems", getTradeable().size());
        stats.put("stackableItems", getStackable().size());
        
        // Healing distribution for food
        Map<String, Integer> healRanges = new HashMap<>();
        healRanges.put("1-5", getByHealAmountRange(1, 5).size());
        healRanges.put("6-10", getByHealAmountRange(6, 10).size());
        healRanges.put("11-15", getByHealAmountRange(11, 15).size());
        healRanges.put("16-20", getByHealAmountRange(16, 20).size());
        healRanges.put("20+", getByHealAmountRange(21, 100).size());
        stats.put("healingRanges", healRanges);
        
        // Value distribution
        Map<String, Integer> valueRanges = new HashMap<>();
        valueRanges.put("1-50", getAffordable(50).size());
        valueRanges.put("51-200", consumablesById.values().stream()
                .mapToInt(c -> (c.getValue() > 50 && c.getValue() <= 200) ? 1 : 0).sum());
        valueRanges.put("201-1000", consumablesById.values().stream()
                .mapToInt(c -> (c.getValue() > 200 && c.getValue() <= 1000) ? 1 : 0).sum());
        valueRanges.put("1000+", consumablesById.values().stream()
                .mapToInt(c -> (c.getValue() > 1000) ? 1 : 0).sum());
        stats.put("valueRanges", valueRanges);
        
        return stats;
    }
}
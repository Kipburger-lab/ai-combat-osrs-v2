package database.models;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

/**
 * Represents a bank location in OSRS with coordinates, accessibility, and services
 */
public class BankLocation {
    // Basic location information
    private int locationId;
    private String name;
    private String description;
    private BankType type;
    
    // Coordinates
    private int x;
    private int y;
    private int z; // plane
    private String region;
    private String area;
    
    // Accessibility
    private List<String> accessMethods; // teleports, walking routes, etc.
    private Map<String, Integer> levelRequirements; // skill requirements
    private List<String> questRequirements;
    private List<String> itemRequirements;
    private boolean membersOnly;
    private boolean freeToPlay;
    
    // Services available
    private boolean bankChest;
    private boolean bankBooth;
    private boolean depositBox;
    private boolean grandExchange;
    private boolean shops;
    private List<String> nearbyShops;
    private boolean altar;
    private boolean furnace;
    private boolean anvil;
    private boolean range;
    private boolean well;
    
    // Combat and safety
    private boolean safeArea;
    private boolean multiCombat;
    private boolean wilderness;
    private int wildernessLevel;
    private boolean guardedArea;
    private List<String> nearbyThreats;
    
    // Teleportation
    private List<Teleport> teleports;
    private boolean hasSpellbookAltar;
    private List<String> spellbookTypes; // ancient, lunar, arceuus
    
    // Efficiency metrics
    private int walkingDistance; // from common locations
    private int bankingSpeed; // relative speed rating 1-10
    private boolean crowded; // typically busy
    private String peakHours;
    
    // Special features
    private boolean craftingArea;
    private boolean smithingArea;
    private boolean cookingArea;
    private boolean magicTraining;
    private boolean prayerTraining;
    private List<String> specialFeatures;
    
    public BankLocation() {
        this.accessMethods = new ArrayList<>();
        this.levelRequirements = new HashMap<>();
        this.questRequirements = new ArrayList<>();
        this.itemRequirements = new ArrayList<>();
        this.nearbyShops = new ArrayList<>();
        this.nearbyThreats = new ArrayList<>();
        this.teleports = new ArrayList<>();
        this.spellbookTypes = new ArrayList<>();
        this.specialFeatures = new ArrayList<>();
    }
    
    public BankLocation(int locationId, String name, BankType type, int x, int y, int z) {
        this();
        this.locationId = locationId;
        this.name = name;
        this.type = type;
        this.x = x;
        this.y = y;
        this.z = z;
    }
    
    // Getters and setters
    public int getLocationId() { return locationId; }
    public void setLocationId(int locationId) { this.locationId = locationId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public BankType getType() { return type; }
    public void setType(BankType type) { this.type = type; }
    
    public int getX() { return x; }
    public void setX(int x) { this.x = x; }
    
    public int getY() { return y; }
    public void setY(int y) { this.y = y; }
    
    public int getZ() { return z; }
    public void setZ(int z) { this.z = z; }
    
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    
    public String getArea() { return area; }
    public void setArea(String area) { this.area = area; }
    
    public List<String> getAccessMethods() { return accessMethods; }
    public void setAccessMethods(List<String> accessMethods) { 
        this.accessMethods = accessMethods != null ? accessMethods : new ArrayList<>(); 
    }
    
    public Map<String, Integer> getLevelRequirements() { return levelRequirements; }
    public void setLevelRequirements(Map<String, Integer> levelRequirements) { 
        this.levelRequirements = levelRequirements != null ? levelRequirements : new HashMap<>(); 
    }
    
    public List<String> getQuestRequirements() { return questRequirements; }
    public void setQuestRequirements(List<String> questRequirements) { 
        this.questRequirements = questRequirements != null ? questRequirements : new ArrayList<>(); 
    }
    
    public List<String> getItemRequirements() { return itemRequirements; }
    public void setItemRequirements(List<String> itemRequirements) { 
        this.itemRequirements = itemRequirements != null ? itemRequirements : new ArrayList<>(); 
    }
    
    public boolean isMembersOnly() { return membersOnly; }
    public void setMembersOnly(boolean membersOnly) { this.membersOnly = membersOnly; }
    
    public boolean isFreeToPlay() { return freeToPlay; }
    public void setFreeToPlay(boolean freeToPlay) { this.freeToPlay = freeToPlay; }
    
    public boolean isBankChest() { return bankChest; }
    public void setBankChest(boolean bankChest) { this.bankChest = bankChest; }
    
    public boolean isBankBooth() { return bankBooth; }
    public void setBankBooth(boolean bankBooth) { this.bankBooth = bankBooth; }
    
    public boolean isDepositBox() { return depositBox; }
    public void setDepositBox(boolean depositBox) { this.depositBox = depositBox; }
    
    public boolean isGrandExchange() { return grandExchange; }
    public void setGrandExchange(boolean grandExchange) { this.grandExchange = grandExchange; }
    
    public boolean isShops() { return shops; }
    public void setShops(boolean shops) { this.shops = shops; }
    
    public List<String> getNearbyShops() { return nearbyShops; }
    public void setNearbyShops(List<String> nearbyShops) { 
        this.nearbyShops = nearbyShops != null ? nearbyShops : new ArrayList<>(); 
    }
    
    public boolean isAltar() { return altar; }
    public void setAltar(boolean altar) { this.altar = altar; }
    
    public boolean isFurnace() { return furnace; }
    public void setFurnace(boolean furnace) { this.furnace = furnace; }
    
    public boolean isAnvil() { return anvil; }
    public void setAnvil(boolean anvil) { this.anvil = anvil; }
    
    public boolean isRange() { return range; }
    public void setRange(boolean range) { this.range = range; }
    
    public boolean isWell() { return well; }
    public void setWell(boolean well) { this.well = well; }
    
    public boolean isSafeArea() { return safeArea; }
    public void setSafeArea(boolean safeArea) { this.safeArea = safeArea; }
    
    public boolean isMultiCombat() { return multiCombat; }
    public void setMultiCombat(boolean multiCombat) { this.multiCombat = multiCombat; }
    
    public boolean isWilderness() { return wilderness; }
    public void setWilderness(boolean wilderness) { this.wilderness = wilderness; }
    
    public int getWildernessLevel() { return wildernessLevel; }
    public void setWildernessLevel(int wildernessLevel) { this.wildernessLevel = wildernessLevel; }
    
    public boolean isGuardedArea() { return guardedArea; }
    public void setGuardedArea(boolean guardedArea) { this.guardedArea = guardedArea; }
    
    public List<String> getNearbyThreats() { return nearbyThreats; }
    public void setNearbyThreats(List<String> nearbyThreats) { 
        this.nearbyThreats = nearbyThreats != null ? nearbyThreats : new ArrayList<>(); 
    }
    
    public List<Teleport> getTeleports() { return teleports; }
    public void setTeleports(List<Teleport> teleports) { 
        this.teleports = teleports != null ? teleports : new ArrayList<>(); 
    }
    
    public boolean isHasSpellbookAltar() { return hasSpellbookAltar; }
    public void setHasSpellbookAltar(boolean hasSpellbookAltar) { this.hasSpellbookAltar = hasSpellbookAltar; }
    
    public List<String> getSpellbookTypes() { return spellbookTypes; }
    public void setSpellbookTypes(List<String> spellbookTypes) { 
        this.spellbookTypes = spellbookTypes != null ? spellbookTypes : new ArrayList<>(); 
    }
    
    public int getWalkingDistance() { return walkingDistance; }
    public void setWalkingDistance(int walkingDistance) { this.walkingDistance = Math.max(0, walkingDistance); }
    
    public int getBankingSpeed() { return bankingSpeed; }
    public void setBankingSpeed(int bankingSpeed) { this.bankingSpeed = Math.max(1, Math.min(10, bankingSpeed)); }
    
    public boolean isCrowded() { return crowded; }
    public void setCrowded(boolean crowded) { this.crowded = crowded; }
    
    public String getPeakHours() { return peakHours; }
    public void setPeakHours(String peakHours) { this.peakHours = peakHours; }
    
    public boolean isCraftingArea() { return craftingArea; }
    public void setCraftingArea(boolean craftingArea) { this.craftingArea = craftingArea; }
    
    public boolean isSmithingArea() { return smithingArea; }
    public void setSmithingArea(boolean smithingArea) { this.smithingArea = smithingArea; }
    
    public boolean isCookingArea() { return cookingArea; }
    public void setCookingArea(boolean cookingArea) { this.cookingArea = cookingArea; }
    
    public boolean isMagicTraining() { return magicTraining; }
    public void setMagicTraining(boolean magicTraining) { this.magicTraining = magicTraining; }
    
    public boolean isPrayerTraining() { return prayerTraining; }
    public void setPrayerTraining(boolean prayerTraining) { this.prayerTraining = prayerTraining; }
    
    public List<String> getSpecialFeatures() { return specialFeatures; }
    public void setSpecialFeatures(List<String> specialFeatures) { 
        this.specialFeatures = specialFeatures != null ? specialFeatures : new ArrayList<>(); 
    }
    
    // Utility methods
    public boolean canAccess(Map<String, Integer> playerStats, List<String> completedQuests, List<String> inventory) {
        // Check level requirements
        for (Map.Entry<String, Integer> requirement : levelRequirements.entrySet()) {
            String skill = requirement.getKey();
            int requiredLevel = requirement.getValue();
            int playerLevel = playerStats.getOrDefault(skill, 1);
            
            if (playerLevel < requiredLevel) {
                return false;
            }
        }
        
        // Check quest requirements
        for (String quest : questRequirements) {
            if (!completedQuests.contains(quest)) {
                return false;
            }
        }
        
        // Check item requirements
        for (String item : itemRequirements) {
            if (!inventory.contains(item)) {
                return false;
            }
        }
        
        return true;
    }
    
    public boolean hasBankingFacility() {
        return bankChest || bankBooth || depositBox;
    }
    
    public boolean hasSkillFacilities() {
        return furnace || anvil || range || altar || craftingArea || smithingArea || cookingArea;
    }
    
    public void addAccessMethod(String method) {
        if (method != null && !accessMethods.contains(method)) {
            accessMethods.add(method);
        }
    }
    
    public void addLevelRequirement(String skill, int level) {
        if (skill != null && level > 0) {
            levelRequirements.put(skill.toLowerCase(), level);
        }
    }
    
    public void addQuestRequirement(String quest) {
        if (quest != null && !questRequirements.contains(quest)) {
            questRequirements.add(quest);
        }
    }
    
    public void addItemRequirement(String item) {
        if (item != null && !itemRequirements.contains(item)) {
            itemRequirements.add(item);
        }
    }
    
    public void addNearbyShop(String shop) {
        if (shop != null && !nearbyShops.contains(shop)) {
            nearbyShops.add(shop);
        }
    }
    
    public void addNearbyThreat(String threat) {
        if (threat != null && !nearbyThreats.contains(threat)) {
            nearbyThreats.add(threat);
        }
    }
    
    public void addTeleport(Teleport teleport) {
        if (teleport != null && !teleports.contains(teleport)) {
            teleports.add(teleport);
        }
    }
    
    public void addSpellbookType(String spellbook) {
        if (spellbook != null && !spellbookTypes.contains(spellbook)) {
            spellbookTypes.add(spellbook);
        }
    }
    
    public void addSpecialFeature(String feature) {
        if (feature != null && !specialFeatures.contains(feature)) {
            specialFeatures.add(feature);
        }
    }
    
    public double getDistanceFrom(int fromX, int fromY) {
        return Math.sqrt(Math.pow(x - fromX, 2) + Math.pow(y - fromY, 2));
    }
    
    public boolean isInRegion(String regionName) {
        return region != null && region.equalsIgnoreCase(regionName);
    }
    
    public boolean isInArea(String areaName) {
        return area != null && area.equalsIgnoreCase(areaName);
    }
    
    public int getEfficiencyScore() {
        int score = bankingSpeed * 10;
        
        // Bonus for multiple banking options
        if (bankChest) score += 5;
        if (bankBooth) score += 5;
        if (depositBox) score += 3;
        
        // Bonus for additional services
        if (grandExchange) score += 15;
        if (shops) score += 5;
        if (hasSkillFacilities()) score += 10;
        
        // Penalty for restrictions
        if (membersOnly) score -= 5;
        if (wilderness) score -= 10;
        if (crowded) score -= 5;
        
        // Bonus for safety
        if (safeArea) score += 5;
        if (guardedArea) score += 3;
        
        return Math.max(0, score);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BankLocation that = (BankLocation) o;
        return locationId == that.locationId;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(locationId);
    }
    
    @Override
    public String toString() {
        return String.format("BankLocation{id=%d, name='%s', type=%s, coords=(%d,%d,%d)}", 
                           locationId, name, type, x, y, z);
    }
    
    // Enums
    public enum BankType {
        BANK_BOOTH, BANK_CHEST, DEPOSIT_BOX, GRAND_EXCHANGE, 
        CLAN_BANK, BARBARIAN_OUTPOST, CASTLE_WARS, OTHER
    }
    
    // Inner classes
    public static class Teleport {
        private String name;
        private TeleportType type;
        private Map<String, Integer> levelRequirements;
        private List<String> itemRequirements;
        private List<String> runeRequirements;
        private int cost; // GP cost
        private boolean membersOnly;
        private String spellbook;
        
        public Teleport() {
            this.levelRequirements = new HashMap<>();
            this.itemRequirements = new ArrayList<>();
            this.runeRequirements = new ArrayList<>();
        }
        
        public Teleport(String name, TeleportType type) {
            this();
            this.name = name;
            this.type = type;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public TeleportType getType() { return type; }
        public void setType(TeleportType type) { this.type = type; }
        
        public Map<String, Integer> getLevelRequirements() { return levelRequirements; }
        public void setLevelRequirements(Map<String, Integer> levelRequirements) { 
            this.levelRequirements = levelRequirements != null ? levelRequirements : new HashMap<>(); 
        }
        
        public List<String> getItemRequirements() { return itemRequirements; }
        public void setItemRequirements(List<String> itemRequirements) { 
            this.itemRequirements = itemRequirements != null ? itemRequirements : new ArrayList<>(); 
        }
        
        public List<String> getRuneRequirements() { return runeRequirements; }
        public void setRuneRequirements(List<String> runeRequirements) { 
            this.runeRequirements = runeRequirements != null ? runeRequirements : new ArrayList<>(); 
        }
        
        public int getCost() { return cost; }
        public void setCost(int cost) { this.cost = Math.max(0, cost); }
        
        public boolean isMembersOnly() { return membersOnly; }
        public void setMembersOnly(boolean membersOnly) { this.membersOnly = membersOnly; }
        
        public String getSpellbook() { return spellbook; }
        public void setSpellbook(String spellbook) { this.spellbook = spellbook; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Teleport teleport = (Teleport) o;
            return Objects.equals(name, teleport.name) && type == teleport.type;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(name, type);
        }
        
        @Override
        public String toString() {
            return String.format("Teleport{name='%s', type=%s, cost=%d}", name, type, cost);
        }
    }
    
    public enum TeleportType {
        SPELL, TABLET, JEWELRY, ITEM, MINIGAME, QUEST, ACHIEVEMENT_DIARY, OTHER
    }
}
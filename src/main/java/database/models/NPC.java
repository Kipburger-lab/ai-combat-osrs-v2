package database.models;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;

/**
 * Represents an NPC in OSRS with combat stats, locations, and drop information
 */
public class NPC {
    // Basic NPC information
    private int npcId;
    private String name;
    private String description;
    private NPCType type;
    
    // Combat stats
    private CombatStats combatStats;
    
    // Location information
    private List<Location> locations;
    
    // Behavior properties
    private boolean aggressive;
    private boolean poisonous;
    private boolean undead;
    private int aggroRange;
    private int respawnTime; // in ticks
    
    // Drop information
    private List<Drop> dropTable;
    private int maxHit;
    
    // Slayer information
    private boolean slayerMonster;
    private List<String> slayerMasters;
    private int slayerLevel;
    private String slayerCategory;
    
    // Weaknesses and resistances
    private Map<String, Integer> weaknesses; // attack style -> effectiveness
    private Map<String, Integer> resistances;
    
    public NPC() {
        this.locations = new ArrayList<>();
        this.dropTable = new ArrayList<>();
        this.slayerMasters = new ArrayList<>();
        this.weaknesses = new HashMap<>();
        this.resistances = new HashMap<>();
        this.combatStats = new CombatStats();
    }
    
    public NPC(int npcId, String name, NPCType type) {
        this();
        this.npcId = npcId;
        this.name = name;
        this.type = type;
    }
    
    // Getters and setters
    public int getNpcId() { return npcId; }
    public void setNpcId(int npcId) { this.npcId = npcId; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public NPCType getType() { return type; }
    public void setType(NPCType type) { this.type = type; }
    
    public CombatStats getCombatStats() { return combatStats; }
    public void setCombatStats(CombatStats combatStats) { 
        this.combatStats = combatStats != null ? combatStats : new CombatStats(); 
    }
    
    public List<Location> getLocations() { return locations; }
    public void setLocations(List<Location> locations) { 
        this.locations = locations != null ? locations : new ArrayList<>(); 
    }
    
    public boolean isAggressive() { return aggressive; }
    public void setAggressive(boolean aggressive) { this.aggressive = aggressive; }
    
    public boolean isPoisonous() { return poisonous; }
    public void setPoisonous(boolean poisonous) { this.poisonous = poisonous; }
    
    public boolean isUndead() { return undead; }
    public void setUndead(boolean undead) { this.undead = undead; }
    
    public int getAggroRange() { return aggroRange; }
    public void setAggroRange(int aggroRange) { this.aggroRange = aggroRange; }
    
    public int getRespawnTime() { return respawnTime; }
    public void setRespawnTime(int respawnTime) { this.respawnTime = respawnTime; }
    
    public List<Drop> getDropTable() { return dropTable; }
    public void setDropTable(List<Drop> dropTable) { 
        this.dropTable = dropTable != null ? dropTable : new ArrayList<>(); 
    }
    
    public int getMaxHit() { return maxHit; }
    public void setMaxHit(int maxHit) { this.maxHit = maxHit; }
    
    public boolean isSlayerMonster() { return slayerMonster; }
    public void setSlayerMonster(boolean slayerMonster) { this.slayerMonster = slayerMonster; }
    
    public List<String> getSlayerMasters() { return slayerMasters; }
    public void setSlayerMasters(List<String> slayerMasters) { 
        this.slayerMasters = slayerMasters != null ? slayerMasters : new ArrayList<>(); 
    }
    
    public int getSlayerLevel() { return slayerLevel; }
    public void setSlayerLevel(int slayerLevel) { this.slayerLevel = slayerLevel; }
    
    public String getSlayerCategory() { return slayerCategory; }
    public void setSlayerCategory(String slayerCategory) { this.slayerCategory = slayerCategory; }
    
    public Map<String, Integer> getWeaknesses() { return weaknesses; }
    public void setWeaknesses(Map<String, Integer> weaknesses) { 
        this.weaknesses = weaknesses != null ? weaknesses : new HashMap<>(); 
    }
    
    public Map<String, Integer> getResistances() { return resistances; }
    public void setResistances(Map<String, Integer> resistances) { 
        this.resistances = resistances != null ? resistances : new HashMap<>(); 
    }
    
    // Utility methods
    public int getCombatLevel() {
        if (combatStats == null) return 1;
        return combatStats.getCombatLevel();
    }
    
    public boolean isInLocation(String locationName) {
        return locations.stream().anyMatch(loc -> loc.getName().equalsIgnoreCase(locationName));
    }
    
    public boolean isInRegion(String regionName) {
        return locations.stream().anyMatch(loc -> loc.getRegion().equalsIgnoreCase(regionName));
    }
    
    /**
     * Get the primary location name for this NPC
     * @return the name of the first location, or null if no locations exist
     */
    public String getLocation() {
        if (locations == null || locations.isEmpty()) {
            return null;
        }
        return locations.get(0).getName();
    }
    
    public void addLocation(Location location) {
        if (location != null && !locations.contains(location)) {
            locations.add(location);
        }
    }
    
    public void addDrop(Drop drop) {
        if (drop != null && !dropTable.contains(drop)) {
            dropTable.add(drop);
        }
    }
    
    public boolean hasWeakness(String attackStyle) {
        return weaknesses.containsKey(attackStyle.toLowerCase());
    }
    
    public int getWeaknessValue(String attackStyle) {
        return weaknesses.getOrDefault(attackStyle.toLowerCase(), 0);
    }
    
    public boolean hasResistance(String attackStyle) {
        return resistances.containsKey(attackStyle.toLowerCase());
    }
    
    public int getResistanceValue(String attackStyle) {
        return resistances.getOrDefault(attackStyle.toLowerCase(), 0);
    }
    
    public boolean isSafeSpottable() {
        // NPCs that can be safe spotted (ranged/magic only)
        return !aggressive || type == NPCType.MONSTER;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NPC npc = (NPC) o;
        return npcId == npc.npcId;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(npcId);
    }
    
    @Override
    public String toString() {
        return String.format("NPC{id=%d, name='%s', type=%s, combatLevel=%d}", 
                           npcId, name, type, getCombatLevel());
    }
    
    // Enums
    public enum NPCType {
        MONSTER, BOSS, SLAYER_MONSTER, PVP_NPC, QUEST_NPC, SKILL_NPC, SHOP_NPC, OTHER
    }
    
    // Inner classes
    public static class CombatStats {
        private int hitpoints = 1;
        private int attack = 1;
        private int strength = 1;
        private int defence = 1;
        private int ranged = 1;
        private int magic = 1;
        private int prayer = 1;
        
        public int getHitpoints() { return hitpoints; }
        public void setHitpoints(int hitpoints) { this.hitpoints = Math.max(1, hitpoints); }
        
        public int getAttack() { return attack; }
        public void setAttack(int attack) { this.attack = Math.max(1, attack); }
        
        public int getStrength() { return strength; }
        public void setStrength(int strength) { this.strength = Math.max(1, strength); }
        
        public int getDefence() { return defence; }
        public void setDefence(int defence) { this.defence = Math.max(1, defence); }
        
        public int getRanged() { return ranged; }
        public void setRanged(int ranged) { this.ranged = Math.max(1, ranged); }
        
        public int getMagic() { return magic; }
        public void setMagic(int magic) { this.magic = Math.max(1, magic); }
        
        public int getPrayer() { return prayer; }
        public void setPrayer(int prayer) { this.prayer = Math.max(1, prayer); }
        
        public int getCombatLevel() {
            // OSRS combat level formula
            double base = 0.25 * (defence + hitpoints + Math.floor(prayer / 2));
            double melee = 0.325 * (attack + strength);
            double rangedLevel = 0.325 * (Math.floor(ranged * 1.5));
            double magicLevel = 0.325 * (Math.floor(magic * 1.5));
            
            return (int) Math.floor(base + Math.max(melee, Math.max(rangedLevel, magicLevel)));
        }
    }
    
    public static class Location {
        private String name;
        private String region;
        private int x;
        private int y;
        private int z; // plane
        private boolean multiCombat;
        private boolean wilderness;
        private int wildernessLevel;
        
        public Location() {}
        
        public Location(String name, String region, int x, int y, int z) {
            this.name = name;
            this.region = region;
            this.x = x;
            this.y = y;
            this.z = z;
        }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        
        public int getX() { return x; }
        public void setX(int x) { this.x = x; }
        
        public int getY() { return y; }
        public void setY(int y) { this.y = y; }
        
        public int getZ() { return z; }
        public void setZ(int z) { this.z = z; }
        
        public boolean isMultiCombat() { return multiCombat; }
        public void setMultiCombat(boolean multiCombat) { this.multiCombat = multiCombat; }
        
        public boolean isWilderness() { return wilderness; }
        public void setWilderness(boolean wilderness) { this.wilderness = wilderness; }
        
        public int getWildernessLevel() { return wildernessLevel; }
        public void setWildernessLevel(int wildernessLevel) { this.wildernessLevel = wildernessLevel; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Location location = (Location) o;
            return x == location.x && y == location.y && z == location.z;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(x, y, z);
        }
        
        @Override
        public String toString() {
            return String.format("Location{name='%s', region='%s', coords=(%d,%d,%d)}", 
                               name, region, x, y, z);
        }
    }
    
    public static class Drop {
        private int itemId;
        private String itemName;
        private int minQuantity;
        private int maxQuantity;
        private double dropRate; // 0.0 to 1.0
        private DropRarity rarity;
        private boolean noted;
        
        public Drop() {}
        
        public Drop(int itemId, String itemName, int minQuantity, int maxQuantity, double dropRate) {
            this.itemId = itemId;
            this.itemName = itemName;
            this.minQuantity = minQuantity;
            this.maxQuantity = maxQuantity;
            this.dropRate = dropRate;
        }
        
        public int getItemId() { return itemId; }
        public void setItemId(int itemId) { this.itemId = itemId; }
        
        public String getItemName() { return itemName; }
        public void setItemName(String itemName) { this.itemName = itemName; }
        
        public int getMinQuantity() { return minQuantity; }
        public void setMinQuantity(int minQuantity) { this.minQuantity = minQuantity; }
        
        public int getMaxQuantity() { return maxQuantity; }
        public void setMaxQuantity(int maxQuantity) { this.maxQuantity = maxQuantity; }
        
        public double getDropRate() { return dropRate; }
        public void setDropRate(double dropRate) { this.dropRate = Math.max(0.0, Math.min(1.0, dropRate)); }
        
        public DropRarity getRarity() { return rarity; }
        public void setRarity(DropRarity rarity) { this.rarity = rarity; }
        
        public boolean isNoted() { return noted; }
        public void setNoted(boolean noted) { this.noted = noted; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Drop drop = (Drop) o;
            return itemId == drop.itemId;
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(itemId);
        }
        
        @Override
        public String toString() {
            return String.format("Drop{itemId=%d, name='%s', quantity=%d-%d, rate=%.4f}", 
                               itemId, itemName, minQuantity, maxQuantity, dropRate);
        }
    }
    
    public enum DropRarity {
        ALWAYS, COMMON, UNCOMMON, RARE, VERY_RARE, ULTRA_RARE
    }
}
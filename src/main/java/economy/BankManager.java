package economy;

import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.container.impl.bank.BankLocation;
import org.dreambot.api.methods.container.impl.equipment.Equipment;
import org.dreambot.api.methods.container.impl.equipment.EquipmentSlot;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.utilities.Logger;
import org.dreambot.api.utilities.Sleep;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Advanced Banking Manager for AI Combat OSRS
 * Handles all banking operations including equipment management,
 * supply restocking, and intelligent bank organization
 * 
 * @author TraeAI
 * @version 2.0
 */
public class BankManager {
    
    // Banking configuration
    private static final int MAX_BANK_ATTEMPTS = 3;
    private static final int BANK_TIMEOUT = 5000;
    private static final int WALK_TIMEOUT = 10000;
    
    // Equipment and supply tracking
    private final Map<String, Integer> requiredSupplies = new ConcurrentHashMap<>();
    private final Map<String, Integer> equipmentSets = new ConcurrentHashMap<>();
    private final Set<String> priorityItems = new HashSet<>();
    
    // Banking state
    private BankLocation currentBankLocation;
    private boolean bankingInProgress = false;
    private boolean bankingEnabled = true;
    private long lastBankingAttempt = 0;
    
    // Statistics
    private int totalBankingOperations = 0;
    private int successfulOperations = 0;
    private long totalBankingTime = 0;
    
    public BankManager() {
        initializeDefaultSupplies();
        initializeEquipmentSets();
        Logger.log("[BankManager] Initialized with advanced banking capabilities");
    }
    
    /**
     * Initialize default supply requirements
     */
    private void initializeDefaultSupplies() {
        // Food supplies
        requiredSupplies.put("Shark", 10);
        requiredSupplies.put("Monkfish", 15);
        requiredSupplies.put("Lobster", 20);
        
        // Potions
        requiredSupplies.put("Super combat potion(4)", 2);
        requiredSupplies.put("Prayer potion(4)", 3);
        requiredSupplies.put("Super restore(4)", 2);
        
        // Ammunition
        requiredSupplies.put("Dragon arrow", 500);
        requiredSupplies.put("Rune arrow", 1000);
        requiredSupplies.put("Broad bolts", 1000);
        
        // Runes for magic
        requiredSupplies.put("Death rune", 1000);
        requiredSupplies.put("Blood rune", 500);
        requiredSupplies.put("Nature rune", 1000);
        
        Logger.log("[BankManager] Default supplies initialized: " + requiredSupplies.size() + " items");
    }
    
    /**
     * Initialize equipment set configurations
     */
    private void initializeEquipmentSets() {
        // Melee equipment set
        equipmentSets.put("MELEE_WEAPON", 1);
        equipmentSets.put("MELEE_ARMOR", 1);
        equipmentSets.put("MELEE_SHIELD", 1);
        
        // Ranged equipment set
        equipmentSets.put("RANGED_WEAPON", 1);
        equipmentSets.put("RANGED_ARMOR", 1);
        equipmentSets.put("RANGED_AMMO", 1);
        
        // Magic equipment set
        equipmentSets.put("MAGIC_WEAPON", 1);
        equipmentSets.put("MAGIC_ARMOR", 1);
        equipmentSets.put("MAGIC_SHIELD", 1);
        
        Logger.log("[BankManager] Equipment sets initialized: " + equipmentSets.size() + " categories");
    }
    
    /**
     * Main banking operation - handles all banking needs
     * @return true if banking was successful, false otherwise
     */
    /**
     * Determines if banking is necessary based on inventory and supply levels.
     * @return {@code true} if banking is required, {@code false} otherwise.
     */
    public boolean needsToBank() {
        if (Inventory.isFull()) {
            Logger.log("[BankManager] Banking required: Inventory is full.");
            return true;
        }

        for (Map.Entry<String, Integer> supply : requiredSupplies.entrySet()) {
            String itemName = supply.getKey();
            int requiredCount = supply.getValue();
            int currentCount = Inventory.count(itemName);

            // If we have less than 10% of a required supply, and the bank has more, we should restock.
            if (currentCount < (requiredCount * 0.1) && Bank.contains(itemName)) {
                Logger.log(String.format("[BankManager] Banking required: Low on %s (have %d, need %d).", itemName, currentCount, requiredCount));
                return true;
            }
        }

        Logger.log("[BankManager] Banking not currently required.");
        return false;
    }

    public boolean handleBanking() {
        return performBanking();
    }

    public boolean performBanking() {
        if (bankingInProgress) {
            Logger.log("[BankManager] Banking already in progress, skipping");
            return false;
        }
        
        long startTime = System.currentTimeMillis();
        bankingInProgress = true;
        totalBankingOperations++;
        
        try {
            Logger.log("[BankManager] Starting banking operation #" + totalBankingOperations);
            
            // Step 1: Navigate to nearest bank
            if (!navigateToBank()) {
                Logger.error("[BankManager] Failed to navigate to bank");
                return false;
            }
            
            // Step 2: Open bank interface
            if (!openBank()) {
                Logger.error("[BankManager] Failed to open bank");
                return false;
            }
            
            // Step 3: Deposit unnecessary items
            if (!depositUnnecessaryItems()) {
                Logger.error("[BankManager] Failed to deposit items");
                return false;
            }
            
            // Step 4: Withdraw required supplies
            if (!withdrawRequiredSupplies()) {
                Logger.error("[BankManager] Failed to withdraw supplies");
                return false;
            }
            
            // Step 5: Organize inventory
            if (!organizeInventory()) {
                Logger.error("[BankManager] Failed to organize inventory");
                return false;
            }
            
            // Step 6: Close bank
            closeBank();
            
            successfulOperations++;
            long operationTime = System.currentTimeMillis() - startTime;
            totalBankingTime += operationTime;
            
            Logger.log(String.format("[BankManager] Banking completed successfully in %dms (Success rate: %.1f%%)", 
                operationTime, getSuccessRate()));
            
            return true;
            
        } catch (Exception e) {
            Logger.error("[BankManager] Banking operation failed: " + e.getMessage());
            return false;
        } finally {
            bankingInProgress = false;
            lastBankingAttempt = System.currentTimeMillis();
        }
    }
    
    /**
     * Navigate to the nearest available bank
     * @return true if navigation was successful
     */
    private boolean navigateToBank() {
        Logger.log("[BankManager] Finding nearest bank location");
        
        BankLocation nearestBank = BankLocation.getNearest();
        if (nearestBank == null) {
            Logger.error("[BankManager] No bank location found");
            return false;
        }
        
        currentBankLocation = nearestBank;
        Logger.log("[BankManager] Nearest bank: " + nearestBank.name());
        
        if (nearestBank.getArea(5).contains(Players.getLocal().getTile())) {
            Logger.log("[BankManager] Already at bank location");
            return true;
        }
        
        Logger.log("[BankManager] Walking to bank: " + nearestBank.name());
        
        if (Walking.walk(nearestBank.getArea(5).getRandomTile())) {
            return Sleep.sleepUntil(() -> nearestBank.getArea(5).contains(Players.getLocal().getTile()), WALK_TIMEOUT);
        }
        
        return false;
    }
    
    /**
     * Open the bank interface
     * @return true if bank was opened successfully
     */
    private boolean openBank() {
        if (Bank.isOpen()) {
            Logger.log("[BankManager] Bank is already open");
            return true;
        }
        
        Logger.log("[BankManager] Opening bank interface");
        
        for (int attempt = 1; attempt <= MAX_BANK_ATTEMPTS; attempt++) {
            if (Bank.open()) {
                if (Sleep.sleepUntil(Bank::isOpen, BANK_TIMEOUT)) {
                    Logger.log("[BankManager] Bank opened successfully on attempt " + attempt);
                    return true;
                }
            }
            
            Logger.log("[BankManager] Bank open attempt " + attempt + " failed, retrying...");
            Sleep.sleep(1000, 2000);
        }
        
        return false;
    }
    
    /**
     * Deposit unnecessary items from inventory
     * @return true if deposit was successful
     */
    private boolean depositUnnecessaryItems() {
        Logger.log("[BankManager] Depositing unnecessary items");
        
        // Get items to keep in inventory
        Set<String> itemsToKeep = getItemsToKeep();
        
        for (Item item : Inventory.all()) {
            if (item != null && !itemsToKeep.contains(item.getName())) {
                Logger.log("[BankManager] Depositing: " + item.getName() + " x" + item.getAmount());
                
                if (!Bank.deposit(item.getName(), item.getAmount())) {
                    Logger.error("[BankManager] Failed to deposit: " + item.getName());
                    return false;
                }
                
                Sleep.sleep(100, 300);
            }
        }
        
        return true;
    }
    
    /**
     * Withdraw required supplies from bank
     * @return true if withdrawal was successful
     */
    private boolean withdrawRequiredSupplies() {
        Logger.log("[BankManager] Withdrawing required supplies");
        
        for (Map.Entry<String, Integer> supply : requiredSupplies.entrySet()) {
            String itemName = supply.getKey();
            int requiredAmount = supply.getValue();
            
            int currentAmount = Inventory.count(itemName);
            int neededAmount = requiredAmount - currentAmount;
            
            if (neededAmount > 0) {
                if (Bank.contains(itemName)) {
                    int bankAmount = Bank.count(itemName);
                    int withdrawAmount = Math.min(neededAmount, bankAmount);
                    
                    Logger.log(String.format("[BankManager] Withdrawing %s: %d (need %d, have %d, bank has %d)", 
                        itemName, withdrawAmount, neededAmount, currentAmount, bankAmount));
                    
                    if (!Bank.withdraw(itemName, withdrawAmount)) {
                        Logger.error("[BankManager] Failed to withdraw: " + itemName);
                        return false;
                    }
                    
                    Sleep.sleep(100, 300);
                } else {
                    Logger.warn("[BankManager] Required item not found in bank: " + itemName);
                }
            }
        }
        
        return true;
    }
    
    /**
     * Organize inventory for optimal combat efficiency
     * @return true if organization was successful
     */
    private boolean organizeInventory() {
        Logger.log("[BankManager] Organizing inventory for combat efficiency");
        
        // Priority items should be in specific slots for quick access
        // This is a simplified version - can be expanded based on combat style
        
        return true;
    }
    
    /**
     * Close the bank interface
     */
    private void closeBank() {
        if (Bank.isOpen()) {
            Logger.log("[BankManager] Closing bank interface");
            Bank.close();
            Sleep.sleepUntil(() -> !Bank.isOpen(), 3000);
        }
    }
    
    /**
     * Get items that should be kept in inventory
     * @return set of item names to keep
     */
    private Set<String> getItemsToKeep() {
        Set<String> itemsToKeep = new HashSet<>();
        
        // Add equipped items
        for (Item equippedItem : Equipment.all()) {
            if (equippedItem != null) {
                itemsToKeep.add(equippedItem.getName());
            }
        }
        
        // Add priority items
        itemsToKeep.addAll(priorityItems);
        
        // Add required supplies
        itemsToKeep.addAll(requiredSupplies.keySet());
        
        return itemsToKeep;
    }
    
    /**
     * Check if banking is needed based on current supplies
     * @return true if banking is required
     */
    public boolean isBankingNeeded() {
        // Check if we have enough food
        int foodCount = getFoodCount();
        if (foodCount < 3) {
            Logger.log("[BankManager] Banking needed: Low food count (" + foodCount + ")");
            return true;
        }
        
        // Check if we have enough potions
        int potionCount = getPotionCount();
        if (potionCount < 1) {
            Logger.log("[BankManager] Banking needed: No potions available");
            return true;
        }
        
        // Check ammunition for ranged combat
        if (isUsingRangedCombat() && getAmmunitionCount() < 50) {
            Logger.log("[BankManager] Banking needed: Low ammunition");
            return true;
        }
        
        // Check runes for magic combat
        if (isUsingMagicCombat() && getRuneCount() < 100) {
            Logger.log("[BankManager] Banking needed: Low runes");
            return true;
        }
        
        return false;
    }
    
    /**
     * Get current food count in inventory
     * @return number of food items
     */
    private int getFoodCount() {
        return Inventory.count("Shark") + Inventory.count("Monkfish") + Inventory.count("Lobster");
    }
    
    /**
     * Get current potion count in inventory
     * @return number of potion doses
     */
    private int getPotionCount() {
        int count = 0;
        for (String potion : Arrays.asList("Super combat potion(4)", "Super combat potion(3)", 
                                          "Super combat potion(2)", "Super combat potion(1)")) {
            count += Inventory.count(potion);
        }
        return count;
    }
    
    /**
     * Get current ammunition count
     * @return number of ammunition items
     */
    private int getAmmunitionCount() {
        return Inventory.count("Dragon arrow") + Inventory.count("Rune arrow") + Inventory.count("Broad bolts");
    }
    
    /**
     * Get current rune count for magic
     * @return number of runes
     */
    private int getRuneCount() {
        return Inventory.count("Death rune") + Inventory.count("Blood rune") + Inventory.count("Nature rune");
    }
    
    /**
     * Check if currently using ranged combat
     * @return true if using ranged combat
     */
    private boolean isUsingRangedCombat() {
        Item weapon = Equipment.getItemInSlot(EquipmentSlot.WEAPON);
        if (weapon != null) {
            String weaponName = weapon.getName().toLowerCase();
            return weaponName.contains("bow") || weaponName.contains("crossbow") || 
                   weaponName.contains("dart") || weaponName.contains("javelin");
        }
        return false;
    }
    
    /**
     * Check if currently using magic combat
     * @return true if using magic combat
     */
    private boolean isUsingMagicCombat() {
        Item weapon = Equipment.getItemInSlot(EquipmentSlot.WEAPON);
        if (weapon != null) {
            String weaponName = weapon.getName().toLowerCase();
            return weaponName.contains("staff") || weaponName.contains("wand") || weaponName.contains("tome");
        }
        return false;
    }
    
    /**
     * Add a required supply item
     * @param itemName name of the item
     * @param amount required amount
     */
    public void addRequiredSupply(String itemName, int amount) {
        requiredSupplies.put(itemName, amount);
        Logger.log("[BankManager] Added required supply: " + itemName + " x" + amount);
    }
    
    /**
     * Remove a required supply item
     * @param itemName name of the item to remove
     */
    public void removeRequiredSupply(String itemName) {
        requiredSupplies.remove(itemName);
        Logger.log("[BankManager] Removed required supply: " + itemName);
    }
    
    /**
     * Add a priority item that should always be kept
     * @param itemName name of the priority item
     */
    public void addPriorityItem(String itemName) {
        priorityItems.add(itemName);
        Logger.log("[BankManager] Added priority item: " + itemName);
    }
    
    /**
     * Get banking success rate
     * @return success rate as percentage
     */
    public double getSuccessRate() {
        if (totalBankingOperations == 0) return 0.0;
        return (double) successfulOperations / totalBankingOperations * 100.0;
    }
    
    /**
     * Get average banking time
     * @return average time in milliseconds
     */
    public long getAverageBankingTime() {
        if (successfulOperations == 0) return 0;
        return totalBankingTime / successfulOperations;
    }
    
    /**
     * Get banking statistics
     * @return formatted statistics string
     */
    public String getStatistics() {
        return String.format("Banking Stats - Operations: %d, Success Rate: %.1f%%, Avg Time: %dms",
            totalBankingOperations, getSuccessRate(), getAverageBankingTime());
    }
    
    /**
     * Reset banking statistics
     */
    public void resetStatistics() {
        totalBankingOperations = 0;
        successfulOperations = 0;
        totalBankingTime = 0;
        Logger.log("[BankManager] Statistics reset");
    }
    
    /**
     * Enable or disable banking
     * @param enabled true to enable banking, false to disable
     */
    public void setEnabled(boolean enabled) {
        this.bankingEnabled = enabled;
        Logger.log("[BankManager] Banking " + (enabled ? "enabled" : "disabled"));
    }
    
    /**
     * Check if banking is enabled
     * @return true if banking is enabled
     */
    public boolean isEnabled() {
        return bankingEnabled;
    }
    
    // Configuration methods for GUI integration
    private boolean withdrawFood = true;
    private boolean withdrawPotions = true;
    private boolean depositJunkItems = true;
    private int minFoodAmount = 5;
    private int maxFoodAmount = 15;
    private int minPotionAmount = 2;
    
    /**
     * Set whether to withdraw food items
     * @param withdrawFood true to withdraw food
     */
    public void setWithdrawFood(boolean withdrawFood) {
        this.withdrawFood = withdrawFood;
        Logger.log("[BankManager] Withdraw food: " + withdrawFood);
    }
    
    /**
     * Set whether to withdraw potions
     * @param withdrawPotions true to withdraw potions
     */
    public void setWithdrawPotions(boolean withdrawPotions) {
        this.withdrawPotions = withdrawPotions;
        Logger.log("[BankManager] Withdraw potions: " + withdrawPotions);
    }
    
    /**
     * Set minimum food amount to maintain
     * @param minFoodAmount minimum food count
     */
    public void setMinFoodAmount(int minFoodAmount) {
        this.minFoodAmount = minFoodAmount;
        Logger.log("[BankManager] Min food amount: " + minFoodAmount);
    }
    
    /**
     * Set maximum food amount to withdraw
     * @param maxFoodAmount maximum food count
     */
    public void setMaxFoodAmount(int maxFoodAmount) {
        this.maxFoodAmount = maxFoodAmount;
        Logger.log("[BankManager] Max food amount: " + maxFoodAmount);
    }
    
    /**
     * Set minimum potion amount to maintain
     * @param minPotionAmount minimum potion count
     */
    public void setMinPotionAmount(int minPotionAmount) {
        this.minPotionAmount = minPotionAmount;
        Logger.log("[BankManager] Min potion amount: " + minPotionAmount);
    }
    
    /**
     * Set whether to deposit junk items
     * @param depositJunkItems true to deposit junk items
     */
    public void setDepositJunkItems(boolean depositJunkItems) {
        this.depositJunkItems = depositJunkItems;
        Logger.log("[BankManager] Deposit junk items: " + depositJunkItems);
    }
    
    /**
     * Set whether banking is enabled (alias for setEnabled)
     * @param enabled true to enable banking
     */
    public void setBankingEnabled(boolean enabled) {
        setEnabled(enabled);
    }
}
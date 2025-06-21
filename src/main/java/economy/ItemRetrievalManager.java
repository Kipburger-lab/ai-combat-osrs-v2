package economy;

import org.dreambot.api.methods.Calculations;
import org.dreambot.api.methods.container.impl.Inventory;
import org.dreambot.api.methods.container.impl.bank.Bank;
import org.dreambot.api.methods.interactive.GameObjects;
import org.dreambot.api.methods.interactive.NPCs;
import org.dreambot.api.methods.map.Area;
import org.dreambot.api.methods.walking.impl.Walking;
import org.dreambot.api.methods.widget.Widgets;
import org.dreambot.api.methods.interactive.Players;
import org.dreambot.api.wrappers.interactive.GameObject;
import org.dreambot.api.methods.map.Tile;
import org.dreambot.api.wrappers.interactive.NPC;
import org.dreambot.api.wrappers.items.Item;
import org.dreambot.api.utilities.Timer;
import org.dreambot.api.utilities.Sleep;

import database.repositories.BankLocationRepository;
import database.models.BankLocation;
import org.dreambot.api.utilities.Logger;

import java.util.List;
import java.util.Arrays;

/**
 * Manages item retrieval logic including banking, Ring of Wealth teleportation,
 * and walking to banks when items are missing
 */
public class ItemRetrievalManager {
    
    private BankLocationRepository bankLocationRepository;
    private String selectedBankLocation;
    private static final String[] RING_OF_WEALTH_NAMES = {
        "Ring of wealth (1)", "Ring of wealth (2)", "Ring of wealth (3)",
        "Ring of wealth (4)", "Ring of wealth (5)", "Ring of wealth (6)"
    };
    
    public ItemRetrievalManager() {
        this.bankLocationRepository = new BankLocationRepository();
        this.selectedBankLocation = "Lumbridge"; // Default bank location
    }
    
    /**
     * Set the selected bank location from GUI
     */
    public void setSelectedBankLocation(String bankLocation) {
        this.selectedBankLocation = bankLocation;
        Logger.log("[ItemRetrievalManager] Bank location set to: " + bankLocation);
    }
    
    /**
     * Main method to handle missing items retrieval
     * @param requiredItems List of item names that are required
     * @return true if items were successfully retrieved or are available
     */
    public boolean retrieveMissingItems(List<String> requiredItems) {
        Logger.log("[ItemRetrievalManager] Checking for missing items: " + requiredItems);
        
        List<String> missingItems = getMissingItems(requiredItems);
        if (missingItems.isEmpty()) {
            Logger.log("[ItemRetrievalManager] All required items are available");
            return true;
        }
        
        Logger.log("[ItemRetrievalManager] Missing items detected: " + missingItems);
        
        // Step 1: Try to withdraw from selected bank
        if (goToBankAndWithdraw(missingItems)) {
            return true;
        }
        
        // Step 2: If items not in bank, try Ring of Wealth teleport to GE
        if (useRingOfWealthToGE()) {
            Logger.log("[ItemRetrievalManager] Teleported to Grand Exchange via Ring of Wealth");
            return true;
        }
        
        // Step 3: Walk to bank as fallback
        return walkToBank();
    }
    
    /**
     * Get list of missing items from inventory
     */
    private List<String> getMissingItems(List<String> requiredItems) {
        return requiredItems.stream()
            .filter(itemName -> !Inventory.contains(itemName))
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * Go to selected bank and try to withdraw missing items
     */
    private boolean goToBankAndWithdraw(List<String> missingItems) {
        Logger.log("[ItemRetrievalManager] Attempting to go to bank: " + selectedBankLocation);
        
        // Get bank location from database
        BankLocation bankLocation = bankLocationRepository.findByName(selectedBankLocation).stream()
            .findFirst().orElse(null);
        
        if (bankLocation == null) {
            Logger.log("[ItemRetrievalManager] Bank location not found in database: " + selectedBankLocation);
            return false;
        }
        
        // Walk to bank location
        if (!walkToLocation(bankLocation)) {
            Logger.log("[ItemRetrievalManager] Failed to reach bank location");
            return false;
        }
        
        // Open bank
        if (!openBank()) {
            Logger.log("[ItemRetrievalManager] Failed to open bank");
            return false;
        }
        
        // Try to withdraw missing items
        boolean allItemsWithdrawn = true;
        for (String itemName : missingItems) {
            if (Bank.contains(itemName)) {
                if (Bank.withdraw(itemName, 1)) {
                    Logger.log("[ItemRetrievalManager] Withdrew: " + itemName);
                    Sleep.sleepUntil(() -> Inventory.contains(itemName), 3000);
                } else {
                    Logger.log("[ItemRetrievalManager] Failed to withdraw: " + itemName);
                    allItemsWithdrawn = false;
                }
            } else {
                Logger.log("[ItemRetrievalManager] Item not found in bank: " + itemName);
                allItemsWithdrawn = false;
            }
        }
        
        Bank.close();
        return allItemsWithdrawn;
    }
    
    /**
     * Use Ring of Wealth to teleport to Grand Exchange
     */
    private boolean useRingOfWealthToGE() {
        Logger.log("[ItemRetrievalManager] Searching for Ring of Wealth");
        
        // Check inventory first
        Item ringOfWealth = findRingOfWealth(true);
        if (ringOfWealth != null) {
            return teleportWithRing(ringOfWealth);
        }
        
        // Check bank if we can access it
        if (Bank.isOpen() || openBank()) {
            ringOfWealth = findRingOfWealth(false);
            if (ringOfWealth != null) {
                // Withdraw ring from bank
                final String ringName = ringOfWealth.getName();
                if (Bank.withdraw(ringName, 1)) {
                    Sleep.sleepUntil(() -> Inventory.contains(ringName), 3000);
                    Bank.close();
                    
                    // Try to use the ring
                    Item inventoryRing = findRingOfWealth(true);
                    if (inventoryRing != null) {
                        return teleportWithRing(inventoryRing);
                    }
                }
            }
            Bank.close();
        }
        
        Logger.log("[ItemRetrievalManager] No Ring of Wealth found");
        return false;
    }
    
    /**
     * Find Ring of Wealth in inventory or bank
     */
    private Item findRingOfWealth(boolean searchInventory) {
        for (String ringName : RING_OF_WEALTH_NAMES) {
            Item ring = searchInventory ? Inventory.get(ringName) : Bank.get(ringName);
            if (ring != null) {
                Logger.log("[ItemRetrievalManager] Found " + ringName);
                return ring;
            }
        }
        return null;
    }
    
    /**
     * Teleport using Ring of Wealth
     */
    private boolean teleportWithRing(Item ring) {
        Logger.log("[ItemRetrievalManager] Attempting to teleport with: " + ring.getName());
        
        if (ring.interact("Grand Exchange")) {
            Timer teleportTimer = new Timer(10000);
            Sleep.sleepUntil(() -> {
                // Check if we're at Grand Exchange (approximate area)
                Area geArea = new Area(3144, 3444, 3184, 3484);
                return geArea.contains(Players.getLocal().getTile());
            }, 10000);
            
            Area geArea = new Area(3144, 3444, 3184, 3484);
            if (geArea.contains(Players.getLocal().getTile())) {
                Logger.log("[ItemRetrievalManager] Successfully teleported to Grand Exchange");
                return true;
            }
        }
        
        Logger.log("[ItemRetrievalManager] Failed to teleport with Ring of Wealth");
        return false;
    }
    
    /**
     * Walk to bank as fallback option
     */
    private boolean walkToBank() {
        Logger.log("[ItemRetrievalManager] Walking to bank as fallback");
        
        BankLocation bankLocation = bankLocationRepository.findByName(selectedBankLocation).stream()
            .findFirst().orElse(null);
        
        if (bankLocation == null) {
            // Use default Lumbridge bank
            Logger.log("[ItemRetrievalManager] Using default Lumbridge bank");
            return Walking.walk(3208, 3220, 2); // Lumbridge bank coordinates
        }
        
        return walkToLocation(bankLocation);
    }
    
    /**
     * Walk to a specific location
     */
    private boolean walkToLocation(BankLocation location) {
        Logger.log("[ItemRetrievalManager] Walking to: " + location.getName());
        
        int x = location.getX();
        int y = location.getY();
        int z = location.getZ();
        
        if (Walking.walk(x, y, z)) {
            Timer walkTimer = new Timer(30000);
            Sleep.sleepUntil(() -> {
                Area targetArea = new Area(x - 5, y - 5, x + 5, y + 5);
                return targetArea.contains(Players.getLocal().getTile());
            }, 30000);
            
            Area targetArea = new Area(x - 5, y - 5, x + 5, y + 5);
            return targetArea.contains(Players.getLocal().getTile());
        }
        
        return false;
    }
    
    /**
     * Open bank at current location
     */
    private boolean openBank() {
        if (Bank.isOpen()) {
            return true;
        }
        
        // Try to find and interact with bank booth
        GameObject bankBooth = GameObjects.closest("Bank booth");
        if (bankBooth != null && bankBooth.interact("Bank")) {
            Sleep.sleepUntil(Bank::isOpen, 5000);
            return Bank.isOpen();
        }
        
        // Try to find and interact with banker NPC
        NPC banker = NPCs.closest(npc -> npc != null && 
            (npc.getName().toLowerCase().contains("banker") || 
             npc.getName().toLowerCase().contains("bank")));
        
        if (banker != null && banker.interact("Bank")) {
            Sleep.sleepUntil(Bank::isOpen, 5000);
            return Bank.isOpen();
        }
        
        Logger.log("[ItemRetrievalManager] No bank booth or banker found");
        return false;
    }
    
    /**
     * Check if player is near a bank
     */
    public boolean isNearBank() {
        return GameObjects.closest("Bank booth") != null || 
               NPCs.closest(npc -> npc != null && 
                   (npc.getName().toLowerCase().contains("banker") || 
                    npc.getName().toLowerCase().contains("bank"))) != null;
    }
    
    /**
     * Get current selected bank location
     */
    public String getSelectedBankLocation() {
        return selectedBankLocation;
    }
}
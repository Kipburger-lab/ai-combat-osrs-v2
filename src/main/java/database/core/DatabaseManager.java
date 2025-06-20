package database.core;

import database.repositories.*;
import org.dreambot.api.utilities.Logger;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Central database manager for OSRS game data
 * Handles initialization, loading, and coordination of all data repositories
 */
public class DatabaseManager {
    private static DatabaseManager instance;
    private static final String DATA_PATH = "src/main/java/database/data/";
    
    // Repository instances
    private EquipmentRepository equipmentRepository;
    private NPCRepository npcRepository;
    private ConsumableRepository consumableRepository;
    private LocationRepository locationRepository;
    
    // Loading state
    private boolean isInitialized = false;
    private boolean isLoading = false;
    private ExecutorService loadingExecutor;
    
    private DatabaseManager() {
        this.loadingExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "DatabaseLoader");
            t.setDaemon(true);
            return t;
        });
    }
    
    /**
     * Get singleton instance of DatabaseManager
     */
    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }
    
    /**
     * Initialize database asynchronously
     * @return CompletableFuture that completes when initialization is done
     */
    public CompletableFuture<Void> initializeAsync() {
        if (isInitialized) {
            return CompletableFuture.completedFuture(null);
        }
        
        if (isLoading) {
            Logger.log("Database is already loading...");
            return CompletableFuture.completedFuture(null);
        }
        
        return CompletableFuture.runAsync(() -> {
            try {
                isLoading = true;
                Logger.log("Starting database initialization...");
                
                long startTime = System.currentTimeMillis();
                
                // Verify data directory exists
                File dataDir = new File(DATA_PATH);
                if (!dataDir.exists()) {
                    Logger.error("Database data directory not found: " + DATA_PATH);
                    return;
                }
                
                // Initialize repositories
                initializeRepositories();
                
                // Load data
                loadAllData();
                
                long loadTime = System.currentTimeMillis() - startTime;
                Logger.log("Database initialization completed in " + loadTime + "ms");
                
                isInitialized = true;
                
            } catch (Exception e) {
                Logger.error("Failed to initialize database: " + e.getMessage());
                e.printStackTrace();
            } finally {
                isLoading = false;
            }
        }, loadingExecutor);
    }
    
    /**
     * Initialize database synchronously (blocking)
     */
    public void initialize() {
        try {
            initializeAsync().get();
        } catch (Exception e) {
            Logger.error("Failed to initialize database synchronously: " + e.getMessage());
        }
    }
    
    /**
     * Initialize all repository instances
     */
    private void initializeRepositories() {
        Logger.log("Initializing repositories...");
        
        equipmentRepository = new EquipmentRepository();
        npcRepository = new NPCRepository();
        consumableRepository = new ConsumableRepository();
        locationRepository = new LocationRepository();
        
        Logger.log("Repositories initialized successfully");
    }
    
    /**
     * Load data into all repositories
     */
    private void loadAllData() {
        Logger.log("Loading database data...");
        
        try {
            // Load equipment data
            equipmentRepository.loadData(DATA_PATH + "equipment.json");
            Logger.log("Equipment data loaded: " + equipmentRepository.getCount() + " items");
            
            // Load NPC data
            npcRepository.loadData(DATA_PATH + "npcs.json");
            Logger.log("NPC data loaded: " + npcRepository.getCount() + " NPCs");
            
            // Load consumable data
            consumableRepository.loadData(DATA_PATH + "consumables.json");
            Logger.log("Consumable data loaded: " + consumableRepository.getCount() + " items");
            
            // Load location data
            locationRepository.loadData(DATA_PATH + "locations.json");
            Logger.log("Location data loaded: " + locationRepository.getCount() + " locations");
            
        } catch (Exception e) {
            Logger.error("Failed to load database data: " + e.getMessage());
            throw new RuntimeException("Database loading failed", e);
        }
    }
    
    /**
     * Get equipment repository
     */
    public EquipmentRepository getEquipmentRepository() {
        ensureInitialized();
        return equipmentRepository;
    }
    
    /**
     * Get NPC repository
     */
    public NPCRepository getNPCRepository() {
        ensureInitialized();
        return npcRepository;
    }
    
    /**
     * Get consumable repository
     */
    public ConsumableRepository getConsumableRepository() {
        ensureInitialized();
        return consumableRepository;
    }
    
    /**
     * Get location repository
     */
    public LocationRepository getLocationRepository() {
        ensureInitialized();
        return locationRepository;
    }
    
    /**
     * Check if database is initialized
     */
    public boolean isInitialized() {
        return isInitialized;
    }
    
    /**
     * Check if database is currently loading
     */
    public boolean isLoading() {
        return isLoading;
    }
    
    /**
     * Get database statistics
     */
    public DatabaseStats getStats() {
        if (!isInitialized) {
            return new DatabaseStats(0, 0, 0, 0);
        }
        
        return new DatabaseStats(
            equipmentRepository.getCount(),
            npcRepository.getCount(),
            consumableRepository.getCount(),
            locationRepository.getCount()
        );
    }
    
    /**
     * Ensure database is initialized before accessing repositories
     */
    private void ensureInitialized() {
        if (!isInitialized && !isLoading) {
            Logger.warn("Database not initialized, initializing synchronously...");
            initialize();
        }
    }
    
    /**
     * Shutdown database manager and cleanup resources
     */
    public void shutdown() {
        if (loadingExecutor != null && !loadingExecutor.isShutdown()) {
            loadingExecutor.shutdown();
        }
        Logger.log("Database manager shutdown completed");
    }
    
    /**
     * Database statistics holder
     */
    public static class DatabaseStats {
        public final int equipmentCount;
        public final int npcCount;
        public final int consumableCount;
        public final int locationCount;
        
        public DatabaseStats(int equipmentCount, int npcCount, int consumableCount, int locationCount) {
            this.equipmentCount = equipmentCount;
            this.npcCount = npcCount;
            this.consumableCount = consumableCount;
            this.locationCount = locationCount;
        }
        
        @Override
        public String toString() {
            return String.format("DatabaseStats{equipment=%d, npcs=%d, consumables=%d, locations=%d}",
                equipmentCount, npcCount, consumableCount, locationCount);
        }
    }
}
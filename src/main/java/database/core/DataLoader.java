package database.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.dreambot.api.utilities.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Utility class for loading and parsing JSON data files
 * Handles file I/O, JSON parsing, and error recovery
 */
public class DataLoader {
    private static final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .setLenient()
            .create();
    
    /**
     * Load a list of objects from a JSON file
     * @param filePath Path to the JSON file
     * @param typeToken TypeToken for the list type
     * @param <T> Type of objects in the list
     * @return List of loaded objects, empty list if file doesn't exist or parsing fails
     */
    public static <T> List<T> loadList(String filePath, TypeToken<List<T>> typeToken) {
        try {
            // Try to load from classpath resources first
            InputStream inputStream = DataLoader.class.getClassLoader().getResourceAsStream(filePath);
            String jsonContent;
            
            if (inputStream != null) {
                // Load from classpath resources
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                    jsonContent = content.toString();
                }
            } else {
                // Fallback to file system
                File file = new File(filePath);
                if (!file.exists()) {
                    Logger.warn("Data file not found: " + filePath + ", returning empty list");
                    return List.of();
                }
                jsonContent = Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
            }
            
            if (jsonContent.trim().isEmpty()) {
                Logger.warn("Data file is empty: " + filePath + ", returning empty list");
                return List.of();
            }
            
            Type listType = typeToken.getType();
            List<T> result = gson.fromJson(jsonContent, listType);
            
            if (result == null) {
                Logger.warn("Failed to parse JSON from: " + filePath + ", returning empty list");
                return List.of();
            }
            
            Logger.log("Successfully loaded " + result.size() + " items from " + filePath);
            return result;
            
        } catch (JsonSyntaxException e) {
            Logger.error("JSON syntax error in file " + filePath + ": " + e.getMessage());
            return List.of();
        } catch (IOException e) {
            Logger.error("IO error reading file " + filePath + ": " + e.getMessage());
            return List.of();
        } catch (Exception e) {
            Logger.error("Unexpected error loading file " + filePath + ": " + e.getMessage());
            e.printStackTrace();
            return List.of();
        }
    }
    
    /**
     * Load a single object from a JSON file
     * @param filePath Path to the JSON file
     * @param clazz Class of the object to load
     * @param <T> Type of object to load
     * @return Loaded object, null if file doesn't exist or parsing fails
     */
    public static <T> T loadObject(String filePath, Class<T> clazz) {
        try {
            // Try to load from classpath resources first
            InputStream inputStream = DataLoader.class.getClassLoader().getResourceAsStream(filePath);
            String jsonContent;
            
            if (inputStream != null) {
                // Load from classpath resources
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                    StringBuilder content = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                    jsonContent = content.toString();
                }
            } else {
                // Fallback to file system
                File file = new File(filePath);
                if (!file.exists()) {
                    Logger.warn("Data file not found: " + filePath);
                    return null;
                }
                jsonContent = Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
            }
            
            if (jsonContent.trim().isEmpty()) {
                Logger.warn("Data file is empty: " + filePath);
                return null;
            }
            
            T result = gson.fromJson(jsonContent, clazz);
            
            if (result == null) {
                Logger.warn("Failed to parse JSON from: " + filePath);
                return null;
            }
            
            Logger.log("Successfully loaded object from " + filePath);
            return result;
            
        } catch (JsonSyntaxException e) {
            Logger.error("JSON syntax error in file " + filePath + ": " + e.getMessage());
            return null;
        } catch (IOException e) {
            Logger.error("IO error reading file " + filePath + ": " + e.getMessage());
            return null;
        } catch (Exception e) {
            Logger.error("Unexpected error loading file " + filePath + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * Save a list of objects to a JSON file
     * @param filePath Path to save the JSON file
     * @param data List of objects to save
     * @param <T> Type of objects in the list
     * @return true if save was successful, false otherwise
     */
    public static <T> boolean saveList(String filePath, List<T> data) {
        try {
            // Ensure directory exists
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            String jsonContent = gson.toJson(data);
            Files.writeString(Paths.get(filePath), jsonContent, StandardCharsets.UTF_8);
            
            Logger.log("Successfully saved " + data.size() + " items to " + filePath);
            return true;
            
        } catch (IOException e) {
            Logger.error("IO error writing file " + filePath + ": " + e.getMessage());
            return false;
        } catch (Exception e) {
            Logger.error("Unexpected error saving file " + filePath + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Save a single object to a JSON file
     * @param filePath Path to save the JSON file
     * @param data Object to save
     * @param <T> Type of object to save
     * @return true if save was successful, false otherwise
     */
    public static <T> boolean saveObject(String filePath, T data) {
        try {
            // Ensure directory exists
            File file = new File(filePath);
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            String jsonContent = gson.toJson(data);
            Files.writeString(Paths.get(filePath), jsonContent, StandardCharsets.UTF_8);
            
            Logger.log("Successfully saved object to " + filePath);
            return true;
            
        } catch (IOException e) {
            Logger.error("IO error writing file " + filePath + ": " + e.getMessage());
            return false;
        } catch (Exception e) {
            Logger.error("Unexpected error saving file " + filePath + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Validate that a JSON file exists and is readable
     * @param filePath Path to the JSON file
     * @return true if file exists and is readable, false otherwise
     */
    public static boolean validateFile(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                Logger.warn("File does not exist: " + filePath);
                return false;
            }
            
            if (!file.canRead()) {
                Logger.warn("File is not readable: " + filePath);
                return false;
            }
            
            if (file.length() == 0) {
                Logger.warn("File is empty: " + filePath);
                return false;
            }
            
            // Try to parse as JSON to validate syntax
            String content = Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
            gson.fromJson(content, Object.class);
            
            return true;
            
        } catch (JsonSyntaxException e) {
            Logger.error("Invalid JSON syntax in file " + filePath + ": " + e.getMessage());
            return false;
        } catch (IOException e) {
            Logger.error("IO error validating file " + filePath + ": " + e.getMessage());
            return false;
        } catch (Exception e) {
            Logger.error("Unexpected error validating file " + filePath + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get file size in bytes
     * @param filePath Path to the file
     * @return File size in bytes, -1 if file doesn't exist or error occurs
     */
    public static long getFileSize(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return -1;
            }
            return file.length();
        } catch (Exception e) {
            Logger.error("Error getting file size for " + filePath + ": " + e.getMessage());
            return -1;
        }
    }
    
    /**
     * Create an empty JSON array file if it doesn't exist
     * @param filePath Path to create the file
     * @return true if file was created or already exists, false on error
     */
    public static boolean createEmptyArrayFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                return true;
            }
            
            // Ensure directory exists
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            Files.writeString(Paths.get(filePath), "[]", StandardCharsets.UTF_8);
            Logger.log("Created empty JSON array file: " + filePath);
            return true;
            
        } catch (IOException e) {
            Logger.error("Error creating empty file " + filePath + ": " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Load JSON data from a file (alias for loadList method)
     * This method is used by repository classes for consistency
     * @param filePath Path to the JSON file
     * @param typeToken TypeToken for the list type
     * @param <T> Type of objects in the list
     * @return List of loaded objects, empty list if file doesn't exist or parsing fails
     */
    public static <T> List<T> loadJsonData(String filePath, TypeToken<List<T>> typeToken) {
        return loadList(filePath, typeToken);
    }
    
    /**
     * Load data from a file (generic method for repository compatibility)
     * @param filename Name of the file to load
     * @return Raw string content of the file, empty string if error occurs
     */
    public static String loadData(String filename) {
        try {
            // Try to load from classpath resources first
            InputStream inputStream = DataLoader.class.getClassLoader().getResourceAsStream(filename);
            String content;
            
            if (inputStream != null) {
                // Load from classpath resources
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                    StringBuilder contentBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        contentBuilder.append(line).append("\n");
                    }
                    content = contentBuilder.toString();
                }
            } else {
                // Fallback to file system
                File file = new File(filename);
                if (!file.exists()) {
                    Logger.warn("Data file not found: " + filename);
                    return "";
                }
                content = Files.readString(Paths.get(filename), StandardCharsets.UTF_8);
            }
            Logger.log("Successfully loaded data from " + filename);
            return content;
            
        } catch (IOException e) {
            Logger.error("IO error reading file " + filename + ": " + e.getMessage());
            return "";
        } catch (Exception e) {
            Logger.error("Unexpected error loading file " + filename + ": " + e.getMessage());
            return "";
        }
    }
}
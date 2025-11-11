package com.writer;

import java.io.*;
import java.util.*;
import com.reader.LabConnectUtil;

/**
 * Configuration class for patient name formatting rules
 * Loads prefixes to be truncated from properties file
 */
public class PatientNameConfig {
    
    private static List<String> prefixesToRemove = new ArrayList<String>();
    private static boolean initialized = false;
    
    // Default prefixes if properties file is not available
    private static final String[] DEFAULT_PREFIXES = {
        "Baby",
        "Baby boy of",
        "Baby girl of",
        "BABY.",
        "Dr.",
        "MASTER",
        "Master.",
        "MISS.",
        "MR.",
        "MRS",
        "Mrs.",
        "MS",
        "Ms.",
        "MX."
    };
    
    /**
     * Initialize and load prefixes from properties file
     */
    public static synchronized void initialize() {
        if (initialized) {
            return;
        }
        
        try {
            String rootDrive = "D://";
            if (System.getProperty("rootDrive") != null) {
                rootDrive = System.getProperty("rootDrive");
            }
            
            String propertyFileSuffix = "VITEK2COMPACT";
            if (System.getProperty("propertyFileSuffix") != null) {
                propertyFileSuffix = System.getProperty("propertyFileSuffix");
            }
            
            String configPath = rootDrive + File.separator + "mednet" + File.separator + 
                               "windowsService" + propertyFileSuffix + ".properties";
            
            File configFile = new File(configPath);
            
            if (configFile.exists()) {
                loadPrefixesFromFile(configPath);
                log("PatientNameConfig: Loaded prefixes from " + configPath);
            } else {
                loadDefaultPrefixes();
                log("PatientNameConfig: Config file not found, using default prefixes");
            }
            
        } catch (Exception ex) {
            log("PatientNameConfig: Error loading config: " + ex.getMessage());
            loadDefaultPrefixes();
        }
        
        initialized = true;
    }
    
    /**
     * Load prefixes from properties file
     */
    private static void loadPrefixesFromFile(String configPath) {
        try {
            ResourceBundle bundle = new PropertyResourceBundle(new FileInputStream(configPath));
            
            // Load comma-separated list of prefixes
            if (bundle.containsKey("patientNamePrefixesToRemove")) {
                String prefixesStr = bundle.getString("patientNamePrefixesToRemove");
                String[] prefixes = prefixesStr.split(",");
                
                prefixesToRemove.clear();
                for (String prefix : prefixes) {
                    String trimmed = prefix.trim();
                    if (!trimmed.isEmpty()) {
                        prefixesToRemove.add(trimmed);
                    }
                }
                
                log("PatientNameConfig: Loaded " + prefixesToRemove.size() + " prefixes from config");
            } else {
                loadDefaultPrefixes();
                log("PatientNameConfig: Property 'patientNamePrefixesToRemove' not found, using defaults");
            }
            
        } catch (Exception ex) {
            log("PatientNameConfig: Error reading prefixes: " + ex.getMessage());
            loadDefaultPrefixes();
        }
    }
    
    /**
     * Load default prefixes
     */
    private static void loadDefaultPrefixes() {
        prefixesToRemove.clear();
        prefixesToRemove.addAll(Arrays.asList(DEFAULT_PREFIXES));
        log("PatientNameConfig: Loaded " + prefixesToRemove.size() + " default prefixes");
    }
    
    /**
     * Get list of prefixes to remove from patient names
     */
    public static List<String> getPrefixesToRemove() {
        if (!initialized) {
            initialize();
        }
        return new ArrayList<String>(prefixesToRemove);
    }
    
    /**
     * Remove configured prefixes from patient name
     * Checks for prefixes at the beginning of the name (case-insensitive)
     */
    public static String removePrefixes(String name) {
        if (!initialized) {
            initialize();
        }
        
        if (name == null || name.trim().isEmpty()) {
            return name;
        }
        
        String result = name.trim();
        
        // Sort prefixes by length (longest first) to handle overlapping prefixes
        // e.g., "Baby boy of" should be checked before "Baby"
        List<String> sortedPrefixes = new ArrayList<String>(prefixesToRemove);
        sortedPrefixes.sort((a, b) -> Integer.compare(b.length(), a.length()));
        
        // Keep removing prefixes until no more matches
        boolean changed = true;
        while (changed) {
            changed = false;
            String originalResult = result;
            
            for (String prefix : sortedPrefixes) {
                // Case-insensitive check at the start of the name
                if (result.toLowerCase().startsWith(prefix.toLowerCase())) {
                    result = result.substring(prefix.length()).trim();
                    changed = true;
                    log("PatientNameConfig: Removed prefix '" + prefix + "' from name");
                    break; // Start over with the new result
                }
            }
            
            // Safety check to prevent infinite loop
            if (result.isEmpty() || result.equals(originalResult)) {
                break;
            }
        }
        
        return result;
    }
    
    /**
     * Reload configuration (useful for testing or runtime updates)
     */
    public static synchronized void reload() {
        initialized = false;
        prefixesToRemove.clear();
        initialize();
    }
    
    /**
     * Get count of configured prefixes
     */
    public static int getPrefixCount() {
        if (!initialized) {
            initialize();
        }
        return prefixesToRemove.size();
    }
    
    /**
     * Log messages
     */
    private static void log(String message) {
        LabConnectUtil.log(message);
    }
} 
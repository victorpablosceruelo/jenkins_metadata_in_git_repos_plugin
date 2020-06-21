/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.gwt.metadataInGitRepoPlugin.helpers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author me
 */
public class FilesFinder {

    private static final String KEY_METADATA_REPO_FOLDER_PATH = "METADATA_REPO_FOLDER_PATH";
    
    private static final String KEY_EXTRA_CFG_FILE_PATH = "EXTRA_CFG_FILE_PATH";
    
    private static final String KEY_EXTRA_CFG_FILE_CONTENTS = "EXTRA_CFG_FILE_CONTENTS";
    
    private static final String CFG_FILE = "config.properties";
    
    private static final String EXTRA_CFG_FILE = "extraCfg.json";
    
    private static final Logger LOGGER = Logger.getLogger(FilesFinder.class.getName());
    
    private static final String REG_EXP = File.separator;
    
    public static Map<String, String>  getResolvedVariables(final String jobName, OneTimeLogger oneTimeLogger) {
    
        final String repoPath = GitRepoManager.GIT_REPOSITORY_LOCAL_PATH;
        
        StringBuilder sbMsg = new StringBuilder();
        sbMsg.append("getResolvedVariables: repoPath: ").append(repoPath);
        sbMsg.append(" jobName: ").append(jobName);
        LOGGER.warning(sbMsg.toString());
        
        Map<String, String> resolvedVariables = new HashMap<>();
        resolvedVariables = addMetadataRepoPath(repoPath, resolvedVariables, oneTimeLogger);
        
        String[] jobNameArray = jobName.split(REG_EXP);
        
        // Load repository config file (at the repository root).
        final String repoConfigFileName = repoPath + File.separator + CFG_FILE;
        resolvedVariables = loadConfigFile(repoConfigFileName, resolvedVariables, oneTimeLogger);
        
        resolvedVariables = loadConfigFilesRecursively(repoPath, jobNameArray, resolvedVariables, oneTimeLogger);
        
        return resolvedVariables;
    }
    
    private static Map<String, String> loadConfigFilesRecursively(String repoPath, String [] jobNameArray, Map<String, String> resolvedVariables, OneTimeLogger oneTimeLogger) {

        if (jobNameArray.length < 1) {
            return resolvedVariables;
        }
        if ((jobNameArray[0] == null) || (jobNameArray[0].isEmpty())) {
            return resolvedVariables;
        }        
        
        final String repoPathWithHead = repoPath + File.separator + jobNameArray[0];
        
        final String configFileName = repoPathWithHead + File.separator + CFG_FILE;
        
        final String extraConfigFileName = repoPathWithHead + File.separator + EXTRA_CFG_FILE;
        
        resolvedVariables = loadConfigFile(configFileName, resolvedVariables, oneTimeLogger);
        
        resolvedVariables = addAsVariableExtraConfigFileIfExists(extraConfigFileName, resolvedVariables, oneTimeLogger);
        
        String [] jobNameArrayTail = Arrays.copyOfRange(jobNameArray, 1, jobNameArray.length);
        
        
        
        return loadConfigFilesRecursively(repoPathWithHead, jobNameArrayTail, resolvedVariables, oneTimeLogger);
    }
    
    private static Map<String, String> loadConfigFile(String configFileName, Map<String, String> resolvedVariables, OneTimeLogger oneTimeLogger) {
        
        if (! configFileExists(configFileName)) {
            oneTimeLogger.println("Not found config file " + configFileName);
            return resolvedVariables;
        }

        InputStream is = null;
        try {
            oneTimeLogger.println("Loading cfg in config file " + configFileName);
            is = new FileInputStream(configFileName);
            return loadConfigFileAux(is, resolvedVariables, oneTimeLogger);
        } catch (Exception e) {
            StringBuilder sbMsg = new StringBuilder();
            sbMsg.append("Error reading config file ").append(configFileName);
            sbMsg.append(": ").append(e.getMessage());
                    
            oneTimeLogger.println(sbMsg.toString());
            LOGGER.log(Level.SEVERE, sbMsg.toString(), e);
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                StringBuilder sbMsg = new StringBuilder();
                sbMsg.append("Error closing config file ").append(configFileName);
                sbMsg.append(": ").append(e.getMessage());

                oneTimeLogger.println(sbMsg.toString());
                LOGGER.log(Level.SEVERE, sbMsg.toString(), e);
            }
        }
        return resolvedVariables;
    }

    private static boolean configFileExists(String configFileName) {

        File configFile = new File(configFileName);
        
        if (! configFile.exists()) {
            return false;
        }
        
        if (! configFile.isFile()) {
            return false;
        }
        
        if (!configFile.canRead()) {
            return false;
        }
        
        return true;
    }
    
    private static Map<String, String> loadConfigFileAux(InputStream is, Map<String, String> resolvedVariables, OneTimeLogger oneTimeLogger) 
                    throws FileNotFoundException, IOException {
        

        Properties prop = new Properties();
        prop.load(is);
        
        for (Map.Entry<Object, Object> entry : prop.entrySet()) {
            Object keyObj = entry.getKey();
            Object valueObj = entry.getValue();
            
            if ((keyObj instanceof String) && (valueObj instanceof String)) {
                String key = (String) keyObj;
                String value = (String) valueObj;
                String cleanedValue = cleanStringValue(value);
            
                StringBuilder sbMsg = new StringBuilder();
                sbMsg.append("    ").append(key).append(" = ").append(value);
                sbMsg.append(" -> ").append(cleanedValue);
                sbMsg.append(" (").append(cleanedValue).append(").");
                oneTimeLogger.println(sbMsg.toString());
                
                resolvedVariables.put(key, cleanedValue);
                
            } else {
                StringBuilder sbMsg = new StringBuilder();
                sbMsg.append("Could not read pair key: ").append(keyObj);
                sbMsg.append(" value: ").append(valueObj);
                oneTimeLogger.println(sbMsg.toString());
            }
        }
        
        return resolvedVariables;
    }
    
    private static String cleanStringValue(String value) {
        value = value.trim();
        char first = value.charAt(0);
        char last = value.charAt(value.length() -1);
        
        if (('\"' == first) && ('\"' == last)) {
            value = value.substring(1, (value.length() -1));
        }
        return value;
    }
    
    private static Map<String, String> addMetadataRepoPath(String repoPath, Map<String, String> resolvedVariables, OneTimeLogger oneTimeLogger) {
        final String key = KEY_METADATA_REPO_FOLDER_PATH;
        final String value = repoPath;
            
        StringBuilder sbMsg = new StringBuilder();
        sbMsg.append("    ").append(key).append(" = ").append(value);
        oneTimeLogger.println(sbMsg.toString());
                
        resolvedVariables.put(key, value);
        
        return resolvedVariables;
    }

    private static Map<String, String> addAsVariableExtraConfigFileIfExists(String extraConfigFileName, Map<String, String> resolvedVariables, 
            OneTimeLogger oneTimeLogger) {
         
        if (configFileExists(extraConfigFileName)) {
            final String key = KEY_EXTRA_CFG_FILE_PATH;
            final String value = extraConfigFileName;
            
            StringBuilder sbMsg = new StringBuilder();
            sbMsg.append("    ").append(key).append(" = ").append(value);
            oneTimeLogger.println(sbMsg.toString());
                
            resolvedVariables.put(key, value);
            
            String configFileContents = getConfigFileContents(extraConfigFileName, oneTimeLogger);
            resolvedVariables.put(KEY_EXTRA_CFG_FILE_CONTENTS, configFileContents);
        }
        
        return resolvedVariables;
    }
    
    private static String getConfigFileContents(String extraConfigFileName, OneTimeLogger oneTimeLogger) {

        String exceptionMsg = "EXCEPTION READING FILE " + extraConfigFileName;
        StringBuilder contentBuilder = new StringBuilder();
        FileInputStream is = null;
        InputStreamReader isr = null;
        BufferedReader buffReader = null;
        try 
        {
            is = new FileInputStream(extraConfigFileName);
            isr = new InputStreamReader(is, Charset.forName("UTF8"));
            buffReader = new BufferedReader(isr);
 
            String sCurrentLine;
            while ((sCurrentLine = buffReader.readLine()) != null) 
            {
                contentBuilder.append(sCurrentLine);
            }
            
        } catch (IOException e) {
            
            oneTimeLogger.println("----------------------");
            oneTimeLogger.println(exceptionMsg);
            oneTimeLogger.println(e.getMessage());
            LOGGER.log(Level.SEVERE, exceptionMsg, e);
            oneTimeLogger.println("----------------------");
            
            return null;
            
        } finally {
            try {
                if (buffReader != null) {
                    buffReader.close();
                }
            } catch (IOException e2) {
                LOGGER.log(Level.SEVERE, exceptionMsg, e2);
            }
            
            try {
                if (isr != null) {
                    isr.close();
                }
            } catch (IOException e2) {
                LOGGER.log(Level.SEVERE, exceptionMsg, e2);
            }
            
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e2) {
                LOGGER.log(Level.SEVERE, exceptionMsg, e2);
            }
        }
        return contentBuilder.toString();
    }
}

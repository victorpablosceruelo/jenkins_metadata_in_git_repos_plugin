/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.gwt.metadataInGitRepoPlugin.helpers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 *
 * @author me
 */
public class FilesFinder {

    private static final String CFG_FILE = "config.properties";
    
    private static final Logger LOGGER = Logger.getLogger(FilesFinder.class.getName());
    
    private static final String REG_EXP = File.separator;
    
    public static Map<String, String>  getResolvedVariables(final String jobName, PrintStream jobLogger) {
    
        final String repoPath = GitRepoManager.GIT_REPOSITORY_LOCAL_PATH;
        
        StringBuilder sbMsg = new StringBuilder();
        sbMsg.append("getResolvedVariables: repoPath: ").append(repoPath);
        sbMsg.append(" jobName: ").append(jobName);
        LOGGER.warning(sbMsg.toString());
        
        Map<String, String> resolvedVariables = new HashMap<>();
        
        String[] jobNameArray = jobName.split(REG_EXP);
        
        resolvedVariables = loadConfigFilesRecursively(repoPath, jobNameArray, resolvedVariables, jobLogger);
        
        return resolvedVariables;
    }
    
    private static Map<String, String> loadConfigFilesRecursively(String repoPath, String [] jobNameArray, Map<String, String> resolvedVariables, PrintStream jobLogger) {

        if (jobNameArray.length < 1) {
            return resolvedVariables;
        }
        if ((jobNameArray[0] == null) || (jobNameArray[0].isEmpty())) {
            return resolvedVariables;
        }        
        
        String configFileName = repoPath + File.separator + jobNameArray[0] + File.separator + CFG_FILE;
        
        resolvedVariables = loadConfigFile(configFileName, resolvedVariables, jobLogger);
        
        String [] jobNameArrayTail = Arrays.copyOfRange(jobNameArray, 1, jobNameArray.length);
        
        String repoPathWithHead = repoPath + File.separator + jobNameArray[0];
        
        return loadConfigFilesRecursively(repoPathWithHead, jobNameArrayTail, resolvedVariables, jobLogger);
    }
    
    private static Map<String, String> loadConfigFile(String configFileName, Map<String, String> resolvedVariables, PrintStream jobLogger) {
        
        if (! configFileExists(configFileName)) {
            jobLogger.println("Not found config file " + configFileName);
            return resolvedVariables;
        }

        InputStream is = null;
        try {
            jobLogger.println("Loading cfg in config file " + configFileName);
            is = new FileInputStream(configFileName);
            return loadConfigFileAux(is, resolvedVariables, jobLogger);
        } catch (Exception e) {
            e.printStackTrace(jobLogger);
            
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex2) {
                ex2.printStackTrace(jobLogger);
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
    
    private static Map<String, String> loadConfigFileAux(InputStream is, Map<String, String> resolvedVariables, PrintStream jobLogger) 
                    throws FileNotFoundException, IOException {
        

        Properties prop = new Properties();
        prop.load(is);
        
        for (Map.Entry<Object, Object> entry : prop.entrySet()) {
            Object keyObj = entry.getKey();
            Object valueObj = entry.getValue();
            
            if ((keyObj instanceof String) && (valueObj instanceof String)) {
                String key = (String) keyObj;
                String value = (String) valueObj;
            
                StringBuilder sbMsg = new StringBuilder();
                sbMsg.append(" key: ").append(key);
                sbMsg.append(" value: ").append(value);
                jobLogger.println(sbMsg.toString());
                
                resolvedVariables.put(key, value);
                
            } else {
                StringBuilder sbMsg = new StringBuilder();
                sbMsg.append("Could not read pair key: ").append(keyObj);
                sbMsg.append(" value: ").append(valueObj);
                jobLogger.println(sbMsg.toString());
            }
        }
        
        return resolvedVariables;
    }
}

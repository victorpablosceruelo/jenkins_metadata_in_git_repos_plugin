/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.gwt.metadataInGitRepoPlugin.helpers;

import com.google.common.base.Charsets;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nonnull;

/**
 *
 * @author me
 */
public class OneTimeLogger {
    
    private static final Logger LOGGER = Logger.getLogger(OneTimeLogger.class.getName());
    
    private static final String MSGS_PREFIX = "Metadata in Git Repos Plugin: ";
    private static final String MY_MSG = OneTimeLogger.class.getCanonicalName();
            
            
    public static OneTimeLogger getInstance(@Nonnull final Run r, @Nonnull final TaskListener listener) {
        OneTimeLogger oneTimeLogger = new OneTimeLogger(r, listener);
        oneTimeLogger.println(" ");
        oneTimeLogger.println(MY_MSG);
        return oneTimeLogger;
    }
    
    private final PrintStream jobLogger;
    private final boolean isAllowedToPrint;
    
    private OneTimeLogger(@Nonnull final Run r, @Nonnull final TaskListener listener) {
        this.jobLogger = listener.getLogger();
        this.isAllowedToPrint = getIsAllowedToPrint(r);
    }
    
    private static boolean getIsAllowedToPrint(@Nonnull final Run r) {
        
        InputStreamReader isR = null;
        BufferedReader br = null;
        try {
            isR = new InputStreamReader(r.getLogInputStream(), Charsets.UTF_8);
            br = new BufferedReader(isR);
         
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(MSGS_PREFIX + MY_MSG)) {
                    return false;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error in getIsAllowedToPrint", e);
            return true;
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error in getIsAllowedToPrint", e);
            }
            try {
                if (isR != null) {
                    isR.close();
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error in getIsAllowedToPrint", e);
            }
        }
        return true;
    }
    
    public void println(String msg) {
        if (this.isAllowedToPrint) {
            this.jobLogger.println(MSGS_PREFIX + msg);
        }
    }
    
}

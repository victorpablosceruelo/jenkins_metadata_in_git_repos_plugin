/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jenkinsci.plugins.gwt.metadataInGitRepoPlugin.helpers;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author me
 */
public class FilesFinder {

    private static final Logger LOGGER = Logger.getLogger(FilesFinder.class.getName());
    
    public static Map<String, String>  getResolvedVariables(final String repoPath, final String jobName) {
        
        LOGGER.warning("getResolvedVariables: repoPath: " + repoPath + " jobName: " + jobName);
        
        Map<String, String> resolvedVariables = new HashMap<>();
        
        return resolvedVariables;
    }

}

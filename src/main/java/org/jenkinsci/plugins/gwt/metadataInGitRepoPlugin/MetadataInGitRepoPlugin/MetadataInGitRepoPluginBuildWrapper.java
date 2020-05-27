package org.jenkinsci.plugins.gwt.metadataInGitRepoPlugin.MetadataInGitRepoPlugin;

import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.BuildVariableContributor;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.Nonnull;


/**
 * BuildWrapper to set environment variables from a file.
 * 
 * @author Anders Johansson
 *
 */
@Extension
public class MetadataInGitRepoPluginBuildWrapper extends BuildVariableContributor {

    private static final Logger LOGGER = Logger.getLogger(MetadataInGitRepoPluginBuildWrapper.class.getName());
    
    @Override
    public void buildVariablesFor(@Nonnull AbstractBuild build, @Nonnull Map<String, String> variablesOut) {
        
        LOGGER.warning("buildVariablesFor");
        
        ParametersAction parameters = build.getAction(ParametersAction.class);
        //Only for a parameterized job
        if (parameters != null) {
            Map<String, String> nodeEnvVars = new HashMap<String, String>();
            Map<String, String> injectedEnvVars = new HashMap<>();
            
            for (ParameterValue p : parameters) {
                String key = p.getName();
                if (injectedEnvVars.containsKey(key) && !nodeEnvVars.containsKey(key)) {
                    variablesOut.put(key, injectedEnvVars.get(key));
                }
            }
        }
    }
}

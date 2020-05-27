package org.jenkinsci.plugins.gwt.metadataInGitRepoPlugin.MetadataInGitRepoPlugin;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.EnvironmentContributor;
import hudson.model.ParameterValue;
import hudson.model.ParametersAction;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.io.IOException;
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
public class MetadataInGitRepoPluginBuildWrapper extends EnvironmentContributor {

    private static final Logger LOGGER = Logger.getLogger(MetadataInGitRepoPluginBuildWrapper.class.getName());
    

    @Override
    public void buildEnvironmentFor(
        @SuppressWarnings("rawtypes") @Nonnull final Run r,
        @Nonnull final EnvVars envs,
        @Nonnull final TaskListener listener)
        throws IOException, InterruptedException {
        
        LOGGER.warning("buildVariablesFor");
        
    }
}

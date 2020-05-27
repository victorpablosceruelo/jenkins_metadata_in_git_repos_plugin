package org.jenkinsci.plugins.gwt.metadataInGitRepoPlugin.MetadataInGitRepoPlugin;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.Run;
import hudson.model.TaskListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.Nonnull;
import org.jenkinsci.plugins.gwt.metadataInGitRepoPlugin.global.MetadataInGitRepoPluginData;
import org.jenkinsci.plugins.gwt.metadataInGitRepoPlugin.helpers.FilesFinder;
import org.jenkinsci.plugins.gwt.metadataInGitRepoPlugin.helpers.GitRepoManager;


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
        
        MetadataInGitRepoPluginData data = MetadataInGitRepoPluginData.get();
        
        final String repoUrl = data.getMetadataRepositoryUrl();
        final String gitRepoUsername = data.getGitRepoUsername();
        final String gitRepoPassword = data.getGitRepoPassword();
        final String repoPath = GitRepoManager.updateLocalRepoIfNeedTo(repoUrl, gitRepoUsername, gitRepoPassword);
        
        final String jobName = envs.get("JOB_NAME", "");
        final Map<String, String> resolvedVariables = FilesFinder.getResolvedVariables(repoPath, jobName);
        
        listener.getLogger().println("\nVariables in environment: ");
        for (final Map.Entry<String, String> entry : envs.entrySet()) {
            listener.getLogger().println("    " + entry.getKey() + " = " + entry.getValue());
        }
        
        listener.getLogger().println("\nVariables set from metadata: ");
        for (final Map.Entry<String, String> entry : resolvedVariables.entrySet()) {

            listener.getLogger().println("    " + entry.getKey() + " = " + entry.getValue());
            envs.override(entry.getKey(), entry.getValue());
        }
        listener.getLogger().println("\n");
        
    }
}

package org.jenkinsci.plugins.gwt.metadataInGitRepoPlugin.global;

import com.google.common.annotations.VisibleForTesting;
import hudson.Extension;
import java.io.Serializable;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

@Extension
public class MetadataInGitRepoPluginData extends GlobalConfiguration implements Serializable {

  private static final long serialVersionUID = -1;

  public static MetadataInGitRepoPluginData get() {
    return GlobalConfiguration.all().get(MetadataInGitRepoPluginData.class);
  }

  private String metadataRepositoryUrl;
  private String gitRepoUsername;
  private String gitRepoPassword;

  @VisibleForTesting
  public MetadataInGitRepoPluginData(final String metadataRepositoryUrl) {

      this.metadataRepositoryUrl = metadataRepositoryUrl;
  }

  public MetadataInGitRepoPluginData() {
    load();
  }

  @Override
  public boolean configure(final StaplerRequest req, final JSONObject json) throws FormException {
    cleanUpInstanceValues();
    req.bindJSON(this, json);
    save();
    return true;
  }

  private void cleanUpInstanceValues() {
    setMetadataRepositoryUrl("");
    setGitRepoUsername("");
    setGitRepoPassword("");
  }


  @DataBoundSetter
  public void setMetadataRepositoryUrl(final String metadataRepositoryUrl) {
    this.metadataRepositoryUrl = metadataRepositoryUrl;
  }

  public String getMetadataRepositoryUrl() {
    return metadataRepositoryUrl;
  }

  @DataBoundSetter
  public void setGitRepoUsername(final String gitRepoUsername) {
    this.gitRepoUsername = gitRepoUsername;
  }

  public String getGitRepoUsername() {
    return gitRepoUsername;
  }
  
  @DataBoundSetter
  public void setGitRepoPassword(final String gitRepoPassword) {
    this.gitRepoPassword = gitRepoPassword;
  }

  public String getGitRepoPassword() {
    return gitRepoPassword;
  }
  
}

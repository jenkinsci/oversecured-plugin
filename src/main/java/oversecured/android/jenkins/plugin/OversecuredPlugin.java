package oversecured.android.jenkins.plugin;

import hudson.Launcher;
import hudson.Extension;
import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.tasks.BuildStepDescriptor;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import com.google.common.base.Strings;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import jenkins.tasks.SimpleBuildStep;
import oversecured.android.Uploader;

import org.jenkinsci.Symbol;

public class OversecuredPlugin extends Builder implements SimpleBuildStep {
    private String integrationId;
    private String branchName;
    private String appPath;

    @DataBoundConstructor
    public OversecuredPlugin(String integrationId, String branchName, String appPath) {
        this.integrationId = integrationId;
        this.branchName = branchName;
        this.appPath = appPath;
    }
    
    public String getAppPath() {
        return appPath;
    }
    
    public String getIntegrationId() {
        return integrationId;
    }
    
    public String getBranchName() {
        return branchName;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        File app = new File(getAppPath());
        if(!app.exists() || !app.isFile()) {
            throw new FileNotFoundException(appPath);
        }
        String apiKey = run.getEnvironment(listener).get("oversecuredApiKey");
        if(Strings.isNullOrEmpty(apiKey)) {
            throw new IllegalStateException(Messages.OversecuredPlugin_DescriptorImpl_errors_missingApiKey());
        }
        Uploader uploader = new Uploader(apiKey, integrationId, branchName);
        uploader.setLogger(listener.getLogger());
        uploader.upload(app);
    }

    @Symbol("oversecuredUpload")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        public FormValidation doCheckIntegrationId(@QueryParameter String integrationId) {
            if (Strings.isNullOrEmpty(integrationId)) {
                return FormValidation.errorWithMarkup(Messages.OversecuredPlugin_DescriptorImpl_errors_missingIntegrationId());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckBranchName(@QueryParameter String branchName) {
            if (Strings.isNullOrEmpty(branchName)) {
                return FormValidation.errorWithMarkup(Messages.OversecuredPlugin_DescriptorImpl_errors_missingBranchName());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckAppPath(@QueryParameter String appPath) {
            if (Strings.isNullOrEmpty(appPath)) {
                return FormValidation.errorWithMarkup(Messages.OversecuredPlugin_DescriptorImpl_errors_missingAppPath());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckOversecuredApiKey() {
            return FormValidation.ok(Messages.OversecuredPlugin_DescriptorImpl_apiKey());
        }

        @SuppressWarnings("rawtypes")
        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.OversecuredPlugin_DescriptorImpl_DisplayName();
        }
    }
}
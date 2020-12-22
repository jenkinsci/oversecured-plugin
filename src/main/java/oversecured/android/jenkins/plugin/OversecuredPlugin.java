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
    private String apkPath;
    private String integrationId;

    @DataBoundConstructor
    public OversecuredPlugin(String apkPath, String integrationId) {
        this.apkPath = apkPath;
        this.integrationId = integrationId;
    }
    
    public String getApkPath() {
        return apkPath;
    }
    
    public String getIntegrationId() {
        return integrationId;
    }

    @Override
    public void perform(Run<?, ?> run, FilePath workspace, Launcher launcher, TaskListener listener) throws InterruptedException, IOException {
        File app = new File(getApkPath());
        if(!app.exists() || !app.isFile()) {
            throw new FileNotFoundException(apkPath);
        }
        String apiKey = run.getEnvironment(listener).get("apiKey");
        if(Strings.isNullOrEmpty(apiKey)) {
            throw new IllegalStateException(Messages.OversecuredPlugin_DescriptorImpl_errors_missingApiKey());
        }
        Uploader uploader = new Uploader(apiKey, integrationId);
        uploader.setLogger(listener.getLogger());
        uploader.upload(app);
    }

    @Symbol("oversecuredUpload")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
        public FormValidation doCheckApkPath(@QueryParameter String apkPath) {
            if (Strings.isNullOrEmpty(apkPath)) {
                return FormValidation.errorWithMarkup(Messages.OversecuredPlugin_DescriptorImpl_errors_missingApkPath());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckIntegrationId(@QueryParameter String integrationId) {
            if (Strings.isNullOrEmpty(integrationId)) {
                return FormValidation.errorWithMarkup(Messages.OversecuredPlugin_DescriptorImpl_errors_missingIntegrationId());
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckApiKey() {
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
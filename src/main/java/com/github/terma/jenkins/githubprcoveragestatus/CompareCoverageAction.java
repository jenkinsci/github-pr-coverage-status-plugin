/*

    Copyright 2015-2016 Artem Stasiuk

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/
package com.github.terma.jenkins.githubprcoveragestatus;

import com.google.common.base.Strings;

import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;

import org.kohsuke.github.GHRepository;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundSetter;

@SuppressWarnings({"unused", "WeakerAccess"})
public class CompareCoverageAction extends Recorder implements SimpleBuildStep {

    private String sonarLogin;
    private String sonarPassword;
    private String changeId;
    private String githubUrl;

    @DataBoundConstructor
    public CompareCoverageAction() {
    }

    // todo show message that addition comment in progress as it could take a while
    @SuppressWarnings("NullableProblems")
    @Override
    public void perform(
            final Run build, final FilePath workspace, final Launcher launcher,
            final TaskListener listener) throws InterruptedException, IOException {
        final PrintStream buildLog = listener.getLogger();

        if (build.getResult() != Result.SUCCESS) {
            buildLog.println("Not running coverage plugin, build was not success");
            return;
        }


        buildLog.println("Starting comparison of coverage.");
        final String gitUrl = Strings.isNullOrEmpty(githubUrl) ? Utils.getGitUrl(build, listener) : this.githubUrl;
        final Integer prId = Strings.isNullOrEmpty(changeId) ? Utils.gitPrId(build, listener) : Integer.parseInt(this.changeId);
        buildLog.println("Id to be compared "+prId);
        if (prId == null) {
            throw new UnsupportedOperationException(
                    "Can't find " + Utils.GIT_PR_ID_ENV_PROPERTY + " please use " +
                            "https://wiki.jenkins-ci.org/display/JENKINS/GitHub+pull+request+builder+plugin " +
                            "to trigger build!");
        }
        final GHRepository gitHubRepository = ServiceRegistry.getPullRequestRepository(buildLog).getGitHubRepository(gitUrl);

        final float masterCoverage = ServiceRegistry.getMasterCoverageRepository(buildLog, sonarLogin, sonarPassword).get(gitHubRepository.getName());
        final float coverage = ServiceRegistry.getCoverageRepository().get(workspace);

        final Message message = new Message(coverage, masterCoverage);
        buildLog.println(message.forConsole());

        final String buildUrl = Utils.getBuildUrl(build, listener);

        String jenkinsUrl = ServiceRegistry.getSettingsRepository().getJenkinsUrl();
        if (jenkinsUrl == null) jenkinsUrl = Utils.getJenkinsUrlFromBuildUrl(buildUrl);

        final SettingsRepository settingsRepository = ServiceRegistry.getSettingsRepository();

        try {
            final String comment = message.forComment(
                    buildUrl,
                    jenkinsUrl,
                    settingsRepository.getYellowThreshold(),
                    settingsRepository.getGreenThreshold(),
                    settingsRepository.isPrivateJenkinsPublicGitHub());
            ServiceRegistry.getPullRequestRepository(buildLog).comment(gitHubRepository, prId, comment);
        } catch (Exception ex) {
            PrintWriter pw = listener.error("Couldn't add comment to pull request #" + prId + "!");
            ex.printStackTrace(pw);
            throw new UnsupportedOperationException(ex.getMessage());
        }
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        @Override
        public String getDisplayName() {
            return "Publish coverage to GitHub";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

    }


    @DataBoundSetter
    public void setSonarLogin(String sonarLogin) {
        this.sonarLogin = sonarLogin;
    }

    public String getSonarLogin() {
        return sonarLogin;
    }

    @DataBoundSetter
    public void setSonarPassword(String sonarPassword) {
        this.sonarPassword = sonarPassword;
    }

    @DataBoundSetter
    public void setChangeId(String changeId) {
        this.changeId = changeId;
    }

    @DataBoundSetter
    public void setGithubUrl(String githubUrl) {
        this.githubUrl = githubUrl;
    }

}

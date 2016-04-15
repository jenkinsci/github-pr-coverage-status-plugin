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
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.io.PrintStream;

@SuppressWarnings({"unused", "WeakerAccess"})
public class CompareCoverageAction extends Recorder implements SimpleBuildStep {

    private static final long serialVersionUID = 1L;

    @DataBoundConstructor
    public CompareCoverageAction() {
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public void perform(
            final Run build, final FilePath workspace, final Launcher launcher,
            final TaskListener listener) throws InterruptedException, IOException {
        if (build.getResult() != Result.SUCCESS) return;

        final PrintStream buildLog = listener.getLogger();

        final String gitUrl = Utils.getGitUrl(build, listener);
        final Integer prId = Utils.gitPrId(build, listener);

        if (prId == null) {
            throw new UnsupportedOperationException(
                    "Can't find " + Utils.GIT_PR_ID_ENV_PROPERTY + " please use " +
                            "https://wiki.jenkins-ci.org/display/JENKINS/GitHub+pull+request+builder+plugin" +
                            "to trigger build!");
        }


        final float masterCoverage = Configuration.getMasterCoverage(gitUrl);
        final float coverage = GetCoverageCallable.get(workspace);

        final Message message = new Message(coverage, masterCoverage);
        buildLog.println(message.forConsole());

        final String buildUrl = Utils.getBuildUrl(build, listener);

        try {
            final GHPullRequest pr = new CachedGitHubRepository().getPullRequest(gitUrl, prId);
            pr.comment(message.forComment(buildUrl));
        } catch (IOException ex) {
            listener.error("Couldn't add comment to pull request #" + prId + "!", ex);
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

}

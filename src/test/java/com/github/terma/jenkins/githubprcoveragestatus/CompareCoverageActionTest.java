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

import hudson.EnvVars;
import hudson.model.Build;
import hudson.model.Result;
import hudson.model.TaskListener;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.io.PrintStream;

public class CompareCoverageActionTest {

    private Build build = Mockito.mock(Build.class);

    private PrintStream logger = Mockito.mock(PrintStream.class);
    private TaskListener listener = Mockito.mock(TaskListener.class);

    private EnvVars envVars = Mockito.mock(EnvVars.class);

    private MasterCoverageRepository masterCoverageRepository = Mockito.mock(MasterCoverageRepository.class);
    private CoverageRepository coverageRepository = Mockito.mock(CoverageRepository.class);
    private SettingsRepository settingsRepository = Mockito.mock(SettingsRepository.class);
    private PullRequestRepository pullRequestRepository = Mockito.mock(PullRequestRepository.class);

    @Before
    public void initMocks() {
        ServiceRegistry.setMasterCoverageRepository(masterCoverageRepository);
        ServiceRegistry.setCoverageRepository(coverageRepository);
        ServiceRegistry.setSettingsRepository(settingsRepository);
        ServiceRegistry.setPullRequestRepository(pullRequestRepository);
    }

    @Test
    public void skipStepIfResultOfBuildIsNotSuccess() throws IOException, InterruptedException {
        new CompareCoverageAction().perform(build, null, null, null);
    }

    @Test
    public void postCoverageStatusToPullRequestAsComment() throws IOException, InterruptedException {
        Mockito.when(build.getResult()).thenReturn(Result.SUCCESS);
        Mockito.when(listener.getLogger()).thenReturn(logger);
        Mockito.when(build.getEnvironment(Mockito.any(TaskListener.class))).thenReturn(envVars);
        Mockito.when(envVars.get(Utils.GIT_PR_ID_ENV_PROPERTY)).thenReturn("12");
        Mockito.when(envVars.get(Utils.BUILD_URL_ENV_PROPERTY)).thenReturn("aaa/job/a");

        new CompareCoverageAction().perform(build, null, null, listener);

        Mockito.verify(pullRequestRepository).comment(null, 12, "[![Coverage](aaa/coverage-status-icon?coverage=0.0&masterCoverage=0.0)](aaa/job/a)");
    }

    @Test
    public void postCoverageStatusToPullRequestAsCommentWithCustomJenkinsUrlIfConfigured() throws IOException, InterruptedException {
        Mockito.when(build.getResult()).thenReturn(Result.SUCCESS);
        Mockito.when(listener.getLogger()).thenReturn(logger);
        Mockito.when(build.getEnvironment(Mockito.any(TaskListener.class))).thenReturn(envVars);
        Mockito.when(envVars.get(Utils.GIT_PR_ID_ENV_PROPERTY)).thenReturn("12");
        Mockito.when(envVars.get(Utils.BUILD_URL_ENV_PROPERTY)).thenReturn("aaa/job/a");

        Mockito.when(settingsRepository.getJenkinsUrl()).thenReturn("customJ");

        new CompareCoverageAction().perform(build, null, null, listener);

        Mockito.verify(pullRequestRepository).comment(null, 12, "[![Coverage](customJ/coverage-status-icon?coverage=0.0&masterCoverage=0.0)](aaa/job/a)");
    }

}

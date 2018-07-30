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
import org.kohsuke.github.GHRepository;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.io.PrintWriter;

import static org.mockito.Mockito.*;

public class CompareCoverageActionTest {

    private static final String GIT_URL = "git@github.com:some/my-project.git";
    private Build build = mock(Build.class);

    private PrintWriter printWriter = mock(PrintWriter.class);
    private TaskListener listener = mock(TaskListener.class);

    private EnvVars envVars = mock(EnvVars.class);

    private MasterCoverageRepository masterCoverageRepository = mock(MasterCoverageRepository.class);
    private CoverageRepository coverageRepository = mock(CoverageRepository.class);
    private SettingsRepository settingsRepository = mock(SettingsRepository.class);
    private PullRequestRepository pullRequestRepository = mock(PullRequestRepository.class);
    private GHRepository ghRepository = mock(GHRepository.class);

    @Before
    public void initMocks() throws IOException {
        ServiceRegistry.setMasterCoverageRepository(masterCoverageRepository);
        ServiceRegistry.setCoverageRepository(coverageRepository);
        ServiceRegistry.setSettingsRepository(settingsRepository);
        ServiceRegistry.setPullRequestRepository(pullRequestRepository);
        when(pullRequestRepository.getGitHubRepository(GIT_URL)).thenReturn(ghRepository);
        when(envVars.get(PrIdAndUrlUtils.GIT_URL_PROPERTY)).thenReturn(GIT_URL);
        when(listener.getLogger()).thenReturn(System.out);
    }

    @Test
    public void skipStepIfResultOfBuildIsNotSuccess() throws IOException, InterruptedException {
        new CompareCoverageAction().perform(build, null, null, listener);
    }

    @Test
    public void postCoverageStatusToPullRequestAsComment() throws IOException, InterruptedException {
        when(build.getResult()).thenReturn(Result.SUCCESS);
        when(build.getEnvironment(any(TaskListener.class))).thenReturn(envVars);
        when(envVars.get(PrIdAndUrlUtils.GIT_PR_ID_ENV_PROPERTY)).thenReturn("12");
        when(envVars.get(Utils.BUILD_URL_ENV_PROPERTY)).thenReturn("aaa/job/a");

        new CompareCoverageAction().perform(build, null, null, listener);

        verify(pullRequestRepository).comment(ghRepository, 12, "[![0% (0.0%) vs master 0%](aaa/coverage-status-icon/?coverage=0.0&masterCoverage=0.0)](aaa/job/a)");
    }

    @Test
    public void keepBuildGreenAndLogErrorIfExceptionDuringGitHubAccess() throws IOException, InterruptedException {
        when(build.getResult()).thenReturn(Result.SUCCESS);
        when(build.getEnvironment(any(TaskListener.class))).thenReturn(envVars);
        when(envVars.get(PrIdAndUrlUtils.GIT_PR_ID_ENV_PROPERTY)).thenReturn("12");
        when(envVars.get(Utils.BUILD_URL_ENV_PROPERTY)).thenReturn("aaa/job/a");
        when(listener.error(anyString())).thenReturn(printWriter);

        doThrow(new IOException("???")).when(pullRequestRepository).comment(any(GHRepository.class), anyInt(), anyString());

        new CompareCoverageAction().perform(build, null, null, listener);

        verify(listener).error("Couldn't add comment to pull request #12!");
        verify(printWriter, atLeastOnce()).println(any(Throwable.class));
    }

    @Test
    public void postCoverageStatusToPullRequestAsCommentWithShieldIoIfPrivateJenkinsPublicGitHubTurnOn()
            throws IOException, InterruptedException {
        when(build.getResult()).thenReturn(Result.SUCCESS);
        when(build.getEnvironment(any(TaskListener.class))).thenReturn(envVars);
        when(envVars.get(PrIdAndUrlUtils.GIT_PR_ID_ENV_PROPERTY)).thenReturn("12");
        when(envVars.get(Utils.BUILD_URL_ENV_PROPERTY)).thenReturn("aaa/job/a");

        when(settingsRepository.isPrivateJenkinsPublicGitHub()).thenReturn(true);

        new CompareCoverageAction().perform(build, null, null, listener);

        verify(pullRequestRepository).comment(ghRepository, 12, "[![0% (0.0%) vs master 0%](https://img.shields.io/badge/coverage-0%25%20(0.0%25)%20vs%20master%200%25-brightgreen.svg)](aaa/job/a)");
    }

    @Test
    public void postCoverageStatusToPullRequestAsCommentWithCustomJenkinsUrlIfConfigured() throws IOException, InterruptedException {
        when(build.getResult()).thenReturn(Result.SUCCESS);
        when(build.getEnvironment(any(TaskListener.class))).thenReturn(envVars);
        when(envVars.get(PrIdAndUrlUtils.GIT_PR_ID_ENV_PROPERTY)).thenReturn("12");
        when(envVars.get(Utils.BUILD_URL_ENV_PROPERTY)).thenReturn("aaa/job/a");

        when(settingsRepository.getJenkinsUrl()).thenReturn("customJ");

        new CompareCoverageAction().perform(build, null, null, listener);

        verify(pullRequestRepository).comment(ghRepository, 12, "[![0% (0.0%) vs master 0%](customJ/coverage-status-icon/?coverage=0.0&masterCoverage=0.0)](aaa/job/a)");
    }

    @Test
    public void doNotCommentButFailBuildIfConfiguredToDoSo() throws IOException, InterruptedException {
        when(build.getResult()).thenReturn(Result.SUCCESS);
        when(build.getEnvironment(any(TaskListener.class))).thenReturn(envVars);
        when(envVars.get(PrIdAndUrlUtils.GIT_PR_ID_ENV_PROPERTY)).thenReturn("12");
        when(envVars.get(Utils.BUILD_URL_ENV_PROPERTY)).thenReturn("aaa/job/a");
        when(listener.error(anyString())).thenReturn(printWriter);
        when(masterCoverageRepository.get(anyString())).thenReturn(0.999f);

        CompareCoverageAction underTest = new CompareCoverageAction();
        underTest.setFailBuildOnConverageDecrease(true);
        underTest.perform(build, null, null, listener);

        verify(build, atLeastOnce()).setResult(Result.FAILURE);
    }

    @Test
    public void doNotCommentButPassBuildIfConfiguredToDoSo() throws IOException, InterruptedException {
        when(build.getResult()).thenReturn(Result.SUCCESS);
        when(build.getEnvironment(any(TaskListener.class))).thenReturn(envVars);
        when(envVars.get(PrIdAndUrlUtils.GIT_PR_ID_ENV_PROPERTY)).thenReturn("12");
        when(envVars.get(Utils.BUILD_URL_ENV_PROPERTY)).thenReturn("aaa/job/a");
        when(listener.error(anyString())).thenReturn(printWriter);

        CompareCoverageAction underTest = new CompareCoverageAction();
        underTest.setFailBuildOnConverageDecrease(true);
        underTest.perform(build, null, null, listener);

        verify(build, never()).setResult(Result.FAILURE);
    }

}

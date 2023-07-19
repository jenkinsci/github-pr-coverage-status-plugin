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
import org.kohsuke.github.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

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
    private GHPullRequestCommitDetail commit = mock(GHPullRequestCommitDetail.class);

    private List<GHPullRequestCommitDetail> commits = new ArrayList<GHPullRequestCommitDetail>() {{
        add(mock(GHPullRequestCommitDetail.class));
        add(commit);
    }};
    private PagedIterable<GHPullRequestCommitDetail> pagedIterable = mock(PagedIterable.class);

    private CompareCoverageAction coverageAction = new CompareCoverageAction();

    @Before
    public void initMocks() throws IOException {
        ServiceRegistry.setMasterCoverageRepository(masterCoverageRepository);
        ServiceRegistry.setCoverageRepository(coverageRepository);
        ServiceRegistry.setSettingsRepository(settingsRepository);
        ServiceRegistry.setPullRequestRepository(pullRequestRepository);
        when(pullRequestRepository.getGitHubRepository(GIT_URL)).thenReturn(ghRepository);
        when(listener.getLogger()).thenReturn(System.out);
    }
    
    @Before
    public void reinitializeCoverageRepositories() {
        masterCoverageRepository = mock(MasterCoverageRepository.class);
        coverageRepository = mock(CoverageRepository.class);
    }

    @Test
    public void skipStepIfResultOfBuildIsNotSuccess() throws IOException, InterruptedException {
        new CompareCoverageAction().perform(build, null, null, listener);
    }

    @Test
    public void postCoverageStatusToPullRequestAsComment() throws IOException, InterruptedException {
        prepareBuildSuccess();
        prepareEnvVars();
        coverageAction.setPublishResultAs("comment");

        coverageAction.perform(build, null, null, listener);

        verify(pullRequestRepository).comment(ghRepository, 12, "[![0% (0.0%) vs master 0%](aaa/coverage-status-icon/?coverage=0.0&masterCoverage=0.0)](aaa/job/a)");
    }

    @Test
    public void postResultAsStatusCheck() throws IOException, InterruptedException {
        prepareBuildSuccess();
        prepareEnvVars();
        prepareCommit();
        coverageAction.setPublishResultAs("statusCheck");

        coverageAction.perform(build, null, null, listener);

        verify(pullRequestRepository).createCommitStatus(
                ghRepository,
                "fh3k2l",
                GHCommitState.SUCCESS,
                "aaa/job/a",
                "Coverage 0% changed 0.0% vs master 0%"
        );
    }

    @Test
    public void postResultAsSuccessfulStatusCheck() throws IOException, InterruptedException {
        prepareBuildSuccess();
        prepareEnvVars();
        prepareCommit();
        prepareCoverageData(0.88f, 0.95f);
        coverageAction.setPublishResultAs("statusCheck");

        coverageAction.perform(build, null, null, listener);

        verify(pullRequestRepository).createCommitStatus(
                ghRepository,
                "fh3k2l",
                GHCommitState.SUCCESS,
                "aaa/job/a",
                "Coverage 95% changed +7.0% vs master 88%"
        );
    }

    @Test
    public void postResultAsFailedStatusCheck() throws IOException, InterruptedException {
        prepareBuildSuccess();
        prepareEnvVars();
        prepareCommit();
        prepareCoverageData(0.95f, 0.9f);
        coverageAction.setPublishResultAs("statusCheck");

        coverageAction.perform(build, null, null, listener);

        verify(pullRequestRepository).createCommitStatus(
                ghRepository,
                "fh3k2l",
                GHCommitState.FAILURE,
                "aaa/job/a",
                "Coverage 90% changed -5.0% vs master 95%"
        );
    }

    @Test
    public void postResultAsFailedStatusCheckOnMicroChange() throws IOException, InterruptedException {
        prepareBuildSuccess();
        prepareEnvVars();
        prepareCommit();
        prepareCoverageData(0.95f, 0.94994f); //-0,006%
        coverageAction.setPublishResultAs("statusCheck");

        coverageAction.perform(build, null, null, listener);

        verify(pullRequestRepository).createCommitStatus(
                ghRepository,
                "fh3k2l",
                GHCommitState.FAILURE,
                "aaa/job/a",
                "Coverage 95% changed -0.01% vs master 95%"
        );
    }

    @Test
    public void postResultAsSuccessStatusCheckOnMicroChange() throws IOException, InterruptedException {
        prepareBuildSuccess();
        prepareEnvVars();
        prepareCommit();
        prepareCoverageData(0.95f, 0.94996f); //-0,004%
        coverageAction.setPublishResultAs("statusCheck");

        coverageAction.perform(build, null, null, listener);

        verify(pullRequestRepository).createCommitStatus(
                ghRepository,
                "fh3k2l",
                GHCommitState.SUCCESS,
                "aaa/job/a",
                "Coverage 95% changed 0.0% vs master 95%"
        );
    }

    @Test
    public void keepBuildGreenAndLogErrorIfExceptionDuringGitHubAccess() throws IOException, InterruptedException {
        prepareBuildSuccess();
        prepareEnvVars();
        when(listener.error(anyString())).thenReturn(printWriter);
        coverageAction.setPublishResultAs("comment");

        doThrow(new IOException("???")).when(pullRequestRepository).comment(any(GHRepository.class), anyInt(), anyString());

        coverageAction.perform(build, null, null, listener);

        verify(listener).error("Couldn't add comment to pull request #12!");
        verify(printWriter, atLeastOnce()).println(any(Throwable.class));
    }

    @Test
    public void postCoverageStatusToPullRequestAsCommentWithShieldIoIfPrivateJenkinsPublicGitHubTurnOn()
            throws IOException, InterruptedException {
        prepareBuildSuccess();
        prepareEnvVars();
        when(settingsRepository.isPrivateJenkinsPublicGitHub()).thenReturn(true);
        coverageAction.setPublishResultAs("comment");

        coverageAction.perform(build, null, null, listener);

        verify(pullRequestRepository).comment(ghRepository, 12, "[![0% (0.0%) vs master 0%](https://img.shields.io/badge/coverage-0%25%20(0.0%25)%20vs%20master%200%25-brightgreen.svg)](aaa/job/a)");
    }

    @Test
    public void postCoverageStatusToPullRequestAsCommentWithCustomJenkinsUrlIfConfigured() throws IOException, InterruptedException {
        prepareBuildSuccess();
        prepareEnvVars();
        when(settingsRepository.getJenkinsUrl()).thenReturn("customJ");
        coverageAction.setPublishResultAs("comment");

        coverageAction.perform(build, null, null, listener);

        verify(pullRequestRepository).comment(ghRepository, 12, "[![0% (0.0%) vs master 0%](customJ/coverage-status-icon/?coverage=0.0&masterCoverage=0.0)](aaa/job/a)");
    }
    
    private void prepareCoverageData(float masterCoverage, float prCoverage) throws IOException, InterruptedException {
        when(masterCoverageRepository.get(GIT_URL)).thenReturn(masterCoverage);
        when(coverageRepository.get(null)).thenReturn(prCoverage);
        initMocks();
    }

    private void prepareCommit() throws IOException {
        GHPullRequest ghPullRequest = mock(GHPullRequest.class);
        when(ghRepository.getPullRequest(12)).thenReturn(ghPullRequest);
        when(ghPullRequest.listCommits()).thenReturn(pagedIterable);
        when(pagedIterable.asList()).thenReturn(commits);
        when(commit.getSha()).thenReturn("fh3k2l");
    }

    private void prepareBuildSuccess() throws IOException, InterruptedException {
        when(build.getResult()).thenReturn(Result.SUCCESS);
        when(build.getEnvironment(any(TaskListener.class))).thenReturn(envVars);
    }

    private void prepareEnvVars() {
        String buildUrl = "aaa/job/a";
        String prId = "12";
        when(envVars.get(PrIdAndUrlUtils.GIT_PR_ID_ENV_PROPERTY)).thenReturn(prId);
        when(envVars.get(Utils.BUILD_URL_ENV_PROPERTY)).thenReturn(buildUrl);
        when(envVars.get(PrIdAndUrlUtils.GIT_URL_PROPERTY)).thenReturn(GIT_URL);
    }
}

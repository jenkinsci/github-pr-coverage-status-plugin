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

import com.cloudbees.plugins.credentials.CredentialsScope;
import com.cloudbees.plugins.credentials.SystemCredentialsProvider;
import com.cloudbees.plugins.credentials.impl.UsernamePasswordCredentialsImpl;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import hudson.EnvVars;
import hudson.model.Build;
import hudson.model.Result;
import hudson.model.TaskListener;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.io.PrintWriter;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalToJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CompareCoverageActionTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    private static final String GIT_URL = "git@github.com:some/my-project.git";
    private Build build = mock(Build.class);

    private PrintWriter printWriter = mock(PrintWriter.class);
    private TaskListener listener = mock(TaskListener.class);

    private EnvVars envVars = mock(EnvVars.class);

    private MasterCoverageRepository masterCoverageRepository = mock(MasterCoverageRepository.class);
    private CoverageRepository coverageRepository = mock(CoverageRepository.class);
    private SettingsRepository settingsRepository = mock(SettingsRepository.class);

    final String CREDENTIALID = "7982374987234";
    final String PROJECTCODE = "ProjectCode";
    final String REPOSITORYNAME = "RepositoryName";
    final String BITBUCKETHOST = "http://127.0.0.1:" + wireMockRule.port() + "/bitbucket/";
    final String PULLREQUESTID = "123";
    final String JENKINSURL = "http://jenkins.local";

    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(0));


    @Before
    public void initMocks() throws IOException {
        initializeJenkinsMock();
        initializeBitbucketMock();

        ServiceRegistry.setMasterCoverageRepository(masterCoverageRepository);
        ServiceRegistry.setCoverageRepository(coverageRepository);
        ServiceRegistry.setSettingsRepository(settingsRepository);
        when(envVars.get(PrIdAndUrlUtils.GIT_URL_PROPERTY)).thenReturn(GIT_URL);
        when(listener.getLogger()).thenReturn(System.out);
    }

    private void initializeJenkinsMock() {
        SystemCredentialsProvider.getInstance().getCredentials().add(
            new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, CREDENTIALID, "Desc", "bitbucket", "secret"));

        EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        EnvVars envVars = prop.getEnvVars();
        envVars.put("pullRequestId", PULLREQUESTID);
        jenkinsRule.jenkins.getGlobalNodeProperties().add(prop);
    }

    private void initializeBitbucketMock() {
        wireMockRule.stubFor(post(
            urlPathEqualTo("/bitbucket/rest/api/1.0/projects/" + PROJECTCODE + "/repos/" + REPOSITORYNAME + "/pull-requests/" + PULLREQUESTID + "/comments"))
            .willReturn(aResponse()
                .withStatus(201)
                .withBody("{ \"id\":123456, \"text\": \"Sample\" }")
            )
        );
    }


    @Test
    public void skipStepIfResultOfBuildIsNotSuccess() throws IOException, InterruptedException {
        compareCoverageAction().perform(build, null, null, listener);
    }

    @Test
    public void postCoverageStatusToPullRequestAsComment() throws IOException, InterruptedException {
        when(build.getResult()).thenReturn(Result.SUCCESS);
        when(build.getEnvironment(any(TaskListener.class))).thenReturn(envVars);
        when(envVars.get(PrIdAndUrlUtils.GIT_PR_ID_ENV_PROPERTY)).thenReturn(PULLREQUESTID);
        when(envVars.get(Utils.BUILD_URL_ENV_PROPERTY)).thenReturn("aaa/job/a");

        compareCoverageAction().perform(build, null, null, listener);

        String expectedComment =
            "[![0% (0.0%) vs master 0%](" + JENKINSURL + "/stash-coverage-status-icon/?coverage=0.0&masterCoverage=0.0&color=red)](aaa/job/a)";
        WireMock.verify(postRequestedFor(
            urlEqualTo("/bitbucket/rest/api/1.0/projects/" + PROJECTCODE + "/repos/" + REPOSITORYNAME + "/pull-requests/" + PULLREQUESTID + "/comments"))
            .withRequestBody(equalToJson("{\n"
                + "  \"text\" : \"" + expectedComment + "\"\n"
                + "}")));
    }

    @Test
    public void keepBuildGreenAndLogErrorIfExceptionDuringGitHubAccess() throws IOException, InterruptedException {
        when(build.getResult()).thenReturn(Result.SUCCESS);
        when(build.getEnvironment(any(TaskListener.class))).thenReturn(envVars);
        when(envVars.get(PrIdAndUrlUtils.GIT_PR_ID_ENV_PROPERTY)).thenReturn(PULLREQUESTID);
        when(envVars.get(Utils.BUILD_URL_ENV_PROPERTY)).thenReturn("aaa/job/a");
        when(listener.error(anyString())).thenReturn(printWriter);

        wireMockRule.stubFor(post(
            urlPathEqualTo("/bitbucket/rest/api/1.0/projects/" + PROJECTCODE + "/repos/" + REPOSITORYNAME + "/pull-requests/" + PULLREQUESTID + "/comments"))
            .willReturn(aResponse()
                .withStatus(502)
                .withBody("Backend failure")
            )
        );

        compareCoverageAction().perform(build, null, null, listener);

        verify(listener).error("Couldn't add comment to pull request #" + PULLREQUESTID + "!");
        verify(printWriter, atLeastOnce()).println(any(Throwable.class));
    }

    @Test
    public void postCoverageStatusToPullRequestAsCommentWithShieldIoIfPrivateJenkinsPublicGitHubTurnOn()
        throws IOException, InterruptedException {
        when(build.getResult()).thenReturn(Result.SUCCESS);
        when(build.getEnvironment(any(TaskListener.class))).thenReturn(envVars);
        when(envVars.get(PrIdAndUrlUtils.GIT_PR_ID_ENV_PROPERTY)).thenReturn(PULLREQUESTID);
        when(envVars.get(Utils.BUILD_URL_ENV_PROPERTY)).thenReturn("aaa/job/a");

        when(settingsRepository.isPrivateJenkinsPublicGitHub()).thenReturn(true);

        compareCoverageAction(null, true).perform(build, null, null, listener);

        String expectedComment =
            "[![0% (0.0%) vs master 0%](https://img.shields.io/badge/coverage-0%25%20(0.0%25)%20vs%20master%200%25-red.svg)](aaa/job/a)";
        WireMock.verify(postRequestedFor(
            urlEqualTo("/bitbucket/rest/api/1.0/projects/" + PROJECTCODE + "/repos/" + REPOSITORYNAME + "/pull-requests/" + PULLREQUESTID + "/comments"))
            .withRequestBody(equalToJson("{\n"
                + "  \"text\" : \"" + expectedComment + "\"\n"
                + "}")));
    }

    @Test
    public void postCoverageStatusToPullRequestAsCommentWithNoCustomJenkinsUrlConfigured() throws IOException, InterruptedException {

        when(build.getResult()).thenReturn(Result.SUCCESS);
        when(build.getEnvironment(any(TaskListener.class))).thenReturn(envVars);
        when(envVars.get(PrIdAndUrlUtils.GIT_PR_ID_ENV_PROPERTY)).thenReturn(PULLREQUESTID);
        when(envVars.get(Utils.BUILD_URL_ENV_PROPERTY)).thenReturn("https://somewhere.local/jenkins/job/a");

        compareCoverageAction(null, false).perform(build, null, null, listener);

        String expectedComment =
            "[![0% (0.0%) vs master 0%](https://somewhere.local/jenkins/stash-coverage-status-icon/?coverage=0.0&masterCoverage=0.0&color=red)](https://somewhere.local/jenkins/job/a)";
        WireMock.verify(postRequestedFor(
            urlEqualTo("/bitbucket/rest/api/1.0/projects/" + PROJECTCODE + "/repos/" + REPOSITORYNAME + "/pull-requests/" + PULLREQUESTID + "/comments"))
            .withRequestBody(equalToJson("{\n"
                + "  \"text\" : \"" + expectedComment + "\"\n"
                + "}")));
    }

    private CompareCoverageAction compareCoverageAction() {
        return compareCoverageAction(JENKINSURL, false);
    }

    private CompareCoverageAction compareCoverageAction(String jenkinsUrl, boolean privateJenkins) {
        final boolean negativeCoverageIsRed = true;
        final boolean ignoreSsl = false;
        final boolean disableSimpleCov = false;
        final String yellowThreshold = "60";
        final String greenThreshold = "90";
        final boolean useSonarForMasterCoverage = false;
        final String sonarUrl = "http://127.0.0.1:" + wireMockRule.port() + "/sonar/";
        final String sonarToken = "";
        final String sonarLogin = "";
        final String sonarPassword = "";
        return new CompareCoverageAction(BITBUCKETHOST,
            CREDENTIALID,
            PROJECTCODE,
            REPOSITORYNAME,
            jenkinsUrl,
            privateJenkins,
            yellowThreshold,
            greenThreshold,
            useSonarForMasterCoverage,
            sonarUrl,
            sonarToken,
            sonarLogin,
            sonarPassword,
            disableSimpleCov,
            ignoreSsl,
            negativeCoverageIsRed);
    }
}

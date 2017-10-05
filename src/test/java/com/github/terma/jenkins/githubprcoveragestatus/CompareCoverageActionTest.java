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
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

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
    private static final String GIT_targetBranch = "develop";
    private Build build = mock(Build.class);

    private PrintWriter printWriter = mock(PrintWriter.class);
    private TaskListener listener = mock(TaskListener.class);

    private EnvVars envVars;

    private MasterCoverageRepository masterCoverageRepository = mock(MasterCoverageRepository.class);
    private CoverageRepository coverageRepository = mock(CoverageRepository.class);

    final String CREDENTIALID = "7982374987234";
    final String PROJECTCODE = "ProjectCode";
    final String REPOSITORYNAME = "RepositoryName";
    final String BITBUCKETHOST = "http://127.0.0.1:" + wireMockRule.port() + "/bitbucket/";
    final String PULLREQUESTID = "123";
    final String JENKINSURL = "http://jenkins.local";

    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(0));


    @Before
    @SneakyThrows
    public void initMocks() throws IOException {
        envVars = new EnvVars();
        envVars.put("GIT_URL", GIT_URL);
        envVars.put("targetBranch", GIT_targetBranch);
        envVars.put("pullRequestId", PULLREQUESTID);
        envVars.put(Utils.BUILD_URL_ENV_PROPERTY, "aaa/job/a");

        initializeJenkinsMock();
        initializeBitbucketMock();

        ServiceRegistry.setMasterCoverageRepository(masterCoverageRepository);
        ServiceRegistry.setCoverageRepository(coverageRepository);
        when(listener.getLogger()).thenReturn(System.out);
        when(build.getEnvironment(any(TaskListener.class))).thenReturn(envVars);
        when(build.getResult()).thenReturn(Result.SUCCESS);
    }

    @SneakyThrows
    private void initializeJenkinsMock() {
        SystemCredentialsProvider.getInstance().getCredentials().add(
            new UsernamePasswordCredentialsImpl(CredentialsScope.GLOBAL, CREDENTIALID, "Desc", "bitbucket", "secret"));

        EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        for (Map.Entry<String, String> entry : envVars.entrySet()) {
            prop.getEnvVars().put(entry.getKey(), entry.getValue());
        }
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
        when(build.getResult()).thenReturn(Result.FAILURE);
        compareCoverageAction().perform(build, null, null, listener);
    }

    @Test
    public void postCoverageStatusToPullRequestAsComment() throws IOException, InterruptedException {
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
        envVars.put(Utils.BUILD_URL_ENV_PROPERTY, "https://somewhere.local/jenkins/job/a");

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
        final CompareCoverageAction compareCoverageAction = new CompareCoverageAction();
        compareCoverageAction.setBitbucketHost(BITBUCKETHOST);
        compareCoverageAction.setCredentialsId(CREDENTIALID);
        compareCoverageAction.setProjectCode(PROJECTCODE);
        compareCoverageAction.setRepositoryName(REPOSITORYNAME);
        compareCoverageAction.setJenkinsUrl(jenkinsUrl);
        compareCoverageAction.setPrivateJenkins(privateJenkins);
        compareCoverageAction.setYellowThreshold(yellowThreshold);
        compareCoverageAction.setGreenThreshold(greenThreshold);
        compareCoverageAction.setUseSonarForMasterCoverage(useSonarForMasterCoverage);
        compareCoverageAction.setSonarUrl(sonarUrl);
        compareCoverageAction.setSonarToken(sonarToken);
        compareCoverageAction.setSonarLogin(sonarLogin);
        compareCoverageAction.setSonarPassword(sonarPassword);
        compareCoverageAction.setDisableSimpleCov(disableSimpleCov);
        compareCoverageAction.setIgnoreSsl(ignoreSsl);
        compareCoverageAction.setNegativeCoverageIsRed(negativeCoverageIsRed);
        return compareCoverageAction;
    }
}

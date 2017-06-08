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

import java.io.IOException;
import java.io.PrintStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import hudson.EnvVars;
import hudson.model.Run;
import hudson.model.TaskListener;

import static org.mockito.Mockito.*;


public class UtilsTest {
    private static final String PR_ID = "12";
    private static final Integer PR_ID_INT = Integer.parseInt(PR_ID);
    private static final String CHANGE_ID = "13";
    private static final Integer CHANGE_ID_INT = Integer.parseInt(CHANGE_ID);
    private Run build = mock(Run.class);
    private EnvVars envVars = mock(EnvVars.class);
    private TaskListener listener = mock(TaskListener.class);

    @Before
    public void initMocks() throws IOException, InterruptedException {
        when(build.getEnvironment(listener)).thenReturn(envVars);
    }

    @Test
    public void getUserRepo() {
        Assert.assertEquals(
                "terma/jenkins-github-coverage-updater",
                Utils.getUserRepo("https://github.com/terma/jenkins-github-coverage-updater"));

        Assert.assertEquals("terma/jenkins-github-coverage-updater",
                Utils.getUserRepo("https://github.com/terma/jenkins-github-coverage-updater.git"));

        Assert.assertEquals("terma/jenkins-github-coverage-updater",
                Utils.getUserRepo("git@github.com:terma/jenkins-github-coverage-updater.git"));

        Assert.assertEquals("terma/jenkins-github-coverage-updater",
                Utils.getUserRepo("git@github.com:terma/jenkins-github-coverage-updater"));
    }

    @Test
    public void getJenkinsUrlFromBuildUrl() {
        Assert.assertEquals(
                "http://localhost:8080/jenkins",
                Utils.getJenkinsUrlFromBuildUrl("http://localhost:8080/jenkins/job/branch/45"));

        Assert.assertEquals(
                "http://localhost:8080",
                Utils.getJenkinsUrlFromBuildUrl("http://localhost:8080/job/branch/459000"));
    }

    @Test
    public void gitPrIdPrIdHasPriority() throws IOException, InterruptedException  {
        when(envVars.get(Utils.GIT_PR_ID_ENV_PROPERTY)).thenReturn(PR_ID);
        when(envVars.get(Utils.CHANGE_ID_PROPERTY)).thenReturn(CHANGE_ID);

        Assert.assertEquals(PR_ID_INT, Utils.gitPrId(build, listener));
    }

    @Test
    public void gitPrIdIfPrIdIsNullChangeIdIsUsed() throws IOException, InterruptedException {
        when(envVars.get(Utils.GIT_PR_ID_ENV_PROPERTY)).thenReturn(null);
        when(envVars.get(Utils.CHANGE_ID_PROPERTY)).thenReturn(CHANGE_ID);

        Assert.assertEquals(CHANGE_ID_INT, Utils.gitPrId(build, listener));
    }

    @Test
    public void getGitUrlGitUrlHasPriority() throws IOException, InterruptedException  {
        when(envVars.get(Utils.GIT_URL_ENV_PROPERTY)).thenReturn(PR_ID);
        when(envVars.get(Utils.CHANGE_URL_PROPERTY)).thenReturn(CHANGE_ID);

        Assert.assertEquals(PR_ID, Utils.getGitUrl(build, listener));
    }

    @Test
    public void getGitUrlIfGitUrlsNullChangeUrlIsUsed() throws IOException, InterruptedException {
        when(envVars.get(Utils.GIT_URL_ENV_PROPERTY)).thenReturn(null);
        when(envVars.get(Utils.CHANGE_URL_PROPERTY)).thenReturn(CHANGE_ID);

        Assert.assertEquals(CHANGE_ID, Utils.getGitUrl(build, listener));
    }
}

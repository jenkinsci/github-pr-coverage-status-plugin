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
import hudson.model.Run;
import hudson.model.TaskListener;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.kohsuke.github.GHPullRequest;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PrIdAndUrlUtilsTest {

    private static final String PR_ID = "12";
    private static final String CHANGE_ID = "13";
    private static final int PR_ID_INT = 12;
    private static final int CHANGE_ID_INT = 13;
    private static final int SCM_ENVS_PR_ID_INT = 14;

    private Run build = mock(Run.class);
    private EnvVars envVars = mock(EnvVars.class);
    private TaskListener listener = mock(TaskListener.class);
    private PrintStream logger = mock(PrintStream.class);
    private GHPullRequest ghPullRequest = mock(GHPullRequest.class);
    private PullRequestRepository pullRequestRepository = mock(PullRequestRepository.class);
    private Map<String, String> scmVars;

    @Before
    public void initMocks() throws IOException, InterruptedException {
        ServiceRegistry.setPullRequestRepository(pullRequestRepository);
        when(pullRequestRepository.getPullRequestFor(anyString(), anyString(), anyString())).thenReturn(ghPullRequest);

        when(listener.getLogger()).thenReturn(logger);
        when(build.getEnvironment(listener)).thenReturn(envVars);
        when(envVars.get(PrIdAndUrlUtils.GIT_PR_ID_ENV_PROPERTY)).thenReturn(PR_ID);
        when(envVars.get(PrIdAndUrlUtils.CHANGE_ID_PROPERTY)).thenReturn(CHANGE_ID);

        when(envVars.get(PrIdAndUrlUtils.GIT_URL_PROPERTY)).thenReturn("GIT_URL");
        when(envVars.get(PrIdAndUrlUtils.CHANGE_URL_PROPERTY)).thenReturn("CHANGE_URL");

        scmVars = new HashMap<String, String>() {{
            put("GIT_BRANCH", "SCM_VARS_GIT_BRANCH");
            put("GIT_COMMIT", "SCM_VARS_GIT_COMMIT");
            put(PrIdAndUrlUtils.GIT_URL_PROPERTY, "SCM_VARS_GIT_URL");
        }};
    }

    @Test
    public void gitPrIdPrIdHasPriority() throws IOException, InterruptedException {
        when(envVars.get(PrIdAndUrlUtils.GIT_PR_ID_ENV_PROPERTY)).thenReturn(PR_ID);
        when(envVars.get(PrIdAndUrlUtils.CHANGE_ID_PROPERTY)).thenReturn(CHANGE_ID);

        Assert.assertEquals(PR_ID_INT, PrIdAndUrlUtils.getPrId(scmVars, build, listener));
    }

    @Test
    public void gitPrIdIfPrIdIsNullChangeIdIsUsed() throws IOException, InterruptedException {
        when(envVars.get(PrIdAndUrlUtils.GIT_PR_ID_ENV_PROPERTY)).thenReturn(null);
        when(envVars.get(PrIdAndUrlUtils.CHANGE_ID_PROPERTY)).thenReturn(CHANGE_ID);

        Assert.assertEquals(CHANGE_ID_INT, PrIdAndUrlUtils.getPrId(scmVars, build, listener));
    }

    @Test
    public void getGitPrIdFromScmVarsIfOtherNull() throws IOException, InterruptedException {
        when(envVars.get(PrIdAndUrlUtils.GIT_PR_ID_ENV_PROPERTY)).thenReturn(null);
        when(envVars.get(PrIdAndUrlUtils.CHANGE_ID_PROPERTY)).thenReturn(null);
        when(ghPullRequest.getNumber()).thenReturn(SCM_ENVS_PR_ID_INT);
        Assert.assertEquals(SCM_ENVS_PR_ID_INT, PrIdAndUrlUtils.getPrId(scmVars, build, listener));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void throwExceptionWhenNoPrId() throws IOException, InterruptedException {
        when(envVars.get(PrIdAndUrlUtils.GIT_URL_PROPERTY)).thenReturn(null);
        when(envVars.get(PrIdAndUrlUtils.CHANGE_URL_PROPERTY)).thenReturn(null);
        Assert.assertEquals(PR_ID, PrIdAndUrlUtils.getGitUrl(null, build, listener));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void throwExceptionWhenNoPrIdAndScmVarsEmpty() throws IOException, InterruptedException {
        when(envVars.get(PrIdAndUrlUtils.GIT_URL_PROPERTY)).thenReturn(null);
        when(envVars.get(PrIdAndUrlUtils.CHANGE_URL_PROPERTY)).thenReturn(null);
        scmVars.clear();
        Assert.assertEquals(PR_ID, PrIdAndUrlUtils.getGitUrl(scmVars, build, listener));
    }

    @Test
    public void getGitUrlGitUrlHasPriority() throws IOException, InterruptedException {
        when(envVars.get(PrIdAndUrlUtils.GIT_URL_PROPERTY)).thenReturn(PR_ID);
        when(envVars.get(PrIdAndUrlUtils.CHANGE_URL_PROPERTY)).thenReturn(CHANGE_ID);

        Assert.assertEquals(PR_ID, PrIdAndUrlUtils.getGitUrl(null, build, listener));
    }

    @Test
    public void getGitUrlIfGitUrlsNullChangeUrlIsUsed() throws IOException, InterruptedException {
        when(envVars.get(PrIdAndUrlUtils.GIT_URL_PROPERTY)).thenReturn(null);
        when(envVars.get(PrIdAndUrlUtils.CHANGE_URL_PROPERTY)).thenReturn(CHANGE_ID);

        Assert.assertEquals(CHANGE_ID, PrIdAndUrlUtils.getGitUrl(null, build, listener));
    }

    @Test
    public void getGitUrlFromScmVarsIfOtherNull() throws IOException, InterruptedException {
        when(envVars.get(PrIdAndUrlUtils.GIT_URL_PROPERTY)).thenReturn(null);
        when(envVars.get(PrIdAndUrlUtils.CHANGE_URL_PROPERTY)).thenReturn(null);

        Assert.assertEquals("SCM_VARS_GIT_URL", PrIdAndUrlUtils.getGitUrl(scmVars, build, listener));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowExceptionWhenGetGitUrlIfAllNull() throws IOException, InterruptedException {
        when(envVars.get(PrIdAndUrlUtils.GIT_URL_PROPERTY)).thenReturn(null);
        when(envVars.get(PrIdAndUrlUtils.CHANGE_URL_PROPERTY)).thenReturn(null);
        PrIdAndUrlUtils.getGitUrl(null, build, listener);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void shouldThrowExceptionWhenGetGitUrlIfAllEmpty() throws IOException, InterruptedException {
        when(envVars.get(PrIdAndUrlUtils.GIT_URL_PROPERTY)).thenReturn(null);
        when(envVars.get(PrIdAndUrlUtils.CHANGE_URL_PROPERTY)).thenReturn(null);
        scmVars.clear();
        PrIdAndUrlUtils.getGitUrl(scmVars, build, listener);
    }

    @Test
    public void getGitUrlScmVarsHasPriority() throws IOException, InterruptedException {
        when(envVars.get(PrIdAndUrlUtils.GIT_URL_PROPERTY)).thenReturn(PR_ID);
        when(envVars.get(PrIdAndUrlUtils.CHANGE_URL_PROPERTY)).thenReturn(CHANGE_ID);

        Assert.assertEquals("SCM_VARS_GIT_URL", PrIdAndUrlUtils.getGitUrl(scmVars, build, listener));
    }
}

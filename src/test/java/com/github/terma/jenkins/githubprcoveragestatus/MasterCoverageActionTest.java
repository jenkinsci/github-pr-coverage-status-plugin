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
import lombok.SneakyThrows;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mockito;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

public class MasterCoverageActionTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    private Build build;
    private TaskListener listener;
    EnvVars envVars;
    private CoverageRepository coverageRepository;
    final String GIT_URL = "http://bitbucket/project/repository.git";
    final float CURRENT_COVERAGE = 0.47f;
    Configuration configuration;

    @Before
    @SneakyThrows
    public void before() {
        build = Mockito.mock(Build.class);
        listener = Mockito.mock(TaskListener.class);
        coverageRepository = Mockito.mock(CoverageRepository.class);
        ServiceRegistry.setCoverageRepository(coverageRepository);

        envVars = new EnvVars();
        envVars.put("GIT_URL", GIT_URL);
        when(listener.getLogger()).thenReturn(new PrintStream(new ByteArrayOutputStream()));
        when(build.getEnvironment(listener)).thenReturn(envVars);
        when(coverageRepository.get(any())).thenReturn(CURRENT_COVERAGE);

        configuration = new Configuration();
    }

    @Test
    public void skipStepIfResultOfBuildIsNotSuccess() throws IOException, InterruptedException {
        final Map<String, Float> coverageByRepo = configuration.getDescriptor().getCoverageByRepo();
        coverageByRepo.remove(GIT_URL);

        when(build.getResult()).thenReturn(Result.FAILURE);
        getMasterCoverageAction().perform(build, null, null, null);

        assertThat("Still has zero coverage entry", coverageByRepo.size(), equalTo(0));
    }

    @Test
    public void testStoreCoverage() throws IOException, InterruptedException {
        final Map<String, Float> coverageByRepo = configuration.getDescriptor().getCoverageByRepo();
        coverageByRepo.remove(GIT_URL);
        assertThat("No coverage entry", coverageByRepo.isEmpty(), equalTo(true));

        when(build.getResult()).thenReturn(Result.SUCCESS);
        getMasterCoverageAction().perform(build, null, null, listener);

        assertThat("Has coverage entry", coverageByRepo.size(), equalTo(1));
        assertThat("Has coverage entry", coverageByRepo.get(GIT_URL), equalTo(CURRENT_COVERAGE));
    }

    private MasterCoverageAction getMasterCoverageAction() {
        final MasterCoverageAction masterCoverageAction = new MasterCoverageAction();
        masterCoverageAction.setDisableSimpleCov(false);
        return masterCoverageAction;
    }

}

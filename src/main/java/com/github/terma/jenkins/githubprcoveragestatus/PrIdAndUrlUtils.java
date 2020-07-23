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
import org.kohsuke.github.GHPullRequest;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

public class PrIdAndUrlUtils {

    /**
     * Injected by Git plugin
     */
    public static final String GIT_URL_PROPERTY = "GIT_URL";

    /**
     * Injected by
     * https://plugins.jenkins.io/ghprb/
     */
    public static final String GIT_PR_ID_ENV_PROPERTY = "ghprbPullId";
    public static final String CHANGE_ID_PROPERTY = "CHANGE_ID";
    public static final String CHANGE_URL_PROPERTY = "CHANGE_URL";

    private PrIdAndUrlUtils() {
        throw new UnsupportedOperationException("Util class!");
    }

    private static Integer getPullRequestBuilder(Run build, TaskListener listener) throws IOException, InterruptedException {
        final EnvVars envVars = build.getEnvironment(listener);
        final String gitPrId = envVars.get(GIT_PR_ID_ENV_PROPERTY);
        final String changeId = envVars.get(CHANGE_ID_PROPERTY);
        final String idString = gitPrId != null ? gitPrId : changeId;
        return idString != null ? Integer.parseInt(idString) : null;
    }

    private static Integer getMultiBranch(Map<String, String> scmVars, TaskListener listener) throws IOException {
        if (scmVars == null) return null;
        final PrintStream buildLog = listener.getLogger();
        final String url = scmVars.get(GIT_URL_PROPERTY);
        final String branch = scmVars.get("GIT_BRANCH");
        final String sha = scmVars.get("GIT_COMMIT");
        buildLog.println(CompareCoverageAction.BUILD_LOG_PREFIX + String.format("Attempt to discover PR for %s @ %s", branch, sha));
        GHPullRequest gitPr = ServiceRegistry.getPullRequestRepository().getPullRequestFor(url, branch, sha);
        int id = gitPr.getNumber();
        buildLog.println(CompareCoverageAction.BUILD_LOG_PREFIX + String.format("Discovered PR %d", id));
        return id;
    }

    public static int getPrId(
            final Map<String, String> scmVars, final Run build, final TaskListener listener) throws IOException, InterruptedException {
        Integer id = getPullRequestBuilder(build, listener);
        if (id == null) id = getMultiBranch(scmVars, listener);
        if (id == null) throw new UnsupportedOperationException(
                "Can't find " + GIT_PR_ID_ENV_PROPERTY + " or scmVars in build variables!");
        return id;
    }

    public static String getGitUrl(final Map<String, String> scmVars, final Run build, final TaskListener listener) throws IOException, InterruptedException {
        Map<String, String> envVars = build.getEnvironment(listener);
        final String gitUrl = envVars.get(GIT_URL_PROPERTY);
        final String changeUrl = envVars.get(CHANGE_URL_PROPERTY);
        if (scmVars != null && scmVars.containsKey(GIT_URL_PROPERTY)) return scmVars.get(GIT_URL_PROPERTY);
        else if (gitUrl != null) return gitUrl;
        else if (changeUrl != null) return changeUrl;
        else throw new UnsupportedOperationException("Can't find " + GIT_URL_PROPERTY
                    + " or " + CHANGE_URL_PROPERTY + " in envs: " + envVars);
    }

}

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
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

public class PrIdAndUrlUtils {

    /**
     * Injected by Git plugin
     */
    public static final String GIT_URL_PROPERTY = "GIT_URL";
    public static final String GIT_BRANCH_PROPERTY = "GIT_BRANCH";

    /**
     * Injected by
     * https://wiki.jenkins-ci.org/display/JENKINS/GitHub+pull+request+builder+plugin
     */
    public static final String GIT_PR_ID_ENV_PROPERTY = "pullRequestId";
    public static final String GIT_TARGETBRANCH_ENV_PROPERTY = "targetBranch";

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
        final String branch = scmVars.get("GIT_BRANCH");
        final String sha = scmVars.get("GIT_COMMIT");
        buildLog.println(CompareCoverageAction.BUILD_LOG_PREFIX + String.format("Attempt to discover PR for %s @ %s", branch, sha));
        PullRequest gitPr = ServiceRegistry.getPullRequestRepository().getPullRequestForId(branch, sha);
        int id = Integer.parseInt(gitPr.getId());
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

    /**
     * Returns the target branch read from environment variables targetBranch or GIT_BRANCH.
     * Removes prefix "origin/"
     * If branch could not be determined, it will return "master"
     *
     * @param envVars
     * @return
     */
    public static String getTargetBranch(Map<String, String> envVars) {
        final String branch = StringUtils.defaultIfBlank(envVars.get(GIT_TARGETBRANCH_ENV_PROPERTY), envVars.get(GIT_BRANCH_PROPERTY));
        if (StringUtils.isBlank(branch)) {
            return "master";
        }

        if (StringUtils.startsWith(branch, "origin/")) {
            return branch.substring(7);
        }
        return branch;
    }

    @SneakyThrows
    public static String getGitUrlWithBranch(Run build, TaskListener listener) {
        Map<String, String> envVars = build.getEnvironment(listener);
        final String gitUrl = envVars.get(GIT_URL_PROPERTY);
        final String branch = envVars.get(GIT_BRANCH_PROPERTY);

        if (StringUtils.isBlank(branch)) {
            return gitUrl;
        }
        String combined = gitUrl + "#";
        if (StringUtils.startsWith(branch, "origin/")) {
            combined += branch.substring(7);
        } else {
            combined += branch;
        }
        return combined;
    }

    @SneakyThrows
    public static String getGitUrlForTargetBranch(Run build, TaskListener listener) {
        Map<String, String> envVars = build.getEnvironment(listener);
        final String gitUrl = envVars.get(GIT_URL_PROPERTY);

        String branch = getTargetBranch(envVars);
        return gitUrl + "#" + branch;
    }

}

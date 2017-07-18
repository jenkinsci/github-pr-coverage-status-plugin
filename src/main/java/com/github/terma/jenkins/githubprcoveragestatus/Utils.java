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

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("WeakerAccess")
class Utils {

    /**
     * Injected by Git plugin
     */
    public static final String GIT_URL_ENV_PROPERTY = "GIT_URL";

    public static final String BUILD_URL_ENV_PROPERTY = "BUILD_URL";

    /**
     * Injected by
     * https://wiki.jenkins-ci.org/display/JENKINS/GitHub+pull+request+builder+plugin
     */
    public static final String GIT_PR_ID_ENV_PROPERTY = "ghprbPullId";

    public static final Pattern HTTP_GITHUB_USER_REPO_PATTERN = Pattern.compile("^(http[s]?://[^/]*)/([^/]*/[^/]*).*");
    public static final Pattern SSH_GITHUB_USER_REPO_PATTERN = Pattern.compile("^.+:(.+)");

    public static final String CHANGE_ID_PROPERTY = "CHANGE_ID";
    public static final String CHANGE_URL_PROPERTY = "CHANGE_URL";

    /**
     * Extract repo name from Git URL.
     * For example: <code>https://github.com/terma/jenkins-github-coverage-updater.git</code>
     * Result: <code>jenkins-github-coverage-updater</code>
     *
     * @param gitRepoUrl - Git repository URL
     * @return repo name
     */
    public static String getRepoName(String gitRepoUrl) {
        String[] userRepo = getUserRepo(gitRepoUrl).split("/");
        if (userRepo.length < 2) throw new IllegalArgumentException("Bad Git repository URL: " + gitRepoUrl);
        return userRepo[1];
    }

    /**
     * Extract user name and repo name from Git URL.
     * For example: <code>https://github.com/terma/jenkins-github-coverage-updater.git</code>
     * Result: <code>terma/jenkins-github-coverage-updater</code>
     *
     * @param gitRepoUrl - Git repository URL
     * @return user name with repo name
     */
    public static String getUserRepo(final String gitRepoUrl) {
        String userRepo = null;

        if (gitRepoUrl != null) {
            Matcher m = HTTP_GITHUB_USER_REPO_PATTERN.matcher(gitRepoUrl);
            if (m.matches()) userRepo = m.group(2);

            if (userRepo == null) {
                m = SSH_GITHUB_USER_REPO_PATTERN.matcher(gitRepoUrl);
                if (m.matches()) userRepo = m.group(1);
            }
        }

        if (userRepo == null) {
            throw new IllegalStateException(String.format("Invalid Git Hub repository URL: %s", gitRepoUrl));
        }

        if (userRepo.endsWith(".git")) userRepo = userRepo.substring(0, userRepo.length() - ".git".length());
        return userRepo;
    }

    public static String getJenkinsUrlFromBuildUrl(String buildUrl) {
        final String keyword = "/job/";
        final int index = buildUrl.indexOf(keyword);
        if (index < 0) throw new IllegalArgumentException("Invalid build URL: " + buildUrl + "!");
        return buildUrl.substring(0, index);
    }

    public static String getGitUrl(Run build, TaskListener listener) throws IOException, InterruptedException {
        final EnvVars envVars = build.getEnvironment(listener);
        final String gitUrl = envVars.get(GIT_URL_ENV_PROPERTY);
        final String changeUrl = envVars.get(CHANGE_URL_PROPERTY);
        if (gitUrl != null) return gitUrl;
        else if (changeUrl != null) return changeUrl;
        else throw new UnsupportedOperationException("Can't find " + GIT_URL_ENV_PROPERTY
                    + " or " + CHANGE_URL_PROPERTY + " in envs: " + envVars);
    }

    public static String getBuildUrl(Run build, TaskListener listener) throws IOException, InterruptedException {
        final EnvVars envVars = build.getEnvironment(listener);
        return envVars.get(BUILD_URL_ENV_PROPERTY);
    }

    public static int gitPrId(Run build, TaskListener listener) throws IOException, InterruptedException {
        final EnvVars envVars = build.getEnvironment(listener);
        final String gitPrId = envVars.get(GIT_PR_ID_ENV_PROPERTY);
        final String changeId = envVars.get(CHANGE_ID_PROPERTY);
        final String prIdString = gitPrId != null ? gitPrId : changeId;
        if (prIdString == null) {
            throw new UnsupportedOperationException("Can't find " + GIT_PR_ID_ENV_PROPERTY
                    + " or " + CHANGE_ID_PROPERTY + " in envs: " + envVars);
        } else {
            return Integer.parseInt(prIdString);
        }
    }

}

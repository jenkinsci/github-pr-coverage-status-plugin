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
import java.io.PrintStream;
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

    public static String getUserRepo(final String url) {
        String userRepo = null;

        if (url != null) {
            Matcher m = HTTP_GITHUB_USER_REPO_PATTERN.matcher(url);
            if (m.matches()) userRepo = m.group(2);

            if (userRepo == null) {
                m = SSH_GITHUB_USER_REPO_PATTERN.matcher(url);
                if (m.matches()) userRepo = m.group(1);
            }
        }

        if (userRepo == null) {
            throw new IllegalStateException(String.format("Invalid GitHub project url: %s", url));
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
        return gitUrl != null ? gitUrl : changeUrl;
    }

    public static String getBuildUrl(Run build, TaskListener listener) throws IOException, InterruptedException {
        final EnvVars envVars = build.getEnvironment(listener);
        return envVars.get(BUILD_URL_ENV_PROPERTY);
    }

    public static Integer gitPrId(Run build, TaskListener listener, PrintStream buildLog) throws IOException, InterruptedException {
        final EnvVars envVars = build.getEnvironment(listener);
        buildLog.println(envVars.toString());
        final String gitPrId = System.getenv(GIT_PR_ID_ENV_PROPERTY);
        final String changeId = System.getenv(CHANGE_ID_PROPERTY);
        final String prIdString = gitPrId != null ? gitPrId : changeId;
        if (prIdString == null) {
            return null;
        } else {
            return Integer.parseInt(prIdString);
        }
    }

}

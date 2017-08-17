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

    public static final String JOB_NAME = "JOB_NAME_ENV_PROPERTY";

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

    public static final String CHANGE_ID_PROPERTY = "CHANGE_ID";
    public static final String CHANGE_URL_PROPERTY = "CHANGE_URL";

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

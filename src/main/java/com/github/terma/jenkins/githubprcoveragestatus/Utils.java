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

@SuppressWarnings("WeakerAccess")
class Utils {

    public static final String BUILD_URL_ENV_PROPERTY = "BUILD_URL";
    public static final String TARGET_BRANCH = "CHANGE_TARGET";

    public static String getJenkinsUrlFromBuildUrl(String buildUrl) {
        final String keyword = "/job/";
        final int index = buildUrl.indexOf(keyword);
        if (index < 0) throw new IllegalArgumentException("Invalid build URL: " + buildUrl + "!");
        return buildUrl.substring(0, index);
    }

    public static String getBuildUrl(Run build, TaskListener listener) throws IOException, InterruptedException {
        final EnvVars envVars = build.getEnvironment(listener);
        return envVars.get(BUILD_URL_ENV_PROPERTY);
    }

    public static String getTargetBranch(Run build, TaskListener listener) throws IOException, InterruptedException {
        final EnvVars envVars = build.getEnvironment(listener);
        return envVars.get(TARGET_BRANCH);
    }

}

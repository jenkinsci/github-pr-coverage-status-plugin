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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("WeakerAccess")
public class GitUtils {

    public static final Pattern HTTP_GITHUB_REPO_URL = Pattern.compile("^(http[s]?://[^/]*/[^/]*/[^/]*).*");

    public static final Pattern HTTP_GITHUB_USER_REPO_PATTERN = Pattern.compile("^(http[s]?://[^/]*)/([^/]*/[^/]*).*");
    public static final Pattern SSH_GITHUB_USER_REPO_PATTERN = Pattern.compile("^.+:(.+)");

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
     * Extract repo URL part form Git URL. For example <code>https://github.com/terma/test/pull/1</code>
     * should be converted to <code>https://github.com/terma/test</code>
     *
     * @param gitUrl - any type of Git URL
     * @return repo URL exclude branches or pull request parts
     */
    public static String getRepoUrl(String gitUrl) {
        String repoUrl = null;

        if (gitUrl != null) {
            if (gitUrl.startsWith("git@")) {
                repoUrl = gitUrl;
            } else {
                Matcher m = HTTP_GITHUB_REPO_URL.matcher(gitUrl);
                if (m.matches()) repoUrl = m.group(1);
            }
        }

        if (repoUrl == null) {
            throw new IllegalArgumentException(String.format("Invalid Git Hub repository URL: %s", gitUrl));
        }

        if (repoUrl.endsWith(".git")) repoUrl = repoUrl.substring(0, repoUrl.length() - ".git".length());
        return repoUrl;
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
            throw new IllegalArgumentException(String.format("Invalid Git Hub repository URL: %s", gitRepoUrl));
        }

        if (userRepo.endsWith(".git")) userRepo = userRepo.substring(0, userRepo.length() - ".git".length());
        return userRepo;
    }
}

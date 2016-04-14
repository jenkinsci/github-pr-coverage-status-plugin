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

import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("WeakerAccess")
class CachedGitHubRepository {

    private static final transient Logger logger = Logger.getLogger(CachedGitHubRepository.class.getName());

    private transient GHRepository gitHubRepository;

    public GHPullRequest getPullRequest(final String gitHubUrl, final int prId) throws IOException {
        if (gitHubRepository == null) initGitHubRepository(gitHubUrl);
        return gitHubRepository.getPullRequest(prId);
    }

    private GitHub getGitHub() throws IOException {
        final String apiUrl = Configuration.getGitHubApiUrl();
        final String personalAccessToken = Configuration.getPersonalAccessToken();

        if (apiUrl != null) {
            if (personalAccessToken != null) {
                return GitHub.connectToEnterprise(apiUrl, personalAccessToken);
            } else {
                return GitHub.connectToEnterpriseAnonymously(apiUrl);
            }
        } else {
            if (personalAccessToken != null) {
                return GitHub.connectUsingOAuth(personalAccessToken);
            } else {
                return GitHub.connectAnonymously();
            }
        }
    }

    private boolean initGitHubRepository(final String gitHubUrl) {
        if (gitHubRepository != null) {
            return true;
        }

        GitHub gitHub;

        try {
            gitHub = getGitHub();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error while accessing rate limit API", ex);
            return false;
        }

        if (gitHub == null) {
            logger.log(Level.SEVERE, "No connection returned to GitHub server!");
            return false;
        }

        try {
            if (gitHub.getRateLimit().remaining == 0) {
                logger.log(Level.INFO, "Exceeded rate limit for repository");
                return false;
            }
        } catch (FileNotFoundException ex) {
            logger.log(Level.INFO, "Rate limit API not found.");
            return false;
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Error while accessing rate limit API", ex);
            return false;
        }

        final String userRepo = Utils.getUserRepo(gitHubUrl);

        try {
            gitHubRepository = gitHub.getRepository(userRepo);
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Could not retrieve GitHub repository named " + userRepo
                    + " (Do you have properly set 'GitHub project' field in job configuration?)", ex);
            return false;
        }
        return true;
    }

}

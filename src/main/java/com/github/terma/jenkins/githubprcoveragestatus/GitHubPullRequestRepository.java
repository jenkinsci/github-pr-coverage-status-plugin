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

import org.kohsuke.github.*;

import java.io.FileNotFoundException;
import java.io.IOException;

public class GitHubPullRequestRepository implements PullRequestRepository {

    @Override
    public GHPullRequest getPullRequestFor(String gitHubUrl, String branch, String sha) throws IOException {
        for (GHPullRequest pr : getGitHubRepository(gitHubUrl).getPullRequests(GHIssueState.OPEN)) {
            if (pr.getHead().getRef().equals(branch) && pr.getHead().getSha().equals(sha)) {
                return pr;
            }
        }
        throw new IOException(String.format("No PR found for %s %s @ %s", gitHubUrl, branch, sha));
    }

    @Override
    public GHRepository getGitHubRepository(final String gitHubUrl) throws IOException {
        GitHub gitHub = getGitHub();

        try {
            if (gitHub.getRateLimit().remaining == 0) {
                throw new IOException("Exceeded rate limit for repository");
            }
        } catch (FileNotFoundException ex) {
            throw new IOException("Rate limit API not found.");
        } catch (IOException ex) {
            throw new IOException("Error while accessing rate limit API", ex);
        }

        final String userRepo = GitUtils.getUserRepo(gitHubUrl);

        try {
            return gitHub.getRepository(userRepo);
        } catch (IOException ex) {
            throw new IOException("Could not retrieve GitHub repository named " + userRepo
                    + " (Do you have properly set 'GitHub project' field in job configuration?)", ex);
        }
    }

    private static GitHub getGitHub() throws IOException {
        final SettingsRepository settingsRepository = ServiceRegistry.getSettingsRepository();
        final String apiUrl = settingsRepository.getGitHubApiUrl();
        final String personalAccessToken = settingsRepository.getPersonalAccessToken();

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

    @Override
    public void comment(final GHRepository ghRepository, final int prId, final String message) throws IOException {
        ghRepository.getPullRequest(prId).comment(message);
    }

    @Override
    public void createCommitStatus(
            GHRepository ghRepository,
            String sha1, GHCommitState state,
            String targetUrl,
            String description
    ) throws IOException {
        ghRepository.createCommitStatus(sha1, state, targetUrl, description, "test-coverage-plugin");
    }
}

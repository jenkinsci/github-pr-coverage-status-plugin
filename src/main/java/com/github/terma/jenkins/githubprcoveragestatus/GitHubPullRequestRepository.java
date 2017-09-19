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

import com.cdancy.bitbucket.rest.BitbucketApi;
import com.cdancy.bitbucket.rest.BitbucketClient;
import com.cdancy.bitbucket.rest.domain.pullrequest.PullRequest;
import com.cdancy.bitbucket.rest.domain.pullrequest.PullRequestPage;
import com.cdancy.bitbucket.rest.domain.repository.Repository;

import java.io.IOException;

public class GitHubPullRequestRepository implements PullRequestRepository {

    private final BitbucketApi bitbucketApi;
    private final String repositoryName;
    private final String projectName;

    public GitHubPullRequestRepository() {
        final SettingsRepository settingsRepository = ServiceRegistry.getSettingsRepository();
        projectName = settingsRepository.getBitbucketProject();
        repositoryName = settingsRepository.getBitbucketRepository();
        final String apiUrl = settingsRepository.getGitHubApiUrl();
        final String personalAccessToken = settingsRepository.getPersonalAccessToken();

        bitbucketApi = BitbucketClient.builder().endPoint(apiUrl).credentials(personalAccessToken).build().api();
    }

    @Override
    public PullRequest getPullRequestFor(String gitHubUrl, String branch, String sha) throws IOException {
        final PullRequestPage list = bitbucketApi.pullRequestApi().list(projectName, repositoryName,
            "incoming", null, "OPEN", "NEWEST", true, true, 0, 9999);

        for (PullRequest pr : list.values()) {
            if (pr.fromRef().id().equals(branch) && pr.fromRef().latestCommit().equals(sha)) {
                return pr;
            }
        }
        throw new IOException(String.format("No PR found for %s %s @ %s", gitHubUrl, branch, sha));
    }

    @Override
    public Repository getBitbucketRepository(final String gitHubUrl) throws IOException {
        return bitbucketApi.repositoryApi().get(projectName, repositoryName);
    }


    @Override
    public void comment(final Repository repository, final int prId, final String message) throws IOException {
        bitbucketApi.commentsApi().comment(projectName, repositoryName, repository.id(), message);
    }

}

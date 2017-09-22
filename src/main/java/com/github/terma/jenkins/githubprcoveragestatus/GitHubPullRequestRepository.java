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

import com.github.terma.jenkins.githubprcoveragestatus.stash.StashApiClient;
import com.github.terma.jenkins.githubprcoveragestatus.stash.StashPullRequestResponseValue;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.List;

public class GitHubPullRequestRepository implements BitbucketApi {

    private final StashApiClient stashApiClient;

    public GitHubPullRequestRepository(StashApiClient stashApiClient) {
        this.stashApiClient = stashApiClient;
    }

    @Override
    public PullRequest getPullRequestForId(String branch, String sha) throws IOException {
        final List<StashPullRequestResponseValue> pullRequests = stashApiClient.getPullRequests();
        for (StashPullRequestResponseValue pullRequest : pullRequests) {
            if ((StringUtils.equals(pullRequest.getFromRef().getBranch().getName(), branch)) &&
                ((StringUtils.equals(pullRequest.getFromRef().getCommit().getHash(), sha)))) {
                return new PullRequest(pullRequest.getId(), pullRequest.getTitle());
            }
        }
        throw new IOException(String.format("No PR found for %s @ %s", branch, sha));
    }

    @Override
    public void comment(final String prId, final String message) throws IOException {
        stashApiClient.postPullRequestComment(prId, message);
    }

}

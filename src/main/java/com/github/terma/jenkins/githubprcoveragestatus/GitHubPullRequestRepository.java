package com.github.terma.jenkins.githubprcoveragestatus;

import java.io.IOException;

public class GitHubPullRequestRepository implements PullRequestRepository {
    @Override
    public void comment(String gitUrl, int prId, String message) throws IOException {
        new CachedGitHubRepository().getPullRequest(gitUrl, prId).comment(message);
    }
}

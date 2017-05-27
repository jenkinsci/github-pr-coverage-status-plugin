package com.github.terma.jenkins.githubprcoveragestatus;

import java.io.IOException;

import org.kohsuke.github.GHRepository;

interface PullRequestRepository {

    GHRepository getGitHubRepository(final String gitHubUrl) throws IOException;

    void comment(GHRepository ghRepository, int prId, String message) throws IOException;

}

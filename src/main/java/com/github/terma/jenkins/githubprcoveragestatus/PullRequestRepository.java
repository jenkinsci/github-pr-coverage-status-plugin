package com.github.terma.jenkins.githubprcoveragestatus;

import java.io.IOException;

interface PullRequestRepository {

    void comment(String gitUrl, int prId, String message) throws IOException;

}

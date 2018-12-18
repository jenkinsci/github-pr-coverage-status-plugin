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

import org.kohsuke.github.GHCommitState;
import org.kohsuke.github.GHPullRequest;
import org.kohsuke.github.GHRepository;

import java.io.IOException;

interface PullRequestRepository {

    GHPullRequest getPullRequestFor(String repo, String branch, String sha) throws IOException;

    GHRepository getGitHubRepository(final String gitHubUrl) throws IOException;

    void comment(GHRepository ghRepository, int prId, String message) throws IOException;

    void createCommitStatus(GHRepository ghRepository, String sha1, GHCommitState state, String targetUrl, String description);
}

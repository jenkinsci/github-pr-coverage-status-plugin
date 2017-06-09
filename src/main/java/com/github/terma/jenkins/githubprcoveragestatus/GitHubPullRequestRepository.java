package com.github.terma.jenkins.githubprcoveragestatus;

import org.kohsuke.github.GHIssueComment;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

public class GitHubPullRequestRepository implements PullRequestRepository {

    private PrintStream buildLog;

    public GitHubPullRequestRepository(PrintStream buildLog) {
        this.buildLog = buildLog;
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

        final String userRepo = Utils.getUserRepo(gitHubUrl);

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
        buildLog.printf("Sending message %s for prId %d", message, prId);
        buildLog.println();
        GHIssueComment commentResponse = ghRepository.getPullRequest(prId).comment(message);
        buildLog.printf("Got response %s, %d, %s", commentResponse.getBody(), commentResponse.getId(), commentResponse.getUser().getName());
        buildLog.println();

    }
}

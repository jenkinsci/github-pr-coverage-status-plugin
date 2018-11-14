package com.github.terma.jenkins.githubprcoveragestatus;

import hudson.model.TaskListener;
import org.kohsuke.github.GHCommitState;
import org.kohsuke.github.GHPullRequestCommitDetail;
import org.kohsuke.github.GHRepository;

import java.io.PrintWriter;
import java.util.List;

public class ResultPublisher {
    public void publish(String publishAs, GHRepository gitHubRepository, int prId, String buildUrl, float targetCoverage, float coverage, final TaskListener listener,
                        Message message, String jenkinsUrl, SettingsRepository settingsRepository) {
        if ("comment".equalsIgnoreCase(publishAs)) {
            publishComment(message, buildUrl, jenkinsUrl, settingsRepository, gitHubRepository, prId, listener);
        } else {
            publishStatusCheck(gitHubRepository, prId, targetCoverage, coverage, buildUrl, listener);
        }
    }

    private void publishComment(
            Message message, String buildUrl, String jenkinsUrl, SettingsRepository settingsRepository,
            GHRepository gitHubRepository, int prId, TaskListener listener) {
        try {
            final String comment = message.forComment(
                    buildUrl,
                    jenkinsUrl,
                    settingsRepository.getYellowThreshold(),
                    settingsRepository.getGreenThreshold(),
                    settingsRepository.isPrivateJenkinsPublicGitHub());
            ServiceRegistry.getPullRequestRepository().comment(gitHubRepository, prId, comment);
        } catch (Exception ex) {
            PrintWriter pw = listener.error("Couldn't add comment to pull request #" + prId + "!");
            ex.printStackTrace(pw);
        }
    }

    private void publishStatusCheck(GHRepository gitHubRepository, int prId, float targetCoverage,
                                    float coverage, String buildUrl, TaskListener listener) {
        try {
            List<GHPullRequestCommitDetail> commits = gitHubRepository.getPullRequest(prId).listCommits().asList();
            for (int i = 0; i < commits.size(); i++) {
                if (i == commits.size() - 1) {
                    gitHubRepository.createCommitStatus(commits.get(i).getSha(), GHCommitState.SUCCESS, buildUrl,
                            targetCoverage + " vs " + coverage);
                }
            }
        } catch (Exception e) {
            PrintWriter pw = listener.error("Couldn't add status check to pull request #" + prId + "!");
            e.printStackTrace(pw);
        }
    }
}

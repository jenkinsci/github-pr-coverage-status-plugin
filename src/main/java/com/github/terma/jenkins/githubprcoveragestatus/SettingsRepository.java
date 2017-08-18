package com.github.terma.jenkins.githubprcoveragestatus;

interface SettingsRepository {

    String getGitHubApiUrl();

    String getPersonalAccessToken();

    String getJenkinsUrl();

    int getYellowThreshold();

    int getGreenThreshold();

    boolean isPrivateJenkinsPublicGitHub();

    boolean isUseSonarForMasterCoverage();

    String getSonarUrl();

    String getSonarToken();
}

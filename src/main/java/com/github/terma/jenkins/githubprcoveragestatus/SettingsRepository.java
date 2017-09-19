package com.github.terma.jenkins.githubprcoveragestatus;

interface SettingsRepository {

    String getGitHubApiUrl();

    String getPersonalAccessToken();

    String getJenkinsUrl();

    int getYellowThreshold();

    int getGreenThreshold();

    boolean isPrivateJenkinsPublicGitHub();

    boolean isUseSonarForMasterCoverage();

    boolean isDisableSimpleCov();

    String getSonarUrl();

    String getSonarToken();

    String getBitbucketProject();

    String getBitbucketRepository();
}

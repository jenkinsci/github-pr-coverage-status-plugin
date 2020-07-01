package com.github.terma.jenkins.githubprcoveragestatus;

interface SettingsRepository {

    String getGitHubApiUrl();

    String getPersonalAccessToken();

    String getJenkinsUrl();

    int getYellowThreshold();

    int getGreenThreshold();
    
    float getTolerance();

    boolean isPrivateJenkinsPublicGitHub();

    boolean isUseSonarForMasterCoverage();

    boolean isDisableSimpleCov();

    String getSonarUrl();

    String getSonarToken();
}

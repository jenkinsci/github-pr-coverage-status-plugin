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
    
    boolean isUseAggregatesForCoverage();

    String getSonarUrl();

    String getSonarToken();

    String getSonarCoverageMetric();
    
    int getCoverageRoundingDigits();
}

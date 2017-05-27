package com.github.terma.jenkins.githubprcoveragestatus;

interface MasterCoverageRepository {

    float get(final String repoName);

}

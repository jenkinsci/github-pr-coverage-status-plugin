package com.github.terma.jenkins.githubprcoveragestatus;

interface MasterCoverageRepository {

    float get(String gitUrl);

}

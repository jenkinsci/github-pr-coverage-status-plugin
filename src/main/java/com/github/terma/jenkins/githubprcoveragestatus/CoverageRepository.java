package com.github.terma.jenkins.githubprcoveragestatus;

import hudson.FilePath;

import java.io.IOException;

interface CoverageRepository {

    float get(FilePath workspace) throws IOException, InterruptedException;

}

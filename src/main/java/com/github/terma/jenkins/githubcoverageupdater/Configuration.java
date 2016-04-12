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
package com.github.terma.jenkins.githubcoverageupdater;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("WeakerAccess")
public class Configuration extends AbstractDescribableImpl<Configuration> {

    @Extension
    public static final MasterCoverageDescriptor DESCRIPTOR = new MasterCoverageDescriptor();

    @DataBoundConstructor
    public Configuration() {
    }

    public static String getGitHubApiUrl() {
        return DESCRIPTOR.getGitHubApiUrl();
    }

    public static String getPersonalAccessToken() {
        return DESCRIPTOR.getPersonalAccessToken();
    }

    public static float getMasterCoverage(final String repo) {
        return DESCRIPTOR.get(repo);
    }

    public static void setMasterCoverage(final String repo, final float coverage) {
        DESCRIPTOR.set(repo, coverage);
    }

    @Override
    public MasterCoverageDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @SuppressWarnings("unused")
    public static final class MasterCoverageDescriptor extends Descriptor<Configuration> {

        private final Map<String, Float> coverageByRepo = new ConcurrentHashMap<String, Float>();

        private String gitHubApiUrl;
        private String personalAccessToken;

        public MasterCoverageDescriptor() {
            load();
        }

        @Override
        public String getDisplayName() {
            return "Master Coverage";
        }

        public float get(String repo) {
            final Float coverage = coverageByRepo.get(repo);
            return coverage == null ? 0 : coverage;
        }

        public void set(String repo, float coverage) {
            coverageByRepo.put(repo, coverage);
            save();
        }

        public Map<String, Float> getCoverageByRepo() {
            return coverageByRepo;
        }

        public String getGitHubApiUrl() {
            return gitHubApiUrl;
        }

        public String getPersonalAccessToken() {
            return personalAccessToken;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            gitHubApiUrl = StringUtils.trimToNull(formData.getString("gitHubApiUrl"));
            personalAccessToken = StringUtils.trimToNull(formData.getString("personalAccessToken"));
            save();
            return super.configure(req, formData);
        }

    }

}

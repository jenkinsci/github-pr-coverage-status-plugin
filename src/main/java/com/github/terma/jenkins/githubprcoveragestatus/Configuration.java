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
package com.github.terma.jenkins.githubprcoveragestatus;

import hudson.Extension;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@SuppressWarnings("WeakerAccess")
public class Configuration extends AbstractDescribableImpl<Configuration> {

    public static final int DEFAULT_YELLOW_THRESHOLD = 80;
    public static final int DEFAULT_GREEN_THRESHOLD = 90;
    public static final boolean DEFAULT_NEGATIVE_COVERAGE_IS_RED = false;

    @Extension
    public static final ConfigurationDescriptor DESCRIPTOR = new ConfigurationDescriptor();

    @DataBoundConstructor
    public Configuration() {
    }

    public static void setMasterCoverage(final String repo, final float coverage) {
        DESCRIPTOR.set(repo, coverage);
    }

    @Override
    public ConfigurationDescriptor getDescriptor() {
        return DESCRIPTOR;
    }

    @SuppressWarnings("unused")
    public static final class ConfigurationDescriptor extends Descriptor<Configuration> {

        private final Map<String, Float> coverageByRepo = new ConcurrentHashMap<>();

        public ConfigurationDescriptor() {
            load();
        }

        @Override
        public String getDisplayName() {
            return "Coverage status for GitHub Pull Requests";
        }

        @Nonnull
        public Map<String, Float> getCoverageByRepo() {
            return coverageByRepo;
        }

        public void set(String repo, float coverage) {
            coverageByRepo.put(repo, coverage);
            save();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            save();
            return super.configure(req, formData);
        }

    }

}

package com.github.terma.jenkins.githubprcoveragestatus;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

import java.io.IOException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.lang.StringUtils;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SonarMasterCoverageRepository implements MasterCoverageRepository {

    private static final transient Logger logger = Logger.getLogger(SonarMasterCoverageRepository.class.getName());

    private static final String SONAR_SEARCH_PROJECTS_API_PATH = "/api/projects/index";
    private static final String SONAR_COMPONENT_MEASURE_API_PATH = "/api/measures/component";
    private static final String SONAR_OVERALL_LINE_COVERAGE_METRIC_NAME = "overall_line_coverage";

    private final String sonarEndpoint;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper().disable(FAIL_ON_UNKNOWN_PROPERTIES);

    public SonarMasterCoverageRepository(String sonarEndpoint) {
        this.sonarEndpoint = sonarEndpoint;
        httpClient = new HttpClient();
    }

    @Override
    public float get(String gitUrl) {
        logger.log(INFO, "Getting coverage for {0}", gitUrl);

        try {
            final SonarProject sonarProject = getSonarProject(gitUrl);
            if (sonarProject != null) {
                return getCoverageMeasure(sonarProject);
            } else {
                return 0;
            }
        } catch (Exception e) {
            logger.log(SEVERE, "failed to get master coverage for " + gitUrl, e);
            return 0;
        }
    }

    /**
     * Try to find the project in sonarqube based on the repo name from the git uri
     */
    private SonarProject getSonarProject(String gitUrl) throws Exception {
        String repoName = StringUtils.substringAfterLast(StringUtils.removeEnd(gitUrl, ".git"), "/");

        final String searchUri = sonarEndpoint + SONAR_SEARCH_PROJECTS_API_PATH + "?search=" + repoName;
        try {
            final GetMethod method = executeGetRequest(searchUri);
            List<SonarProject> sonarProjects = objectMapper.readValue(method.getResponseBodyAsStream(), new TypeReference<List<SonarProject>>() {});

            if (sonarProjects.isEmpty()) {
                logger.log(WARNING, "No sonar project found for repo {0}", repoName);
                throw new RuntimeException("No sonar project found for repo" + repoName);
            } else if (sonarProjects.size() == 1) {
                logger.log(INFO, "Found project for repo name {0} - {1}", new Object[]{repoName, sonarProjects.get(0)});
                return sonarProjects.get(0);
            } else {
                logger.log(INFO, "Found multiple projects for repo name {0} - found {1} - returning first result", new Object[]{repoName, sonarProjects.toArray()});
                return sonarProjects.get(0);
            }
        } catch (Exception e) {
            logger.log(SEVERE, "failed to search for sonar project {0} - {1}", new Object[]{searchUri, e.getMessage()});
            throw e;
        }
    }

    /**
     * Try to find code coverage measure for project in sonarqube
     */
    private float getCoverageMeasure(SonarProject project) throws Exception {

        final String uri = MessageFormat.format("{0}{1}?componentKey={2}&metricKeys={3}", sonarEndpoint, SONAR_COMPONENT_MEASURE_API_PATH, URLEncoder.encode(project.getKey()), SONAR_OVERALL_LINE_COVERAGE_METRIC_NAME);
        try {
            final GetMethod method = executeGetRequest(uri);
            String value = JsonUtils.findInJson(method.getResponseBodyAsString(), "component.measures[0].value");
            return Float.valueOf(value) / 100;
        } catch (Exception e) {
            logger.log(SEVERE, "failed to get coverage for sonar project {0} {1}", new Object[]{uri, e});
            throw e;
        }
    }

    private GetMethod executeGetRequest(String uri) throws IOException, HttpClientException {
        final GetMethod method = new GetMethod(uri);
        int status = httpClient.executeMethod(method);
        if (status >= 400) {
            throw new HttpClientException(uri, status, method.getResponseBodyAsString());
        }
        return method;
    }

    private static class  HttpClientException extends Exception {
        HttpClientException(String uri, int status, String reason) {
            super("request to "  + uri + " failed with " + status + " reason " + reason);
        }
    }

    private static class SonarProject {
        @JsonProperty("k")
        String key;

        String getKey() {
            return key;
        }

        void setKey(String key) {
            this.key = key;
        }

        @Override
        public String toString() {
            return MessageFormat.format("({0}(key = {1}))", this.getClass().getSimpleName(), key);
        }
    }
}

package com.github.terma.jenkins.githubprcoveragestatus;

import com.github.tomakehurst.wiremock.client.MappingBuilder;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.google.common.base.Charsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class SonarMasterCoverageRepositoryTest {

    private static final String REPO_NAME = "my-project";
    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(0));

    private SonarMasterCoverageRepository sonarMasterCoverageRepository;
    private ByteArrayOutputStream buildLogOutputStream;

    @After
    public void afterTest() throws Exception {
        System.out.println(buildLogOutputStream.toString());
        wireMockRule.resetMappings();
    }

    @Test
    public void should_get_coverage() throws IOException {
        testCoverage(null, null);
        testCoverage("token", "");
        testCoverage("login", "password");
    }

    private void testCoverage(final String login, final String password) throws IOException {
        givenCoverageRepository(login, password);
        givenProjectResponseWithSingleMatch(login, password);

        givenMeasureResponse();

        final float coverage = sonarMasterCoverageRepository.get(REPO_NAME);
        assertThat(coverage, is(0.953f));
    }

    @Test
    public void should_get_coverage_for_multiple_projects_found() throws IOException {
        givenCoverageRepository(null, null);

        givenProjectResponseWithMultipleMatches();
        givenMeasureResponse();

        final float coverage = sonarMasterCoverageRepository.get(REPO_NAME);
        assertThat(coverage, is(0.953f));
    }

    @Test
    public void should_get_zero_coverage_for_not_found() {
        givenCoverageRepository(null, null);

        givenProjectResponseWithoutMatch();

        assertThat(sonarMasterCoverageRepository.get(REPO_NAME), is(0f));
    }

    @Test
    public void should_get_zero_coverage_for_unknown_metric() throws IOException {
        givenCoverageRepository(null, null);

        givenProjectResponseWithSingleMatch(null, null);
        givenNotFoundMeasureResponse();

        assertThat(sonarMasterCoverageRepository.get(REPO_NAME), is(0f));
    }

    private void givenCoverageRepository(final String login, String password) {
        buildLogOutputStream = new ByteArrayOutputStream();
        sonarMasterCoverageRepository = new SonarMasterCoverageRepository("http://localhost:" + wireMockRule.port(),
                                                                          login, password, new PrintStream(buildLogOutputStream, true));
    }

    private void givenProjectResponseWithSingleMatch(final String login, String password) throws IOException {
        final MappingBuilder search = get(urlPathEqualTo("/api/projects/index"))
                                      .withQueryParam("search", equalTo(REPO_NAME));
        if (login != null) {
            search.withBasicAuth(login, password);
        }
        wireMockRule.stubFor(search.willReturn(
                            aResponse().withStatus(200)
                                       .withBody(getResponseBodyFromFile("singleProjectFound.json"))
                )
        );
    }

    private void givenProjectResponseWithMultipleMatches() throws IOException {
        wireMockRule.stubFor(get(urlPathEqualTo("/api/projects/index"))
                .withQueryParam("search", equalTo(REPO_NAME))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(getResponseBodyFromFile("multipleProjectsFound.json"))
                )
        );
    }

    private void givenProjectResponseWithoutMatch() {
        wireMockRule.stubFor(get(urlPathEqualTo("/api/projects/index"))
                .withQueryParam("search", equalTo(REPO_NAME))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("[]")
                )
        );
    }

    private void givenMeasureResponse() throws IOException {
        wireMockRule.stubFor(get(urlPathEqualTo("/api/measures/component"))
                .withQueryParam("componentKey", equalTo("my-project:origin/master"))
                .withQueryParam("metricKeys", equalTo(SonarMasterCoverageRepository.SONAR_OVERALL_LINE_COVERAGE_METRIC_NAME))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(getResponseBodyFromFile("measureFound.json"))
                )
        );
    }

    private void givenNotFoundMeasureResponse() throws IOException {
        wireMockRule.stubFor(get(urlPathEqualTo("/api/measures/component"))
                .withQueryParam("componentKey", equalTo("my-project:origin/master"))
                .withQueryParam("metricKeys", equalTo(SonarMasterCoverageRepository.SONAR_OVERALL_LINE_COVERAGE_METRIC_NAME))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withBody(getResponseBodyFromFile("metricNotFound.json"))
                )
        );
    }

    private String getResponseBodyFromFile(String fileName) throws IOException {
        return IOUtils.toString(this.getClass().getResourceAsStream("/com/github/terma/jenkins/githubprcoveragestatus/SonarMasterCoverageRepositoryTest/" + fileName), UTF_8);
    }
}
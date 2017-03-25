package com.github.terma.jenkins.githubprcoveragestatus;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.commons.io.IOUtils;
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

    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(0));

    private SonarMasterCoverageRepository sonarMasterCoverageRepository;

    @Test
    public void should_get_coverage() throws IOException {
        givenCoverageRepository();

        givenProjectResponseWithSingleMatch();
        givenMeasureResponse();

        final float coverage = sonarMasterCoverageRepository.get("git@github.com:some/my-project.git");
        assertThat(coverage, is(0.953f));
    }

    @Test
    public void should_get_zero_coverage_for_not_found() {
        givenCoverageRepository();

        givenProjectResponseWithoutMatch();

        assertThat(sonarMasterCoverageRepository.get("git@github.com:some/my-project.git"), is(0f));
    }

    @Test
    public void should_get_zero_coverage_for_unknown_metric() throws IOException {
        givenCoverageRepository();

        givenProjectResponseWithSingleMatch();
        givenNotFoundMeasureResponse();

        assertThat(sonarMasterCoverageRepository.get("git@github.com:some/my-project.git"), is(0f));
    }

    private void givenCoverageRepository() {
        ByteArrayOutputStream buildLogOutputStream = new ByteArrayOutputStream();
        sonarMasterCoverageRepository = new SonarMasterCoverageRepository("http://localhost:" + wireMockRule.port(),
                new PrintStream(buildLogOutputStream, true));
    }

    private void givenProjectResponseWithSingleMatch() throws IOException {
        wireMockRule.stubFor(get(urlPathEqualTo("/api/projects/index"))
                .withQueryParam("search", equalTo("my-project"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(getResponseBodyFromFile("singleProjectFound.json"))
                )
        );
    }

    private void givenProjectResponseWithoutMatch() {
        wireMockRule.stubFor(get(urlPathEqualTo("/api/projects/index"))
                .withQueryParam("search", equalTo("my-project"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody("[]")
                )
        );
    }

    private void givenMeasureResponse() throws IOException {
        wireMockRule.stubFor(get(urlPathEqualTo("/api/measures/component"))
                .withQueryParam("componentKey", equalTo("my-project:origin/master"))
                .withQueryParam("metricKeys", equalTo("overall_line_coverage"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withBody(getResponseBodyFromFile("measureFound.json"))
                )
        );
    }

    private void givenNotFoundMeasureResponse() throws IOException {
        wireMockRule.stubFor(get(urlPathEqualTo("/api/measures/component"))
                .withQueryParam("componentKey", equalTo("my-project:origin/master"))
                .withQueryParam("metricKeys", equalTo("overall_line_coverage"))
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
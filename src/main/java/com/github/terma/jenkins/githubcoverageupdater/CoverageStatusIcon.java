package com.github.terma.jenkins.githubcoverageupdater;

import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import java.io.IOException;

@Extension
public class CoverageStatusIcon implements UnprotectedRootAction {

    @Override
    public String getIconFileName() {
        return null;
    }

    @Override
    public String getDisplayName() {
        return null;
    }

    @Override
    public String getUrlName() {
        return "coverage-status-icon";
    }

    /**
     * Used by Jenkins Stapler service when get request on that URL
     *
     * @param request - request
     * @param response - response
     * @throws IOException
     */
    @SuppressWarnings("unused")
    public void doIndex(StaplerRequest request, StaplerResponse response) throws IOException {
        final float coverage = Float.parseFloat(request.getParameter("coverage"));
        final float masterCoverage = Float.parseFloat(request.getParameter("masterCoverage"));

        response.setContentType("image/svg+xml");

        String svg = IOUtils.toString(this.getClass().getResourceAsStream(
                "/com/github/terma/jenkins/githubcoverageupdater/Icon/icon.svg"));

        Message message = new Message(coverage, masterCoverage);
        svg = StringUtils.replace(svg, "{{ message }}", message.forIcon());

        String color;
        if (coverage < 0.8) color = "#b94947"; // red
        else if (coverage < 0.9) color = "#F89406"; // yellow
        else color = "#97CA00"; // green
        svg = StringUtils.replace(svg, "{{ color }}", color);

        response.getWriter().write(svg);
    }

}

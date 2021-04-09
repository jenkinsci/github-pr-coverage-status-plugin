package com.github.terma.jenkins.githubprcoveragestatus;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import hudson.Extension;
import hudson.model.UnprotectedRootAction;

@Extension
public class CoverageStatusIconAction implements UnprotectedRootAction {

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
     * Used by Jenkins Stapler service when get request on URL jenkins_host/getUrlName()
     *
     * @param request - request
     * @param response - response
     * @throws IOException
     */
    @SuppressWarnings("unused")
    public void doIndex(StaplerRequest request, StaplerResponse response) throws IOException {
        final float coverage = Float.parseFloat(request.getParameter("coverage"));
        final float masterCoverage = Float.parseFloat(request.getParameter("masterCoverage"));
        final String targetBranch = request.getParameter("targetBranch");

        response.setContentType("image/svg+xml");

        String svg = IOUtils.toString(this.getClass().getResourceAsStream(
                "/com/github/terma/jenkins/githubprcoveragestatus/Icon/icon.svg"));

        final Message message = new Message(coverage, masterCoverage);
        svg = StringUtils.replace(svg, "{{ message }}", message.forIcon(targetBranch));

        final int coveragePercent = Percent.of(coverage);
        String color;
        if (coveragePercent < Configuration.getYellowThreshold()) color = "#b94947"; // red
        else if (coveragePercent < Configuration.getGreenThreshold()) color = "#F89406"; // yellow
        else color = "#97CA00"; // green
        svg = StringUtils.replace(svg, "{{ color }}", color);

        response.getWriter().write(svg);
    }

}

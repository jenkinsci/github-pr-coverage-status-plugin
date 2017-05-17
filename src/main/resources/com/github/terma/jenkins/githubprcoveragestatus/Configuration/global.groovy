//j = namespace("jelly:core")
f = namespace("/lib/form")


f.section(title: descriptor.displayName) {

    f.entry(field: "gitHubApiUrl", title: _("GitHub API URL")) {
        f.textbox()
    }

    f.entry(field: "privateJenkinsPublicGitHub", title: _("Jenkins is not accessible for GitHub")) {
        f.checkbox()
    }

    f.entry(field: "personalAccessToken", title: _("GitHub Personal Access Token")) {
        f.password()
    }

    f.entry(field: "jenkinsUrl", title: _("Jenkins URL for icon in Pull Request")) {
        f.textbox()
    }

    f.entry(field: "yellowThreshold", title: _("Yellow threshold 0-100%")) {
        f.textbox()
    }

    f.entry(field: "greenThreshold", title: _("Green Threshold 0-100%")) {
        f.textbox()
    }

    f.entry(field: "useSonarForMasterCoverage", title: _("Use Sonar for master coverage")) {
        f.checkbox()
    }

    f.entry(field: "sonarUrl", title: _("Sonar endpoint URL")) {
        f.textbox()
    }

    f.entry(field: "sonarToken", title: _("Sonar access token")) {
        f.textbox()
    }

}

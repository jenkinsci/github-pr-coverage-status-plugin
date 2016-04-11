//j = namespace("jelly:core")
f = namespace("/lib/form")


f.section(title: descriptor.displayName) {

    f.entry(field: "gitHubApiUrl", title: _("GitHub API URL")) {
        f.textbox()
    }

    f.entry(field: "personalAccessToken", title: _("GitHub Personal Access Token")) {
        f.password()
    }

}

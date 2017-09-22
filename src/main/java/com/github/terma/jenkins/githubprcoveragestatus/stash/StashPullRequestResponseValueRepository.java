package com.github.terma.jenkins.githubprcoveragestatus.stash;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang.StringUtils;

/**
 * Created by Nathan McCarthy
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StashPullRequestResponseValueRepository {
    private static final String REFS_PREFIX = "refs/";
    private static final String HEADS_PREFIX = "heads/";
    private StashPullRequestResponseValueRepositoryRepository repository;

    @JsonIgnore
    private StashPullRequestResponseValueRepositoryBranch branch;

    @JsonIgnore
    private StashPullRequestResponseValueRepositoryCommit commit;

    private String latestChangeset;
    private String id;
    private String latestCommit;

    @JsonProperty("id")
    public String getId() {
        return id;
    }

    @JsonProperty("id")
    public void setId(String id) {
        this.id = id;
        this.branch = new StashPullRequestResponseValueRepositoryBranch();
        this.branch.setName(convertIdToBranchName(id));
    }

    /**
     * Convert a pull request identifier to a branch name. Assumption: A pull request identifier always looks like
     * "refs/heads/master". The branch name is without the "refs/heads/" part.
     * To be on the save side, this method will check for the "refs/" and the "heads/" and strip them accordingly.
     * <p>
     * More information about the Stash REST API can be found here:
     * <a href="https://developer.atlassian.com/stash/docs/latest/">https://developer.atlassian.com/stash/docs/latest/</a>
     *
     * @param id The unique name of the pull request.
     * @return The branch name
     */
    private String convertIdToBranchName(String id) {
        String branchName = StringUtils.EMPTY;
        if (StringUtils.isEmpty(id)) {
            return branchName;
        }

        branchName = id;

        if (StringUtils.startsWith(branchName, REFS_PREFIX)) {
            branchName = StringUtils.removeStart(branchName, REFS_PREFIX);
        }

        if (StringUtils.startsWith(branchName, HEADS_PREFIX)) {
            branchName = StringUtils.removeStart(branchName, HEADS_PREFIX);
        }

        return branchName;
    }

    @JsonProperty("latestChangeset")
    public String getLatestChangeset() {
        return latestChangeset;
    }

    @JsonProperty("latestChangeset")
    public void setLatestChangeset(String latestChangeset) { //TODO
        this.latestChangeset = latestChangeset;
        this.commit = new StashPullRequestResponseValueRepositoryCommit();
        this.commit.setHash(latestChangeset);
    }

    @JsonProperty("repository")
    public StashPullRequestResponseValueRepositoryRepository getRepository() {
        return repository;
    }

    @JsonProperty("repository")
    public void setRepository(StashPullRequestResponseValueRepositoryRepository repository) {
        this.repository = repository;
    }

    @JsonProperty("branch")
    public StashPullRequestResponseValueRepositoryBranch getBranch() {
        return branch;
    }

    @JsonProperty("branch")
    public void setBranch(StashPullRequestResponseValueRepositoryBranch branch) {
        this.branch = branch;
    }

    public StashPullRequestResponseValueRepositoryCommit getCommit() {
        return commit;
    }

    public void setCommit(StashPullRequestResponseValueRepositoryCommit commit) {
        this.commit = commit;
    }

    public String getLatestCommit() {
        if (commit != null) {
            return commit.getHash();
        }
        return latestCommit;
    }

    public void setLatestCommit(String latestCommit) {
        this.latestCommit = latestCommit;
    }
}



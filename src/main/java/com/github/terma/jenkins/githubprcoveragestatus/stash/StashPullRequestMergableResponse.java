package com.github.terma.jenkins.githubprcoveragestatus.stash;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;

/**
 * If pull request is mergeable
 * https://developer.atlassian.com/static/rest/stash/3.9.2/stash-rest.html#idp2785024
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StashPullRequestMergableResponse {

    private Boolean canMerge;
    private Boolean conflicted;
    private ArrayList<StashPullRequestMergableVetoMessage> vetoes;

    public Boolean getCanMerge() {
        return canMerge;
    }

    public void setCanMerge(Boolean canMerge) {
        this.canMerge = canMerge;
    }

    public Boolean getConflicted() {
        return conflicted;
    }

    public void setConflicted(Boolean conflicted) {
        this.conflicted = conflicted;
    }

    public ArrayList<StashPullRequestMergableVetoMessage> getVetoes() {
        return vetoes;
    }

    public void setVetoes(ArrayList<StashPullRequestMergableVetoMessage> vetoes) {
        this.vetoes = vetoes;
    }
}

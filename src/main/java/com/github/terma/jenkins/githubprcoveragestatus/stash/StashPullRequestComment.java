package com.github.terma.jenkins.githubprcoveragestatus.stash;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Created by Nathan McCarthy
 */
@SuppressFBWarnings("EQ_COMPARETO_USE_OBJECT_EQUALS")
@JsonIgnoreProperties(ignoreUnknown = true)
public class StashPullRequestComment implements Comparable<StashPullRequestComment> {

    private Integer commentId;//
    private String text;

    @JsonProperty("id")
    public Integer getCommentId() {
        return commentId;
    }

    @JsonProperty("id")
    public void setCommentId(Integer commentId) {
        this.commentId = commentId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int compareTo(StashPullRequestComment target) {
        if (this.getCommentId() > target.getCommentId()) {
            return 1;
        } else if (this.getCommentId().equals(target.getCommentId())) {
            return 0;
        } else {
            return -1;
        }
    }
}

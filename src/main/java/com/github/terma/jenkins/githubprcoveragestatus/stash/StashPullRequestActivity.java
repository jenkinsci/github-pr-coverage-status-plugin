package com.github.terma.jenkins.githubprcoveragestatus.stash;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Created by Nathan on 20/03/2015.
 */
@SuppressFBWarnings("EQ_COMPARETO_USE_OBJECT_EQUALS")
@JsonIgnoreProperties(ignoreUnknown = true)
public class StashPullRequestActivity implements Comparable<StashPullRequestActivity> {
    private StashPullRequestComment comment;

    public StashPullRequestComment getComment() {
        return comment;
    }

    public void setComment(StashPullRequestComment comment) {
        this.comment = comment;
    }

    public int compareTo(StashPullRequestActivity target) {
        if (this.comment == null || target.getComment() == null) {
            return -1;
        }
        int commmentIdThis = this.comment.getCommentId();
        int commmentIdOther = target.getComment().getCommentId();

        if (commmentIdThis > commmentIdOther) {
            return 1;
        } else if (commmentIdThis == commmentIdOther) {
            return 0;
        } else {
            return -1;
        }
    }
}

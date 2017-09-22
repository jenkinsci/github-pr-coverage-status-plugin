package com.github.terma.jenkins.githubprcoveragestatus.stash;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by Nathan McCarthy
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class StashPullRequestResponse {
    //    private int pageLength;
    private List<StashPullRequestResponseValue> prValues;

    //    private int page;

    private int size;//

    private boolean isLastPage;

    private int nextPageStart;

    //    @JsonProperty("pagelen")
    //    public int getPageLength() {
    //        return pageLength;
    //    }
    //
    //    @JsonProperty("pagelen")
    //    public void setPageLength(int pageLength) {
    //        this.pageLength = pageLength;
    //    }

    @JsonProperty("values")
    public List<StashPullRequestResponseValue> getPrValues() {
        return prValues;
    }

    @JsonProperty("values")
    public void setPrValues(List<StashPullRequestResponseValue> prValues) {
        this.prValues = prValues;
    }

    //    @JsonProperty("page")
    //    public int getPage() {
    //        return page;
    //    }
    //
    //    @JsonProperty("page")
    //    public void setPage(int page) {
    //        this.page = page;
    //    }

    @JsonProperty("size")
    public int getSize() {
        return size;
    }

    @JsonProperty("size")
    public void setSize(int size) {
        this.size = size;
    }

    public boolean getIsLastPage() {
        return isLastPage;
    }

    public int getNextPageStart() {
        return nextPageStart;
    }

}

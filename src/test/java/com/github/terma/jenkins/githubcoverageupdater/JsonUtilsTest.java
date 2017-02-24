package com.github.terma.jenkins.githubcoverageupdater;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;


/**
 * Created by stevegal on 24/02/2017.
 */
public class JsonUtilsTest {

    @Test
    public void extractsJsonValueFromPath(){
        Double extractedValue = JsonUtils.findInJson("{\"metrics\":{\"covered_percent\":85.6543}}","$.metrics.covered_percent");
        assertThat(extractedValue,is(closeTo(85.6543,0.00001)));
    }



}
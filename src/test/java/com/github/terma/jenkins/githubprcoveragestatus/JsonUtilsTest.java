package com.github.terma.jenkins.githubprcoveragestatus;

import com.github.terma.jenkins.githubprcoveragestatus.JsonUtils;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.number.IsCloseTo.closeTo;


public class JsonUtilsTest {

    @Test
    public void extractsJsonValueFromPath() {
        Double extractedValue = JsonUtils.findInJson("{\"metrics\":{\"covered_percent\":85.6543}}", "$.metrics.covered_percent");
        assertThat(extractedValue, is(closeTo(85.6543, 0.00001)));
    }


}
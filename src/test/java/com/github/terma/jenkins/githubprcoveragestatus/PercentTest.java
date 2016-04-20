/*

    Copyright 2015-2016 Artem Stasiuk

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/
package com.github.terma.jenkins.githubprcoveragestatus;

import org.junit.Assert;
import org.junit.Test;

public class PercentTest {

    @Test
    public void toWholeString() {
        Assert.assertEquals("-1%", Percent.toWholeString(-0.007f));
    }

    @Test
    public void toString1() {
        Assert.assertEquals("-0.7%", Percent.toString(-0.007f));
    }

}

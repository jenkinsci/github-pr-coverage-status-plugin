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

@SuppressWarnings("WeakerAccess")
class Percent {

    public static float change(float value1, float avg) {
        if (avg > 0) {
            return value1 - avg;
        } else if (value1 > 0) {
            return value1;
        } else {
            return 0;
        }
    }

    public static int of(float value) {
        return (int) Math.round((value * 100.0));
    }

    public static float ofAndFourDigit(float value) {
        return roundFourAfterDigit(value * 100);
    }

    public static float roundFourAfterDigit(float value) {
        return ((float) Math.round(value * 10000)) / 10000;
    }

    public static float roundCustomAfterDigit(float value, int places) {
      double scale = Math.pow(10, places);
      return (float) (Math.ceil(value * scale) / scale);
    }

    public static String toWholeString(float value) {
        return (value < 0 ? "-" : value > 0 ? "+" : "") + toWholeNoSignString(value);
    }

    public static String toString(float value) {
        return (value < 0 ? "-" : value > 0 ? "+" : "") + toNoSignString(value);
    }

    public static String toWholeNoSignString(float value) {
        return Math.abs(of(value)) + "%";
    }

    public static String toNoSignString(float value) {
        return Math.abs(ofAndFourDigit(value)) + "%";
    }

}

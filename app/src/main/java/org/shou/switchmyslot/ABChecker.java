/********************************************************************************************
 org/shou/switchmyslot/ABChecker.java: A/B checker for Switch My Slot Android App

 Credits:
 Treble Check app:
  - https://github.com/kevintresuelo/treble/blob/master/app/src/main/java/com/kevintresuelo/treble/checker/AB.kt


 Copyright (C) 2010 - 2021 Shou

 MIT License

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

package org.shou.switchmyslot;

import static org.shou.switchmyslot.Utils.getProperty;
import static org.shou.switchmyslot.Utils.isBlank;

public class ABChecker {


    public static class ABResult {

        private boolean isVirtual; // if the A/B partitions are virtual or not

        public ABResult(boolean isVirtual) {
            this.isVirtual = isVirtual;
        }

        public boolean isVirtual() {
            return isVirtual;
        }
    }


    /**
     * Checks if device supports A/B partitions.
     *
     * @return ABResult object if the device supports A/B, else null. The ABResult object contains a boolean that tells if the A/B partitions are virtual or not.
     */
    public static ABResult check() {

        /**
         * Checks if the device supports Virtual A/B partitions
         */
        if (getProperty("ro.virtual_ab.enabled").equals("true") && getProperty("ro.virtual_ab.retrofit").equals("false")) {
            return new ABResult(true);
        }

        /**
         * Checks if the device supports the conventional A/B partitions
         */
        if (!isBlank(getProperty("ro.boot.slot_suffix")) || getProperty("ro.build.ab_update").equals("true")) {
            return new ABResult(false);
        }

        /**
         * Returns null if the device doesn't support A/B partitions at all
         */
        return null;
    }

}

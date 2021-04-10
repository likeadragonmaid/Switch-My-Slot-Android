/********************************************************************************************
 org/shou/switchmyslot/Utils.java: Utilities for Switch My Slot Android App

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

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Utils {

    /**
     * Checks if the string is empty or contains only spaces.
     *
     * @param str The string to check.
     * @return Returns true if the string is empty or contains only spaces, else returns false.
     */
    public static boolean isBlank(String str) {
        return str.trim().isEmpty();
    }


    /**
     * Checks if the string object is null, empty or contains only spaces.
     *
     * @param str The string to check.
     * @return Returns true if the string is null, empty or contains only spaces, else returns false.
     */
    public static boolean isNullOrBlank(String str) {
        return str == null || isBlank(str);
    }


    /**
     * Wraps the getProperty function with defaultValue of an empty string.
     *
     * @param property The property to check with 'getprop' command.
     * @return The value of the property or an empty string if there isn't such property.
     */
    public static String getProperty(String property) {
        return getProperty(property, "");
    }


    /**
     * Gets the value of a given property with 'getprop' command.
     *
     * @param property The property to check with 'getprop' command.
     * @param defaultValue The value to return when there isn't such property.
     * @return The value of the property or the defaultValue if there isn't such property.
     */
    public static String getProperty(String property, String defaultValue) {

        String value;

        try {
            Process p = Runtime.getRuntime().exec("getprop " + property);
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));

            value = input.readLine();
            if (!isNullOrBlank(value)) return value;

            input.close();
        } catch (Exception err) {
            err.printStackTrace();
        }

        return defaultValue;
    }

}

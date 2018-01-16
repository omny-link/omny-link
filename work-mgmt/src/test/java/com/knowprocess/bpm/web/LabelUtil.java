/*******************************************************************************
 *Copyright 2011-2018 Tim Stephenson and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.knowprocess.bpm.web;

public class LabelUtil {

    // pixels for width of 'standard' char
    private static final int FONT_SIZE = 16;

    public static int rowCount(String text, float labelHeight, float labelWidth) {
        return Math.round(labelHeight / FONT_SIZE);
    }

    public static String[] rows(String text, float labelHeight, float labelWidth) {
        int charCount = text.length();
        int rowCount = rowCount(text, labelHeight, labelWidth);
        int charsPerLine = Math.round(charCount / rowCount);
        String[] rows = new String[rowCount];
        int curIdx = 0;
        for (int i = 0; i < rowCount; i++) {
            String cutBefore = "";
            try {
                String tmp = text.substring(curIdx, curIdx + charsPerLine);
                cutBefore = tmp.substring(0, tmp.lastIndexOf(' '));
                if (i + 1 == rowCount && !text.endsWith(cutBefore)) {
                    cutBefore = text.substring(curIdx);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("  b4: " + cutBefore);
            String cutAfter = "";
            try {
                cutAfter = text.substring(curIdx,
                        text.indexOf(' ', curIdx + (i + 1 * charsPerLine)));
            } catch (StringIndexOutOfBoundsException e) {
                cutAfter = text.substring(curIdx);
            }
            System.out.println("  after: " + cutAfter);
            if (charsPerLine - cutBefore.length() < cutAfter.length()
                    - charsPerLine) {
                rows[i] = cutBefore.trim();
                curIdx = curIdx + cutBefore.length();
            } else {
                rows[i] = cutAfter.trim();
                curIdx = curIdx + cutAfter.length();
            }
        }
        return rows;
    }

}

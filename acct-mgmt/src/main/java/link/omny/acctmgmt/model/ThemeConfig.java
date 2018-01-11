/*******************************************************************************
 *Copyright 2018 Tim Stephenson and contributors
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
package link.omny.acctmgmt.model;

import lombok.Data;

@Data
public class ThemeConfig {

    private String logoUrl;
    private String iconUrl;
    private String cssUrl;

    private String headingColor = "#0e9acd";
    private String subHeadingColor = "#6f6f71";
    private String bodyColor = "#6f6f71";
    private String accentColor = "#ff6c06";
    private String iconColor = "#6f6f71";

    public void set(String name, String value) {
        switch (name) {
        case "logo":
            setLogoUrl(value);
            break;
        case "icon":
            setIconUrl(value);
            break;
        case "cssUrl":
            setCssUrl(value);
            break;
        case "headingColor":
            setHeadingColor(value);
            break;
        case "subHeadingColor":
            setSubHeadingColor(value);
            break;
        case "bodyColor":
            setBodyColor(value);
            break;
        case "accentColor":
            setAccentColor(value);
            break;
        case "iconColor":
            setIconColor(value);
            break;
        default:
            System.err.println("Unsupported theme property: " + name);
        }
    }

}

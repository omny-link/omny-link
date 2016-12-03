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

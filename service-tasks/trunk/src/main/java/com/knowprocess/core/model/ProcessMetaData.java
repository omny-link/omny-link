package com.knowprocess.core.model;

import java.io.IOException;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.activiti.engine.impl.identity.Authentication;

@XmlRootElement(name = "meta")
@XmlType(propOrder = { "start" })
public class ProcessMetaData extends Object implements Serializable {

    /**
     * Determines if a de-serialized file is compatible with this class.
     * 
     * Maintainers must change this value if and only if the new version of this
     * class is not compatible with old versions. See Sun docs for <a
     * href=http://java.sun.com/products/jdk/1.1/docs/guide
     * /serialization/spec/version.doc.html> details. </a>
     * 
     * Not necessary to include in first version of the class, but included here
     * as a reminder of its importance.
     */
    private static final long serialVersionUID = -1913644930614561256L;

    public static final String VAR = "_meta";

    private GregorianCalendar start;

    private static boolean autoApprove = false;

    private static boolean enableFollowUp = false;

    public ProcessMetaData() {
        start = new GregorianCalendar();
    }

    public GregorianCalendar getStart() {
        return start;
    }

    public void setStart(GregorianCalendar start) {
        this.start = start;
    }

    public Date startTimePlus(String iso8601Duration) {
        Calendar cal = (Calendar) start.clone();
        append(cal, iso8601Duration);
        return cal.getTime();
    }

    private Calendar append(Calendar cal, String iso8601Duration) {
        if (iso8601Duration.startsWith("PT")) {
            StringBuilder interval = new StringBuilder();
            // start at 2 to ignore PT
            for (int i = 2; i < iso8601Duration.length(); i++) {
                char c = iso8601Duration.charAt(i);
                if (Character.isDigit(c)) {
                    interval.append(c);
                } else if (Character.isLetter(c)) {
                    switch (c) {
                    case 'H':
                        cal.roll(Calendar.HOUR,
                                Integer.parseInt(interval.toString()));
                        break;
                    case 'M':
                        cal.roll(Calendar.MINUTE,
                                Integer.parseInt(interval.toString()));
                        break;
                    case 'S':
                        cal.roll(Calendar.SECOND,
                                Integer.parseInt(interval.toString()));
                        break;
                    }
                    interval = new StringBuilder();
                }
            }
            return cal;
        } else if (iso8601Duration.startsWith("P")) {
            StringBuilder interval = new StringBuilder();
            // start at 1 to ignore P
            for (int i = 1; i < iso8601Duration.length(); i++) {
                char c = iso8601Duration.charAt(i);
                if (Character.isDigit(c)) {
                    interval.append(c);
                } else if (Character.isLetter(c)) {
                    switch (c) {
                    case 'Y':
                        cal.roll(Calendar.YEAR,
                                Integer.parseInt(interval.toString()));
                        break;
                    case 'M':
                        cal.roll(Calendar.MONTH,
                                Integer.parseInt(interval.toString()));
                        break;
                    case 'W':
                        cal.roll(Calendar.WEEK_OF_YEAR,
                                Integer.parseInt(interval.toString()));
                        break;
                    case 'D':
                        cal.roll(Calendar.DAY_OF_YEAR,
                                Integer.parseInt(interval.toString()));
                        break;
                    }
                    interval = new StringBuilder();
                }
            }
            return cal;
        } else {
            throw new IllegalArgumentException(
                    "Only durations (starting P or PT) supported");
        }
    }

    public String getUser() {
        return Authentication.getAuthenticatedUserId();
    }

    public String getMailFrom() {
        return "donotreply@knowprocess.com";
    }

    public String getAdminGroup() {
        return "app_admin";
    }

    public String getMailBcc() {
        return "tim@knowprocess.com";
    }

    @XmlTransient
    // Note that even though property is static this accessor is not to conform
    // to JavaBean / JUEL expectations
    public boolean isAutoApprove() {
        return autoApprove;
    }

    // Note that even though property is static this accessor is not to conform
    // to JavaBean / JUEL expectations
    public void setAutoApprove(boolean newAutoApprove) {
        autoApprove = newAutoApprove;
    }

    @XmlTransient
    // Note that even though property is static this accessor is to conform
    // to JavaBean / JUEL expectations
    public boolean isEnableFollowUp() {
        return enableFollowUp;
    }

    // Note that even though property is static this accessor is to conform
    // to JavaBean / JUEL expectations
    public void setEnableFollowUp(boolean enableFollowUp) {
        this.enableFollowUp = enableFollowUp;
    }

    public String getText(String bundle, String key) {
        // TODO no localisation for now
        return getResources(bundle, Locale.getDefault()).getString(key);
    }

    private ResourceBundle getResources(String bundle, Locale locale) {
        // TODO No localisation for now
        // Locale l = new Locale()

        // TODO cache bundles??
        return ResourceBundle.getBundle(bundle + "_Resources");
    }

    /**
     * Returns an HTML string formed by merging the variables supplied into the
     * application-wide FreeMarker template.
     * 
     * <p>
     * TODO May offer an override to supply template name in the future if
     * needed.
     * 
     * @param params
     * @return HTML content as String.
     * @throws IOException
     * @throws TemplateException
     */
    // public String getHtmlContent(String bundle, String key,
    // Map<String, Object> params) throws IOException, TemplateException {
    // Configuration cfg = getFreemarkerConfiguration();
    //
    // String templateName = bundle + key;
    // Template t;
    // try {
    // t = cfg.getTemplate(templateName);
    // } catch (FileNotFoundException e) {
    // String templateStr = getText(bundle, key);
    // t = new Template(templateName, new StringReader(templateStr), cfg);
    // }
    //
    // Writer out = new StringWriter();
    // t.process(params, out);
    // return out.toString();
    // }
    //
    // private Configuration getFreemarkerConfiguration() {
    // // TODO Auto-generated method stub
    // return null;
    // }

    public String getJsonString() throws Exception {
        return String.format("{\"start\": \"%s\"}",
                Consts.isoDateTimeFormat.format(start.getTime()));
    }

    @Override
    public String toString() {
        return String.format("PailzMetaData [start=%s]", start);
    }

}

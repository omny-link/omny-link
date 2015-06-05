package com.knowprocess.collector.api;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Status {
    private static final String PROJECT_REGEX = "(\\+\\S+)";
    private static final String TAG_REGEX = "(#\\S+)";

    /**
     * @return code denoting success, error code etc. Reuses HTTP codes.
     */
    private int code = 200;
    private String text = "OK";
    private String url;
    private String project;
    private String context;
    private String tags;

    // Used by PailzPoster
    public Status(String text) {
        super();
        this.text = text;
    }

    // Used by Delicious poster
    // TODO replace with general purpose parser from Big BPM Cloud project??
    public Status(String text, String url, String project, String context,
            String tags) {
        super();
        this.text = text;
        this.url = url;
        this.project = project;
        this.context = context;
        this.tags = tags;
    }

    public Status(String url, String markup) {
        this.url = url;
        text = markup;
        extractProjects(markup);
        extractContext(markup);
        extractTags(markup);
    }

    private void extractProjects(String markup) {
        project = extractField(markup, '+', false);
    }

    private void extractContext(String markup) {
        context = extractField(markup, '@', true);
    }

    private void extractTags(String markup) {
        tags = extractField(markup, '#', true);
    }

    private String extractField(String markup, char delimiter,
            boolean stripDelimiter) {
        String field = "";
        String regex;
        if (delimiter == '+') {
            regex = PROJECT_REGEX;
        } else {
            regex = TAG_REGEX.replace('#', delimiter);
        }
        Pattern tagPattern = Pattern.compile(regex);
        Matcher m = tagPattern.matcher(markup);

        while (m.find()) {
            String tag;
            if (stripDelimiter) {
                // strip leading #, + or whatever
                tag = m.group(1).substring(1);
            } else {
                tag = m.group(1);
            }
            field += tag.endsWith(",") ? tag : tag + ",";
        }
        // Whatever is left of markup after removing tags is the description
        text = text.replaceAll(regex, "").trim();
        return field;
    }

    /**
     * @return the code
     */
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return the project
     */
    public String getProject() {
        return project;
    }

    /**
     * @return the context
     */
    public String getContext() {
        return context;
    }

    /**
     * @return the tags
     */
    public String getTags() {
        return tags;
    }

    public String getText() {
        return text;
    }
}

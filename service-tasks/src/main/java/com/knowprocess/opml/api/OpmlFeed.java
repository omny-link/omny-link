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
package com.knowprocess.opml.api;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Valid combinations (so far unenforced) are children+title+text or all values
 * other than children
 * 
 * @author tstephen
 * 
 */
public class OpmlFeed implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5478770712028770926L;

    private List<OpmlFeed> children;

    private String text;
    private String title;
    private String type = "rss";
    private String xmlUrl;
    private String htmlUrl;

    public OpmlFeed() {
        super();
        this.children = new ArrayList<OpmlFeed>();
    }

    public OpmlFeed(String title, String text, String type, String xmlUrl,
            String htmlUrl) {
        this();
        this.text = text;
        this.title = title;
        this.type = type;
        this.xmlUrl = xmlUrl;
        this.htmlUrl = htmlUrl;
    }

    public void addChild(OpmlFeed child) {
        children.add(child);
    }

    public List<OpmlFeed> getChildren() {
        return children;
    }

    public void setChildren(List<OpmlFeed> children) {
        this.children = children;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getXmlUrl() {
        return xmlUrl;
    }

    public void setXmlUrl(String xmlUrl) {
        this.xmlUrl = xmlUrl;
    }

    public String getHtmlUrl() {
        return htmlUrl;
    }

    public void setHtmlUrl(String htmlUrl) {
        this.htmlUrl = htmlUrl;
    }

    public String[] getChildUrls() {
        List<String> urls = new ArrayList<String>();
        for (OpmlFeed feed : getChildren()) {
            urls.add(feed.getXmlUrl());
        }
        return urls.toArray(new String[urls.size()]);
    }
}

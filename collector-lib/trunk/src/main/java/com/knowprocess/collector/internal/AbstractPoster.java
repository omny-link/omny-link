package com.knowprocess.collector.internal;

import com.knowprocess.collector.api.Poster;
import com.knowprocess.collector.api.Status;

public abstract class AbstractPoster implements Poster {

    public Status createStatus(String summary, String markup) {
        int start = summary.indexOf("http");
        String url = null;
        try {
            url = summary.substring(start, summary.indexOf(' ', start));
        } catch (StringIndexOutOfBoundsException e) {
            url = summary;
        }
    
        return new Status(url, markup);
    }



}

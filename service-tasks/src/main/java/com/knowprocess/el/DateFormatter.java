package com.knowprocess.el;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateFormatter {
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(DateFormatter.class);
    private DateFormat isoDateFormatter = new SimpleDateFormat("yyyy-MM-dd");

    public String isoDate(Date d) {
        return isoDateFormatter.format(d);
    }

    public String toString(Date d, String pattern) {
        if (d == null) { 
            return pattern.toLowerCase();
        } else {
            return new SimpleDateFormat(pattern).format(d);
        }
    }

    public String toString(String source, String pattern) {
        if (source == null || source.trim().length()==0) {
            return "";
        }
        try {
            return toString(isoDateFormatter.parse(source), pattern);
        } catch (ParseException e) {
            LOGGER.error(String.format("When coercing %1$s to date: %2$s",
                    source, e.getMessage()));
            return source;
        }
    }
}

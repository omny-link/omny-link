package com.knowprocess.bpm.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DateUtils {
    private static DateFormat isoFormatter = new SimpleDateFormat("yyyy-MM-dd");

    /**
     * @param isoDate a string to parse an ISO 8601 date from.
     * @return date
     * @throws IllegalArgumentException If date is unparseable.
     */
    public static Date parseDate(String isoDate) {
        try {
            return isoFormatter.parse(isoDate);
        } catch (ParseException e) {
            throw new IllegalArgumentException(
                    String.format(
                            "Parameter 'isoDate' must be an ISO 8601 date, not '%1$s'",
                            isoDate));
        }
    }
    /**
     * @return date one month ago from now.
     */
    public static Date oneMonthAgo() {
        GregorianCalendar oneMonthAgo = new GregorianCalendar();
        oneMonthAgo.add(Calendar.MONTH, -1);
        return oneMonthAgo.getTime();
    }
}
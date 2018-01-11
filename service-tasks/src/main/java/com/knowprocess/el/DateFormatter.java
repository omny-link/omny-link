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
package com.knowprocess.el;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
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

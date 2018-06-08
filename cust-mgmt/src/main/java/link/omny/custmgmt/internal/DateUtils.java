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
package link.omny.custmgmt.internal;

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

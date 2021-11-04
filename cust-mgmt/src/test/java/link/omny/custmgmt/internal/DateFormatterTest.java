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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class DateFormatterTest {

    private static DateFormatter dateFormatter;

    @BeforeAll
    public static void setUpClass() {
        dateFormatter = new DateFormatter();
    }

    @Test
    public void testNullDate() {
        assertEquals("dd-mm-yyyy", dateFormatter.toString((Date) null, "dd-MM-yyyy"));
        assertEquals("dd/mm/yyyy", dateFormatter.toString((Date) null, "dd/MM/yyyy"));
        assertEquals("mm/dd/yyyy", dateFormatter.toString((Date) null, "MM/dd/yyyy"));
    }

    @Test
    public void testReformatDate() {
        assertEquals("31-12-2016", dateFormatter.toString("2016-12-31", "dd-MM-yyyy"));
    }

    @Test
    public void testBigDecimalToDateString() {
        BigDecimal millis = new BigDecimal(1519121436000l);
        assertEquals("20-02-2018", dateFormatter.toString(millis, "dd-MM-yyyy"));
    }

    @Test
    public void testMillisToDateString() {
        assertEquals("20-02-2018", dateFormatter.toString(1519121436000l, "dd-MM-yyyy"));
    }
}

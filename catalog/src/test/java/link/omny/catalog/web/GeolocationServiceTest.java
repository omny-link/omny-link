/*******************************************************************************
 * Copyright 2015-2022 Tim Stephenson and contributors
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
package link.omny.catalog.web;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import link.omny.catalog.model.GeoPoint;
import link.omny.catalog.model.StockCategory;

public class GeolocationServiceTest {

    protected GeoLocationService svc = new GeoLocationService();

    @Test
    public void testDistanceWatfordToAldermaston() {
        GeoPoint callevaPark = new GeoPoint(51.361589, -1.161292);
        StockCategory watford = new StockCategory();
        watford.setPostCode("WD18 8PH");
        watford.setLat(51.644605);
        watford.setLng(-0.424819);

        double distance = svc.distance(callevaPark, watford);

        assertEquals(60.0d, distance, 0.1);
    }

    @Test
    public void testDistanceReadingToAldermaston() {
        GeoPoint callevaPark = new GeoPoint(51.361589, -1.161292);
        StockCategory reading = new StockCategory();
        reading.setLat(51.459409);
        reading.setLng(-0.973169);

        double distance = svc.distance(callevaPark, reading);

        assertEquals(17.0d, distance, 0.1);
    }

}

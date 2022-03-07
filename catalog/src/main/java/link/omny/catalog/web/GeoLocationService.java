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

import link.omny.catalog.model.GeoPoint;
import link.omny.catalog.model.StockCategory;

public class GeoLocationService {

    public double distance(GeoPoint target, StockCategory stockCategory) {
        stockCategory.setDistance(Haversine.distance(
                stockCategory.getGeoPoint(), target));
        return stockCategory.getDistance();
    }

    /**
     * An equation giving great-circle distances between two points on a sphere
     * from their longitudes and latitudes.
     *
     * @see https://en.wikipedia.org/wiki/Haversine_formula
     */
    static class Haversine {
        /** Approx Earth radius in km */
        private static final int EARTH_RADIUS = 6371;

        public static double distance(GeoPoint to, GeoPoint from) {

            double dLat = Math.toRadians((to.getLat() - from.getLat()));
            double dLong = Math.toRadians((to.getLng() - from.getLng()));

            double fromLat = Math.toRadians(from.getLat());
            double toLat = Math.toRadians(to.getLat());

            double a = haversine(dLat) + Math.cos(fromLat) * Math.cos(toLat)
                    * haversine(dLong);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

            return EARTH_RADIUS * c;
        }

        public static double haversine(double val) {
            return Math.pow(Math.sin(val / 2), 2);
        }
    }
}

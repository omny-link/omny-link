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
package link.omny.catalog.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import link.omny.catalog.internal.LruCache;
import link.omny.catalog.model.GeoPoint;
import link.omny.catalog.model.StockCategory;

public class GeoLocationService {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(GeoLocationService.class);

    private static final String GEOCODING_URL = "https://maps.googleapis.com/maps/api/geocode/json?address=%1$s,UK";

    private LruCache<String, GeoPoint> cache ;

    public GeoLocationService(int cacheSize) {
        cache = new LruCache<String, GeoPoint>(cacheSize);
    }

    public GeoPoint locate(@NotNull String q) throws IOException {
        if (cache.containsKey(q)) {
            LOGGER.debug(String.format("  Geo-coding cache hit for: %1$s", q));
            return cache.get(q);
        }

        JsonReader jsonReader = null;
        URL url = new URL(String.format(GEOCODING_URL,
                URLEncoder.encode(q, "UTF-8")));
        try (InputStream is = (InputStream) url.getContent()) {
            long start = System.currentTimeMillis();
            LOGGER.debug("  Geo-coding url constructed: {}", url);

            jsonReader = Json.createReader(new InputStreamReader(is));
            JsonObject obj = jsonReader.readObject();
            JsonObject location = ((JsonObject) obj.getJsonArray("results")
                    .get(0)).getJsonObject("geometry")
                    .getJsonObject("location");

            GeoPoint geoPoint = new GeoPoint(location.getJsonNumber("lat")
                    .doubleValue(), location.getJsonNumber("lng").doubleValue());
            cache.put(q, geoPoint);

            LOGGER.info("Geo-coding took {} ms",
                    (System.currentTimeMillis() - start));
            return geoPoint;
        } catch (IndexOutOfBoundsException | NullPointerException e) {
            LOGGER.warn("Location {} could not be geocoded, constructed URL: {}",
                    q, url.toExternalForm());
            return null;
        }
    }

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

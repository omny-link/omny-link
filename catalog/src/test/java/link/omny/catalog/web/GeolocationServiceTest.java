package link.omny.catalog.web;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import link.omny.catalog.model.GeoPoint;
import link.omny.catalog.model.StockCategory;

import org.junit.Test;

public class GeolocationServiceTest {

    protected GeolocationService svc = new GeolocationService();

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

    @Test
    public void testLocate() throws IOException {
        GeoPoint corsham = svc.locate("SN13 9BS");

        assertEquals(51.43, corsham.getLat(), 0.1);
        assertEquals(-2.1, corsham.getLng(), 0.1);
    }
}

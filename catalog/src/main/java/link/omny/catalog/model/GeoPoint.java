package link.omny.catalog.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A tuple to hold latitude and longitude of a location.
 *
 *
 * @author Tim Stephenson
 */
@Data
@AllArgsConstructor
public class GeoPoint {

    @JsonProperty
    private double lat;

    @JsonProperty
    private double lng;

}

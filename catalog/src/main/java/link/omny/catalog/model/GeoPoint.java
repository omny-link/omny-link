package link.omny.catalog.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A tuple to hold latitude and longitude of a location.
 *
 *
 * @author Tim Stephenson
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GeoPoint {

    @JsonProperty
    private double lat;

    @JsonProperty
    private double lng;

}

package link.omny.acctmgmt.model;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Embeddable
public class TenantTypeaheadValue implements Serializable {

    private static final long serialVersionUID = -163516717625120278L;

    @NotNull
    @JsonProperty
    private String id;

    @JsonProperty
    private int idx = -1;

    @NotNull
    @JsonProperty
    private String name;

}

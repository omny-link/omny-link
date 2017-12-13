package link.omny.acctmgmt.model;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

@Data
@Embeddable
public class TenantTypeaheadValue implements Serializable {

    private static final long serialVersionUID = -163516717625120278L;

    @NotNull
    @JsonProperty
    private String id;

    @JsonProperty
    // Include override is necessary due to this decision:
    // https://github.com/FasterXML/jackson-databind/issues/849
    @JsonInclude(value = Include.ALWAYS)
    private Integer idx = -1;

    @NotNull
    @JsonProperty
    private String name;

    public TenantTypeaheadValue() {

    }

    public TenantTypeaheadValue(String id) {
        this();
        setId(id);
        setName(id);
    }

    public TenantTypeaheadValue(String id, String name) {
        this();
        setId(id);
        setName(name);
    }
}

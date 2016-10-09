package link.omny.custmgmt.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

@MappedSuperclass
@Data
@EqualsAndHashCode(exclude = { "id" })
@AllArgsConstructor
@NoArgsConstructor
public class CustomField implements Serializable {
    private static final long serialVersionUID = 7496048564725313117L;
    protected static final Logger LOGGER = LoggerFactory
            .getLogger(CustomField.class);

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    @JsonProperty
    private Long id;

    /**
     */
    @NotNull
    @JsonProperty
    @Column(nullable = false)
    private String name;

    /**
     */
    @JsonProperty
    private String value;

    public CustomField(String key, Object value2) {
        this.name = key;
        this.value = value2 == null ? null : value2.toString();
    }
}

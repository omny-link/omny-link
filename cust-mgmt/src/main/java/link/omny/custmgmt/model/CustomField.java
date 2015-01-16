package link.omny.custmgmt.model;

import java.io.Serializable;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Data
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
    @NotNull
    @JsonProperty
    @Column(nullable = false)
    private String type;

    /**
     */
    @JsonProperty
    private String value;

    @ManyToOne(targetEntity = Contact.class, fetch = FetchType.EAGER, cascade = {
            CascadeType.PERSIST, CascadeType.MERGE })
    @JoinColumn(name = "contact_id")
    private Contact contact;

}

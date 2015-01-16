package link.omny.custmgmt.model;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.Digits;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.fasterxml.jackson.annotation.JsonProperty;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountInfo implements Serializable {

    private static final long serialVersionUID = -1955316248920138892L;

    @Id
    @Column(name = "id")
    @JsonProperty
    private String id;

    /**
     */
    @NotNull
    @JsonProperty
    private String name;

    /**
     */
    @Digits(integer = 8, fraction = 0)
    @JsonProperty
    private Integer companyNumber;

    @JsonProperty
    private String aliases;

    @JsonProperty
    private String businessWebsite;

    /**
     */
    @Size(max = 120)
    @JsonProperty
    private String shortDesc;

    @JsonProperty
    private String description;

    /**
     */
    @Min(1L)
    private Integer noOfEmployees;

    /**
     */
    @Digits(integer = 4, fraction = 0)
    @JsonProperty
    private Integer incorporationYear;

    /**
     */
    private String alreadyContacted;


}

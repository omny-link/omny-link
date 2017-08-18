package link.omny.supportservices.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.NoArgsConstructor;

@Entity(name = "OL_FOUNTAIN")
@NoArgsConstructor
public class NumberFountain {

    @JsonProperty
    @Id
    @GeneratedValue
    private Long id;

    @JsonProperty
    private String entityName;
    
    @JsonProperty
    private Long lastUsed;

    public NumberFountain(String entityName) {
        this.entityName = entityName;
        this.lastUsed = 0l;
    }

    public NumberFountain(String entityName, Long lastUsed) {
        this.entityName = entityName;
        this.lastUsed = lastUsed;
    }

    public Long getNext() {
        return getLastUsed() + 1l;
    }

    public String getEntityName() {
        return entityName;
    }

    public Long getLastUsed() {
        return lastUsed;
    }

    public void increment() {
        this.lastUsed = this.getLastUsed() + 1l;        
    }
    
}
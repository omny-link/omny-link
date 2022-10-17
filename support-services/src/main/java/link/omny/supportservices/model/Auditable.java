package link.omny.supportservices.model;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Data
public abstract class Auditable<U> {

    @JsonProperty
    @CreatedBy
    @Column(name = "created_by", length = 150)
    protected U createdBy;

    @CreatedDate
    @Column(name = "created")
    @Temporal(TemporalType.TIMESTAMP)
    protected Date created;

    @JsonProperty
    @LastModifiedBy
    @Column(name = "last_updated_by", length = 150)
    protected U lastUpdatedBy;

    @LastModifiedDate
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "last_updated")
    protected Date lastUpdated;

}

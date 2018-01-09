package link.omny.catalog.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import link.omny.custmgmt.model.CustomField;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.data.rest.core.annotation.RestResource;

@Entity
@Table(name = "OL_FEEDBACK_CUSTOM")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter
@Setter
@NoArgsConstructor
public class CustomFeedbackField extends CustomField {

    private static final long serialVersionUID = 8058972553121806986L;

    @ManyToOne(optional = false, targetEntity = Feedback.class)
    @RestResource(rel = "customFeedback")
    private Feedback feedback;

    public CustomFeedbackField(String key, String object) {
        super(key, object);
    }

}

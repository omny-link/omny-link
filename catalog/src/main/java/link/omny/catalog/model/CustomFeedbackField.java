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

    @Override
    public String toString() {
        return String
                .format("CustomFeedbackField [id=%s, name=%s, value=%s, feedbackId=%s]",
                        getId(), getName(), getValue(), 
                        feedback == null ? null : feedback.getId());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime
                * result
                + ((feedback == null) ? 0 : 
                    feedback.getId() == null ? 0 : feedback.getId().hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        CustomFeedbackField other = (CustomFeedbackField) obj;
        if (feedback == null) {
            if (other.feedback != null)
                return false;
        } else if (!feedback.getId().equals(other.feedback.getId()))
            return false;
        return true;
    }
}

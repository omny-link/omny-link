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
@Table(name = "OL_ORDER_CUSTOM")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter
@Setter
@NoArgsConstructor
public class CustomOrderField extends CustomField {

    private static final long serialVersionUID = -4795761632689941890L;

    @ManyToOne(optional = false, targetEntity = Order.class)
    @RestResource(rel = "customOrder")
    private Order order;

    public CustomOrderField(String key, String object) {
        super(key, object);
    }

    @Override
    public String toString() {
        return String.format(
                "CustomOrderField [id=%s, name=%s, value=%s, orderId=%s]",
                getId(), getName(), getValue(),
                order == null ? null : order.getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        CustomOrderField other = (CustomOrderField) obj;
        if (order == null) {
            if (other.order != null)
                return false;
        } else if (!order.getId().equals(other.order.getId()))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((order == null) ? 0 : order.getId() == null ? 0 : order
                        .getId().hashCode());
        return result;
    }

}

package link.omny.catalog.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.springframework.data.rest.core.annotation.RestResource;

import link.omny.custmgmt.model.CustomField;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "OL_ORDER_ITEM_CUSTOM")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Getter
@Setter
@NoArgsConstructor
public class CustomOrderItemField extends CustomField {

    private static final long serialVersionUID = 8058972553121806986L;

    @ManyToOne(optional = false, targetEntity = OrderItem.class)
    @RestResource(rel = "customOrderItem")
    @JoinColumn(name = "order_item_id")
    private OrderItem orderItem;

    public CustomOrderItemField(String key, Object object) {
        super(key, object);
    }

    @Override
    public String toString() {
        return String
                .format("CustomOrderItemField [id=%s, name=%s, value=%s, orderItemId=%s]",
                        getId(), getName(), getValue(),
                        orderItem == null ? null : orderItem.getId());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((orderItem == null) ? 0 : orderItem.getId() == null ? 0
                        : orderItem.getId().hashCode());
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
        CustomOrderItemField other = (CustomOrderItemField) obj;
        if (orderItem == null) {
            if (other.orderItem != null)
                return false;
        } else if (!orderItem.getId().equals(other.orderItem.getId()))
            return false;
        return true;
    }

}

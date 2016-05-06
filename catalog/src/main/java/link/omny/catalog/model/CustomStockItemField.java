package link.omny.catalog.model;

import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import link.omny.custmgmt.model.CustomField;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import org.springframework.data.rest.core.annotation.RestResource;

@Entity
@Table(name = "OL_STOCK_ITEM_CUSTOM")
@Data
@EqualsAndHashCode(callSuper = true)
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@NoArgsConstructor
public class CustomStockItemField extends CustomField {

    private static final long serialVersionUID = -4795761632689941890L;

    @ManyToOne(optional = false, targetEntity = StockItem.class)
    @RestResource(rel = "customStockItem")
    private StockItem stockItem;

    public CustomStockItemField(String key, String object) {
        super(key, object);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        CustomStockItemField other = (CustomStockItemField) obj;
        if (stockItem == null) {
            if (other.stockItem != null)
                return false;
        } else if (stockItem.getId() == null && other.stockItem.getId() != null) {
            return false;
        } else if (!stockItem.equals(other.stockItem))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime
                * result
                + ((stockItem == null || stockItem.getId() == null) ? 0
                        : stockItem.getId().hashCode());
        return result;
    }

}

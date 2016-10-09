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
    public String toString() {
        return String
                .format("CustomStockItemField [id=%s, name=%s, value=%s, stockItemId=%s]",
                        getId(), getName(), getValue(),
                        stockItem == null ? null : stockItem.getId());
    }

}

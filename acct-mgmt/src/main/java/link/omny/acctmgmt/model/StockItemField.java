package link.omny.acctmgmt.model;

import io.onedecision.engine.domain.model.EntityField;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("stockItem")
@NoArgsConstructor
public class StockItemField extends EntityField {

}

package link.omny.catalog.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.data.rest.core.config.Projection;

@Projection(name = "incCategory", types = { CustomStockItemField.class,
        StockItem.class, StockCategory.class })
interface StockItemIncCategoryProjection {

    Long getId();

    String getName();

    String getDescription();

    String getSize();

    String getUnit();

    BigDecimal getPrice();

    String getType();

    Date getCreated();

    Date getLastUpdated();

    String getTenantId();

    StockCategory getStockCategory();

    List<MediaResource> getImages();

    List<CustomStockItemField> getCustomFields();

    Object getField(@NotNull String fieldName);
}

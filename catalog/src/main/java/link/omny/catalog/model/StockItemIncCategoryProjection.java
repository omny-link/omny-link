package link.omny.catalog.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

import link.omny.catalog.json.JsonCustomStockItemFieldDeserializer;
import link.omny.custmgmt.json.JsonCustomFieldSerializer;

import org.springframework.data.rest.core.config.Projection;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

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

    String getStatus();

    Date getCreated();

    Date getLastUpdated();

    String getTenantId();

    StockCategory getStockCategory();

    List<MediaResource> getImages();

    @JsonDeserialize(using = JsonCustomStockItemFieldDeserializer.class)
    @JsonSerialize(using = JsonCustomFieldSerializer.class)
    List<CustomStockItemField> getCustomFields();

    Object getField(@NotNull String fieldName);
}

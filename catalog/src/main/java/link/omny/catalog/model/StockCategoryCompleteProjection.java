package link.omny.catalog.model;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

import link.omny.catalog.json.JsonCustomStockCategoryFieldDeserializer;
import link.omny.custmgmt.json.JsonCustomFieldSerializer;

import org.springframework.data.rest.core.config.Projection;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Projection(name = "complete", types = { CustomStockCategoryField.class,
        MediaResource.class, StockCategory.class, StockItem.class })
interface StockCategoryCompleteProjection {

    Long getId();

    String getName();

    String getDescription();

    String getVideoCode();

    String getAddress1();

    String getAddress2();

    String getTown();

    String getCountyOrCity();

    String getPostCode();

    String getCountry();

    Date getCreated();

    Date getLastUpdated();

    String getTenantId();

    @JsonDeserialize(using = JsonCustomStockCategoryFieldDeserializer.class)
    @JsonSerialize(using = JsonCustomFieldSerializer.class)
    List<CustomStockCategoryField> getCustomFields();

    List<StockItem> getStockItems();

    List<MediaResource> getImages();

    Object getField(@NotNull String fieldName);

}

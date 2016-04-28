package link.omny.catalog.model;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotNull;

import org.springframework.data.rest.core.config.Projection;

@Projection(name = "complete", types = { CustomStockCategoryField.class,
        MediaResource.class, StockCategory.class, StockItem.class })
interface StockCategoryCompleteProjection {

    Long getId();

    String getName();

    String getDescription();

    String getAddress1();

    String getAddress2();

    String getTown();

    String getCountyOrCity();

    String getPostCode();

    String getCountry();

    Date getCreated();

    Date getLastUpdated();

    String getTenantId();

    List<CustomStockCategoryField> getCustomFields();

    List<StockItem> getStockItems();

    Object getField(@NotNull String fieldName);

}

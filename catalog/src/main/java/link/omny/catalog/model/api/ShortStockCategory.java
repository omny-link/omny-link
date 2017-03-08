package link.omny.catalog.model.api;

import java.util.Date;
import java.util.List;

public interface ShortStockCategory {
    String getName();

    String getDescription();

    String getAddress1();

    String getAddress2();

    String getTown();

    String getCountyOrCity();

    String getPostCode();

    String getCountry();

    String getTags();

    String getMapUrl();

    Double getLat();

    Double getLng();

    String getVideoCode();

    String getProductSheetUrl();

    String getStatus();

    Date getCreated();

    Date getLastUpdated();
    
    List<? extends ShortStockItem> getStockItems();
}
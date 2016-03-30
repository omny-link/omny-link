package link.omny.catalog.internal;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import link.omny.catalog.model.StockCategory;
import link.omny.catalog.model.StockItem;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

public class CatalogCsvImporter {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(CatalogCsvImporter.class);

    public List<StockItem> readStockItems(Reader in, String[] headers,
            String tenantId) throws IOException {
        List<StockItem> stockItems = new ArrayList<StockItem>();
        PropertyDescriptor[] propertyDescriptors = BeanUtils
                .getPropertyDescriptors(StockItem.class);
        PropertyDescriptor[] acctPropertyDescriptors = BeanUtils
                .getPropertyDescriptors(StockCategory.class);
        // This JavaDoc is not (currently) true:
        // If your source contains a header record, you can simplify your
        // code and safely reference columns, by using withHeader(String...)
        // with no arguments:
        // CSVFormat.EXCEL.withHeader();

        final CSVParser parser = new CSVParser(in,
                CSVFormat.EXCEL.withHeader(headers));
        Iterable<CSVRecord> records = parser.getRecords();
        // Iterable<CSVRecord> records = CSVFormat.EXCEL.withHeader(headers)
        // .parse(in);

        for (CSVRecord record : records) {
            // skip header
            if (record.getRecordNumber() > 1) {
                StockItem stockItem = new StockItem();
                for (PropertyDescriptor pd : propertyDescriptors) {
                    LOGGER.debug("  " + pd.getName());
                    if (record.isMapped(pd.getName())) {
                        setField(stockItem, pd, record.get(pd.getName()).trim());
                    }
                }

                for (PropertyDescriptor pd : acctPropertyDescriptors) {
                    String name = "stockCategory." + pd.getName();
                    LOGGER.debug("  " + name);
                    if (record.isMapped(name)) {
                        setField(stockItem.getStockCategory(), pd,
                                record.get(name).trim());
                    }
                }
                stockItem.setTenantId(tenantId);
                stockItems.add(stockItem);
            }
        }
        parser.close();
        return stockItems;
    }

    private void setField(Object bean, PropertyDescriptor propertyDescriptor,
            Object value) {
        try {
            Method method = propertyDescriptor.getWriteMethod();
            switch (method.getParameterTypes()[0].getName()) {
            case "boolean":
                method.invoke(bean, Boolean.parseBoolean(value.toString()));
                break;
            default:
                method.invoke(bean, value.toString());
            }

        } catch (IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            LOGGER.error(String.format("Error parsing CSV into %1$s", bean
                    .getClass().getName()));
        }
    }

}

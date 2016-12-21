package link.omny.catalog.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import link.omny.catalog.model.StockItem;

import org.junit.BeforeClass;
import org.junit.Test;

public class CatalogCsvImporterTest {

    private static final String TENANT_ID = "omny";
    private static final String HEADER_LINE = "stockCategory.name,name,tags,customFields.vacant,size";
    private static final String RECORD_1_LINE = "Borehamwood,Studio 101/102,Office,Occupied,475.000";
    private static final String LF = System.getProperty("line.separator");

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Test
    public void testFlexspaceUnits() {
        String content = HEADER_LINE + LF + RECORD_1_LINE + LF;
        List<StockItem> list;
        try {
            list = new CatalogCsvImporter().readStockItems(new StringReader(
                    content), content.substring(0, content.indexOf('\n'))
                    .split(","), TENANT_ID);
            System.out.println(String.format("  found %1$d stockItems",
                    list.size()));
            assertEquals(1, list.size());

            // stock item 1
            assertEquals("Studio 101/102", list.get(0).getName());
            assertEquals("Borehamwood", list.get(0).getStockCategory()
                    .getName());
            assertEquals("Office", list.get(0).getTags());
            assertEquals("475.000", list.get(0).getSize());
            assertEquals(TENANT_ID, list.get(0).getTenantId());

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            fail("Unexpected IO exception: " + e.getMessage());
        }

    }

}

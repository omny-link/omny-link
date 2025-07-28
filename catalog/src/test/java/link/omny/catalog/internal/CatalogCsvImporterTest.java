/*******************************************************************************
 * Copyright 2015-2025 Tim Stephenson and contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package link.omny.catalog.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.junit.jupiter.api.Test;

import link.omny.catalog.model.StockItem;

public class CatalogCsvImporterTest {

    private static final String TENANT_ID = "omny";
    private static final String HEADER_LINE = "stockCategory.name,name,tags,customFields.vacant,size";
    private static final String RECORD_1_LINE = "Trumpton,Studio 101/102,Office,Occupied,475.000";
    private static final String LF = System.getProperty("line.separator");

    @Test
    public void testStockItems() {
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
            assertEquals("Trumpton", list.get(0).getStockCategory()
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

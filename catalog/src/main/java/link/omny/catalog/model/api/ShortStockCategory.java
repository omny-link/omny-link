/*******************************************************************************
 * Copyright 2015-2022 Tim Stephenson and contributors
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
package link.omny.catalog.model.api;

import java.util.Date;
import java.util.Set;

public interface ShortStockCategory {
    String getName();

    String getDescription();

    String getAddress1();

    String getAddress2();

    String getTown();

    String getCountyOrCity();

    String getPostCode();

    String getCountry();

    String getMapUrl();

    Double getLat();

    Double getLng();

    String getVideoCode();

    String getProductSheetUrl();

    String getStatus();

    Date getCreated();

    Date getLastUpdated();
    
    Set<? extends ShortStockItem> getStockItems();
}

/*******************************************************************************
 *Copyright 2015-2018 Tim Stephenson and contributors
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
package link.omny.server.model.mixins;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonView;

import link.omny.catalog.views.OrderViews;
import link.omny.catalog.views.StockCategoryViews;
import link.omny.catalog.views.StockItemViews;
import link.omny.custmgmt.model.views.AccountViews;
import link.omny.custmgmt.model.views.ContactViews;

public abstract class DocumentMixIn {

    @JsonView( {
        // catalog
        OrderViews.Detailed.class,
        StockCategoryViews.Detailed.class,
        StockItemViews.Detailed.class,

        // custmgmt
        AccountViews.Detailed.class,
        ContactViews.Detailed.class,
    } )
    private Long id;

    @JsonView( {
        // catalog
        OrderViews.Detailed.class,
        StockCategoryViews.Detailed.class,
        StockItemViews.Detailed.class,

        // custmgmt
        AccountViews.Detailed.class,
        ContactViews.Detailed.class,
    } )
    private String author;

    @JsonView( {
        // catalog
        OrderViews.Detailed.class,
        StockCategoryViews.Detailed.class,
        StockItemViews.Detailed.class,

        // custmgmt
        AccountViews.Detailed.class,
        ContactViews.Detailed.class,
    } )
    private Date created;

    @JsonView( {
        // catalog
        OrderViews.Detailed.class,
        StockCategoryViews.Detailed.class,
        StockItemViews.Detailed.class,

        // custmgmt
        AccountViews.Detailed.class,
        ContactViews.Detailed.class,
    } )
    private String name;

    @JsonView( {
        // catalog
        OrderViews.Detailed.class,
        StockCategoryViews.Detailed.class,
        StockItemViews.Detailed.class,

        // custmgmt
        AccountViews.Detailed.class,
        ContactViews.Detailed.class,
    } )
    private boolean url;
}

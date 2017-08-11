package link.omny.server.model.mixins;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonView;

import link.omny.catalog.views.OrderViews;
import link.omny.catalog.views.StockCategoryViews;
import link.omny.catalog.views.StockItemViews;
import link.omny.custmgmt.model.views.AccountViews;
import link.omny.custmgmt.model.views.ContactViews;

public abstract class NoteMixIn {

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
    private String content;

    @JsonView( {
        // catalog
        OrderViews.Detailed.class,
        StockCategoryViews.Detailed.class,
        StockItemViews.Detailed.class,

        // custmgmt
        AccountViews.Detailed.class,
        ContactViews.Detailed.class,
    } )
    private boolean favorite;
}

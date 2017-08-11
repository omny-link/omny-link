package link.omny.server.model.mixins;

import com.fasterxml.jackson.annotation.JsonView;

import link.omny.catalog.views.FeedbackViews;
import link.omny.catalog.views.MediaResourceViews;
import link.omny.catalog.views.OrderViews;
import link.omny.catalog.views.StockCategoryViews;
import link.omny.catalog.views.StockItemViews;
import link.omny.custmgmt.model.views.AccountViews;
import link.omny.custmgmt.model.views.ContactViews;
import link.omny.custmgmt.model.views.DocumentViews;
import link.omny.custmgmt.model.views.MemoViews;
import link.omny.custmgmt.model.views.NoteViews;

public abstract class LinkMixIn {

    @JsonView( {
        // catalog
        FeedbackViews.Summary.class,
        MediaResourceViews.Summary.class,
        OrderViews.Summary.class,
        StockCategoryViews.Detailed.class,
        StockItemViews.Detailed.class,

        // custmgmt
        AccountViews.Summary.class,
        ContactViews.Summary.class,
        DocumentViews.Summary.class,
        MemoViews.Summary.class,
        NoteViews.Summary.class,
    } )
    private long rel;

    @JsonView( {
        // catalog
        FeedbackViews.Summary.class,
        MediaResourceViews.Summary.class,
        OrderViews.Summary.class,
        StockCategoryViews.Detailed.class,
        StockItemViews.Detailed.class,

        // custmgmt
        AccountViews.Detailed.class,
        ContactViews.Detailed.class,
        DocumentViews.Detailed.class,
        MemoViews.Detailed.class,
        NoteViews.Detailed.class
    } )
    private long href;

}

package link.omny.catalog.model.mixins;

import com.fasterxml.jackson.annotation.JsonView;

import link.omny.catalog.views.MediaResourceViews;
import link.omny.catalog.views.StockCategoryViews;

public abstract class LinkMixIn {

    @JsonView( {MediaResourceViews.Summary.class, StockCategoryViews.Detailed.class} )
    private long rel;

    @JsonView( {MediaResourceViews.Summary.class, StockCategoryViews.Detailed.class} )
    private long href;
    
}

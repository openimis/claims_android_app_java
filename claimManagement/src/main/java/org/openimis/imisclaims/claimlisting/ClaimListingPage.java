package org.openimis.imisclaims.claimlisting;

import org.json.JSONArray;
import org.openimis.imisclaims.R;
import org.openimis.imisclaims.SQLHandler;

/**
 * Enum specifying available claim listing pages and resource id with the title of the page
 */
public enum ClaimListingPage {
    ENTERED_PAGE(R.string.entered, SQLHandler::getEnteredClaimInfo),
    ACCEPTED_PAGE(R.string.accepted, SQLHandler::getAcceptedClaimInfo),
    REJECTED_PAGE(R.string.rejected, SQLHandler::getRejectedClaimInfo);

    interface PageDataLoader {
        JSONArray loadData(SQLHandler sqlHandler);
    }

    ClaimListingPage(int titleResId, PageDataLoader pageDataLoader) {
        this.titleResId = titleResId;
        this.pageDataLoader = pageDataLoader;
    }

    public final int titleResId;
    private final PageDataLoader pageDataLoader;


    public JSONArray loadPageData(SQLHandler sqlHandler) {
        return pageDataLoader.loadData(sqlHandler);
    }
}


package org.openimis.imisclaims.claimlisting;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Adapter controlling claim listing page bar.
 */
public class ClaimListingPageAdapter extends FragmentPagerAdapter {
    private final Context mContext;

    public ClaimListingPageAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        return ClaimListingFragment.newInstance(ClaimListingPage.values()[position]);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(ClaimListingPage.values()[position].titleResId);
    }

    @Override
    public int getCount() {
        return ClaimListingPage.values().length;
    }
}
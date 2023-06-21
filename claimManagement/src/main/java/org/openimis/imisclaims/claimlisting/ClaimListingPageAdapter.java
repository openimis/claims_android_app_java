package org.openimis.imisclaims.claimlisting;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

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
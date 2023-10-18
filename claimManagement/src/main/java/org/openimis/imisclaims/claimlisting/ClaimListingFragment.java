package org.openimis.imisclaims.claimlisting;

import android.os.Bundle;
import androidx.core.widget.ContentLoadingProgressBar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.openimis.imisclaims.R;
import org.openimis.imisclaims.SQLHandler;

import java.util.Objects;

/**
 * A single claim listing page. This fragment should be used only in claim listing activity
 */
public class ClaimListingFragment extends Fragment {
    private static final String ARG_PAGE = "ClaimListingFragment.ARG_PAGE";

    private RecyclerView claimList;
    private ContentLoadingProgressBar progressIcon;
    private ClaimListingPage page;
    private SQLHandler sqlHandler;
    private ClaimListingFragmentAdapter claimListingFragmentAdapter;

    public static ClaimListingFragment newInstance(ClaimListingPage page) {
        ClaimListingFragment fragment = new ClaimListingFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_PAGE, page.ordinal());
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sqlHandler = new SQLHandler(getActivity());
        if (getArguments() != null) {
            page = ClaimListingPage.values()[getArguments().getInt(ARG_PAGE)];
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (claimListingFragmentAdapter != null) {
            loadContent();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_claim_listing, container, false);

        progressIcon = root.findViewById(R.id.progressIcon);
        claimList = root.findViewById(R.id.claimList);
        claimList.setLayoutManager(new LinearLayoutManager(getContext()));
        claimListingFragmentAdapter = new ClaimListingFragmentAdapter(getActivity(), page, new JSONArray());
        claimList.setAdapter(claimListingFragmentAdapter);

        loadContent();
        return root;
    }

    public void loadContent() {
        new Thread(() -> {
            showProgressIndicator();
            claimListingFragmentAdapter.data = this.page.loadPageData(sqlHandler);
            Objects.requireNonNull(getActivity()).runOnUiThread(() -> claimListingFragmentAdapter.notifyDataSetChanged());
            showContent();
        }).start();
    }

    public void showProgressIndicator() {
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            progressIcon.show();
            claimList.setVisibility(View.GONE);
        });
    }

    public void showContent() {
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            progressIcon.hide();
            claimList.setVisibility(View.VISIBLE);
        });
    }
}
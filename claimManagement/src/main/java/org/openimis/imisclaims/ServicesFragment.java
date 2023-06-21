package org.openimis.imisclaims;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.openimis.imisclaims.domain.entity.Claim;

/**
 * Created by Hiren on 06/09/2019.
 */

public class ServicesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_services, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Claim claim = ((ClaimReview) getContext()).claim;



        ((RecyclerView) view.findViewById(R.id.listOfServices)).setAdapter(
                new ServicesAdapter(claim)
        );
    }
}

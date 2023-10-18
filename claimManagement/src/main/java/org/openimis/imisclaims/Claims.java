package org.openimis.imisclaims;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.recyclerview.widget.RecyclerView;

import org.openimis.imisclaims.domain.entity.Claim;

import java.util.ArrayList;
import java.util.List;

public class Claims extends ImisActivity {
    private static final String CLAIMS_EXTRA = "claims";

    public static Intent newIntent(Context context, List<Claim> claims) {
        return new Intent(context, Claims.class)
                .putParcelableArrayListExtra(CLAIMS_EXTRA, new ArrayList<>(claims));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_claims);
        actionBar.setTitle(getResources().getString(R.string.claims));
        ((RecyclerView) findViewById(R.id.listOfClaims)).setAdapter(
                new ClaimsAdapter(getIntent().getParcelableArrayListExtra(CLAIMS_EXTRA))
        );
    }
}

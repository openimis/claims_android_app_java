package org.openimis.imisclaims.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imisclaims.GetControlsQuery;
import org.openimis.imisclaims.domain.entity.Control;
import org.openimis.imisclaims.network.request.GetControlsGraphQLRequest;
import org.openimis.imisclaims.network.util.Mapper;

import java.util.List;
import java.util.Objects;

public class FetchControls {

    @NonNull
    private final GetControlsGraphQLRequest request;

    public FetchControls() {
        this(new GetControlsGraphQLRequest());
    }

    public FetchControls(
            @NonNull GetControlsGraphQLRequest request
    ) {
        this.request = request;
    }

    @WorkerThread
    @NonNull
    public List<Control> execute() throws Exception {
        return Mapper.map(request.get(), dto -> {
            GetControlsQuery.Node node = Objects.requireNonNull(dto.node());
            return new Control(
                    /* name = */ node.name(),
                    /* usage = */ node.usage(),
                    /* adjustability = */ node.adjustability()
            );
        });
    }
}

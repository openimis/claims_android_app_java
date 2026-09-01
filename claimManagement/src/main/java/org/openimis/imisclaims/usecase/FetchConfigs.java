package org.openimis.imisclaims.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imisclaims.domain.entity.ModuleConfig;
import org.openimis.imisclaims.network.request.GetConfigGraphQLRequest;
import org.openimis.imisclaims.network.util.Mapper;

import java.util.List;

public class FetchConfigs {

    @NonNull
    private final GetConfigGraphQLRequest request;

    public FetchConfigs() {
        this(new GetConfigGraphQLRequest());
    }

    public FetchConfigs(
            @NonNull GetConfigGraphQLRequest request
    ) {
        this.request = request;
    }

    @WorkerThread
    @NonNull
    public List<ModuleConfig> execute() throws Exception {
        return Mapper.map(request.get(), dto -> {
            return new ModuleConfig(
                    /* config = */ dto.config(),
                    /* module = */ dto.module()
            );
        });
    }
}
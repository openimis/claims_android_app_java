package org.openimis.imisclaims.usecase;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import org.openimis.imisclaims.GetConfigsQuery;
import org.openimis.imisclaims.domain.entity.ModuleConfiguration;
import org.openimis.imisclaims.network.request.GetMedicationsGraphQLRequest;
import org.openimis.imisclaims.network.request.GetModuleConfigGraphQLRequest;
import org.openimis.imisclaims.network.util.Mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FetchModuleConfigurations {

    @NonNull
    private final GetModuleConfigGraphQLRequest request;
    public FetchModuleConfigurations(){this(new GetModuleConfigGraphQLRequest());}
    public FetchModuleConfigurations(
            @NonNull GetModuleConfigGraphQLRequest request
    ) {
        this.request = request;
    }

    @WorkerThread
    @NonNull
    public List<ModuleConfiguration> execute()throws  Exception{
        List<ModuleConfiguration> configs = new ArrayList<>();
        List<GetConfigsQuery.ModuleConfiguration> response = request.get("fe");
        for(int i = 0; i < response.size(); i++){
            GetConfigsQuery.ModuleConfiguration node = response.get(i);
            configs.add(new ModuleConfiguration(String.valueOf(i+1),node.module(), node.config()));
        }
        return configs;
    }
}

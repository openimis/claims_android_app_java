package org.openimis.imisclaims.usecase;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;
import org.apache.commons.codec.binary.Base64;
import org.openimis.imisclaims.GetHealthFacilitiesQuery;
import org.openimis.imisclaims.domain.entity.HealthFacility;
import org.openimis.imisclaims.network.request.GetHealthfacilitiesGraphQLRequest;
import org.openimis.imisclaims.network.util.Mapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
public class FetchHealthfacilities {
    @NonNull
    private final GetHealthfacilitiesGraphQLRequest hfRequest;
    public FetchHealthfacilities(){
        this(new GetHealthfacilitiesGraphQLRequest());
    }
    public FetchHealthfacilities(
            @NonNull GetHealthfacilitiesGraphQLRequest hfRequest
    ) {
        this.hfRequest = hfRequest;
    }
    @WorkerThread
    @NonNull
    public List<HealthFacility> execute() throws Exception{
        List<HealthFacility> healthFacilities = new ArrayList<>();
        int page = 0;
        boolean hasNextPage;
        do{
            GetHealthFacilitiesQuery.HealthFacilities response = hfRequest.get(page);
            healthFacilities.addAll(Mapper.map(
                    response.edges(),
                    dto ->{
                        GetHealthFacilitiesQuery.Node node = Objects.requireNonNull(dto.node());
                        byte[] bytes = node.id().getBytes();
                        String id = new String(Base64.decodeBase64(bytes)).split(":")[1];
                        return new HealthFacility(
                                id,
                                node.code(),
                                node.name()
                        );
                    }
            ));
            hasNextPage = response.pageInfo().hasNextPage();
            page = page + 100;
        }while (hasNextPage);
        return healthFacilities;
    }
}
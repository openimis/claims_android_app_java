package org.openimis.imisclaims.usecase;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.openimis.imisclaims.GetHealthFacilityQuery;
import org.openimis.imisclaims.network.request.GetHealthfacilityGraphQLRequest;

import java.util.Collections;
import java.util.List;

public class CheckHealthFacilityTest {

    @Test
    public void execute_mapsEmptyVsNonEmptyToBoolean() throws Exception {
        FakeGetHealthfacilityGraphQLRequest request = new FakeGetHealthfacilityGraphQLRequest();
        CheckHealthFacility checkHealthFacility = new CheckHealthFacility(request);

        request.nextResult = Collections.emptyList();
        assertFalse(checkHealthFacility.execute("HF1"));

        GetHealthFacilityQuery.Node node = new GetHealthFacilityQuery.Node("HealthFacilityGQLType", "1", "HF1", "HF Name");
        GetHealthFacilityQuery.Edge edge = new GetHealthFacilityQuery.Edge("HealthFacilityGQLEdge", node);
        request.nextResult = Collections.singletonList(edge);
        assertTrue(checkHealthFacility.execute("HF1"));
    }

    private static class FakeGetHealthfacilityGraphQLRequest extends GetHealthfacilityGraphQLRequest {
        private List<GetHealthFacilityQuery.Edge> nextResult = Collections.emptyList();

        @Override
        public List<GetHealthFacilityQuery.Edge> get(String hfCode) {
            return nextResult;
        }
    }
}

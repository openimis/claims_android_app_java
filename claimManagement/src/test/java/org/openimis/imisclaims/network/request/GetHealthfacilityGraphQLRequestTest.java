package org.openimis.imisclaims.network.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.apollographql.apollo.api.Input;
import com.apollographql.apollo.api.Operation;
import com.apollographql.apollo.api.Query;
import com.apollographql.apollo.api.Response;

import org.junit.BeforeClass;
import org.junit.AfterClass;
import org.junit.Test;
import org.openimis.imisclaims.Global;
import org.openimis.imisclaims.GetHealthFacilityQuery;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import android.content.SharedPreferences;

public class GetHealthfacilityGraphQLRequestTest {
    private static Object previousGlobalInstance;

    @BeforeClass
    public static void setUpGlobalForStaticClientInit() throws Exception {
        Field instanceField = Global.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        previousGlobalInstance = instanceField.get(null);

        Global global = mock(Global.class);
        SharedPreferences prefs = mock(SharedPreferences.class);
        when(global.getDefaultSharedPreferences()).thenReturn(prefs);
        instanceField.set(null, global);
    }

    @AfterClass
    public static void restoreGlobalInstance() throws Exception {
        Field instanceField = Global.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        instanceField.set(null, previousGlobalInstance);
    }

    @Test
    public void get_returnsEmpty_forNullGraphqlShapes_andEdgesWhenPresent() throws Exception {
        TestableGetHealthfacilityGraphQLRequest request = new TestableGetHealthfacilityGraphQLRequest();

        request.response = responseFor(new GetHealthFacilityQuery(Input.fromNullable("HF1")), null);
        assertTrue(request.get("HF1").isEmpty());

        request.response = responseFor(
                new GetHealthFacilityQuery(Input.fromNullable("HF1")),
                new GetHealthFacilityQuery.Data(null)
        );
        assertTrue(request.get("HF1").isEmpty());

        request.response = responseFor(
                new GetHealthFacilityQuery(Input.fromNullable("HF1")),
                new GetHealthFacilityQuery.Data(new NullEdgesHealthFacilities())
        );
        assertTrue(request.get("HF1").isEmpty());

        GetHealthFacilityQuery.Node node = new GetHealthFacilityQuery.Node("HealthFacilityGQLType", "1", "HF1", "HF Name");
        GetHealthFacilityQuery.Edge edge = new GetHealthFacilityQuery.Edge("HealthFacilityGQLEdge", node);
        GetHealthFacilityQuery.HealthFacilities facilities =
                new GetHealthFacilityQuery.HealthFacilities("HealthFacilityGQLConnection", Collections.singletonList(edge));
        request.response = responseFor(
                new GetHealthFacilityQuery(Input.fromNullable("HF1")),
                new GetHealthFacilityQuery.Data(facilities)
        );

        List<GetHealthFacilityQuery.Edge> result = request.get("HF1");
        assertEquals(1, result.size());
        assertEquals("HF1", result.get(0).node().code());
    }

    private static Response<GetHealthFacilityQuery.Data> responseFor(
            Query<GetHealthFacilityQuery.Data, ?, ?> operation,
            GetHealthFacilityQuery.Data data
    ) {
        return Response.<GetHealthFacilityQuery.Data>builder(operation)
                .data(data)
                .build();
    }

    private static class TestableGetHealthfacilityGraphQLRequest extends GetHealthfacilityGraphQLRequest {
        Response<GetHealthFacilityQuery.Data> response;

        @Override
        protected <T extends Operation.Data> Response<T> makeSynchronous(Query<T, ?, ?> query) {
            return (Response<T>) response;
        }
    }

    private static class NullEdgesHealthFacilities extends GetHealthFacilityQuery.HealthFacilities {
        NullEdgesHealthFacilities() {
            super("HealthFacilityGQLConnection", Collections.emptyList());
        }

        @Override
        public List<GetHealthFacilityQuery.Edge> edges() {
            return null;
        }
    }
}

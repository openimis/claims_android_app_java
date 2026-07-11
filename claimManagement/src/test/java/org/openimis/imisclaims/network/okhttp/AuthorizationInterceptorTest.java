package org.openimis.imisclaims.network.okhttp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.openimis.imisclaims.Global;
import org.openimis.imisclaims.Token;
import org.openimis.imisclaims.network.util.PersistentCookieJar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Connection;
import okhttp3.Interceptor;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Timeout;

public class AuthorizationInterceptorTest {

    private Global global;
    private Token token;
    private PersistentCookieJar cookieJar;
    private AuthorizationInterceptor interceptor;

    @Before
    public void setUp() {
        global = mock(Global.class);
        token = mock(Token.class);
        cookieJar = mock(PersistentCookieJar.class);
        when(global.getJWTToken()).thenReturn(token);
        when(global.getCookieJar()).thenReturn(cookieJar);
        interceptor = new AuthorizationInterceptor(global);
    }

    @Test
    public void intercept_addsExpectedHeaders_basedOnAuthAndCsrfState() throws Exception {
        when(token.isTokenValidJWT()).thenReturn(true);
        when(token.getTokenText()).thenReturn(" jwt_token ");

        when(global.getCsrfToken()).thenReturn("csrf-1");
        Request request1 = new Request.Builder().url("https://demo.openimis.org/api/graphql").build();
        FakeChain chain1 = new FakeChain(request1, responseFor(request1, 200, "ok"));
        interceptor.intercept(chain1);
        Request sent1 = chain1.proceededRequests.get(0);
        assertEquals("bearer jwt_token", sent1.header("Authorization"));
        assertEquals("mobile_app", sent1.header("User-Agent"));
        assertEquals("csrf-1", sent1.header("X-CSRFToken"));

        when(global.getCsrfToken()).thenReturn(" ");
        Request request2 = new Request.Builder().url("https://demo.openimis.org/api/graphql").build();
        FakeChain chain2 = new FakeChain(request2, responseFor(request2, 200, "ok"));
        interceptor.intercept(chain2);
        Request sent2 = chain2.proceededRequests.get(0);
        assertNull(sent2.header("X-CSRFToken"));

        when(token.isTokenValidJWT()).thenReturn(false);
        Request request3 = new Request.Builder().url("https://demo.openimis.org/api/graphql").build();
        FakeChain chain3 = new FakeChain(request3, responseFor(request3, 200, "ok"));
        interceptor.intercept(chain3);
        Request sent3 = chain3.proceededRequests.get(0);
        assertNull(sent3.header("Authorization"));
        assertNull(sent3.header("X-CSRFToken"));
    }

    @Test
    public void intercept_clearsSessionAndRetries_onUnauthorizedSignals() throws Exception {
        when(token.isTokenValidJWT()).thenReturn(true);
        when(token.getTokenText()).thenReturn("jwt");
        when(global.getCsrfToken()).thenReturn("csrf");

        Request request1 = new Request.Builder().url("https://demo.openimis.org/api/graphql").build();
        FakeChain chain401 = new FakeChain(
                request1,
                responseFor(request1, 401, "unauthorized"),
                responseFor(request1, 200, "retried")
        );
        Response retried401 = interceptor.intercept(chain401);
        assertEquals(2, chain401.proceededRequests.size());
        assertEquals(200, retried401.code());

        Request request2 = new Request.Builder().url("https://demo.openimis.org/api/graphql").build();
        FakeChain chainCsrf = new FakeChain(
                request2,
                responseFor(request2, 200, "Missing 'csrftoken'"),
                responseFor(request2, 200, "retried")
        );
        Response retriedCsrf = interceptor.intercept(chainCsrf);
        assertEquals(2, chainCsrf.proceededRequests.size());
        assertEquals(200, retriedCsrf.code());

        verify(token, times(2)).clearToken();
        verify(cookieJar, times(2)).clear();
    }

    @Test
    public void intercept_noRetry_whenResponseIsAuthorizedAndNoCsrfMarker() throws Exception {
        when(token.isTokenValidJWT()).thenReturn(true);
        when(token.getTokenText()).thenReturn("jwt");
        when(global.getCsrfToken()).thenReturn("csrf");

        Request request = new Request.Builder().url("https://demo.openimis.org/api/graphql").build();
        FakeChain chain = new FakeChain(request, responseFor(request, 200, "all good"));

        Response response = interceptor.intercept(chain);

        assertEquals(1, chain.proceededRequests.size());
        assertEquals(200, response.code());
        assertTrue(response.body().string().contains("all good"));
    }

    private static Response responseFor(Request request, int code, String body) {
        return new Response.Builder()
                .request(request)
                .protocol(Protocol.HTTP_1_1)
                .code(code)
                .message("msg")
                .body(ResponseBody.create(body, okhttp3.MediaType.parse("application/json")))
                .build();
    }

    private static class FakeChain implements Interceptor.Chain {
        private final Request originalRequest;
        private final List<Response> queuedResponses;
        private int responseIndex = 0;
        private final List<Request> proceededRequests = new ArrayList<>();

        FakeChain(Request originalRequest, Response... responses) {
            this.originalRequest = originalRequest;
            this.queuedResponses = Arrays.asList(responses);
        }

        @Override
        public Request request() {
            return originalRequest;
        }

        @Override
        public Response proceed(Request request) throws IOException {
            proceededRequests.add(request);
            if (responseIndex >= queuedResponses.size()) {
                throw new IOException("No queued response");
            }
            return queuedResponses.get(responseIndex++);
        }

        @Override
        public Connection connection() {
            return null;
        }

        @Override
        public Call call() {
            return new Call() {
                @Override
                public Request request() {
                    return originalRequest;
                }

                @Override
                public Response execute() {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void enqueue(okhttp3.Callback responseCallback) {
                    throw new UnsupportedOperationException();
                }

                @Override
                public void cancel() {
                }

                @Override
                public boolean isExecuted() {
                    return false;
                }

                @Override
                public boolean isCanceled() {
                    return false;
                }

                @Override
                public Timeout timeout() {
                    return Timeout.NONE;
                }

                @Override
                public Call clone() {
                    return this;
                }
            };
        }

        @Override
        public int connectTimeoutMillis() {
            return 0;
        }

        @Override
        public Interceptor.Chain withConnectTimeout(int timeout, TimeUnit unit) {
            return this;
        }

        @Override
        public int readTimeoutMillis() {
            return 0;
        }

        @Override
        public Interceptor.Chain withReadTimeout(int timeout, TimeUnit unit) {
            return this;
        }

        @Override
        public int writeTimeoutMillis() {
            return 0;
        }

        @Override
        public Interceptor.Chain withWriteTimeout(int timeout, TimeUnit unit) {
            return this;
        }
    }
}

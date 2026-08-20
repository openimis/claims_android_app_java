package org.openimis.imisclaims;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.openimis.imisclaims.network.util.PersistentCookieJar;

@RunWith(MockitoJUnitRunner.class)
public class GlobalSessionTest {

    @Mock
    private Token token;
    @Mock
    private SharedPreferences preferences;
    @Mock
    private PersistentCookieJar cookieJar;

    private Global global;

    @Before
    public void setUp() {
        global = spy(new Global());
        doReturn(token).when(global).getJWTToken();
        doReturn(preferences).when(global).getDefaultSharedPreferences();
        global.setCookieJar(cookieJar);
    }

    @Test
    public void isLoggedIn_trueOnlyWhenJwtValidAndSessionNotExpired_otherwiseClearsState() {
        when(token.isTokenValidJWT()).thenReturn(true);
        when(preferences.getLong("session_expiry", 0)).thenReturn(System.currentTimeMillis() + 5_000);
        assertTrue(global.isLoggedIn());
        verify(token, never()).clearToken();

        when(token.isTokenValidJWT()).thenReturn(false);
        when(preferences.getLong("session_expiry", 0)).thenReturn(System.currentTimeMillis() + 5_000);
        assertFalse(global.isLoggedIn());
        verify(token).clearToken();
        verify(cookieJar).clear();

        when(token.isTokenValidJWT()).thenReturn(true);
        when(preferences.getLong("session_expiry", 0)).thenReturn(System.currentTimeMillis() - 5_000);
        assertFalse(global.isLoggedIn());
    }

    @Test
    public void cookieJar_reference_setAndGet_roundTrip() {
        PersistentCookieJar anotherJar = org.mockito.Mockito.mock(PersistentCookieJar.class);
        global.setCookieJar(anotherJar);
        assertSame(anotherJar, global.getCookieJar());
    }
}

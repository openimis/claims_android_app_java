package org.openimis.imisclaims.network.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import okhttp3.Cookie;
import okhttp3.HttpUrl;

@RunWith(RobolectricTestRunner.class)
public class PersistentCookieJarTest {

    private SharedPreferences prefs;
    private PersistentCookieJar cookieJar;
    private HttpUrl url;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        prefs = context.getSharedPreferences("cookie_jar_test", Context.MODE_PRIVATE);
        prefs.edit().clear().commit();
        cookieJar = new PersistentCookieJar(prefs);
        url = HttpUrl.get("https://demo.openimis.org/api/graphql");
    }

    @Test
    public void sessionCookie_lifecycle_saveLoadClear_behavesCorrectly() {
        long expiry = System.currentTimeMillis() + 60_000;
        Cookie sessionCookie = new Cookie.Builder()
                .name("openimis_session")
                .value("abc123")
                .domain("demo.openimis.org")
                .path("/")
                .expiresAt(expiry)
                .build();

        cookieJar.saveFromResponse(url, Collections.singletonList(sessionCookie));
        List<Cookie> loaded = cookieJar.loadForRequest(url);
        assertEquals(1, loaded.size());
        assertEquals("openimis_session", loaded.get(0).name());
        assertEquals("abc123", loaded.get(0).value());
        assertEquals(expiry, prefs.getLong("session_expiry", -1));

        cookieJar.clear();
        assertTrue(cookieJar.loadForRequest(url).isEmpty());
        assertEquals(-1, prefs.getLong("session_expiry", -1));
    }

    @Test
    public void saveFromResponse_ignoresNonSessionCookies_andMissingStateReturnsEmpty() {
        Cookie otherCookie = new Cookie.Builder()
                .name("another_cookie")
                .value("zzz")
                .domain("demo.openimis.org")
                .path("/")
                .expiresAt(System.currentTimeMillis() + 60_000)
                .build();

        cookieJar.saveFromResponse(url, Arrays.asList(otherCookie));
        assertTrue(cookieJar.loadForRequest(url).isEmpty());

        prefs.edit().putString("session_value", "only_value_no_expiry").remove("session_expiry").commit();
        assertTrue(cookieJar.loadForRequest(url).isEmpty());
    }
}

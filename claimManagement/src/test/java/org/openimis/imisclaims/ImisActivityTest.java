package org.openimis.imisclaims;
import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
public class ImisActivityTest {

    static class TestActivity extends ImisActivity {}

    private String savedLanguage() {
        TestActivity activity = Robolectric.buildActivity(TestActivity.class)
                .create()
                .get();

        try {
            return activity.global.getSavedLanguage();
        } finally {
            // Close the real databases opened in onCreate so the connections to
            // the shared test DB files do not leak and cause SQLITE_BUSY in
            // later tests of the suite.
            if (activity.sqlHandler != null) {
                activity.sqlHandler.closeDatabases();
            }
        }
    }

    @Test
    @Config(qualifiers = "fr")
    public void french_locale_saves_fr() {
        assertEquals("fr", savedLanguage());
    }

    @Test
    @Config(qualifiers = "en")
    public void english_locale_saves_en() {
        assertEquals("en", savedLanguage());
    }

    @Test
    @Config(qualifiers = "es")
    public void unsupported_locale_defaults_en() {
        assertEquals("en", savedLanguage());
    }
}
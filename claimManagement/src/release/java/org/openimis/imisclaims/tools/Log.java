package org.openimis.imisclaims.tools;

import android.content.Context;
import android.net.Uri;
import android.support.v4.content.FileProvider;

import org.openimis.imisclaims.AppInformation;
import org.openimis.imisclaims.BuildConfig;
import org.openimis.imisclaims.Global;
import org.openimis.imisclaims.util.ZipUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

/**
 * This class is a wrapper for the default android Log class, adding additional functionality while
 * keeping the android.util.Log api
 * Release version should not allow any logging
 */
public class Log {
    public static final boolean isLoggingEnabled = BuildConfig.LOGGING_ENABLED;

    public static void v(String tag, String msg) {
    }

    public static void v(String tag, String msg, Throwable thr) {
    }

    public static void d(String tag, String msg) {
    }

    public static void d(String tag, String msg, Throwable thr) {
    }

    public static void i(String tag, String msg) {
    }

    public static void i(String tag, String msg, Throwable thr) {
    }

    public static void w(String tag, String msg) {
    }

    public static void w(String tag, String msg, Throwable thr) {
    }

    public static void e(String tag, String msg) {
    }

    public static void e(String tag, String msg, Throwable thr) {
    }

    public static void zipLogFiles(Context context) {
    }

    public static void deleteLogFiles() {
    }
}
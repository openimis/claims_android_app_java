package org.openimis.imisclaims.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openimis.imisclaims.tools.Log;
import org.xmlpull.v1.XmlSerializer;

import java.io.IOException;
import java.util.Iterator;

public class XmlUtils {
    private static final String LOG_TAG = "XMLUTILS";

    /**
     * Serialize gives key value pair as an XML tag. This method will capitalize the key.
     *
     * @param serializer XML serializer object with already started document
     * @param key        key to be used for this value
     * @param value      value to be used as tag value
     */
    public static void serializeXml(XmlSerializer serializer, String key, String value) {
        String capitalizedKey = StringUtils.capitalize(key);
        try {

            serializer.startTag(null, capitalizedKey);
            if (!StringUtils.isEmpty(value)) {
                serializer.text(value);
            }

            serializer.endTag(null, capitalizedKey);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error while serializing string key:" + key + ", value: " + value, e);
        }
    }

    /**
     * Serialize a JSON object as xml tag. This method will capitalize the key.
     *
     * @param serializer XML serializer object with already started document
     * @param key        Key to be used for this object
     * @param object     Object to be serialized
     */
    public static void serializeXml(XmlSerializer serializer, String key, JSONObject object) {
        String capitalizedKey = StringUtils.capitalize(key);
        Iterator<String> it = object.keys();

        try {
            serializer.startTag(null, capitalizedKey);
            while (it.hasNext()) {
                String nextKey = it.next();
                Object o = object.get(nextKey);
                if (o instanceof String) serializeXml(serializer, nextKey, (String) o);
                else if (o instanceof JSONObject) serializeXml(serializer, nextKey, (JSONObject) o);
                else if (o instanceof JSONArray) serializeXml(serializer, nextKey, (JSONArray) o);
                else throw new IOException("Unknown value type for key: " + nextKey);
            }
            serializer.endTag(null, capitalizedKey);
        } catch (IOException | JSONException e) {
            Log.e(LOG_TAG, "Error while serializing json object key:" + key + ", value: " + object, e);
        }
    }

    /**
     * Serialize a JSON array as xml tag. This method will capitalize the key.
     *
     * @param serializer XML serializer object with already started document
     * @param key        Key to be used for this object. This key will be used to derive the item key
     *                   by removing last "s" character (Items -> Item). If the key does not end with "s"
     *                   it will be used also as item key.
     * @param array      Array to be serialized
     */
    public static void serializeXml(XmlSerializer serializer, String key, JSONArray array) {
        String capitalizedKey = StringUtils.capitalize(key);
        String nextKey = capitalizedKey;
        if (capitalizedKey.endsWith("s")) {
            //remove last letter
            nextKey = capitalizedKey.substring(0, capitalizedKey.length() - 1);
        }

        try {
            serializer.startTag(null, capitalizedKey);
            for (int i = 0; i < array.length(); i++) {
                Object o = array.get(i);
                if (o instanceof JSONObject) serializeXml(serializer, nextKey, (JSONObject) o);
                else if (o instanceof JSONArray) serializeXml(serializer, nextKey, (JSONArray) o);
                else serializeXml(serializer, nextKey, o.toString());
            }
            serializer.endTag(null, capitalizedKey);
        } catch (IOException | JSONException e) {
            Log.e(LOG_TAG, "Error while serializing json array key:" + key + ", value: " + array, e);
        }
    }
}

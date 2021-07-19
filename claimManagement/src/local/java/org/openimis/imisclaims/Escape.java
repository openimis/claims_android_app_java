package org.openimis.imisclaims;

public class Escape {
    public boolean CheckCHFID(String InsureeNumber) {
        return InsureeNumber != null && InsureeNumber.length() != 0;
    }
}

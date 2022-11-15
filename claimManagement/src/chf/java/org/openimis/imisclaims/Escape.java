package org.openimis.imisclaims;

public class Escape {
    public boolean CheckCHFID(String InsureeNumber) {
        if (InsureeNumber.length() != 9) return false;
        int actualControlNumber, expectedControlNumber;

        expectedControlNumber = Integer.parseInt(InsureeNumber.substring(8));
        actualControlNumber = Integer.parseInt(InsureeNumber.substring(0, 8)) % 7;

        return expectedControlNumber == actualControlNumber;
    }
}

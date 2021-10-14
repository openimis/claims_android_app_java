package org.openimis.imisclaims;

public class Escape {
    public boolean CheckCHFID(String insureeNumber) {
        if (insureeNumber.length() != 10) return false;
        int actualControlNumber, expectedControlNumber;

        expectedControlNumber = Integer.parseInt(insureeNumber.substring(9));
        actualControlNumber = Integer.parseInt(insureeNumber.substring(0, 9)) % 7;

        return expectedControlNumber == actualControlNumber;
    }
}

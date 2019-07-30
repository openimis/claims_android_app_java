package org.openimis.imisclaims;

public class Escape {

    public boolean CheckCHFID(){
        EnquireActivity enquireActivity = new EnquireActivity();
        if (enquireActivity.getEtCHFID().getText().length() == 0){
            enquireActivity.ShowDialog(enquireActivity.getTvCHFID(), enquireActivity.getResources().getString(R.string.MissingCHFID));
            return false;
        }

        return true;
    }

}

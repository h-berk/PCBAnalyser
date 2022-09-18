package hberk.pcbanalyser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;

public class Utilities {

    /**
    * https://stackoverflow.com/questions/32444193/count-different-values-in-array-in-java
     */
    public static int diffValues(int[] numArray){
        int numOfDifferentVals = 0;

        ArrayList<Integer> diffNum = new ArrayList<>();

        for(int i=0; i<numArray.length; i++){
            if(!diffNum.contains(numArray[i])){
                diffNum.add(numArray[i]);
            }
        }

        if(diffNum.size()==1){
            numOfDifferentVals = 0;
        }
        else{
            numOfDifferentVals = diffNum.size();
        }

        return numOfDifferentVals;
    }

    public static boolean max30Chars(String string) {
        return string.length() <= 30;
    }

    public static boolean validEmail(String email) {
        if (email != null) {
            return (email.contains("@") && email.contains("."));
        }
        return false;
    }

    public static boolean validTelephone(String telephone) {
        return telephone.matches("\\d{10}");
    }

    public static boolean onlyContainsNumbers(String text) {
        if (text != null) {
            String numbersOnly = "[0-9]+";
            return (text.matches(numbersOnly));
        }
        return false;
    }

    public static boolean max10Chars(String string) {
        if (string.length() <= 10) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean validPPS(String pps) {
        return pps.matches("[0-9]{7}[A-z]");
    }

    public static boolean validEirCode(String eirCode) {
        return eirCode.matches("[ACDEFHKNPRTVWXY]{1}[0-9]{1}[0-9W]{1}[\\ \\-]?[0-9ACDEFHKNPRTVWXY]{4}"); //src= https://stackoverflow.com/questions/33391412/validation-for-irish-eircode
    }

    public static boolean validBoothIdentifier(String boothIdentifier) {
        return boothIdentifier.matches("[A-z][0-9]");
    }

    public static boolean validPastDateTime(LocalDateTime dateTime) {
        if (dateTime != null) {
            return !dateTime.isAfter(LocalDateTime.now());
        } else {
            return false;
        }
    }

    public static boolean validPastDate(LocalDate date) {
        if (date != null) {
            return !date.isAfter(LocalDate.now());
        } else {
            return false;
        }
    }
}

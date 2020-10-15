package com.example.projet_dintgration.Utilities;

public class NumberUtil {
    private NumberUtil() {}

    public static boolean tryParseInt(String input){
        try {
            Integer.parseInt(input);
            return true;
        }catch (Exception e){
            return false;
        }
    }
}

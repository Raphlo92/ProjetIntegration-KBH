package com.example.projet_dintgration.Utilities;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
    private StringUtil(){ }

    public static String Strip(String input){
        return input.replaceAll("[()\\[\\]{}]", "");
    }

    public static boolean Matches(String match, String input){
        Pattern pattern;
        Matcher matcher;
        return false;
    }

    public static String ReplaceAbbreviations(String input){
        input = input.replaceAll("Alt.", "Alternative");

        return input;
    }
}

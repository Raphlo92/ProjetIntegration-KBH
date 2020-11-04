package com.example.projetdintegration.Utilities;

import java.util.ArrayList;
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

    public static String toCommaSeparatedString(ArrayList<Integer> list) {
        if (list.size() > 0) {
            StringBuilder nameBuilder = new StringBuilder();
            for (Integer item : list) {
                nameBuilder.append(item).append(", ");
            }
            nameBuilder.deleteCharAt(nameBuilder.length() - 1);
            nameBuilder.deleteCharAt(nameBuilder.length() - 1);
            return nameBuilder.toString();
        } else {
            return "";
        }
    }
}

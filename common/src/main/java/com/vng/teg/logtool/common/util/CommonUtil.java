package com.vng.teg.logtool.common.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: sonnt4
 * Date: 12/11/13
 * Time: 11:29 AM
 * To change this template use File | Settings | File Templates.
 */
public class CommonUtil {
    static Pattern p = Pattern.compile("\\d+");

    public static void main(String[] args){
//        System.out.println(String.valueOf(-1));
        System.out.println(String.format("%,.0f", (double)1000));
    }
    public static String formatNumber(Integer number) {
        return String.format("%,.0f", (double)number);
    }
    public static String renderMessage(String template, Map<String, String> data) {
        Pattern pattern = Pattern.compile("\\{(.+?)\\}");
        Matcher matcher = pattern.matcher(template);

        StringBuffer buffer = new StringBuffer();

        while (matcher.find()) {
            String replacement = data.get(matcher.group(1));
            if (replacement != null) {
                matcher.appendReplacement(buffer, "");
                buffer.append(replacement);
            }
        }

        matcher.appendTail(buffer);
        return buffer.toString();
    }
    public static String printPrettyObj(Object obj){
        String jsString = gson.toJson(obj);
        return printPretty(jsString);
    }
    public static String printPretty(String jsString){
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(jsString);
        return gson.toJson(je);
    }
    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

}

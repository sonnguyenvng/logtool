package com.vng.teg.logtool.common.util;

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
        System.out.println(String.valueOf(-1));
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
    public static String getWebChannelZoneFromWebsite(String website){
        return getWebChannelZoneFromWebsite(website, ";");
    }
    public static String getWebChannelZoneFromWebsite(String website, String splitCharacter){
        String web = "", ch = "", zone = "";
        String[] webArray = website.split("_");
        if(webArray.length > 0){
            web = webArray[0];
        }
        if(webArray.length > 1){
            ch = webArray[1];
        }
        if(webArray.length > 2){
            zone = webArray[2];
        }
        return web + splitCharacter  + ch + splitCharacter + zone;
    }

}

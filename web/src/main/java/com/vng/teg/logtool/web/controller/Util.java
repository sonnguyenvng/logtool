package com.vng.teg.logtool.web.controller;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by tungpv on 28/03/2016.
 */
public class Util {

    public String getHttpClient(String url) throws IOException {
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(url);

        //request.addHeader("accept", "application/json");
        HttpResponse getResponse = client.execute(request);
        if (getResponse.getStatusLine().getStatusCode() != 200) {
            throw new RuntimeException("Failed : HTTP error code : " + getResponse.getStatusLine().getStatusCode());
        }
        BufferedReader rd = new BufferedReader(new InputStreamReader(getResponse.getEntity().getContent()));
        StringBuffer result = new StringBuffer();
        String line = "";
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        client.getConnectionManager().shutdown();
        return String.valueOf(result);
    }

    public String customFormatDate(String oldFormat, String newFormat, String newTimeZone, String stringDate) throws ParseException {
        String dateResult = "";
        TimeZone tz;
        if (newTimeZone != null && !newTimeZone.isEmpty()) {
            tz = TimeZone.getTimeZone(newTimeZone);
        } else {
            tz = Calendar.getInstance().getTimeZone();
        }
        try {
            SimpleDateFormat sdfOld = new SimpleDateFormat(oldFormat);
            Date dateOfOldFormat = sdfOld.parse(stringDate);
            SimpleDateFormat sdfNew = new SimpleDateFormat(newFormat);
            sdfNew.setTimeZone(tz);
            Date dateOfNewFormat = sdfNew.parse(sdfNew.format(dateOfOldFormat));
            dateResult = sdfNew.format(dateOfNewFormat);
        }catch (Exception e){
            e.printStackTrace();
        }
        return dateResult;
    }
}

package com.vng.teg.logtool.web.controller;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by sonnguyen on 22/03/2016.
 */
@RestController
public class AlertController extends ApplicationObjectSupport {
    @RequestMapping(value = "/public/hello", method= RequestMethod.GET)
    public String hello() {
        return "abc";
    }
    @RequestMapping(value = "/public/hello2", method= RequestMethod.GET, produces={"application/json"})
    public void hello2(@RequestParam(value= "name", required = false) String name, HttpServletResponse response) {
        String jsonResponse = "Hello" + name;
//        response.setContentType(responseType);
        try {
            response.getOutputStream().write(jsonResponse.getBytes());
            response.getOutputStream().close();
        } catch (Exception e){

        }

    }

    @RequestMapping(value = "/public/alert", method = RequestMethod.GET, produces = {"application/json"})
    public String alert(@RequestParam(value = "actionId", required = false) String actionId, HttpServletResponse response) {
        String dataJob = "";
        String dataJobChild = "";
        long total = 0;
        String nominalTime = "";
        String oldFormat = "EEE,dd MMM yyyy HH:mm:ss z";
        String newFormat = "yyyy-MM-dd HH:mm:ss";
        String setTimeZone = "Asia/Ho_Chi_Minh";
        String nominalTimeFormat = "";
        try {
            Util util = new Util();
            String urlJob = "http://10.60.43.7:11000/oozie/v2/job/" + actionId + "?show=info&timezone=GMT";
            dataJob = util.getHttpClient(urlJob);
            JSONObject jsJob = (JSONObject) new JSONParser().parse(String.valueOf(dataJob));
            total = (Long) jsJob.get("total");

            String urlJobChild = "http://10.60.43.7:11000/oozie/v2/job/" + actionId + "@" + total + "?show=info&timezone=GMT";
            dataJobChild = util.getHttpClient(urlJobChild);
            JSONObject jsJobChild = (JSONObject) new JSONParser().parse(String.valueOf(dataJobChild));
            nominalTime = (String) jsJobChild.get("nominalTime");

            nominalTimeFormat = util.customFormatDate(oldFormat, newFormat, setTimeZone, nominalTime);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }catch (java.text.ParseException e){
            e.printStackTrace();
        }
        return nominalTimeFormat;

    }
}

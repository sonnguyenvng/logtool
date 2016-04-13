package com.vng.teg.logtool.web.controller;

import com.vng.teg.logtool.common.Constants;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;

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

    @RequestMapping(value = "/public/wf/alert", method = RequestMethod.GET, produces = {"application/json"})
    public String alert(@RequestParam(value = "wfId", required = false) String wfId, HttpServletResponse response) {
        String dataJob = "";
        String dataJobChild = "";
        String parentId="";
        String nominalTime = "";
        String oldFormat = "EEE,dd MMM yyyy HH:mm:ss z";
        String newFormat = "yyyy-MM-dd HH:mm:ss";
        String setTimeZone = "Asia/Ho_Chi_Minh";
        String nominalTimeFormat = "";
        try {
            String prefixURL = propertyFactory.getObject().getProperty(Constants.OOZIE_API_URL);
            Util util = new Util();
            String urlJob = prefixURL + "/v2/job/" + wfId + "?show=info&timezone=GMT";
            dataJob = util.getHttpClient(urlJob);
            System.out.println(String.format("%s\n%s", urlJob, Util.printPrettyObj(dataJob)));
            JSONObject jsJob = (JSONObject) new JSONParser().parse(String.valueOf(dataJob));
            parentId =(String) jsJob.get("parentId");

            String urlJobParent = prefixURL + "/v2/job/" + parentId + "?show=info&timezone=GMT";
            dataJobChild = util.getHttpClient(urlJobParent);
            System.out.println(String.format("%s\n%s", urlJobParent, Util.printPrettyObj(dataJobChild)));
            JSONObject jsJobChild = (JSONObject) new JSONParser().parse(String.valueOf(dataJobChild));
            nominalTime = (String) jsJobChild.get("nominalTime");

            nominalTimeFormat = util.customFormatDate(oldFormat, newFormat, setTimeZone, nominalTime);

            System.out.println();
            System.out.println(wfId + "\t" + nominalTimeFormat);
            System.out.println();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return nominalTimeFormat;

    }

    @Autowired
    private PropertiesFactoryBean propertyFactory;
}

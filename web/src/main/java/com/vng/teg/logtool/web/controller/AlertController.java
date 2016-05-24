package com.vng.teg.logtool.web.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vng.teg.logtool.common.Constants;
import com.vng.teg.logtool.common.util.CommonUtil;
import com.vng.teg.logtool.common.util.DBUtil;
import com.vng.teg.logtool.common.util.EmailUtil;
import com.vng.teg.logtool.common.util.TimestampUtil;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by sonnguyen on 22/03/2016.
 */
@RestController
public class AlertController extends ApplicationObjectSupport {

    @RequestMapping(value = "/public/wf/notify/alllogs", method = RequestMethod.GET, produces = {"application/json"})
    public String alertAllLogs(@RequestParam(value = "wfId", required = false) String wfId,
                               @RequestParam(value = "gc", required = false) String gc,
                               @RequestParam(value = "date", required = false) String dateStr,
                               @RequestParam(value = "status", required = false) String status,
                               @RequestParam(value = "tz", required = false) String timezone,
                               HttpServletResponse response) {
        String dataJob = "";
        String dataJobChild = "";
        String parentId="";
        String nominalTime = "";
        String oldFormat = "EEE,dd MMM yyyy HH:mm:ss z";
        String newFormat = "yyyy-MM-dd";
//        String newTimeZone = "Asia/Ho_Chi_Minh";
        String newTimeZone = StringUtils.isNotBlank(timezone)? timezone : "GMT";
        String nominalTimeFormat = "";
        System.out.println(String.format("\t\t %s, %s, %s", gc, wfId, status));
        Map<String, String> renderData;
        String qTemplate;
        Connection mysqlConn = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            if(StringUtils.isNotBlank(gc) && "SUCCEEDED".equals(status)){
                if(StringUtils.isNotBlank(wfId)){
                    String prefixURL = propertyFactory.getObject().getProperty(Constants.OOZIE_API_URL);
                    Util util = new Util();
                    String urlJob = prefixURL + "/v2/job/" + wfId + "?show=info&timezone=GMT";
                    dataJob = util.getHttpClient(urlJob);
//                    System.out.println(String.format("%s\n%s", urlJob, Util.printPrettyObj(dataJob)));
                    JSONObject jsJob = (JSONObject) new JSONParser().parse(String.valueOf(dataJob));
                    parentId =(String) jsJob.get("parentId");

                    String urlJobParent = prefixURL + "/v2/job/" + parentId + "?show=info&timezone=GMT";
                    dataJobChild = util.getHttpClient(urlJobParent);
//                    System.out.println(String.format("%s\n%s", urlJobParent, Util.printPrettyObj(dataJobChild)));
                    JSONObject jsJobChild = (JSONObject) new JSONParser().parse(String.valueOf(dataJobChild));
                    nominalTime = (String) jsJobChild.get("nominalTime");

                    nominalTimeFormat = util.customFormatDate(oldFormat, newFormat, newTimeZone, nominalTime);
                }else if(StringUtils.isNotBlank(dateStr)){
                    nominalTimeFormat = dateStr;
                }

                System.out.println();
                System.out.println(wfId + "\t" + nominalTimeFormat);
                System.out.println();
                String connStr = propertyFactory.getObject().getProperty(Constants.DB_MYSQL_CONNECTION);
                mysqlConn = DBUtil.createMySQLConnection(connStr);
                if(mysqlConn != null) {
                    qTemplate = propertyFactory.getObject().getProperty(Constants.DB_ALERT_STATUS_INSERT);
                    if(StringUtils.isNotBlank(qTemplate)){
                        Calendar curCal = Calendar.getInstance();
                        Calendar cal = Calendar.getInstance();
                        DateFormat df = new SimpleDateFormat(newFormat);
                        Date d = df.parse(nominalTimeFormat);
                        cal.setTime(d);
//                        curCal.set(Calendar.YEAR, cal.get(Calendar.YEAR));
//                        curCal.set(Calendar.MONTH, cal.get(Calendar.MONTH));
//                        curCal.set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH));
                        curCal.add(Calendar.MINUTE, 30);
                        df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        renderData = new HashMap<String, String>();
                        renderData.put(Constants.GAME_CODE, gc);
                        renderData.put(Constants.FROM_DATE, nominalTimeFormat);
                        renderData.put(Constants.NEXT_ALERT_DATE, df.format(curCal.getTime()));
                        renderData.put(Constants.WF_ID, StringUtils.defaultString(wfId, ""));
                        renderData.put(Constants.COORD_ID, StringUtils.defaultString(parentId, ""));
                        String q = CommonUtil.renderMessage(qTemplate, renderData);
                        System.out.println(q);
                        statement = mysqlConn.createStatement();
                        DBUtil.executeMySQL(statement, q);
                    }
                }

//                Map map = new HashMap();
//                map.put(Constants.GAME_CODE, gc);
//                map.put(Constants.LOG_DATE, nominalTimeFormat);
//                jmsTemplate.convertAndSend("alertQueue", map);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (mysqlConn != null) {
                    mysqlConn.close();
                }
            } catch (Exception ex) {}
        }
        return nominalTimeFormat;

    }

    @RequestMapping(value = {"/public/alert/data/save"}, method= RequestMethod.POST)
    public String saveConsultantProfile(HttpServletRequest request,
                                               @RequestParam(value= "file", required = false) MultipartFile file) throws Exception{
        ByteArrayInputStream stream = new   ByteArrayInputStream(file.getBytes());
        String sql = IOUtils.toString(stream, "UTF-8");
        if(StringUtils.isNotBlank(sql)){
            Connection mysqlConn = null;
            Statement statement = null;
            ResultSet resultSet = null;
            try {
                String connStr = propertyFactory.getObject().getProperty(Constants.DB_MYSQL_CONNECTION);
                System.out.println(connStr);
                mysqlConn = DBUtil.createMySQLConnection(connStr);
                if(mysqlConn != null) {
                    statement = mysqlConn.createStatement();
                    DBUtil.executeMySQL(statement, sql);
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    if (statement != null) {
                        statement.close();
                    }
                    if (mysqlConn != null) {
                        mysqlConn.close();
                    }
                } catch (Exception ex) {}
            }
        }
        return sql;
    }
    @RequestMapping(value="/public/alert/reload", method = RequestMethod.GET)
    protected String reloadAlertConfig(HttpServletRequest request, HttpServletResponse response,
                                       @RequestParam(value= "gc", required = false) String gc) throws Exception {
        StringBuilder sb = new StringBuilder();
        if(StringUtils.isNotBlank(gc)){
            Connection mysqlConn = null;
            Statement statement = null;
            ResultSet resultSet = null;
            try {
                Calendar cal = Calendar.getInstance();
                String connStr = propertyFactory.getObject().getProperty(Constants.DB_MYSQL_CONNECTION);
                System.out.println(connStr);
                mysqlConn = DBUtil.createMySQLConnection(connStr);
                if(mysqlConn != null) {
                    String qTemplate = propertyFactory.getObject().getProperty(Constants.DB_ALERT_CONFIG_INSERT);

                    if(StringUtils.isNotBlank(qTemplate)){
                        Map<String, String> renderData = new HashMap<String, String>();
                        renderData.put(Constants.FROM_DATE, TimestampUtil.getDate(cal.getTime(), Constants.YYYY_MM_DD, -14));
                        renderData.put(Constants.GAME_CODE, gc);
                        String q = CommonUtil.renderMessage(qTemplate, renderData);
//                        System.out.println(q);
                        sb.append(q);
                        statement = mysqlConn.createStatement();
                        DBUtil.executeMySQL(statement, q);
                        /*resultSet = DBUtil.executeMySQL(statement, q);
                        while (resultSet.next()) {
                            String tableName = resultSet.getString(1);
                        }*/
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (resultSet != null) {
                        resultSet.close();
                    }
                    if (statement != null) {
                        statement.close();
                    }
                    if (mysqlConn != null) {
                        mysqlConn.close();
                    }
                } catch (Exception ex) {}
            }
        }

        return sb.toString();
    }
    @RequestMapping(value="/public/props/reload", method = RequestMethod.GET)
    protected String reloadProps(HttpServletRequest request, HttpServletResponse response) throws Exception {

        System.out.println(propertyFactory.getObject().getProperty("abc"));
        propertyFactory.afterPropertiesSet();

        return gson.toJson(propertyFactory.getObject());
    }
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
    public String alert(@RequestParam(value = "wfId", required = false) String wfId,
                        @RequestParam(value = "gc", required = false) String gc, HttpServletResponse response) {
        return "";
    }

    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Autowired
    private PropertiesFactoryBean propertyFactory;

//    @Autowired
//    private JmsTemplate jmsTemplate;
}

package com.vng.teg.logtool.web.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vng.teg.logtool.common.Constants;
import com.vng.teg.logtool.common.util.CommonUtil;
import com.vng.teg.logtool.common.util.DBUtil;
import com.vng.teg.logtool.common.util.EmailUtil;
import com.vng.teg.logtool.common.util.TimestampUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.support.ApplicationObjectSupport;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    @RequestMapping(value = "/public/wf/notify/alllogs", method = RequestMethod.GET, produces = {"application/json"})
    public String alertAllLogs(@RequestParam(value = "wfId", required = false) String wfId,
                        @RequestParam(value = "gc", required = false) String gc,
                        @RequestParam(value = "status", required = false) String status,
                        HttpServletResponse response) {
        String dataJob = "";
        String dataJobChild = "";
        String parentId="";
        String nominalTime = "";
        String oldFormat = "EEE,dd MMM yyyy HH:mm:ss z";
        String newFormat = "yyyy-MM-dd";
        String setTimeZone = "Asia/Ho_Chi_Minh";
        String nominalTimeFormat = "";
        try {
            /*String prefixURL = propertyFactory.getObject().getProperty(Constants.OOZIE_API_URL);
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
            System.out.println();*/

//            Map map = new HashMap();
//            map.put(Constants.GAME_CODE, gc);
//            map.put(Constants.LOG_DATE, "2016-04-25");
//            jmsTemplate.convertAndSend("alertQueue", map);

        } catch (Exception e) {
            e.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();
        sb.append("<html><head></head><body><table border=1><thead><tr><th>Log Type</th>");
        Connection mysqlConn = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            if(StringUtils.isNotBlank(gc) && "SUCCEEDED".equals(status)){
                int dayAgo = 5;
                List<String> dayList = new ArrayList<String>();
                Map<String, Map<String, Integer>> dataMap = new HashMap<String, Map<String, Integer>>();
                String logDate = "2016-04-25";
                DateFormat df = new SimpleDateFormat(Constants.YYYY_MM_DD);
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(df.parse(logDate).getTime());
                String dStr = TimestampUtil.getDate(cal.getTime(), Constants.YYYY_MM_DD, 0);
                dayList.add(dStr);
                dayList.add("avg");
                dayList.add("percent");
                Set<String> dateSet = new HashSet<String>();
                sb.append("<th>").append(dStr).append("</th><th>Latest ").append(dayAgo).append(" days Average</th><th>Percent</th> ");
                for (int i = 1; i <= dayAgo; i++) {
//                cal.add(Calendar.DATE, -1);
                    dStr = TimestampUtil.getDate(cal.getTime(), Constants.YYYY_MM_DD, -i);
                    dayList.add(dStr);
                    dateSet.add(dStr);
                    sb.append("<th>").append(dStr).append("</th>");
//                sb.append("<th>").append(TimestampUtil.getDate(cal.getTime(), Constants.YYYY_MM_DD, 0)).append("</th>");
                }
                System.out.println(String.format("%s, %s", gc, logDate));

                String connStr = propertyFactory.getObject().getProperty(Constants.DB_MYSQL_CONNECTION);
                String recipients = propertyFactory.getObject().getProperty(Constants.ALERT_RECIPIENTS);
                System.out.println(connStr);
                mysqlConn = DBUtil.createMySQLConnection(connStr);

                if(mysqlConn != null) {
                    String qTemplate;
                    Map<String, String> renderData;

                    qTemplate = propertyFactory.getObject().getProperty(Constants.DB_ALERT_CONFIG_QUERY);

                    if(StringUtils.isNotBlank(qTemplate)){
                        renderData = new HashMap<String, String>();
                        renderData.put(Constants.GAME_CODE, gc);
                        String q = CommonUtil.renderMessage(qTemplate, renderData);
                        System.out.println(q);
                        statement = mysqlConn.createStatement();
                        resultSet = DBUtil.executeMySQLQuery(statement, q);
                        while (resultSet.next()) {
                            String game = resultSet.getString(1);
                            String lType = resultSet.getString(2);
                            String alertType = resultSet.getString(3);
                            System.out.println(String.format("%s, %s, %s", game, lType, alertType));
                            if(!dataMap.containsKey(lType)){
                                dataMap.put(lType, new HashMap<String, Integer>());
                                for (String s : dayList) {
                                    dataMap.get(lType).put(s, 0);
                                }
                            }
                        }
                    }

                    qTemplate = propertyFactory.getObject().getProperty(Constants.DB_MYSQL_COUNT_QUERY);

                    if(StringUtils.isNotBlank(qTemplate)){
                        renderData = new HashMap<String, String>();
                        renderData.put(Constants.FROM_DATE, TimestampUtil.getDate(logDate, Constants.YYYY_MM_DD, - dayAgo));
                        renderData.put(Constants.TO_DATE, TimestampUtil.getDate(logDate, Constants.YYYY_MM_DD, 0));
                        renderData.put(Constants.GAME_CODE, gc);
                        String q = CommonUtil.renderMessage(qTemplate, renderData);
                        System.out.println(q);
                        statement = mysqlConn.createStatement();
                        resultSet = DBUtil.executeMySQLQuery(statement, q);
                        while (resultSet.next()) {
                            String lType = resultSet.getString(1);
                            String lDate = resultSet.getString(2);
                            Integer count = resultSet.getInt(3);
//                        System.out.println(String.format("%s, %s, %s", lType, lDate, count));
                            if(dataMap.containsKey(lType)){
                                dataMap.get(lType).put(lDate, count);
                            }
                        }
                    }
                }
                for(String lType: dataMap.keySet()){
                    Integer total = 0, avg = 0, percent = 100;
                    for(String dayStr: dayList){
                        if(dateSet.contains(dayStr)){
                            total+= dataMap.get(lType).get(dayStr);
                        }
                    }
                    avg = total/ dayAgo;
                    Integer todayCount = dataMap.get(lType).get(logDate);
                    if(avg > 0){
                        percent = ((todayCount - avg) * 100) / avg;
                        System.out.println(lType + "\t" + avg + "\t" + todayCount + "\t" + percent);
                    }
                    String color = "black";
                    if(Math.abs(percent) >= 50){
                        color = "red";
                    }else if(Math.abs(percent) >= 30){
                        color = "brown";
                    }else if(percent > 0 && todayCount > 0 && avg == 0){
                        color = "green";
                    }else if(todayCount == 0 && avg == 0){
                        color = "blue";
                    }
                    sb.append("<tr><td style=\"color:").append(color).append("\">").append(lType).append("</td>");

                    dataMap.get(lType).put("avg", avg);
                    dataMap.get(lType).put("percent", percent);
                    for(String dayStr: dayList){
                        if(dayStr.equals("percent")){
                            sb.append("<td>").append(dataMap.get(lType).get(dayStr)).append("% </td>");
                        }else{
                            sb.append("<td>").append(dataMap.get(lType).get(dayStr)).append("</td>");
                        }
                    }
                    sb.append("</tr>");
                }
                sb.append("</tbody></table></body></html>");
                if(StringUtils.isNotBlank(recipients)){
                    EmailUtil.sendEmail(propertyFactory.getObject(), Arrays.asList(recipients.split(";")), gc.toUpperCase() + " [" + logDate + "] to [", sb.toString(), null);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

//        return nominalTimeFormat;

        return sb.toString();

    }

    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Autowired
    private PropertiesFactoryBean propertyFactory;

    @Autowired
    private JmsTemplate jmsTemplate;
}

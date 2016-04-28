package com.vng.teg.logtool.web.jms;

import com.google.gson.Gson;
import com.vng.teg.logtool.common.Constants;
import com.vng.teg.logtool.common.util.CommonUtil;
import com.vng.teg.logtool.common.util.DBUtil;
import com.vng.teg.logtool.common.util.EmailUtil;
import com.vng.teg.logtool.common.util.TimestampUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Son Nguyen on 5/24/2015.
 */
@Component
@Transactional
public class JMSService {

    @JmsListener(destination = "alertQueue", concurrency = "1-3", containerFactory = "jmsListenerContainerFactory")
    public void processAlertQueue(@Payload Map map){
        System.out.println("------------------ JMS alert queue process -------- ");
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head></head><body><table border=1><thead><tr><th>Log Type</th>");
        Connection mysqlConn = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            String logDate = (String) map.get(Constants.LOG_DATE);
            String gc = (String) map.get(Constants.GAME_CODE);
            System.out.println(String.format("%s, %s", gc, logDate));
            if(StringUtils.isNotBlank(gc) && StringUtils.isNotBlank(logDate)){
                int dayAgo = 5;
                List<String> dayList = new ArrayList<String>();
                Map<String, Map<String, Integer>> dataMap = new LinkedHashMap<String, Map<String, Integer>>();
//                String logDate = "2016-04-25";
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
                String recipients = propertyFactory.getObject().getProperty(gc + "." + Constants.ALERT_RECIPIENTS);
                if(StringUtils.isBlank(recipients)){
                    recipients = propertyFactory.getObject().getProperty(Constants.ALERT_RECIPIENTS);
                }
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
//                            System.out.println(String.format("%s, %s, %s", game, lType, alertType));
                            if(!dataMap.containsKey(lType)){
                                dataMap.put(lType, new LinkedHashMap<String, Integer>());
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
                Map<String, String> redList = new LinkedHashMap<String, String>();
                Map<String, String> brownList = new LinkedHashMap<String, String>();
                Map<String, String> blueList = new LinkedHashMap<String, String>();
                Map<String, String> greenList = new LinkedHashMap<String, String>();
                Map<String, String> orderList = new LinkedHashMap<String, String>();

                for(String lType: dataMap.keySet()){
                    Integer total = 0, avg = 0, percent = 0;
                    for(String dayStr: dayList){
                        if(dateSet.contains(dayStr)){
                            total+= dataMap.get(lType).get(dayStr);
                        }
                    }
                    avg = total/ dayAgo;
                    Integer todayCount = dataMap.get(lType).get(logDate);
                    if(avg > 0){
                        percent = ((todayCount - avg) * 100) / avg;
//                        System.out.println(lType + "\t" + avg + "\t" + todayCount + "\t" + percent);
                    }
                    String color = "black";
                    if(Math.abs(percent) >= 50){
                        color = "red";
                        redList.put(lType, color);
                    }else if(Math.abs(percent) >= 30){
                        color = "brown";
                        brownList.put(lType, color);
                    }else if(percent == 0 && todayCount > 0 && avg == 0){
                        color = "green";
                        greenList.put(lType, color);
                    }else if(todayCount == 0 && avg == 0){
                        color = "blue";
                        blueList.put(lType, color);
                    }else {
                        orderList.put(lType, color);
                    }

                    dataMap.get(lType).put("avg", avg);
                    dataMap.get(lType).put("percent", percent);

                    /*for(String dayStr: dayList){
                        if(dayStr.equals("percent")){
                            sb.append("<td>").append(dataMap.get(lType).get(dayStr)).append("% </td>");
                        }else{
                            sb.append("<td>").append(dataMap.get(lType).get(dayStr)).append("</td>");
                        }
                    }*/
                    sb.append("</tr>");
                }
                Map<String, String> orderedKeyList = new LinkedHashMap<String, String>();
                orderedKeyList.putAll(redList);
                orderedKeyList.putAll(brownList);
                orderedKeyList.putAll(greenList);
                orderedKeyList.putAll(blueList);
                orderedKeyList.putAll(orderList);
                for(String lType: orderedKeyList.keySet()){
                    String color = orderedKeyList.get(lType);
                    sb.append("<tr><td style=\"color:").append(color).append("\">").append(lType).append("</td>");
                    System.out.println(lType + "\t\t" + color);
                    for(String dayStr: dataMap.get(lType).keySet()){
                        System.out.println("\t" + dayStr + "\t" + dataMap.get(lType).get(dayStr));
                        if(dayStr.equals("percent")){
                            sb.append("<td style=\"color:").append(color).append("\">").append(dataMap.get(lType).get(dayStr)).append("% </td>");
                        }else if(logDate.equals(dayStr) || dayStr.equals("avg")) {
                            sb.append("<td style=\"color:").append(color).append("\">").append(dataMap.get(lType).get(dayStr)).append("</td>");
                        }else{
                            sb.append("<td>").append(dataMap.get(lType).get(dayStr)).append("</td>");
                        }
                    }
                }
                sb.append("</tbody></table></body></html>");
                System.out.println();
                System.out.println();
                System.out.println();
                System.out.println(sb.toString());
                if(StringUtils.isNotBlank(recipients)){
                    EmailUtil.sendEmail(propertyFactory.getObject(), Arrays.asList(recipients.split(";")), gc.toUpperCase() + " [" + logDate + "]", sb.toString(), null);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("------------------ jms server -------- ");
    }

    @Autowired
    private PropertiesFactoryBean propertyFactory;

    private Gson gson = new Gson();
}

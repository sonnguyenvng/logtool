package com.vng.teg.logtool.web.schedule;

import com.vng.teg.logtool.common.Constants;
import com.vng.teg.logtool.common.util.CommonUtil;
import com.vng.teg.logtool.common.util.DBUtil;
import com.vng.teg.logtool.common.util.EmailUtil;
import com.vng.teg.logtool.common.util.TimestampUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by sonnguyen on 16/05/2016.
 */
@Component
public class ScheduledTasks {
    @Scheduled(cron="30 11 * * * ?")
    public void reportAllGamesMorning() throws Exception {
        System.out.println("----------- reportAllGamesMorning ------------" + new Date());
        String morningGames = propertyFactory.getObject().getProperty(Constants.ALERT_GAME_MORNING);
        String[] games = morningGames.split(";", -1);
//        System.out.println(CommonUtil.printPrettyObj(games));
        reportAllGames(games);

    }
    public void reportAllGames(String... gameCodes) throws Exception {
        if(gameCodes != null && gameCodes.length > 0){
            Map<String, String> renderData;
            String qTemplate;
            Connection mysqlConn = null;
            Statement statement = null;
            ResultSet resultSet = null;
            try {
                String connStr = propertyFactory.getObject().getProperty(Constants.DB_MYSQL_CONNECTION);
                mysqlConn = DBUtil.createMySQLConnection(connStr);
                if(mysqlConn != null) {
                    qTemplate = propertyFactory.getObject().getProperty(Constants.DB_ALERT_CONFIG_QUERY);

                    if(StringUtils.isNotBlank(qTemplate)){
                        String recipients = propertyFactory.getObject().getProperty(Constants.ALERT_RECIPIENTS_LOGTEAM);
                        int dayAgo = 5;
                        StringBuilder beginSb = new StringBuilder();
                        StringBuilder errorSb = new StringBuilder();
                        boolean hasError = false;
                        StringBuilder sb = new StringBuilder();
                        beginSb.append("<html><head></head><body>");
                        List<String> dayList = new ArrayList<String>();
                        Calendar cal = Calendar.getInstance();
                        cal.add(Calendar.DATE, -1);
                        String dStr = TimestampUtil.getDate(cal.getTime(), Constants.YYYY_MM_DD, 0);
                        String logDate = dStr;
                        dayList.add(dStr);
                        dayList.add("avg");
                        dayList.add("percent");
                        Set<String> dateSet = new HashSet<String>();
                        beginSb.append("Games: ").append(CommonUtil.printPrettyObj(gameCodes)).append("<br><br>");

                        sb.append("<table border=1><thead><tr><th>Game</th><th>Log Type</th>");
                        sb.append("<th>").append(dStr).append("</th><th>Latest ").append(dayAgo).append(" days Average</th><th>Percent</th> ");
                        for (int i = 1; i <= dayAgo; i++) {
                            dStr = TimestampUtil.getDate(cal.getTime(), Constants.YYYY_MM_DD, -i);
                            dayList.add(dStr);
                            dateSet.add(dStr);
                            sb.append("<th>").append(dStr).append("</th>");
                        }
                        errorSb.append(sb.toString());
                        for (String gameCode : gameCodes) {
                            boolean hasGameError = false;
                            Map<String, Map<String, Integer>> dataMap = new LinkedHashMap<String, Map<String, Integer>>();
                            qTemplate = propertyFactory.getObject().getProperty(Constants.DB_ALERT_CONFIG_QUERY);
                            renderData = new HashMap<String, String>();
                            renderData.put(Constants.GAME_CODE, gameCode);
                            String q = CommonUtil.renderMessage(qTemplate, renderData);
                            statement = mysqlConn.createStatement();
                            resultSet = DBUtil.executeMySQLQuery(statement, q);
                            while (resultSet.next()) {
                                String lType = resultSet.getString(2);
                                if(!dataMap.containsKey(lType)){
                                    dataMap.put(lType, new LinkedHashMap<String, Integer>());
                                    for (String s : dayList) {
                                        dataMap.get(lType).put(s, 0);
                                    }
                                }
                            }
                            qTemplate = propertyFactory.getObject().getProperty(Constants.DB_MYSQL_COUNT_QUERY);

                            if(StringUtils.isNotBlank(qTemplate)){
                                renderData = new HashMap<String, String>();
                                renderData.put(Constants.FROM_DATE, TimestampUtil.getDate(logDate, Constants.YYYY_MM_DD, - dayAgo));
                                renderData.put(Constants.TO_DATE, TimestampUtil.getDate(logDate, Constants.YYYY_MM_DD, 0));
                                renderData.put(Constants.GAME_CODE, gameCode);
                                q = CommonUtil.renderMessage(qTemplate, renderData);
                                statement = mysqlConn.createStatement();
                                resultSet = DBUtil.executeMySQLQuery(statement, q);
                                while (resultSet.next()) {
                                    String lType = resultSet.getString(1);
                                    String lDate = resultSet.getString(2);
                                    Integer count = resultSet.getInt(3);
                                    if(dataMap.containsKey(lType)){
                                        dataMap.get(lType).put(lDate, count);
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
                                }
                                String color = "black";
                                if(avg > 100 && Math.abs(percent) >= 75){
                                    color = "red";
                                    redList.put(lType, color);
                                    hasError = true;
                                    hasGameError = true;
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

                            }
                            Map<String, String> orderedKeyList = new LinkedHashMap<String, String>();
                            orderedKeyList.putAll(redList);
                            orderedKeyList.putAll(brownList);
                            orderedKeyList.putAll(greenList);
                            orderedKeyList.putAll(blueList);
                            orderedKeyList.putAll(orderList);
                            StringBuilder tmpSB;
                            for(String lType: orderedKeyList.keySet()){
                                String color = orderedKeyList.get(lType);
                                if("red".equals(color)){
                                    tmpSB = errorSb;
                                }else {
                                    tmpSB = sb;
                                }
                                tmpSB.append("<tr><td style=\"color:").append(color).append("\"><b>").append(gameCode).append("</b></td>");
                                tmpSB.append("<td style=\"color:").append(color).append("\">").append(lType).append("</td>");
                                for(String dayStr: dataMap.get(lType).keySet()){
                                    if(dayStr.equals("percent")){
                                        tmpSB.append("<td style=\"color:").append(color).append("\">").append(dataMap.get(lType).get(dayStr)).append("% </td>");
                                    }else if(logDate.equals(dayStr) || dayStr.equals("avg")) {
                                        tmpSB.append("<td style=\"color:").append(color).append("\">").append(CommonUtil.formatNumber(dataMap.get(lType).get(dayStr))).append("</td>");
                                    }else{
                                        tmpSB.append("<td>").append(CommonUtil.formatNumber(dataMap.get(lType).get(dayStr))).append("</td>");
                                    }
                                }
                                tmpSB.append("</tr>");
                            }
                            if(hasGameError){
                                errorSb.append("<tr><td colspan=\"").append(dayAgo + 5).append("\">&nbsp;</td></tr>");
                            }
                            sb.append("<tr><td colspan=\"").append(dayAgo + 5).append("\">&nbsp;</td></tr>");

                        }
                        sb.append("</tbody></table>");
                        if(hasError){
                            beginSb.append(errorSb.toString());
                            beginSb.append("</tbody></table>");
                            beginSb.append("<br><br><hr><br><br>");
                        }
                        beginSb.append(sb.toString());
                        beginSb.append("</body></html>");
                        if(StringUtils.isNotBlank(recipients)){
                            EmailUtil.sendEmail(propertyFactory.getObject(), Arrays.asList(recipients.split(";")), "[LOG][PIG.File] All games [" + logDate + "]", beginSb.toString(), null);
                        }
                    }
                }
            }catch (Exception ex){
                ex.printStackTrace();
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
        }

    }
    @Scheduled(fixedRate = 1500000)//30 minutes
    public void autoScan() throws Exception {
        System.out.println("----------- start ------------");
        Map<String, String> renderData;
        String qTemplate;
        Connection mysqlConn = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            String connStr = propertyFactory.getObject().getProperty(Constants.DB_MYSQL_CONNECTION);
            mysqlConn = DBUtil.createMySQLConnection(connStr);
            if(mysqlConn != null) {
                qTemplate = propertyFactory.getObject().getProperty(Constants.DB_ALERT_STATUS_QUERY);
                if(StringUtils.isNotBlank(qTemplate)){
//                    System.out.println(qTemplate);
                    statement = mysqlConn.createStatement();
                    resultSet = DBUtil.executeMySQLQuery(statement, qTemplate);
                    while (resultSet.next()) {
                        String gc = resultSet.getString(1);
                        String logDate = resultSet.getString(2);
                        String wfId = resultSet.getString(3);
                        String coordId = resultSet.getString(4);
                        System.out.println(String.format("%s, %s, %s, %s", gc, logDate, wfId, coordId));

                        StringBuilder sb = new StringBuilder();
                        sb.append("<html><head>wfId=").append(wfId).append("<br>coordId=").append(coordId).append("</head><body><table border=1><thead><tr><th>Log Type</th>");

                        if(StringUtils.isNotBlank(gc) && StringUtils.isNotBlank(logDate)){
                            int dayAgo = 5;
                            List<String> dayList = new ArrayList<String>();
                            Map<String, Map<String, Integer>> dataMap = new LinkedHashMap<String, Map<String, Integer>>();
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
                                dStr = TimestampUtil.getDate(cal.getTime(), Constants.YYYY_MM_DD, -i);
                                dayList.add(dStr);
                                dateSet.add(dStr);
                                sb.append("<th>").append(dStr).append("</th>");
                            }
                            System.out.println(String.format("%s, %s", gc, logDate));

                            String recipients = propertyFactory.getObject().getProperty(gc + "." + Constants.ALERT_RECIPIENTS);
                            if(StringUtils.isBlank(recipients)){
                                recipients = propertyFactory.getObject().getProperty(Constants.ALERT_RECIPIENTS);
                            }
                            mysqlConn = DBUtil.createMySQLConnection(connStr);

                            qTemplate = propertyFactory.getObject().getProperty(Constants.DB_ALERT_CONFIG_QUERY);

                            if(StringUtils.isNotBlank(qTemplate)){
                                renderData = new HashMap<String, String>();
                                renderData.put(Constants.GAME_CODE, gc);
                                String q = CommonUtil.renderMessage(qTemplate, renderData);
                                statement = mysqlConn.createStatement();
                                resultSet = DBUtil.executeMySQLQuery(statement, q);
                                while (resultSet.next()) {
                                    String lType = resultSet.getString(2);
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
                                statement = mysqlConn.createStatement();
                                resultSet = DBUtil.executeMySQLQuery(statement, q);
                                while (resultSet.next()) {
                                    String lType = resultSet.getString(1);
                                    String lDate = resultSet.getString(2);
                                    Integer count = resultSet.getInt(3);
                                    if(dataMap.containsKey(lType)){
                                        dataMap.get(lType).put(lDate, count);
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
//                                System.out.println(lType + "\t\t" + color);
                                for(String dayStr: dataMap.get(lType).keySet()){
//                                    System.out.println("\t" + dayStr + "\t" + dataMap.get(lType).get(dayStr));
                                    if(dayStr.equals("percent")){
                                        sb.append("<td style=\"color:").append(color).append("\">").append(dataMap.get(lType).get(dayStr)).append("% </td>");
                                    }else if(logDate.equals(dayStr) || dayStr.equals("avg")) {
                                        sb.append("<td style=\"color:").append(color).append("\">").append(CommonUtil.formatNumber(dataMap.get(lType).get(dayStr))).append("</td>");
                                    }else{
                                        sb.append("<td>").append(CommonUtil.formatNumber(dataMap.get(lType).get(dayStr))).append("</td>");
                                    }
                                }
                            }
                            sb.append("</tbody></table></body></html>");
                            if(StringUtils.isNotBlank(recipients)){
                                EmailUtil.sendEmail(propertyFactory.getObject(), Arrays.asList(recipients.split(";")), "[LOG] " + gc.toUpperCase() + " [" + logDate + "]", sb.toString(), null);
                            }
                        }

                        qTemplate = propertyFactory.getObject().getProperty(Constants.DB_ALERT_STATUS_UPDATE);
                        renderData = new HashMap<String, String>();
                        renderData.put(Constants.FROM_DATE, logDate);
                        renderData.put(Constants.GAME_CODE, gc);
                        qTemplate = CommonUtil.renderMessage(qTemplate, renderData);
                        statement = mysqlConn.createStatement();
                        DBUtil.executeMySQL(statement, qTemplate);
//                        System.out.println(qTemplate);
                    }
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
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
    }

    @Autowired
    private PropertiesFactoryBean propertyFactory;
}

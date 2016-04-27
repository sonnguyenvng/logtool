package com.vng.teg.logtool.web.jms;

import com.google.gson.Gson;
import com.vng.teg.logtool.common.Constants;
import com.vng.teg.logtool.common.util.CommonUtil;
import com.vng.teg.logtool.common.util.DBUtil;
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
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Son Nguyen on 5/24/2015.
 */
@Component
@Transactional
public class JMSService {

    @JmsListener(destination = "alertQueue", concurrency = "1-3", containerFactory = "jmsListenerContainerFactory")
    public void processAlertQueue(@Payload Map map){
        System.out.println("------------------ jms server -------- ");
        Connection mysqlConn = null;
        Statement statement = null;
        ResultSet resultSet = null;
        try {
            String logDate = (String) map.get(Constants.LOG_DATE);
            String gc = (String) map.get(Constants.GAME_CODE);
            System.out.println(String.format("%s, %s", gc, logDate));

            String connStr = propertyFactory.getObject().getProperty(Constants.DB_MYSQL_CONNECTION);
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
                    }
                }

                qTemplate = propertyFactory.getObject().getProperty(Constants.DB_MYSQL_COUNT_QUERY);

                if(StringUtils.isNotBlank(qTemplate)){
                    renderData = new HashMap<String, String>();
                    renderData.put(Constants.FROM_DATE, TimestampUtil.getDate(logDate, Constants.YYYY_MM_DD, -5));
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
                        System.out.println(String.format("%s, %s, %s", lType, lDate, count));
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

//        System.out.println("url = " + url);
        System.out.println("------------------ jms server -------- ");
    }

    @Autowired
    private PropertiesFactoryBean propertyFactory;

    private Gson gson = new Gson();
}

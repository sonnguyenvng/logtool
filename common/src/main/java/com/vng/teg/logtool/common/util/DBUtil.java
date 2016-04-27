package com.vng.teg.logtool.common.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;

/**
 * Created with IntelliJ IDEA.
 * User: sonnt4
 * Date: 12/11/13
 * Time: 12:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class DBUtil {

    private static final Log LOG = LogFactory.getLog(DBUtil.class);

    public static Connection createMySQLConnection(String host, String port, String dbName, String user, String password) throws Exception {
        String conn = String.format("jdbc:mysql://%s:%s/%s?user=%s&password=%s&useUnicode=true&characterEncoding=UTF-8", host, port, dbName, user, password);
        return createMySQLConnection(conn);
    }

    public static Connection createMySQLConnection(String conn) throws Exception {
        Connection connect = null;
        Class.forName("com.mysql.jdbc.Driver");
//        Class.forName("com.mysql.cj.jdbc.Driver");
        connect = DriverManager.getConnection(conn);
        return connect;
    }

    public static ResultSet executeMySQLQuery(Statement statement, String query){
        ResultSet resultSet = null;
        try {
            resultSet = statement.executeQuery(query);
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        return resultSet;
    }
    public static boolean executeMySQL(Statement statement, String query){
        boolean rs = false;
        try {
            rs = statement.execute(query);
        } catch (SQLException e) {
            LOG.error(e.getMessage(), e);
        }
        return rs;
    }
}

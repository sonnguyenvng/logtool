package com.vng.teg.logtool.common.util;

import com.vng.teg.logtool.common.Constants;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by sonnt4 on 2/18/14.
 */
public class EmailUtil {

    public static String sendEmail(Properties properties, List<String> toList, String subject, String body, File attachedFile) throws Exception {
//        Properties properties = new Properties();

        // Setup mail server
//        properties.setProperty("mail.smtp.host", Props.getInstance().getProperty(Constants.EMAIL_SMTP_HOST));

        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties);

        // Create a default MimeMessage object.
        MimeMessage message = new MimeMessage(session);

        // Set From: header field of the header.
        message.setFrom(new InternetAddress(properties.getProperty(Constants.EMAIL_FROM)));

        // Set To: header field of the header.
        for(String to: toList){
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        }

        // Set Subject: header field
        message.setSubject(subject);

        // Create the message part
        MimeBodyPart messageBodyPart = new MimeBodyPart();

        // Fill the message
        messageBodyPart.setText(body, "UTF-8", "html");

        // Create a multipar message
        Multipart multipart = new MimeMultipart();

        // Set text message part
        multipart.addBodyPart(messageBodyPart);

        // Part two is attachment
        if(attachedFile != null){
            messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(attachedFile);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(attachedFile.getName());
            multipart.addBodyPart(messageBodyPart);
        }

        // Send the complete message parts
        message.setContent(multipart);

        // Send message
        Transport.send(message);
        return "Sent message successfully.... to " + toList;
    }
    public static void sendEmailWithInlineImages(Properties properties, List<String> toList, String subject, String body, List<File> imageFiles) throws Exception {
//        Properties properties = new Properties();
//
//        properties.setProperty("mail.smtp.host", Props.getInstance().getProperty(Constants.EMAIL_SMTP_HOST));

        Session session = Session.getDefaultInstance(properties);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(properties.getProperty(Constants.EMAIL_FROM)));

        for(String to: toList){
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        }

        message.setSubject(subject);

        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(body, "UTF-8", "html");
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        if(imageFiles != null){
            int idx = 1;
            for(File attachedFile: imageFiles){
                messageBodyPart = new MimeBodyPart();
                messageBodyPart.setDataHandler(new DataHandler(new FileDataSource(attachedFile)));
                messageBodyPart.setHeader("Content-ID","<image" + (idx++) + ">");
                multipart.addBodyPart(messageBodyPart);
            }
        }

        // Send the complete message parts
        message.setContent(multipart);
        Transport.send(message);
        System.out.println("Sent message successfully.... to " + toList);

    }
    public static void sendEmailWithManyFiles(Properties properties, List<String> toList, String subject, String body, List<File> attachedFiles) throws Exception {
//        Properties properties = new Properties();
//
//        properties.setProperty("mail.smtp.host", Props.getInstance().getProperty(Constants.EMAIL_SMTP_HOST));

        Session session = Session.getDefaultInstance(properties);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(properties.getProperty(Constants.EMAIL_FROM)));

        for(String to: toList){
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        }

        message.setSubject(subject);

        MimeBodyPart messageBodyPart = new MimeBodyPart();
        messageBodyPart.setText(body, "UTF-8", "html");
        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(messageBodyPart);

        if(attachedFiles != null){
            for(File attachedFile: attachedFiles){
                messageBodyPart = new MimeBodyPart();
                messageBodyPart.setDataHandler(new DataHandler(new FileDataSource(attachedFile)));
                messageBodyPart.setFileName(attachedFile.getName());
                multipart.addBodyPart(messageBodyPart);
            }
        }

        // Send the complete message parts
        message.setContent(multipart);
        Transport.send(message);
        System.out.println("Sent message successfully.... to " + toList);

    }
    public static void sendTextEmail(Properties properties, List<String> toList, String subject, String body) throws Exception {
//        Properties properties = new Properties();
//        properties.setProperty("mail.smtp.host", Props.getInstance().getProperty(Constants.EMAIL_SMTP_HOST));
        Session session = Session.getDefaultInstance(properties);
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(properties.getProperty(Constants.EMAIL_FROM)));
        for(String to: toList){
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
        }
        message.setSubject(subject);
        message.setText(body, "UTF-8", "html");
        Transport.send(message);
        System.out.println("Sent message successfully.... to " + toList);
    }
    static String getTableBody(){
        return "<table border=1><thead><tr><th>Game</th><th>Date</th><th>Role Count</th><th>Record Total</th></tr></thead><tbody><tr><td>JX3</td><td>2014-03-01</td><td>888</td><td>1005790</td></tr><tr><td>JX3</td><td>2014-03-02</td><td>1076</td><td>1006866</td></tr></tbody></table>";
    }
    static String getGoogleChartBody(){
        StringBuilder sb = new StringBuilder();
        sb.append("<script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>");
        sb.append("<div id=\"chart_div\" style=\"width: 900px; height: 410px;\"></div>");
        sb.append("<script type=\"text/javascript\">");
        sb.append("var arrayOfArray = [['Date', 'User Count', 'Average']];");
        sb.append("arrayOfArray.push(['01/01/2014', 100, 5]);");
        sb.append("");
        sb.append("");
        sb.append("var data = google.visualization.arrayToDataTable(arrayOfArray);");
        sb.append("var options = {\n" +
                "                        title: 'User Count',\n" +
                "                        hAxis: {title: \"Date\"},\n" +
                "                        seriesType: \"bars\",\n" +
                "                        series: {1: {type: \"line\"}}\n" +
                "                    };");
        sb.append("var chart = new google.visualization.ComboChart(document.getElementById('chart_div'));");
        sb.append("chart.draw(data, options);");

        sb.append("");
        sb.append("</script>");
        return sb.toString();
    }
    static String getBody(){
        return "<html>\n" +
                "  <head>\n" +
                "    <!--Load the AJAX API-->\n" +
                "    <script type=\"text/javascript\" src=\"https://www.google.com/jsapi\"></script>\n" +
                "    <script type=\"text/javascript\">\n" +
                "\n" +
                "      // Load the Visualization API and the piechart package.\n" +
                "      google.load('visualization', '1.0', {'packages':['corechart']});\n" +
                "\n" +
                "      // Set a callback to run when the Google Visualization API is loaded.\n" +
                "      google.setOnLoadCallback(drawChart);\n" +
                "\n" +
                "      // Callback that creates and populates a data table,\n" +
                "      // instantiates the pie chart, passes in the data and\n" +
                "      // draws it.\n" +
                "      function drawChart() {\n" +
                "\n" +
                "        // Create the data table.\n" +
                "        var data = new google.visualization.DataTable();\n" +
                "        data.addColumn('string', 'Topping');\n" +
                "        data.addColumn('number', 'Slices');\n" +
                "        data.addRows([\n" +
                "          ['Mushrooms', 3],\n" +
                "          ['Onions', 1],\n" +
                "          ['Olives', 1],\n" +
                "          ['Zucchini', 1],\n" +
                "          ['Pepperoni', 2]\n" +
                "        ]);\n" +
                "\n" +
                "        // Set chart options\n" +
                "        var options = {'title':'How Much Pizza I Ate Last Night',\n" +
                "                       'width':400,\n" +
                "                       'height':300};\n" +
                "\n" +
                "        // Instantiate and draw our chart, passing in some options.\n" +
                "        var chart = new google.visualization.PieChart(document.getElementById('chart_div'));\n" +
                "        chart.draw(data, options);\n" +
                "      }\n" +
                "    </script>\n" +
                "  </head>\n" +
                "\n" +
                "  <body>\n" +
                "    <!--Div that will hold the pie chart-->\n" +
                "    <div id=\"chart_div\"></div>\n" +
                "  </body>\n" +
                "</html>";
    }
    public static void main(String[] args){
        try{
            List<String> to = new ArrayList<String>();
            to.add("sonnt4@vng.com.vn");
            String subject = "test subject";
//            String body = getTableBody();
//            String body = getGoogleChartBody();
            String body = getBody();
            File attachedFile = null;
//            Props.getInstance().put(Constants.EMAIL_SMTP_HOST, "localhost");
//            Props.getInstance().put(Constants.EMAIL_FROM, "vmas@vinagame.vn");
//            sendEmail(to, subject, body, attachedFile);
//            sendTextEmail(to, subject, body);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}

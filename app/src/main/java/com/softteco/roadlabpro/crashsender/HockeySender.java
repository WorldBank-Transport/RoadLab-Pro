package com.softteco.roadlabpro.crashsender;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.collector.CrashReportData;
import org.acra.sender.ReportSender;
import org.acra.sender.ReportSenderException;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HockeySender implements ReportSender {
  private static String BASE_URL = "https://rink.hockeyapp.net/api/2/apps/";
  private static String CRASHES_PATH = "/crashes";

  @Override
  public void send(CrashReportData report) throws ReportSenderException {
    String log = createCrashLog(report);
    String url = BASE_URL + ACRA.getConfig().formKey() + CRASHES_PATH;

    try {
      DefaultHttpClient httpClient = new DefaultHttpClient(); 
      HttpPost httpPost = new HttpPost(url);

      List<NameValuePair> parameters = new ArrayList<NameValuePair>(); 
      parameters.add(new BasicNameValuePair("raw", log));
      parameters.add(new BasicNameValuePair("userID", report.get(ReportField.INSTALLATION_ID)));
      parameters.add(new BasicNameValuePair("contact", report.get(ReportField.USER_EMAIL)));
      parameters.add(new BasicNameValuePair("description", report.get(ReportField.USER_COMMENT)));
      
      httpPost.setEntity(new UrlEncodedFormEntity(parameters, HTTP.UTF_8));

      httpClient.execute(httpPost);   
    }
    catch (Exception e) {
      e.printStackTrace();
    } 
  }

  private String createCrashLog(CrashReportData report) {
    Date now = new Date();
    StringBuilder log = new StringBuilder();
    
    log.append("Package: " + report.get(ReportField.PACKAGE_NAME) + "\n");
    log.append("Version: " + report.get(ReportField.APP_VERSION_CODE) + "\n");
    log.append("Android: " + report.get(ReportField.ANDROID_VERSION) + "\n");
    log.append("Manufacturer: " + android.os.Build.MANUFACTURER + "\n");
    log.append("Log: " + report.get(ReportField.LOGCAT) + "\n");
    log.append("Date: " + now + "\n");
    log.append("\n");
    log.append(report.get(ReportField.STACK_TRACE));

    return log.toString();
  }
}
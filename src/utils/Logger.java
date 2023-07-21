package utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

  public static void printMsg(String msg) {
    System.out.println(msg);
  }
  public static String getTimeStamp() {
    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
    return "[Time: " + sdf.format(new Date()) + "] ";
  }

  public static void requestLog(String req) {
    System.out.println(getTimeStamp() + "REQUEST: " + req);
  }

  public static void requestLog(String timestamp, String req) {
    System.out.println(timestamp + "REQUEST: " + req);
  }

  public static void responseLog(String res) {
    System.out.println(getTimeStamp() + "RESPONSE: " + res);
  }

  public static void errorLog(String err) {
    System.out.println("====================================================================================");
    System.out.println(getTimeStamp() + "ERROR: " + err);
    System.out.println("====================================================================================");
  }
}

package server;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import utils.Logger;

import static utils.Logger.printMsg;
import static utils.Logger.requestLog;
import static utils.Logger.responseLog;

public class MapMethodsImpl implements MapMethods {
  static final ReadWriteLock rwlock = new ReentrantReadWriteLock();
  static final Properties prop = new Properties();
  static OutputStream writer;
  static InputStream reader;
  private final Scanner sc = new Scanner(System.in);
  private Coordinator coordinator;

  public MapMethodsImpl() throws IOException {
    reader = new FileInputStream("map.properties");
    prop.load(reader);
    writer = new FileOutputStream("map.properties", false);
    prop.store(writer, null);
  }

  @Override
  public void setCoordinator(Coordinator coordinator) throws RemoteException {
    this.coordinator = coordinator;
  }

  @Override
  public String handleRequest(String operation, String key, String value) throws IOException, InterruptedException {
    boolean shouldProceed;
    String res = null;
    operation = operation.toUpperCase();
    switch (operation) {
      case "GET":
        getFromMap(key);
        break;

      case "PUT":
        shouldProceed = coordinator.broadcastPrepare();
        if (shouldProceed) {
          coordinator.broadcastPut(key, value);
          res = "Successfully inserted key value pair in map";
        } else {
          throw new RuntimeException("Request aborted. 1 or more participants failed to prepare/commit.");
        }
        break;

      case "DELETE":
        shouldProceed = coordinator.broadcastPrepare();
        if (shouldProceed) {
          coordinator.broadcastDelete(key);
          res = "Successfully deleted key value pair from map";
        } else {
          throw new RuntimeException("Request aborted. 1 or more participants failed to prepare/commit.");
        }
        break;

      case "SAVE":
        printMsg("Saving data to file");
        this.saveFile();
        res = "Client disconnected...";
        printMsg(res);
        break;

      default:
        Logger.errorLog("Invalid request");
        throw new IllegalArgumentException("Invalid request. Must be GET, PUT, DELETE or STOP only.");
    }

    return res;
  }

  private void saveFile() throws IOException {
    prop.store(writer, null);
  }

  @Override
  public boolean askPrepare() throws RemoteException {
    System.out.print("Are you prepared for new transaction? (y/n) : ");
    return sc.nextLine().trim().equalsIgnoreCase("y");
  }

  @Override
  public boolean askCommit() throws RemoteException {
    System.out.print("Are you ready to commit? (y/n) : ");
    return sc.nextLine().trim().equalsIgnoreCase("y");
  }

  @Override
  public String getFromMap(String key) {
    rwlock.readLock().lock();
    requestLog("GET " + key);
    String value = prop.getProperty(key);
    rwlock.readLock().unlock();

    String res = (value == null  || value.equals("~null~")?
            "No value found for key \"" + key + "\"" : "Key: \"" + key + "\" ,Value: \"" + value + "\"");
    responseLog(res);
    return res;
  }

  @Override
  public String put(String key, String value) throws RemoteException {
    rwlock.writeLock().lock();
    requestLog("PUT " + key + " | " + value);
    prop.setProperty(key, value);
    rwlock.writeLock().unlock();

    String res = "Inserted key \"" + key + "\" with value \"" + value + "\"";
    responseLog(res);
    return res;
  }

  @Override
  public String delete(String key) throws RemoteException {
    rwlock.writeLock().lock();
    requestLog("DELETE " + key);
    String res = "";

    if(prop.containsKey(key)) {
      put(key, "~null~");
      prop.remove(key);
      res = "Deleted key \"" + key + "\"";
    }
    else {
      res = "Key not found.";
    }
    responseLog(res);
    rwlock.writeLock().unlock();
    return res;
  }

  @Override
  public void test() throws RemoteException {
    System.out.println("hello");
  }
}

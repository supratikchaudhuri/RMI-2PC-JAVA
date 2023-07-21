package server;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.RemoteException;
import java.util.Properties;
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
  private Coordinator coordinator;
  private final String host;
  private final int port;
  private State state;

  enum State {IDLE, BUSY}

  ;

  public MapMethodsImpl(String host, int port) throws IOException {
    this.state = State.IDLE;
    this.host = host;
    this.port = port;
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
  public String handleRequest(String operation, String key, String value)
          throws IOException, InterruptedException {
    boolean ready;
    String res = null;
    operation = operation.toUpperCase();
    switch (operation) {
      case "GET":
        get(key);
        break;

      case "PUT":
        ready = coordinator.broadcastPrepare();
        if (ready) {
          coordinator.broadcastPut(key, value);
          res = "Successfully inserted key value pair in map";
        } else {
          throw new RuntimeException("Request aborted. 1 or more participants failed to prepare/commit.");
        }
        break;

      case "DELETE":
        ready = coordinator.broadcastPrepare();
        if (ready) {
          coordinator.broadcastDelete(key);
          res = "Successfully deleted key value pair from map (if existed)";
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
    if (this.state == State.BUSY)
      return false;
    this.state = State.BUSY;
    return true;
  }

  @Override
  public boolean askCommit() throws RemoteException {
    if (this.state != State.BUSY)
      return false;
    this.state = State.IDLE;
    return true;
  }

  @Override
  public String get(String key) {
    rwlock.readLock().lock();
    requestLog("GET " + key);
    String value = prop.getProperty(key);
    rwlock.readLock().unlock();

    String res = (value == null || value.equals("~null~") ?
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

    if (prop.containsKey(key)) {
      put(key, "~null~");
      prop.remove(key);
      res = "Deleted key \"" + key + "\"";
    } else {
      res = "Key not found.";
    }
    responseLog(res);
    rwlock.writeLock().unlock();
    return res;
  }

  @Override
  public String getName() throws RemoteException {
    return host + ":" + port;
  }

}

package server;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface MapMethods extends Remote {
  void setCoordinator(Coordinator coordinator) throws RemoteException;

  String handleRequest(String command, String key, String value) throws IOException, InterruptedException;

  boolean askPrepare() throws RemoteException;

  boolean askCommit() throws RemoteException;

  String getFromMap(String key) throws RemoteException;

  String put(String key, String value) throws RemoteException;

  String delete(String key) throws RemoteException;

  void test() throws RemoteException;
}

package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Coordinator extends Remote {

  void addParticipant(MapMethods participant) throws RemoteException;

  boolean broadcastPrepare() throws RemoteException, InterruptedException;

  boolean broadcastCommit() throws RemoteException, InterruptedException;

  void broadcastPut(String key, String value) throws RemoteException, InterruptedException;

  void broadcastDelete(String key) throws RemoteException, InterruptedException;
}

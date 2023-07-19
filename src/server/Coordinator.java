package server;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Coordinator class to manage the flow of messages during the Two Phase Protocol
 */
public interface Coordinator extends Remote {

  /**
   * Adds participant for communication
   *
   * @param participant participating class
   * @throws RemoteException exception
   */
  void addParticipant(MapMethods participant) throws RemoteException;

  /**
   * Broadcasts a prepare message to all participants.
   *
   * @return ready or not response
   * @throws RemoteException      exception
   * @throws InterruptedException exception
   */
  boolean broadcastPrepare() throws RemoteException, InterruptedException;

  /**
   * Broadcasts a commit message to all participants.
   *
   * @return ready to commit ot not response
   * @throws RemoteException      exception
   * @throws InterruptedException exception
   */
  boolean broadcastCommit() throws RemoteException, InterruptedException;

  /**
   * Broadcasts a put message to all participants.
   *
   * @param key   key to be inserted
   * @param value value associated to the key
   * @throws RemoteException      exception
   * @throws InterruptedException exception
   */
  void broadcastPut(String key, String value) throws RemoteException, InterruptedException;

  /**
   * Broadcasts a delete message to all participants.
   *
   * @param key key to be deleted
   * @throws RemoteException      exception
   * @throws InterruptedException exception
   */
  void broadcastDelete(String key) throws RemoteException, InterruptedException;
}

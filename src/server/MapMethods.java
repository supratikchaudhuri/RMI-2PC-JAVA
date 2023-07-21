package server;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Participant class that defines rmi methods and interacts with the coordinator.
 */
public interface MapMethods extends Remote {
  /**
   * Assigns a coordinator to the participant.
   *
   * @param coordinator coordinator of 2PC protocol
   * @throws RemoteException exception
   */
  void setCoordinator(Coordinator coordinator) throws RemoteException;

  /**
   * Sends a request to the coordinator to prepare other participants
   *
   * @param command type of operation
   * @param key     key to be inserted
   * @param value   value to be inserted
   * @return ready or not
   * @throws IOException          exception
   * @throws InterruptedException exception
   */
  String handleRequest(String command, String key, String value) throws IOException, InterruptedException;

  /**
   * Asks the coordinator is all participants are prepared for transaction or not
   *
   * @return prepared or not result
   * @throws RemoteException exception
   */
  boolean askPrepare() throws RemoteException;

  /**
   * Asks the coordinator is all participants can commit transaction or not
   *
   * @return can commit or not
   * @throws RemoteException exception
   */
  boolean askCommit() throws RemoteException;

  /**
   * Get item from map
   *
   * @param key key to find from map
   * @return value of the key
   * @throws RemoteException exception
   */
  String get(String key) throws RemoteException;

  /**
   * Puts an item in the map
   *
   * @param key   key to put
   * @param value value to put
   * @return response of put operation
   * @throws RemoteException exception
   */
  String put(String key, String value) throws RemoteException, InterruptedException;

  /**
   * Deletes item from map
   *
   * @param key to be deleted
   * @return delete response
   * @throws RemoteException exception
   */
  String delete(String key) throws RemoteException, InterruptedException;

  /**
   * Returns name of the server
   * @return name
   * @throws RemoteException exception
   */
  String getName() throws RemoteException;

}

package server;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import utils.Logger;

import static utils.Logger.errorLog;

public class CoordinatorImpl implements Coordinator{
  private final List<MapMethods> participants;

  public CoordinatorImpl(String host, int port) throws RemoteException {
    participants = new ArrayList<>();
    System.setProperty("java.rmi.server.hostname", host);
    Coordinator thisObj = (Coordinator) UnicastRemoteObject.exportObject(this, port);
    Registry registry = LocateRegistry.createRegistry(port);
    registry.rebind("Coordinator", thisObj);

    Logger.printMsg("Coordinator started...");
  }

  @Override
  public void addParticipant(MapMethods participant) throws RemoteException {
    participants.add(participant);
    Logger.printMsg("New participant added");
  }

  @Override
  public boolean broadcastPrepare() throws RemoteException, InterruptedException {
    boolean shouldProceed = this.broadcast("prepare");
    if (!shouldProceed) {
      Logger.errorLog("Request aborted. 1 or more participants failed to prepare.");
      return false;
    }
    return this.broadcastCommit();
  }

  @Override
  public boolean broadcastCommit() throws RemoteException, InterruptedException {
    boolean shouldProceed = this.broadcast("commit");
    if (!shouldProceed) {
      Logger.errorLog("Aborting request. Please try again...");
      return false;
    }
    return true;
  }

  private boolean broadcast(String txn) throws InterruptedException {
    List<Thread> threads = new ArrayList<>();
    List<Boolean> results = new ArrayList<>(); // change this, dont need this

    for (MapMethods participant : participants) {
      Thread thread = new Thread(() -> {
        try {
          switch (txn) {
            case "prepare":
              results.add(participant.askPrepare());
              break;

            case "commit":
              results.add(participant.askCommit());
              break;
          }
        } catch (RemoteException e) {
          throw new RuntimeException(e);
        }
      });
      threads.add(thread);
      thread.start();
    }

    for (Thread thread : threads) {
      thread.join();
    }

    boolean shouldProceed = true;
    for (boolean result : results) {
      if (!result) {
        shouldProceed = false;
        break;
      }
    }
    return shouldProceed;
  }

  @Override
  public void broadcastPut(String key, String value) throws RemoteException, InterruptedException {
    List<Thread> participantThreads = new ArrayList<>();

    for (MapMethods participant : participants) {
      Thread thread = new Thread(() -> {
        try {
          participant.put(key, value);
        } catch (RemoteException e) {
          throw new RuntimeException(e);
        }
      });
      participantThreads.add(thread);
      thread.start();
    }

    for (Thread thread : participantThreads) {
      thread.join();
    }
  }

  @Override
  public void broadcastDelete(String key) throws RemoteException, InterruptedException {
    List<Thread> threads = new ArrayList<>();

    for (MapMethods participant : participants) {
      Thread thread = new Thread(() -> {
        try {
          participant.delete(key);
        } catch (RemoteException e) {
          throw new RuntimeException(e);
        }
      });
      threads.add(thread);
      thread.start();
    }

    for (Thread thread : threads) {
      thread.join();
    }
  }

  public static void main(String[] args) {
    try {
      if (args.length != 2) {
        throw new IllegalArgumentException("Please Enter a valid host and port number.");
      }

      String host = args[0];
      int port = Integer.parseInt(args[1]);

      Coordinator coordinator = new CoordinatorImpl(host, port);
    } catch (IllegalArgumentException | RemoteException e) {
      errorLog(e.getMessage());
    }
  }
}

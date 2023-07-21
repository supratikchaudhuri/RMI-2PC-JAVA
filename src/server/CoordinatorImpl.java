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

  public static void main(String[] args) {
    try {
      if (args.length != 2) {
        throw new IllegalArgumentException("Please Enter a valid host and port number.");
      }
      System.setProperty("sun.rmi.transport.tcp.responseTimeout", "30000");
      System.setProperty("sun.rmi.transport.tcp.connectionTimeout", "30000");

      String host = args[0];
      int port = Integer.parseInt(args[1]);

      Coordinator coordinator = new CoordinatorImpl(host, port);
    } catch (IllegalArgumentException | RemoteException e) {
      errorLog(e.getMessage());
    }
  }

  @Override
  public void addParticipant(MapMethods participant) throws RemoteException {
    participants.add(participant);
    Logger.printMsg("New participant added " + participant.getName());
  }

  @Override
  public boolean broadcastPrepare() throws RemoteException, InterruptedException {
      boolean ready = this.broadcast("prepare");
      if (!ready) {
        Logger.errorLog("Request aborted. 1 or more participants failed to prepare.");
        return false;
      }
      return this.broadcastCommit();
  }

  @Override
  public boolean broadcastCommit() throws RemoteException, InterruptedException {
      boolean ready = this.broadcast("commit");
      if (!ready) {
        Logger.errorLog("Aborting request. Please try again...");
        return false;
      }
      return true;
  }

  private boolean broadcast(String txn) throws InterruptedException {
    List<Thread> threads = new ArrayList<>();
    List<Boolean> results = new ArrayList<>();

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
          results.add(false);
        }
      });
      threads.add(thread);
      thread.start();
    }

    for (Thread thread : threads) {
      thread.join();
    }

    boolean ready = true;
    for (boolean result : results) {
      if (!result) {
        ready = false;
        break;
      }
    }
    return ready;
  }

  @Override
  public void broadcastPut(String key, String value) throws RemoteException, InterruptedException {
    List<Thread> participantThreads = new ArrayList<>();

    for (MapMethods participant : participants) {
      Thread thread = new Thread(() -> {
        try {
          participant.put(key, value);
        } catch (RemoteException | InterruptedException e) {
          errorLog(e.getMessage());
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
        } catch (RemoteException | InterruptedException e) {
          errorLog(e.getMessage());
        }
      });
      threads.add(thread);
      thread.start();
    }

    for (Thread thread : threads) {
      thread.join();
    }
  }
}

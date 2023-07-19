package server;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import utils.Logger;

public class Participant {
  Coordinator coordinator;
  public Participant(String host, int port, String coordinatorHost, int coordinatorPort) throws IOException, URISyntaxException, NotBoundException {
    Logger.printMsg("Starting server...");

    System.setProperty("java.rmi.server.hostname", host);
    MapMethods obj = new MapMethodsImpl();
    MapMethods mm = (MapMethods) UnicastRemoteObject.exportObject(obj, port);

    Registry registry = LocateRegistry.createRegistry(port);
    registry.rebind("MapMethods", mm);

    // get the Coordinator from the registry and add MapMethods Object to its participant list
    System.setProperty("java.rmi.server.hostname", coordinatorHost);
    Registry coordinatorRegistry = LocateRegistry.getRegistry(coordinatorPort);
    coordinator = (Coordinator) coordinatorRegistry.lookup("Coordinator");
    coordinator.addParticipant(mm);
    mm.setCoordinator(coordinator);

    Logger.printMsg("Server participant added to coordinator...");
  }

  public static void main(String[] args) {
    try {
      if (args.length != 4) {
        throw new IllegalArgumentException("Exactly 4 arguments required, \"server_ip server_port coordinator_id coordinator_port\"");
      }

      String host = args[0];
      int port = Integer.parseInt(args[1]);
      String coordinatorHost = args[2];
      int coordinatorPort = Integer.parseInt(args[3]);

      Participant participant = new Participant(host, port, coordinatorHost, coordinatorPort);
      Logger.printMsg("Participant ready at port " + port + " \n\n");
    } catch (URISyntaxException | IOException | IllegalArgumentException e) {
      Logger.errorLog(e.getMessage());
    } catch (NotBoundException e) {
      throw new RuntimeException(e);
    }
  }
}

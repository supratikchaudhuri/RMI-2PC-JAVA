package server;

import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import utils.Logger;

/**
 * Server Class that interacts with coordinator and servers requests to clients.
 */
public class Participant {
  Coordinator coordinator;

  /**
   * Constructor for participating class.
   * It is responsible to communicate with the coordinator in order to follow the 2PC protocol
   * It creates a participant and adds itself to the participant list of the coordinator.
   *
   * @param host            server's host
   * @param port            server's port
   * @param coordinatorHost coordinator's host
   * @param coordinatorPort coordinator's port
   * @throws IOException        exception
   * @throws URISyntaxException exception
   * @throws NotBoundException  exception
   */
  public Participant(String host, int port, String coordinatorHost, int coordinatorPort)
          throws IOException, URISyntaxException, NotBoundException {

    Logger.printMsg("Starting server...");

    System.setProperty("java.rmi.server.hostname", host);
    MapMethods obj = new MapMethodsImpl(host, port);
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

  /**
   * Entry point for the class.
   *
   * @param args accepts constructor arguments from command line
   */
  public static void main(String[] args) {
    try {
      if (args.length != 4) {
        throw new IllegalArgumentException("Exactly 4 arguments required, \"<server_ip> <server_port> <coordinator_ip> <coordinator_port>\"");
      }
      System.setProperty("sun.rmi.transport.tcp.responseTimeout", "30000");
      System.setProperty("sun.rmi.transport.tcp.connectionTimeout", "30000");

      String host = args[0];
      int port = Integer.parseInt(args[1]);
      String coordinatorHost = args[2];
      int coordinatorPort = Integer.parseInt(args[3]);

      Participant participant = new Participant(host, port, coordinatorHost, coordinatorPort);
      Logger.printMsg("Participant ready at port " + port + " \n\n");
    } catch (URISyntaxException | IOException | IllegalArgumentException | NotBoundException e) {
      Logger.errorLog(e.getMessage());
    }
  }
}

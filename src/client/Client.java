package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;

import server.MapMethods;
import utils.Logger;

import static utils.Logger.getTimeStamp;
import static utils.Logger.requestLog;
import static utils.Logger.responseLog;

/**
 * Client class that is used to interact with a server and invoke remote methods
 */
public class Client {
  static final BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
  static List<MapMethods> servers = new ArrayList<>();
  static MapMethods currentServer;


  /**
   * Driver method of this class
   * @param args accepts command line arguments
   */
  public static void main(String[] args) {
    try {
      if (args.length == 0 || args.length % 2 != 0 || Integer.parseInt(args[1]) > 65535) {
        throw new IllegalArgumentException("Invalid arguments. " +
                "Please provide valid IPs and PORT (0-65535) numbers and start again.");
      }

      System.setProperty("sun.rmi.transport.tcp.responseTimeout", "30000");
      System.setProperty("sun.rmi.transport.tcp.connectionTimeout", "30000");

      Logger.printMsg(getTimeStamp() + " establishing communication with servers...");
      for(int i = 0; i < args.length; i += 2) {
        String host = args[i];
        int port = Integer.parseInt(args[i+1]);
        addServer(host, port);
      }

      // populates key value stores with default values, and sets default servers as "server 0"
      // put inside try catch block
//      addDefaultKeyValuePairs();

      Logger.printMsg(getTimeStamp() + " Successfully connected to all servers.");

      boolean exit = false;
      while (!exit) {
        try {
          exit = getOperationUI(exit);
        } catch (IndexOutOfBoundsException e) {
          Logger.errorLog("Server does not exits. Try choosing a different server...");
        } catch (RuntimeException e) {
          e.printStackTrace();
        }
      }
    } catch (IOException | InterruptedException | NotBoundException e) {
      Logger.errorLog(e.getMessage());
      Logger.printMsg("Please try again...");
    }
  }

  private static void addServer(String host, int port) throws RemoteException, NotBoundException {
    Registry registry = LocateRegistry.getRegistry(host, port);
    MapMethods mm = (MapMethods) registry.lookup("MapMethods");
    servers.add(mm);
  }

  private static boolean getOperationUI(boolean exit) throws IOException, InterruptedException {
    String requestTime = "";
    String request = "";
    String response = "";

    printMenu();
    String op = br.readLine().trim();

    switch (op) {
      case "1": {
        String key = getKey();
        request = "Get " + key;
        requestTime = getTimeStamp();
        response = currentServer.getFromMap(key);

        break;
      }
      case "2": {
        String key = getKey();
        String value = getValue();
        request = "PUT " + key + " | " + value;
        requestTime = getTimeStamp();
        response = currentServer.handleRequest("put", key, value);

        break;
      }
      case "3": {
        String key = getKey();
        request = "Delete " + key;
        requestTime = getTimeStamp();
        response = currentServer.handleRequest("delete", key, null);

        break;
      }
      case "4": {
        currentServer = servers.get(getServerId());
        break;
      }
      case "5": {
        currentServer.handleRequest("save", null, null);
        exit = true;
        break;
      }
      default:
        Logger.printMsg("Please choose a valid operation.");
        break;
    }

    requestLog(requestTime, request);
    responseLog(response);
    return exit;
  }

  private static void printMenu() {
    System.out.print("Operation List: \n1. Get\n2. Put\n3. Delete\n" +
            "4. Change server\n5. Save and Exit\n" +
            "Choose operation: ");
  }

  private static String getKey() throws IOException {
    System.out.print("Enter key: ");
    return br.readLine().trim();
  }

  private static String getValue() throws IOException {
    System.out.print("Enter Value: ");
    return br.readLine().trim();
  }

  private static int getServerId() throws IOException {
    Logger.printMsg("Choose one of the available servers ids...");
    for(int i = 0; i < servers.size(); i++) {
      Logger.printMsg("Server id: " + (i+1));
    }

    System.out.print("Enter server id: ");
    int id = Integer.parseInt(br.readLine().trim());
    System.out.println("Choosing: " + (id-1));
    return id-1;
  }

  private static void addDefaultKeyValuePairs() throws IOException, InterruptedException {
    for(int i = servers.size()-1; i >= 0; i--) {
      currentServer = servers.get(i);
      currentServer.handleRequest("put", "hello", "world");
      currentServer.handleRequest("put", "MS", "Computer Science");
      currentServer.handleRequest("put", "CS6650", "Building Scalable Distributed System");
      currentServer.handleRequest("put", "Firstname Lastname", "John Doe");
      currentServer.handleRequest("put", "BTC", "Bitcoin");
    }
  }
}


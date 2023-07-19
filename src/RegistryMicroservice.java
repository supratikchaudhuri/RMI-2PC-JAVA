import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import server.MapMethods;

public class RegistryMicroservice {
  Properties prop;
  static OutputStream writer;
  static InputStream reader;
  String FILE_NAME = "servers.properties";
  List<MapMethods> servers;


  public RegistryMicroservice() throws IOException {
    prop = new Properties();
    servers = new ArrayList<>();
  }

  public void addServer(String host, int port) throws IOException {
    writer = new FileOutputStream(FILE_NAME, false);
    prop.setProperty(host, String.valueOf(port));
    prop.store(writer, null);
  }

  public List<MapMethods> getServers() throws IOException, NotBoundException {
    reader = new FileInputStream(FILE_NAME);
    prop.load(reader);

    for (Object key: prop.keySet()) {
      System.out.println(key + ": " + prop.getProperty(key.toString()));
      String host = key.toString();
      int port = Integer.parseInt(prop.getProperty(key.toString()));

//      Registry registry = LocateRegistry.getRegistry(host, port);
//      MapMethods mm = (MapMethods) registry.lookup("MapMethods");
//      servers.add(mm);
    }

    return servers;
  }

  public static void main(String[] args) throws IOException, NotBoundException {
    RegistryMicroservice rm = new RegistryMicroservice();
    rm.addServer("localhost", 5003);
    rm.addServer("localhost2", 5004);
    rm.addServer("localhost3", 5005);
    rm.addServer("localhost4", 5006);

    List<MapMethods> A = rm.getServers();
  }
}

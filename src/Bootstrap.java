import java.net.*;
import java.rmi.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.rmi.server.*;

public class Bootstrap extends UnicastRemoteObject implements BootstrapInterface {
    
    private HashMap<String, InetAddress> onlineNodes = new HashMap<>();
    
    public Bootstrap() throws RemoteException {}
    
    public static void main(String []args) {
        try {
            Registry registry = LocateRegistry.createRegistry(4000);
            Bootstrap remote = new Bootstrap();
            System.out.println("Bootstrap is ready");
            InetAddress ipaddr = InetAddress.getLocalHost();
            System.out.println("BOOTSTRAP IP: " + ipaddr.getHostAddress());
            System.out.println("BOOTSTRAP NAME: " + ipaddr.getHostName());
            System.out.println();
            registry.rebind("Bootstrap", remote);
            
        } catch(Exception e) {
            System.out.println("Exception: " + e);
        }
    }

    @Override
    public void addNewNode(String identifier, InetAddress ip) throws RemoteException {
        onlineNodes.put(identifier, ip);
    }
    
    @Override
    public void removeNode(String identifier, InetAddress ip) throws RemoteException {
        onlineNodes.remove(identifier);
    }
    
    @Override
    public ArrayList<InetAddress> getIPList() throws RemoteException {
        System.out.println("VIEW COMMAND RECEIVED");
        return new ArrayList<>(onlineNodes.values());
    }
    
    @Override
    public InetAddress getIPFromID(String peerIdentifier) throws RemoteException {
        System.out.println("VIEW COMMAND RECEIVED");
        for(Map.Entry<String, InetAddress> entry : onlineNodes.entrySet()) {
            if(entry.getKey().equalsIgnoreCase(peerIdentifier)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public InetAddress insert(String peerIdentifier) throws RemoteException {
        System.out.println("INSERT COMMAND RECEIVED");
        for(Map.Entry<String, InetAddress> entry : onlineNodes.entrySet()) {
            if(entry.getKey().equalsIgnoreCase(peerIdentifier)) {
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    public InetAddress search(String peerIdentifier) throws RemoteException {
        System.out.println("SEARCH COMMAND RECEIVED");
        for(Map.Entry<String, InetAddress> entry : onlineNodes.entrySet()) {
            if(entry.getKey().equalsIgnoreCase(peerIdentifier)) {
                return entry.getValue();
            }
        }
        return null;
    }
    
    @Override
    public boolean isInList(String peerIdentifier) throws RemoteException {
        return onlineNodes.get(peerIdentifier) != null;
    }
}

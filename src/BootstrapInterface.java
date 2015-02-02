import java.net.*;
import java.rmi.*;
import java.util.*;

public interface BootstrapInterface extends Remote {
    
    void addNewNode(String identifier, InetAddress ip) throws RemoteException;
    
    void removeNode(String identifier, InetAddress ip) throws RemoteException;
    
    InetAddress insert(String peerIdentifier) throws RemoteException;
    
    InetAddress search(String peerIdentifier) throws RemoteException;

    ArrayList<InetAddress> getIPList() throws RemoteException;
    
    InetAddress getIPFromID(String peerIdentifier) throws RemoteException;
    
    boolean isInList(String peerIdentifier) throws RemoteException;
}

import java.net.*;
import java.rmi.*;
import java.util.*;
import java.awt.geom.Point2D;

public interface NodeInterface extends Remote {
    
    String view() throws RemoteException;
    
    Peer join(String identifier, Point2D.Float point, InetAddress newNodeIP) throws RemoteException;
    
    ArrayList insertKeyword(String file, String keyword) throws RemoteException;
    
    ArrayList searchKeyword(String keyword) throws RemoteException;
    
    void addNeighbor(Zone zone, InetAddress ip) throws RemoteException;
    
    void removeNeighbor(InetAddress ip) throws RemoteException;
    
    void mergeNode(Peer removePeer) throws RemoteException;
    
    void tempMergeNode(Peer peer) throws RemoteException;
    
    void checkNeighborIsDelete() throws RemoteException;
    
    Peer canPeerMergeDelete(Zone askPeerZone) throws RemoteException;
    
    void removeDuplicate(Zone zone, InetAddress ip) throws RemoteException;
}

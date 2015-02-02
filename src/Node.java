import java.io.*;
import java.net.*;
import java.rmi.*;
import java.util.*;
import java.rmi.server.*;
import java.awt.geom.Point2D;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class Node extends UnicastRemoteObject implements NodeInterface {
    static boolean leave = false;
    static String addBootstrapURL;
    static BootstrapInterface bsi;
    static Peer nodePeer = null;
    static ArrayList<Peer> neighbors = null;
    
    public Node() throws RemoteException {}
    
    public static void main(String []args) {
        try {           
            int input;
            ArrayList result;
            String NodeURL;
            NodeInterface ni;
            InetAddress startingIP;
            ArrayList<InetAddress> CANList;
            String identifier, keyword, file;
            Registry myReg = LocateRegistry.getRegistry(args[0], 4000);
            bsi = (BootstrapInterface) myReg.lookup("Bootstrap");

            System.out.println("Please choose a command:");
            System.out.println("1. insert");
            System.out.println("2. search");
            System.out.println("3. view");
            System.out.println("4. join");
            System.out.println("5. leave");
            System.out.print("Input: ");
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            while((input = Integer.parseInt(br.readLine())) != -1) {
                switch(input) {
                    case 1:
                        System.out.print("Input peer identifier: ");
                        identifier = br.readLine();
                        System.out.print("Input keyword: ");
                        keyword = br.readLine();
                        System.out.print("Input file: ");
                        file = br.readLine();
                        startingIP = bsi.insert(identifier);
                        if(startingIP == null) {
                            System.out.println("Failure: Cannot insert a file with the given identifier.");
                            break;
                        }
                        
                        Registry myNode = LocateRegistry.getRegistry(startingIP.getHostAddress(), 2000);
                        ni = (NodeInterface) myNode.lookup("Node");
                        result = ni.insertKeyword(file, keyword);
                        
                        if(result == null) {
                            System.out.println("Failure: Cannot insert a file with the given keyword.");
                            break;
                        }
                        System.out.println("Peer stores the file: ");
                        System.out.println(((Peer) result.get(0)).view());
                        System.out.println("Route at the IP layer from peer to the destination peer: ");
                        for(int i = ((ArrayList)result.get(1)).size() - 1; i >= 0; i--) {
                            if(i == 0) {
                                System.out.println(((ArrayList<InetAddress>)result.get(1)).get(i));
                            }
                            else {
                                System.out.print(((ArrayList<InetAddress>)result.get(1)).get(i) + "-->");
                            }
                        }
                        break;
                    case 2:
                        System.out.print("Input peer identifier: ");
                        identifier = br.readLine();
                        System.out.print("Input keyword: ");
                        keyword = br.readLine();
                        
                        startingIP = bsi.search(identifier);
                        if(startingIP == null) {
                            System.out.println("Failure: Cannot insert a file with the given identifier.");
                            break;
                        }
                        
                        myNode = LocateRegistry.getRegistry(startingIP.getHostAddress(), 2000);
                        ni = (NodeInterface) myNode.lookup("Node");
                        result = ni.searchKeyword(keyword);
                        
                        if(result == null) {
                            System.out.println("Failure: Cannot find a file with the given keyword.");
                            break;
                        }
                        System.out.println("Peer stores the file: ");
                        ((Peer) result.get(0)).view();
                        System.out.println("Route at the IP layer from peer to the destination peer: ");
                        for(int i = ((ArrayList)result.get(1)).size() - 1; i >= 0; i--) {
                            if(i == 0) {
                                System.out.println(((ArrayList<InetAddress>)result.get(1)).get(i));
                            }
                            else {
                                System.out.print(((ArrayList<InetAddress>)result.get(1)).get(i) + "-->");
                            }
                        }
                        break;
                    case 3:
                        System.out.print("Input peer identifier: ");
                        identifier = br.readLine();
                        if(identifier.equals("")) {
                            for(InetAddress ip : bsi.getIPList()) {
                                myNode = LocateRegistry.getRegistry(ip.getHostAddress(), 2000);
                                ni = (NodeInterface) myNode.lookup("Node");
                                System.out.println(ni.view());
                            }
                        }
                        else {
                            InetAddress ip = bsi.getIPFromID(identifier);
                            if(bsi.getIPFromID(identifier) == null) {
                                System.out.println("Failure: Peer identifier is not correct.");
                            }
                            else {
                                myNode = LocateRegistry.getRegistry(ip.getHostAddress(), 2000);
                                ni = (NodeInterface) myNode.lookup("Node");
                                System.out.println(ni.view());
                            }
                        }
                        break;
                    case 4:
                        if(nodePeer != null) {
                            System.out.println("Failure: Peer has already joined the CAN network.");
                            break;
                        }
                        System.out.print("Peer identifier: ");
                        identifier = br.readLine();
                        // first node to join
                        CANList = bsi.getIPList();
                        if(CANList.isEmpty()) {
                            nodePeer = new Peer(identifier, InetAddress.getLocalHost());
                            Point2D.Float startCoordinate = new Point2D.Float(0.0f, 0.0f);
                            Point2D.Float endCoordinate = new Point2D.Float(10.0f, 10.0f);
                            nodePeer.setZone(new Zone(startCoordinate, endCoordinate));
                            nodePeer.setSquare(true);
                            bsi.addNewNode(nodePeer.getIdentifier(), nodePeer.getIP());
                        }
                        else {
                            if(!bsi.isInList(identifier)) {
                                InetAddress randNode = randomNode(CANList);
                                Point2D.Float randPoint = randomPoint();
                                System.out.println("Random Node: " + randNode.getHostAddress());
                                System.out.println("Random Point: " + randPoint);
                                myNode = LocateRegistry.getRegistry(randNode.getHostAddress(), 2000);
                                ni = (NodeInterface) myNode.lookup("Node");
                                nodePeer = ni.join(identifier, randPoint, InetAddress.getLocalHost());
                                if(nodePeer == null) {
                                    System.out.println("Failure.");
                                    return;
                                }
                                bsi.addNewNode(nodePeer.getIdentifier(), nodePeer.getIP());
                            }
                            else {
                                System.out.println("Failure: Please choose a different identifier.");
                                break;
                            }
                        }
                        
                        Node remote = new Node();
                        System.out.println("Node is ready");
                        InetAddress ipaddr = InetAddress.getLocalHost();
                        System.out.println("Node IP: " + ipaddr.getHostAddress());
                        System.out.println("Node NAME: " + ipaddr.getHostName());
                        System.out.println();   
                        Registry registry = LocateRegistry.createRegistry(2000);
                        registry.rebind("Node", remote);
                        break;
                    case 5:
                        if(nodePeer == null) {
                            System.out.println("Failure: Peer is not in the CAN network.");
                            break;
                        }
                        bsi.removeNode(nodePeer.getIdentifier(), nodePeer.getIP());
                        if(bsi.getIPList().isEmpty()) {
                            System.exit(0);
                        }
                        leave();
                        System.out.println("Neighbor(s): ");
                        Iterator it = nodePeer.getNeighbors().entrySet().iterator();
                        while(it.hasNext()) {
                            Map.Entry<Zone, InetAddress> entry = (Map.Entry<Zone, InetAddress>) it.next();
                            myNode = LocateRegistry.getRegistry(entry.getValue().getHostAddress(), 2000);
                            ni = (NodeInterface) myNode.lookup("Node");
                            System.out.println(ni.view());
                        }
                        System.exit(0);
                    default:
                        System.out.println("Please choose a correct command.");
                }
                System.out.println("");
                System.out.println("Please choose a command:");
                System.out.println("1. insert");
                System.out.println("2. search");
                System.out.println("3. view");
                System.out.println("4. join");
                System.out.println("5. leave");
                System.out.print("Input: ");
            }
        } catch(IOException e) {
            System.out.println("IOException: " + e);
        } catch (NotBoundException e) {
            System.out.println("NotBoundException: " + e);
        }
    }
    
    private static InetAddress randomNode(ArrayList<InetAddress> CANList) {
        Random rand = new Random();
        return CANList.get(rand.nextInt(CANList.size()));
    }
    
    private static Point2D.Float randomPoint() {
        Random rand = new Random();
        return new Point2D.Float(rand.nextFloat() * 10.0f, rand.nextFloat() * 10.0f);
    }

    
    @Override
    public String view() throws RemoteException {
        return nodePeer.view();
    }
    
    @Override
    public ArrayList insertKeyword(String file, String keyword) {
        Registry myNode;
        NodeInterface ni;
        ArrayList result = new ArrayList();
        ArrayList<InetAddress> ipList = new ArrayList<>();
        Point2D.Float keyPoint = keywordToPoint(keyword);
        if(isInZone(nodePeer, keyPoint)) {
            nodePeer.getData().put(keyword, file);
            ipList.add(nodePeer.getIP());
            result.add(nodePeer);
            result.add(ipList);
            return result;
        }
        else {
            for(Peer p : nodePeer.getTempJoin()) {
                if(isInZone(p, keyPoint)) {
                    p.getData().put(keyword, file);
                    ipList.add(nodePeer.getIP());
                    result.add(nodePeer);
                    result.add(ipList);
                    return result;
                }
            }
        }
        InetAddress nextIP = neighborClosestToPoint(nodePeer, keyPoint);
        try {
            myNode = LocateRegistry.getRegistry(nextIP.getHostAddress(), 2000);
            ni = (NodeInterface) myNode.lookup("Node");
            result = ni.insertKeyword(file, keyword);
            if(result != null) {
                ((ArrayList) result.get(1)).add(nodePeer.getIP());
            }
            return result;
        } catch(IOException e) {
            System.out.println("IOException: " + e);
            return null;
        } catch (NotBoundException e) {
            System.out.println("NotBoundException: " + e);
            return null;
        } 
    }
    
    @Override
    public ArrayList searchKeyword(String keyword) {
        Registry myNode;
        NodeInterface ni;
        ArrayList result = new ArrayList();
        ArrayList<InetAddress> ipList = new ArrayList<>();
        Point2D.Float keyPoint = keywordToPoint(keyword);
        if(isInZone(nodePeer, keyPoint)) {
            if(!nodePeer.getData().containsKey(keyword)) {
                return null;
            }
            ipList.add(nodePeer.getIP());
            result.add(nodePeer);
            result.add(ipList);
            return result;
        }
        else {
            for(Peer p : nodePeer.getTempJoin()) {
                if(isInZone(p, keyPoint)) {
                    if(!p.getData().containsKey(keyword)) {
                        return null;
                    }
                    ipList.add(nodePeer.getIP());
                    result.add(nodePeer);
                    result.add(ipList);
                    return result;
                }
            }
        }
        InetAddress nextIP = neighborClosestToPoint(nodePeer, keyPoint);
        try {
            myNode = LocateRegistry.getRegistry(nextIP.getHostAddress(), 2000);
            ni = (NodeInterface) myNode.lookup("Node");
            result = ni.searchKeyword(keyword);
            if(result != null) {
                ((ArrayList) result.get(1)).add(nodePeer.getIP());
            }
            return result;
        } catch(IOException e) {
            System.out.println("IOException: " + e);
            return null;
        } catch (NotBoundException e) {
            System.out.println("NotBoundException: " + e);
            return null;
        } 
    }
    
    @Override
    public Peer join(String identifier, Point2D.Float point, InetAddress joinNodeIP) throws RemoteException {
        Registry myNode;
        NodeInterface ni;
        
        if(isInZone(nodePeer, point)) {
            if(nodePeer.isSquare()) {
                return verticalSplit(identifier, joinNodeIP);
            }
            else {
                return horizontalSplit(identifier, joinNodeIP);
            }
        }
        else {
            // check if newly-join node can take one of the taken-over nodes
            if(!nodePeer.getTempJoin().isEmpty()) {
                for(int i = 0; i < nodePeer.getTempJoin().size(); i++) {
                    Peer temp = nodePeer.getTempJoin().get(i);
                    if(isInZone(temp, point)) {
                        nodePeer.getTempJoin().remove(i);
                        temp.setIdentifier(identifier);
                        temp.setIP(joinNodeIP);
                        Iterator it = temp.getNeighbors().entrySet().iterator();
                        while(it.hasNext()) {
                            Map.Entry<Zone, InetAddress> entry = (Map.Entry<Zone, InetAddress>) it.next();
                            try {
                                myNode = LocateRegistry.getRegistry(entry.getValue().getHostAddress(), 2000);
                                ni = (NodeInterface) myNode.lookup("Node");
                                ni.addNeighbor(temp.getZone(), joinNodeIP);
                            } catch(IOException e) {
                                System.out.println("IOException: " + e);
                                return null;
                            } catch (NotBoundException e) {
                                System.out.println("NotBoundException: " + e);
                                return null;
                            }
                        }
                        return temp;
                    }
                }
            }
            InetAddress nextIP = neighborClosestToPoint(nodePeer, point);
            try {
                myNode = LocateRegistry.getRegistry(nextIP.getHostAddress(), 2000);
                ni = (NodeInterface) myNode.lookup("Node");
                return ni.join(identifier, point, joinNodeIP);
            } catch(IOException e) {
                System.out.println("IOException: " + e);
                return null;
            } catch (NotBoundException e) {
                System.out.println("NotBoundException: " + e);
                return null;
            }
        }
    }

    @Override
    public void addNeighbor(Zone zone, InetAddress ip) throws RemoteException {
        if(isNeighbor(nodePeer.getZone(), zone)) {
            Iterator it = nodePeer.getNeighbors().entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<Zone, InetAddress> entry = (Map.Entry<Zone, InetAddress>) it.next();
                if(entry.getKey().equals(zone)) {
                    it.remove();
                }
            }
            nodePeer.getNeighbors().put(zone, ip);
        }
        for(Peer p : nodePeer.getTempJoin()) {
            if(isNeighbor(p.getZone(), zone)) {
                Iterator it = p.getNeighbors().entrySet().iterator();
                while(it.hasNext()) {
                    Map.Entry<Zone, InetAddress> entry = (Map.Entry<Zone, InetAddress>) it.next();
                    if(entry.getValue().equals(ip)) {
                        it.remove();
                        break;
                    }    
                }
                p.getNeighbors().put(zone, ip);
            }
        }
    }
    
    @Override
    public void removeNeighbor(InetAddress ip) throws RemoteException {
        Iterator it = nodePeer.getNeighbors().entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<Zone, InetAddress> entry = (Map.Entry<Zone, InetAddress>) it.next();
            if(entry.getValue().equals(ip)) {
                it.remove();
                break;
            }
        }
        for(Peer p : nodePeer.getTempJoin()) {
            it = p.getNeighbors().entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<Zone, InetAddress> entry = (Map.Entry<Zone, InetAddress>) it.next();
                if(entry.getValue().equals(ip)) {
                    it.remove();
                }
            }
        }
    }
    
    private boolean isInZone(Peer member, Point2D.Float point) {
        if(point.getX() < member.getZone().getStartCoordinate().getX())
            return false;
        if(point.getX() > member.getZone().getEndCoordinate().getX())
            return false;
        if(point.getY() < member.getZone().getStartCoordinate().getY())
            return false;
        if(point.getY() > member.getZone().getEndCoordinate().getY())
            return false;
        
        return true;
    }
    
    private Peer horizontalSplit(String identifier, InetAddress joinNodeIP) {
        Peer newPeer;
        Registry myNode;
        NodeInterface ni;
        // create new zone
        Point2D.Float newStartCoordinateBottom = new Point2D.Float();
        newStartCoordinateBottom.setLocation(nodePeer.getZone().getStartCoordinate().getX(),
                nodePeer.getZone().getStartCoordinate().getY());
        Point2D.Float newEndCoordinateBottom = new Point2D.Float();
        newEndCoordinateBottom.setLocation(nodePeer.getZone().getEndCoordinate().getX(),
                (nodePeer.getZone().getStartCoordinate().getY() +
                 nodePeer.getZone().getEndCoordinate().getY()) / 2);
        Zone newZoneBottom = new Zone(newStartCoordinateBottom, newEndCoordinateBottom);
        
        Point2D.Float newStartCoordinateTop = new Point2D.Float();
        newStartCoordinateTop.setLocation(nodePeer.getZone().getStartCoordinate().getX(),
                (nodePeer.getZone().getStartCoordinate().getY() +
                 nodePeer.getZone().getEndCoordinate().getY()) / 2);
        Point2D.Float newEndCoordinateTop = new Point2D.Float();
        newEndCoordinateTop.setLocation(nodePeer.getZone().getEndCoordinate().getX(),
                nodePeer.getZone().getEndCoordinate().getY());
        Zone newZoneTop = new Zone(newStartCoordinateTop, newEndCoordinateTop);
        
        // create new peer for newly-joined node & update this node
        newPeer = new Peer(identifier, joinNodeIP);
        nodePeer.setZone(newZoneBottom);
        newPeer.setZone(newZoneTop);
        nodePeer.setSquare(true);
        newPeer.setSquare(true);      
        // update data based on the keyword (hash to position)
        splitData(nodePeer, newPeer);
        Iterator it = nodePeer.getNeighbors().entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<Zone, InetAddress> entry = (Map.Entry<Zone, InetAddress>) it.next();
            try {
                myNode = LocateRegistry.getRegistry(entry.getValue().getHostAddress(), 2000);
                ni = (NodeInterface) myNode.lookup("Node");
                if(!isNeighbor(entry.getKey(), nodePeer.getZone())) {
                    newPeer.getNeighbors().put(entry.getKey(), entry.getValue());
                    ni.addNeighbor(newPeer.getZone(), newPeer.getIP());
                    ni.removeNeighbor(nodePeer.getIP());
                    it.remove();
                }
                else {
                    if(isNeighbor(entry.getKey(), newPeer.getZone())) {
                        newPeer.getNeighbors().put(entry.getKey(), entry.getValue());
                        ni.addNeighbor(newPeer.getZone(), newPeer.getIP());
                    }
                    ni.removeNeighbor(nodePeer.getIP());
                    ni.addNeighbor(nodePeer.getZone(), nodePeer.getIP());
                }
            } catch(IOException e) {
                System.out.println("IOException: " + e);
                return null;
            } catch (NotBoundException e) {
                System.out.println("NotBoundException: " + e);
                return null;
            }
        }
        newPeer.getNeighbors().put(nodePeer.getZone(), nodePeer.getIP());
        nodePeer.getNeighbors().put(newPeer.getZone(), newPeer.getIP());
        
        return newPeer;
    }
    
    private Peer verticalSplit(String identifier, InetAddress joinNodeIP) {     
        Peer newPeer;
        Registry myNode;
        NodeInterface ni;
        // create new zone
        Point2D.Float newStartCoordinateLeft = new Point2D.Float();
        newStartCoordinateLeft.setLocation(nodePeer.getZone().getStartCoordinate().getX(),
                nodePeer.getZone().getStartCoordinate().getY());
        Point2D.Float newEndCoordinateLeft = new Point2D.Float();
        newEndCoordinateLeft.setLocation((nodePeer.getZone().getStartCoordinate().getX() + 
                nodePeer.getZone().getEndCoordinate().getX()) / 2,
                nodePeer.getZone().getEndCoordinate().getY());
        Zone newZoneLeft = new Zone(newStartCoordinateLeft, newEndCoordinateLeft); 
        
        Point2D.Float newStartCoordinateRight = new Point2D.Float();
        newStartCoordinateRight.setLocation((nodePeer.getZone().getStartCoordinate().getX() + 
                nodePeer.getZone().getEndCoordinate().getX()) / 2,
                nodePeer.getZone().getStartCoordinate().getY());
        Point2D.Float newEndCoordinateRight = new Point2D.Float();
        newEndCoordinateRight.setLocation(nodePeer.getZone().getEndCoordinate().getX(),
                nodePeer.getZone().getEndCoordinate().getY());      
        Zone newZoneRight = new Zone(newStartCoordinateRight, newEndCoordinateRight);        
        
        // create new peer for newly-joined node & update this node
        newPeer = new Peer(identifier, joinNodeIP);
        nodePeer.setZone(newZoneLeft);
        newPeer.setZone(newZoneRight);
        nodePeer.setSquare(false);
        newPeer.setSquare(false);
        // update data based on the keyword (hash to position)
        splitData(nodePeer, newPeer);
        Iterator it = nodePeer.getNeighbors().entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<Zone, InetAddress> entry = (Map.Entry<Zone, InetAddress>) it.next();
            try {
                myNode = LocateRegistry.getRegistry(entry.getValue().getHostAddress(), 2000);
                ni = (NodeInterface) myNode.lookup("Node");
                if(!isNeighbor(entry.getKey(), nodePeer.getZone())) {
                    newPeer.getNeighbors().put(entry.getKey(), entry.getValue());
                    ni.addNeighbor(newPeer.getZone(), newPeer.getIP());
                    ni.removeNeighbor(nodePeer.getIP());
                    it.remove();
                }
                else {
                    if(isNeighbor(entry.getKey(), newPeer.getZone())) {
                        newPeer.getNeighbors().put(entry.getKey(), entry.getValue());
                        ni.addNeighbor(newPeer.getZone(), newPeer.getIP());
                    }
                    ni.removeNeighbor(nodePeer.getIP());
                    ni.addNeighbor(nodePeer.getZone(), nodePeer.getIP());
                }
            } catch(IOException e) {
                System.out.println("IOException: " + e);
                return null;
            } catch (NotBoundException e) {
                System.out.println("NotBoundException: " + e);
                return null;
            }
        }
        newPeer.getNeighbors().put(nodePeer.getZone(), nodePeer.getIP());
        nodePeer.getNeighbors().put(newPeer.getZone(), newPeer.getIP());
        
        return newPeer;
    }
    
    private boolean isNeighbor(Zone z1, Zone z2) {
        if(z1.getEndCoordinate().getX() == z2.getStartCoordinate().getX()) {
            return true;
        }
        if(z2.getEndCoordinate().getX() == z1.getStartCoordinate().getX()) {
            return true;
        }
        if(z1.getEndCoordinate().getY() == z2.getStartCoordinate().getY()) {
            return true;
        }
        if(z2.getEndCoordinate().getY() == z1.getStartCoordinate().getY()) {
            return true;
        }
        
        return false;
    }
    
    private void splitData(Peer splitPeer, Peer rcvPeer) {
        ArrayList<String> removeKeys = new ArrayList<>();
        for(Map.Entry<String, String> entry : splitPeer.getData().entrySet()) {
            if(isInZone(rcvPeer, keywordToPoint(entry.getKey()))) {
                rcvPeer.getData().put(entry.getKey(), entry.getValue());
                removeKeys.add(entry.getKey());
            }
        }
        for(String key : removeKeys) {
            splitPeer.getData().remove(key);
        }
    }
    
    private Point2D.Float keywordToPoint(String keyword) {
        Point2D.Float point = new Point2D.Float();
        float x = CharAtOdd(keyword);
        float y = CharAtEven(keyword);
        point.setLocation(x, y);
        return point;
    }
            
    private float CharAtOdd(String keyword) {
        float value = 0;
        for(int i = 0; i < keyword.length(); i++) {
            if((i & 1) == 1) {
                value += keyword.charAt(i);
            }
        }
        return value % 10;
    }
    
    private float CharAtEven(String keyword) {
        float value = 0;
        for(int i = 0; i < keyword.length(); i++) {
            if((i & 1) == 0) {
                value += keyword.charAt(i);
            }
        }
        return value % 10;
    }
    
    private InetAddress neighborClosestToPoint(Peer peer, Point2D.Float point) {
        InetAddress closestP = null;
        float smallest = 100;
        float current;
        Iterator it = peer.getNeighbors().entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<Zone, InetAddress> entry = (Map.Entry<Zone, InetAddress>) it.next();
            current = (float) (entry.getKey().getStartCoordinate().distance(point) + entry.getKey().getEndCoordinate().distance(point));
            if(current < smallest) {
                smallest = current;
                closestP = entry.getValue();
            }
        }
        return closestP;
    }
    
    private static void leave() {
        Registry myNode;
        NodeInterface ni;
        boolean merged = false;
        
        Iterator it = nodePeer.getNeighbors().entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<Zone, InetAddress> entry = (Map.Entry<Zone, InetAddress>) it.next();
            try {
                myNode = LocateRegistry.getRegistry(entry.getValue().getHostAddress(), 2000);
                ni = (NodeInterface) myNode.lookup("Node");
                ni.removeNeighbor(nodePeer.getIP());
                if(isValidMerge(entry.getKey()) && !merged) {
                    merged = true;
                    ni.mergeNode(nodePeer);
                    ni.checkNeighborIsDelete();
                }
            } catch(IOException e) {
                System.out.println("IOException: " + e);
            } catch (NotBoundException e) {
                System.out.println("NotBoundException: " + e);
            }
        }
        
        if(!merged) {
            System.out.println(!merged);
            // to do
            InetAddress smallestPeerIP = searchForSmallestNeighbor();
            try {
                myNode = LocateRegistry.getRegistry(smallestPeerIP.getHostAddress(), 2000);
                ni = (NodeInterface) myNode.lookup("Node");
                ni.tempMergeNode(nodePeer);
            } catch(IOException e) {
                System.out.println("IOException: " + e);
            } catch (NotBoundException e) {
                System.out.println("NotBoundException: " + e);
            }
        }
        
        nodePeer.setIdentifier("$" + nodePeer.getIdentifier());
        nodePeer.setIP(null);
    }
    
    @Override
    public void checkNeighborIsDelete() throws RemoteException {
        Registry myNode;
        NodeInterface ni;
        for (Map.Entry<Zone, InetAddress> entry1 : nodePeer.getNeighbors().entrySet()) {
            for(Map.Entry<Zone, InetAddress> entry2 : nodePeer.getNeighbors().entrySet()) {
                if(!entry1.getKey().equals(entry2.getKey()) && entry1.getValue().equals(entry2.getValue())) {
                    try {
                        myNode = LocateRegistry.getRegistry(entry1.getValue().getHostAddress(), 2000);
                        ni = (NodeInterface) myNode.lookup("Node");
                        Peer result = ni.canPeerMergeDelete(nodePeer.getZone());
                        if(result != null) {
                            this.mergeNode(result);
                            return;
                        }
                    } catch(IOException e) {
                        System.out.println("IOException: " + e);
                    } catch (NotBoundException e) {
                        System.out.println("NotBoundException: " + e);
                    }
                }
            }
        }
        // check if we have valid merge with take-over node
        if(!nodePeer.getTempJoin().isEmpty()) {
            for(Peer p : nodePeer.getTempJoin()) {
                if(isValidMerge(p.getZone())) {
                    nodePeer.getTempJoin().remove(p);
                    this.mergeNode(p);
                    // update node & neighbors
                    Iterator it = nodePeer.getNeighbors().entrySet().iterator();
                    while(it.hasNext()) {
                        Map.Entry<Zone, InetAddress> entry = (Map.Entry<Zone, InetAddress>) it.next();
                        if(!bsi.getIPList().contains(entry.getValue())) {
                            it.remove();
                        }
                        else {
                            try {
                                myNode = LocateRegistry.getRegistry(entry.getValue().getHostAddress(), 2000);
                                ni = (NodeInterface) myNode.lookup("Node");
                                ni.removeDuplicate(nodePeer.getZone(), nodePeer.getIP());
                            } catch(IOException e) {
                                System.out.println("IOException: " + e);
                            } catch (NotBoundException e) {
                                System.out.println("NotBoundException: " + e);
                            }
                        }
                    }
                    return;
                }
            }
        }
    }
    
    @Override
    public void removeDuplicate(Zone zone, InetAddress ip) throws RemoteException {
        Iterator it = nodePeer.getNeighbors().entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<Zone, InetAddress> entry = (Map.Entry<Zone, InetAddress>) it.next();
            if(entry.getValue().equals(ip) && !entry.getKey().equals(zone)) {
                    it.remove();
            }
        }
    }
    
    @Override
    public Peer canPeerMergeDelete(Zone askPeerZone) throws RemoteException {
        for(Peer p : nodePeer.getTempJoin()) {
            if(p.getZone().getStartCoordinate().distance(askPeerZone.getStartCoordinate())
                == p.getZone().getEndCoordinate().distance(askPeerZone.getEndCoordinate())) {
                nodePeer.getTempJoin().remove(p);
                return p;
            }
        }
        return null;
    }
    
    @Override
    public void tempMergeNode(Peer peer) throws RemoteException {
        Registry myNode;
        NodeInterface ni;
        
        nodePeer.getTempJoin().add(peer);
        // update new zone to neighbors
        Iterator it = peer.getNeighbors().entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<Zone, InetAddress> entry = (Map.Entry<Zone, InetAddress>) it.next();
            if(entry.getValue().equals(nodePeer.getIP())) {
                continue;
            }
            try {
                myNode = LocateRegistry.getRegistry(entry.getValue().getHostAddress(), 2000);
                ni = (NodeInterface) myNode.lookup("Node");
                ni.addNeighbor(peer.getZone(), nodePeer.getIP());
            } catch(IOException e) {
                System.out.println("IOException: " + e);
                return;
            } catch (NotBoundException e) {
                System.out.println("NotBoundException: " + e);
                return;
            }
        }
    }
    
    private static boolean isValidMerge(Zone checkZone) {
        if(nodePeer.getZone().getStartCoordinate().getX() == checkZone.getStartCoordinate().getX()
                && nodePeer.getZone().getEndCoordinate().getX() == checkZone.getEndCoordinate().getX()) {
            return true;
        }
        
        if(nodePeer.getZone().getStartCoordinate().getY() == checkZone.getStartCoordinate().getY()
                && nodePeer.getZone().getEndCoordinate().getY() == checkZone.getEndCoordinate().getY()) {
            return true;
        }
        
        return false;
    }
    
    @Override
    public void mergeNode(Peer removePeer) throws RemoteException {
        mergeZone(removePeer.getZone());
        mergeNeighbors(removePeer.getNeighbors());
        mergeData(removePeer.getData());
        nodePeer.setSquare(!nodePeer.isSquare());
        mergeTempJoin(removePeer.getTempJoin(), removePeer.getIP());
    }
    
    private void mergeTempJoin(ArrayList<Peer> tempJoin, InetAddress oldIP) {
        Registry myNode;
        NodeInterface ni;
        for(Peer p : tempJoin) {
            Iterator it = p.getNeighbors().entrySet().iterator();
            while(it.hasNext()) {
                Map.Entry<Zone, InetAddress> entry = (Map.Entry<Zone, InetAddress>) it.next();
                if(entry.getValue().equals(nodePeer.getIP())) {
                    continue;
                }
                if(entry.getValue().equals(oldIP)) {
                    it.remove();
                    continue;
                }
                
                try {
                    myNode = LocateRegistry.getRegistry(entry.getValue().getHostAddress(), 2000);
                    ni = (NodeInterface) myNode.lookup("Node");
                    ni.removeNeighbor(oldIP);
                    ni.addNeighbor(p.getZone(), nodePeer.getIP());
                } catch(IOException e) {
                    System.out.println("IOException: " + e);
                    return;
                } catch (NotBoundException e) {
                    System.out.println("NotBoundException: " + e);
                    return;
                }
            }
            nodePeer.getTempJoin().add(p);
            if(isNeighbor(nodePeer.getZone(), p.getZone())) {
                p.getNeighbors().put(nodePeer.getZone(), nodePeer.getIP());
            }
        }
    }
    
    private void mergeZone(Zone mergeZone) {
        Registry myNode;
        NodeInterface ni;
        Point2D.Float startCoordinate = new Point2D.Float();
        Point2D.Float endCoordinate = new Point2D.Float();
        
        if(nodePeer.getZone().getStartCoordinate().getX() > mergeZone.getStartCoordinate().getX()) {
            startCoordinate.setLocation(mergeZone.getStartCoordinate());
            endCoordinate.setLocation(nodePeer.getZone().getEndCoordinate());
        }
        else if(nodePeer.getZone().getStartCoordinate().getX() < mergeZone.getStartCoordinate().getX()) {
            startCoordinate.setLocation(nodePeer.getZone().getStartCoordinate());
            endCoordinate.setLocation(mergeZone.getEndCoordinate());
        }
        else {
            if(nodePeer.getZone().getStartCoordinate().getY() > mergeZone.getStartCoordinate().getY()) {
                startCoordinate.setLocation(mergeZone.getStartCoordinate());
                endCoordinate.setLocation(nodePeer.getZone().getEndCoordinate());
            }
            else {
                startCoordinate.setLocation(nodePeer.getZone().getStartCoordinate());
                endCoordinate.setLocation(mergeZone.getEndCoordinate());
            }
        }
        nodePeer.setZone(new Zone(startCoordinate, endCoordinate));
        
        // update new zone to neighbors
        Iterator it = nodePeer.getNeighbors().entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<Zone, InetAddress> entry = (Map.Entry<Zone, InetAddress>) it.next();
            try {
                myNode = LocateRegistry.getRegistry(entry.getValue().getHostAddress(), 2000);
                ni = (NodeInterface) myNode.lookup("Node");
                ni.removeNeighbor(nodePeer.getIP());
                ni.addNeighbor(nodePeer.getZone(), nodePeer.getIP());
            } catch(IOException e) {
                System.out.println("IOException: " + e);
                return;
            } catch (NotBoundException e) {
                System.out.println("NotBoundException: " + e);
                return;
            }
        }
    }
        
    private void mergeNeighbors(HashMap<Zone, InetAddress> mergeNeighbors) {
        Registry myNode;
        NodeInterface ni;
        
        Iterator it = mergeNeighbors.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<Zone, InetAddress> entry = (Map.Entry<Zone, InetAddress>) it.next();
            try {
                myNode = LocateRegistry.getRegistry(entry.getValue().getHostAddress(), 2000);
                ni = (NodeInterface) myNode.lookup("Node");
                // If not the node itself
                if(!nodePeer.getIP().equals(entry.getValue())) {
                    // update the new neighbors
                    if(!nodePeer.getNeighbors().containsValue(entry.getValue())) {
                        nodePeer.getNeighbors().put(entry.getKey(), entry.getValue());
                        ni.addNeighbor(nodePeer.getZone(), nodePeer.getIP());
                    }
                }
            } catch(IOException e) {
                System.out.println("IOException: " + e);
                return;
            } catch (NotBoundException e) {
                System.out.println("NotBoundException: " + e);
                return;
            }
        }
    }
    
    private void mergeData(HashMap<String, String> data) {
        for(Map.Entry<String, String> entry : data.entrySet()) {
            nodePeer.getData().put(entry.getKey(), entry.getValue());
        }
    }
    
    private static InetAddress searchForSmallestNeighbor() {
        InetAddress smallestPeer = null;
        float smallestArea = 100;
        float currentArea;
        Iterator it = nodePeer.getNeighbors().entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<Zone, InetAddress> entry = (Map.Entry<Zone, InetAddress>) it.next();
            currentArea = zoneArea(entry.getKey());
            if(smallestArea > currentArea) {
                smallestArea = currentArea;
                smallestPeer = entry.getValue();
            }
        }
        return smallestPeer;
    }
    
    private static float zoneArea(Zone zone) {
        float x = (float) (zone.getEndCoordinate().getX() - zone.getStartCoordinate().getX());
        float y = (float) (zone.getEndCoordinate().getY() - zone.getStartCoordinate().getY());
        return x * y;
    }
}

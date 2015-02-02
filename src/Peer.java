import java.net.*;
import java.util.*;
import java.io.Serializable;

public class Peer implements Serializable {
    private Zone zone;
    private InetAddress ip;
    private boolean square;
    private String identifier;
    private ArrayList<Peer> tempJoin;
    private HashMap<Zone, InetAddress> neighbors;
    private HashMap<String, String> data;
     
    public Peer(String identifier, InetAddress ip) {
        this.identifier = identifier;
        this.ip = ip;
        this.neighbors = new HashMap<>();
        this.data = new HashMap<>();
        this.zone = null;
        this.square = true;
        this.tempJoin = new ArrayList<>();
    }
 
    public String view() {
        String result = "------------------------------------------------------------------------------\n";
        result += "#Node: " + this.identifier + "\n";
        result += "#IP Address: " + this.ip.toString() + "\n";
        result += "#Zone: "
                + this.zone.toString() + "\n";
        if(!this.getTempJoin().isEmpty()) {
            result += "#Temporary zone(s):\n";
            for(Peer p : this.getTempJoin()) {
                result += "Zone: " + p.getZone().toString() + "\n";
            }
        }
        result += "#Neighbors:\n";
        Iterator it = this.neighbors.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<Zone, InetAddress> entry = (Map.Entry<Zone, InetAddress>) it.next();
            result += "<" + entry.getKey().toString() + "," + entry.getValue() + ">" + "\n";
        }
        result += "#Data:\n";
        it = this.data.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
            result += "(" + entry.getKey() + "," + entry.getValue() + ")";
            if(it.hasNext()) {
                result += ", ";
            }
            else {
                result += "\n";
            }
        }
        if(!this.getTempJoin().isEmpty()) {
            result += "#Temporary data(s):\n";
            for(Peer p : this.getTempJoin()) {
                it = p.getData()
                        .entrySet().iterator();
                while(it.hasNext()) {
                    Map.Entry<String, String> entry = (Map.Entry<String, String>) it.next();
                    result += "(" + entry.getKey() + "," + entry.getValue() + ")";
                    if(it.hasNext()) {
                        result += ", ";
                    }
                    else {
                        result += "\n";
                    }
                }
            }
        }
        result += "------------------------------------------------------------------------------\n";
        return result;
    }

    public String getIdentifier() {
        return identifier;
    }

    public InetAddress getIP() {
        return ip;
    }

    public HashMap<Zone, InetAddress> getNeighbors() {
        return neighbors;
    }

    public HashMap<String, String> getData() {
        return data;
    }
    
    public Zone getZone() {
        return zone;
    }

    public boolean isSquare() {
        return square;
    }

    public ArrayList<Peer> getTempJoin() {
        return tempJoin;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public void setIP(InetAddress ip) {
        this.ip = ip;
    }

    public void setNeighbors(HashMap<Zone, InetAddress> neighbors) {
        this.neighbors = neighbors;
    }
    
    public void setData(HashMap<String, String> data) {
        this.data = data;
    }
    
    public void setZone(Zone zone) {
        this.zone = zone;
    }

    public void setSquare(boolean square) {
        this.square = square;
    }

    public void setTempJoin(ArrayList<Peer> tempJoin) {
        this.tempJoin = tempJoin;
    }
    
    @Override
    public boolean equals(Object other) {
        if(!(other instanceof Peer)) {
            return false;
        }
        Peer otherPeer = (Peer) other;
        return (this.identifier == null ? otherPeer.identifier == null : this.identifier.equals(otherPeer.identifier));    
    }
}

import java.awt.geom.Point2D;
import java.io.Serializable;

public class Zone implements Serializable {
    private Point2D.Float startCoordinate;
    private Point2D.Float endCoordinate;

    public Zone(Point2D.Float startCoordinate, Point2D.Float endCoordinate) {
        this.startCoordinate = startCoordinate;
        this.endCoordinate = endCoordinate;
    }

    public Point2D.Float getStartCoordinate() {
        return startCoordinate;
    }

    public Point2D.Float getEndCoordinate() {
        return endCoordinate;
    }

    public void setStartCoordinate(Point2D.Float startCoordinate) {
        this.startCoordinate = startCoordinate;
    }

    public void setEndCoordinate(Point2D.Float endCoordinate) {
        this.endCoordinate = endCoordinate;
    }   
    
    @Override
    public String toString() {
        String result = "[" + this.getStartCoordinate().getX() + "-" + this.getEndCoordinate().getX() + "," 
                + this.getStartCoordinate().getY() + "-" + this.getEndCoordinate().getY() + "]";
        return result;
    }
    
    @Override
    public boolean equals(Object other) {
        if(!(other instanceof Zone)) {
            return false;
        }
        Zone otherZone = (Zone) other;
        return this.getStartCoordinate().equals(otherZone.getStartCoordinate()) && this.getEndCoordinate().equals(otherZone.getEndCoordinate());
    }
}

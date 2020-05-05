import java.util.ArrayList;
import java.util.HashMap;

class Metro {
    private HashMap<String, ArrayList<String>> stations;
    private ArrayList<ArrayList<HashMap<String, String>>> connections;
    private ArrayList<Line> lines;

    public Metro(HashMap<String, ArrayList<String>> stations,
                 ArrayList<Line> lines, ArrayList<ArrayList<HashMap<String, String>>> connections){
        this.stations = stations;
        this.lines = lines;
        this.connections = connections;
    }

    public HashMap<String, ArrayList<String>> getStations() {
        return stations;
    }

    public ArrayList<ArrayList<HashMap<String, String>>> getConnections() {
        return connections;
    }

    public ArrayList<Line> getLines() {
        return lines;
    }
}

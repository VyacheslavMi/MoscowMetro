import java.util.ArrayList;
import java.util.HashMap;

class Metro {
    private HashMap<String, ArrayList<String>> stations;
    private ArrayList<ArrayList<HashMap<String, String>>> connections;
    private ArrayList<Line> lines;

    public Metro(){ }

    public HashMap<String, ArrayList<String>> getStations() {
        return stations;
    }

    public void setStations(HashMap<String, ArrayList<String>> stations) {
        this.stations = stations;
    }

    public ArrayList<ArrayList<HashMap<String, String>>> getConnections() {
        return connections;
    }

    public void setConnections(ArrayList<ArrayList<HashMap<String, String>>> connections) {
        this.connections = connections;
    }

    public ArrayList<Line> getLines() {
        return lines;
    }

    public void setLines(ArrayList<Line> lines) {
        this.lines = lines;
    }


}

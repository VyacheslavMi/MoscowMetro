import com.google.gson.GsonBuilder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.google.gson.Gson;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Main {
    private static final String LINK = "https://ru.wikipedia.org/wiki/Список_станций_Московского_метрополитена";
    private static final String PATH = "src/main/resources/map.json";
    private static final Comparator<Object> keyComparator = Comparator
            .comparingInt(s -> Integer.parseInt(((String) s).replaceAll("\\D", "")))
            .thenComparing(s -> ((String) s).replaceAll("\\d", ""));

    public static void main(String[] args) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Metro moscowMetro = new Metro();

        try {
            Document doc = Jsoup.connect(LINK).get();
            Elements neededTableElements = doc.select("table[class^=standard]")
                                              .select("td:lt(2)");

            moscowMetro.setStations(createStations(neededTableElements));
            moscowMetro.setLines(createLines(neededTableElements));
            moscowMetro.setConnections(createConnections(moscowMetro));

            String json = gson.toJson(moscowMetro);
            FileWriter jsonToFile = new FileWriter(PATH);
            jsonToFile.write(json);
            jsonToFile.close();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }

    private static ArrayList<Line> createLines(Elements elements) {
        ArrayList<Line> lines = new ArrayList<>();
        HashMap<String, String> lineFromHtml = new HashMap<>();

        for (int i = 0; i < elements.size(); i += 2) {
            String number = elements.get(i).select("span").first().text();
            if (number.charAt(0) == '0') number = number.substring(1);
            number = number.replace('A', 'А');
            if(!lineFromHtml.containsKey(number)) {
                String name = elements.get(i).select("a").attr("title");
                lineFromHtml.put(number, name);
            }
        }

        lineFromHtml.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(keyComparator))
                .forEach(i -> {
                    Line line = new Line();
                    line.setNumber(i.getKey());
                    line.setName(i.getValue());
                    lines.add(line);
                });
        return lines;
    }

    private static HashMap<String, ArrayList<String>> createStations(Elements elements) {
        HashMap<String, ArrayList<String>> stations = new HashMap<>();

        for (int i = 0; i < elements.size(); i += 2) {
            String number = elements.get(i).select("span").first().text();
            if (number.charAt(0) == '0') number = number.substring(1);
            String name = elements.get(i + 1).select("a").first().text();

            if (!stations.containsKey(number)) {
                ArrayList<String > array = new ArrayList<>();
                array.add(name);
                stations.put(number, array);
            }
            else stations.get(number).add(name);
        }

        return stations.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(keyComparator))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (e1, e2) -> e2, LinkedHashMap::new));
    }

    private static ArrayList<ArrayList<HashMap<String, String>>> createConnections(Metro metro) throws IOException {
        ArrayList<ArrayList<HashMap<String, String>>> connections = new ArrayList<>();

        Elements neededTableRows = searchRowsWithConnection();
        for (Element row : neededTableRows) {
            Elements numAndName = row.select("td:lt(2)");
            String name = numAndName.get(1).select("a").first().text();
            if (isConnectStation(name, connections)) continue;

            ArrayList<HashMap<String, String>> arrayList = new ArrayList<>();
            HashMap<String, String> station = new HashMap<>();

            String number = numAndName.get(0).select("span").first().text();
            if (number.charAt(0) == '0') number = number.substring(1);
            station.put("line", number);
            station.put("name", name);
            arrayList.add(station);
            arrayList.addAll(getConnectedStations(row, metro));

            connections.add(arrayList);
        }
        return connections;
    }

    private static Elements searchRowsWithConnection() throws IOException {
        Elements elements = new Elements();
        Document doc = Jsoup.connect(LINK).get();

        Elements rows = doc.select("table[class^=standard]").select("tr");
        for (Element row : rows) {
            Elements connectionCeils = row
                    .select("span[title~=" +
                            "([П][деорх]{6})*([К][а-я-]+\\s[адекпрс]+)*\\s[ан]{2}\\s[аинстцю]+\\s([а-яА-Я-ё]+\\s)+[а-я]+]");
            if (connectionCeils.size() != 0)
                elements.add(row);
        }
        return elements;
    }

    private static boolean isConnectStation(String string, ArrayList<ArrayList<HashMap<String, String>>> connections){
        for (ArrayList<HashMap<String, String>> connection : connections)
            for (HashMap<String, String> station : connection) {
                if (station.containsValue(string)){
                    return true;
                }
            }
        return false;
    }

    private static ArrayList<HashMap<String, String>> getConnectedStations(Element element, Metro metro){
        ArrayList<HashMap<String, String>> connectedStations = new ArrayList<>();

        Elements cellsOfConnectedStations = element.select("td:eq(3)").select("img");
        for (Element cell : cellsOfConnectedStations) {
            HashMap<String,String> station = new HashMap<>();
            String link = cell.attr("src");
            String imageName = cell.attr("alt");

            int beginIndex = link.indexOf("Line_") + 5;
            int lastIndex = link.indexOf('.', beginIndex);
            String lineNumber = link.substring(beginIndex, lastIndex).replace('A', 'А');
            if (lineNumber.equals("11%D0%90")) lineNumber = "11А";

            String lineName = searchLineName(lineNumber, metro);
            beginIndex = imageName.indexOf("цию") + 3;
            lastIndex = imageName.lastIndexOf(lineName.substring(0,5));
            String stationName = imageName.substring(beginIndex, lastIndex).trim();
            station.put("line", lineNumber);
            station.put("name", stationName);
            connectedStations.add(station);
        }
        return connectedStations;
    }

    private static String searchLineName(String number, Metro metro){
        ArrayList<Line> lines = metro.getLines();
        String name = "";
        for (Line line : lines) {
            if (line.getNumber().equals(number)) {
                name = line.getName();
            }
        }
        return name;
    }
}
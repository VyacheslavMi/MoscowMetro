import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

class MetroSchemeBuilder {
    private HashMap<String, ArrayList<String>> stations;
    private ArrayList<Line> lines;
    private String link;
    private static final Comparator<Object> keyComparator = Comparator
            .comparingInt(s -> Integer.parseInt(((String) s).replaceAll("\\D", "")))
            .thenComparing(s -> ((String) s).replaceAll("\\d", ""));

    public MetroSchemeBuilder (String link) {
        try {
            this.link = link;
            Document doc = Jsoup.connect(this.link).get();
            Elements tableElements = doc.select("table[class^=standard]")
                    .select("td:lt(2)");

            this.stations = createStations(tableElements);
            this.lines = createLines(tableElements);
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public HashMap<String, ArrayList<String>> getStations() {
        return stations;
    }

    public ArrayList<Line> getLines() {
        return lines;
    }

    public String getLink() {
        return link;
    }

    public Metro buildMetroScheme() throws IOException {
        return new Metro(this.stations, this.lines, createConnections(this.link));
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
                    Line line = new Line(i.getKey(), i.getValue());
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

    private static ArrayList<ArrayList<HashMap<String, String>>> createConnections(String link)
            throws IOException {
        ArrayList<ArrayList<HashMap<String, String>>> connections = new ArrayList<>();

        Elements neededTableRows = searchRowsWithConnection(link);
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
            arrayList.addAll(getConnectedStations(row));

            connections.add(arrayList);
        }
        return connections;
    }

    private static Elements searchRowsWithConnection(String link) throws IOException {
        Elements elements = new Elements();
        Document doc = Jsoup.connect(link).get();

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

    private static ArrayList<HashMap<String, String>> getConnectedStations(Element element){
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

            beginIndex = imageName.indexOf("цию") + 3;
            lastIndex = lastIndexOfRegex(imageName);
            String stationName = imageName.substring(beginIndex, lastIndex).trim();
            station.put("line", lineNumber);
            station.put("name", stationName);
            connectedStations.add(station);
        }
        return connectedStations;
    }

    private static int lastIndexOfRegex(String string){
        int lastIndex = -1;
        Pattern pattern = Pattern.compile("[А-Я][а-яё]+(-[А-Я][а-яё]+)?(\\s[а-яё]+)?\\s[а-я]+");
        Matcher matcher = pattern.matcher(string);

        while (matcher.find()) lastIndex = matcher.start();
        return lastIndex;
    }
}
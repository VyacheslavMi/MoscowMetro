import com.google.gson.GsonBuilder;
import com.google.gson.Gson;

import java.io.FileWriter;

public class Main {
    private static final String LINK = "https://ru.wikipedia.org/wiki/Список_станций_Московского_метрополитена";
    private static final String PATH = "src/main/resources/map.json";

    public static void main(String[] args) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            MetroSchemeBuilder moscowMetroScheme = new MetroSchemeBuilder(LINK);
            Metro moscowMetro = moscowMetroScheme.buildMetroScheme();

            String json = gson.toJson(moscowMetro);
            FileWriter jsonToFile = new FileWriter(PATH);
            jsonToFile.write(json);
            jsonToFile.close();
        }
        catch (Exception ex){
            ex.printStackTrace();
        }
    }
}
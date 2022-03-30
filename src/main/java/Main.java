
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.nio.file.Files;
import java.nio.file.Path;

import java.text.SimpleDateFormat;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        String requiredOrigin = "VVO"; // Владивосток
        String requiredDestination = "TLV"; // Тель-Авив
        int percentileInd = 90; // 90-й процентиль
        SimpleDateFormat format = new SimpleDateFormat("dd.MM.yy HH:mm");

        System.out.println("Введите путь файла JSON");
        Scanner scanner = new Scanner(System.in);
        String filePath = scanner.next().replace("/", "\\");

        try {

            String jsonString = Files.readString(Path.of(filePath));
            jsonString = jsonString.replace("\uFEFF", ""); //Убран BOM символ в начале файла

            JSONParser parser = new JSONParser();
            JSONObject jsonObject = (JSONObject) parser.parse(jsonString);
            JSONArray tickets = (JSONArray) jsonObject.get("tickets");

            ArrayList<Long> flightMinutes = new ArrayList<>();

            for (Object obj : tickets) {

                JSONObject ticket = (JSONObject) obj;
                if (!ticket.get("origin").equals(requiredOrigin)) continue;
                if (!ticket.get("destination").equals(requiredDestination)) continue;

                String departure = String.format("%s %s", ticket.get("departure_date"), ticket.get("departure_time"));
                String arrival = String.format("%s %s", ticket.get("arrival_date"), ticket.get("arrival_time"));
                long diffMS = format.parse(arrival).getTime() - format.parse(departure).getTime();
                flightMinutes.add(diffMS / 1000 / 60);

            }

            Collections.sort(flightMinutes);

            long average = flightMinutes.stream().mapToLong(a -> a).sum() / flightMinutes.size();
            System.out.printf("Среднее время полета - %s\n", formatTime(average));

            int num = (int) Math.ceil(percentileInd / 100.0 * flightMinutes.size());
            long percentile = flightMinutes.get(num - 1);
            System.out.printf("%d-й процентиль времени полета - %s\n", percentileInd, formatTime(percentile));

        } catch(Exception e) {
            e.printStackTrace();
        }

    }

    private static String formatTime(long minutes) {
        return String.format("%d ч. %d мин.", minutes / 60, minutes % 60);
    }

}

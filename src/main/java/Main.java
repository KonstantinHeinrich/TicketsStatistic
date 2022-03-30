
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.nio.file.Files;
import java.nio.file.Path;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        // Владивосток
        String requiredOrigin = "VVO";
        String originZone = "Asia/Vladivostok";

        // Тель-Авив
        String requiredDestination = "TLV";
        String destinationZone = "Asia/Tel_Aviv";

        int percentileInd = 90; // 90-й процентиль

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

                ZonedDateTime departure = parseDate(ticket.get("departure_date"), ticket.get("departure_time"), originZone);
                ZonedDateTime arrival = parseDate(ticket.get("arrival_date"), ticket.get("arrival_time"), destinationZone);
                flightMinutes.add(ChronoUnit.MINUTES.between(departure, arrival));

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

    private static ZonedDateTime parseDate(Object datePart, Object timePart, String timeZone) {
        String strDate = String.format("%s %s", datePart, timePart);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yy H:mm");
        return LocalDateTime.parse(strDate , formatter).atZone(ZoneId.of(timeZone));
    }

    private static String formatTime(long minutes) {
        return String.format("%d ч. %d мин.", minutes / 60, minutes % 60);
    }

}

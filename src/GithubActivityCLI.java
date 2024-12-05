import javax.json.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
@SuppressWarnings("deprecation")
public class GithubActivityCLI {

    public static void main(String[] args) {

        if(args.length != 1) {
            System.out.println("Usage: java GithubActivityCLI <username>");
            return;
        }
//        String username = "IamKarimovich";
//        fetchAndDisplayActivity(username);

        String username = args[0];
        fetchAndDisplayActivity(username);
    }

    public static void fetchAndDisplayActivity(String username) {
        try {

            String apiUrl = "https://api.github.com/users/" + username + "/events";

            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Aagent", "GithubActivityCLI");

            int responseCode = connection.getResponseCode();

            if(responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;

                while((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                GithubActivityCLI.processActivity((response.toString()));
            }else {
            System.out.println("Error: " + responseCode + " " + connection.getResponseMessage());
        }
    } catch (Exception e) {
        System.out.println("An error occurred while fetching data: " + e.getMessage());
    }
}


    public static void processActivity(String jsonResponse) {

        try (JsonReader reader = Json.createReader(new StringReader(jsonResponse))) {
            JsonArray events = reader.readArray();

            for (JsonObject event : events.getValuesAs(JsonObject.class)) {
                String eventType = event.getString("type");
                String repoName = event.getJsonObject("repo").getString("name");

                switch(eventType) {
                    case "PushEvent":
                        int commitCount = event.getJsonObject("payload").getJsonArray("commits").size();
                        System.out.println("Pushed " + commitCount + " commits to" + repoName);
                        break;
                    case "IssuesEvent":
                        String action = event.getJsonObject("payload").getString("action");
                        System.out.println("Opened a new issue in "  + repoName + " for " + action);
                        break;
                    case "WatchEvent":
                        System.out.println(repoName + " is watching " + event.getJsonObject("payload").getString("name"));
                        break;
                    default:
                        System.out.println("Unknown event type " + eventType);
                }
            }
        } catch (Exception exception) {
            System.out.println(exception.getMessage());
        }



    }
}
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {

    public static void main(String[] args) throws URISyntaxException, IOException, InterruptedException {
        // Example cURL command as a string
        String curlCommand = "curl --location 'http://offline-stream.pod.ir/register/?token=ed24e37c7ee84313acf2805a80122f94&hashFile=JEQYZVRLOKXS4ZL4&progressive=false&security=true&quality=720&mobile=false' --header 'Authorization: Bearer 4f8d523e76874f019f2bcd9959cfa16d.XzIwMjM1'";

        // Periodic time in milliseconds (adjust as needed)
        long periodTime = 5000;

        // Create an HttpClient instance
        HttpClient httpClient = HttpClient.newBuilder().build();

        // Create a periodic task to run the cURL command
        Runnable task = () -> {
            try {

//                HttpRequest request = convertCurlToRequest(curlCommand);
//
//                // Send the request and retrieve the response
//                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());


                HttpClient client = HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("http://offline-stream.pod.ir/register/?token=ed24e37c7ee84313acf2805a80122f94&hashFile=JEQYZVRLOKXS4ZL4&progressive=false&security=true&quality=720&mobile=false"))
                        .GET()
                        .setHeader("Authorization", "Bearer 4f8d523e76874f019f2bcd9959cfa16d.XzIwMjM1")
                        .build();
                long sendTime = System.currentTimeMillis();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                long receiveTime = System.currentTimeMillis();

                // Save the response and request time to a log file
                saveResponseToLogFile(sendTime, receiveTime, response);

                // Process the response as needed
                System.out.println("Response: " + response.statusCode() + " " + response.body());
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
               throw new RuntimeException(e);
            }
        };

        // Schedule the periodic task to run every periodTime milliseconds
        long initialDelay = 0; // Delay before the first execution
        Duration period = Duration.ofMillis(periodTime);
        java.util.concurrent.ScheduledExecutorService executor = java.util.concurrent.Executors.newScheduledThreadPool(15);
        executor.scheduleAtFixedRate(task, initialDelay, period.toMillis(), java.util.concurrent.TimeUnit.MILLISECONDS);

        // Sleep to keep the main thread alive (adjust as needed)
        Thread.sleep(periodTime * 1000000);

        // Shutdown the executor
        executor.shutdown();
    }

    private static HttpRequest convertCurlToRequest(String curlCommand) throws URISyntaxException {
        String[] tokens = curlCommand.split("\\s+");

        String method = tokens[1];
        String url = tokens[2].replaceFirst("'(.+)'", "$1");

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(new URI(url))
                .method(method, HttpRequest.BodyPublishers.noBody());

        for (int i = 3; i < tokens.length; i++) {
            if (tokens[i].equalsIgnoreCase("--header")) {
                String header = tokens[++i].replaceFirst("'(.+)'", "$1");
                String[] headerTokens = header.split(":", 2);
                requestBuilder.header(headerTokens[0].trim(), headerTokens[1].trim());
            }
        }

        return requestBuilder.build();
    }


    private synchronized static void saveResponseToLogFile(long sendTime, long receiveTime, HttpResponse<String> response) {
        String logFilePath = "request.log";
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        long totalTime = receiveTime - sendTime;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFilePath, true))) {
            writer.write("Bitch " +"\n");
            if (totalTime > 3000) {
                writer.write("Total Time: " + totalTime + "****************************************************" + "\n");
                writer.write("Send Time: " + sendTime + "\n");
                writer.write("Received Time: " + receiveTime + "\n");
                writer.write("Response Time: " + now.format(formatter) + "\n");
                writer.write("Status Code: " + response.statusCode() + "\n");
                writer.write("Response Body: " + response.body() + "\n");
                writer.write("------------------------------\n");}
             } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

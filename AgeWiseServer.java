
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.time.temporal.ChronoUnit;

public class AgeWiseServer {

    static void send(
        com.sun.net.httpserver.HttpExchange ex,
        int status, String body, String type
    ) throws IOException {
        byte[] data = body.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", type);
        ex.sendResponseHeaders(status, data.length);
        ex.getResponseBody().write(data);
        ex.close();
    }

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(
            new InetSocketAddress(8080), 0
        );

        server.createContext("/api/age", ex -> {
            try {
                String query = ex.getRequestURI().getRawQuery();
                String dob = null;

                if (query != null) {
                    for (String item : query.split("&")) {
                        String[] pair = item.split("=", 2);
                        if (pair.length == 2 &&
                            pair[0].equals("dob")) {
                            dob = java.net.URLDecoder.decode(
                                pair[1], StandardCharsets.UTF_8
                            );
                        }
                    }
                }

                if (dob == null) {
                    send(ex, 400,
                        "{\"error\":\"Date of birth required\"}",
                        "application/json; charset=UTF-8");
                    return;
                }

                LocalDate birth = LocalDate.parse(dob);
                LocalDate today = LocalDate.now();

                if (birth.isAfter(today)) {
                    send(ex, 400,
                        "{\"error\":\"Invalid future date\"}",
                        "application/json; charset=UTF-8");
                    return;
                }

                Period age = Period.between(birth, today);
                long totalDays =
                    ChronoUnit.DAYS.between(birth, today);

                LocalDate next = birth.withYear(today.getYear());
                if (!next.isAfter(today)) {
                    next = birth.withYear(today.getYear() + 1);
                }

                long daysLeft = ChronoUnit.DAYS.between(today, next);
                int years = age.getYears();

                String category = years < 13 ? "Child"
                    : years < 20 ? "Teenager"
                    : years < 60 ? "Adult" : "Senior";

                String json = String.format(
                    "{\"years\":%d,\"months\":%d,\"days\":%d," +
                    "\"totalDays\":%d,\"nextBirthday\":\"%s\"," +
                    "\"daysUntilBirthday\":%d,\"category\":\"%s\"}",
                    age.getYears(), age.getMonths(), age.getDays(),
                    totalDays, next, daysLeft, category
                );

                send(ex, 200, json,
                    "application/json; charset=UTF-8");

            } catch (Exception e) {
                send(ex, 400,
                    "{\"error\":\"Invalid date\"}",
                    "application/json; charset=UTF-8");
            }
        });

        server.createContext("/", ex -> {
            String urlPath = ex.getRequestURI().getPath();
            if (urlPath.equals("/")) urlPath = "/index.html";

            Path root = Paths.get("web").toAbsolutePath().normalize();
            Path file = root.resolve(urlPath.substring(1)).normalize();

            if (!file.startsWith(root) || !Files.isRegularFile(file)) {
                send(ex, 404, "File not found",
                    "text/plain; charset=UTF-8");
                return;
            }

            String type = "text/plain; charset=UTF-8";
            if (urlPath.endsWith(".html"))
                type = "text/html; charset=UTF-8";
            else if (urlPath.endsWith(".css"))
                type = "text/css; charset=UTF-8";
            else if (urlPath.endsWith(".js"))
                type = "application/javascript; charset=UTF-8";

            send(ex, 200, Files.readString(file), type);
        });

        server.start();
        System.out.println("AgeWise Server Started!");
        System.out.println("Open http://localhost:8080");

        
        java.awt.Desktop.getDesktop().browse(
            java.net.URI.create("http://localhost:8080")
        );
    }
}


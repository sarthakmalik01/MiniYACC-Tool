import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class webserver {

    private static final int PORT = Integer.parseInt(
    System.getenv().getOrDefault("PORT", "8080")
);


    private static final Path WEB_ROOT =
            Paths.get("web").toAbsolutePath().normalize();
            static {
   
}

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(
        new InetSocketAddress("0.0.0.0", PORT), 0
);
       

        // Health-check API
        server.createContext("/api/health", exchange -> {

            String response =
                    "{\"status\":\"running\","
                    + "\"message\":\"Mini YACC server is working!\"}";

            sendResponse(exchange, 200, "application/json",
                    response.getBytes("UTF-8"));
        });

        // Grammar analysis API (temporary test endpoint)
        
// Grammar analysis API
server.createContext("/api/analyze", exchange -> {

    if (!exchange.getRequestMethod().equals("POST")) {
        sendResponse(exchange, 405, "text/plain",
                "POST method required".getBytes("UTF-8"));
        return;
    }

    try {
        // Read data sent by website
        String body = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );

        // Extract form data
        Map<String, String> params = new HashMap<>();

        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);

            String key = URLDecoder.decode(
                    parts[0], "UTF-8"
            );

            String value = parts.length > 1
                    ? URLDecoder.decode(parts[1], "UTF-8")
                    : "";

            params.put(key, value);
        }

        String grammarText = params.get("grammar");
        String startSymbol = params.get("startSymbol");

        if (grammarText == null || startSymbol == null
                || startSymbol.trim().isEmpty()) {

            sendResponse(exchange, 400, "application/json",
                    ("{\"status\":\"error\","
                    + "\"message\":\"Grammar and start symbol are required.\"}")
                    .getBytes("UTF-8"));
            return;
        }
        

        // Create grammar object
        Grammar grammar = new Grammar();

        List<String> productions = Arrays.asList(
                grammarText.split("\\R")
        );

        // Load and validate grammar
        boolean valid = grammar.loadGrammar(
                productions, startSymbol
        );

        
        String response;

        if (valid) {

        // Calculate FIRST and FOLLOW
        FirstFollow ff = new FirstFollow(grammar);

        if (ff.calculateSets()) {

                String firstSets = ff.getFirst().toString();
                String followSets = ff.getFollow().toString();
        // Generate LR(0) states
        LR0generator lr0 = new LR0generator(grammar);
        lr0.generateStates();

        // Retrieve generated data
        List<List<int[]>> stateData = lr0.getStateData();
        List<String> ruleLHS = lr0.getRuleLHS();
        List<List<String>> ruleRHS = lr0.getRuleRHS();

        Map<Integer, Map<String, Integer>> transitions =
                lr0.getTransitions();

        // Convert LR(0) states into readable text
        StringBuilder lr0Output = new StringBuilder();

        for (int i = 0; i < stateData.size(); i++) {

        lr0Output.append("\nI").append(i).append(":\n");

        for (int[] item : stateData.get(i)) {

                int ruleIndex = item[0];
                int dotPosition = item[1];

                List<String> rhs =
                        new ArrayList<>(ruleRHS.get(ruleIndex));

                rhs.add(dotPosition, ".");

                lr0Output.append(ruleLHS.get(ruleIndex))
                        .append(" -> ")
                        .append(String.join(" ", rhs))
                        .append("\n");
        }

        lr0Output.append("Transitions:\n");

        Map<String, Integer> stateTransitions =
                transitions.get(i);

        for (Map.Entry<String, Integer> entry :
                stateTransitions.entrySet()) {

                lr0Output.append("  ")
                        .append(entry.getKey())
                        .append(" -> I")
                        .append(entry.getValue())
                        .append("\n");
        }
        }
        // Generate SLR Parsing Table
        SLRTable slr = new SLRTable(grammar, lr0, ff);
        slr.generateTable();

        // Retrieve ACTION and GOTO tables
        Map<Integer, Map<String, String>> actionTable =
                slr.getActionTable();

        Map<Integer, Map<String, Integer>> gotoTable =
                slr.getGotoTable();

        List<String> terminals = slr.getTerminals();
        List<String> nonTerminals = slr.getNonTerminals();

        // Convert SLR table into readable text
        StringBuilder slrOutput = new StringBuilder();

        slrOutput.append("ACTION TABLE\n");

        slrOutput.append(String.format("%-8s", "STATE"));

        for (String terminal : terminals) {
        slrOutput.append(String.format("%-10s", terminal));
        }

        slrOutput.append("\n");

        for (int state = 0; state < actionTable.size(); state++) {

        slrOutput.append(String.format("%-8d", state));

        for (String terminal : terminals) {

                String value = actionTable.get(state)
                        .getOrDefault(terminal, "-");

                slrOutput.append(String.format("%-10s", value));
        }

        slrOutput.append("\n");
        }

        slrOutput.append("\nGOTO TABLE\n");

        slrOutput.append(String.format("%-8s", "STATE"));

        for (String nt : nonTerminals) {
        slrOutput.append(String.format("%-10s", nt));
        }

        slrOutput.append("\n");

        for (int state = 0; state < gotoTable.size(); state++) {

        slrOutput.append(String.format("%-8d", state));

        for (String nt : nonTerminals) {

                String value = gotoTable.get(state).containsKey(nt)
                        ? String.valueOf(gotoTable.get(state).get(nt))
                        : "-";

                slrOutput.append(String.format("%-10s", value));
        }

        slrOutput.append("\n");
        }

        boolean slrConflict = slr.hasConflict();
        
            
        response = "{"
                + "\"status\":\"success\","
                + "\"message\":\"Grammar is valid!\","
                + "\"first\":\"" + escapeJson(firstSets) + "\","
                + "\"follow\":\"" + escapeJson(followSets) + "\","
                + "\"lr0\":\"" + escapeJson(lr0Output.toString()) + "\","
                + "\"slr\":\"" + escapeJson(slrOutput.toString()) + "\","
                + "\"conflict\":" + slrConflict
                + "}";

        } else {

                response = "{\"status\":\"error\","
                        + "\"message\":\"FIRST/FOLLOW calculation failed.\"}";
        }

        } else {

        response = "{\"status\":\"error\","
                + "\"message\":\"Grammar validation failed.\"}";
        }


        sendResponse(exchange, 200, "application/json",
                response.getBytes("UTF-8"));

    } catch (IOException e) {

        String response =
                "{\"status\":\"error\","
                + "\"message\":\"Invalid request.\"}";

        sendResponse(exchange, 400, "application/json",
                response.getBytes("UTF-8"));
    }
});


// Parser Generation API
server.createContext("/api/generate", exchange -> {

    if (!exchange.getRequestMethod().equals("POST")) {
        sendResponse(exchange, 405, "text/plain",
                "POST method required".getBytes("UTF-8"));
        return;
    }

    try {
        String body = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );

        Map<String, String> params = new HashMap<>();

        for (String pair : body.split("&")) {
            String[] parts = pair.split("=", 2);

            String key = URLDecoder.decode(parts[0], "UTF-8");

            String value = parts.length > 1
                    ? URLDecoder.decode(parts[1], "UTF-8")
                    : "";

            params.put(key, value);
        }

        String grammarText = params.get("grammar");
        String startSymbol = params.get("startSymbol");

        if (grammarText == null || startSymbol == null
                || startSymbol.trim().isEmpty()) {
            sendResponse(exchange, 400, "text/plain",
                    "Grammar and start symbol are required."
                            .getBytes("UTF-8"));
            return;
        }

        Grammar grammar = new Grammar();

        List<String> productions = Arrays.asList(
                grammarText.split("\\R")
        );

        if (!grammar.loadGrammar(productions, startSymbol)) {
            sendResponse(exchange, 400, "text/plain",
                    "Invalid grammar.".getBytes("UTF-8"));
            return;
        }

        FirstFollow ff = new FirstFollow(grammar);

        if (!ff.calculateSets()) {
            throw new IllegalStateException(
                    "FIRST/FOLLOW calculation failed."
            );
        }

        LR0generator lr0 = new LR0generator(grammar);
        lr0.generateStates();

        SLRTable slr = new SLRTable(grammar, lr0, ff);
        slr.generateTable();

        ParserGenerator generator =
                new ParserGenerator(grammar, slr);

        File generatedFile = generator.generateParserFile();

        byte[] fileContent = Files.readAllBytes(
                generatedFile.toPath()
        );

        exchange.getResponseHeaders().set(
                "Content-Disposition",
                "attachment; filename=\"GeneratedParser.java\""
        );

        sendResponse(
                exchange,
                200,
                "text/x-java-source",
                fileContent
        );

    } catch (Exception e) {
        sendResponse(
                exchange,
                500,
                "text/plain",
                ("Parser generation failed: " + e.getMessage())
                        .getBytes(StandardCharsets.UTF_8)
        );
    }
});

        // Serve website files
       
// Parsing API
server.createContext("/api/parse", exchange -> {

    if (!exchange.getRequestMethod().equals("POST")) {
        sendResponse(exchange, 405, "text/plain",
                "POST method required".getBytes("UTF-8"));
        return;
    }

    try {

        // Read website data
        String body = new String(
                exchange.getRequestBody().readAllBytes(),
                StandardCharsets.UTF_8
        );

        // Extract form data
        Map<String, String> params = new HashMap<>();

        for (String pair : body.split("&")) {

            String[] parts = pair.split("=", 2);

            String key = URLDecoder.decode(parts[0], "UTF-8");

            String value = parts.length > 1
                    ? URLDecoder.decode(parts[1], "UTF-8")
                    : "";

            params.put(key, value);
        }

        String grammarText = params.get("grammar");
        String startSymbol = params.get("startSymbol");
        String inputString = params.get("inputString");

        if (grammarText == null || startSymbol == null
                || inputString == null
                || startSymbol.trim().isEmpty()) {

            String response =
                    "{\"status\":\"error\","
                    + "\"message\":\"Grammar, start symbol and input are required.\"}";

            sendResponse(exchange, 400, "application/json",
                    response.getBytes("UTF-8"));
            return;
        }

        // Create and validate grammar
        Grammar grammar = new Grammar();

        List<String> productions = Arrays.asList(
                grammarText.split("\\R")
        );

        boolean valid = grammar.loadGrammar(
                productions, startSymbol
        );

        if (!valid) {

            String response =
                    "{\"status\":\"error\","
                    + "\"message\":\"Grammar validation failed.\"}";

            sendResponse(exchange, 200, "application/json",
                    response.getBytes("UTF-8"));
            return;
        }

        // Calculate FIRST and FOLLOW
        FirstFollow ff = new FirstFollow(grammar);

        if (!ff.calculateSets()) {
            throw new IllegalStateException(
                    "FIRST/FOLLOW calculation failed."
            );
        }

        // Generate LR(0) states
        LR0generator lr0 = new LR0generator(grammar);
        lr0.generateStates();

        // Generate SLR table
        SLRTable slr = new SLRTable(grammar, lr0, ff);
        slr.generateTable();

        // Create parser
        Parser parser = new Parser(grammar, slr);

        // Perform parsing
        List<String> results =
                parser.parseWithResult(inputString);

        String parseOutput = String.join("\n", results);

        // Prepare JSON response
        String response =
                "{"
                + "\"status\":\"success\","
                + "\"parse\":\"" + escapeJson(parseOutput) + "\""
                + "}";

        sendResponse(exchange, 200, "application/json",
                response.getBytes("UTF-8"));

    } catch (Exception e) {

        String response =
                "{\"status\":\"error\","
                + "\"message\":\"Parsing request failed.\"}";

        sendResponse(exchange, 500, "application/json",
                response.getBytes("UTF-8"));
    }
});

        server.createContext("/", exchange -> {


            String requestedPath =
                    exchange.getRequestURI().getPath();

            if (requestedPath.equals("/")) {
                requestedPath = "/index.html";
            }

            Path file = WEB_ROOT.resolve(
                    requestedPath.substring(1)
            ).normalize();

            if (!file.startsWith(WEB_ROOT)
                    || !Files.exists(file)
                    || Files.isDirectory(file)) {

                sendResponse(exchange, 404, "text/plain",
                        "File not found".getBytes("UTF-8"));
                return;
            }

            String contentType = "text/plain";

            String filename = file.getFileName().toString();

            if (filename.endsWith(".html")) {
                contentType = "text/html";
            } else if (filename.endsWith(".css")) {
                contentType = "text/css";
            } else if (filename.endsWith(".js")) {
                contentType = "application/javascript";
            }

            byte[] content = Files.readAllBytes(file);

            sendResponse(exchange, 200, contentType, content);
        });

        server.setExecutor(null);
        server.start();

        System.out.println("=================================");
        System.out.println("       MINI YACC WEB SERVER");
        System.out.println("=================================");
        System.out.println(
                "Server running at http://localhost:" + PORT
        );
    }

    private static void sendResponse(
            HttpExchange exchange,
            int status,
            String contentType,
            byte[] response
    ) throws IOException {

        exchange.getResponseHeaders().set(
                "Content-Type",
                contentType + "; charset=UTF-8"
        );

        exchange.sendResponseHeaders(status, response.length);

        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }
    private static String escapeJson(String text) {
    return text.replace("\\", "\\\\")
               .replace("\"", "\\\"")
               .replace("\n", "\\n")
               .replace("\r", "\\r")
               .replace("\t", "\\t");
}
}

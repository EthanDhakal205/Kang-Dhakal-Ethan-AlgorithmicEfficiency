import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.function.Consumer;

class OllamaChatClient {
    static final String DEFAULT_ENDPOINT = "http://localhost:11434/api/chat";
    static final String DEFAULT_MODEL = "llama3.2";

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(8))
        .build();

    static class Message {
        final String role;
        final String content;

        Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    String chat(String endpoint, String model, List<Message> messages) throws IOException, InterruptedException {
        String payload = buildPayload(model, messages);
        HttpRequest request = HttpRequest.newBuilder(normalizeApiEndpoint(endpoint, "chat"))
            .timeout(Duration.ofSeconds(120))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(payload))
            .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Ollama returned HTTP " + response.statusCode() + ": " + compact(response.body()));
        }
        String content = extractMessageContent(response.body());
        if (content == null || content.isBlank()) {
            throw new IOException("Ollama returned an empty or unexpected response.");
        }
        return content.trim();
    }

    boolean isServerAvailable(String endpoint) {
        try {
            HttpRequest request = HttpRequest.newBuilder(normalizeApiEndpoint(endpoint, "tags"))
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() >= 200 && response.statusCode() < 300;
        } catch (IOException | InterruptedException | IllegalArgumentException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }

    boolean hasModel(String endpoint, String model) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(normalizeApiEndpoint(endpoint, "tags"))
            .timeout(Duration.ofSeconds(10))
            .GET()
            .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Ollama returned HTTP " + response.statusCode() + ": " + compact(response.body()));
        }
        String wanted = normalizeModel(model);
        for (String name : extractJsonStrings(response.body(), "name")) {
            if (modelMatches(name, wanted)) {
                return true;
            }
        }
        for (String name : extractJsonStrings(response.body(), "model")) {
            if (modelMatches(name, wanted)) {
                return true;
            }
        }
        return false;
    }

    void pullModel(String endpoint, String model, Consumer<String> progress) throws IOException, InterruptedException {
        String modelName = model == null || model.isBlank() ? DEFAULT_MODEL : model.trim();
        HttpRequest request = HttpRequest.newBuilder(normalizeApiEndpoint(endpoint, "pull"))
            .timeout(Duration.ofMinutes(45))
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString("{\"model\":\"" + escape(modelName) + "\"}"))
            .build();
        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            String body = new String(response.body().readAllBytes(), StandardCharsets.UTF_8);
            throw new IOException("Ollama returned HTTP " + response.statusCode() + ": " + compact(body));
        }

        StringBuilder line = new StringBuilder();
        try (InputStream stream = response.body()) {
            int next;
            while ((next = stream.read()) != -1) {
                if (next == '\n') {
                    handlePullLine(line.toString(), progress);
                    line.setLength(0);
                } else if (next != '\r') {
                    line.append((char) next);
                }
            }
        }
        if (line.length() > 0) {
            handlePullLine(line.toString(), progress);
        }
    }

    URI normalizeApiEndpoint(String rawEndpoint, String apiName) {
        return URI.create(normalizeBaseEndpoint(rawEndpoint) + "/api/" + apiName);
    }

    String normalizeBaseEndpoint(String rawEndpoint) {
        String endpoint = rawEndpoint == null || rawEndpoint.isBlank()
            ? DEFAULT_ENDPOINT
            : rawEndpoint.trim();
        while (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }
        if (endpoint.endsWith("/api/chat")) {
            endpoint = endpoint.substring(0, endpoint.length() - "/api/chat".length());
        } else if (endpoint.endsWith("/api")) {
            endpoint = endpoint.substring(0, endpoint.length() - "/api".length());
        }
        return endpoint;
    }

    private void handlePullLine(String json, Consumer<String> progress) throws IOException {
        if (json == null || json.isBlank()) {
            return;
        }
        String error = extractJsonString(json, "error");
        if (error != null && !error.isBlank()) {
            throw new IOException(error);
        }
        String status = extractJsonString(json, "status");
        if (status == null || status.isBlank()) {
            return;
        }
        long completed = extractJsonLong(json, "completed");
        long total = extractJsonLong(json, "total");
        if (completed > 0 && total > 0) {
            int percent = (int) Math.min(100, Math.round(completed * 100.0 / total));
            progress.accept(status + " (" + percent + "%)");
        } else {
            progress.accept(status);
        }
    }

    private String buildPayload(String model, List<Message> messages) {
        StringBuilder json = new StringBuilder();
        json.append("{\"model\":\"")
            .append(escape(model == null || model.isBlank() ? DEFAULT_MODEL : model.trim()))
            .append("\",\"stream\":false,\"messages\":[");
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            if (i > 0) {
                json.append(',');
            }
            json.append("{\"role\":\"")
                .append(escape(message.role))
                .append("\",\"content\":\"")
                .append(escape(message.content))
                .append("\"}");
        }
        json.append("]}");
        return json.toString();
    }

    private String normalizeModel(String model) {
        return model == null || model.isBlank() ? DEFAULT_MODEL : model.trim();
    }

    private boolean modelMatches(String candidate, String wanted) {
        if (candidate == null) {
            return false;
        }
        return candidate.equals(wanted) || (!wanted.contains(":") && candidate.startsWith(wanted + ":"));
    }

    private String extractMessageContent(String json) {
        return extractJsonString(json, "content", json.indexOf("\"message\""));
    }

    private List<String> extractJsonStrings(String json, String key) {
        java.util.ArrayList<String> values = new java.util.ArrayList<>();
        int from = 0;
        while (from < json.length()) {
            String value = extractJsonString(json, key, from);
            if (value == null) {
                break;
            }
            values.add(value);
            int keyAt = json.indexOf("\"" + key + "\"", from);
            if (keyAt < 0) {
                break;
            }
            from = keyAt + key.length() + 2;
        }
        return values;
    }

    private String extractJsonString(String json, String key) {
        return extractJsonString(json, key, 0);
    }

    private String extractJsonString(String json, String key, int startAt) {
        int searchFrom = Math.max(0, startAt);
        int keyAt = json.indexOf("\"" + key + "\"", searchFrom);
        if (keyAt < 0) {
            return null;
        }
        int colonAt = json.indexOf(':', keyAt);
        if (colonAt < 0) {
            return null;
        }
        int quoteAt = json.indexOf('"', colonAt + 1);
        if (quoteAt < 0) {
            return null;
        }
        return readJsonString(json, quoteAt);
    }

    private long extractJsonLong(String json, String key) {
        int keyAt = json.indexOf("\"" + key + "\"");
        if (keyAt < 0) {
            return -1;
        }
        int colonAt = json.indexOf(':', keyAt);
        if (colonAt < 0) {
            return -1;
        }
        int start = colonAt + 1;
        while (start < json.length() && Character.isWhitespace(json.charAt(start))) {
            start++;
        }
        int end = start;
        while (end < json.length() && Character.isDigit(json.charAt(end))) {
            end++;
        }
        if (start == end) {
            return -1;
        }
        try {
            return Long.parseLong(json.substring(start, end));
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private String readJsonString(String json, int openingQuote) {
        StringBuilder value = new StringBuilder();
        for (int i = openingQuote + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '"') {
                return value.toString();
            }
            if (c == '\\' && i + 1 < json.length()) {
                char escaped = json.charAt(++i);
                switch (escaped) {
                    case '"' -> value.append('"');
                    case '\\' -> value.append('\\');
                    case '/' -> value.append('/');
                    case 'b' -> value.append('\b');
                    case 'f' -> value.append('\f');
                    case 'n' -> value.append('\n');
                    case 'r' -> value.append('\r');
                    case 't' -> value.append('\t');
                    case 'u' -> {
                        if (i + 4 < json.length()) {
                            String hex = json.substring(i + 1, i + 5);
                            try {
                                value.append((char) Integer.parseInt(hex, 16));
                            } catch (NumberFormatException ex) {
                                throw new UncheckedIOException(new IOException("Invalid JSON escape sequence."));
                            }
                            i += 4;
                        }
                    }
                    default -> value.append(escaped);
                }
            } else {
                value.append(c);
            }
        }
        return value.toString();
    }

    private String escape(String value) {
        StringBuilder escaped = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> escaped.append("\\\"");
                case '\\' -> escaped.append("\\\\");
                case '\b' -> escaped.append("\\b");
                case '\f' -> escaped.append("\\f");
                case '\n' -> escaped.append("\\n");
                case '\r' -> escaped.append("\\r");
                case '\t' -> escaped.append("\\t");
                default -> {
                    if (c < 0x20) {
                        escaped.append(String.format("\\u%04x", (int) c));
                    } else {
                        escaped.append(c);
                    }
                }
            }
        }
        return escaped.toString();
    }

    private String compact(String body) {
        if (body == null) {
            return "";
        }
        String text = body.replaceAll("\\s+", " ").trim();
        return text.length() <= 300 ? text : text.substring(0, 300) + "...";
    }
}

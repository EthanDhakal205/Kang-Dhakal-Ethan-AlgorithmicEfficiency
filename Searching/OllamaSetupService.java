import java.awt.Desktop;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

class OllamaSetupService {
    static final String DOWNLOAD_PAGE = "https://ollama.com/download";
    static final String WINDOWS_INSTALLER_URL = "https://ollama.com/download/OllamaSetup.exe";

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(15))
        .build();

    boolean isWindows() {
        return osName().contains("win");
    }

    boolean isMac() {
        return osName().contains("mac");
    }

    boolean isLinux() {
        String os = osName();
        return os.contains("nux") || os.contains("nix");
    }

    boolean isOllamaInstalled() {
        return findOllamaExecutable() != null || commandWorks("ollama", "--version");
    }

    void startOllamaIfPossible() throws IOException {
        Path executable = findOllamaExecutable();
        ProcessBuilder builder = executable == null
            ? new ProcessBuilder("ollama", "serve")
            : new ProcessBuilder(executable.toString(), "serve");
        builder.redirectErrorStream(true);
        builder.start();
    }

    Path downloadWindowsInstaller(Consumer<String> progress) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(URI.create(WINDOWS_INSTALLER_URL))
            .timeout(Duration.ofMinutes(20))
            .GET()
            .build();
        HttpResponse<InputStream> response = httpClient.send(request, HttpResponse.BodyHandlers.ofInputStream());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("Download failed with HTTP " + response.statusCode());
        }

        long total = response.headers().firstValueAsLong("content-length").orElse(-1L);
        Path directory = Files.createTempDirectory("ollama-setup");
        Path installer = directory.resolve("OllamaSetup.exe");
        byte[] buffer = new byte[128 * 1024];
        long downloaded = 0L;

        try (InputStream input = response.body();
             OutputStream output = Files.newOutputStream(installer)) {
            int read;
            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
                downloaded += read;
                progress.accept(downloadProgress(downloaded, total));
            }
        }

        return installer;
    }

    void launchInstaller(Path installer) throws IOException {
        new ProcessBuilder(installer.toAbsolutePath().toString()).start();
    }

    void openDownloadPage() throws IOException {
        if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            throw new IOException("Desktop browsing is not available on this computer.");
        }
        Desktop.getDesktop().browse(URI.create(DOWNLOAD_PAGE));
    }

    String platformInstructions() {
        if (isWindows()) {
            return "This will download the official Windows installer and open it. Finish the installer, then click Check again.";
        }
        if (isMac()) {
            return "macOS users should install Ollama from the official download page, then click Check again.";
        }
        if (isLinux()) {
            return "Linux users should install Ollama from the official download page or run the official shell installer, then click Check again.";
        }
        return "Install Ollama from the official download page, then click Check again.";
    }

    private Path findOllamaExecutable() {
        if (isWindows()) {
            String localAppData = System.getenv("LOCALAPPDATA");
            if (localAppData != null && !localAppData.isBlank()) {
                Path candidate = Paths.get(localAppData, "Programs", "Ollama", "ollama.exe");
                if (Files.isRegularFile(candidate)) {
                    return candidate;
                }
            }
        }
        return null;
    }

    private boolean commandWorks(String... command) {
        try {
            Process process = new ProcessBuilder(command)
                .redirectErrorStream(true)
                .start();
            return process.waitFor(5, TimeUnit.SECONDS) && process.exitValue() == 0;
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            return false;
        }
    }

    private String downloadProgress(long downloaded, long total) {
        long downloadedMb = downloaded / (1024 * 1024);
        if (total <= 0) {
            return "Downloading installer: " + downloadedMb + " MB";
        }
        long totalMb = Math.max(1, total / (1024 * 1024));
        int percent = (int) Math.min(100, Math.round(downloaded * 100.0 / total));
        return "Downloading installer: " + percent + "% (" + downloadedMb + " / " + totalMb + " MB)";
    }

    private String osName() {
        return System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
    }
}

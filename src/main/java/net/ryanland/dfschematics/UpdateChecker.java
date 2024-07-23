package net.ryanland.dfschematics;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Logger;

/**
 * Checks for updates on a GitHub repository
 */
public class UpdateChecker {

    private final String currentVersion;
    private final URL url;
    private final boolean disabled;

    private transient CompletableFuture<String> latestVersionFuture = null;

    /**
     * Start the program with <code>-Dtechnicjelle.updatechecker.disabled</code> to disable the update checker
     *
     * @param author         GitHub Username
     * @param repoName       GitHub Repository Name
     * @param currentVersion Current version of the program. This must be in the same format as the version tags on GitHub
     */
    public UpdateChecker(@NotNull String author, @NotNull String repoName, @NotNull String currentVersion) {
        this.currentVersion = removePrefix(currentVersion);
        this.disabled = System.getProperty("technicjelle.updatechecker.disabled") != null;
        try {
            this.url = new URL("https://github.com/" + author + "/" + repoName + "/releases/latest");
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks for updates from a GitHub repository's releases<br>
     * <i>This method blocks the thread it is called from</i>
     *
     * @see #checkAsync()
     */
    public void check() {
        checkAsync();
        latestVersionFuture.join();
    }

    /**
     * Checks for updates from a GitHub repository's releases<br>
     * <i>This method does not block the thread it is called from</i>
     *
     * @see #check()
     */
    public void checkAsync() {
        latestVersionFuture = CompletableFuture.supplyAsync(this::fetchLatestVersion);
    }

    /**
     * Checks if necessary and returns the latest available version
     *
     * @return the latest available version
     */
    public synchronized String getLatestVersion() {
        if (latestVersionFuture == null) checkAsync();
        return latestVersionFuture.join();
    }

    private String fetchLatestVersion() {
        if (disabled) return currentVersion;
        try {
            // Connect to GitHub website
            HttpURLConnection con;
            con = (HttpURLConnection) url.openConnection();
            con.setInstanceFollowRedirects(false);

            // Check if the response is a redirect
            String newUrl = con.getHeaderField("Location");

            if (newUrl == null) {
                throw new IOException("Did not get a redirect");
            }

            // Get the latest version tag from the redirect url
            String[] split = newUrl.split("/");
            return removePrefix(split[split.length - 1]);
        } catch (IOException ex) {
            throw new CompletionException("Exception trying to fetch the latest version", ex);
        }
    }

    /**
     * Checks if necessary and returns whether an update is available or not
     *
     * @return <code>true</code> if there is an update available or <code>false</code> otherwise.
     */
    public boolean isUpdateAvailable() {
        return !getLatestVersion().equals(currentVersion);
    }

    /**
     * Checks if necessary and returns a message if an update is available.<br>
     * The message will contain the latest version and a link to the GitHub releases page.<br>
     * Useful if you don't use Java's own {@link java.util.logging.Logger} and you want to use your own.<br>
     * Example:<br>
     * <code>New version available: v2.5 (current: v2.4)<br>
     * Download it at <a href="https://github.com/TechnicJelle/UpdateCheckerJava/releases/latest">https://github.com/TechnicJelle/UpdateCheckerJava/releases/latest</a></code>
     *
     * @return An optional containing the update message or an empty optional if there is no update available
     */
    public Optional<String> getUpdateMessage() {
        if (isUpdateAvailable()) {
            return Optional.of("New version available: v" + getLatestVersion() + " (current: v" + currentVersion + ")\nDownload it at " + url);
        }
        return Optional.empty();
    }

    /**
     * Gets the current version of the program.<br>
     * Useful in case you want to log a custom message.<br>
     * <br>
     * Does not actually check for updates
     *
     * @return The current version of the program
     */
    public String getCurrentVersion() {
        return currentVersion;
    }

    /**
     * Gets the URL to the GitHub releases page,
     * where the latest version can be downloaded.<br>
     * Useful in case you want to log a custom message.<br>
     * <br>
     * Does not actually check for updates
     *
     * @return The URL to the GitHub releases page
     */
    public String getUpdateUrl() {
        return url.toString();
    }

    /**
     * This method logs a message to the console if an update is available<br>
     *
     * @param logger Logger to log a potential update notification to
     */
    public void logUpdateMessage(@NotNull Logger logger) {
        getUpdateMessage().ifPresent(logger::warning);
    }

    /**
     * This method logs a message to the console if an update is available, asynchronously<br>
     *
     * @param logger Logger to log a potential update notification to
     */
    public synchronized void logUpdateMessageAsync(@NotNull Logger logger) {
        if (latestVersionFuture == null) checkAsync();
        latestVersionFuture.thenRun(() -> logUpdateMessage(logger));
    }

    /**
     * Removes a potential <code>v</code> prefix from a version
     *
     * @param version Version to remove the prefix from
     * @return The version without the prefix
     */
    @Contract(pure = true)
    private static @NotNull String removePrefix(@NotNull String version) {
        return version.replaceFirst("^v", "");
    }
}
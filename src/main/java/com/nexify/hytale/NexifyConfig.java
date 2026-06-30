package com.nexify.hytale;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Loads config.json from the plugin's data folder, copying the bundled
 * default on first run.
 */
public class NexifyConfig {

    public String apiBaseUrl = "https://api.nexify.gg";
    public String apiToken = "";
    public int pollIntervalSeconds = 10;

    public static NexifyConfig load(Path dataFolder) throws IOException {
        Path configPath = dataFolder.resolve("config.json");

        if (!Files.exists(configPath)) {
            Files.createDirectories(dataFolder);
            try (InputStreamReader defaultConfig = new InputStreamReader(
                    NexifyConfig.class.getResourceAsStream("/config.json"), StandardCharsets.UTF_8)) {
                Files.writeString(configPath, new Gson().toJson(new Gson().fromJson(defaultConfig, NexifyConfig.class)));
            }
        }

        try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
            return new Gson().fromJson(reader, NexifyConfig.class);
        }
    }
}

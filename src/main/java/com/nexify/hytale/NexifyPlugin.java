package com.nexify.hytale;

import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import javax.annotation.Nonnull;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Polls the Nexify store-delivery API and applies purchases in-game.
 *
 * Pontos pendentes (ver README.md):
 *  - PlayerLookup real (achar jogador online pelo nome)
 *  - Dar/remover item no inventário
 *  - Adicionar/remover grupo de permissão
 *  - Notificação in-game (provavelmente via EventTitleUtil, já confirmado existir)
 */
public class NexifyPlugin extends JavaPlugin {

    private static NexifyPlugin instance;

    private NexifyConfig config;
    private NexifyApiClient apiClient;
    private DeliveryDispatcher dispatcher;
    private ScheduledExecutorService scheduler;

    public NexifyPlugin(@Nonnull JavaPluginInit init) {
        super(init);
        instance = this;
        getLogger().at(Level.INFO).log("[Nexify] Plugin loaded!");
    }

    public static NexifyPlugin getInstance() {
        return instance;
    }

    @Override
    protected void setup() {
        try {
            // TODO: confirm the real method to resolve the plugin's data folder.
            Path dataFolder = Path.of("plugins", "NexifyHytale");
            this.config = NexifyConfig.load(dataFolder);
        } catch (Exception e) {
            getLogger().at(Level.SEVERE).withCause(e).log("[Nexify] Falha ao carregar config.json");
            return;
        }

        if (config.apiToken == null || config.apiToken.isBlank() || config.apiToken.equals("PASTE_YOUR_STORE_TOKEN_HERE")) {
            getLogger().at(Level.WARNING).log("[Nexify] apiToken não configurado em config.json — entregas desativadas.");
            return;
        }

        NexifyLog log = new NexifyLog() {
            @Override public void info(String message) { getLogger().at(Level.INFO).log(message); }
            @Override public void warn(String message) { getLogger().at(Level.WARNING).log(message); }
            @Override public void warn(String message, Throwable cause) { getLogger().at(Level.WARNING).withCause(cause).log(message); }
        };

        this.apiClient = new NexifyApiClient(config, log);

        // TODO: replace with the real online-player lookup once the Hytale player
        // service API is known (e.g. server.getPlayerManager().getOnlinePlayer(name)).
        DeliveryDispatcher.PlayerLookup playerLookup = playerName -> null;

        this.dispatcher = new DeliveryDispatcher(log, playerLookup);

        registerEvents();
        registerCommands();
    }

    @Override
    protected void start() {
        getLogger().at(Level.INFO).log("[Nexify] Plugin enabled! Polling every " + config.pollIntervalSeconds + "s");

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(this::pollAndDeliver, 0, config.pollIntervalSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void shutdown() {
        getLogger().at(Level.INFO).log("[Nexify] Plugin disabled!");
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    private void pollAndDeliver() {
        if (apiClient == null || dispatcher == null) return;

        List<PendingDelivery> deliveries = apiClient.fetchPendingDeliveries();
        for (PendingDelivery delivery : deliveries) {
            try {
                boolean handled = dispatcher.dispatch(delivery);
                if (handled) {
                    apiClient.completeDelivery(delivery.deliveryId);
                }
                // if not handled (player offline), it stays pending and is retried
                // on the next poll automatically — no local queue needed.
            } catch (Exception e) {
                getLogger().at(Level.SEVERE).withCause(e).log("[Nexify] Erro processando entrega " + delivery.deliveryId);
            }
        }
    }

    private void registerEvents() {
        // TODO: register a PlayerJoinEvent listener to call pollAndDeliver() immediately
        // when a player logs in, so they don't wait up to pollIntervalSeconds for items
        // bought while offline.
    }

    private void registerCommands() {
        // No player-facing commands needed yet; everything is driven by the API poll.
    }
}

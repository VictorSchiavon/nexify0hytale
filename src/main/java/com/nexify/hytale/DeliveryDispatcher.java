package com.nexify.hytale;

/**
 * Routes one PendingDelivery to the right in-game action based on its
 * "fivemarket:xxx" command name, mirroring the contract already used by
 * the Minecraft plugin:
 *
 *   addItem / removeItem               -> value = "itemId:amount"
 *   addCar / removeCar                 -> value = "carId"            (no Hytale equivalent yet, no-op + warn)
 *   addCarTemporary                    -> value = "carId:minutes"    (no Hytale equivalent yet, no-op + warn)
 *   addGroup / removeGroup             -> value = "groupName"
 *   addHouse / removeHouse             -> value = "houseId"
 *   addHouseTemporary                  -> value = "houseId:minutes"
 *
 * Every branch marked TODO needs the real Hytale API call once the server
 * source / javadocs are available — see README.md "Pontos pendentes".
 */
public class DeliveryDispatcher {

    private final NexifyLog log;
    // TODO: replace with the real Hytale player-lookup service once known,
    // e.g. something like server.getPlayerManager().getOnlinePlayer(name).
    private final PlayerLookup playerLookup;

    public DeliveryDispatcher(NexifyLog log, PlayerLookup playerLookup) {
        this.log = log;
        this.playerLookup = playerLookup;
    }

    /**
     * @return true if the delivery was handled (and should be ack'd to the API),
     *         false if it should be retried on the next poll (e.g. player offline).
     */
    public boolean dispatch(PendingDelivery delivery) {
        Object player = playerLookup.findOnline(delivery.player); // null if offline
        String commandName = delivery.commandName();
        String value = delivery.value;

        switch (commandName) {
            case "addItem":
                return withOnlinePlayer(player, delivery, p -> giveItem(p, value));
            case "removeItem":
                return withOnlinePlayer(player, delivery, p -> removeItem(p, value));
            case "addGroup":
                return withOnlinePlayer(player, delivery, p -> addGroup(p, value));
            case "removeGroup":
                return withOnlinePlayer(player, delivery, p -> removeGroup(p, value));
            case "addHouse":
                return withOnlinePlayer(player, delivery, p -> addHouse(p, value));
            case "removeHouse":
                return withOnlinePlayer(player, delivery, p -> removeHouse(p, value));
            case "addHouseTemporary":
                return withOnlinePlayer(player, delivery, p -> addHouseTemporary(p, value));
            case "addCar":
            case "removeCar":
            case "addCarTemporary":
                // Hytale has no vehicles at launch. Ack so it doesn't loop forever,
                // but log loudly so store owners notice misconfigured products.
                log.warn("[Nexify] Comando '" + delivery.command + "' não tem equivalente em Hytale (sem veículos). Ignorado para " + delivery.player);
                return true;
            default:
                log.warn("[Nexify] Comando desconhecido: " + delivery.command + " (delivery " + delivery.deliveryId + ")");
                return true; // ack unknown commands instead of looping forever
        }
    }

    private boolean withOnlinePlayer(Object player, PendingDelivery delivery, java.util.function.Consumer<Object> action) {
        if (player == null) {
            // Player offline: leave delivery pending, it'll be retried on a future poll
            // (and ideally re-attempted right away on PlayerJoinEvent — see NexifyPlugin).
            return false;
        }
        action.accept(player);
        notifyPlayer(player, delivery);
        return true;
    }

    private void notifyPlayer(Object player, PendingDelivery delivery) {
        // TODO: wire to EventTitleUtil.showEventTitleToPlayer(playerRef, title, subtitle, major)
        // as seen in the official command example, once `player` is the real Player type.
        log.info("[Nexify] TODO: notificar " + delivery.player + " sobre entrega " + delivery.deliveryId);
    }

    // ---- Item -------------------------------------------------------------

    private void giveItem(Object player, String value) {
        String[] parts = value.split(":", 2);
        String itemId = parts[0];
        int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
        // TODO: call Hytale's inventory API, e.g. player.getInventory().addItem(itemId, amount)
        log.info("[Nexify] TODO: dar item " + itemId + " x" + amount);
    }

    private void removeItem(Object player, String value) {
        String[] parts = value.split(":", 2);
        String itemId = parts[0];
        int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
        // TODO: call Hytale's inventory API, e.g. player.getInventory().removeItem(itemId, amount)
        log.info("[Nexify] TODO: remover item " + itemId + " x" + amount);
    }

    // ---- Groups / permissions ----------------------------------------------

    private void addGroup(Object player, String groupName) {
        // TODO: call Hytale's native permission/group API once documented.
        log.info("[Nexify] TODO: adicionar grupo " + groupName);
    }

    private void removeGroup(Object player, String groupName) {
        // TODO: call Hytale's native permission/group API once documented.
        log.info("[Nexify] TODO: remover grupo " + groupName);
    }

    // ---- Houses / land claims ------------------------------------------------

    private void addHouse(Object player, String houseId) {
        // TODO: no public Hytale land-claim API yet; depends on server's own plugin.
        log.info("[Nexify] TODO: liberar casa " + houseId);
    }

    private void removeHouse(Object player, String houseId) {
        log.info("[Nexify] TODO: remover casa " + houseId);
    }

    private void addHouseTemporary(Object player, String value) {
        String[] parts = value.split(":", 2);
        String houseId = parts[0];
        int minutes = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        log.info("[Nexify] TODO: liberar casa " + houseId + " por " + minutes + " minutos");
    }

    /** Abstraction over Hytale's (currently undocumented) online-player lookup. */
    public interface PlayerLookup {
        Object findOnline(String playerName);
    }
}

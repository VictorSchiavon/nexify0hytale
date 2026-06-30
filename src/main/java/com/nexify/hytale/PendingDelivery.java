package com.nexify.hytale;

/**
 * Mirrors one entry returned by GET /store/deliveries/pending/:api_token
 *
 * ASSUMPTION: field names below are inferred from the command log format
 * shared by the user ("Executando comando [cmd_xxx]: fivemarket:addItem | Valor: water:23")
 * and from the Minecraft plugin contract. Confirm against a real response
 * from https://api.nexify.gg/store/test/reset/:api_token before relying on this.
 */
public class PendingDelivery {
    public String deliveryId;
    public String player;
    public String command;
    public String value;

    /** e.g. "fivemarket:addItem" -> "addItem" */
    public String commandName() {
        if (command == null) return "";
        int idx = command.indexOf(':');
        return idx >= 0 ? command.substring(idx + 1) : command;
    }
}

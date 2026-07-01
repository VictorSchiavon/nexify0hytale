package com.nexify.hytale;

import java.util.List;

/**
 * Formato real confirmado via API:
 * {
 *   "hasPending": true,
 *   "deliveryId": "uuid",
 *   "data": {
 *     "buyer": "NickDoJogador",
 *     "variable": {
 *       "commands": ["give NickDoJogador sword 1", "say NickDoJogador recebeu!"]
 *     }
 *   }
 * }
 */
public class PendingDelivery {
    public boolean hasPending;
    public String deliveryId;
    public Data data;

    public static class Data {
        public String buyer;
        public Variable variable;
    }

    public static class Variable {
        public List<Command> commands;
    }

    /** Cada comando vem como objeto {id, status_payment, command, command_value} da API */
    public static class Command {
        public String id;
        public String status_payment;
        public String command;
        public String command_value;

        public String toCommandString() {
            if ("fivemarket:addItem".equals(command)) {
                // "ItemName:Quantity" → "give {player} ItemName --quantity=Quantity"
                String[] parts = (command_value != null ? command_value : "").split(":");
                String item = parts.length > 0 ? parts[0] : "";
                String qty  = parts.length > 1 ? parts[1] : "1";
                return "give {player} " + item + " --quantity=" + qty;
            }
            if (command_value != null && !command_value.isBlank()) {
                return command + " " + command_value;
            }
            return command;
        }
    }

    public List<String> getCommands() {
        if (data == null || data.variable == null || data.variable.commands == null)
            return List.of();
        List<String> result = new java.util.ArrayList<>();
        for (Command cmd : data.variable.commands) {
            if (cmd != null && cmd.command != null) {
                result.add(cmd.toCommandString());
            }
        }
        return result;
    }

    public String getBuyer() {
        return data != null ? data.buyer : null;
    }
}

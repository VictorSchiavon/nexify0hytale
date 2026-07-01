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

    /** Cada comando vem como objeto {id, command, argument} da API */
    public static class Command {
        public String id;
        public String command;
        public String argument;

        public String toCommandString() {
            if (argument != null && !argument.isBlank()) {
                return command + " " + argument;
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

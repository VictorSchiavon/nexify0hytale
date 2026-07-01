package com.nexify.hytale;

import java.util.List;

/**
 * Executa os comandos de um delivery diretamente no servidor Hytale.
 * A API retorna strings de comando prontas — basta executar cada uma.
 */
public class DeliveryDispatcher {

    private final NexifyLog log;
    private final CommandExecutor commandExecutor;

    public DeliveryDispatcher(NexifyLog log, CommandExecutor commandExecutor) {
        this.log = log;
        this.commandExecutor = commandExecutor;
    }

    /**
     * @return true se o delivery foi processado (deve ser confirmado na API),
     *         false se deve ser tentado novamente.
     */
    public boolean dispatch(PendingDelivery delivery) {
        List<String> commands = delivery.getCommands();
        String buyer = delivery.getBuyer();
        String playerName = resolvePlayerName(buyer);

        if (commands.isEmpty()) {
            log.warn("[Nexify] Delivery " + delivery.deliveryId + " sem comandos para executar.");
            return true; // confirmar mesmo assim para não ficar preso
        }

        log.info("[Nexify] Executando " + commands.size() + " comando(s) para " + playerName + " (delivery " + delivery.deliveryId + ")");

        for (String raw : commands) {
            String command = raw
                    .replace("{player}", playerName)
                    .replace("{buyer}", playerName)
                    .replace("{nick}", playerName);
            try {
                commandExecutor.execute(command);
                log.info("[Nexify] Executado: " + command);
            } catch (Exception e) {
                log.warn("[Nexify] Erro ao executar comando '" + command + "': " + e.getMessage(), e);
            }
        }

        return true;
    }

    /** Extrai o nick do jogador — buyer pode ser nick puro ou JSON {"name":"...","email":"..."} */
    private String resolvePlayerName(String buyer) {
        if (buyer == null || buyer.isBlank()) return "unknown";
        if (buyer.startsWith("{")) {
            try {
                // Extrai o campo "name" do JSON manualmente (sem dependência extra)
                int nameIdx = buyer.indexOf("\"name\"");
                if (nameIdx >= 0) {
                    int colon = buyer.indexOf(':', nameIdx);
                    int open  = buyer.indexOf('"', colon + 1);
                    int close = buyer.indexOf('"', open + 1);
                    if (open >= 0 && close > open) {
                        return buyer.substring(open + 1, close);
                    }
                }
            } catch (Exception ignored) {}
        }
        return buyer;
    }

    /** Interface para execução de comandos no servidor. */
    public interface CommandExecutor {
        void execute(String command);
    }
}

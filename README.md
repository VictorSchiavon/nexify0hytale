# Nexify Hytale Plugin

Plugin Hytale (Java) que entrega compras feitas na Nexify dentro do jogo, no
mesmo modelo do plugin de Minecraft: ele faz **polling** na API da loja e
aplica cada entrega pendente.

## Como funciona

1. A cada `pollIntervalSeconds` (padrão 10s), o plugin chama:
   `GET {apiBaseUrl}/store/deliveries/pending/{apiToken}`
2. Para cada entrega retornada, identifica o comando (`fivemarket:addItem`,
   `removeGroup`, etc.), tenta achar o jogador online e aplica a ação.
3. Se a ação for aplicada com sucesso, chama:
   `POST {apiBaseUrl}/store/deliveries/complete/{apiToken}` com
   `{"deliveryId": "..."}`.
4. Se o jogador estiver offline, a entrega **não** é confirmada — ela
   continua pendente na API e será reprocessada no próximo poll
   automaticamente (sem precisar de fila local).

## Configuração

Edite `plugins/NexifyHytale/config.json` (gerado automaticamente no primeiro
boot a partir do `config.json` empacotado):

```json
{
  "apiBaseUrl": "https://api.nexify.gg",
  "apiToken": "TOKEN_DA_LOJA_AQUI",
  "pollIntervalSeconds": 10
}
```

Cada cliente/loja usa o **próprio token**, então o mesmo `.jar` serve para
todos os servidores — só muda o `config.json`.

## Build

```bash
git clone <este-repo>
cd nexify-hytale
./gradlew shadowJar
```

Jar final em `build/libs/nexify-hytale-1.0.0.jar`. Requer Java 25 JDK
(mesma versão usada no template oficial da Hypixel Studios).

> Antes de buildar, coloque o `HytaleServer.jar` real na raiz do projeto
> (ele é a dependência `compileOnly` declarada no `build.gradle` — não é
> redistribuído, então não está incluso aqui).

## Pontos pendentes (precisam da API real do servidor Hytale)

A documentação pública de modding ainda não cobre essas APIs (o código-fonte
do servidor não foi liberado até agora). Cada um está marcado com `TODO` no
código:

| O quê | Onde | Status |
|---|---|---|
| Achar jogador online pelo nome | `NexifyPlugin.playerLookup` | placeholder retorna sempre `null` |
| Dar/remover item do inventário | `DeliveryDispatcher.giveItem/removeItem` | só loga, não executa |
| Adicionar/remover grupo de permissão | `DeliveryDispatcher.addGroup/removeGroup` | só loga, não executa |
| Notificação in-game de entrega | `DeliveryDispatcher.notifyPlayer` | só loga; provavelmente usar `EventTitleUtil.showEventTitleToPlayer(...)` (já confirmado existir na API de comandos) |
| Liberar/remover casa | `DeliveryDispatcher.addHouse/removeHouse/addHouseTemporary` | sem API pública de claims ainda |
| Retry imediato ao logar | `NexifyPlugin.registerEvents` | falta o evento real de `PlayerJoinEvent` |

`addCar` / `removeCar` / `addCarTemporary` são reconhecidos mas **ignorados
com aviso no log**, já que Hytale não tem veículos.

## Formato assumido da resposta da API

Como a doc não foi compartilhada, o formato abaixo foi inferido a partir do
log de exemplo (`Executando comando [cmd_xxx]: fivemarket:addItem | Valor: water:23`).
**Confirme contra uma chamada real** (use `https://api.nexify.gg/store/test/reset/:api_token`
para gerar dados mock) antes de ir para produção:

```json
[
  {
    "deliveryId": "cmd_1775518258303",
    "player": "NickDoJogador",
    "command": "fivemarket:addItem",
    "value": "water:23"
  }
]
```

Se a API real envolver o array em `{"data": [...]}` ou usar outros nomes de
campo, ajuste `PendingDelivery.java` e o `TypeToken` em `NexifyApiClient.java`.

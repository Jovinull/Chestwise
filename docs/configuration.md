# Configuration

The server/common configuration is `config/chestwise-server.properties`. It is
created with safe defaults on first start. An invalid file is rejected as one
unit and defaults are used; partial unsafe values are never applied.

| Property | Default | Valid range | Meaning |
| --- | ---: | ---: | --- |
| `scanRadius` | 16 | 1–64 | Discovery distance on each coordinate axis. |
| `maxInventories` | 128 | 1–1024 | Maximum indexed physical sources per terminal. |
| `discoveryIntervalTicks` | 100 | 20–72000 | Controlled block-entity rediscovery interval. |
| `inventoriesPolledPerTick` | 8 | 1–1024 | Bounded polling budget per terminal tick. |
| `interactionDistance` | 8.0 | 1–64 | Maximum player distance for terminal mutations. |

Twenty game ticks are approximately one second under normal server load. A
larger radius increases the number of loaded chunks inspected during discovery;
it does not load missing chunks. Increase limits conservatively on servers with
many terminals.

Client-only preferences are stored in `config/chestwise-client.properties`:

| Property | Default | Values |
| --- | --- | --- |
| `sortMode` | `quantity` | `quantity`, `name`, `namespace`, `registry` |
| `mouseWheelTransfer` | `false` | `true`, `false` |
| `protectedSlots` | empty | comma-separated slot indexes from `0` to `35` |

The sort button persists `sortMode`. Wheel transfer is intentionally opt-in and
can currently be enabled by editing the file while the client is stopped.

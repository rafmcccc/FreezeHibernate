# FreezeHibernate

A Paper plugin that freezes the server's tick loop whenever no players are online, cutting CPU usage to near idle and reducing memory pressure.

Based on the original work by [Malfrador](https://github.com/Malfrador/FreezeHibernate), maintained in this fork by rafmcccc.

## How it works

While frozen the server performs zero ticks per second: no entity updates, no block ticks, no daylight cycle. Network connections stay alive, so players can join at any time and the server unfreezes the moment someone does.

The plugin freezes on:

- Server startup, if nobody is online
- The last player disconnecting

It unfreezes when a player joins. If the server is manually unfrozen with `/tick unfreeze` while empty, a periodic check re-freezes it within the configured interval, so the server never runs unthrottled with zero players for long.

## Requirements

- Paper (or a fork) 1.20.6 or newer
- Java 21

## Installation

Drop the jar from `build/libs/` into your server's `plugins/` folder and restart the server.

## Configuration

`plugins/FreezeHibernate/config.yml`:

```yaml
gc-on-freeze: true
check-interval-seconds: 60

messages:
  frozen-on-empty: "Last player disconnected. Server is now frozen."
```

- `gc-on-freeze`: requests a full garbage collection after freezing to reclaim unused memory. Note that this frees unused objects but does not shrink the heap the JVM has committed; committed memory is controlled by JVM startup flags, which most managed hosts do not let you change.
- `check-interval-seconds`: how often, in seconds, the plugin checks whether an empty server needs re-freezing. Set to `0` to disable.
- `messages.frozen-on-empty`: message logged to the console when the last player leaves.

## Commands and permissions

| Command | Description | Permission |
| --- | --- | --- |
| `/hibernate status` | Show whether the server is currently frozen | `freezehibernate.admin` |
| `/hibernate freeze` | Manually freeze the server (only when empty) | `freezehibernate.admin` |
| `/hibernate unfreeze` | Manually unfreeze the server | `freezehibernate.admin` |

The `freezehibernate.admin` permission defaults to operators.

## Hosting notes

- Lowering `view-distance` and `simulation-distance` in `server.properties` reduces memory usage while playing and lets the server settle into its frozen state faster after everyone leaves.
- Most managed hosts control the Java startup command, so custom JVM flags such as `-Xms`, `-Xmx`, or garbage collector selection are usually not available there. The tick freeze itself already removes almost all CPU load while hibernating.
- Check your host's panel for your allocated RAM and CPU limits if you are unsure what you are working with.

## Building

```sh
sh gradlew build
```

The jar is written to `build/libs/`.

## License

MIT. See [LICENSE](LICENSE).

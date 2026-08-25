package de.malfrador.freezehibernate;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.ServerTickManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerLoadEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public final class FreezeHibernate extends JavaPlugin implements Listener, TabExecutor {

    private ServerTickManager serverTickManager;
    private Server server;
    private boolean gcOnFreeze;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        gcOnFreeze = getConfig().getBoolean("gc-on-freeze", true);
        server = Bukkit.getServer();
        serverTickManager = server.getServerTickManager();
        Bukkit.getPluginManager().registerEvents(this, this);
        if (getCommand("hibernate") != null) {
            getCommand("hibernate").setExecutor(this);
            getCommand("hibernate").setTabCompleter(this);
        }
        int interval = Math.max(0, getConfig().getInt("check-interval-seconds", 60));
        if (interval > 0) {
            long ticks = interval * 20L;
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (server.getOnlinePlayers().isEmpty() && !serverTickManager.isFrozen()) {
                        freeze("No players online and server was not frozen. Freezing now.");
                    }
                }
            }.runTaskTimer(this, ticks, ticks);
        }
        getLogger().info(serverTickManager.isFrozen()
                ? "Server is currently frozen."
                : "Server is currently running.");
    }

    private void freeze(String message) {
        serverTickManager.setFrozen(true);
        getLogger().info(message);
        if (gcOnFreeze) {
            System.gc();
        }
    }

    private void unfreeze(String message) {
        serverTickManager.setFrozen(false);
        getLogger().info(message);
    }

    @EventHandler
    private void onQuit(PlayerQuitEvent event) {
        if (server.getOnlinePlayers().size() == 1) {
            freeze(getConfig().getString("messages.frozen-on-empty",
                    "Last player disconnected. Server is now frozen."));
        }
    }

    @EventHandler
    private void onJoin(PlayerJoinEvent event) {
        if (serverTickManager.isFrozen()) {
            unfreeze("A player joined. Server is now unfrozen.");
        }
    }

    @EventHandler
    private void onStartComplete(ServerLoadEvent event) {
        if (event.getType() == ServerLoadEvent.LoadType.RELOAD) {
            return;
        }
        if (server.getOnlinePlayers().isEmpty()) {
            freeze("Server is frozen until a player joins.");
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("freezehibernate.admin")) {
            sender.sendMessage("You do not have permission to use this command.");
            return true;
        }
        if (args.length != 1) {
            sender.sendMessage("Usage: /hibernate <status|freeze|unfreeze>");
            return true;
        }
        switch (args[0].toLowerCase()) {
            case "status" -> sender.sendMessage(serverTickManager.isFrozen()
                    ? "Server is frozen (zero ticks per second)."
                    : "Server is running.");
            case "freeze" -> {
                if (server.getOnlinePlayers().isEmpty()) {
                    freeze("Server manually frozen via /hibernate freeze.");
                } else {
                    sender.sendMessage("Cannot freeze while players are online.");
                }
            }
            case "unfreeze" -> unfreeze("Server manually unfrozen via /hibernate unfreeze.");
            default -> sender.sendMessage("Usage: /hibernate <status|freeze|unfreeze>");
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("freezehibernate.admin")) {
            return List.of();
        }
        if (args.length == 1) {
            return List.of("status", "freeze", "unfreeze").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        return List.of();
    }
}

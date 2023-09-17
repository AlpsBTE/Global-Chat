package com.alpsbte.globalchat;

import me.clip.placeholderapi.*;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.logging.Level;

public class GlobalChat extends JavaPlugin implements Listener, PluginMessageListener {

    private static GlobalChat plugin;

    private FileConfiguration config;
    private File configFile;

    private String serverName;
    private boolean enableGlobalChat;
    private String socketIP;
    private int socketPort;

    @Override
    public void onEnable() {
        plugin = this;

        Objects.requireNonNull(this.getCommand("gcreload")).setExecutor(new CMD_Reload());
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getMessenger().registerIncomingPluginChannel(this, "alpsbte:globalchat", this);

        reloadConfig();

        this.getLogger().log(Level.INFO, "Successfully enabled AlpsBTE-GlobalChat plugin!");
    }

    @Override
    public void onDisable() {
        this.getServer().getMessenger().unregisterIncomingPluginChannel(this, "alpsbte:globalchat");
        super.onDisable();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerChatEvent(AsyncPlayerChatEvent event) {
        event.setFormat(getFormattedMessage(event.getPlayer(), event.getMessage()));
        if (enableGlobalChat) broadcastPlayerMessage(event.getPlayer(), event.getMessage());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoinEvent(PlayerJoinEvent event) {
        event.setJoinMessage(null);
        Bukkit.broadcastMessage("§7[§6+§7] » " + event.getPlayer().getName());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerLeaveEvent(PlayerQuitEvent event) {
        event.setQuitMessage(null);
        Bukkit.broadcastMessage("§7[§c-§7] » " + event.getPlayer().getName());
    }

    @Override
    public void onPluginMessageReceived(String channel, @NotNull Player player, byte[] message) {
        if (!channel.equals("alpsbte:globalchat")) {
            return;
        }

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));
        try {
            Bukkit.broadcastMessage(in.readUTF());
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not broadcast received socket message!", ex);
        }
    }

    public void broadcastPlayerMessage(Player player, String message) {
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            try (Socket socket = new Socket(socketIP, socketPort)) {
                OutputStream output = socket.getOutputStream();
                ObjectOutputStream objectOutput = new ObjectOutputStream(output);

                // Send player message
                objectOutput.writeObject(getFormattedMessage(player, message));
                objectOutput.writeObject(serverName);
                objectOutput.flush();

                objectOutput.close();
            } catch (IOException ex) {
                getLogger().log(Level.SEVERE, "Could not broadcast message to server socket!", ex);
            }
        });
    }

    public String getFormattedMessage(Player player, String message) {
        return ChatColor.translateAlternateColorCodes('&', PlaceholderAPI.setPlaceholders(player, "§8[§a" + player.getWorld().getName().substring(0,1).toUpperCase() + "§8] [%luckperms_prefix%§8]&7 %player_name% &8» &7") + message);
    }

    @Override
    public void reloadConfig() {
        configFile = new File(getDataFolder(), "config.yml");
        if (configFile.exists()) {
            config = YamlConfiguration.loadConfiguration(configFile);
        } else {
            // Look for default configuration file
            Reader defConfigStream = new InputStreamReader(Objects.requireNonNull(this.getResource("defaultConfig.yml")), StandardCharsets.UTF_8);

            config = YamlConfiguration.loadConfiguration(defConfigStream);
        }

        saveConfig();

        this.serverName = getConfig().getString("server-name");
        this.enableGlobalChat = getConfig().getBoolean("enable-global-chat");
        this.socketIP = getConfig().getString("socket.IP");
        this.socketPort = getConfig().getInt("socket.port");
    }

    @Override
    public @NotNull FileConfiguration getConfig() {
        if (config == null) {
            reloadConfig();
        }
        return config;
    }

    @Override
    public void saveConfig() {
        if (config == null || configFile == null) {
            return;
        }

        try {
            getConfig().save(configFile);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, "Could not save config to " + configFile, ex);
        }
    }

    public static GlobalChat getPlugin() {
        return plugin;
    }
}

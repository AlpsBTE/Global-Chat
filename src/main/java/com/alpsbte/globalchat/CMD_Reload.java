package com.alpsbte.globalchat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CMD_Reload implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(sender.hasPermission("alpsbte.admin")) {
            GlobalChat.getPlugin().reloadConfig();
            GlobalChat.getPlugin().saveConfig();

            sender.sendMessage("§7>> §aSuccessfully reloaded config.");
        } else {
            sender.sendMessage("§7>> §cYou don't have permission to execute this command!");
        }
        return true;
    }
}

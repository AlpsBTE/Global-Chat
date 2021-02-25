package github.AlpsBTE_Chat;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CMD_Reload implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String s, String[] args) {
        if(sender.hasPermission("alpsbte.reload")) {
            AlpsBTE_Chat.getPlugin().reloadConfig();
            AlpsBTE_Chat.getPlugin().saveConfig();

            sender.sendMessage("Successfully reloaded config.");
        }
        return true;
    }
}

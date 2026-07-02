package gg.hcfclone.teamfights.commands;

import gg.hcfclone.teamfights.TeamfightsPlugin;
import gg.hcfclone.teamfights.util.ItemBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class TeamfightsCommand implements CommandExecutor {

    private final TeamfightsPlugin plugin;

    public TeamfightsCommand(TeamfightsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp() && !sender.hasPermission("teamfights.admin")) {
            sender.sendMessage(ItemBuilder.color("&cNo tienes permiso."));
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(ItemBuilder.color("&7Uso: &f/teamfights reload"));
            return true;
        }
        if (args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            plugin.getBardManager().reload();
            sender.sendMessage(ItemBuilder.color("&aConfig recargada."));
        }
        return true;
    }
}

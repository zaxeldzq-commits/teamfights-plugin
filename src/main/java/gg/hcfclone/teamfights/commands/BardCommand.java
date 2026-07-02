package gg.hcfclone.teamfights.commands;

import gg.hcfclone.teamfights.TeamfightsPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class BardCommand implements CommandExecutor {

    private final TeamfightsPlugin plugin;

    public BardCommand(TeamfightsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo jugadores pueden usar este comando.");
            return true;
        }
        plugin.getBardManager().giveKit(player);
        return true;
    }
}

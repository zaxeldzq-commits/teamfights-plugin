package gg.hcfclone.teamfights.commands;

import gg.hcfclone.teamfights.TeamfightsPlugin;
import gg.hcfclone.teamfights.claims.Claim;
import gg.hcfclone.teamfights.claims.ClaimManager;
import gg.hcfclone.teamfights.teams.Team;
import gg.hcfclone.teamfights.util.ItemBuilder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClaimCommand implements CommandExecutor {

    private final TeamfightsPlugin plugin;

    public ClaimCommand(TeamfightsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo jugadores pueden usar este comando.");
            return true;
        }

        ClaimManager cm = plugin.getClaimManager();
        Team team = plugin.getTeamManager().getTeamOf(player);

        if (args.length == 0) {
            msg(player, "&7Uso: &f/claim <create <tamaño>|info|delete>");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> {
                if (team == null) { msg(player, "&cNecesitas un team para hacer un claim."); return true; }
                if (!team.getOwner().equals(player.getUniqueId())) { msg(player, "&cSolo el líder del team puede crear el claim."); return true; }
                if (args.length < 2) {
                    msg(player, "&7Uso: &f/claim create <tamaño> &7(entre " + cm.getMinSize() + " y " + cm.getMaxSize() + ")");
                    return true;
                }
                int size;
                try {
                    size = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    msg(player, "&cEse tamaño no es un número válido.");
                    return true;
                }

                ClaimManager.CreateResult result = cm.create(team.getName(), player.getLocation(), size);
                switch (result) {
                    case OK -> msg(player, "&aClaim creado: &f" + size + "x" + size + " &acentrado en tu posición.");
                    case TOO_SMALL -> msg(player, "&cEl tamaño mínimo es " + cm.getMinSize() + "x" + cm.getMinSize() + ".");
                    case TOO_BIG -> msg(player, "&cEl tamaño máximo es " + cm.getMaxSize() + "x" + cm.getMaxSize() + ".");
                    case ALREADY_HAS_CLAIM -> msg(player, "&cTu team ya tiene un claim. Bórralo primero con /claim delete.");
                    case OVERLAPS -> msg(player, "&cEse territorio se solapa con el claim de otro team.");
                }
            }
            case "info" -> {
                if (team == null) { msg(player, "&cNo perteneces a ningún team."); return true; }
                Claim claim = cm.getClaimOf(team.getName());
                if (claim == null) { msg(player, "&cTu team no tiene claim todavía."); return true; }
                msg(player, "&7Claim de &f" + team.getName());
                msg(player, "&7Mundo: &f" + claim.getWorld());
                msg(player, "&7Tamaño: &f" + claim.sizeX() + "x" + claim.sizeZ());
                msg(player, "&7Esquinas: &f(" + claim.getMinX() + ", " + claim.getMinZ() + ") a (" + claim.getMaxX() + ", " + claim.getMaxZ() + ")");
            }
            case "delete" -> {
                if (team == null) { msg(player, "&cNo perteneces a ningún team."); return true; }
                if (!team.getOwner().equals(player.getUniqueId())) { msg(player, "&cSolo el líder puede borrar el claim."); return true; }
                if (!cm.hasClaim(team.getName())) { msg(player, "&cTu team no tiene claim."); return true; }
                cm.delete(team.getName());
                msg(player, "&aClaim borrado.");
            }
            default -> msg(player, "&7Uso: &f/claim <create <tamaño>|info|delete>");
        }
        return true;
    }

    private void msg(CommandSender to, String text) {
        to.sendMessage(ItemBuilder.color(text));
    }
}

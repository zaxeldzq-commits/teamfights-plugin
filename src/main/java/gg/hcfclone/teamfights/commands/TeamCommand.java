package gg.hcfclone.teamfights.commands;

import gg.hcfclone.teamfights.TeamfightsPlugin;
import gg.hcfclone.teamfights.teams.Team;
import gg.hcfclone.teamfights.teams.TeamManager;
import gg.hcfclone.teamfights.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class TeamCommand implements CommandExecutor {

    private final TeamfightsPlugin plugin;

    public TeamCommand(TeamfightsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Solo jugadores pueden usar este comando.");
            return true;
        }
        TeamManager tm = plugin.getTeamManager();

        if (args.length == 0) {
            msg(player, "&7Uso: &f/team <create|invite|accept|leave|disband|list|info|ff>");
            return true;
        }

        String sub = args[0].toLowerCase();
        switch (sub) {
            case "create" -> {
                if (args.length < 2) { msg(player, "&7Uso: &f/team create <nombre>"); return true; }
                String name = args[1];
                if (tm.getTeamOf(player) != null) { msg(player, "&cYa perteneces a un team."); return true; }
                if (tm.teamExists(name)) { msg(player, "&cEse nombre de team ya existe."); return true; }
                tm.createTeam(name, player);
                msg(player, "&aCreaste el team &f" + name + "&a.");
            }
            case "invite" -> {
                if (args.length < 2) { msg(player, "&7Uso: &f/team invite <jugador>"); return true; }
                Team team = tm.getTeamOf(player);
                if (team == null) { msg(player, "&cNo perteneces a ningún team."); return true; }
                if (!team.getOwner().equals(player.getUniqueId())) { msg(player, "&cSolo el líder puede invitar."); return true; }

                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) { msg(player, "&cEse jugador no está conectado."); return true; }
                if (tm.getTeamOf(target) != null) { msg(player, "&cEse jugador ya está en un team."); return true; }

                int max = plugin.getConfig().getInt("teams.max-members", 8);
                if (team.getMembers().size() >= max) { msg(player, "&cTu team ya está lleno."); return true; }

                tm.invite(team, target);
                msg(player, "&aInvitaste a &f" + target.getName() + " &aal team.");
                msg(target, "&f" + player.getName() + " &ate invitó a su team &f" + team.getName() +
                        "&a. Usa &f/team accept " + team.getName() + " &apara unirte.");
            }
            case "accept" -> {
                if (args.length < 2) { msg(player, "&7Uso: &f/team accept <team>"); return true; }
                Team team = tm.getTeam(args[1]);
                if (team == null) { msg(player, "&cEse team no existe."); return true; }
                if (tm.getTeamOf(player) != null) { msg(player, "&cYa perteneces a un team."); return true; }
                if (!tm.hasInvite(team, player)) { msg(player, "&cNo tienes invitación de ese team."); return true; }

                tm.join(team, player);
                for (UUID uuid : team.getMembers()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) msg(p, "&f" + player.getName() + " &ase unió al team.");
                }
            }
            case "leave" -> {
                Team team = tm.getTeamOf(player);
                if (team == null) { msg(player, "&cNo perteneces a ningún team."); return true; }
                tm.leave(player);
                msg(player, "&aSaliste del team.");
            }
            case "disband" -> {
                Team team = tm.getTeamOf(player);
                if (team == null) { msg(player, "&cNo perteneces a ningún team."); return true; }
                if (!team.getOwner().equals(player.getUniqueId())) { msg(player, "&cSolo el líder puede disolver el team."); return true; }
                for (UUID uuid : team.getMembers()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) msg(p, "&cEl team fue disuelto.");
                }
                tm.disband(team);
            }
            case "list" -> {
                Team team = tm.getTeamOf(player);
                if (team == null) { msg(player, "&cNo perteneces a ningún team."); return true; }
                StringBuilder sb = new StringBuilder();
                for (UUID uuid : team.getMembers()) {
                    String name = Bukkit.getOfflinePlayer(uuid).getName();
                    boolean isOwner = uuid.equals(team.getOwner());
                    sb.append(isOwner ? "&e★ " : "&7- ").append(name).append(" ");
                }
                msg(player, "&7Miembros de &f" + team.getName() + "&7: " + sb);
            }
            case "info" -> {
                Team team = tm.getTeamOf(player);
                if (team == null) { msg(player, "&cNo perteneces a ningún team."); return true; }
                msg(player, "&7Team: &f" + team.getName());
                msg(player, "&7Líder: &f" + Bukkit.getOfflinePlayer(team.getOwner()).getName());
                msg(player, "&7Miembros: &f" + team.getMembers().size());
                msg(player, "&7Friendly fire: &f" + (team.isFriendlyFire() ? "activado" : "desactivado"));
            }
            case "ff" -> {
                Team team = tm.getTeamOf(player);
                if (team == null) { msg(player, "&cNo perteneces a ningún team."); return true; }
                if (!team.getOwner().equals(player.getUniqueId())) { msg(player, "&cSolo el líder puede cambiar esto."); return true; }
                team.setFriendlyFire(!team.isFriendlyFire());
                msg(player, "&aFriendly fire de tu team ahora: &f" + (team.isFriendlyFire() ? "activado" : "desactivado"));
            }
            default -> msg(player, "&7Uso: &f/team <create|invite|accept|leave|disband|list|info|ff>");
        }
        return true;
    }

    private void msg(CommandSender to, String text) {
        to.sendMessage(ItemBuilder.color(text));
    }
}

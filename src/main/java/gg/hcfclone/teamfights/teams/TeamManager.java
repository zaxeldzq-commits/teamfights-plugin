package gg.hcfclone.teamfights.teams;

import gg.hcfclone.teamfights.TeamfightsPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TeamManager {

    private final TeamfightsPlugin plugin;
    private final Map<String, Team> teamsByName = new HashMap<>();
    private final Map<UUID, String> playerTeam = new HashMap<>();
    private File file;

    public TeamManager(TeamfightsPlugin plugin) {
        this.plugin = plugin;
    }

    // ---------- persistencia ----------

    public void load() {
        String fileName = plugin.getConfig().getString("teams.data-file", "teams.yml");
        file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) return;

        FileConfiguration yml = YamlConfiguration.loadConfiguration(file);
        if (yml.getConfigurationSection("teams") == null) return;

        for (String teamName : yml.getConfigurationSection("teams").getKeys(false)) {
            String base = "teams." + teamName + ".";
            UUID owner = UUID.fromString(yml.getString(base + "owner"));
            Team team = new Team(teamName, owner);
            team.setFriendlyFire(yml.getBoolean(base + "friendly-fire", false));

            for (String memberStr : yml.getStringList(base + "members")) {
                UUID member = UUID.fromString(memberStr);
                team.getMembers().add(member);
                playerTeam.put(member, teamName);
            }
            teamsByName.put(teamName.toLowerCase(), team);
        }
        plugin.getLogger().info("Cargados " + teamsByName.size() + " teams.");
    }

    public void save() {
        if (file == null) {
            String fileName = plugin.getConfig().getString("teams.data-file", "teams.yml");
            file = new File(plugin.getDataFolder(), fileName);
        }
        YamlConfiguration yml = new YamlConfiguration();
        for (Team team : teamsByName.values()) {
            String base = "teams." + team.getName() + ".";
            yml.set(base + "owner", team.getOwner().toString());
            yml.set(base + "friendly-fire", team.isFriendlyFire());
            List<String> members = new ArrayList<>();
            for (UUID uuid : team.getMembers()) members.add(uuid.toString());
            yml.set(base + "members", members);
        }
        try {
            yml.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("No se pudo guardar teams.yml: " + e.getMessage());
        }
    }

    // ---------- lógica ----------

    public boolean teamExists(String name) {
        return teamsByName.containsKey(name.toLowerCase());
    }

    public Team createTeam(String name, Player owner) {
        Team team = new Team(name, owner.getUniqueId());
        teamsByName.put(name.toLowerCase(), team);
        playerTeam.put(owner.getUniqueId(), name.toLowerCase());
        return team;
    }

    public Team getTeam(String name) {
        return teamsByName.get(name.toLowerCase());
    }

    public Team getTeamOf(Player player) {
        return getTeamOf(player.getUniqueId());
    }

    public Team getTeamOf(UUID uuid) {
        String name = playerTeam.get(uuid);
        return name == null ? null : teamsByName.get(name);
    }

    public void invite(Team team, Player target) {
        team.getInvited().add(target.getUniqueId());
    }

    public boolean hasInvite(Team team, Player target) {
        return team.getInvited().contains(target.getUniqueId());
    }

    public void join(Team team, Player player) {
        team.getMembers().add(player.getUniqueId());
        team.getInvited().remove(player.getUniqueId());
        playerTeam.put(player.getUniqueId(), team.getName().toLowerCase());
    }

    public void leave(Player player) {
        Team team = getTeamOf(player);
        if (team == null) return;
        team.getMembers().remove(player.getUniqueId());
        playerTeam.remove(player.getUniqueId());

        if (team.getMembers().isEmpty()) {
            teamsByName.remove(team.getName().toLowerCase());
        } else if (team.getOwner().equals(player.getUniqueId())) {
            // pasa el ownership al siguiente miembro
            team.setOwner(team.getMembers().iterator().next());
        }
    }

    public void disband(Team team) {
        for (UUID uuid : team.getMembers()) {
            playerTeam.remove(uuid);
        }
        teamsByName.remove(team.getName().toLowerCase());
    }

    /** True si ambos jugadores están en el mismo team (por lo tanto no se debe hacer daño). */
    public boolean areAllies(UUID a, UUID b) {
        if (a.equals(b)) return true;
        Team teamA = getTeamOf(a);
        if (teamA == null) return false;
        return teamA.isMember(b);
    }

    public Collection<Team> getTeams() {
        return teamsByName.values();
    }
}

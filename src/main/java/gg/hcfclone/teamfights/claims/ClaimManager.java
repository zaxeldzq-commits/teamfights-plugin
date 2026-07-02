package gg.hcfclone.teamfights.claims;

import gg.hcfclone.teamfights.TeamfightsPlugin;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ClaimManager {

    private final TeamfightsPlugin plugin;
    private final Map<String, Claim> claimsByTeam = new HashMap<>();
    private File file;

    public ClaimManager(TeamfightsPlugin plugin) {
        this.plugin = plugin;
    }

    public int getMinSize() {
        return plugin.getConfig().getInt("claims.min-size", 5);
    }

    public int getMaxSize() {
        return plugin.getConfig().getInt("claims.max-size", 40);
    }

    // ---------- persistencia ----------

    public void load() {
        String fileName = plugin.getConfig().getString("claims.data-file", "claims.yml");
        file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) return;

        FileConfiguration yml = YamlConfiguration.loadConfiguration(file);
        if (yml.getConfigurationSection("claims") == null) return;

        for (String teamName : yml.getConfigurationSection("claims").getKeys(false)) {
            String base = "claims." + teamName + ".";
            String world = yml.getString(base + "world");
            int minX = yml.getInt(base + "minX");
            int minZ = yml.getInt(base + "minZ");
            int maxX = yml.getInt(base + "maxX");
            int maxZ = yml.getInt(base + "maxZ");
            claimsByTeam.put(teamName.toLowerCase(), new Claim(teamName, world, minX, minZ, maxX, maxZ));
        }
        plugin.getLogger().info("Cargados " + claimsByTeam.size() + " claims.");
    }

    public void save() {
        if (file == null) {
            String fileName = plugin.getConfig().getString("claims.data-file", "claims.yml");
            file = new File(plugin.getDataFolder(), fileName);
        }
        YamlConfiguration yml = new YamlConfiguration();
        for (Claim claim : claimsByTeam.values()) {
            String base = "claims." + claim.getTeamName() + ".";
            yml.set(base + "world", claim.getWorld());
            yml.set(base + "minX", claim.getMinX());
            yml.set(base + "minZ", claim.getMinZ());
            yml.set(base + "maxX", claim.getMaxX());
            yml.set(base + "maxZ", claim.getMaxZ());
        }
        try {
            yml.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("No se pudo guardar claims.yml: " + e.getMessage());
        }
    }

    // ---------- lógica ----------

    public Claim getClaimOf(String teamName) {
        return claimsByTeam.get(teamName.toLowerCase());
    }

    public boolean hasClaim(String teamName) {
        return claimsByTeam.containsKey(teamName.toLowerCase());
    }

    public Claim getClaimAt(Location loc) {
        String world = loc.getWorld().getName();
        int x = loc.getBlockX();
        int z = loc.getBlockZ();
        for (Claim claim : claimsByTeam.values()) {
            if (claim.contains(world, x, z)) return claim;
        }
        return null;
    }

    public enum CreateResult { OK, TOO_SMALL, TOO_BIG, ALREADY_HAS_CLAIM, OVERLAPS }

    public CreateResult create(String teamName, Location center, int size) {
        if (hasClaim(teamName)) return CreateResult.ALREADY_HAS_CLAIM;

        int min = getMinSize();
        int max = getMaxSize();
        if (size < min) return CreateResult.TOO_SMALL;
        if (size > max) return CreateResult.TOO_BIG;

        int half = size / 2;
        int minX = center.getBlockX() - half;
        int maxX = minX + size - 1;
        int minZ = center.getBlockZ() - half;
        int maxZ = minZ + size - 1;

        Claim candidate = new Claim(teamName, center.getWorld().getName(), minX, minZ, maxX, maxZ);
        for (Claim existing : claimsByTeam.values()) {
            if (candidate.overlaps(existing)) return CreateResult.OVERLAPS;
        }

        claimsByTeam.put(teamName.toLowerCase(), candidate);
        return CreateResult.OK;
    }

    public void delete(String teamName) {
        claimsByTeam.remove(teamName.toLowerCase());
    }
}

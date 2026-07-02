package gg.hcfclone.teamfights.energy;

import gg.hcfclone.teamfights.TeamfightsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Maneja la energía de cada jugador (usada para los "burst" del bard) y
 * opcionalmente la muestra con una bossbar tipo HCF.
 */
public class EnergyManager implements Listener {

    private final TeamfightsPlugin plugin;
    private final Map<UUID, Double> energy = new HashMap<>();
    private final Map<UUID, BossBar> bossBars = new HashMap<>();

    public EnergyManager(TeamfightsPlugin plugin) {
        this.plugin = plugin;
    }

    public double getMax() {
        return plugin.getConfig().getDouble("energy.max", 100);
    }

    public double getRegenPerSecond() {
        return plugin.getConfig().getDouble("energy.regen-per-second", 4);
    }

    public double getEnergy(Player player) {
        return energy.getOrDefault(player.getUniqueId(), getMax());
    }

    public boolean hasEnergy(Player player, double amount) {
        return getEnergy(player) >= amount;
    }

    /** Devuelve true si pudo consumir la energía (y la descuenta). */
    public boolean consume(Player player, double amount) {
        double current = getEnergy(player);
        if (current < amount) return false;
        setEnergy(player, current - amount);
        return true;
    }

    public void setEnergy(Player player, double value) {
        double max = getMax();
        double clamped = Math.max(0, Math.min(max, value));
        energy.put(player.getUniqueId(), clamped);
        updateBossBar(player, clamped, max);
    }

    private void updateBossBar(Player player, double value, double max) {
        if (!plugin.getConfig().getBoolean("energy.show-bossbar", true)) return;

        BossBar bar = bossBars.computeIfAbsent(player.getUniqueId(), id -> {
            BarColor color;
            try {
                color = BarColor.valueOf(plugin.getConfig().getString("energy.bossbar-color", "PURPLE"));
            } catch (IllegalArgumentException e) {
                color = BarColor.PURPLE;
            }
            BossBar newBar = Bukkit.createBossBar("Energía", color, BarStyle.SEGMENTED_10);
            newBar.addPlayer(player);
            return newBar;
        });

        double progress = max <= 0 ? 0 : value / max;
        bar.setProgress(Math.max(0, Math.min(1, progress)));
        bar.setTitle("Energía: " + (int) value + "/" + (int) max);
    }

    public void startRegenTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            double regen = getRegenPerSecond();
            double max = getMax();
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                double current = getEnergy(player);
                if (current < max) {
                    setEnergy(player, Math.min(max, current + regen));
                } else {
                    // igual refrescamos la bossbar por si se acaba de conectar
                    updateBossBar(player, current, max);
                }
            }
        }, 20L, 20L);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        setEnergy(event.getPlayer(), getMax());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        BossBar bar = bossBars.remove(event.getPlayer().getUniqueId());
        if (bar != null) bar.removeAll();
        energy.remove(event.getPlayer().getUniqueId());
    }
}

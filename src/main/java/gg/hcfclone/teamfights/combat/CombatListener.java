package gg.hcfclone.teamfights.combat;

import gg.hcfclone.teamfights.TeamfightsPlugin;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.projectiles.ProjectileSource;

public class CombatListener implements Listener {

    private final TeamfightsPlugin plugin;

    public CombatListener(TeamfightsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        Player damager = resolveAttacker(event);
        if (damager == null) return;

        // ---- Friendly fire ----
        boolean globalFF = plugin.getConfig().getBoolean("teams.friendly-fire", false);
        var team = plugin.getTeamManager().getTeamOf(damager);
        boolean teamAllowsFF = team != null && team.isFriendlyFire();

        if (!globalFF && !teamAllowsFF && plugin.getTeamManager().areAllies(damager.getUniqueId(), victim.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        // ---- Damage cap anti one-shot ----
        if (plugin.getConfig().getBoolean("combat.enable-damage-cap", true)) {
            double maxPercent = plugin.getConfig().getDouble("combat.max-damage-percent", 0.55);
            double maxHealth = victim.getAttribute(Attribute.MAX_HEALTH).getValue();
            double cap = maxHealth * maxPercent;
            if (event.getFinalDamage() > cap) {
                event.setDamage(cap);
            }
        }
    }

    /** Resuelve el jugador que realmente causó el daño, incluyendo flechas/tridentes. */
    private Player resolveAttacker(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player player) {
            return player;
        }
        if (event.getDamager() instanceof Projectile projectile) {
            ProjectileSource source = projectile.getShooter();
            if (source instanceof Player player) {
                return player;
            }
        }
        return null;
    }
}

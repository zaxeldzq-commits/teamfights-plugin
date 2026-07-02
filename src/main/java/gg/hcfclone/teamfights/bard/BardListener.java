package gg.hcfclone.teamfights.bard;

import gg.hcfclone.teamfights.TeamfightsPlugin;
import gg.hcfclone.teamfights.util.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class BardListener implements Listener {

    private final TeamfightsPlugin plugin;

    public BardListener(TeamfightsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        // los items de bard ahora se usan desde la mano PRINCIPAL, no la offhand
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getAction().isRightClick()) return;

        Player player = event.getPlayer();
        BardItemDefinition def = plugin.getBardManager().getDefinitionFromItem(event.getItem());
        if (def == null) return;

        event.setCancelled(true);

        long remaining = plugin.getBardManager().getRemainingCooldown(player, def);
        if (remaining > 0) {
            player.sendMessage(ItemBuilder.color("&c&lBard &7> &fEspera &c" +
                    String.format("%.1f", remaining / 1000.0) + "s &fpara volver a usar esto."));
            return;
        }

        double cost = def.getEnergyCost();
        if (!plugin.getEnergyManager().hasEnergy(player, cost)) {
            player.sendMessage(ItemBuilder.color("&c&lBard &7> &fNo tienes suficiente energía. Necesitas &c" +
                    (int) cost + " &fy tienes &c" + (int) plugin.getEnergyManager().getEnergy(player) + "&f."));
            return;
        }

        plugin.getEnergyManager().consume(player, cost);
        plugin.getBardManager().applyBurst(player, def);
        plugin.getBardManager().setCooldown(player, def);

        player.sendMessage(ItemBuilder.color("&d&lBard &7> &fActivaste &d" + def.getDisplayName() + "&f."));
    }
}

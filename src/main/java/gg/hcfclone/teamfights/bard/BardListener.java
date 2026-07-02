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
        // evita duplicar el evento por mano principal + secundaria
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getAction().isRightClick()) return;

        Player player = event.getPlayer();
        BardEffectDefinition def = plugin.getBardManager().getDefinitionFromItem(event.getItem());
        if (def == null || def.getKind() != BardEffectDefinition.Kind.CLICKABLE) return;

        event.setCancelled(true);

        long remaining = plugin.getBardManager().getRemainingCooldown(player, def);
        if (remaining > 0) {
            player.sendMessage(ItemBuilder.color("&c&lBard &7> &fEspera &c" +
                    String.format("%.1f", remaining / 1000.0) + "s &fpara volver a usar esto."));
            return;
        }

        plugin.getBardManager().applyClickable(player, def);
        plugin.getBardManager().setCooldown(player, def);
    }
}

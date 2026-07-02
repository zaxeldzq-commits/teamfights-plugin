package gg.hcfclone.teamfights.claims;

import gg.hcfclone.teamfights.TeamfightsPlugin;
import gg.hcfclone.teamfights.teams.Team;
import gg.hcfclone.teamfights.util.ItemBuilder;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

import java.util.Iterator;

public class ClaimListener implements Listener {

    private final TeamfightsPlugin plugin;

    public ClaimListener(TeamfightsPlugin plugin) {
        this.plugin = plugin;
    }

    private boolean canBuild(Player player, Block block) {
        if (player.isOp() || player.hasPermission("teamfights.claims.bypass")) return true;

        Claim claim = plugin.getClaimManager().getClaimAt(block.getLocation());
        if (claim == null) return true;

        Team team = plugin.getTeamManager().getTeamOf(player);
        return team != null && team.getName().equalsIgnoreCase(claim.getTeamName());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        if (!canBuild(event.getPlayer(), event.getBlock())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ItemBuilder.color("&cEste territorio no es tuyo."));
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (!canBuild(event.getPlayer(), event.getBlock())) {
            event.setCancelled(true);
            event.getPlayer().sendMessage(ItemBuilder.color("&cEste territorio no es tuyo."));
        }
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        if (!canBuild(event.getPlayer(), event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        if (!canBuild(event.getPlayer(), event.getBlock())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInteract(org.bukkit.event.player.PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK || event.getClickedBlock() == null) return;
        // protege interacción con cofres, puertas, botones, palancas, etc. dentro de claims ajenos
        if (!canBuild(event.getPlayer(), event.getClickedBlock())) {
            String type = event.getClickedBlock().getType().name();
            if (type.contains("CHEST") || type.contains("DOOR") || type.contains("TRAPDOOR")
                    || type.contains("BUTTON") || type.contains("LEVER") || type.contains("FURNACE")
                    || type.contains("BARREL") || type.contains("SHULKER")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(ItemBuilder.color("&cEste territorio no es tuyo."));
            }
        }
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        Iterator<Block> it = event.blockList().iterator();
        while (it.hasNext()) {
            Block block = it.next();
            if (plugin.getClaimManager().getClaimAt(block.getLocation()) != null) {
                it.remove();
            }
        }
    }

    @EventHandler
    public void onBlockExplode(BlockExplodeEvent event) {
        Iterator<Block> it = event.blockList().iterator();
        while (it.hasNext()) {
            Block block = it.next();
            if (plugin.getClaimManager().getClaimAt(block.getLocation()) != null) {
                it.remove();
            }
        }
    }
}

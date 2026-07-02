package gg.hcfclone.teamfights.bard;

import gg.hcfclone.teamfights.TeamfightsPlugin;
import gg.hcfclone.teamfights.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class BardManager {

    private final TeamfightsPlugin plugin;
    public final NamespacedKey BARD_ITEM_KEY;

    private final Map<String, BardItemDefinition> definitions = new LinkedHashMap<>();
    // cooldowns: uuid -> (id item -> timestamp fin de cooldown en millis)
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    // último item de bard sostenido en la mano principal, para saber cuándo quitar el efecto pasivo
    private final Map<UUID, String> lastHeldId = new HashMap<>();

    public BardManager(TeamfightsPlugin plugin) {
        this.plugin = plugin;
        this.BARD_ITEM_KEY = new NamespacedKey(plugin, "bard_item_id");
        loadDefinitions();
    }

    private void loadDefinitions() {
        definitions.clear();
        ConfigurationSection itemsSec = plugin.getConfig().getConfigurationSection("bard.items");
        if (itemsSec == null) return;

        for (String id : itemsSec.getKeys(false)) {
            ConfigurationSection s = itemsSec.getConfigurationSection(id);
            definitions.put(id, new BardItemDefinition(
                    id,
                    Material.valueOf(s.getString("material")),
                    s.getString("custom-name"),
                    s.getStringList("lore"),
                    PotionEffectType.getByName(s.getString("passive-effect")),
                    s.getInt("passive-amplifier", 0),
                    PotionEffectType.getByName(s.getString("burst-effect")),
                    s.getInt("burst-amplifier", 0),
                    s.getInt("burst-duration-seconds", 6),
                    s.getInt("cooldown-seconds", 20),
                    s.getInt("energy-cost", 25)
            ));
        }
        plugin.getLogger().info("Cargados " + definitions.size() + " items de bard.");
    }

    public void reload() {
        loadDefinitions();
    }

    public Collection<BardItemDefinition> getDefinitions() {
        return definitions.values();
    }

    public BardItemDefinition getDefinitionFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        String id = meta.getPersistentDataContainer().get(BARD_ITEM_KEY, PersistentDataType.STRING);
        if (id == null) return null;
        return definitions.get(id);
    }

    public ItemStack buildItem(BardItemDefinition def) {
        return new ItemBuilder(def.getMaterial())
                .name(def.getDisplayName())
                .lore(def.getLore())
                .tag(BARD_ITEM_KEY, def.getId())
                .build();
    }

    /** Da el kit completo del bard (los 5 items) al inventario. */
    public void giveKit(Player player) {
        for (BardItemDefinition def : definitions.values()) {
            player.getInventory().addItem(buildItem(def));
        }
        player.sendMessage(ItemBuilder.color("&d&lBard &7> &fRecibiste tu kit. Sostén un item en tu mano" +
                " PRINCIPAL para el efecto pasivo, y click derecho para el efecto reforzado (gasta energía)."));
    }

    // ---------- cooldowns ----------

    public long getRemainingCooldown(Player player, BardItemDefinition def) {
        Map<String, Long> map = cooldowns.get(player.getUniqueId());
        if (map == null) return 0;
        Long end = map.get(def.getId());
        if (end == null) return 0;
        return Math.max(0, end - System.currentTimeMillis());
    }

    public void setCooldown(Player player, BardItemDefinition def) {
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                .put(def.getId(), System.currentTimeMillis() + def.getCooldownSeconds() * 1000L);
    }

    // ---------- burst (click derecho) ----------

    public void applyBurst(Player player, BardItemDefinition def) {
        int durationTicks = def.getBurstDurationSeconds() * 20;
        player.addPotionEffect(new PotionEffect(def.getBurstEffect(), durationTicks, def.getBurstAmplifier(), false, true, true));
    }

    // ---------- tarea pasiva (mano principal) ----------

    public void startPassiveTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                ItemStack mainHand = player.getInventory().getItemInMainHand();
                BardItemDefinition def = getDefinitionFromItem(mainHand);

                String previousId = lastHeldId.get(player.getUniqueId());

                if (def == null) {
                    // ya no sostiene ningún item de bard: si antes tenía uno, le quitamos el efecto pasivo
                    if (previousId != null) {
                        BardItemDefinition prev = definitions.get(previousId);
                        if (prev != null) {
                            player.removePotionEffect(prev.getPassiveEffect());
                        }
                        lastHeldId.remove(player.getUniqueId());
                    }
                    continue;
                }

                if (previousId != null && !previousId.equals(def.getId())) {
                    // cambió de item de bard: quitamos el efecto pasivo del anterior
                    BardItemDefinition prev = definitions.get(previousId);
                    if (prev != null) {
                        player.removePotionEffect(prev.getPassiveEffect());
                    }
                }

                lastHeldId.put(player.getUniqueId(), def.getId());
                // se reaplica cada segundo con una duración un poco mayor para que nunca se corte
                player.addPotionEffect(new PotionEffect(def.getPassiveEffect(), 60, def.getPassiveAmplifier(), false, false, false));
            }
        }, 20L, 20L);
    }
}

package gg.hcfclone.teamfights.bard;

import gg.hcfclone.teamfights.TeamfightsPlugin;
import gg.hcfclone.teamfights.teams.Team;
import gg.hcfclone.teamfights.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
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

    private final Map<String, BardEffectDefinition> definitions = new LinkedHashMap<>();
    // cooldowns: uuid jugador -> (id clickeable -> timestamp fin de cooldown en millis)
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();

    public BardManager(TeamfightsPlugin plugin) {
        this.plugin = plugin;
        this.BARD_ITEM_KEY = new NamespacedKey(plugin, "bard_item_id");
        loadDefinitions();
    }

    private void loadDefinitions() {
        definitions.clear();
        ConfigurationSection holdSec = plugin.getConfig().getConfigurationSection("bard.holdables");
        if (holdSec != null) {
            for (String id : holdSec.getKeys(false)) {
                ConfigurationSection s = holdSec.getConfigurationSection(id);
                definitions.put(id, new BardEffectDefinition(
                        id,
                        BardEffectDefinition.Kind.HOLDABLE,
                        Material.valueOf(s.getString("material")),
                        s.getString("custom-name"),
                        s.getStringList("lore"),
                        PotionEffectType.getByName(s.getString("effect")),
                        s.getInt("amplifier", 0),
                        0, 0
                ));
            }
        }
        ConfigurationSection clickSec = plugin.getConfig().getConfigurationSection("bard.clickables");
        if (clickSec != null) {
            for (String id : clickSec.getKeys(false)) {
                ConfigurationSection s = clickSec.getConfigurationSection(id);
                definitions.put(id, new BardEffectDefinition(
                        id,
                        BardEffectDefinition.Kind.CLICKABLE,
                        Material.valueOf(s.getString("material")),
                        s.getString("custom-name"),
                        s.getStringList("lore"),
                        PotionEffectType.getByName(s.getString("effect")),
                        s.getInt("amplifier", 0),
                        s.getInt("duration-seconds", 4),
                        s.getInt("cooldown-seconds", 30)
                ));
            }
        }
        plugin.getLogger().info("Cargadas " + definitions.size() + " definiciones de bard.");
    }

    public void reload() {
        loadDefinitions();
    }

    public Collection<BardEffectDefinition> getDefinitions() {
        return definitions.values();
    }

    public BardEffectDefinition getDefinitionFromItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        String id = meta.getPersistentDataContainer().get(BARD_ITEM_KEY, PersistentDataType.STRING);
        if (id == null) return null;
        return definitions.get(id);
    }

    public ItemStack buildItem(BardEffectDefinition def) {
        return new ItemBuilder(def.getMaterial())
                .name(def.getDisplayName())
                .lore(def.getLore())
                .tag(BARD_ITEM_KEY, def.getId())
                .build();
    }

    /** Da el kit completo del bard: holdables + clickeables en el inventario. */
    public void giveKit(Player player) {
        for (BardEffectDefinition def : definitions.values()) {
            player.getInventory().addItem(buildItem(def));
        }
        player.sendMessage(ItemBuilder.color("&d&lBard &7> &fRecibiste tu kit. Sostén una vara en la mano" +
                " secundaria (F) y usa click derecho con las notas para tus efectos."));
    }

    // ---------- cooldowns ----------

    public long getRemainingCooldown(Player player, BardEffectDefinition def) {
        Map<String, Long> map = cooldowns.get(player.getUniqueId());
        if (map == null) return 0;
        Long end = map.get(def.getId());
        if (end == null) return 0;
        long remaining = end - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    public void setCooldown(Player player, BardEffectDefinition def) {
        cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>())
                .put(def.getId(), System.currentTimeMillis() + def.getCooldownSeconds() * 1000L);
    }

    // ---------- aplicar efectos ----------

    public void applyClickable(Player caster, BardEffectDefinition def) {
        int radius = plugin.getConfig().getInt("bard.clickable-radius", 8);
        Team team = plugin.getTeamManager().getTeamOf(caster);

        List<Player> targets = new ArrayList<>();
        targets.add(caster);
        for (Player nearby : caster.getWorld().getPlayers()) {
            if (nearby.equals(caster)) continue;
            if (nearby.getLocation().distance(caster.getLocation()) > radius) continue;
            if (team != null && team.isMember(nearby.getUniqueId())) {
                targets.add(nearby);
            }
        }

        int durationTicks = def.getDurationSeconds() * 20;
        for (Player target : targets) {
            target.addPotionEffect(new PotionEffect(def.getEffect(), durationTicks, def.getAmplifier(), false, true, true));
        }

        try {
            Sound sound = Sound.valueOf(plugin.getConfig().getString("bard.effect-sound", "ENTITY_ILLUSIONER_CAST_SPELL"));
            caster.getWorld().playSound(caster.getLocation(), sound, 1f, 1.2f);
        } catch (IllegalArgumentException ignored) {}

        try {
            Particle particle = Particle.valueOf(plugin.getConfig().getString("bard.effect-particle", "WITCH"));
            caster.getWorld().spawnParticle(particle, caster.getLocation().add(0, 1, 0), 30, 0.5, 0.7, 0.5);
        } catch (IllegalArgumentException ignored) {}

        caster.sendMessage(ItemBuilder.color("&d&lBard &7> &fActivaste &d" + def.getDisplayName() +
                " &fpara " + (targets.size() - 1) + " compañero(s) cercano(s)."));
    }

    // ---------- tarea pasiva (holdables) ----------

    public void startPassiveTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                ItemStack offHand = player.getInventory().getItemInOffHand();
                BardEffectDefinition def = getDefinitionFromItem(offHand);
                if (def == null || def.getKind() != BardEffectDefinition.Kind.HOLDABLE) continue;

                // se re-aplica cada segundo con una duración un poco mayor para que nunca se corte
                player.addPotionEffect(new PotionEffect(def.getEffect(), 60, def.getAmplifier(), false, false, false));
            }
        }, 20L, 20L);
    }
}

package gg.hcfclone.teamfights.classes;

import gg.hcfclone.teamfights.TeamfightsPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

/**
 * Sistema de clases old-HCF: si el jugador tiene puesto el set COMPLETO
 * (casco+pecho+piernas+botas) de un material específico, recibe los
 * efectos de esa clase. Se le quitan apenas se saca alguna pieza.
 */
public class ClassManager {

    private final TeamfightsPlugin plugin;
    private final Map<String, ArmorClassDefinition> definitions = new LinkedHashMap<>();
    // clase actual de cada jugador, para saber cuándo cambió/quitó
    private final Map<UUID, String> currentClass = new HashMap<>();

    public ClassManager(TeamfightsPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    private void load() {
        definitions.clear();
        ConfigurationSection sec = plugin.getConfig().getConfigurationSection("classes");
        if (sec == null) return;

        for (String id : sec.getKeys(false)) {
            ConfigurationSection s = sec.getConfigurationSection(id);
            List<ArmorClassDefinition.EffectEntry> effects = new ArrayList<>();
            List<Map<?, ?>> rawEffects = s.getMapList("effects");
            for (Map<?, ?> raw : rawEffects) {
                PotionEffectType type = PotionEffectType.getByName(String.valueOf(raw.get("effect")));
                int amplifier = raw.get("amplifier") != null ? Integer.parseInt(String.valueOf(raw.get("amplifier"))) : 0;
                if (type != null) {
                    effects.add(new ArmorClassDefinition.EffectEntry(type, amplifier));
                }
            }
            definitions.put(id, new ArmorClassDefinition(
                    id,
                    s.getString("armor-material"),
                    s.getString("display-name"),
                    effects
            ));
        }
        plugin.getLogger().info("Cargadas " + definitions.size() + " clases por armadura.");
    }

    public void reload() {
        load();
    }

    /** Devuelve la clase activa según la armadura puesta, o null si no coincide ningún set completo. */
    private ArmorClassDefinition detectClass(Player player) {
        PlayerInventory inv = player.getInventory();
        ItemStack helmet = inv.getHelmet();
        ItemStack chest = inv.getChestplate();
        ItemStack legs = inv.getLeggings();
        ItemStack boots = inv.getBoots();

        if (helmet == null || chest == null || legs == null || boots == null) return null;

        for (ArmorClassDefinition def : definitions.values()) {
            String prefix = def.getArmorMaterialPrefix();
            if (matches(helmet.getType(), prefix, "HELMET")
                    && matches(chest.getType(), prefix, "CHESTPLATE")
                    && matches(legs.getType(), prefix, "LEGGINGS")
                    && matches(boots.getType(), prefix, "BOOTS")) {
                return def;
            }
        }
        return null;
    }

    private boolean matches(Material material, String prefix, String piece) {
        return material.name().equals(prefix + "_" + piece);
    }

    /** Corre cada segundo: aplica/actualiza/quita efectos según la armadura puesta. */
    public void tick(Player player) {
        ArmorClassDefinition detected = detectClass(player);
        String previousId = currentClass.get(player.getUniqueId());

        if (detected == null) {
            if (previousId != null) {
                removeEffectsOf(player, definitions.get(previousId));
                currentClass.remove(player.getUniqueId());
            }
            return;
        }

        if (previousId != null && !previousId.equals(detected.getId())) {
            removeEffectsOf(player, definitions.get(previousId));
        }

        currentClass.put(player.getUniqueId(), detected.getId());
        for (ArmorClassDefinition.EffectEntry entry : detected.getEffects()) {
            player.addPotionEffect(new PotionEffect(entry.type(), 60, entry.amplifier(), false, false, false));
        }
    }

    private void removeEffectsOf(Player player, ArmorClassDefinition def) {
        if (def == null) return;
        for (ArmorClassDefinition.EffectEntry entry : def.getEffects()) {
            player.removePotionEffect(entry.type());
        }
    }

    public void clearPlayer(Player player) {
        String previousId = currentClass.remove(player.getUniqueId());
        if (previousId != null) {
            removeEffectsOf(player, definitions.get(previousId));
        }
    }

    public void startTask() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                tick(player);
            }
        }, 20L, 20L);
    }
}

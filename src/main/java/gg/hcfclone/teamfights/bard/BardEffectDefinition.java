package gg.hcfclone.teamfights.bard;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

/**
 * Definición genérica de un item de bard (holdable o clickeable), cargada desde config.yml.
 */
public class BardEffectDefinition {

    public enum Kind { HOLDABLE, CLICKABLE }

    private final String id;
    private final Kind kind;
    private final Material material;
    private final String displayName;
    private final List<String> lore;
    private final PotionEffectType effect;
    private final int amplifier;
    private final int durationSeconds;   // solo clickeables
    private final int cooldownSeconds;   // solo clickeables

    public BardEffectDefinition(String id, Kind kind, Material material, String displayName, List<String> lore,
                                 PotionEffectType effect, int amplifier, int durationSeconds, int cooldownSeconds) {
        this.id = id;
        this.kind = kind;
        this.material = material;
        this.displayName = displayName;
        this.lore = lore;
        this.effect = effect;
        this.amplifier = amplifier;
        this.durationSeconds = durationSeconds;
        this.cooldownSeconds = cooldownSeconds;
    }

    public String getId() { return id; }
    public Kind getKind() { return kind; }
    public Material getMaterial() { return material; }
    public String getDisplayName() { return displayName; }
    public List<String> getLore() { return lore; }
    public PotionEffectType getEffect() { return effect; }
    public int getAmplifier() { return amplifier; }
    public int getDurationSeconds() { return durationSeconds; }
    public int getCooldownSeconds() { return cooldownSeconds; }
}

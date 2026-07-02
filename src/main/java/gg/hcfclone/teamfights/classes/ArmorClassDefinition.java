package gg.hcfclone.teamfights.classes;

import org.bukkit.potion.PotionEffectType;

import java.util.List;

public class ArmorClassDefinition {

    public record EffectEntry(PotionEffectType type, int amplifier) {}

    private final String id;
    private final String armorMaterialPrefix; // ej: "GOLDEN" o "LEATHER"
    private final String displayName;
    private final List<EffectEntry> effects;

    public ArmorClassDefinition(String id, String armorMaterialPrefix, String displayName, List<EffectEntry> effects) {
        this.id = id;
        this.armorMaterialPrefix = armorMaterialPrefix;
        this.displayName = displayName;
        this.effects = effects;
    }

    public String getId() { return id; }
    public String getArmorMaterialPrefix() { return armorMaterialPrefix; }
    public String getDisplayName() { return displayName; }
    public List<EffectEntry> getEffects() { return effects; }
}

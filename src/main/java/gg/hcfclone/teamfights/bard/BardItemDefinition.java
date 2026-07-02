package gg.hcfclone.teamfights.bard;

import org.bukkit.Material;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

/**
 * Item de bard old-HCF: se sostiene en la mano PRINCIPAL.
 * - passiveEffect/passiveAmplifier: efecto mientras lo sostienes.
 * - burstEffect/burstAmplifier/burstDurationSeconds: efecto reforzado al click derecho.
 * - cooldownSeconds + energyCost: limitan el uso del burst.
 */
public class BardItemDefinition {

    private final String id;
    private final Material material;
    private final String displayName;
    private final List<String> lore;

    private final PotionEffectType passiveEffect;
    private final int passiveAmplifier;

    private final PotionEffectType burstEffect;
    private final int burstAmplifier;
    private final int burstDurationSeconds;

    private final int cooldownSeconds;
    private final int energyCost;

    public BardItemDefinition(String id, Material material, String displayName, List<String> lore,
                               PotionEffectType passiveEffect, int passiveAmplifier,
                               PotionEffectType burstEffect, int burstAmplifier, int burstDurationSeconds,
                               int cooldownSeconds, int energyCost) {
        this.id = id;
        this.material = material;
        this.displayName = displayName;
        this.lore = lore;
        this.passiveEffect = passiveEffect;
        this.passiveAmplifier = passiveAmplifier;
        this.burstEffect = burstEffect;
        this.burstAmplifier = burstAmplifier;
        this.burstDurationSeconds = burstDurationSeconds;
        this.cooldownSeconds = cooldownSeconds;
        this.energyCost = energyCost;
    }

    public String getId() { return id; }
    public Material getMaterial() { return material; }
    public String getDisplayName() { return displayName; }
    public List<String> getLore() { return lore; }
    public PotionEffectType getPassiveEffect() { return passiveEffect; }
    public int getPassiveAmplifier() { return passiveAmplifier; }
    public PotionEffectType getBurstEffect() { return burstEffect; }
    public int getBurstAmplifier() { return burstAmplifier; }
    public int getBurstDurationSeconds() { return burstDurationSeconds; }
    public int getCooldownSeconds() { return cooldownSeconds; }
    public int getEnergyCost() { return energyCost; }
}

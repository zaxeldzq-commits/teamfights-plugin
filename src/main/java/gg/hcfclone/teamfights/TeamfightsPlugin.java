package gg.hcfclone.teamfights;

import gg.hcfclone.teamfights.bard.BardListener;
import gg.hcfclone.teamfights.bard.BardManager;
import gg.hcfclone.teamfights.claims.ClaimListener;
import gg.hcfclone.teamfights.claims.ClaimManager;
import gg.hcfclone.teamfights.classes.ClassManager;
import gg.hcfclone.teamfights.combat.CombatListener;
import gg.hcfclone.teamfights.commands.BardCommand;
import gg.hcfclone.teamfights.commands.ClaimCommand;
import gg.hcfclone.teamfights.commands.TeamCommand;
import gg.hcfclone.teamfights.commands.TeamfightsCommand;
import gg.hcfclone.teamfights.energy.EnergyManager;
import gg.hcfclone.teamfights.teams.TeamManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class TeamfightsPlugin extends JavaPlugin {

    private static TeamfightsPlugin instance;

    private TeamManager teamManager;
    private BardManager bardManager;
    private EnergyManager energyManager;
    private ClassManager classManager;
    private ClaimManager claimManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.teamManager = new TeamManager(this);
        this.teamManager.load();

        this.claimManager = new ClaimManager(this);
        this.claimManager.load();

        this.energyManager = new EnergyManager(this);
        this.energyManager.startRegenTask();

        this.bardManager = new BardManager(this);
        this.bardManager.startPassiveTask();

        this.classManager = new ClassManager(this);
        this.classManager.startTask();

        // Listeners
        getServer().getPluginManager().registerEvents(new BardListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new ClaimListener(this), this);
        getServer().getPluginManager().registerEvents(energyManager, this);

        // Comandos
        getCommand("team").setExecutor(new TeamCommand(this));
        getCommand("bard").setExecutor(new BardCommand(this));
        getCommand("claim").setExecutor(new ClaimCommand(this));
        getCommand("teamfights").setExecutor(new TeamfightsCommand(this));

        getLogger().info("Teamfights habilitado. Recuerda tener NethPot (u otro plugin de combate 1.8/1.9) instalado para el PvP.");
    }

    @Override
    public void onDisable() {
        if (teamManager != null) teamManager.save();
        if (claimManager != null) claimManager.save();
    }

    public static TeamfightsPlugin get() {
        return instance;
    }

    public TeamManager getTeamManager() {
        return teamManager;
    }

    public BardManager getBardManager() {
        return bardManager;
    }

    public EnergyManager getEnergyManager() {
        return energyManager;
    }

    public ClassManager getClassManager() {
        return classManager;
    }

    public ClaimManager getClaimManager() {
        return claimManager;
    }
}

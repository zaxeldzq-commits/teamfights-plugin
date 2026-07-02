package gg.hcfclone.teamfights.teams;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

public class Team {

    private final String name;
    private UUID owner;
    private final Set<UUID> members = new LinkedHashSet<>();
    private final Set<UUID> invited = new LinkedHashSet<>();
    private boolean friendlyFire;

    public Team(String name, UUID owner) {
        this.name = name;
        this.owner = owner;
        this.members.add(owner);
    }

    public String getName() {
        return name;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public Set<UUID> getMembers() {
        return members;
    }

    public Set<UUID> getInvited() {
        return invited;
    }

    public boolean isMember(UUID uuid) {
        return members.contains(uuid);
    }

    public boolean isFriendlyFire() {
        return friendlyFire;
    }

    public void setFriendlyFire(boolean friendlyFire) {
        this.friendlyFire = friendlyFire;
    }
}

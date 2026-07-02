package gg.hcfclone.teamfights.claims;

public class Claim {

    private final String teamName;
    private final String world;
    private final int minX;
    private final int minZ;
    private final int maxX;
    private final int maxZ;

    public Claim(String teamName, String world, int minX, int minZ, int maxX, int maxZ) {
        this.teamName = teamName;
        this.world = world;
        this.minX = minX;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxZ = maxZ;
    }

    public String getTeamName() { return teamName; }
    public String getWorld() { return world; }
    public int getMinX() { return minX; }
    public int getMinZ() { return minZ; }
    public int getMaxX() { return maxX; }
    public int getMaxZ() { return maxZ; }

    public int sizeX() { return (maxX - minX) + 1; }
    public int sizeZ() { return (maxZ - minZ) + 1; }

    public boolean contains(String world, int x, int z) {
        return this.world.equals(world) && x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public boolean overlaps(Claim other) {
        if (!this.world.equals(other.world)) return false;
        return this.minX <= other.maxX && this.maxX >= other.minX
                && this.minZ <= other.maxZ && this.maxZ >= other.minZ;
    }
}

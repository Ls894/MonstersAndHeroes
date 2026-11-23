package world;

public final class TerrainTile extends Tile {
    private final TileType type;

    public TerrainTile(TileType type) {
        if (type == null || type == TileType.NEXUS || type == TileType.INACCESSIBLE) {
            throw new IllegalArgumentException("Expected a lane interior tile type.");
        }
        this.type = type;
    }

    @Override
    public TileType getType() {
        return type;
    }

    @Override
    public boolean isAccessible() {
        return type != TileType.OBSTACLE;
    }

    @Override
    public char getSymbol() {
        return type.getSymbol();
    }


}
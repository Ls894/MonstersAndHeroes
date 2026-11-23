package world;

// Class for not access part
public class InaccessibleTile extends Tile {

    @Override
    public boolean isAccessible() {
        return false;
    }

    @Override
    public char getSymbol() {
        return '#';
    }

    @Override
    public TileType getType() {
        return TileType.INACCESSIBLE;
    }
}

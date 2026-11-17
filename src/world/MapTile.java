package world;

public class MapTile extends Tile{

    @Override
    public boolean isAccessible() {
        return true;
    }

    @Override
    public char getSymbol() {
        return '.';
    }
}

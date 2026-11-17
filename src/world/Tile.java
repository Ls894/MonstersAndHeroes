package world;

// Defines the behavior of a map tile.
public abstract class Tile {
    public abstract boolean isAccessible();
    public abstract char getSymbol();
}

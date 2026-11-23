package world;

public enum TileType {
    NEXUS('N'),
    INACCESSIBLE('I'),
    OBSTACLE('X'),
    PLAIN(' '),
    BUSH('B'),
    CAVE('C'),
    KOULOU('K');

    private final char symbol;

    TileType(char symbol) {
        this.symbol = symbol;
    }

    public char getSymbol() {
        return symbol;
    }
}

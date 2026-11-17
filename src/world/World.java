package world;

// Stores a square game world.
public class World {

    private final Tile[][] tiles;
    private final int size;

    public World(Tile[][] tiles) {
        if (tiles == null || tiles.length == 0) {
            throw new IllegalArgumentException(
                    "World cannot be empty."
            );
        }

        int n = tiles.length;

        for (Tile[] row : tiles) {
            if (row == null || row.length != n) {
                throw new IllegalArgumentException(
                        "World must be square."
                );
            }

            for (Tile tile : row) {
                if (tile == null) {
                    throw new IllegalArgumentException(
                            "Tiles cannot be null."
                    );
                }
            }
        }

        this.size = n;
        this.tiles = new Tile[n][n];

        for (int r = 0; r < n; r++) {
            System.arraycopy(
                    tiles[r], 0, this.tiles[r], 0, n
            );
        }
    }

    public int getSize() {
        return size;
    }

    public boolean isInBounds(Position position) {
        if (position == null) {
            return false;
        }

        int row = position.getRow();
        int col = position.getCol();

        return row >= 0 && row < size
                && col >= 0 && col < size;
    }

    public Tile getTile(Position position) {
        if (!isInBounds(position)) {
            throw new IllegalArgumentException(
                    "Position is outside the world."
            );
        }

        return tiles[position.getRow()][position.getCol()];
    }

    // Checks whether a tile is walkable.
    public boolean canMoveTo(Position position) {
        return isInBounds(position)
                && getTile(position).isAccessible();
    }

    // Renders the map with the party position.
    public String render(Position partyPosition) {
        if (!canMoveTo(partyPosition)) {
            throw new IllegalArgumentException(
                    "Invalid party position."
            );
        }

        StringBuilder result = new StringBuilder();
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                if (r == partyPosition.getRow()
                        && c == partyPosition.getCol()) {
                    result.append('P');
                } else {
                    result.append(tiles[r][c].getSymbol());
                }
                if (c < size - 1) {
                    result.append(' ');
                }
            }
            if (r < size - 1) {
                result.append(System.lineSeparator());
            }
        }
        return result.toString();
    }

    // Prints the current map.
    public void display(Position partyPosition) {
        System.out.println(render(partyPosition));
    }
}

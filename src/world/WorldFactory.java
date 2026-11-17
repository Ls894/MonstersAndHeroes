package world;

import game.DataLoader;
import item.Armor;
import item.Item;
import item.Potion;
import item.PotionAttribute;
import item.Weapon;
import market.Market;

// World factory class.

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

// Builds connected worlds and market inventories.
public class WorldFactory {

    private final DataLoader dataLoader;
    private final Random random;

    public WorldFactory(
            DataLoader dataLoader,
            Random random) {

        if (dataLoader == null || random == null) {
            throw new IllegalArgumentException(
                    "Invalid world factory configuration."
            );
        }

        this.dataLoader = dataLoader;
        this.random = random;
    }

    // Generates tiles and markets.
    public World createWorld(int size) throws IOException {

        if (size < 4) {
            throw new IllegalArgumentException(
                    "World size must be at least 4."
            );
        }

        int total = size * size;
        int blockedCount = Math.round(total * 0.20f);
        int marketCount = Math.round(total * 0.30f);

        Position start = new Position(
                size / 2, size / 2
        );

        Tile[][] tiles = null;

        // Generate a connected map.
        for (int attempt = 0; attempt < 1000; attempt++) {

            tiles = new Tile[size][size];

            for (int r = 0; r < size; r++) {
                for (int c = 0; c < size; c++) {
                    tiles[r][c] = new MapTile();
                }
            }

            List<Position> candidates =
                    new ArrayList<>();

            for (int r = 0; r < size; r++) {
                for (int c = 0; c < size; c++) {
                    Position p = new Position(r, c);

                    if (!p.equals(start)) {
                        candidates.add(p);
                    }
                }
            }

            Collections.shuffle(candidates, random);

            for (int i = 0; i < blockedCount; i++) {
                Position p = candidates.get(i);

                tiles[p.getRow()][p.getCol()] =
                        new InaccessibleTile();
            }

            if (isConnected(tiles, start)) {
                break;
            }

            tiles = null;
        }

        if (tiles == null) {
            throw new IllegalStateException(
                    "Could not generate a connected world."
            );
        }

        // Place the starting market.
        tiles[start.getRow()][start.getCol()] =
                new MarketTile(createMarket());

        List<Position> available = new ArrayList<>();

        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                Position p = new Position(r, c);

                if (!p.equals(start)
                        && tiles[r][c].isAccessible()) {
                    available.add(p);
                }
            }
        }

        Collections.shuffle(available, random);

        for (int i = 0; i < marketCount - 1; i++) {
            Position p = available.get(i);

            tiles[p.getRow()][p.getCol()] =
                    new MarketTile(createMarket());
        }

        return new World(tiles);
    }

    private Market createMarket() throws IOException {

        // Give each market fresh items.
        List<Item> catalog = dataLoader.loadItems();

        if (catalog.isEmpty()) {
            throw new IllegalStateException(
                    "Item catalog is empty."
            );
        }

        Market market = new Market();

        // Include affordable level-one essentials.
        addStarterItem(market, catalog, Weapon.class);
        addStarterItem(market, catalog, Armor.class);
        addStarterItem(market, catalog, Potion.class);

        Collections.shuffle(catalog, random);

        int additionalItems = Math.min(
                6, catalog.size()
        );

        for (int i = 0; i < additionalItems; i++) {
            market.addStock(catalog.get(i));
        }

        return market;
    }

    // Adds the cheapest level-one item of a given type.
    private void addStarterItem(
            Market market,
            List<Item> catalog,
            Class<? extends Item> type) {

        Item cheapest = null;
        for (Item item : catalog) {
            if (!type.isInstance(item) || item.getRequiredLevel() != 1) {
                continue;
            }
            if (item instanceof Potion
                    && !((Potion) item).getAffectedAttributes()
                    .contains(PotionAttribute.HEALTH)) {
                continue;
            }
            if (cheapest == null || item.getPrice() < cheapest.getPrice()) {
                cheapest = item;
            }
        }

        if (cheapest != null) {
            market.addStock(cheapest);
            catalog.remove(cheapest);
        }
    }

    // Checks accessible tiles using BFS.
    private boolean isConnected(
            Tile[][] tiles,
            Position start) {

        int size = tiles.length;
        boolean[][] visited = new boolean[size][size];

        ArrayDeque<Position> queue = new ArrayDeque<>();

        queue.add(start);
        visited[start.getRow()][start.getCol()] = true;

        int reached = 0;

        int[][] directions = {
                {-1, 0}, {1, 0}, {0, -1}, {0, 1}
        };

        while (!queue.isEmpty()) {
            Position current = queue.remove();
            reached++;

            for (int[] direction : directions) {

                int row = current.getRow() + direction[0];
                int col = current.getCol() + direction[1];

                if (row < 0 || row >= size
                        || col < 0 || col >= size
                        || visited[row][col]
                        || !tiles[row][col].isAccessible()) {
                    continue;
                }

                visited[row][col] = true;
                queue.add(new Position(row, col));
            }
        }

        int accessibleCount = 0;

        for (Tile[] row : tiles) {
            for (Tile tile : row) {
                if (tile.isAccessible()) {
                    accessibleCount++;
                }
            }
        }

        return reached == accessibleCount;
    }
}

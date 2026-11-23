package world;

import character.Hero;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import character.Monster;
import market.Market;

import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashMap;
// Stores a square game world.
public class World {

    private final Tile[][] tiles;
    private final int size;
    private final Map<Hero, Position> heroPositions = new HashMap<>();
    private final Map<Hero, Position> heroSpawnPositions = new LinkedHashMap<>();
    private final Map<Monster, Position> monsterPositions = new LinkedHashMap<>();

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

    public List<Hero> getRegisteredHeroes() {
        return new ArrayList<>(heroSpawnPositions.keySet());
    }


    // Returns the lane index
    public int getValorLane(Position position) {
        if (!isInBounds(position)) {
            return -1;
        }
        int col = position.getCol();
        if (col == 2 || col == 5) {
            return -1;
        }
        return col / 3;
    }


    // Places a hero .
    public boolean tryPlaceHero(Hero hero, int lane) {
        Position spawn = new Position(size - 1, lane * 3);
        if (heroSpawnPositions.containsKey(hero) || heroPositions.containsValue(spawn)) {
            return false;
        }

        heroPositions.put(hero, spawn);
        heroSpawnPositions.put(hero, spawn);
        return true;
    }

    // Places a  monster .
    public boolean trySpawnMonster(Monster monster, int lane) {
        Position spawn = new Position(0, lane * 3 + 1);

        if (monsterPositions.containsValue(spawn)) {
            return false;
        }

        monsterPositions.put(monster, spawn);
        return true;
    }

    // Returns the hero's position.
    public Optional<Position> positionOf(Hero hero) {
        return Optional.ofNullable(heroPositions.get(hero));
    }

    // Returns the monsters .
    public List<Monster> getMonstersOnBoard() {
        return new ArrayList<>(monsterPositions.keySet());
    }

    // Returns the monster's position.
    public Optional<Position> positionOf(Monster monster) {
        return Optional.ofNullable(monsterPositions.get(monster));
    }

    // Moves a hero one square if the destination is legal.
    public boolean tryMoveHero(Hero hero, Position to) {
        Position from = heroPositions.get(hero);
        if (from == null || !canMoveTo(to) || heroPositions.containsValue(to)) {
            return false;
        }

        if (!isAdjacent(from, to)) {
            return false;
        }

        if (isBehindMonster(to)) {
            return false;
        }

        heroPositions.put(hero, to);
        return true;
    }



    private boolean isBehindMonster(Position position) {
        int lane = getValorLane(position);

        for (Position monsterPosition : monsterPositions.values()) {
            if (getValorLane(monsterPosition) == lane && position.getRow() < monsterPosition.getRow()) {
                return true;
            }
        }

        return false;
    }

    // Teleports a hero beside or below a teammate in another lane.
    public boolean tryTeleport(Hero hero, Hero teammate, Position to) {
        Position from = heroPositions.get(hero);
        Position anchor = heroPositions.get(teammate);

        if (from == null || anchor == null || getValorLane(from) == getValorLane(anchor)) {
            return false;
        }

        boolean nextToTeammate = anchor.move(0, -1).equals(to) || anchor.move(0, 1).equals(to) || anchor.move(1, 0).equals(to);

        if (!nextToTeammate || !canMoveTo(to) || heroPositions.containsValue(to) || isBehindMonster(to)) {
            return false;
        }

        heroPositions.put(hero, to);
        return true;
    }

    // Returns an on-board hero to original Nexus cell.
    public boolean tryRecall(Hero hero) {
        if (!heroPositions.containsKey(hero)) {
            return false;
        }

        Position spawn = heroSpawnPositions.get(hero);
        if (heroPositions.containsValue(spawn)) {
            return false;
        }

        heroPositions.put(hero, spawn);
        return true;
    }

    private boolean isAdjacent(Position from, Position to) {
        int rowDistance = Math.abs(to.getRow() - from.getRow());
        int colDistance = Math.abs(to.getCol() - from.getCol());
        return rowDistance + colDistance == 1;
    }

    // Clears an adjacent obstacle, turning it into a plain tile.
    public boolean tryClearObstacle(Hero hero, Position target) {
        Position from = heroPositions.get(hero);
        if (from == null || !isInBounds(target) || !isAdjacent(from, target)) {
            return false;
        }

        int row = target.getRow();
        int col = target.getCol();
        Tile tile = tiles[row][col];

        if (tile.getType() != TileType.OBSTACLE) {
            return false;
        }

        tiles[row][col] = new TerrainTile(TileType.PLAIN);
        return true;
    }

    // Removes a hero from the board .
    public void removeHero(Hero hero) {
        heroPositions.remove(hero);
    }

    // Places hero back at original Nexus cell.
    public boolean tryRespawnHero(Hero hero) {
        Position spawn = heroSpawnPositions.get(hero);

        if (spawn == null || heroPositions.containsKey(hero) || heroPositions.containsValue(spawn)) {
            return false;
        }

        heroPositions.put(hero, spawn);
        return true;
    }

    boolean canMoveMonsterTo(Monster monster, Position to) {
        Position from = monsterPositions.get(monster);
        if (from == null || !canMoveTo(to) || !isAdjacent(from, to) || monsterPositions.containsValue(to)) {
            return false;
        }

        int lane = getValorLane(to);
        for (Position heroPosition : heroPositions.values()) {
            if (getValorLane(heroPosition) == lane && to.getRow() > heroPosition.getRow()) {
                return false;
            }
        }

        return true;
    }

    public boolean tryMoveMonster(Monster monster, Position to) {
        if (!canMoveMonsterTo(monster, to)) {
            return false;
        }

        monsterPositions.put(monster, to);
        return true;
    }

    private boolean isInAttackRange(Position from, Position to) {
        int rowDistance = Math.abs(to.getRow() - from.getRow());
        int colDistance = Math.abs(to.getCol() - from.getCol());
        return rowDistance <= 1 && colDistance <= 1;
    }

    // Returns heroes which can be attacked.
    public List<Hero> getAttackableHeroes(Monster monster) {
        List<Hero> targets = new ArrayList<>();
        Position from = monsterPositions.get(monster);
        if (from == null) {
            return targets;
        }

        for (Map.Entry<Hero, Position> entry : heroPositions.entrySet()) {
            if (isInAttackRange(from, entry.getValue())) {
                targets.add(entry.getKey());
            }
        }

        return targets;
    }

    // Returns monsters which can be attacked.
    public List<Monster> getAttackableMonsters(Hero hero) {
        List<Monster> targets = new ArrayList<>();
        Position from = heroPositions.get(hero);
        if (from == null) {
            return targets;
        }

        for (Map.Entry<Monster, Position> entry : monsterPositions.entrySet()) {
            if (isInAttackRange(from, entry.getValue())) {
                targets.add(entry.getKey());
            }
        }

        return targets;
    }

    // Removes a  monster .
    public void removeMonster(Monster monster) {
        monsterPositions.remove(monster);
    }

    public boolean hasReachedEnemyNexus(Hero hero) {
        Position position = heroPositions.get(hero);
        return position != null && position.getRow() == 0;
    }

    public boolean hasReachedEnemyNexus(Monster monster) {
        Position position = monsterPositions.get(monster);
        return position != null && position.getRow() == size - 1;
    }

    public TileType getTileType(Position position) {
        return getTile(position).getType();
    }

    // Returns the market .
    public Optional<Market> getMarketFor(Hero hero) {
        Position position = heroPositions.get(hero);
        if (position == null) {
            return Optional.empty();
        }

        Tile tile = tiles[position.getRow()][position.getCol()];
        if (tile instanceof NexusTile) {
            return ((NexusTile) tile).getMarket();
        }

        return Optional.empty();
    }




}

package game;

import battle.Battle;
import battle.BattleStatus;
import character.Hero;
import character.Monster;
import market.Market;
import world.MapTile;
import world.MarketTile;
import world.MonsterFactory;
import world.Position;
import world.Tile;
import world.World;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Game {

    // Fields
    private static final double ENCOUNTER_RATE = 0.20;

    private final World world;
    private final List<Hero> heroes;
    private final MonsterFactory monsterFactory;
    private final Random random;

    private Position position;
    private Battle currentBattle;
    private boolean gameOver;

    // constructor
    public Game(
            World world,
            List<Hero> heroes,
            MonsterFactory monsterFactory,
            Random random) {

        if (world == null
                || heroes == null
                || heroes.isEmpty()
                || heroes.size() > 3
                || heroes.contains(null)
                || new java.util.HashSet<>(heroes).size() != heroes.size()
                || monsterFactory == null
                || random == null) {
            throw new IllegalArgumentException(
                    "Invalid game configuration."
            );
        }

        this.world = world;
        this.heroes = new ArrayList<>(heroes);
        this.monsterFactory = monsterFactory;
        this.random = random;

        this.position = new Position(
                world.getSize() / 2,
                world.getSize() / 2
        );

        if (!world.canMoveTo(position)) {
            throw new IllegalArgumentException(
                    "Starting position is inaccessible."
            );
        }

        this.currentBattle = null;
        this.gameOver = false;
    }

    // Getters
    public World getWorld() {
        return world;
    }
    public Position getPosition() {
        return position;
    }
    public List<Hero> getHeroes() {
        return Collections.unmodifiableList(heroes);
    }
    public Battle getCurrentBattle() {
        return currentBattle;
    }

    public boolean isGameOver() {
        return gameOver;
    }

    public boolean move(int rowDelta, int colDelta) {

        if (gameOver || currentBattle != null) {
            return false;
        }

        if (Math.abs(rowDelta) + Math.abs(colDelta) != 1) {
            return false;
        }

        Position next = position.move(
                rowDelta, colDelta
        );

        if (!world.canMoveTo(next)) {
            return false;
        }

        position = next;

        Tile tile = world.getTile(position);

        if (tile instanceof MapTile
                && random.nextDouble() < ENCOUNTER_RATE) {

            List<Monster> monsters =
                    monsterFactory.createEncounter(heroes);

            currentBattle = new Battle(heroes, monsters, random);
        }

        return true;
    }

    public Market getCurrentMarket() {

        if (currentBattle != null || gameOver) {
            return null;
        }

        Tile tile = world.getTile(position);

        if (tile instanceof MarketTile) {
            return ((MarketTile) tile).getMarket();
        }

        return null;
    }

    public void finishBattle() {

        if (currentBattle == null
                || !currentBattle.isOver()) {
            return;
        }

        if (currentBattle.getStatus()
                == BattleStatus.MONSTER_VICTORY) {
            gameOver = true;
        }

        currentBattle = null;
    }
}

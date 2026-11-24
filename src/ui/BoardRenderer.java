package ui;

import character.Hero;
import character.Monster;
import world.Position;
import world.TileType;
import world.World;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Displays terrain and both occupants in every cell.
public final class BoardRenderer {
    private BoardRenderer() { }

    public static String render(World world) {
        Map<Position, String> actors = new HashMap<>();
        List<Hero> heroes = world.getRegisteredHeroes();
        for (int i = 0; i < heroes.size(); i++) {
            String label = "H" + (i + 1);
            world.positionOf(heroes.get(i)).ifPresent(p -> actors.put(p, label));
        }
        List<Monster> monsters = world.getMonstersOnBoard();
        for (int i = 0; i < monsters.size(); i++) {
            String label = "M" + (i + 1);
            world.positionOf(monsters.get(i)).ifPresent(p -> actors.merge(p, label, (h, m) -> h + "/" + m));
        }
        int width = 10;
        String border = ("+" + "-".repeat(width)).repeat(world.getSize()) + "+";
        StringBuilder board = new StringBuilder(border);
        for (int r = 0; r < world.getSize(); r++) {
            board.append('\n').append('|');
            for (int c = 0; c < world.getSize(); c++) {
                Position p = new Position(r, c);
                TileType tile = world.getTileType(p);
                String label = String.valueOf(tile == TileType.PLAIN ? 'P' : tile.getSymbol());
                if (actors.containsKey(p)) label += " " + actors.get(p);
                board.append(String.format("%-" + width + "s", label)).append('|');
            }
            board.append('\n').append(border);
        }
        return board.toString();
    }
}

package game;

import character.Hero;
import character.Monster;
import ui.ConsoleUI;
import world.MonsterFactory;
import world.World;
import world.WorldFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

// Loads game data and starts the console UI.
public class Main {

    public static void main(String[] args) {

        Path dataPath = args.length > 0
                ? Paths.get(args[0])
                : Paths.get("Legends_Monsters_and_Heroes");

        try (Scanner scanner = new Scanner(System.in)) {

            Random random = new Random();

            // Load hero and monster templates.
            DataLoader loader = new DataLoader(dataPath);

            List<Hero> candidates = loader.loadHeroes();
            List<Monster> monsterTemplates =
                    loader.loadMonsters();

            // Select the playable hero party.
            List<Hero> party = ConsoleUI.selectHeroes(
                    scanner, candidates
            );

            List<Integer> lanes = ConsoleUI.selectLanes(scanner, party);

            // Prepare monster waves.
            MonsterFactory monsterFactory =
                    new MonsterFactory(
                            monsterTemplates, random
                    );

            // Generate the map and market stock.
            WorldFactory worldFactory =
                    new WorldFactory(loader, random);

            World world = worldFactory.createValorWorld();

            // Initialize game state.
            Game game = new Game(
                    world,
                    party,
                    monsterFactory,
                    random,
                    lanes
            );

            // Start the game loop.
            ConsoleUI ui = new ConsoleUI(
                    game, scanner
            );

            ui.run();

        } catch (IOException e) {
            System.err.println(
                    "Could not read game data: "
                            + e.getMessage()
            );

        } catch (IllegalArgumentException
                 | IllegalStateException e) {
            System.err.println(
                    "Game error: " + e.getMessage()
            );
        }
    }
}
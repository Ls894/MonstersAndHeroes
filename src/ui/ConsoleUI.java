package ui;

import battle.CombatRules;
import battle.BattleStatus;
import character.Hero;
import character.Monster;
import game.Game;
import inventory.Equipment;
import inventory.Hand;
import item.Armor;
import item.Item;
import item.Potion;
import item.Spell;
import item.Weapon;
import market.Market;
import world.Position;
import world.World;
import world.TileType;

// Console UI.

import java.util.*;

public class ConsoleUI {

    // Fields
    private final Game game;
    private final Scanner scanner;
    private boolean quitting;

    public ConsoleUI(Game game, Scanner scanner) {
        if (game == null || scanner == null) {
            throw new IllegalArgumentException(
                    "Game and scanner cannot be null."
            );
        }

        this.game = game;
        this.scanner = scanner;
        this.quitting = false;
    }

    // Hero selection.
    public static List<Hero> selectHeroes(
            Scanner scanner,
            List<Hero> candidates) {

        if (candidates == null || candidates.isEmpty()) {
            throw new IllegalArgumentException(
                    "No heroes available."
            );
        }

        System.out.println("=== Choose Your Party ===");

        final int count = 3;
        if (candidates.size() < count) throw new IllegalArgumentException("Need at least three heroes.");

        List<Hero> selected = new ArrayList<>();

        while (selected.size() < count) {

            System.out.println("\nAvailable Heroes:");

            for (int i = 0; i < candidates.size(); i++) {
                Hero hero = candidates.get(i);

                System.out.printf(
                        "%2d. %-24s %-12s STR %.0f DEX %.0f AGI %.0f%n",
                        i + 1,
                        hero.getName(),
                        hero.getClass().getSimpleName(),
                        hero.getStrength(),
                        hero.getDexterity(),
                        hero.getAgility()
                );
            }

            int index = readInitialNumber(
                    scanner,
                    "Choose hero " + (selected.size() + 1) + ": ",
                    1,
                    candidates.size()
            ) - 1;

            Hero hero = candidates.get(index);

            if (selected.contains(hero)) {
                System.out.println(
                        "This hero is already selected."
                );
                continue;
            }

            selected.add(hero);
            System.out.println(
                    "Selected: " + hero.getName()
            );
        }

        return selected;
    }

    // Read and validate a numeric selection during game setup.
    private static int readInitialNumber(
            Scanner scanner,
            String prompt,
            int min,
            int max) {

        while (true) {
            System.out.print(prompt);

            if (!scanner.hasNextLine()) throw new IllegalStateException("Input ended during setup.");
            String line = scanner.nextLine().trim();

            try {
                int value = Integer.parseInt(line);

                if (value >= min && value <= max) {
                    return value;
                }
            } catch (NumberFormatException ignored) {
                // Ask again.
            }

            System.out.println("Invalid selection.");
        }
    }

    // Read a command and detect quit requests or end of input.
    private String input(String prompt) {
        System.out.print(prompt);

        if (!scanner.hasNextLine()) {
            quitting = true;
            return "q";
        }

        String answer = scanner.nextLine().trim();

        if (answer.equalsIgnoreCase("q")) {
            quitting = true;
        }

        return answer;
    }

    // Read a menu choice and return its zero-based index, or -1 on cancellation.
    private int chooseNumber(
            String prompt,
            int max) {

        if (max <= 0) {
            return -1;
        }

        while (!quitting) {
            String line = input(
                    prompt + " (1-" + max + ", B back): "
            );

            if (quitting || line.equalsIgnoreCase("b")) {
                return -1;
            }

            try {
                int choice = Integer.parseInt(line);

                if (choice >= 1 && choice <= max) {
                    return choice - 1;
                }
            } catch (NumberFormatException ignored) {
                // Ask again.
            }

            System.out.println("Invalid selection.");
        }

        return -1;
    }

    // Display the hero's stats, resources, and equipped items.
    private void printHero(Hero hero) {

        System.out.printf(
                "%n%s (%s) Level %d%n",
                hero.getName(),
                hero.getClass().getSimpleName(),
                hero.getLevel()
        );

        System.out.printf(
                "HP %.1f / %.1f | MP %.1f / %.1f | Gold %d | EXP %d%n",
                hero.getHp(),
                hero.getMaxHp(),
                hero.getMp(),
                hero.getBaseMp(),
                hero.getGold(),
                hero.getExperience()
        );

        System.out.printf(
                "STR %.1f | DEX %.1f | AGI %.1f | DEF %.1f%n",
                hero.getStrength(),
                hero.getDexterity(),
                hero.getAgility(),
                hero.getBonusDefense()
        );

        Equipment equipment = hero.getEquipment();

        System.out.println(
                "Left Hand: " + itemName(equipment.getLeftHand())
        );

        System.out.println(
                "Right Hand: " + itemName(equipment.getRightHand())
        );

        System.out.println(
                "Armor: " + itemName(equipment.getArmor())
        );
    }

    private String itemName(Item item) {
        return item == null ? "Empty" : item.getName();
    }

    // Filter items by type and prompt the player to choose one.
    private <T extends Item> T chooseItem(
            List<Item> source,
            Class<T> type) {

        List<T> matches = new ArrayList<>();

        for (Item item : source) {
            if (type.isInstance(item)) {
                matches.add(type.cast(item));
            }
        }

        if (matches.isEmpty()) {
            System.out.println("No matching items.");
            return null;
        }

        for (int i = 0; i < matches.size(); i++) {
            System.out.println(
                    (i + 1) + ". " + matches.get(i)
            );
        }

        int index = chooseNumber(
                "Choose item", matches.size()
        );

        return index < 0 ? null : matches.get(index);
    }

    // Let the player choose an attack method and a target monster.
    private boolean performAttack(
            Hero hero) {

        List<Weapon> options = new ArrayList<>();
        options.add(null);

        Equipment equipment = hero.getEquipment();

        Weapon left = equipment.getLeftHand();
        Weapon right = equipment.getRightHand();

        if (left != null) {
            options.add(left);
        }

        if (right != null && right != left) {
            options.add(right);
        }

        System.out.println("1. Unarmed Attack");

        for (int i = 1; i < options.size(); i++) {
            System.out.println(
                    (i + 1) + ". " + options.get(i)
            );
        }

        int weaponIndex = chooseNumber(
                "Choose attack", options.size()
        );

        if (weaponIndex < 0) {
            return false;
        }

        Monster target = chooseMonster(hero);

        if (target == null) {
            return false;
        }

        return game.heroAttack(hero, target, options.get(weaponIndex));
    }

    // Let the player select a spell and a target monster.
    private boolean performSpell(
            Hero hero) {

        Spell spell = chooseItem(
                hero.getInventory().getItems(),
                Spell.class
        );

        if (spell == null) {
            return false;
        }

        Monster target = chooseMonster(hero);

        if (target == null) {
            return false;
        }

        return game.heroCastSpell(hero, target, spell);
    }

    // Select exactly one starting lane per hero, using the same registration order.
    public static List<Integer> selectLanes(Scanner scanner, List<Hero> heroes) {
        List<Integer> lanes = new ArrayList<>();
        for (Hero hero : heroes) {
            while (true) {
                int lane = readInitialNumber(scanner, hero.getName()
                        + " starting lane (1 Top, 2 Mid, 3 Bottom): ", 1, 3) - 1;
                if (!lanes.contains(lane)) {
                    lanes.add(lane);
                    break;
                }
                System.out.println("This lane is already assigned.");
            }
        }
        return lanes;
    }

    // Delegate board display to the board renderer.
    public static String renderBoard(World world) {
        return BoardRenderer.render(world);
    }

    // Run the console game loop until victory, defeat, or quit.
    public void run() {
        System.out.println("=== Legends of Valor ===");
        printInstructions();
        showEvents();
        while (!quitting && !game.isGameOver()) {
            for (Hero hero : game.getHeroes()) {
                while (!quitting && game.canHeroAct(hero)) {
                    System.out.println("\nRound " + game.getRound() + " | Hero phase");
                    System.out.println(renderBoard(game.getWorld()));
                    showCurrentHero(hero);
                    String action = input("Action (H for help): ").toLowerCase(Locale.ROOT);
                    if (quitting) break;
                    if (performAction(hero, action)) {
                        showEvents();
                    }
                }
                if (quitting || game.isGameOver()) break;
            }
            if (!quitting && !game.isGameOver()) {
                System.out.println("\n--- Monster phase ---");
                game.advanceRound();
                showEvents();
            }
        }
        if (game.isGameOver()) {
            System.out.println(renderBoard(game.getWorld()));
            System.out.println(game.getStatus() == BattleStatus.HERO_VICTORY
                    ? "Victory! A hero reached the monsters' Nexus."
                    : "Defeat! A monster reached the heroes' Nexus.");
        } else {
            System.out.println("Thanks for playing!");
        }
    }

    // Print and clear messages generated by the game.
    private void showEvents() {
        for (String event : game.drainEvents()) System.out.println(event);
    }

    // Display the active hero, terrain bonuses, and nearby targets.
    private void showCurrentHero(Hero hero) {
        TileType terrain = game.terrainOf(hero);
        int id = game.getHeroes().indexOf(hero) + 1;
        System.out.printf("H%d %s | HP %.1f | MP %.1f | Gold %d | %s | %s%n",
                id, hero.getName(), hero.getHp(), hero.getMp(), hero.getGold(),
                game.getWorld().positionOf(hero).get(), terrain);
        System.out.printf("Effective STR %.1f | DEX %.1f | AGI %.1f%n",
                CombatRules.effectiveStrength(hero, terrain),
                CombatRules.effectiveDexterity(hero, terrain),
                CombatRules.effectiveAgility(hero, terrain));
        List<Monster> targets = game.getAttackableMonsters(hero);
        System.out.print("Targets in range: ");
        if (targets.isEmpty()) System.out.println("None");
        else {
            for (Monster monster : targets) System.out.print(monsterLabel(monster) + " ");
            System.out.println();
        }
    }

    // Route player commands to the appropriate game actions.
    private boolean performAction(Hero hero, String action) {
        boolean success;
        switch (action) {
            case "w": success = game.move(hero, -1, 0); break;
            case "a": success = game.move(hero, 0, -1); break;
            case "s": success = game.move(hero, 1, 0); break;
            case "d": success = game.move(hero, 0, 1); break;
            case "f": success = performAttack(hero); break;
            case "c": success = performSpell(hero); break;
            case "e": success = handleEquipment(hero); break;
            case "p":
                Potion potion = chooseItem(hero.getInventory().getItems(), Potion.class);
                success = potion != null && game.heroUsePotion(hero, potion);
                break;
            case "t": success = teleport(hero); break;
            case "r": success = game.recall(hero); break;
            case "o": success = clearObstacle(hero); break;
            case "x": success = game.pass(hero); break;
            case "m": openMarket(hero); return false;
            case "i":
                for (Hero member : game.getHeroes()) printHero(member);
                for (Monster monster : game.getWorld().getMonstersOnBoard()) {
                    System.out.printf("%s %s | Level %d | HP %.1f | %s%n", monsterLabel(monster),
                            monster.getName(), monster.getLevel(), monster.getHp(),
                            game.getWorld().positionOf(monster).get());
                }
                return false;
            case "v": hero.getInventory().display(); return false;
            case "h": printInstructions(); return false;
            default: System.out.println("Unknown command."); return false;
        }
        if (!success && !quitting) System.out.println("Action unavailable or cancelled. Try again.");
        return success;
    }

    // Generate the monster label from its current board-list index.
    private String monsterLabel(Monster monster) {
        return "M" + (game.getWorld().getMonstersOnBoard().indexOf(monster) + 1);
    }

    // Prompt the player to select a monster within attack range.
    private Monster chooseMonster(Hero hero) {
        List<Monster> targets = game.getAttackableMonsters(hero);
        if (targets.isEmpty()) {
            System.out.println("No monsters in range.");
            return null;
        }
        for (int i = 0; i < targets.size(); i++) {
            Monster monster = targets.get(i);
            System.out.printf("%d. %s %s | HP %.1f | %s%n", i + 1, monsterLabel(monster),
                    monster.getName(), monster.getHp(), game.getWorld().positionOf(monster).get());
        }
        int choice = chooseNumber("Choose target", targets.size());
        return choice < 0 ? null : targets.get(choice);
    }

    // Choose a teammate in another lane and a destination beside them.
    private boolean teleport(Hero hero) {
        List<Hero> teammates = new ArrayList<>();
        World world = game.getWorld();
        int lane = world.getValorLane(world.positionOf(hero).get());
        for (Hero teammate : game.getHeroes()) {
            if (teammate != hero && teammate.isAlive() && world.positionOf(teammate).isPresent()
                    && world.getValorLane(world.positionOf(teammate).get()) != lane) teammates.add(teammate);
        }
        if (teammates.isEmpty()) {
            System.out.println("No living teammate in another lane.");
            return false;
        }
        for (int i = 0; i < teammates.size(); i++) {
            System.out.println((i + 1) + ". " + teammates.get(i).getName() + " "
                    + world.positionOf(teammates.get(i)).get());
        }
        int index = chooseNumber("Teleport to teammate", teammates.size());
        if (index < 0) return false;
        Hero teammate = teammates.get(index);
        Position anchor = world.positionOf(teammate).get();
        System.out.println("1 Left | 2 Right | 3 Behind");
        int direction = chooseNumber("Destination", 3);
        if (direction < 0) return false;
        Position target = direction == 0 ? anchor.move(0, -1)
                : direction == 1 ? anchor.move(0, 1) : anchor.move(1, 0);
        return game.teleport(hero, teammate, target);
    }

    // Prompt for the direction of the obstacle to remove.
    private boolean clearObstacle(Hero hero) {
        System.out.println("Choose adjacent obstacle: W/A/S/D, B back");
        String direction = input("> ").toLowerCase(Locale.ROOT);
        Position from = game.getWorld().positionOf(hero).get();
        switch (direction) {
            case "w": return game.clearObstacle(hero, from.move(-1, 0));
            case "a": return game.clearObstacle(hero, from.move(0, -1));
            case "s": return game.clearObstacle(hero, from.move(1, 0));
            case "d": return game.clearObstacle(hero, from.move(0, 1));
            default: return false;
        }
    }

    // Handle market transactions without ending the hero's turn.
    private void openMarket(Hero hero) {
        Market market = game.getMarketFor(hero).orElse(null);
        if (market == null) {
            System.out.println("This hero is not at a friendly Nexus market.");
            return;
        }
        while (!quitting) {
            System.out.println("\n=== Market: " + hero.getName() + " | Gold " + hero.getGold() + " ===");
            System.out.println("1 Buy | 2 Sell | 3 Repair | 4 Leave");
            String choice = input("> ");
            if (quitting || choice.equals("4") || choice.equalsIgnoreCase("b")) return;
            if (!choice.equals("1") && !choice.equals("2") && !choice.equals("3")) {
                System.out.println("Invalid option.");
                continue;
            }
            Item item = chooseItem(choice.equals("1") ? market.getStock()
                    : hero.getInventory().getItems(), Item.class);
            if (item == null) continue;
            boolean success = choice.equals("1") ? game.buy(hero, item)
                    : choice.equals("2") ? game.sell(hero, item) : game.repair(hero, item);
            System.out.println(success ? "Transaction successful." : "Transaction failed.");
        }
    }

    // Handle weapon and armor changes for the current hero.
    private boolean handleEquipment(Hero hero) {
        System.out.println("1 Left Hand | 2 Right Hand | 3 Two Hands");
        System.out.println("4 Armor | 5 Unequip Weapon | 6 Unequip Armor | B Back");
        String choice = input("> ");
        if (quitting) return false;
        switch (choice) {
            case "1": case "2": case "3": case "5":
                Weapon weapon = chooseItem(hero.getInventory().getItems(), Weapon.class);
                if (weapon == null) return false;
                if (choice.equals("5")) return game.heroUnequipWeapon(hero, weapon);
                if (choice.equals("3")) return game.heroEquipTwoHands(hero, weapon);
                return game.heroEquipOneHand(hero, weapon, choice.equals("1") ? Hand.LEFT : Hand.RIGHT);
            case "4":
                Armor armor = chooseItem(hero.getInventory().getItems(), Armor.class);
                return armor != null && game.heroEquipArmor(hero, armor);
            case "6": return game.heroUnequipArmor(hero);
            default: return false;
        }
    }

    // Display available commands and basic game rules.
    private void printInstructions() {
        System.out.println("W/A/S/D Move | F Attack | C Spell | E Equip | P Potion");
        System.out.println("T Teleport | R Recall | O Clear obstacle | X Pass");
        System.out.println("M Market | I Info | V Inventory | H Help | Q Quit");
        System.out.println("One valid action per hero per round. Market and information are free.");
        System.out.println("Reach the enemy Nexus to win. N Nexus, I Wall, X Obstacle, P Plain, B Bush, C Cave, K Koulou.");
    }
}
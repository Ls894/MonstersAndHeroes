package ui;

import battle.Battle;
import battle.BattlePhase;
import battle.BattleStatus;
import character.Hero;
import character.Monster;
import game.Game;
import game.Main;
import inventory.Equipment;
import inventory.Hand;
import inventory.Inventory;
import item.Armor;
import item.Item;
import item.Potion;
import item.Spell;
import item.Weapon;
import market.Market;
import world.Position;
import world.World;

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

        int count = readInitialNumber(
                scanner,
                "Number of heroes (1-3): ",
                1,
                3
        );

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
    public static String renderBoard(World world) {
        Map<Position, String> actorLabels = new HashMap<>();

        List<Hero> heroes = world.getRegisteredHeroes();
        for (int i = 0; i < heroes.size(); i++) {
            String label = "H" + (i + 1);
            world.positionOf(heroes.get(i)).ifPresent(position -> actorLabels.put(position, label));
        }

        List<Monster> monsters = world.getMonstersOnBoard();
        for (int i = 0; i < monsters.size(); i++) {
            String label = "M" + (i + 1);
            world.positionOf(monsters.get(i)).ifPresent(position -> actorLabels.merge(position, label, (heroLabel, monsterLabel) -> heroLabel + "/" + monsterLabel));
        }

        int size = world.getSize();
        int cellWidth = 7;
        String border = ("+" + "=".repeat(cellWidth)).repeat(size) + "+";
        String newline = System.lineSeparator();
        StringBuilder board = new StringBuilder(border);

        for (int row = 0; row < size; row++) {
            board.append(newline).append('|');

            for (int col = 0; col < size; col++) {
                Position position = new Position(row, col);
                String label = actorLabels.get(position);
                if (label == null) {
                    label = String.valueOf(world.getTileType(position).getSymbol());
                }

                int padding = cellWidth - label.length();
                int leftPadding = padding / 2;
                board.append(" ".repeat(leftPadding))
                        .append(label)
                        .append(" ".repeat(padding - leftPadding))
                        .append('|');
            }

            board.append(newline).append(border);
        }

        return board.toString();
    }

    private static int readInitialNumber(
            Scanner scanner,
            String prompt,
            int min,
            int max) {

        while (true) {
            System.out.print(prompt);

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

    // Game.Main game loop.
    public void run() {

        printInstructions();

        while (!quitting && !game.isGameOver()) {

            if (game.getCurrentBattle() != null) {
                playBattle();
                continue;
            }

            System.out.println(
                    "\n" + game.getWorld().render(
                            game.getPosition()
                    )
            );

            System.out.println(
                    "W/A/S/D Move | M Market | I Info"
            );
            System.out.println(
                    "V Inventory | E Equipment | P Potion"
            );
            System.out.println(
                    "H Help | Q Quit"
            );

            String command = input("> ").toLowerCase();

            if (quitting) {
                break;
            }

            switch (command) {
                case "w":
                    move(-1, 0);
                    break;

                case "s":
                    move(1, 0);
                    break;

                case "a":
                    move(0, -1);
                    break;

                case "d":
                    move(0, 1);
                    break;

                case "m":
                    openMarket();
                    break;

                case "i":
                    showPartyInformation();
                    break;

                case "v":
                    showInventories();
                    break;

                case "e":
                    Hero equipHero = chooseHero();

                    if (equipHero != null) {
                        handleEquipment(equipHero, null);
                    }
                    break;

                case "p":
                    Hero potionHero = chooseHero();

                    if (potionHero != null) {
                        Potion potion = chooseItem(
                                potionHero.getInventory().getItems(),
                                Potion.class
                        );

                        if (potion != null) {
                            System.out.println(
                                    "Potion used: "
                                            + potionHero.usePotion(potion)
                            );
                        }
                    }
                    break;

                case "h":
                    printInstructions();
                    break;

                default:
                    System.out.println("Unknown command.");
                    break;
            }
        }

        if (game.isGameOver()) {
            System.out.println(
                    "\nAll heroes fainted. Game Over."
            );
        } else {
            System.out.println("\nThanks for playing!");
        }
    }

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

    // Movement.
    private void move(int dr, int dc) {

        if (!game.move(dr, dc)) {
            System.out.println("Cannot move there.");
            return;
        }

        System.out.println(
                "Moved to " + game.getPosition()
        );

        if (game.getCurrentBattle() != null) {
            System.out.println(
                    "\nA group of monsters appeared!"
            );
        }
    }

    // Party information.
    private void showPartyInformation() {

        for (Hero hero : game.getHeroes()) {
            printHero(hero);
        }
    }

    private void printHero(Hero hero) {

        System.out.printf(
                "%n%s (%s) Level %d%n",
                hero.getName(),
                hero.getClass().getSimpleName(),
                hero.getLevel()
        );

        System.out.printf(
                "HP %.1f | MP %.1f | Gold %d | EXP %d%n",
                hero.getHp(),
                hero.getMp(),
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

    private void showInventories() {

        for (Hero hero : game.getHeroes()) {
            System.out.println(
                    "\n" + hero.getName() + "'s Inventory:"
            );

            hero.getInventory().display();
        }
    }

    private Hero chooseHero() {

        List<Hero> heroes = game.getHeroes();

        for (int i = 0; i < heroes.size(); i++) {
            System.out.printf(
                    "%d. %s%n",
                    i + 1,
                    heroes.get(i).getName()
            );
        }

        int index = chooseNumber(
                "Choose hero", heroes.size()
        );

        return index < 0 ? null : heroes.get(index);
    }

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
    // Market
    private void openMarket() {

        Market market = game.getCurrentMarket();

        if (market == null) {
            System.out.println(
                    "You are not on a market tile."
            );
            return;
        }

        while (!quitting) {

            System.out.println(
                    "\n=== Market ==="
            );
            System.out.println(
                    "1 Buy | 2 Sell | 3 Repair | 4 Leave"
            );

            String choice = input("> ");

            if (quitting || choice.equals("4")) {
                return;
            }

            if (!choice.equals("1")
                    && !choice.equals("2")
                    && !choice.equals("3")) {
                System.out.println("Invalid option.");
                continue;
            }

            Hero hero = chooseHero();

            if (hero == null) {
                continue;
            }

            System.out.println(
                    "Gold: " + hero.getGold()
            );

            switch (choice) {
                case "1":
                    buyFromMarket(market, hero);
                    break;

                case "2":
                    sellToMarket(market, hero);
                    break;

                case "3":
                    repairAtMarket(market, hero);
                    break;

                default:
                    break;
            }
        }
    }

    private void buyFromMarket(
            Market market,
            Hero hero) {

        Item item = chooseItem(
                market.getStock(), Item.class
        );

        if (item != null) {
            System.out.println(
                    market.buyItem(hero, item)
                            ? "Purchase successful."
                            : "Cannot purchase this item."
            );
        }
    }

    private void sellToMarket(
            Market market,
            Hero hero) {

        Item item = chooseItem(
                hero.getInventory().getItems(),
                Item.class
        );

        if (item != null) {
            System.out.println(
                    market.sellItem(hero, item)
                            ? "Item sold."
                            : "Cannot sell this item."
            );
        }
    }

    private void repairAtMarket(
            Market market,
            Hero hero) {

        Item item = chooseItem(
                hero.getInventory().getItems(),
                Item.class
        );

        if (item == null) {
            return;
        }

        boolean success;

        if (item instanceof Weapon) {
            success = market.repairWeapon(
                    hero, (Weapon) item
            );
        } else if (item instanceof Armor) {
            success = market.repairArmor(
                    hero, (Armor) item
            );
        } else {
            System.out.println(
                    "This item cannot be repaired."
            );
            return;
        }

        System.out.println(
                success ? "Repair successful."
                        : "Cannot repair this item."
        );
    }
    // Equipment
    private boolean handleEquipment(
            Hero hero,
            Battle battle) {

        System.out.println(
                "1 Left Hand | 2 Right Hand | 3 Two Hands"
        );
        System.out.println(
                "4 Armor | 5 Unequip Weapon | 6 Unequip Armor"
        );

        String choice = input("> ");

        if (quitting) {
            return false;
        }

        Equipment equipment = hero.getEquipment();

        switch (choice) {
            case "1":
            case "2":
            case "3": {
                Weapon weapon = chooseItem(
                        hero.getInventory().getItems(),
                        Weapon.class
                );

                if (weapon == null) {
                    return false;
                }

                if (battle == null) {
                    if (choice.equals("3")) {
                        return equipment.equipTwoHands(weapon);
                    }

                    return equipment.equipOneHand(
                            weapon,
                            choice.equals("1")
                                    ? Hand.LEFT : Hand.RIGHT
                    );
                }

                if (choice.equals("3")) {
                    return battle.heroEquipTwoHands(
                            hero, weapon
                    );
                }

                return battle.heroEquipOneHand(
                        hero,
                        weapon,
                        choice.equals("1")
                                ? Hand.LEFT : Hand.RIGHT
                );
            }

            case "4": {
                Armor armor = chooseItem(
                        hero.getInventory().getItems(),
                        Armor.class
                );

                if (armor == null) {
                    return false;
                }

                return battle == null
                        ? equipment.equipArmor(armor)
                        : battle.heroEquipArmor(hero, armor);
            }

            case "5": {
                Weapon weapon = chooseItem(
                        hero.getInventory().getItems(),
                        Weapon.class
                );

                if (weapon == null) {
                    return false;
                }

                return battle == null
                        ? equipment.unequipWeapon(weapon)
                        : battle.heroUnequipWeapon(hero, weapon);
            }

            case "6":
                return battle == null
                        ? equipment.unequipArmor()
                        : battle.heroUnequipArmor(hero);

            default:
                System.out.println("Invalid option.");
                return false;
        }
    }

    // Battle part
    private void playBattle() {

        Battle battle = game.getCurrentBattle();

        while (!quitting && !battle.isOver()) {

            System.out.println(
                    "\n=== Battle Round "
                            + battle.getRound() + " ==="
            );

            showBattleInformation(battle);

            if (battle.getPhase()
                    == BattlePhase.HERO_PHASE) {

                for (Hero hero : battle.getHeroes()) {

                    if (quitting || battle.isOver()) {
                        break;
                    }

                    if (!battle.canHeroAct(hero)) {
                        continue;
                    }

                    performHeroAction(battle, hero);
                }

            } else if (battle.getPhase()
                    == BattlePhase.MONSTER_PHASE) {

                List<Double> hpBefore = new ArrayList<>();

                for (Hero hero : battle.getHeroes()) {
                    hpBefore.add(hero.getHp());
                }

                if (battle.runMonsterTurn()) {
                    System.out.println("\nMonster phase:");

                    for (String event : battle.getLastMonsterEvents()) {
                        System.out.println(event);
                    }

                    // Show HP after regeneration.
                    for (int i = 0; i < battle.getHeroes().size(); i++) {
                        Hero hero = battle.getHeroes().get(i);
                        System.out.printf(
                                "%s HP: %.1f -> %.1f (including regeneration)%n",
                                hero.getName(), hpBefore.get(i), hero.getHp()
                        );
                    }
                }

                if (!battle.isOver()) {
                    battle.nextRound();
                }
            }
        }

        if (quitting) {
            return;
        }

        System.out.println(
                "\nBattle Result: " + battle.getStatus()
        );

        if (battle.getStatus()
                == BattleStatus.HERO_VICTORY) {
            System.out.println(
                    "Victory! Rewards have been applied."
            );
        } else {
            System.out.println(
                    "All heroes have fainted."
            );
        }

        game.finishBattle();
    }

    private void showBattleInformation(Battle battle) {

        System.out.println("\nHeroes:");

        for (Hero hero : battle.getHeroes()) {
            printHero(hero);
        }

        System.out.println("\nMonsters:");

        List<Monster> monsters = battle.getMonsters();

        for (int i = 0; i < monsters.size(); i++) {
            Monster monster = monsters.get(i);

            System.out.printf(
                    "%d. %s Lv%d HP %.1f DMG %.1f DEF %.1f DODGE %.1f%n",
                    i + 1,
                    monster.getName(),
                    monster.getLevel(),
                    monster.getHp(),
                    monster.getDamage(),
                    monster.getDefense(),
                    monster.getDodgeChance()
            );
        }
    }

    private Monster chooseMonster(Battle battle) {

        List<Monster> alive = battle.getAliveMonsters();

        for (int i = 0; i < alive.size(); i++) {
            System.out.printf(
                    "%d. %s (HP %.1f)%n",
                    i + 1,
                    alive.get(i).getName(),
                    alive.get(i).getHp()
            );
        }

        int index = chooseNumber(
                "Choose target", alive.size()
        );

        return index < 0 ? null : alive.get(index);
    }

    private void performHeroAction(
            Battle battle,
            Hero hero) {

        while (!quitting && battle.canHeroAct(hero)) {

            System.out.println(
                    "\n" + hero.getName() + "'s Turn"
            );

            System.out.println(
                    "A Attack | S Spell | P Potion"
            );
            System.out.println(
                    "E Equipment | I Information | Q Quit"
            );

            String choice = input("> ").toLowerCase();

            if (quitting) {
                return;
            }

            boolean success = false;

            switch (choice) {
                case "a":
                    success = performAttack(battle, hero);
                    break;

                case "s":
                    success = performSpell(battle, hero);
                    break;

                case "p": {
                    Potion potion = chooseItem(
                            hero.getInventory().getItems(),
                            Potion.class
                    );

                    if (potion != null) {
                        success = battle.heroUsePotion(
                                hero, potion
                        );

                        if (success) {
                            System.out.println(
                                    hero.getName()
                                            + " used " + potion.getName()
                            );
                        }
                    }
                    break;
                }

                case "e":
                    success = handleEquipment(hero, battle);
                    break;

                case "i":
                    showBattleInformation(battle);
                    continue;

                default:
                    System.out.println("Unknown action.");
                    continue;
            }

            if (!quitting && !success) {
                System.out.println(
                        "Action failed or cancelled."
                );
            }
        }
    }

    private boolean performAttack(
            Battle battle,
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

        Monster target = chooseMonster(battle);

        if (target == null) {
            return false;
        }

        double before = target.getHp();

        boolean success = battle.heroAttack(
                hero, target, options.get(weaponIndex)
        );

        if (success) {
            double lost = before - target.getHp();

            System.out.printf(
                    "%s attacked %s for %.1f damage.%n",
                    hero.getName(),
                    target.getName(),
                    lost
            );

            if (lost == 0) {
                System.out.println(
                        "Attack was dodged or blocked."
                );
            }
        }

        return success;
    }

    private boolean performSpell(
            Battle battle,
            Hero hero) {

        Spell spell = chooseItem(
                hero.getInventory().getItems(),
                Spell.class
        );

        if (spell == null) {
            return false;
        }

        Monster target = chooseMonster(battle);

        if (target == null) {
            return false;
        }

        double before = target.getHp();

        boolean success = battle.heroCastSpell(
                hero, target, spell
        );

        if (success) {
            System.out.printf(
                    "%s cast %s on %s for %.1f damage.%n",
                    hero.getName(),
                    spell.getName(),
                    target.getName(),
                    before - target.getHp()
            );
        }

        return success;
    }

    // Instructions for player
    private void printInstructions() {

        System.out.println(
                "\n=== Legends: Monsters and Heroes ==="
        );

        System.out.println(
                "Move using W, A, S, D."
        );

        System.out.println(
                "P = Party, M = Market, # = Blocked, . = Common."
        );

        System.out.println(
                "Common tiles may trigger monster encounters."
        );

        System.out.println(
                "At markets, buy, sell, or repair equipment."
        );

        System.out.println(
                "During battle, each living hero acts once per round."
        );

        System.out.println(
                "Defeat every monster to earn gold and experience."
        );

        System.out.println(
                "Type I for information or Q to quit."
        );

        System.out.println(
                "Use B to cancel a numbered selection."
        );
    }
}

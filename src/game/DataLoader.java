package game;

import character.Dragon;
import character.Exoskeleton;
import character.Hero;
import character.Monster;
import character.Paladin;
import character.Sorcerer;
import character.Spirit;
import character.Warrior;
import item.Armor;
import item.FireSpell;
import item.IceSpell;
import item.Item;
import item.LightningSpell;
import item.Potion;
import item.PotionAttribute;
import item.Spell;
import item.Weapon;

// Class for data loader

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class DataLoader {

    // fields
    private static final int WEAPON_DURABILITY = 10;
    private static final int ARMOR_DURABILITY = 10;
    private static final int SPELL_USES = 3;

    private final Path directory;

    public DataLoader(Path directory) {
        if (directory == null) {
            throw new IllegalArgumentException(
                    "Data directory cannot be null."
            );
        }

        this.directory = directory;
    }

    private List<String[]> readRows(String fileName)
            throws IOException {

        Path file = directory.resolve(fileName);

        List<String> lines = Files.readAllLines(
                file, StandardCharsets.UTF_8
        );

        List<String[]> rows = new ArrayList<>();

        // The first line is the column header.
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();

            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            rows.add(line.split("\\s+"));
        }

        return rows;
    }

    // Load for heroes
    public List<Hero> loadHeroes() throws IOException {
        List<Hero> heroes = new ArrayList<>();

        loadHeroType("Warriors.txt", "Warrior", heroes);
        loadHeroType("Sorcerers.txt", "Sorcerer", heroes);
        loadHeroType("Paladins.txt", "Paladin", heroes);

        return heroes;
    }

    private void loadHeroType(
            String fileName,
            String type,
            List<Hero> heroes) throws IOException {

        for (String[] p : readRows(fileName)) {
            String name = p[0];

            double mana = Double.parseDouble(p[1]);
            double strength = Double.parseDouble(p[2]);
            double agility = Double.parseDouble(p[3]);
            double dexterity = Double.parseDouble(p[4]);

            int gold = Integer.parseInt(p[5]);
            int experience = Integer.parseInt(p[6]);

            Hero hero;

            switch (type) {
                case "Warrior":
                    hero = new Warrior(
                            name, 1, mana,
                            strength, dexterity, agility,
                            gold, experience
                    );
                    break;

                case "Sorcerer":
                    hero = new Sorcerer(
                            name, 1, mana,
                            strength, dexterity, agility,
                            gold, experience
                    );
                    break;

                case "Paladin":
                    hero = new Paladin(
                            name, 1, mana,
                            strength, dexterity, agility,
                            gold, experience
                    );
                    break;

                default:
                    throw new IllegalArgumentException(
                            "Unknown hero type: " + type
                    );
            }

            heroes.add(hero);
        }
    }

    // Monster templates
    public List<Monster> loadMonsters()
            throws IOException {

        List<Monster> monsters = new ArrayList<>();

        loadMonsterType(
                "Dragons.txt", "Monster.Dragon", monsters
        );

        loadMonsterType(
                "Exoskeletons.txt", "Monster.Exoskeleton", monsters
        );

        loadMonsterType(
                "Spirits.txt", "Spirit", monsters
        );

        return monsters;
    }

    private void loadMonsterType(
            String fileName,
            String type,
            List<Monster> monsters) throws IOException {

        for (String[] p : readRows(fileName)) {
            String name = p[0];
            int level = Integer.parseInt(p[1]);
            double damage = Double.parseDouble(p[2]);
            double defense = Double.parseDouble(p[3]);
            double dodge = Double.parseDouble(p[4]);

            Monster monster;

            switch (type) {
                case "Monster.Dragon":
                    monster = new Dragon(
                            name, level, damage, defense, dodge
                    );
                    break;

                case "Monster.Exoskeleton":
                    monster = new Exoskeleton(
                            name, level, damage, defense, dodge
                    );
                    break;

                case "Spirit":
                    monster = new Spirit(
                            name, level, damage, defense, dodge
                    );
                    break;

                default:
                    throw new IllegalArgumentException(
                            "Unknown monster type: " + type
                    );
            }

            monsters.add(monster);
        }
    }

    // Items part
    public List<Item> loadItems() throws IOException {
        List<Item> items = new ArrayList<>();

        loadWeapons(items);
        loadArmors(items);
        loadPotions(items);

        loadSpells("FireSpells.txt", "Fire", items);
        loadSpells("IceSpells.txt", "Ice", items);
        loadSpells(
                "LightningSpells.txt", "Lightning", items
        );

        return items;
    }

    private void loadWeapons(List<Item> items)
            throws IOException {

        for (String[] p : readRows("Weaponry.txt")) {
            items.add(new Weapon(
                    p[0],
                    Integer.parseInt(p[1]),
                    Integer.parseInt(p[2]),
                    Double.parseDouble(p[3]),
                    Integer.parseInt(p[4]),
                    WEAPON_DURABILITY
            ));
        }
    }

    private void loadArmors(List<Item> items)
            throws IOException {

        for (String[] p : readRows("Armory.txt")) {
            items.add(new Armor(
                    p[0],
                    Integer.parseInt(p[1]),
                    Integer.parseInt(p[2]),
                    Double.parseDouble(p[3]),
                    ARMOR_DURABILITY
            ));
        }
    }

    private void loadPotions(List<Item> items)
            throws IOException {

        for (String[] p : readRows("Potions.txt")) {
            StringBuilder attributes = new StringBuilder();

            for (int i = 4; i < p.length; i++) {
                if (attributes.length() > 0) {
                    attributes.append(" ");
                }
                attributes.append(p[i]);
            }

            items.add(new Potion(
                    p[0],
                    Integer.parseInt(p[1]),
                    Integer.parseInt(p[2]),
                    Double.parseDouble(p[3]),
                    parsePotionAttributes(attributes.toString())
            ));
        }
    }

    private EnumSet<PotionAttribute> parsePotionAttributes(
            String text) {

        if (text.trim().startsWith("All")) {
            return EnumSet.allOf(PotionAttribute.class);
        }

        EnumSet<PotionAttribute> result =
                EnumSet.noneOf(PotionAttribute.class);

        for (String attribute : text.split("/")) {
            String key = attribute.trim().toUpperCase();

            switch (key) {
                case "HEALTH":
                    result.add(PotionAttribute.HEALTH);
                    break;

                case "MANA":
                    result.add(PotionAttribute.MANA);
                    break;

                case "STRENGTH":
                    result.add(PotionAttribute.STRENGTH);
                    break;

                case "DEXTERITY":
                    result.add(PotionAttribute.DEXTERITY);
                    break;

                case "AGILITY":
                    result.add(PotionAttribute.AGILITY);
                    break;

                case "DEFENSE":
                    result.add(PotionAttribute.DEFENSE);
                    break;

                default:
                    throw new IllegalArgumentException(
                            "Unknown potion attribute: " + key
                    );
            }
        }

        return result;
    }

    private void loadSpells(
            String fileName,
            String type,
            List<Item> items) throws IOException {

        for (String[] p : readRows(fileName)) {
            String name = p[0];
            int price = Integer.parseInt(p[1]);
            int level = Integer.parseInt(p[2]);
            double damage = Double.parseDouble(p[3]);
            double manaCost = Double.parseDouble(p[4]);

            Spell spell;

            switch (type) {
                case "Fire":
                    spell = new FireSpell(
                            name, price, level,
                            damage, manaCost, SPELL_USES
                    );
                    break;

                case "Ice":
                    spell = new IceSpell(
                            name, price, level,
                            damage, manaCost, SPELL_USES
                    );
                    break;

                case "Lightning":
                    spell = new LightningSpell(
                            name, price, level,
                            damage, manaCost, SPELL_USES
                    );
                    break;

                default:
                    throw new IllegalArgumentException(
                            "Unknown spell type: " + type
                    );
            }

            items.add(spell);
        }
    }
}

package world;

import character.Dragon;
import character.Exoskeleton;
import character.Hero;
import character.Monster;
import character.Spirit;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MonsterFactory {

    private final List<Monster> templates;
    private final Random random;

    public MonsterFactory(
            List<Monster> templates,
            Random random) {

        if (templates == null || templates.isEmpty()
                || templates.contains(null)
                || random == null) {
            throw new IllegalArgumentException(
                    "Invalid monster factory configuration."
            );
        }

        this.templates = new ArrayList<>(templates);
        this.random = random;
    }

    public List<Monster> createEncounter(
            List<Hero> heroes) {

        if (heroes == null
                || heroes.isEmpty()
                || heroes.size() > 3
                || heroes.contains(null)) {
            throw new IllegalArgumentException(
                    "An encounter requires 1 to 3 heroes."
            );
        }

        int targetLevel = 1;

        for (Hero hero : heroes) {
            targetLevel = Math.max(
                    targetLevel,
                    hero.getLevel()
            );
        }

        List<Monster> encounter = new ArrayList<>();

        for (int i = 0; i < heroes.size(); i++) {
            encounter.add(createMonster(targetLevel));
        }

        return encounter;
    }

    private Monster createScaledMonster(
            Monster template,
            int targetLevel) {

        double scale =
                (double) targetLevel / template.getLevel();

        double damage = template.getDamage() * scale;
        double defense = template.getDefense() * scale;
        double dodge = template.getDodgeChance();

        String name = template.getName();

        if (template instanceof Dragon) {
            return new Dragon(
                    name, targetLevel, damage, defense, dodge
            );
        }

        if (template instanceof Exoskeleton) {
            return new Exoskeleton(
                    name, targetLevel, damage, defense, dodge
            );
        }

        if (template instanceof Spirit) {
            return new Spirit(
                    name, targetLevel, damage, defense, dodge
            );
        }

        throw new IllegalArgumentException(
                "Unsupported monster type."
        );
    }

    // Creates one random monster at the requested level.
    public Monster createMonster(int level) {
        Monster template = templates.get(random.nextInt(templates.size()));
        return createScaledMonster(template, level);
    }
}

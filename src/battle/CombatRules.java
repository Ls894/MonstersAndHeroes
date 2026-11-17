package battle;

import character.Hero;
import character.Monster;
import inventory.Equipment;
import item.Armor;
import item.Spell;
import item.Weapon;

// Combat rules.

import java.util.Random;

public final class CombatRules {

    // Fields
    private static final double ATTRIBUTE_SCALE = 10000.0;
    private static final double TWO_HAND_BONUS = 1.20;

    private static final double MONSTER_DODGE_SCALE = 0.004;
    private static final double HERO_DODGE_SCALE = 0.0005;
    private static final double MONSTER_DAMAGE_SCALE = 0.35;
    private static final double DEFENSE_BASE = 100.0;

    private CombatRules() {
        // Prevent instantiation.
    }

    // Physical damage.
    public static double calculatePhysicalDamage(
            Hero attacker,
            Weapon weapon,
            Monster target) {

        if (attacker == null || target == null) {
            throw new IllegalArgumentException(
                    "Attacker and target cannot be null."
            );
        }

        double baseDamage;

        if (weapon == null) {
            // Unarmed attack
            baseDamage = attacker.getStrength() / 10.0;
        } else {
            if (!weapon.isUsable()) {
                throw new IllegalArgumentException(
                        "Weapon is broken."
                );
            }

            baseDamage = weapon.getDamage();

            Equipment equipment = attacker.getEquipment();

            if (equipment.isHeldWithTwoHands(weapon)
                    && weapon.getRequiredHands() == 1) {
                baseDamage *= TWO_HAND_BONUS;
            }
        }

        double rawDamage = baseDamage
                * (1.0 + attacker.getStrength() / ATTRIBUTE_SCALE);

        // Apply proportional monster defense.
        return applyDefense(rawDamage, target.getDefense());
    }

    // Spell damage.
    public static double calculateSpellDamage(
            Hero caster,
            Spell spell,
            Monster target) {

        if (caster == null || spell == null || target == null) {
            throw new IllegalArgumentException(
                    "Caster, spell and target cannot be null."
            );
        }

        double rawDamage = spell.getDamage()
                * (1.0 + caster.getDexterity() / ATTRIBUTE_SCALE);

        // Apply proportional monster defense.
        return applyDefense(rawDamage, target.getDefense());
    }

    public static double calculateMonsterDamage(
            Monster attacker,
            Hero target) {

        if (attacker == null || target == null) {
            throw new IllegalArgumentException(
                    "Attacker and target cannot be null."
            );
        }

        double reduction = target.getBonusDefense();

        Armor armor = target.getEquipment().getArmor();

        if (armor != null && armor.isUsable()) {
            reduction += armor.getDamageReduction();
        }

        // Reduce incoming damage without making armor invincible.
        double rawDamage = attacker.getDamage() * MONSTER_DAMAGE_SCALE;
        return applyDefense(rawDamage, reduction);
    }

    // Hero dodge.
    public static double getHeroDodgeProbability(Hero hero) {
        if (hero == null) {
            throw new IllegalArgumentException(
                    "Hero cannot be null."
            );
        }

        return clampProbability(
                hero.getAgility() * HERO_DODGE_SCALE
        );
    }

    public static double getMonsterDodgeProbability(
            Monster monster) {

        if (monster == null) {
            throw new IllegalArgumentException(
                    "Monster cannot be null."
            );
        }

        return clampProbability(
                monster.getDodgeChance() * MONSTER_DODGE_SCALE
        );
    }

    public static boolean rollDodge(
            double probability,
            Random random) {

        if (random == null
                || !Double.isFinite(probability)
                || probability < 0
                || probability > 1) {
            throw new IllegalArgumentException(
                    "Invalid dodge probability or random generator."
            );
        }

        return random.nextDouble() < probability;
    }

    // Defense reduces damage but never makes it negative.
    private static double applyDefense(double rawDamage, double defense) {
        return rawDamage * DEFENSE_BASE / (DEFENSE_BASE + defense);
    }

    private static double clampProbability(double probability) {
        return Math.max(0, Math.min(1, probability));
    }
}

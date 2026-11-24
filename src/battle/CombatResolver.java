package battle;

import character.Hero;
import character.Monster;
import inventory.Hand;
import item.Armor;
import item.Potion;
import item.Spell;
import item.Weapon;
import world.TileType;

import java.util.Objects;
import java.util.Random;

// Resolves one action. The caller controls range, rounds and rewards.
public final class CombatResolver {
    private final Random random;

    public CombatResolver(Random random) {
        this.random = Objects.requireNonNull(random);
    }

    public boolean heroAttack(Hero hero, Monster target, Weapon weapon, TileType terrain) {
        if (!valid(hero, target)) return false;
        if (weapon != null && (!hero.getInventory().contains(weapon)
                || !hero.getEquipment().isEquipped(weapon) || !weapon.isUsable())) {
            return false;
        }
        double damage = CombatRules.calculatePhysicalDamage(hero, weapon, target, terrain);
        if (weapon != null) weapon.use();
        if (!CombatRules.rollDodge(CombatRules.getMonsterDodgeProbability(target), random)) {
            target.takeDamage(damage);
        }
        return true;
    }

    public boolean heroCastSpell(Hero hero, Monster target, Spell spell, TileType terrain) {
        if (!valid(hero, target) || spell == null
                || !hero.getInventory().contains(spell) || !spell.canCast(hero)) return false;
        double damage = CombatRules.calculateSpellDamage(hero, spell, target, terrain);
        if (!hero.consumeSpellForCast(spell)) return false;
        if (!CombatRules.rollDodge(CombatRules.getMonsterDodgeProbability(target), random)) {
            target.takeDamage(damage);
            if (target.isAlive()) spell.applyEffect(target);
        }
        return true;
    }

    public boolean monsterAttack(Monster monster, Hero hero, TileType terrain) {
        if (!valid(hero, monster)) return false;
        if (!CombatRules.rollDodge(CombatRules.getHeroDodgeProbability(hero, terrain), random)) {
            double damage = CombatRules.calculateMonsterDamage(monster, hero);
            Armor armor = hero.getEquipment().getArmor();
            if (armor != null && armor.isUsable()) armor.use();
            hero.takeDamage(damage);
        }
        return true;
    }

    public boolean usePotion(Hero hero, Potion potion) {
        return hero != null && hero.isAlive() && hero.usePotion(potion);
    }

    public boolean equipOneHand(Hero hero, Weapon weapon, Hand hand) {
        return hero != null && hero.isAlive() && hero.getEquipment().equipOneHand(weapon, hand);
    }

    public boolean equipTwoHands(Hero hero, Weapon weapon) {
        return hero != null && hero.isAlive() && hero.getEquipment().equipTwoHands(weapon);
    }

    public boolean equipArmor(Hero hero, Armor armor) {
        return hero != null && hero.isAlive() && hero.getEquipment().equipArmor(armor);
    }

    public boolean unequipWeapon(Hero hero, Weapon weapon) {
        return hero != null && hero.isAlive() && hero.getEquipment().unequipWeapon(weapon);
    }

    public boolean unequipArmor(Hero hero) {
        return hero != null && hero.isAlive() && hero.getEquipment().unequipArmor();
    }

    private boolean valid(Hero hero, Monster monster) {
        return hero != null && monster != null && hero.isAlive() && monster.isAlive();
    }
}

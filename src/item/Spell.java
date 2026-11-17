package item;

import character.Hero;
import character.Monster;

public abstract class Spell extends Item {

    private final double damage;
    private final double manaCost;
    private int remainingUses;

    protected Spell(
            String name,
            int price,
            int requiredLevel,
            double damage,
            double manaCost,
            int uses) {

        super(name, price, requiredLevel);

        if (!Double.isFinite(damage) || damage < 0) {
            throw new IllegalArgumentException(
                    "Invalid spell damage."
            );
        }

        if (!Double.isFinite(manaCost) || manaCost < 0) {
            throw new IllegalArgumentException(
                    "Invalid mana cost."
            );
        }

        if (uses <= 0) {
            throw new IllegalArgumentException(
                    "Uses must be positive."
            );
        }

        this.damage = damage;
        this.manaCost = manaCost;
        this.remainingUses = uses;
    }

    public double getDamage() {
        return damage;
    }

    public double getManaCost() {
        return manaCost;
    }

    public int getRemainingUses() {
        return remainingUses;
    }

    public boolean isUsable() {
        return remainingUses > 0;
    }

    public boolean canCast(Hero caster) {
        return caster != null
                && caster.isAlive()
                && caster.getLevel() >= getRequiredLevel()
                && caster.getMp() >= manaCost
                && isUsable();
    }

    public boolean consumeUse() {
        if (!isUsable()) {
            return false;
        }

        remainingUses--;
        return true;
    }

    public abstract void applyEffect(Monster target);

    @Override
    public String toString() {
        return super.toString()
                + " | Damage: " + damage
                + " | Mana Cost: " + manaCost
                + " | Uses: " + remainingUses;
    }
}

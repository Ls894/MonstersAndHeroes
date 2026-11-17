package item;

// Represents a weapon with durability.
public class Weapon extends Item {

    private final double damage;
    private final int requiredHands;

    private final int maxDurability;
    private int durability;

    public Weapon(
            String name,
            int price,
            int requiredLevel,
            double damage,
            int requiredHands,
            int maxDurability) {

        super(name, price, requiredLevel);

        if (!Double.isFinite(damage) || damage < 0) {
            throw new IllegalArgumentException(
                    "Weapon damage cannot be negative."
            );
        }

        if (requiredHands != 1 && requiredHands != 2) {
            throw new IllegalArgumentException(
                    "Weapon must require 1 or 2 hands."
            );
        }

        if (maxDurability <= 0) {
            throw new IllegalArgumentException(
                    "Durability must be positive."
            );
        }

        this.damage = damage;
        this.requiredHands = requiredHands;
        this.maxDurability = maxDurability;
        this.durability = maxDurability;
    }

    public double getDamage() {
        return damage;
    }

    public int getRequiredHands() {
        return requiredHands;
    }

    public int getDurability() {
        return durability;
    }

    public int getMaxDurability() {
        return maxDurability;
    }

    public boolean isUsable() {
        return durability > 0;
    }

    // Reduces durability after an attack.
    public boolean use() {
        if (!isUsable()) {
            return false;
        }

        durability--;
        return true;
    }

    // Restores full durability.
    public void repair() {
        durability = maxDurability;
    }

    @Override
    public String toString() {
        return super.toString()
                + " | Damage: " + damage
                + " | Hands: " + requiredHands
                + " | Durability: " + durability
                + "/" + maxDurability;
    }
}

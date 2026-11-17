package item;

// Class of armor for hero, also a item.
public class Armor extends Item {

    // fields
    private final double damageReduction;
    private final int maxDurability;
    private int durability;

    // Constructor
    public Armor(
            String name,
            int price,
            int requiredLevel,
            double damageReduction,
            int maxDurability) {

        super(name, price, requiredLevel);

        if (!Double.isFinite(damageReduction)
                || damageReduction < 0) {
            throw new IllegalArgumentException(
                    "Damage reduction cannot be negative."
            );
        }

        if (maxDurability <= 0) {
            throw new IllegalArgumentException(
                    "Durability must be positive."
            );
        }

        this.damageReduction = damageReduction;
        this.maxDurability = maxDurability;
        this.durability = maxDurability;
    }

    //Getters
    public double getDamageReduction() {
        return damageReduction;
    }
    public int getDurability() {
        return durability;
    }
    public int getMaxDurability() {
        return maxDurability;
    }

    // Check if it still can use.
    public boolean isUsable() {
        return durability > 0;
    }

    public boolean use() {
        if (!isUsable()) {
            return false;
        }

        durability--;
        return true;
    }

    // Repair this armor
    public void repair() {
        durability = maxDurability;
    }

    @Override
    public String toString() {
        return super.toString()
                + " | Damage Reduction: " + damageReduction
                + " | Durability: " + durability
                + "/" + maxDurability;
    }
}

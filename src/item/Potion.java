package item;

import character.Hero;

import java.util.EnumSet;
import java.util.Set;

public class Potion extends Item {

    private final double effectAmount;
    private final Set<PotionAttribute> affectedAttributes;
    private boolean consumed;

    public Potion(
            String name,
            int price,
            int requiredLevel,
            double effectAmount,
            Set<PotionAttribute> affectedAttributes) {

        super(name, price, requiredLevel);

        if (!Double.isFinite(effectAmount) || effectAmount <= 0) {
            throw new IllegalArgumentException(
                    "Potion effect must be positive."
            );
        }

        if (affectedAttributes == null
                || affectedAttributes.isEmpty()
                || affectedAttributes.contains(null)) {
            throw new IllegalArgumentException(
                    "Potion must affect valid attributes."
            );
        }

        this.effectAmount = effectAmount;
        this.affectedAttributes =
                EnumSet.copyOf(affectedAttributes);
        this.consumed = false;
    }

    public double getEffectAmount() {
        return effectAmount;
    }

    public Set<PotionAttribute> getAffectedAttributes() {
        return EnumSet.copyOf(affectedAttributes);
    }

    public boolean isUsable() {
        return !consumed;
    }

    public boolean applyTo(Hero hero) {
        if (!isUsable() || hero == null
                || !hero.isAlive()
                || hero.getLevel() < getRequiredLevel()) {
            return false;
        }

        for (PotionAttribute attribute : affectedAttributes) {
            hero.applyPotionEffect(attribute, effectAmount);
        }

        consumed = true;
        return true;
    }

    @Override
    public String toString() {
        return super.toString()
                + " | Effect: " + effectAmount
                + " | Attributes: " + affectedAttributes
                + " | Available: " + isUsable();
    }
}

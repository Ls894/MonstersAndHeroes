package item;

import character.Monster;

public class LightningSpell extends Spell {

    private static final double DODGE_REDUCTION = 0.10;

    public LightningSpell(
            String name,
            int price,
            int requiredLevel,
            double damage,
            double manaCost,
            int uses) {

        super(
                name,
                price,
                requiredLevel,
                damage,
                manaCost,
                uses
        );
    }

    @Override
    public void applyEffect(Monster target) {
        if (target == null) {
            throw new IllegalArgumentException(
                    "Target cannot be null."
            );
        }

        target.reduceDodgeChance(DODGE_REDUCTION);
    }
}

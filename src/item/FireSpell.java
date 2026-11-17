package item;

import character.Monster;

// Fire spell
public class FireSpell extends Spell {

    // fields
    private static final double DEFENSE_REDUCTION = 0.10;

    // constractor
    public FireSpell(
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

        target.reduceDefense(DEFENSE_REDUCTION);
    }
}

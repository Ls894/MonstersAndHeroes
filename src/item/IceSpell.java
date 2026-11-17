package item;

import character.Monster;

// Class for ice spell
public class IceSpell extends Spell {

    private static final double DAMAGE_REDUCTION = 0.10;

    public IceSpell(
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

        target.reduceDamage(DAMAGE_REDUCTION);
    }
}

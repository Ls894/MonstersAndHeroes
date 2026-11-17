package character;

// A exoskeleton class for a monster type.
public class Exoskeleton extends Monster {

    public Exoskeleton(
            String name,
            int level,
            double damage,
            double defense,
            double dodgeChance) {

        super(
                name,
                level,
                damage,
                defense,
                dodgeChance
        );
    }
}

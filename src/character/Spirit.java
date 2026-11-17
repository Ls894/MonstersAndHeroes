package character;

// Represents a spirit monster.
public class Spirit extends Monster {

    public Spirit(
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

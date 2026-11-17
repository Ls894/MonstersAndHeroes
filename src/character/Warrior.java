package character;

public class Warrior extends Hero {

    public Warrior(
            String name,
            int level,
            double mp,
            double strength,
            double dexterity,
            double agility,
            int gold,
            int experience) {

        super(
                name,
                level,
                mp,
                strength,
                dexterity,
                agility,
                gold,
                experience
        );
    }

    @Override
    protected void increaseAttributes() {
        increaseStrength(1.10);
        increaseDexterity(1.05);
        increaseAgility(1.10);
    }
}

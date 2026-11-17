package character;

public abstract class Monster extends Character {

    private double damage;
    private double defense;
    private double dodgeChance;

    protected Monster(
            String name,
            int level,
            double damage,
            double defense,
            double dodgeChance) {

        super(name, level, level * 100.0);

        if (!Double.isFinite(damage) || damage < 0
                || !Double.isFinite(defense) || defense < 0
                || !Double.isFinite(dodgeChance)
                || dodgeChance < 0) {
            throw new IllegalArgumentException(
                    "Invalid monster attributes."
            );
        }

        this.damage = damage;
        this.defense = defense;
        this.dodgeChance = dodgeChance;
    }

    public double getDamage() {
        return damage;
    }

    public double getDefense() {
        return defense;
    }

    public double getDodgeChance() {
        return dodgeChance;
    }

    public void reduceDamage(double percentage) {
        validatePercentage(percentage);
        damage *= (1.0 - percentage);
    }

    public void reduceDefense(double percentage) {
        validatePercentage(percentage);
        defense *= (1.0 - percentage);
    }

    public void reduceDodgeChance(double percentage) {
        validatePercentage(percentage);
        dodgeChance *= (1.0 - percentage);
    }

    private void validatePercentage(double percentage) {
        if (!Double.isFinite(percentage)
                || percentage < 0
                || percentage > 1) {
            throw new IllegalArgumentException(
                    "Percentage must be between 0 and 1."
            );
        }
    }
}

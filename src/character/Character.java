package character;

//Character class for all hero and monster
public abstract class Character {

    // filed
    private final String name;
    private int level;
    private double hp;

    //constractor
    protected Character(String name, int level, double hp) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Character name cannot be empty."
            );
        }

        if (level < 1 || hp < 0 || !Double.isFinite(hp)) {
            throw new IllegalArgumentException(
                    "Invalid level or HP."
            );
        }

        this.name = name;
        this.level = level;
        this.hp = hp;
    }

    // Getters
    public String getName() {
        return name;
    }
    public int getLevel() {
        return level;
    }
    public double getHp() {
        return hp;
    }

    public boolean isAlive() {
        return hp > 0;
    }
    public void takeDamage(double amount) {
        validateAmount(amount);
        hp = Math.max(0, hp - amount);
    }

    public void restoreHp(double amount) {
        validateAmount(amount);
        hp += amount;
    }

    // Setter
    protected void setLevel(int level) {
        if (level < 1) {
            throw new IllegalArgumentException(
                    "Level must be positive."
            );
        }
        this.level = level;
    }
    protected void setHp(double hp) {
        if (hp < 0 || !Double.isFinite(hp)) {
            throw new IllegalArgumentException(
                    "Invalid HP."
            );
        }
        this.hp = hp;
    }

    private void validateAmount(double amount) {
        if (!Double.isFinite(amount) || amount < 0) {
            throw new IllegalArgumentException(
                    "Amount must be finite and nonnegative."
            );
        }
    }
}

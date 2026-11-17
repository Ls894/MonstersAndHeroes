package item;

import character.Hero;

public abstract class Item {

    private final String name;
    private final int price;
    private final int requiredLevel;

    protected Item(String name, int price, int requiredLevel) {

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Item name cannot be empty."
            );
        }

        if (price < 0) {
            throw new IllegalArgumentException(
                    "Item price cannot be negative."
            );
        }

        if (requiredLevel < 1) {
            throw new IllegalArgumentException(
                    "Required level must be at least 1."
            );
        }

        this.name = name;
        this.price = price;
        this.requiredLevel = requiredLevel;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public int getRequiredLevel() {
        return requiredLevel;
    }

    public boolean canBePurchasedBy(Hero hero) {
        return hero != null
                && hero.getLevel() >= requiredLevel
                && hero.getGold() >= price;
    }

    public int getSellPrice() {
        return price / 2;
    }

    @Override
    public String toString() {
        return name
                + " | Price: " + price
                + " | Level: " + requiredLevel;
    }
}

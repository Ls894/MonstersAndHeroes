package inventory;

import character.Hero;
import item.Item;

// Class for inventory of hero.
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Manages items owned by a hero.
public class Inventory {

    private final Hero owner;
    private final List<Item> items;

    public Inventory(Hero owner) {
        if (owner == null) {
            throw new IllegalArgumentException(
                    "Inventory owner cannot be null."
            );
        }

        this.owner = owner;
        this.items = new ArrayList<>();
    }

    // Rejects null or duplicate items.
    public void addItem(Item item) {
        if (item == null) {
            throw new IllegalArgumentException(
                    "Item cannot be null."
            );
        }

        if (items.contains(item)) {
            throw new IllegalArgumentException(
                    "Item is already in this inventory."
            );
        }

        items.add(item);
    }

    // Removes an item and unequips it first.
    public boolean removeItem(Item item) {
        if (item == null || !items.contains(item)) {
            return false;
        }

        owner.getEquipment().unequipItem(item);

        return items.remove(item);
    }

    public Item getItem(int index) {
        return items.get(index);
    }

    public boolean contains(Item item) {
        return items.contains(item);
    }

    // Exposes a read-only inventory view.
    public List<Item> getItems() {
        return Collections.unmodifiableList(items);
    }

    public int size() {
        return items.size();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    // Prints the current inventory.
    public void display() {
        if (items.isEmpty()) {
            System.out.println("Inventory is empty.");
            return;
        }

        for (int i = 0; i < items.size(); i++) {
            System.out.println(
                    (i + 1) + ". " + items.get(i)
            );
        }
    }
}

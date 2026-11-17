package market;

import character.Hero;
import item.Armor;
import item.Item;
import item.Weapon;

// Class for market.
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// Manages item stock and hero transactions.
public class Market {

    private static final double REPAIR_RATE = 0.50;

    private final List<Item> stock;

    public Market() {
        stock = new ArrayList<>();
    }

    // Stock operations

    // Prevents external stock edits.
    public List<Item> getStock() {
        return Collections.unmodifiableList(stock);
    }

    public boolean containsItem(Item item) {
        return item != null && stock.contains(item);
    }

    public int getStockSize() {
        return stock.size();
    }

    public void addStock(Item item) {
        if (item == null) {
            throw new IllegalArgumentException(
                    "Stock item cannot be null."
            );
        }

        if (stock.contains(item)) {
            throw new IllegalArgumentException(
                    "Item is already in this market."
            );
        }

        stock.add(item);
    }

    // Transfers purchased items to heroes.

    // Charges gold before transferring ownership.
    public boolean buyItem(Hero hero, Item item) {

        if (hero == null || !containsItem(item)) {
            return false;
        }

        if (!item.canBePurchasedBy(hero)) {
            return false;
        }

        if (!hero.spendGold(item.getPrice())) {
            return false;
        }

        stock.remove(item);
        hero.getInventory().addItem(item);

        return true;
    }

    // Buys items back from heroes.

    // Validates gold before removing the item.
    public boolean sellItem(Hero hero, Item item) {

        if (hero == null
                || item == null
                || !hero.getInventory().contains(item)
                || stock.contains(item)) {
            return false;
        }

        int sellPrice = item.getSellPrice();

        if ((long) hero.getGold() + sellPrice
                > Integer.MAX_VALUE) {
            return false;
        }

        if (!hero.removeItem(item)) {
            return false;
        }

        hero.earnGold(sellPrice);
        stock.add(item);

        return true;
    }

    // Calculates the fixed repair fee.

    private int calculateRepairCost(Item item) {

        if (item == null) {
            throw new IllegalArgumentException(
                    "Item cannot be null."
            );
        }

        return (int) (item.getPrice() * REPAIR_RATE);
    }

    // Repairs a hero-owned weapon.

    public boolean repairWeapon(
            Hero hero,
            Weapon weapon) {

        if (hero == null
                || weapon == null
                || !hero.getInventory().contains(weapon)) {
            return false;
        }

        if (weapon.getDurability()
                == weapon.getMaxDurability()) {
            return false;
        }

        int cost = calculateRepairCost(weapon);

        if (!hero.spendGold(cost)) {
            return false;
        }

        weapon.repair();
        return true;
    }

    // Repairs a hero-owned armor item.

    public boolean repairArmor(
            Hero hero,
            Armor armor) {

        if (hero == null
                || armor == null
                || !hero.getInventory().contains(armor)) {
            return false;
        }

        if (armor.getDurability()
                == armor.getMaxDurability()) {
            return false;
        }

        int cost = calculateRepairCost(armor);

        if (!hero.spendGold(cost)) {
            return false;
        }

        armor.repair();
        return true;
    }

    // Prints available items.

    public void displayStock() {

        if (stock.isEmpty()) {
            System.out.println("Market is empty.");
            return;
        }

        for (int i = 0; i < stock.size(); i++) {
            System.out.println(
                    (i + 1) + ". " + stock.get(i)
            );
        }
    }
}

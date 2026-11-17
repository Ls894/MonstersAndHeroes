package inventory;

import character.Hero;
import item.Armor;
import item.Item;
import item.Weapon;

// Equipment class for a item type.
public class Equipment {

    // fields
    private final Hero owner;

    private Weapon leftHand;
    private Weapon rightHand;
    private Armor armor;

    // constractor
    public Equipment(Hero owner) {
        if (owner == null) {
            throw new IllegalArgumentException(
                    "Equipment owner cannot be null."
            );
        }

        this.owner = owner;
    }

    private boolean canEquip(Item item) {
        return item != null
                && owner.getInventory().contains(item)
                && owner.getLevel() >= item.getRequiredLevel();
    }

    // Equip one hand armor.
    public boolean equipOneHand(Weapon weapon, Hand hand) {
        if (weapon == null
                || hand == null
                || !canEquip(weapon)
                || !weapon.isUsable()
                || weapon.getRequiredHands() != 1) {
            return false;
        }

        if (hand == Hand.LEFT) {
            if (leftHand != null || rightHand == weapon) {
                return false;
            }

            leftHand = weapon;
        } else {
            if (rightHand != null || leftHand == weapon) {
                return false;
            }

            rightHand = weapon;
        }

        return true;
    }

    // Equip both hands.
    public boolean equipTwoHands(Weapon weapon) {
        if (weapon == null
                || !canEquip(weapon)
                || !weapon.isUsable()
                || leftHand != null
                || rightHand != null) {
            return false;
        }

        leftHand = weapon;
        rightHand = weapon;

        return true;
    }

    // Unequip the weapon
    public boolean unequipWeapon(Weapon weapon) {
        if (weapon == null) {
            return false;
        }

        boolean removed = false;

        if (leftHand == weapon) {
            leftHand = null;
            removed = true;
        }

        if (rightHand == weapon) {
            rightHand = null;
            removed = true;
        }

        return removed;
    }

    public boolean equipArmor(Armor newArmor) {
        if (newArmor == null
                || !canEquip(newArmor)
                || !newArmor.isUsable()
                || armor != null) {
            return false;
        }

        armor = newArmor;
        return true;
    }

    public boolean unequipArmor() {
        if (armor == null) {
            return false;
        }

        armor = null;
        return true;
    }

    // Unequip an item regardless of equipment type.
    public boolean unequipItem(Item item) {
        if (item == null) {
            return false;
        }

        if (item instanceof Weapon) {
            return unequipWeapon((Weapon) item);
        }

        if (item instanceof Armor && armor == item) {
            return unequipArmor();
        }

        return false;
    }

    public Weapon getLeftHand() {
        return leftHand;
    }

    public Weapon getRightHand() {
        return rightHand;
    }

    public Armor getArmor() {
        return armor;
    }

    public boolean isHeldWithTwoHands(Weapon weapon) {
        return weapon != null
                && leftHand == weapon
                && rightHand == weapon;
    }

    public boolean isEquipped(Item item) {
        return item != null
                && (leftHand == item
                || rightHand == item
                || armor == item);
    }
}

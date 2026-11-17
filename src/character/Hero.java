package character;

import battle.Battle;
import inventory.Equipment;
import inventory.Inventory;
import item.Item;
import item.Potion;
import item.PotionAttribute;
import item.Spell;

public abstract class Hero extends Character {

    // Fields
    private int experience;

    private double mp;
    private double baseMp;

    private double strength;
    private double dexterity;
    private double agility;
    private double bonusDefense = 0;

    private int gold;

    private final Inventory inventory;
    private final Equipment equipment;

    // Constructor
    protected Hero(
            String name,
            int level,
            double mp,
            double strength,
            double dexterity,
            double agility,
            int gold,
            int experience) {

        super(name, level, level * 100.0);

        if (!Double.isFinite(mp) || mp < 0
                || !Double.isFinite(strength) || strength < 0
                || !Double.isFinite(dexterity) || dexterity < 0
                || !Double.isFinite(agility) || agility < 0
                || gold < 0
                || experience < 0) {

            throw new IllegalArgumentException(
                    "Invalid hero attributes."
            );
        }

        this.mp = mp;
        this.baseMp = mp;

        this.strength = strength;
        this.dexterity = dexterity;
        this.agility = agility;

        this.gold = gold;
        this.experience = experience;

        this.inventory = new Inventory(this);
        this.equipment = new Equipment(this);
    }

    // Getters
    public int getExperience() {
        return experience;
    }
    public double getMp() {
        return mp;
    }
    public double getBaseMp() {
        return baseMp;
    }
    public double getStrength() {
        return strength;
    }
    public double getDexterity() {
        return dexterity;
    }
    public double getAgility() {
        return agility;
    }
    public double getBonusDefense() {
        return bonusDefense;
    }
    public int getGold() {
        return gold;
    }
    public Inventory getInventory() {
        return inventory;
    }
    public Equipment getEquipment() {
        return equipment;
    }

    // MP Management
    public void restoreMp(double amount) {
        validateAmount(amount);

        if (!Double.isFinite(mp + amount)) {
            throw new IllegalArgumentException(
                    "MP would exceed the supported range."
            );
        }

        mp += amount;
    }

    public boolean consumeMp(double amount) {
        validateAmount(amount);

        if (mp < amount) {
            return false;
        }

        mp -= amount;
        return true;
    }

    // Gold Management
    public void earnGold(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException(
                    "Gold amount cannot be negative."
            );
        }

        gold = Math.addExact(gold, amount);
    }

    public boolean spendGold(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException(
                    "Gold amount cannot be negative."
            );
        }

        if (gold < amount) {
            return false;
        }

        gold -= amount;
        return true;
    }

    // Experience and Level Up
    public void gainExperience(int amount) {
        if (amount < 0) {
            throw new IllegalArgumentException(
                    "Experience cannot be negative."
            );
        }

        experience = Math.addExact(experience, amount);

        while (experience >= experienceForNextLevel()) {
            levelUp();
        }
    }

    private long experienceForNextLevel() {
        long level = getLevel();

        return 10L * level * (level + 1) / 2;
    }

    private void levelUp() {
        setLevel(getLevel() + 1);

        // Reset HP according to the new level.
        setHp(getLevel() * 100.0);

        // Increase current MP and revival MP baseline.
        mp *= 1.10;
        baseMp *= 1.10;

        increaseAttributes();
    }

    protected abstract void increaseAttributes();

    protected void increaseStrength(double multiplier) {
        strength *= multiplier;
    }

    protected void increaseDexterity(double multiplier) {
        dexterity *= multiplier;
    }

    protected void increaseAgility(double multiplier) {
        agility *= multiplier;
    }

    // Round Regeneration
    public void regenerateAfterRound() {

        // Fainted heroes do not regenerate.
        if (!isAlive()) {
            return;
        }

        // Regain 10% of current HP.
        double hpRecovery = getHp() * 0.10;

        if (Double.isFinite(hpRecovery)
                && Double.isFinite(getHp() + hpRecovery)) {
            restoreHp(hpRecovery);
        }

        // Regain 10% of current MP.
        double mpRecovery = mp * 0.10;

        if (Double.isFinite(mpRecovery)
                && Double.isFinite(mp + mpRecovery)) {
            restoreMp(mpRecovery);
        }
    }

    // Battle Revival
    public void reviveAfterBattle() {

        // Only revive fainted heroes.
        if (isAlive()) {
            return;
        }

        // Restore half of the level-based HP.
        setHp(getLevel() * 100.0 * 0.50);

        // Restore half of the MP baseline.
        mp = baseMp * 0.50;
    }

    // Potion Effects
    public void applyPotionEffect(
            PotionAttribute attribute,
            double amount) {

        if (attribute == null
                || !Double.isFinite(amount)
                || amount <= 0) {
            throw new IllegalArgumentException(
                    "Invalid potion effect."
            );
        }

        switch (attribute) {

            case HEALTH:
                restoreHp(amount);
                break;

            case MANA:
                restoreMp(amount);
                break;

            case STRENGTH:
                strength += amount;
                break;

            case DEXTERITY:
                dexterity += amount;
                break;

            case AGILITY:
                agility += amount;
                break;

            case DEFENSE:
                bonusDefense += amount;
                break;
        }
    }

    // Inventory Management
    public boolean removeItem(Item item) {
        return inventory.removeItem(item);
    }

    public boolean usePotion(Potion potion) {

        if (potion == null
                || !inventory.contains(potion)) {
            return false;
        }

        if (!potion.applyTo(this)) {
            return false;
        }

        inventory.removeItem(potion);
        return true;
    }

    public boolean consumeSpellForCast(Spell spell) {

        if (spell == null
                || !inventory.contains(spell)
                || !spell.canCast(this)) {
            return false;
        }

        if (!consumeMp(spell.getManaCost())) {
            return false;
        }

        spell.consumeUse();

        if (!spell.isUsable()) {
            inventory.removeItem(spell);
        }

        return true;
    }

    // Validation part
    private void validateAmount(double amount) {
        if (!Double.isFinite(amount) || amount < 0) {
            throw new IllegalArgumentException(
                    "Amount must be non-negative and finite."
            );
        }
    }
}

package battle;

import character.Hero;
import character.Monster;
import inventory.Equipment;
import inventory.Hand;
import item.Armor;
import item.Potion;
import item.Spell;
import item.Weapon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class Battle {

    // Fields
    private final List<Hero> heroes;
    private final List<Monster> monsters;

    private final Set<Hero> actedHeroes;
    private final List<String> lastMonsterEvents;
    private final Random random;

    private int round;
    private BattleStatus status;
    private BattlePhase phase;

    private boolean monsterTurnCompleted;
    private boolean rewardsDistributed;

    // Constructor
    public Battle(List<Hero> heroes, List<Monster> monsters) {
        this(heroes, monsters, new Random());
    }

    // Allows deterministic combat tests.
    public Battle(List<Hero> heroes, List<Monster> monsters, Random random) {

        if (random == null) {
            throw new IllegalArgumentException("Random cannot be null.");
        }

        if (heroes == null || monsters == null) {
            throw new IllegalArgumentException(
                    "Battle participants cannot be null."
            );
        }

        if (heroes.isEmpty() || heroes.size() > 3) {
            throw new IllegalArgumentException(
                    "A battle requires 1 to 3 heroes."
            );
        }

        if (heroes.size() != monsters.size()) {
            throw new IllegalArgumentException(
                    "Hero and monster counts must match."
            );
        }

        if (heroes.contains(null) || monsters.contains(null)) {
            throw new IllegalArgumentException(
                    "Participants cannot contain null."
            );
        }

        if (new HashSet<>(heroes).size() != heroes.size()
                || new HashSet<>(monsters).size() != monsters.size()) {
            throw new IllegalArgumentException(
                    "Battle participants must be unique."
            );
        }

        for (Hero hero : heroes) {
            if (!hero.isAlive()) {
                throw new IllegalArgumentException(
                        "All heroes must be alive at battle start."
                );
            }
        }

        for (Monster monster : monsters) {
            if (!monster.isAlive()) {
                throw new IllegalArgumentException(
                        "All monsters must be alive at battle start."
                );
            }
        }

        this.heroes = new ArrayList<>(heroes);
        this.monsters = new ArrayList<>(monsters);

        this.actedHeroes = new HashSet<>();
        this.lastMonsterEvents = new ArrayList<>();
        this.random = random;

        this.round = 1;
        this.status = BattleStatus.IN_PROGRESS;
        this.phase = BattlePhase.HERO_PHASE;

        this.monsterTurnCompleted = false;
        this.rewardsDistributed = false;
    }

    // Getters
    public List<Hero> getHeroes() {
        return Collections.unmodifiableList(heroes);
    }
    public List<Monster> getMonsters() {
        return Collections.unmodifiableList(monsters);
    }

    // Returns the most recent monster attack details.
    public List<String> getLastMonsterEvents() {
        return Collections.unmodifiableList(lastMonsterEvents);
    }

    public int getRound() {
        return round;
    }
    public BattleStatus getStatus() {
        return status;
    }

    public BattlePhase getPhase() {
        return phase;
    }

    public boolean isOver() {
        return status != BattleStatus.IN_PROGRESS;
    }

    public List<Hero> getAliveHeroes() {
        List<Hero> alive = new ArrayList<>();

        for (Hero hero : heroes) {
            if (hero.isAlive()) {
                alive.add(hero);
            }
        }

        return alive;
    }

    public List<Monster> getAliveMonsters() {
        List<Monster> alive = new ArrayList<>();

        for (Monster monster : monsters) {
            if (monster.isAlive()) {
                alive.add(monster);
            }
        }

        return alive;
    }

    // Manage battle status
    public void updateStatus() {

        if (isOver()) {
            return;
        }

        if (getAliveMonsters().isEmpty()) {
            status = BattleStatus.HERO_VICTORY;
            phase = BattlePhase.FINISHED;

            distributeVictoryRewards();

        } else if (getAliveHeroes().isEmpty()) {
            status = BattleStatus.MONSTER_VICTORY;
            phase = BattlePhase.FINISHED;
        }
    }

    // Victory rewards.
    private void distributeVictoryRewards() {

        if (rewardsDistributed) {
            return;
        }

        rewardsDistributed = true;

        int totalMonsterLevels = 0;

        for (Monster monster : monsters) {
            totalMonsterLevels = Math.addExact(
                    totalMonsterLevels,
                    monster.getLevel()
            );
        }

        // Easy mode grants extra experience.
        int experienceReward = Math.multiplyExact(totalMonsterLevels, 2);
        int goldReward = Math.multiplyExact(
                totalMonsterLevels, 100
        );

        // Skip fainted heroes.
        for (Hero hero : heroes) {
            if (hero.isAlive()) {
                hero.earnGold(goldReward);
                hero.gainExperience(experienceReward);
            }
        }

        // Revive after rewards.
        reviveFaintedHeroes();
    }


    // Revive fainted heroes.
    private void reviveFaintedHeroes() {

        for (Hero hero : heroes) {
            if (!hero.isAlive()) {
                hero.reviveAfterBattle();
            }
        }
    }

    // Round regeneration.
    private void regenerateHeroes() {

        for (Hero hero : heroes) {
            if (hero.isAlive()) {
                hero.regenerateAfterRound();
            }
        }
    }

    // Hero Turn Management
    public boolean canHeroAct(Hero hero) {

        updateStatus();

        if (hero == null
                || isOver()
                || phase != BattlePhase.HERO_PHASE) {
            return false;
        }

        if (!heroes.contains(hero) || !hero.isAlive()) {
            return false;
        }

        return !actedHeroes.contains(hero);
    }

    public boolean isHeroPhaseComplete() {

        for (Hero hero : heroes) {
            if (hero.isAlive() && !actedHeroes.contains(hero)) {
                return false;
            }
        }

        return true;
    }

    private void finishHeroAction(Hero hero) {

        actedHeroes.add(hero);
        updateStatus();

        if (!isOver() && isHeroPhaseComplete()) {
            phase = BattlePhase.MONSTER_PHASE;
        }
    }

    // Hero physical attack
    public boolean heroAttack(
            Hero attacker,
            Monster target,
            Weapon weapon) {

        if (!canHeroAct(attacker)
                || target == null
                || !monsters.contains(target)
                || !target.isAlive()) {
            return false;
        }

        if (weapon != null) {
            Equipment equipment = attacker.getEquipment();

            if (!attacker.getInventory().contains(weapon)
                    || !equipment.isEquipped(weapon)
                    || !weapon.isUsable()) {
                return false;
            }
        }

        // Calculate damage.
        double damage =
                CombatRules.calculatePhysicalDamage(
                        attacker, weapon, target
                );

        // Consume durability.
        if (weapon != null) {
            weapon.use();
        }

        double dodgeProbability =
                CombatRules.getMonsterDodgeProbability(target);

        boolean dodged =
                CombatRules.rollDodge(
                        dodgeProbability, random
                );

        if (!dodged) {
            target.takeDamage(damage);
        }

        finishHeroAction(attacker);
        return true;
    }

    // Hero spell attack
    public boolean heroCastSpell(
            Hero caster,
            Monster target,
            Spell spell) {

        if (!canHeroAct(caster)
                || target == null
                || !monsters.contains(target)
                || !target.isAlive()
                || spell == null) {
            return false;
        }

        if (!caster.getInventory().contains(spell)
                || !spell.canCast(caster)) {
            return false;
        }

        double damage =
                CombatRules.calculateSpellDamage(
                        caster, spell, target
                );

        if (!caster.consumeSpellForCast(spell)) {
            return false;
        }

        double dodgeProbability =
                CombatRules.getMonsterDodgeProbability(target);

        boolean dodged =
                CombatRules.rollDodge(
                        dodgeProbability, random
                );

        if (!dodged) {
            target.takeDamage(damage);

            if (target.isAlive()) {
                spell.applyEffect(target);
            }
        }

        finishHeroAction(caster);
        return true;
    }

    // Hero potion action
    public boolean heroUsePotion(
            Hero hero,
            Potion potion) {

        if (!canHeroAct(hero)) {
            return false;
        }

        if (potion == null
                || !hero.getInventory().contains(potion)
                || !potion.isUsable()) {
            return false;
        }

        if (!hero.usePotion(potion)) {
            return false;
        }

        finishHeroAction(hero);
        return true;
    }

    // Hero equipment actions
    public boolean heroEquipOneHand(
            Hero hero,
            Weapon weapon,
            Hand hand) {

        if (!canHeroAct(hero)) {
            return false;
        }

        if (!hero.getEquipment().equipOneHand(
                weapon, hand)) {
            return false;
        }

        finishHeroAction(hero);
        return true;
    }

    // Two-handed equipment.
    public boolean heroEquipTwoHands(
            Hero hero,
            Weapon weapon) {

        if (!canHeroAct(hero)) {
            return false;
        }

        if (!hero.getEquipment().equipTwoHands(weapon)) {
            return false;
        }

        finishHeroAction(hero);
        return true;
    }

    // Armor equipment.
    public boolean heroEquipArmor(
            Hero hero,
            Armor armor) {

        if (!canHeroAct(hero)) {
            return false;
        }

        if (!hero.getEquipment().equipArmor(armor)) {
            return false;
        }

        finishHeroAction(hero);
        return true;
    }

    // Hero unequip actions
    public boolean heroUnequipWeapon(
            Hero hero,
            Weapon weapon) {

        if (!canHeroAct(hero)) {
            return false;
        }

        if (!hero.getEquipment().unequipWeapon(weapon)) {
            return false;
        }

        finishHeroAction(hero);
        return true;
    }

    // Unequip armor.
    public boolean heroUnequipArmor(Hero hero) {

        if (!canHeroAct(hero)) {
            return false;
        }

        if (!hero.getEquipment().unequipArmor()) {
            return false;
        }

        finishHeroAction(hero);
        return true;
    }


    // Monster turn
    public boolean runMonsterTurn() {

        updateStatus();

        if (isOver()
                || phase != BattlePhase.MONSTER_PHASE
                || monsterTurnCompleted) {
            return false;
        }

        lastMonsterEvents.clear();

        for (Monster monster : monsters) {

            if (!monster.isAlive()) {
                continue;
            }

            List<Hero> aliveHeroes = getAliveHeroes();

            if (aliveHeroes.isEmpty()) {
                break;
            }

            // Randomly select a living hero.
            Hero target = aliveHeroes.get(
                    random.nextInt(aliveHeroes.size())
            );

            double damage =
                    CombatRules.calculateMonsterDamage(
                            monster, target
                    );

            double dodgeProbability =
                    CombatRules.getHeroDodgeProbability(target);

            boolean dodged =
                    CombatRules.rollDodge(
                            dodgeProbability, random
                    );

            if (dodged) {
                lastMonsterEvents.add(target.getName() + " dodged "
                        + monster.getName() + "'s attack.");
            } else {
                double before = target.getHp();
                Armor armor = target.getEquipment().getArmor();

                if (armor != null && armor.isUsable()) {
                    armor.use();
                }

                target.takeDamage(damage);
                double dealt = before - target.getHp();
                lastMonsterEvents.add(String.format(
                        "%s attacked %s for %.1f damage.%s",
                        monster.getName(), target.getName(), dealt,
                        target.isAlive() ? "" : " " + target.getName() + " fainted!"));
            }

            updateStatus();

            if (isOver()) {
                break;
            }
        }

        monsterTurnCompleted = true;

        // Regenerate survivors.
        // Skip defeated party.
        if (!isOver()) {
            regenerateHeroes();
        }

        return true;
    }

    // Round management
    public boolean nextRound() {

        updateStatus();

        if (isOver()
                || phase != BattlePhase.MONSTER_PHASE
                || !monsterTurnCompleted) {
            return false;
        }

        round++;

        actedHeroes.clear();

        monsterTurnCompleted = false;
        phase = BattlePhase.HERO_PHASE;

        return true;
    }
}

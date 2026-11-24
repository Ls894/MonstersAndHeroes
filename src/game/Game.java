package game;

import battle.BattlePhase;
import battle.BattleStatus;
import battle.CombatResolver;
import character.Hero;
import character.Monster;
import inventory.Hand;
import item.Armor;
import item.Item;
import item.Potion;
import item.Spell;
import item.Weapon;
import market.Market;
import world.MonsterAction;
import world.MonsterAI;
import world.MonsterFactory;
import world.Position;
import world.TileType;
import world.World;

import java.util.*;

// Coordinates Valor rounds; World owns all positions and movement rules.
public class Game {
    private static final int DEFAULT_SPAWN_INTERVAL = 8;
    private static final int GOLD_PER_LEVEL = 100;
    private static final int EXP_PER_LEVEL = 2;
    private final World world;
    private final List<Hero> heroes;
    private final MonsterFactory monsterFactory;
    private final CombatResolver combat;
    private final int spawnInterval;
    private final Set<Hero> actedHeroes = new HashSet<>();
    private final List<String> events = new ArrayList<>();
    private final List<Deque<Monster>> pendingSpawns = new ArrayList<>();
    private int round = 1;
    private BattlePhase phase = BattlePhase.HERO_PHASE;
    private BattleStatus status = BattleStatus.IN_PROGRESS;

    // Initialize the game with the default monster spawn interval.
    public Game(World world, List<Hero> heroes, MonsterFactory factory, Random random,
                List<Integer> lanes) {
        this(world, heroes, factory, random, lanes, DEFAULT_SPAWN_INTERVAL);
    }

    // Validate the game setup and place the initial heroes and monsters.
    public Game(World world, List<Hero> heroes, MonsterFactory factory, Random random,
                List<Integer> lanes, int spawnInterval) {
        if (world == null || world.getSize() != 8 || heroes == null || heroes.size() != 3
                || heroes.stream().anyMatch(h -> h == null || !h.isAlive())
                || new HashSet<>(heroes).size() != 3 || factory == null || random == null
                || lanes == null || lanes.size() != 3 || new HashSet<>(lanes).size() != 3
                || lanes.stream().anyMatch(l -> l == null || l < 0 || l > 2)
                || spawnInterval < 1) {
            throw new IllegalArgumentException("Invalid Valor game configuration.");
        }
        this.world = world;
        this.heroes = List.copyOf(heroes);
        this.monsterFactory = factory;
        this.combat = new CombatResolver(random);
        this.spawnInterval = spawnInterval;
        for (int lane = 0; lane < 3; lane++) pendingSpawns.add(new ArrayDeque<>());
        for (int i = 0; i < heroes.size(); i++) {
            if (!world.tryPlaceHero(heroes.get(i), lanes.get(i))) {
                throw new IllegalStateException("Could not place hero at Nexus.");
            }
        }
        scheduleWave();
        placePendingMonsters();
    }

    public World getWorld() { return world; }
    public List<Hero> getHeroes() { return heroes; }
    public int getRound() { return round; }
    public BattlePhase getPhase() { return phase; }
    public BattleStatus getStatus() { return status; }
    public boolean isGameOver() { return status != BattleStatus.IN_PROGRESS; }

    // Check whether the hero is eligible to act in the current round.
    public boolean canHeroAct(Hero hero) {
        return !isGameOver() && phase == BattlePhase.HERO_PHASE && hero != null
                && heroes.contains(hero) && hero.isAlive() && world.positionOf(hero).isPresent()
                && !actedHeroes.contains(hero);
    }

    // Check whether all eligible heroes have finished their actions.
    public boolean isHeroPhaseComplete() {
        return heroes.stream().noneMatch(this::canHeroAct);
    }

    // Return and clear pending game messages for the UI.
    public List<String> drainEvents() {
        List<String> result = List.copyOf(events);
        events.clear();
        return result;
    }

    public TileType terrainOf(Hero hero) {
        return world.positionOf(hero).map(world::getTileType).orElse(TileType.PLAIN);
    }

    // Find living monsters within the hero's attack range.
    public List<Monster> getAttackableMonsters(Hero hero) {
        if (!canHeroAct(hero)) return Collections.emptyList();
        return world.getAttackableMonsters(hero).stream().filter(Monster::isAlive)
                .collect(java.util.stream.Collectors.toList());
    }

    // Consume a successful action and check for hero victory.
    private boolean finishAction(Hero hero, boolean success) {
        if (!success) return false;
        actedHeroes.add(hero);
        if (world.hasReachedEnemyNexus(hero)) finishGame(BattleStatus.HERO_VICTORY);
        return true;
    }

    // Store the final result and stop further turns.
    private void finishGame(BattleStatus result) {
        status = result;
        phase = BattlePhase.FINISHED;
    }

    // Attempt a one-cell move using the world movement rules.
    public boolean move(Hero hero, int dr, int dc) {
        if (!canHeroAct(hero) || Math.abs(dr) + Math.abs(dc) != 1) return false;
        return finishAction(hero, world.tryMoveHero(hero, world.positionOf(hero).get().move(dr, dc)));
    }

    // Attempt to move the hero next to a teammate in another lane.
    public boolean teleport(Hero hero, Hero teammate, Position to) {
        return canHeroAct(hero) && teammate != null && heroes.contains(teammate)
                && teammate.isAlive() && to != null
                && finishAction(hero, world.tryTeleport(hero, teammate, to));
    }

    // Return the hero to their original Nexus.
    public boolean recall(Hero hero) {
        return canHeroAct(hero) && finishAction(hero, world.tryRecall(hero));
    }

    // Attempt to remove an adjacent obstacle.
    public boolean clearObstacle(Hero hero, Position target) {
        return canHeroAct(hero) && target != null
                && finishAction(hero, world.tryClearObstacle(hero, target));
    }

    // End the hero's turn without taking another action.
    public boolean pass(Hero hero) {
        return canHeroAct(hero) && finishAction(hero, true);
    }

    // Resolve a basic attack and process any resulting monster death.
    public boolean heroAttack(Hero hero, Monster target, Weapon weapon) {
        if (!getAttackableMonsters(hero).contains(target)) return false;
        double before = target.getHp();
        if (!combat.heroAttack(hero, target, weapon, terrainOf(hero))) return false;
        recordAttack(hero.getName(), target.getName(), before - target.getHp());
        handleMonsterDeath(target);
        return finishAction(hero, true);
    }

    // Resolve a spell attack and process any resulting monster death.
    public boolean heroCastSpell(Hero hero, Monster target, Spell spell) {
        if (!getAttackableMonsters(hero).contains(target)) return false;
        double before = target.getHp();
        if (!combat.heroCastSpell(hero, target, spell, terrainOf(hero))) return false;
        recordAttack(hero.getName() + " (" + spell.getName() + ")", target.getName(), before - target.getHp());
        handleMonsterDeath(target);
        return finishAction(hero, true);
    }

    private void recordAttack(String attacker, String target, double damage) {
        events.add(String.format("%s -> %s: %.1f damage%s", attacker, target, damage,
                damage == 0 ? " (dodged or blocked)" : ""));
    }

    private void handleMonsterDeath(Monster monster) {
        if (monster.isAlive() || world.positionOf(monster).isEmpty()) return;
        world.removeMonster(monster);
        int gold = GOLD_PER_LEVEL * monster.getLevel();
        int exp = EXP_PER_LEVEL * monster.getLevel();
        for (Hero hero : heroes) {
            hero.earnGold(gold);
            hero.gainExperience(exp);
        }
        events.add(monster.getName() + " defeated. Each hero receives " + gold + " gold and " + exp + " EXP.");
    }

    public boolean heroUsePotion(Hero hero, Potion potion) {
        return canHeroAct(hero) && finishAction(hero, combat.usePotion(hero, potion));
    }

    public boolean heroEquipOneHand(Hero hero, Weapon weapon, Hand hand) {
        return canHeroAct(hero) && finishAction(hero, combat.equipOneHand(hero, weapon, hand));
    }

    public boolean heroEquipTwoHands(Hero hero, Weapon weapon) {
        return canHeroAct(hero) && finishAction(hero, combat.equipTwoHands(hero, weapon));
    }

    public boolean heroEquipArmor(Hero hero, Armor armor) {
        return canHeroAct(hero) && finishAction(hero, combat.equipArmor(hero, armor));
    }

    public boolean heroUnequipWeapon(Hero hero, Weapon weapon) {
        return canHeroAct(hero) && finishAction(hero, combat.unequipWeapon(hero, weapon));
    }

    public boolean heroUnequipArmor(Hero hero) {
        return canHeroAct(hero) && finishAction(hero, combat.unequipArmor(hero));
    }

    public Optional<Market> getMarketFor(Hero hero) {
        return canHeroAct(hero) ? world.getMarketFor(hero) : Optional.empty();
    }

    // Buy an item without consuming the hero's turn.
    public boolean buy(Hero hero, Item item) {
        return getMarketFor(hero).map(m -> m.buyItem(hero, item)).orElse(false);
    }

    // Sell an item without consuming the hero's turn.
    public boolean sell(Hero hero, Item item) {
        return getMarketFor(hero).map(m -> m.sellItem(hero, item)).orElse(false);
    }

    // Repair a weapon or armor without consuming the hero's turn.
    public boolean repair(Hero hero, Item item) {
        return getMarketFor(hero).map(m -> {
            if (item instanceof Weapon) return m.repairWeapon(hero, (Weapon) item);
            if (item instanceof Armor) return m.repairArmor(hero, (Armor) item);
            return false;
        }).orElse(false);
    }

    // A single call completes the monster phase and starts the next round.
    public boolean advanceRound() {
        if (isGameOver() || phase != BattlePhase.HERO_PHASE || !isHeroPhaseComplete()) return false;
        phase = BattlePhase.MONSTER_PHASE;
        for (Monster monster : world.getMonstersOnBoard()) {
            if (!monster.isAlive()) continue;
            MonsterAction action = MonsterAI.chooseAction(world, monster);
            switch (action.getType()) {
                case ATTACK:
                    Hero target = action.getTargetHero();
                    double before = target.getHp();
                    if (combat.monsterAttack(monster, target, terrainOf(target))) {
                        recordAttack(monster.getName(), target.getName(), before - target.getHp());
                        if (!target.isAlive()) {
                            world.removeHero(target);
                            events.add(target.getName() + " fainted.");
                        }
                    }
                    break;
                case MOVE:
                    if (world.tryMoveMonster(monster, action.getDestination())) {
                        events.add(monster.getName() + " moved to " + action.getDestination());
                        if (world.hasReachedEnemyNexus(monster)) {
                            finishGame(BattleStatus.MONSTER_VICTORY);
                            return true;
                        }
                    }
                    break;
                case WAIT:
                    events.add(monster.getName() + " waits.");
                    break;
            }
        }
        for (Hero hero : heroes) hero.regenerateAfterRound();
        round++;
        actedHeroes.clear();
        for (Hero hero : heroes) {
            if (!hero.isAlive()) {
                if (world.tryRespawnHero(hero)) {
                    hero.reviveAtNexus();
                    events.add(hero.getName() + " respawned.");
                } else events.add(hero.getName() + " cannot respawn: Nexus occupied; will retry next round.");
            }
        }
        if ((round - 1) % spawnInterval == 0) scheduleWave();
        placePendingMonsters();
        phase = BattlePhase.HERO_PHASE;
        return true;
    }

    // Queue one new monster per lane at the highest hero level.
    private void scheduleWave() {
        int level = heroes.stream().mapToInt(Hero::getLevel).max().orElse(1);
        for (Deque<Monster> queue : pendingSpawns) queue.addLast(monsterFactory.createMonster(level));
    }

    // Place queued monsters when their spawn spaces are available.
    private void placePendingMonsters() {
        for (int lane = 0; lane < pendingSpawns.size(); lane++) {
            Deque<Monster> queue = pendingSpawns.get(lane);
            if (!queue.isEmpty() && world.trySpawnMonster(queue.peekFirst(), lane)) {
                events.add(queue.removeFirst().getName() + " spawned in lane " + (lane + 1) + ".");
            }
        }
    }
}
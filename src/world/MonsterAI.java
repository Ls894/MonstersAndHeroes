package world;

import character.Hero;
import character.Monster;

import java.util.List;

public final class MonsterAI {
    private MonsterAI() {
    }

    public static MonsterAction chooseAction(World world, Monster monster) {
        Position from = world.positionOf(monster).orElse(null);
        if (from == null) {
            return MonsterAction.waitTurn();
        }

        List<Hero> targets = world.getAttackableHeroes(monster);
        if (!targets.isEmpty()) {
            return MonsterAction.attack(targets.get(0));
        }

        Position forward = from.move(1, 0);
        if (world.canMoveMonsterTo(monster, forward)) {
            return MonsterAction.move(forward);
        }

        return MonsterAction.waitTurn();
    }
}
package world;

import character.Hero;

public final class MonsterAction {
    public enum Type {
        ATTACK,
        MOVE,
        WAIT
    }

    private final Type type;
    private final Hero targetHero;
    private final Position destination;

    private MonsterAction(Type type, Hero targetHero, Position destination) {
        this.type = type;
        this.targetHero = targetHero;
        this.destination = destination;
    }

    public static MonsterAction attack(Hero targetHero) {
        return new MonsterAction(Type.ATTACK, targetHero, null);
    }

    public static MonsterAction move(Position destination) {
        return new MonsterAction(Type.MOVE, null, destination);
    }

    public static MonsterAction waitTurn() {
        return new MonsterAction(Type.WAIT, null, null);
    }

    public Type getType() {
        return type;
    }

    public Hero getTargetHero() {
        return targetHero;
    }

    public Position getDestination() {
        return destination;
    }
}
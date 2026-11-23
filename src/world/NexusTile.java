package world;
import market.Market;

import java.util.Optional;

public class NexusTile extends Tile {
    private final Market market;

    public NexusTile() {
        this(null);
    }

    public NexusTile(Market market) {
        this.market = market;
    }

    public Optional<Market> getMarket() {
        return Optional.ofNullable(market);
    }

    @Override
    public boolean isAccessible() {
        return true;
    }

    @Override
    public char getSymbol() {
        return 'N';
    }

    @Override
    public TileType getType() {
        return TileType.NEXUS;
    }
}

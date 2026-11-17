package world;

import market.Market;

// Connects a world tile to its market.
public final class MarketTile extends Tile {

    private final Market market;

    public MarketTile(Market market) {
        if (market == null) {
            throw new IllegalArgumentException(
                    "Market cannot be null."
            );
        }

        this.market = market;
    }

    // Creates a market when none is supplied.
    public MarketTile() {
        this(new Market());
    }

    // Provides access to this tile's market.
    public Market getMarket() {
        return market;
    }

    @Override
    public boolean isAccessible() {
        return true;
    }

    @Override
    public char getSymbol() {
        return 'M';
    }
}

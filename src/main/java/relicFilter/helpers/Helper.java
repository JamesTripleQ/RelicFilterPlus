package relicFilter.helpers;

import com.megacrit.cardcrawl.relics.AbstractRelic;

public class Helper {
    public static int RelicTierToInt(AbstractRelic relic) {
        AbstractRelic.RelicTier relicTier = relic.tier;
        switch (relicTier) {
            case STARTER:
                return 0;
            case COMMON:
                return 1;
            case UNCOMMON:
                return 2;
            case RARE:
                return 3;
            case BOSS:
                return 4;
            case SPECIAL:
                return 5;
            case SHOP:
                return 6;
            case DEPRECATED:
                return 7;
        }
        return -1;
    }

    public static int RelicTierToInt(AbstractRelic.RelicTier relicTier) {
        switch (relicTier) {
            case STARTER:
                return 0;
            case COMMON:
                return 1;
            case UNCOMMON:
                return 2;
            case RARE:
                return 3;
            case BOSS:
                return 4;
            case SPECIAL:
                return 5;
            case SHOP:
                return 6;
            case DEPRECATED:
                return 7;
        }
        return -1;
    }

    public static AbstractRelic.RelicTier IntToRelicTier(int tier) {
        switch (tier) {
            case 0:
                return AbstractRelic.RelicTier.STARTER;
            case 1:
                return AbstractRelic.RelicTier.COMMON;
            case 2:
                return AbstractRelic.RelicTier.UNCOMMON;
            case 3:
                return AbstractRelic.RelicTier.RARE;
            case 4:
                return AbstractRelic.RelicTier.BOSS;
            case 5:
                return AbstractRelic.RelicTier.SPECIAL;
            case 6:
                return AbstractRelic.RelicTier.SHOP;
            case 7:
                return AbstractRelic.RelicTier.DEPRECATED;
        }
        return AbstractRelic.RelicTier.DEPRECATED;
    }
}

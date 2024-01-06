package relicFilter.patches.com.megacrit.cardcrawl.relics.TinyHouse;

import com.evacipated.cardcrawl.modthespire.lib.*;
import com.evacipated.cardcrawl.modthespire.patcher.PatchingException;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.PotionHelper;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.relics.Sozu;
import com.megacrit.cardcrawl.relics.TinyHouse;
import javassist.CannotCompileException;
import javassist.CtBehavior;

import java.util.ArrayList;

@SpirePatch(clz = TinyHouse.class, method = "onEquip")
public class TinyHouseOnEquip {
    @SpireInsertPatch(locator = Locator.class)
    public static SpireReturn<Void> Insert(TinyHouse __instance) {
        AbstractDungeon.player.increaseMaxHp(5, true);
        if (__instance.tier == AbstractRelic.RelicTier.BOSS) {
            AbstractDungeon.getCurrRoom().addGoldToRewards(50);
            AbstractDungeon.getCurrRoom().addPotionToRewards(PotionHelper.getRandomPotion(AbstractDungeon.miscRng));
            AbstractDungeon.combatRewardScreen.open(__instance.DESCRIPTIONS[3]);
            (AbstractDungeon.getCurrRoom()).rewardPopOutTimer = 0.0F;
        } else {
            AbstractDungeon.player.gainGold(50);
            if (!AbstractDungeon.player.hasRelic(Sozu.ID)) {
                AbstractDungeon.player.obtainPotion(PotionHelper.getRandomPotion(AbstractDungeon.miscRng));
            }
        }
        return SpireReturn.Return();
    }

    private static class Locator extends SpireInsertLocator {
        public int[] Locate(CtBehavior ctMethodToPatch) throws CannotCompileException, PatchingException {
            Matcher finalMatcher = new Matcher.MethodCallMatcher(AbstractPlayer.class, "increaseMaxHp");
            return LineFinder.findInOrder(ctMethodToPatch, new ArrayList<>(), finalMatcher);
        }
    }
}

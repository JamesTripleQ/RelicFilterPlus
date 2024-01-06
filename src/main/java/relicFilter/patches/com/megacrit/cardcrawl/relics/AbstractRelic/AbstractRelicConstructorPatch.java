package relicFilter.patches.com.megacrit.cardcrawl.relics.AbstractRelic;

import com.evacipated.cardcrawl.modthespire.lib.SpireInsertPatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import relicFilter.RelicFilterMod;

// The rloc hasn't been updated, but it works regardless, so I'm not changing it
@SpirePatch(clz = AbstractRelic.class, method = SpirePatch.CONSTRUCTOR)
public class AbstractRelicConstructorPatch {
    @SpireInsertPatch(rloc = 12)
    public static void Insert(AbstractRelic __instance, String setId, String imgName, AbstractRelic.RelicTier tier, AbstractRelic.LandingSound sfx) {
        if (RelicFilterMod.MasterList != null) __instance.tier = RelicFilterMod.MasterList.get(__instance.relicId);
    }
}

package relicFilter;

import basemod.BaseMod;
import basemod.IUIElement;
import basemod.ModLabel;
import basemod.ModPanel;
import basemod.ReflectionHacks;
import basemod.interfaces.PostDungeonInitializeSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import com.badlogic.gdx.graphics.Texture;
import com.evacipated.cardcrawl.modthespire.Loader;
import com.evacipated.cardcrawl.modthespire.ModInfo;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.daily.mods.*;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.helpers.ModHelper;
import com.megacrit.cardcrawl.helpers.RelicLibrary;
import com.megacrit.cardcrawl.relics.*;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import relicFilter.helpers.Helper;
import relicFilter.panelUI.OptionsSetting;
import relicFilter.panelUI.RelicSetting;
import relicFilter.panelUI.RelicSettingsButton;

@SpireInitializer
public class RelicFilterMod implements PostInitializeSubscriber, PostDungeonInitializeSubscriber {
    public static final Logger logger = LogManager.getLogger(RelicFilterMod.class.getName());
    public static HashMap<AbstractRelic, ArrayList<RelicSetting>> RelicSettings = new HashMap<>();
    public static HashMap<String, AbstractRelic.RelicTier> MasterList;
    public static ModPanel settingsPanel;
    public static RelicFilterModMenu subMenu;
    public static TreeMap<String, AbstractRelic> RelicsList = new TreeMap<>(Comparator.comparing(String::toLowerCase));
    static SpireConfig config;
    public static String DEFAULTSETTINGSUFFIX = "_relicfilter";
    private static final String MODNAME = "RelicFilterMod";
    private static final String AUTHOR = "mattdeav, The_Evil_Pickle";
    private static final String DESCRIPTION = "filter relic tiers";
    public static HashMap<String, RelicSetting> ConfigSettings = new HashMap<>();
    static final float setting_start_x = 970.0F;
    static final float setting_start_y = 750.0F;

    public RelicFilterMod() {
        BaseMod.subscribe(this);
    }

    public static void initialize() {
        new RelicFilterMod();
    }

    public static void BuildSettings(AbstractRelic relic) {
        RelicSettings.put(relic, BuildRelicSettings(relic));
        for (RelicSetting setting : RelicSettings.get(relic))
            ConfigSettings.put(setting.settingsId, setting);
    }

    public static ArrayList<RelicSetting> BuildRelicSettings(AbstractRelic relic) {
        String settingsId = relic.relicId + DEFAULTSETTINGSUFFIX;
        ArrayList<RelicSetting> settings = new ArrayList<>();
        List<String> settingStrings = new ArrayList<>();
        int propertyNum = Helper.RelicTierToInt(relic);
        settingStrings.add("Starter");
        settingStrings.add("Common");
        settingStrings.add("Uncommon");
        settingStrings.add("Rare");
        settingStrings.add("Boss");
        settingStrings.add("Special");
        settingStrings.add("Shop");
        settingStrings.add("Deprecated");
        OptionsSetting SETTING_MODE = new OptionsSetting(settingsId, "Relic Tier", propertyNum, settingStrings);
        settings.add(SETTING_MODE);
        return settings;
    }

    public static RelicSettingsButton BuildSettingsButton(AbstractRelic relic) {
        ArrayList<IUIElement> settingElements = new ArrayList<>();
        ArrayList<RelicSetting> settings = new ArrayList<>();
        float x = setting_start_x;
        float y = setting_start_y;
        settingElements.add(new ModLabel(relic.name + " (" + relic.relicId + ")", x, y, settingsPanel, me -> {
        }));
        y -= 50.0F;

        for (RelicSetting setting : RelicSettings.get(relic)) {
            settings.add(setting);
            settingElements.addAll(setting.GenerateElements(x, y));
            y -= setting.elementHeight;
        }
        return new RelicSettingsButton(null, relic, settingElements, settings);
    }

    public static ArrayList<AbstractRelic> getAllRelics() {
        ArrayList<AbstractRelic> relics = new ArrayList<>();
        HashMap<String, AbstractRelic> sharedRelics = ReflectionHacks.getPrivateStatic(RelicLibrary.class, "sharedRelics");
        if (sharedRelics != null) relics.addAll(sharedRelics.values());
        HashMap<String, AbstractRelic> redRelics = ReflectionHacks.getPrivateStatic(RelicLibrary.class, "redRelics");
        if (redRelics != null) relics.addAll(redRelics.values());
        HashMap<String, AbstractRelic> greenRelics = ReflectionHacks.getPrivateStatic(RelicLibrary.class, "greenRelics");
        if (greenRelics != null) relics.addAll(greenRelics.values());
        HashMap<String, AbstractRelic> blueRelics = ReflectionHacks.getPrivateStatic(RelicLibrary.class, "blueRelics");
        if (blueRelics != null) relics.addAll(blueRelics.values());
        HashMap<String, AbstractRelic> purpleRelics = ReflectionHacks.getPrivateStatic(RelicLibrary.class, "purpleRelics");
        if (purpleRelics != null) relics.addAll(purpleRelics.values());
        if (BaseMod.getAllCustomRelics() != null)
            for (HashMap<String, AbstractRelic> e : BaseMod.getAllCustomRelics().values()) {
                if (e != null) relics.addAll(e.values());
            }
        return relics;
    }

    @Override
    public void receivePostInitialize() {
        settingsPanel = new ModPanel();
        try {
            config = new SpireConfig(MODNAME, "filterSettingsData");
        } catch (IOException e) {
            e.printStackTrace();
        }
        subMenu = new RelicFilterModMenu(settingsPanel);
        buildRelicList();
        loadSettingsData();
        resetRelicSettings();
        Texture badgeTexture = ImageMaster.loadImage("images/modbadge.png");
        BaseMod.registerModBadge(badgeTexture, MODNAME, AUTHOR, DESCRIPTION, settingsPanel);
        logger.info("done PostInitialize");
    }

    public static void buildRelicList() {
        ArrayList<RelicSettingsButton> settingsButtons = new ArrayList<>();
        RelicsList = new TreeMap<>(Comparator.comparing(String::toLowerCase));
        RelicSettings = new HashMap<>();
        for (AbstractRelic relic : getAllRelics()) {
            RelicsList.put(relic.relicId, relic);
            BuildSettings(relic);
        }
        for (Map.Entry<String, AbstractRelic> e : RelicsList.entrySet())
            settingsButtons.add(BuildSettingsButton(e.getValue()));
        subMenu.createRelicsPagination(settingsButtons);
    }

    @Override
    public void receivePostDungeonInitialize() {
        postDungeonInitializeRelicSettings();
    }

    public void resetRelicSettings() {
        MasterList = new HashMap<>();
        for (Map.Entry<AbstractRelic, ArrayList<RelicSetting>> es : RelicSettings.entrySet()) {
            AbstractRelic relic = es.getKey();
            int tierInt = ((OptionsSetting) ((ArrayList<?>) es.getValue()).get(0)).value;
            logger.info("Changing relic tier for " + relic.relicId + " from " + relic.tier + " to " + tierInt);
            AbstractRelic.RelicTier relicTier = relic.tier;
            switch (relicTier) {
                case STARTER:
                    RelicLibrary.starterList.remove(relic);
                    break;
                case COMMON:
                    RelicLibrary.commonList.remove(relic);
                    break;
                case UNCOMMON:
                    RelicLibrary.uncommonList.remove(relic);
                    break;
                case RARE:
                    RelicLibrary.rareList.remove(relic);
                    break;
                case SHOP:
                    RelicLibrary.shopList.remove(relic);
                    break;
                case SPECIAL:
                    RelicLibrary.specialList.remove(relic);
                    break;
                case BOSS:
                    RelicLibrary.bossList.remove(relic);
                    break;
                case DEPRECATED:
                    logger.info(relic.relicId + " is deprecated");
                    break;
                default:
                    logger.info(relic.relicId + " is unknown tier");
                    break;
            }
            relic.tier = Helper.IntToRelicTier(tierInt);
            RelicLibrary.addToTierList(relic);
            MasterList.put(relic.relicId, relic.tier);
        }
        Collections.sort(RelicLibrary.starterList);
        Collections.sort(RelicLibrary.commonList);
        Collections.sort(RelicLibrary.uncommonList);
        Collections.sort(RelicLibrary.rareList);
        Collections.sort(RelicLibrary.bossList);
        Collections.sort(RelicLibrary.specialList);
        Collections.sort(RelicLibrary.shopList);
        logger.info("done changing relic tiers");
    }

    public void postDungeonInitializeRelicSettings() {
        resetRelicSettings();
        AbstractDungeon.relicsToRemoveOnStart.clear();
        AbstractDungeon.commonRelicPool.clear();
        AbstractDungeon.uncommonRelicPool.clear();
        AbstractDungeon.rareRelicPool.clear();
        AbstractDungeon.shopRelicPool.clear();
        AbstractDungeon.bossRelicPool.clear();
        RelicLibrary.populateRelicPool(AbstractDungeon.commonRelicPool, AbstractRelic.RelicTier.COMMON, AbstractDungeon.player.chosenClass);
        RelicLibrary.populateRelicPool(AbstractDungeon.uncommonRelicPool, AbstractRelic.RelicTier.UNCOMMON, AbstractDungeon.player.chosenClass);
        RelicLibrary.populateRelicPool(AbstractDungeon.rareRelicPool, AbstractRelic.RelicTier.RARE, AbstractDungeon.player.chosenClass);
        RelicLibrary.populateRelicPool(AbstractDungeon.shopRelicPool, AbstractRelic.RelicTier.SHOP, AbstractDungeon.player.chosenClass);
        RelicLibrary.populateRelicPool(AbstractDungeon.bossRelicPool, AbstractRelic.RelicTier.BOSS, AbstractDungeon.player.chosenClass);
        if (AbstractDungeon.floorNum >= 1) for (AbstractRelic r : AbstractDungeon.player.relics)
            AbstractDungeon.relicsToRemoveOnStart.add(r.relicId);
        Collections.shuffle(AbstractDungeon.commonRelicPool, new Random(AbstractDungeon.relicRng.randomLong()));
        Collections.shuffle(AbstractDungeon.uncommonRelicPool, new Random(AbstractDungeon.relicRng.randomLong()));
        Collections.shuffle(AbstractDungeon.rareRelicPool, new Random(AbstractDungeon.relicRng.randomLong()));
        Collections.shuffle(AbstractDungeon.shopRelicPool, new Random(AbstractDungeon.relicRng.randomLong()));
        Collections.shuffle(AbstractDungeon.bossRelicPool, new Random(AbstractDungeon.relicRng.randomLong()));
        if (ModHelper.isModEnabled(Flight.ID)) AbstractDungeon.relicsToRemoveOnStart.add(WingBoots.ID);
        if (ModHelper.isModEnabled(Diverse.ID)) AbstractDungeon.relicsToRemoveOnStart.add(PrismaticShard.ID);
        if (ModHelper.isModEnabled(DeadlyEvents.ID)) AbstractDungeon.relicsToRemoveOnStart.add(JuzuBracelet.ID);
        if (ModHelper.isModEnabled(Hoarder.ID)) AbstractDungeon.relicsToRemoveOnStart.add(SmilingMask.ID);
        if (ModHelper.isModEnabled(Draft.ID) || ModHelper.isModEnabled(SealedDeck.ID) || ModHelper.isModEnabled(Shiny.ID) || ModHelper.isModEnabled(Insanity.ID))
            AbstractDungeon.relicsToRemoveOnStart.add(PandorasBox.ID);
        Iterator<String> x;
        for (x = AbstractDungeon.relicsToRemoveOnStart.iterator(); x.hasNext(); ) {
            String remove = x.next();
            Iterator<String> s;
            for (s = AbstractDungeon.commonRelicPool.iterator(); s.hasNext(); ) {
                String derp = s.next();
                if (derp.equals(remove)) {
                    s.remove();
                    logger.info(derp + " removed");
                    break;
                }
            }
            for (s = AbstractDungeon.uncommonRelicPool.iterator(); s.hasNext(); ) {
                String derp = s.next();
                if (derp.equals(remove)) {
                    s.remove();
                    logger.info(derp + " removed");
                    break;
                }
            }
            for (s = AbstractDungeon.rareRelicPool.iterator(); s.hasNext(); ) {
                String derp = s.next();
                if (derp.equals(remove)) {
                    s.remove();
                    logger.info(derp + " removed");
                    break;
                }
            }
            for (s = AbstractDungeon.bossRelicPool.iterator(); s.hasNext(); ) {
                String derp = s.next();
                if (derp.equals(remove)) {
                    s.remove();
                    logger.info(derp + " removed");
                    break;
                }
            }
            for (s = AbstractDungeon.shopRelicPool.iterator(); s.hasNext(); ) {
                String derp = s.next();
                if (derp.equals(remove)) {
                    s.remove();
                    logger.info(derp + " removed");
                }
            }
        }
        if (Settings.isDebug) {
            logger.info("Relic (Common):");
            for (x = AbstractDungeon.commonRelicPool.iterator(); x.hasNext(); ) {
                String s1 = x.next();
                logger.info(" " + s1);
            }
            logger.info("Relic (Uncommon):");
            for (x = AbstractDungeon.uncommonRelicPool.iterator(); x.hasNext(); ) {
                String s2 = x.next();
                logger.info(" " + s2);
            }
            logger.info("Relic (Rare):");
            for (x = AbstractDungeon.rareRelicPool.iterator(); x.hasNext(); ) {
                String s3 = x.next();
                logger.info(" " + s3);
            }
            logger.info("Relic (Shop):");
            for (x = AbstractDungeon.shopRelicPool.iterator(); x.hasNext(); ) {
                String s4 = x.next();
                logger.info(" " + s4);
            }
            logger.info("Relic (Boss):");
            for (x = AbstractDungeon.bossRelicPool.iterator(); x.hasNext(); ) {
                String s5 = x.next();
                logger.info(" " + s5);
            }
        }
    }

    public static void saveSettingsData() {
        try {
            SpireConfig config = new SpireConfig(MODNAME, "filterSettingsData");
            for (String key : ConfigSettings.keySet()) {
                RelicSetting setting = ConfigSettings.get(key);
                setting.SaveToData(config);
            }
            config.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadSettingsData() {
        logger.info("RelicFilter settings data loading");
        try {
            config.load();
            for (String key : ConfigSettings.keySet()) {
                RelicSetting setting = ConfigSettings.get(key);
                if (!config.has(setting.settingsId)) {
                    config.setString(setting.settingsId, setting.defaultProperty);
                    config.setBool(setting.settingsId + DEFAULTSETTINGSUFFIX, true);
                } else if (!config.has(setting.settingsId + DEFAULTSETTINGSUFFIX)) {
                    config.setBool(setting.settingsId + DEFAULTSETTINGSUFFIX, false);
                }
                setting.LoadFromData(config);
            }
        } catch (IOException e) {
            logger.error("Error loading RelicFilter settings data!!!");
            e.printStackTrace();
        }
    }

    public static String findModName(Class<?> cls) {
        URL locationURL = cls.getProtectionDomain().getCodeSource().getLocation();
        if (locationURL == null) try {
            ClassPool pool = Loader.getClassPool();
            CtClass ctCls = pool.get(cls.getName());
            String url = ctCls.getURL().getFile();
            int i = url.lastIndexOf('!');
            url = url.substring(0, i);
            locationURL = new URL(url);
        } catch (NotFoundException | MalformedURLException e) {
            e.printStackTrace();
        }
        if (locationURL == null) return "Unknown";
        try {
            if (locationURL.equals((new File(Loader.STS_JAR)).toURI().toURL())) return null;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return "Unknown";
        }
        for (ModInfo modInfo : Loader.MODINFOS) {
            if (locationURL.equals(modInfo.jarURL)) return modInfo.Name;
        }
        return "Unknown";
    }
}

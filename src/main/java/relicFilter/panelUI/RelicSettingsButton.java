package relicFilter.panelUI;

import basemod.IUIElement;
import basemod.ModButton;
import basemod.patches.whatmod.WhatMod;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Interpolation;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.GameDictionary;
import com.megacrit.cardcrawl.helpers.Hitbox;
import com.megacrit.cardcrawl.helpers.PowerTip;
import com.megacrit.cardcrawl.helpers.TipHelper;
import com.megacrit.cardcrawl.helpers.input.InputHelper;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import com.megacrit.cardcrawl.unlock.UnlockTracker;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import relicFilter.RelicFilterMod;
import relicFilter.helpers.Helper;

public class RelicSettingsButton implements IUIElement {
    public static final float DEFAULT_X = 380.0F;
    public static final float DEFAULT_Y = 715.0F;
    public static final float DEFAULT_W = 100.0F;
    public static final float DEFAULT_H = 100.0F;
    public static RelicSettingsButton activeButton;
    public Texture image;
    public Texture outline;
    public static Texture overlay_deprecated = new Texture("images/deprecated_x.png");
    public static Texture overlay_increased_tier = new Texture("images/increased_tier.png");
    public static Texture overlay_decreased_tier = new Texture("images/lowered_tier.png");
    public float x;
    public float y;
    public float w;
    public float h;
    public ArrayList<PowerTip> tips = new ArrayList<>();
    public String description;
    private Hitbox hitbox;
    private IUIElement resetButton;
    private IUIElement backButton;
    public AbstractRelic relic;
    public List<IUIElement> elements;
    public List<RelicSetting> settings;
    public boolean isSelected;
    public Pagination parent;
    public String modName;
    private Color rendColor;
    public float flashTimer;
    public int lastValue;

    public RelicSettingsButton(Pagination parent, AbstractRelic relic, List<IUIElement> elements) {
        this(parent, relic, DEFAULT_X, DEFAULT_Y, DEFAULT_W, DEFAULT_H, elements);
    }

    public RelicSettingsButton(Pagination parent, AbstractRelic relic, List<IUIElement> elements, List<RelicSetting> settings) {
        this(parent, relic, elements);
        this.settings = settings;
        this.lastValue = ((OptionsSetting) this.settings.get(0)).value;
    }

    public RelicSettingsButton(Pagination parent, AbstractRelic relic, float x, float y, float width, float height, List<IUIElement> elements) {
        this(relic.img, relic.outlineImg, x, y, width, height, elements);
        this.parent = parent;
        tips.add(new PowerTip(relic.name, getRelicTierString(relic.tier) + relic.description));
        description = relic.description;
        this.relic = relic;
        initializeTips();
        w *= 1.5F;
        h *= 1.5F;
        if (!UnlockTracker.isRelicSeen(relic.relicId)) rendColor = Color.BLACK;
    }

    public RelicSettingsButton(Texture image, Texture outline, float x, float y, float width, float height, List<IUIElement> elements) {
        relic = null;
        this.image = image;
        flashTimer = 0.0F;
        this.outline = outline;
        this.x = x;
        this.y = y;
        w = width;
        h = height;
        this.elements = elements;
        settings = new ArrayList<>();
        isSelected = false;
        hitbox = new Hitbox((this.x + 16) * Settings.scale, (this.y + 16) * Settings.scale, w * Settings.scale, h * Settings.scale);
        backButton = new ModButton(1300.0F, 540.0F, new Texture("images/back_to_mod_menu.png"), RelicFilterMod.settingsPanel, me -> unselect());
        resetButton = new ModButton(1310.0F, 300.0F, new Texture("images/reset_this_button.png"), RelicFilterMod.settingsPanel, me -> resetAll());
        rendColor = Color.WHITE;
    }

    public String getRelicTierString(AbstractRelic.RelicTier tier) {
        switch (tier) {
            case COMMON:
                return "#gCommon #gRelic NL ";
            case UNCOMMON:
                return "#gUncommon #gRelic NL ";
            case RARE:
                return "#gRare #gRelic NL ";
            case DEPRECATED:
                return "#rDeprecated #rRelic NL ";
            case STARTER:
                return "#pStarter #pRelic NL ";
            case BOSS:
                return "#bBoss #bRelic NL ";
            case SPECIAL:
                return "#pSpecial #pRelic NL ";
            case SHOP:
                return "#yShop #yRelic NL ";
        }
        return "#rRelic #rTier #rUnknown NL ";
    }

    @Override
    public void render(SpriteBatch sb) {
        if (outline != null) {
            sb.setColor(Color.BLACK);
            sb.draw(outline, x * Settings.scale, y * Settings.scale, w * Settings.scale, h * Settings.scale);
        }
        sb.setColor(rendColor);
        sb.draw(image, x * Settings.scale, y * Settings.scale, w * Settings.scale, h * Settings.scale);
        if (relic != null) {
            sb.setColor(Color.WHITE);
            if (Helper.IntToRelicTier(((OptionsSetting) settings.get(0)).value) == AbstractRelic.RelicTier.DEPRECATED) {
                sb.draw(overlay_deprecated, x * Settings.scale, y * Settings.scale, w * Settings.scale, h * Settings.scale);
            } else if (((OptionsSetting) settings.get(0)).defaultIntValue != ((OptionsSetting) settings.get(0)).value) {
                if (((OptionsSetting) settings.get(0)).defaultIntValue < ((OptionsSetting) settings.get(0)).value) {
                    sb.draw(overlay_increased_tier, x * Settings.scale, y * Settings.scale, w * Settings.scale, h * Settings.scale);
                } else {
                    sb.draw(overlay_decreased_tier, x * Settings.scale, y * Settings.scale, w * Settings.scale, h * Settings.scale);
                }
            }
        }
        hitbox.render(sb);
        if (hitbox.hovered)
            TipHelper.queuePowerTips(InputHelper.mX + 50.0F * Settings.scale, InputHelper.mY + 50.0F * Settings.scale, tips);
        if (isSelected) {
            for (IUIElement element : elements)
                element.render(sb);
            resetButton.render(sb);
            backButton.render(sb);
        }
        if (flashTimer > 0.0F) {
            flashTimer -= Gdx.graphics.getDeltaTime();
            float tmp = Interpolation.exp10In.apply(0.0F, 4.0F, flashTimer / 2.0F);
            sb.setBlendFunction(770, 1);
            sb.setColor(new Color(1.0F, 1.0F, 1.0F, flashTimer * 0.2F));
            float tmpX = x - 18.0F;
            float tmpY = y - 20.0F;
            sb.draw(image, tmpX, tmpY, 64.0F, 64.0F, 128.0F, 128.0F, Settings.scale + tmp, Settings.scale + tmp, 0.0F, 0, 0, 128, 128, false, false);
            sb.draw(image, tmpX, tmpY, 64.0F, 64.0F, 128.0F, 128.0F, Settings.scale + tmp * 0.66F, Settings.scale + tmp * 0.66F, 0.0F, 0, 0, 128, 128, false, false);
            sb.draw(image, tmpX, tmpY, 64.0F, 64.0F, 128.0F, 128.0F, Settings.scale + tmp / 3.0F, Settings.scale + tmp / 3.0F, 0.0F, 0, 0, 128, 128, false, false);
            sb.setBlendFunction(770, 771);
        }
    }

    @Override
    public void update() {
        hitbox.update();
        if (hitbox.hovered) if (InputHelper.justClickedLeft) {
            CardCrawlGame.sound.play("UI_CLICK_1");
            if (isSelected) {
                unselect();
            } else {
                if (activeButton != null) activeButton.isSelected = false;
                activeButton = this;
                isSelected = true;
                parent.displaySortOptions(false);
            }
        } else if (InputHelper.justClickedRight) {
            CardCrawlGame.sound.play("UI_CLICK_1");
            if (Helper.IntToRelicTier(((OptionsSetting) settings.get(0)).value) == AbstractRelic.RelicTier.DEPRECATED) {
                settings.get(0).ResetToDefault();
            } else {
                ((OptionsSetting) settings.get(0)).SetValue(Helper.RelicTierToInt(AbstractRelic.RelicTier.DEPRECATED));
            }
        }
        if (lastValue != ((OptionsSetting) settings.get(0)).value) {
            lastValue = ((OptionsSetting) settings.get(0)).value;
            flashTimer = 2.0F;
        }
        if (isSelected) {
            for (IUIElement element : elements)
                element.update();
            resetButton.update();
            backButton.update();
        }
    }

    public void resetAll() {
        for (RelicSetting element : settings)
            element.ResetToDefault();
    }

    public void unselect() {
        isSelected = false;
        parent.displaySortOptions(true);
    }

    protected void initializeTips() {
        Scanner desc = new Scanner(description);
        while (desc.hasNext()) {
            String s = desc.next();
            if (s.charAt(0) == '#') s = s.substring(2);
            s = s.replace(',', ' ');
            s = s.replace('.', ' ');
            s = s.trim();
            s = s.toLowerCase();
            boolean alreadyExists = false;
            if (GameDictionary.keywords.containsKey(s)) {
                s = GameDictionary.parentWord.get(s);
                for (PowerTip t : tips) {
                    if (t.header.toLowerCase().equals(s)) {
                        alreadyExists = true;
                        break;
                    }
                }
                if (!alreadyExists) tips.add(new PowerTip(TipHelper.capitalize(s), GameDictionary.keywords.get(s)));
            }
        }
        if (WhatMod.enabled && !relic.tips.isEmpty() && relic.tips.get(0).header.equalsIgnoreCase(relic.name)) {
            modName = RelicFilterMod.findModName(relic.getClass());
            if (modName != null) tips.get(0).body = FontHelper.colorString(modName, "p") + " NL " + tips.get(0).body;
        }
        desc.close();
    }

    @Override
    public int renderLayer() {
        return 1;
    }

    @Override
    public int updateOrder() {
        return 1;
    }
}

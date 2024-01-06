package relicFilter.panelUI;

import basemod.IUIElement;
import basemod.ModLabel;
import basemod.ModToggleButton;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.Prefs;

import java.util.ArrayList;
import java.util.List;

import relicFilter.RelicFilterMod;

public class OptionsSetting extends RelicSetting {
    public int value;
    public int defaultIntValue;
    List<String> optionStrings;
    ArrayList<ModToggleButton> buttons;
    public BitmapFont font;
    public float y_interval;

    public OptionsSetting(String id, String name, int defaultProperty, List<String> options) {
        super(id, name, Integer.toString(defaultProperty));
        this.value = defaultProperty;
        this.defaultIntValue = defaultProperty;
        this.optionStrings = options;
        this.elementHeight += 50.0F * options.size();
        this.y_interval = 50.0F;
        this.font = FontHelper.buttonLabelFont;
    }

    public void SetValue(int value) {
        this.value = value;
        if (buttons != null) for (int i = 0; i < buttons.size(); i++)
            buttons.get(i).enabled = (i == this.value);
    }

    @Override
    public void LoadFromData(SpireConfig config) {
        value = config.getInt(settingsId);
        if (config.getBool(settingsId + RelicFilterMod.DEFAULTSETTINGSUFFIX)) value = Integer.parseInt(defaultProperty);
        if (buttons != null) for (int i = 0; i < buttons.size(); i++)
            buttons.get(i).enabled = (i == value);
    }

    @Override
    public void SaveToData(SpireConfig config) {
        config.setInt(settingsId, value);
        config.setBool(settingsId + RelicFilterMod.DEFAULTSETTINGSUFFIX, (value == Integer.parseInt(defaultProperty)));
    }

    @Override
    public void LoadFromData(Prefs config) {
        value = config.getInteger(settingsId, Integer.parseInt(defaultProperty));
        if (buttons != null) for (int i = 0; i < buttons.size(); i++)
            buttons.get(i).enabled = (i == value);
    }

    @Override
    public void SaveToData(Prefs config) {
        config.putInteger(settingsId, value);
    }

    @Override
    public ArrayList<IUIElement> GenerateElements(float x, float y) {
        buttons = new ArrayList<>();
        elements.add(new ModLabel(name, x, y, Color.WHITE, font, RelicFilterMod.settingsPanel, me -> {
        }));
        for (int i = 0; i < optionStrings.size(); i++) {
            y -= y_interval;
            elements.add(new ModLabel(optionStrings.get(i), x + 40.0F, y + 8.0F, Color.WHITE, font, RelicFilterMod.settingsPanel, me -> {
            }));
            ModToggleButton button = new ModToggleButton(x, y, (value == i), false, RelicFilterMod.settingsPanel, me -> {
                boolean nonEnabled = true;
                for (ModToggleButton b : buttons) {
                    if (b.enabled) {
                        nonEnabled = false;
                        break;
                    }
                }
                if (nonEnabled) {
                    me.enabled = true;
                } else {
                    for (int j = 0; j < buttons.size(); j++) {
                        ModToggleButton b = buttons.get(j);
                        if (b == me) {
                            value = j;
                        } else {
                            b.enabled = false;
                        }
                    }
                }
                RelicFilterMod.saveSettingsData();
            });
            buttons.add(button);
            elements.add(button);
        }
        return elements;
    }

    @Override
    public void ResetToDefault() {
        value = Integer.parseInt(defaultProperty);
        if (buttons != null) for (int i = 0; i < buttons.size(); i++)
            buttons.get(i).enabled = (i == value);
        RelicFilterMod.saveSettingsData();
    }
}

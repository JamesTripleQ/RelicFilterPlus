package relicFilter.panelUI;

import basemod.IUIElement;
import com.evacipated.cardcrawl.modthespire.lib.SpireConfig;
import com.megacrit.cardcrawl.helpers.Prefs;

import java.util.ArrayList;

public abstract class RelicSetting {
    public String settingsId;
    public String name;
    public String defaultProperty;
    public ArrayList<IUIElement> elements;
    public float elementHeight;

    public RelicSetting(String id, String name, String defaultProperty) {
        this.settingsId = id;
        this.name = name;
        this.defaultProperty = defaultProperty;
        this.elements = new ArrayList<>();
        this.elementHeight = 50.0F;
    }

    public abstract void LoadFromData(SpireConfig paramSpireConfig);

    public abstract void LoadFromData(Prefs paramPrefs);

    public abstract void SaveToData(SpireConfig paramSpireConfig);

    public abstract void SaveToData(Prefs paramPrefs);

    public abstract void ResetToDefault();

    public abstract ArrayList<IUIElement> GenerateElements(float paramFloat1, float paramFloat2);
}

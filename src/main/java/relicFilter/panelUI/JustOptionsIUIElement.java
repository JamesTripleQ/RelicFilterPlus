package relicFilter.panelUI;

import basemod.IUIElement;
import basemod.ModButton;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.List;

import relicFilter.RelicFilterMod;
import relicFilter.RelicFilterModMenu;

public class JustOptionsIUIElement implements IUIElement {
    public RelicFilterModMenu parentMenu;
    public List<IUIElement> elements;
    public List<RelicSetting> settings;
    public boolean isSelected;
    public int prevValue;
    public ModButton resetButton;

    public JustOptionsIUIElement(RelicFilterModMenu parent, List<IUIElement> elements, List<RelicSetting> settings) {
        this.parentMenu = parent;
        this.elements = elements;
        this.settings = settings;
        this.isSelected = true;
        this.prevValue = ((OptionsSetting) this.settings.get(0)).value;
        this.resetButton = new ModButton(1310.0F, 300.0F, new Texture("images/reset_all_button.png"), RelicFilterMod.settingsPanel, me -> resetAll());
    }

    public void resetAll() {
        parentMenu.resetAll();
    }

    @Override
    public void render(SpriteBatch sb) {
        if (isSelected) {
            for (IUIElement element : elements)
                element.render(sb);
            resetButton.render(sb);
        }
    }

    @Override
    public void update() {
        if (isSelected) {
            for (IUIElement element : elements)
                element.update();
            resetButton.update();
        }
        if (((OptionsSetting) settings.get(0)).value != prevValue) {
            prevValue = ((OptionsSetting) settings.get(0)).value;
            parentMenu.filterRelicList(((OptionsSetting) settings.get(0)).value);
        }
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

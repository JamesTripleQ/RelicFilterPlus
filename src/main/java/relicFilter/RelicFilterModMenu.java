package relicFilter;

import basemod.IUIElement;
import basemod.ModLabel;
import basemod.ModPanel;
import com.megacrit.cardcrawl.helpers.FontHelper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import relicFilter.helpers.Helper;
import relicFilter.panelUI.ImageButton;
import relicFilter.panelUI.JustOptionsIUIElement;
import relicFilter.panelUI.OptionsSetting;
import relicFilter.panelUI.Pagination;
import relicFilter.panelUI.RelicSetting;
import relicFilter.panelUI.RelicSettingsButton;

public class RelicFilterModMenu {
    private ImageButton right = new ImageButton("images/tinyRightArrow.png", 876, 460, 80, 80, b -> {
    });
    private ImageButton left = new ImageButton("images/tinyLeftArrow.png", 316, 460, 80, 80, b -> {
    });
    public ModPanel parentPanel;
    public JustOptionsIUIElement sortButton;
    public ArrayList<RelicSettingsButton> settingsButtons;
    public ArrayList<RelicSettingsButton> filteredButtons;
    public Pagination pager;

    public RelicFilterModMenu(ModPanel parentPanel) {
        this.parentPanel = parentPanel;
        String filterId = RelicFilterMod.DEFAULTSETTINGSUFFIX + "_filteroptions";
        ArrayList<RelicSetting> filterSettings = new ArrayList<>();
        ArrayList<IUIElement> settingElements = new ArrayList<>();
        List<String> filterStrings = new ArrayList<>();
        filterStrings.add("Starter");
        filterStrings.add("Common");
        filterStrings.add("Uncommon");
        filterStrings.add("Rare");
        filterStrings.add("Boss");
        filterStrings.add("Special");
        filterStrings.add("Shop");
        filterStrings.add("Deprecated");
        filterStrings.add("All");
        OptionsSetting sortOptions = new OptionsSetting(filterId, "Filter Relics by Tier", 8, filterStrings);
        sortOptions.font = FontHelper.healthInfoFont;
        sortOptions.y_interval = 35.0F;
        sortOptions.elementHeight += 35.0F * filterStrings.size();
        filterSettings.add(sortOptions);
        float x = 1000.0F;
        float y = 750.0F;
        for (RelicSetting setting : filterSettings) {
            settingElements.addAll(setting.GenerateElements(x, y));
            y -= setting.elementHeight;
        }
        sortButton = new JustOptionsIUIElement(this, settingElements, filterSettings);
        parentPanel.addUIElement(sortButton);
        parentPanel.addUIElement(new ModLabel("Right-click relics to quickly remove them from the pool.", 530.0F, 820.0F, parentPanel, me -> {
        }));
    }

    public void createRelicsPagination(ArrayList<RelicSettingsButton> settingsButtons) {
        this.settingsButtons = settingsButtons;
        settingsButtons.sort(Comparator.comparing(button -> button.relic.name));
        pager = new Pagination(this, right, left, 10, 8, 60, 60, settingsButtons);
        parentPanel.addUIElement(pager);
    }

    public void displaySortOptions(boolean sort) {
        sortButton.isSelected = sort;
    }

    public void resetAll() {
        pager.resetAll();
    }

    public void filterRelicList(int relicTier) {
        filteredButtons = new ArrayList<>();
        for (RelicSettingsButton b : settingsButtons) {
            if (relicTier == 8 || b.relic.tier == Helper.IntToRelicTier(relicTier)) filteredButtons.add(b);
        }
        filteredButtons.sort(Comparator.comparing(button -> button.relic.name));
        pager.refreshElements(filteredButtons);
    }
}

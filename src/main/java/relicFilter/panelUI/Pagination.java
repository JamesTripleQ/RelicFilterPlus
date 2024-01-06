package relicFilter.panelUI;

import basemod.IUIElement;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import java.util.ArrayList;
import java.util.List;

import relicFilter.RelicFilterModMenu;

public class Pagination implements IUIElement {
    private ImageButton next;
    private ImageButton prior;
    private int page;
    private int elementsPerPage;
    public int rows;
    public int columns;
    public int width;
    public int height;
    private List<RelicSettingsButton> elements;
    public RelicFilterModMenu parent;

    public Pagination(RelicFilterModMenu parent, ImageButton next, ImageButton prior, int rows, int columns, int width, int height, List<RelicSettingsButton> elements) {
        this.parent = parent;
        this.rows = rows;
        this.columns = columns;
        this.width = width;
        this.height = height;
        this.page = 0;
        next.click = (b -> this.page++);
        prior.click = (b -> this.page--);
        this.next = next;
        this.prior = prior;
        this.elementsPerPage = rows * columns;
        refreshElements(elements);
    }

    public void refreshElements(List<RelicSettingsButton> elements) {
        this.elements = new ArrayList<>();
        page = 0;
        for (int i = 0; i < elements.size(); i++) {
            RelicSettingsButton newElement, element = elements.get(i);
            if (element.relic != null) {
                newElement = new RelicSettingsButton(this, element.relic, element.x + (width * (i % columns)), element.y - ((float) (height * (i % elementsPerPage - i % columns)) / columns), width, height, element.elements);
            } else {
                newElement = new RelicSettingsButton(element.image, element.outline, element.x + (width * (i % columns)), element.y - ((float) (height * (i % elementsPerPage - i % columns)) / columns), width, height, element.elements);
            }
            newElement.settings = element.settings;
            newElement.lastValue = ((OptionsSetting) newElement.settings.get(0)).value;
            this.elements.add(newElement);
        }
    }

    @Override
    public void render(SpriteBatch spriteBatch) {
        if (page != 0) prior.render(spriteBatch);
        if ((page + 1) * elementsPerPage < elements.size()) next.render(spriteBatch);
        for (RelicSettingsButton element : elements.subList(page * elementsPerPage, Math.min((page + 1) * elementsPerPage, elements.size())))
            element.render(spriteBatch);
    }

    @Override
    public void update() {
        if (page != 0) prior.update();
        if ((page + 1) * elementsPerPage < elements.size()) next.update();
        boolean isRelicSelected = false;
        for (RelicSettingsButton element : elements.subList(page * elementsPerPage, Math.min((page + 1) * elementsPerPage, elements.size()))) {
            element.update();
            if (element.isSelected) isRelicSelected = true;
        }
        parent.displaySortOptions(!isRelicSelected);
    }

    public void resetAll() {
        for (RelicSettingsButton b : elements)
            b.resetAll();
    }

    public void displaySortOptions(boolean sort) {
        parent.displaySortOptions(sort);
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

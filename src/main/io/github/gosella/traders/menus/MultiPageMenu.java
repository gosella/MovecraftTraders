package io.github.gosella.traders.menus;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.ArrayList;
import java.util.List;

public class MultiPageMenu extends Menu {
    private final List<SinglePageMenu> pages;
    private final RadioBoxMenuItem changer;
    private SinglePageMenu currentPage;

    public MultiPageMenu(String title, int rows) {
        super(title, rows);
        pages = new ArrayList<>();
        changer = new RadioBoxMenuItem();
    }

    public SinglePageMenu addPage(ToggleMenuItem button) {
        final SinglePageMenu newPage = new SinglePageMenu(getInventory());
        changer.add(button);
        newPage.add(changer);
        pages.add(newPage);
        if (currentPage == null) {
            currentPage = newPage;
            button.setSelected(true);
        } else {
            button.setSelected(false);
        }
        button.setToggleListener((player, event, selected) -> {
            if (selected) {
                setCurrentPage(newPage);
            }
            return true;
        });
        return newPage;
    }

    public SinglePageMenu getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(SinglePageMenu currentPage) {
        if (this.currentPage != currentPage) {
            int i = pages.indexOf(currentPage);
            if (i != -1) {
                this.currentPage = currentPage;
                changer.select(changer.get(i));
                currentPage.setup();
            }
        }
    }

    @Override
    public void setup() {
        if (currentPage != null) {
            currentPage.setup();
        }
    }

    @Override
    public void update() {
        if (currentPage != null) {
            currentPage.update();
        }
    }

    @Override
    public boolean process(Player player, InventoryClickEvent event) {
        return currentPage == null && super.process(player, event) || currentPage.process(player, event);
    }
}

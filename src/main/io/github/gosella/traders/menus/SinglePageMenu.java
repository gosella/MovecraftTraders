package io.github.gosella.traders.menus;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

public class SinglePageMenu extends Menu {
    private final List<MenuItem> items;

    public SinglePageMenu(String title, int rows) {
        super(title, rows);
        this.items = new ArrayList<>();
    }

    SinglePageMenu(Inventory inventory) {
        super(inventory);
        this.items = new ArrayList<>();
    }

    public List<MenuItem> getItems() {
        return items;
    }

    public void add(MenuItem menuItem) {
        items.add(menuItem);
    }

    @Override
    public void setup() {
        final Inventory inventory = getInventory();
        inventory.clear();
        for (MenuItem item : items) {
            item.setup(inventory);
        }
    }

    @Override
    public void update() {
        for (MenuItem item : items) {
            item.update();
        }
    }

    @Override
    public boolean process(Player player, InventoryClickEvent event) {
        if (super.process(player, event)) {
            return true;
        }
        for (MenuItem item : items) {
            if (item.process(player, event)) {
                return true;
            }
        }
        return false;
    }
}

package io.github.gosella.traders.menus.items;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.List;

public class RadioBoxMenuItem implements MenuItem {
    private List<ToggleMenuItem> items;
    private ToggleMenuItem currentItem;
    private RadioBoxChangeListener changeListener;

    public interface RadioBoxChangeListener {
        boolean onChange(Player player, InventoryClickEvent event, ToggleMenuItem current, ToggleMenuItem previous);
    }

    public RadioBoxMenuItem(ToggleMenuItem... items) {
        this.items = new ArrayList<>(items.length);
        for (ToggleMenuItem item : items) {
            add(item);
        }
    }

    public int size() {
        return items.size();
    }

    public void add(ToggleMenuItem item) {
        items.add(item);
        if (currentItem == null) {
            currentItem = item;
            item.setSelected(true);
        } else {
            item.setSelected(false);
        }
    }

    public ToggleMenuItem get(int index) {
        return items.get(index);
    }

    public void select(ToggleMenuItem item) {
        if (item == currentItem) {
            return;
        }
        if (currentItem != null) {
            this.currentItem.setSelected(false);
            this.currentItem = null;
        }
        item.setSelected(true);
        this.currentItem = item;
    }

    public void setChangeListener(RadioBoxChangeListener changeListener) {
        this.changeListener = changeListener;
    }

    @Override
    public void setup(Inventory inventory) {
        for (ToggleMenuItem item : items) {
            item.setup(inventory);
        }
    }

    @Override
    public void update() {
        for (ToggleMenuItem item : items) {
            item.update();
        }
    }

    @Override
    public boolean process(Player player, InventoryClickEvent event) {
        final int slot = event.getSlot();
        for (ToggleMenuItem item : items) {
            if (item.getPosition() == slot) {
                if (item != currentItem) {
                    ToggleMenuItem previous = currentItem;
                    if (item.process(player, event)) {
                        currentItem.setSelected(false);
                        currentItem = item;
                        item.setSelected(true);
                        return changeListener == null || changeListener.onChange(player, event, item, previous);
                    }
                }
                break;
            }
        }
        return false;
    }
}

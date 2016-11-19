package io.github.gosella.traders.menus.items;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ToggleMenuItem extends ClickableMenuItem {
    final private ItemStack itemSelected;
    final private ItemStack itemUnselected;
    private boolean selected;

    private ToggleListener toggleListener;

    public interface ToggleListener {
        boolean onToggle(Player player, InventoryClickEvent event, boolean selected);
    }

    public ToggleMenuItem(int position, ItemStack itemSelected, ItemStack itemUnselected, boolean selected) {
        super(position, selected ? itemSelected : itemUnselected);
        this.selected = selected;
        this.itemSelected = itemSelected;
        this.itemUnselected = itemUnselected;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        if (this.selected != selected) {
            this.selected = selected;
            update();
        }
    }

    public void toggle() {
        selected = !selected;
        update();
    }

    public void setToggleListener(ToggleListener toggleListener) {
        this.toggleListener = toggleListener;
    }

    @Override
    public void update() {
        setItem(selected ? itemSelected : itemUnselected);
        super.update();
    }

    @Override
    public boolean process(Player player, InventoryClickEvent event) {
        if (super.process(player, event)) {
            toggle();
            return toggleListener == null || toggleListener.onToggle(player, event, selected);
        }
        return false;
    }
}

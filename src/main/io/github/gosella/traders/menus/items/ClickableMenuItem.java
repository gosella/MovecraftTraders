package io.github.gosella.traders.menus.items;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class ClickableMenuItem extends StaticMenuItem {
    private ClickListener clickListener;

    public interface ClickListener {
        boolean onClick(Player player, InventoryClickEvent event);
    }

    public ClickableMenuItem(int position, ItemStack item) {
        super(position, item);
    }

    public ClickableMenuItem(int position, String title, Material type) {
        super(position, title, type);
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public boolean process(Player player, InventoryClickEvent event) {
        return getPosition() == event.getSlot() && (clickListener == null || clickListener.onClick(player, event));
    }
}

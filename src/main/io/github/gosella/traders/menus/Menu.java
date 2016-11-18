package io.github.gosella.traders.menus;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public abstract class Menu {
    private final Inventory inventory;
    private ClickListener clickListener;

    public interface ClickListener {
        boolean onClick(Player player, InventoryClickEvent event);
    }

    public Menu(String title, int rows) {
        this(Bukkit.createInventory(null, rows * 9, title));
    }

    Menu(Inventory inventory) {
        this.inventory = inventory;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public int getRows() {
        return inventory.getSize() / 9;
    }

    public int getSize() {
        return inventory.getSize();
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    abstract void setup();

    abstract void update();

    boolean process(Player player, InventoryClickEvent event) {
        return clickListener != null && event.getRawSlot() >= inventory.getSize() && clickListener.onClick(player, event);
    }
}

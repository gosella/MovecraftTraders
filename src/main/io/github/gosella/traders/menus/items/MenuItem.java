package io.github.gosella.traders.menus.items;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public interface MenuItem {
    void setup(Inventory inventory);

    void update();

    boolean process(Player player, InventoryClickEvent event);
}

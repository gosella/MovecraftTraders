package io.github.gosella.traders.menus.items;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class StaticMenuItem implements MenuItem {
    private Inventory inventory;
    private int position;
    private ItemStack item;

    public StaticMenuItem(int position, ItemStack item) {
        this.position = position;
        this.item = item;
    }

    public StaticMenuItem(int position, String title, Material type) {
        this.position = position;
        this.item = new ItemStack(type, 1);
        ItemMeta meta = this.item.getItemMeta();
        meta.setDisplayName(title);
        this.item.setItemMeta(meta);
    }

    public int getPosition() {
        return position;
    }

    public Inventory getInventory() {
        return inventory;
    }

    public ItemStack getItem() {
        return item;
    }

    protected void setItem(ItemStack item) {
        this.item = item;
    }

    public void setup(Inventory inventory) {
        this.inventory = inventory;
        update();
    }

    public void update() {
        if (inventory != null) {
            inventory.setItem(position, item);
        }
    }

    public boolean process(Player player, InventoryClickEvent event) {
        return false;
    }
}

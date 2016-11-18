package io.github.gosella.traders.menus;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ClickableMenuItem implements MenuItem {
    private Inventory inventory;
    private int position;
    private ItemStack item;
    private ClickListener clickListener;

    public interface ClickListener {
        boolean onClick(Player player, InventoryClickEvent event);
    }

    public ClickableMenuItem(int position, ItemStack item) {
        this.position = position;
        this.item = item;
    }

    public ClickableMenuItem(int position, String title, Material type) {
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

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
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
        return position == event.getSlot() && (clickListener == null || clickListener.onClick(player, event));
    }
}

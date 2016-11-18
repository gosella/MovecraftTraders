package io.github.gosella.traders;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class MerchantItem extends ItemStack {
    public MerchantItem(Material type) {
        this(type, 0.0);
    }

    public MerchantItem(Material type, int amount) {
        this(type, amount, 0.0);
    }

    public MerchantItem(ItemStack stack) throws IllegalArgumentException {
        this(stack, 0.0);
    }

    public MerchantItem(Material type, double price) {
        super(type, 1);
        this.price = price;
    }

    public MerchantItem(Material type, int amount, double price) {
        super(type, amount);
        this.price = price;
    }

    public MerchantItem(ItemStack stack, double price) throws IllegalArgumentException {
        super(stack);
        this.price = price;
    }

    private double price;

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;

        ItemMeta meta = getItemMeta();
        List<String> lore = meta.getLore();
        if (lore == null) {
            lore = new ArrayList<>(1);
        }
        String s = ChatColor.GOLD + "Price: $" + price;  // TODO: Format the price nicely. Refactor string. Probably the wrong place to do this...
        if (lore.size() == 0 || !lore.get(0).startsWith(ChatColor.GOLD + "Price: $")) {
            lore.add(0, s);
        } else {
            lore.set(0, s);
        }
        meta.setLore(lore);
        setItemMeta(meta);
    }

    public String getName() {
        return getItemMeta().getDisplayName();
    }
}

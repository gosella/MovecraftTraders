package io.github.gosella.traders;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class Merchant {
    private String name;
    private List<MerchantItem> items;

    public Merchant(String name) {
        this.name = name;
        this.items = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<MerchantItem> getItems() {
        return items;
    }

    public void addItem(MerchantItem item) {
        items.add(item);
    }

    public boolean hasItem(ItemStack item) {
        return findItem(item) != null;
    }

    public MerchantItem findItem(ItemStack item) {
        if (item != null) {
            for (MerchantItem otherItem : items) {
                if (otherItem.getType() == item.getType() && otherItem.getDurability() == item.getDurability()) {
                    // TODO: Add better comparison (taking enchantments, name, etc, into account)
                    return otherItem;
                }
            }
        }
        return null;
    }
}

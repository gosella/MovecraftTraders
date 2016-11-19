package io.github.gosella.traders.menus.items;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class UpDownMenuItem extends ClickableMenuItem {
    private int min;
    private int max;

    private UpDownListener upDownListener;

    public interface UpDownListener {
        boolean onUpDown(Player player, InventoryClickEvent event, int value);
    }

    public UpDownMenuItem(int position, ItemStack item) {
        super(position, item);
        this.min = 0;
        this.max = item.getType().getMaxStackSize();
        this.getItem().setAmount(1);
    }

    public UpDownMenuItem(int position, String title, Material type, int value) {
        super(position, title, type);
        this.min = 0;
        this.max = type.getMaxStackSize();
        this.getItem().setAmount(value);
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
        final ItemStack item = getItem();
        if (item.getAmount() < min) {
            item.setAmount(min);
        }
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
        final ItemStack item = getItem();
        if (item.getAmount() > max) {
            item.setAmount(max);
        }
    }

    public int getValue() {
        return getItem().getAmount();
    }

    public void setValue(int value) {
        getItem().setAmount(value);
        update();
    }

    public void setUpDownListener(UpDownListener upDownListener) {
        this.upDownListener = upDownListener;
    }

    @Override
    public boolean process(Player player, InventoryClickEvent event) {
        if (getPosition() != event.getSlot()) {
            return false;
        }
        final ItemStack item = getItem();
        int amount = item.getAmount();
        if (event.isLeftClick()) {
            if (amount < max) {
                item.setAmount(++amount);
                update();
            }
        } else if (event.isRightClick()) {
            if (amount > min) {
                item.setAmount(--amount);
                update();
            }
        }
        return upDownListener == null || upDownListener.onUpDown(player, event, amount);
    }
}

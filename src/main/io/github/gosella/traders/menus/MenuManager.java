package io.github.gosella.traders.menus;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MenuManager {
    private Map<Player, Menu> menus;

    public MenuManager(JavaPlugin plugin) {
        this.menus = new ConcurrentHashMap<>();
        Bukkit.getPluginManager().registerEvents(new MenuManagerListener(), plugin);
    }

    public void openMenuFor(Player player, Menu menu) {
        menu.setup();
        menus.put(player, menu);
        player.openInventory(menu.getInventory());
    }

    public void closeMenuFor(Player player) {
        player.closeInventory();
        menus.remove(player);
    }

    public void closeMenus() {
        for (Iterator<Player> iterator = menus.keySet().iterator(); iterator.hasNext(); ) {
            Player player = iterator.next();
            iterator.remove();
            player.closeInventory();
        }
    }

    private class MenuManagerListener implements Listener {
        @EventHandler(priority = EventPriority.HIGHEST)
        public void onInventoryClick(InventoryClickEvent event) {
            final Player player = (Player) event.getWhoClicked();
            final Menu menu = menus.get(player);
            if (menu != null) {
                event.setCancelled(true);
                menu.process(player, event);
            }
        }

        @EventHandler(priority = EventPriority.HIGHEST)
        public void onInventoryClose(InventoryCloseEvent event) {
            final Player player = (Player) event.getPlayer();
            menus.remove(player);
        }

        @EventHandler
        public void onPlayerQuit(PlayerQuitEvent event) {
            menus.remove(event.getPlayer());
        }
    }
}

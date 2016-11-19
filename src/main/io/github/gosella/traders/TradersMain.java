package io.github.gosella.traders;

import io.github.gosella.traders.menus.Menu;
import io.github.gosella.traders.menus.MenuManager;
import io.github.gosella.traders.menus.MultiPageMenu;
import io.github.gosella.traders.menus.SinglePageMenu;
import io.github.gosella.traders.menus.items.*;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class TradersMain extends JavaPlugin {
    private Map<String, Merchant> merchants;
    private MenuManager menuManager;
    private double balance = 10000.0;  // TODO: Remove this!

    @Override
    public void onEnable() {
        menuManager = new MenuManager(this);
        merchants = new HashMap<>();
        loadMerchants();
    }

    @Override
    public void onDisable() {
        menuManager.closeMenus();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!command.getName().equalsIgnoreCase("merchant") || args.length < 1) {
            return false;
        }

        String merchantName = String.join(" ", args).toLowerCase();
        if (merchantName.equals("reload")) {
            if (loadMerchants()) {
                sender.sendMessage(ChatColor.GREEN + "Merchants spreadsheet successfully loaded!");
            } else {
                sender.sendMessage(ChatColor.RED + "There was a problem loading the Merchants spreadsheet.");
            }
            return true;
        }

        if (!(sender instanceof Player) ) {
            return false;
        }
        Player player = (Player) sender;
        Merchant merchant = merchants.get(merchantName);
        if (merchant == null) {
            player.sendMessage(ChatColor.RED + "Merchant for " + merchantName + " not found!");
            return false;
        }

        player.sendMessage(ChatColor.GREEN + "Accessing " + merchantName + " Merchant.");
        menuManager.openMenuFor(player, buildMenuFor(merchant));
        return true;
    }

    private boolean loadMerchants() {
        merchants.clear();

        // Open the spreadsheet

        File workbookFile = new File(this.getDataFolder() + "/Merchants.xls");

        FileInputStream inputStream;
        try {
            inputStream = new FileInputStream(workbookFile);
        } catch (FileNotFoundException e) {
            getLogger().warning("Merchants spreadsheet not found!");
            return false;
        }

        final Workbook workbook;
        try {
            workbook = new HSSFWorkbook(inputStream);
        } catch (IOException e) {
            getLogger().warning("Merchants spreadsheet couldn't be read!");
            return false;
        }

        // Process the "Items" sheet

        final Sheet itemsSheet = workbook.getSheet("Items");
        if (itemsSheet == null) {
            getLogger().warning("Items sheet is missing!");
            return false;
        }

        Iterator<Row> rowIterator = itemsSheet.iterator();
        if (!rowIterator.hasNext()) {
            getLogger().warning("Items sheet is empty!");
            return false;
        }

        getLogger().info("Processing Items:");

        Row header = rowIterator.next();  // TODO: Validate header? Make it adaptable? For now, it's just ignored.

        Map<String, ItemStack> items = new HashMap<>();
        while (rowIterator.hasNext()) {
            final Row itemRow = rowIterator.next();
            final String itemName = itemRow.getCell(0).getStringCellValue().trim();
            final String materialID = itemRow.getCell(1).getStringCellValue();
            final String lore = itemRow.getCell(2).getStringCellValue();
            final Material material = Material.matchMaterial(materialID);
            if (material == null) {
                getLogger().warning("Item \"" + itemName + "\" has an unknown material: \"" + materialID + "\"");
                continue;
            }
            MerchantItem item = new MerchantItem(material, 1);
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(ChatColor.BOLD + "" + ChatColor.WHITE + itemName);
            if (!lore.trim().isEmpty()) {
                meta.setLore(Arrays.asList(lore.split("\n")));
            }
            meta.addEnchant(Enchantment.DURABILITY, 400, false); // TODO: Improve this to use the info on the sheet!
            item.setItemMeta(meta);
            items.put(itemName.toLowerCase(), item);
            getLogger().info("Found item: \"" + itemName + "\"");
        }

        // Process the "Prices" sheet

        getLogger().info("Processing Merchants & Prices:");

        final Sheet pricesSheet = workbook.getSheet("Prices");
        if (pricesSheet == null) {
            getLogger().warning("Prices sheet is missing!");
            return false;
        }

        rowIterator = pricesSheet.iterator();
        if (!rowIterator.hasNext()) {
            getLogger().warning("Prices sheet is empty!");
            return false;
        }

        header = rowIterator.next();
        List<ItemStack> merchantItems = new ArrayList<>(header.getLastCellNum() - header.getFirstCellNum());

        // Skips the first cell of the first row (i.e., $A$1)
        for(int i = header.getFirstCellNum() + 1; i <= header.getLastCellNum(); ++i) {
            final Cell cell = header.getCell(i);
            if (cell == null) {
                continue;
            }
            final String itemName = cell.getStringCellValue().trim();
            final ItemStack itemStack = items.get(itemName.toLowerCase());
            if (itemStack == null) {
                getLogger().warning("Found an unknown item: \"" + itemName + "\"");
                return false;
            }
            merchantItems.add(itemStack);
        }


        // Creates the merchants adding items with its prices.
        while (rowIterator.hasNext()) {
            Row merchantInfo = rowIterator.next();
            final String merchantName = merchantInfo.getCell(0).getStringCellValue().trim();
            final Merchant merchant = new Merchant(merchantName);
            getLogger().info("Found Merchant: \"" + merchantName + "\"");

            Iterator<ItemStack> itemIterator = merchantItems.iterator();
            for(int i = merchantInfo.getFirstCellNum() + 1; i <= merchantInfo.getLastCellNum(); ++i) {
                if (!itemIterator.hasNext()) {
                    break;
                }
                MerchantItem item = new MerchantItem(itemIterator.next());
                Cell cell = merchantInfo.getCell(i);
                if (cell.getCellTypeEnum() == CellType.NUMERIC) {
                    final double price = cell.getNumericCellValue();
                    item.setPrice(price);
                    merchant.addItem(item);
                    getLogger().info("Add item \"" + item.getName() + "\" to Merchant \"" + merchantName + "\"");
                } else if (cell.getCellTypeEnum() != CellType.BLANK) {
                    getLogger().warning("Found a non-numeric price for " + item.getName() + " of " + merchantName);
                }
            }
            merchants.put(merchantName.toLowerCase(), merchant);
        }
        getLogger().info("Merchants spreadsheet successfully loaded!");
        return true;
    }


    private Menu buildMenuFor(Merchant merchant) {
        MultiPageMenu multiPageMenu = new MultiPageMenu(merchant.getName() + " menu", 6);

        SinglePageMenu menu;
        ItemStack selected, unselected;
        ItemMeta meta;

        selected = new ItemStack(Material.SLIME_BALL);
        meta = selected.getItemMeta();
        meta.setDisplayName("Using Player's Inventory");
        selected.setItemMeta(meta);
        unselected = new ItemStack(selected);
        unselected.setType(Material.SNOW_BALL);
        menu = multiPageMenu.addPage(new ToggleMenuItem(6 * 9 - 1, selected, unselected, true));

        //   Buy/sell modes: One item/Full stack/Complete inventory

        selected = new ItemStack(Material.SLIME_BALL);
        meta = selected.getItemMeta();
        meta.setDisplayName("Complete Inventory mode");
        selected.setItemMeta(meta);
        unselected = new ItemStack(selected);
        unselected.setType(Material.SNOW_BALL);
        ToggleMenuItem fillInventoryOption = new ToggleMenuItem(5 * 9, selected, unselected, true);


        selected = new ItemStack(Material.SLIME_BALL);
        meta = selected.getItemMeta();
        meta.setDisplayName("Full Stacks mode");
        selected.setItemMeta(meta);
        unselected = new ItemStack(selected);
        unselected.setType(Material.SNOW_BALL);
        ToggleMenuItem fullStackOption = new ToggleMenuItem(5 * 9 + 1, selected, unselected, true);


        selected = new ItemStack(Material.SLIME_BALL);
        meta = selected.getItemMeta();
        meta.setDisplayName("One item mode");
        selected.setItemMeta(meta);
        unselected = new ItemStack(selected);
        unselected.setType(Material.SNOW_BALL);
        ToggleMenuItem oneItemOption = new ToggleMenuItem(5 * 9 + 2, selected, unselected, true);


        ClickableMenuItem[] items = new ClickableMenuItem[merchant.getItems().size()];


        RadioBoxMenuItem modes = new RadioBoxMenuItem(fillInventoryOption, fullStackOption, oneItemOption);
        modes.setChangeListener((player, event, current, previous) -> {
            boolean fullStacks = current == fullStackOption;
            for (ClickableMenuItem menuItem : items) {
                ItemStack item = menuItem.getItem();
                item.setAmount(fullStacks ? item.getType().getMaxStackSize() : 1);
                menuItem.update();
            }
            return true;
        });
        menu.add(modes);

        //   Process clicks on the Player's inventory (to sell'em to the Merchant)

        menu.setClickListener(((player, event) -> {
            ItemStack item = event.getCurrentItem();
            final MerchantItem selectedItem = merchant.findItem(item);
            if (selectedItem != null) {
                final Inventory playerInventory = player.getInventory();
                final int inventorySize = playerInventory.getSize();
                final double price = selectedItem.getPrice();
                int totalAmount = 0;
                int slot = event.getSlot();
                int nextSlot = slot == 0 ? 1 : 0;
                if (fillInventoryOption.isSelected()) {
                    do {
                        final int amount = item.getAmount();
                        final double charge = price * amount;
                        totalAmount += amount;
                        balance += charge;  // TODO: Change for the real thing
                        playerInventory.clear(slot);
                        player.sendMessage(ChatColor.ITALIC + "Balance += $" + charge);  // TODO: Remove this!
                        for(slot = nextSlot; slot < inventorySize; ++slot) {
                            final ItemStack newItem = playerInventory.getItem(slot);
                            if (newItem != null && newItem.isSimilar(item)) {
                                item = newItem;
                                nextSlot = slot + 1;
                                break;
                            }
                        }
                    } while (slot < inventorySize);
                } else if (oneItemOption.isSelected() || fullStackOption.isSelected()) {
                    item = playerInventory.getItem(slot);
                    final int amount = oneItemOption.isSelected() ? 1 : event.isRightClick() ? Math.max(1, item.getAmount() / 2) : item.getAmount();
                    final double charge = price * amount;
                    totalAmount += amount;
                    balance += charge;  // TODO: Change for the real thing
                    final int newAmount = item.getAmount() - amount;
                    if (newAmount != 0) {
                        item.setAmount(newAmount);
                    } else {
                        playerInventory.clear(slot);
                    }
                    player.sendMessage(ChatColor.ITALIC + "Balance += $" + charge);  // TODO: Remove this!
                }
                player.sendMessage("Sold " + totalAmount + " items of " + selectedItem.getName() + " for " + (totalAmount * price));
            }
            return true;
        }));


        //   Add the items the Merchant sells.

        int position = 0;
        for (MerchantItem merchantItem : merchant.getItems()) {
            ClickableMenuItem menuItem = new ClickableMenuItem(position, merchantItem.clone());

            menuItem.setClickListener((player, event) -> {
                final ItemStack item = event.getCurrentItem();
                final MerchantItem selectedItem = merchant.findItem(item);
                final Inventory playerInventory = player.getInventory();
                final boolean fillInventory = fillInventoryOption.isSelected();
                final double price = selectedItem.getPrice();
                int amount = fillInventory ? item.getMaxStackSize() : event.isRightClick() ? Math.max(1, item.getAmount() / 2) : item.getAmount();
                int totalAmount = 0;
                HashMap<Integer, ItemStack> leftovers;
                do {
                    if (price * amount > balance) {
                        amount = (int)(balance / price);
                        if (amount == 0) {
                            amount = -1;
                            break;
                        }
                    }
                    ItemStack itemToAdd = new ItemStack(selectedItem);
                    itemToAdd.setAmount(amount);
                    leftovers = playerInventory.addItem(itemToAdd);
                    if (!leftovers.isEmpty()) {
                        amount -= leftovers.get(0).getAmount();
                    }
                    if (amount > 0) {
                        double charge = price * amount;
                        balance -= charge;  // TODO: Change for the real thing
                        player.sendMessage(ChatColor.ITALIC + "Balance -= $" + charge);  // TODO: Remove this!
                    }
                    totalAmount += amount;
                } while (fillInventory && leftovers.isEmpty());

                if (totalAmount > 0) {
                    player.sendMessage("Added " + totalAmount + " item" +
                            (totalAmount != 1 ? "s" : "") + " of " + selectedItem.getName() +
                            " to the Player's inventory for " + (price * totalAmount));
                } else if (amount == -1) {
                    player.sendMessage(ChatColor.RED + "Not enough funding! You currently have $" + balance);
                } else {
                    player.sendMessage(ChatColor.RED + "Not enough inventory space!");
                }
                return true;
            });

            items[position++] = menuItem;
            menu.add(menuItem);
        }

        selected = new ItemStack(Material.SLIME_BALL);
        meta = selected.getItemMeta();
        meta.setDisplayName("Cargo Load/Unload");
        selected.setItemMeta(meta);
        unselected = new ItemStack(selected);
        unselected.setType(Material.SNOW_BALL);
        menu = multiPageMenu.addPage(new ToggleMenuItem(6 * 9 - 2, selected, unselected, true));

        ///// PROOF of CONCEPT  /////

        UpDownMenuItem upDownChests;
        UpDownMenuItem upDownDispensers;
        List<String> lore = Arrays.asList(ChatColor.RESET + "Left click is +1", ChatColor.RESET + "Right click is -1");
        ItemStack upDownTestItem;
        ItemStack itemStack;
        ItemMeta upDownTestItemItemMeta;


        position = 4 * 9;
        itemStack = merchant.getItems().get(0).clone();
        upDownTestItemItemMeta = itemStack.getItemMeta();
        upDownTestItemItemMeta.setDisplayName("Item to UNLOAD");
        itemStack.setItemMeta(upDownTestItemItemMeta);
        StaticMenuItem itemUnload = new StaticMenuItem(position++, itemStack);
        menu.add(itemUnload);

        upDownChests = new UpDownMenuItem(position++, "Chests to UNLOAD", Material.CHEST, 7);
        upDownTestItem = upDownChests.getItem();
        upDownTestItemItemMeta = upDownTestItem.getItemMeta();
        upDownTestItemItemMeta.setLore(lore);
        upDownTestItem.setItemMeta(upDownTestItemItemMeta);
        upDownChests.setUpDownListener((player, event, value) -> {
            player.sendMessage(ChatColor.BOLD + "Chests: [" + ChatColor.GOLD + value + ChatColor.RESET + "]");
            return true;
        });
        menu.add(upDownChests);

        upDownDispensers = new UpDownMenuItem(position, "Dispensers to UNLOAD", Material.DISPENSER, 2);
        upDownTestItem = upDownDispensers.getItem();
        upDownTestItemItemMeta = upDownTestItem.getItemMeta();
        upDownTestItemItemMeta.setLore(lore);
        upDownTestItem.setItemMeta(upDownTestItemItemMeta);
        upDownDispensers.setUpDownListener((player, event, value) -> {
            player.sendMessage(ChatColor.BOLD + "Dispensers: [" + ChatColor.GOLD + value + ChatColor.RESET + "]");
            return true;
        });
        menu.add(upDownDispensers);



        position = 4 * 9 + 6;
        itemStack = new ItemStack(Material.SNOW);
        upDownTestItemItemMeta = itemStack.getItemMeta();
        upDownTestItemItemMeta.setDisplayName("Item to LOAD");
        itemStack.setItemMeta(upDownTestItemItemMeta);
        StaticMenuItem itemLoad = new StaticMenuItem(position++, itemStack);
        menu.add(itemLoad);

        upDownChests = new UpDownMenuItem(position++, "Chests to LOAD", Material.CHEST, 7);
        upDownTestItem = upDownChests.getItem();
        upDownTestItemItemMeta = upDownTestItem.getItemMeta();
        upDownTestItemItemMeta.setLore(lore);
        upDownTestItem.setItemMeta(upDownTestItemItemMeta);
        upDownChests.setUpDownListener((player, event, value) -> {
            player.sendMessage(ChatColor.BOLD + "Chests: [" + ChatColor.GOLD + value + ChatColor.RESET + "]");
            return true;
        });
        menu.add(upDownChests);

        upDownDispensers = new UpDownMenuItem(position, "Dispensers to LOAD", Material.DISPENSER, 2);
        upDownTestItem = upDownDispensers.getItem();
        upDownTestItemItemMeta = upDownTestItem.getItemMeta();
        upDownTestItemItemMeta.setLore(lore);
        upDownTestItem.setItemMeta(upDownTestItemItemMeta);
        upDownDispensers.setUpDownListener((player, event, value) -> {
            player.sendMessage(ChatColor.BOLD + "Dispensers: [" + ChatColor.GOLD + value + ChatColor.RESET + "]");
            return true;
        });
        menu.add(upDownDispensers);



        position = 0;
        for (MerchantItem merchantItem : merchant.getItems()) {
            ClickableMenuItem menuItem = new ClickableMenuItem(position++, merchantItem.clone());
            menuItem.setClickListener((player, event) -> {
                ItemStack item = itemLoad.getItem();
                ItemStack currentItem = event.getCurrentItem();
                if (item.getType() != currentItem.getType()) {
                    item.setType(currentItem.getType());
                    itemLoad.update();
                    player.sendMessage(ChatColor.YELLOW + "Recalculating chests...");
                }
                return true;
            });
            menu.add(menuItem);
        }



        ClickableMenuItem buttonProceed = new ClickableMenuItem(5 * 9, "Proceed!", Material.WOOL);
        buttonProceed.setClickListener((player, event) -> {
            player.sendMessage(ChatColor.BOLD + "Unloading: " + itemUnload.getItem().getType() + " / Loading: " + itemLoad.getItem().getType());
            player.sendMessage(ChatColor.RED + "Proceeding... not!");
            player.closeInventory();
            return true;
        });
        menu.add(buttonProceed);


        ///// END of PROOF of CONCEPT  /////


        //// JUST TESTING... ////
/*
        ClickableMenuItem buttonTest = new ClickableMenuItem(5 * 9 + 2, "Click Me!", Material.EMERALD);
        buttonTest.setClickListener((player, event) -> {
            player.sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "Hi!");
            return true;
        });
        menu.add(buttonTest);

        selected = new ItemStack(Material.DIAMOND);
        meta = selected.getItemMeta();
        meta.setDisplayName("Hello!");
        selected.setItemMeta(meta);

        unselected = new ItemStack(Material.EMERALD);
        meta = unselected.getItemMeta();
        meta.setDisplayName("Bye!");
        unselected.setItemMeta(meta);

        ToggleMenuItem toggleTest = new ToggleMenuItem(5 * 9 + 3, selected, unselected, true);
        buttonTest.setClickListener((player, event) -> {
            player.sendMessage(ChatColor.BOLD + "" + ChatColor.GOLD + "Hi!");
            return true;
        });
        menu.add(toggleTest);

        UpDownMenuItem upDownTest = new UpDownMenuItem(5 * 9 + 4, "Try Me!", Material.CHEST, 7);
        upDownTestItem = upDownTest.getItem();
        upDownTestItemItemMeta = upDownTestItem.getItemMeta();
        upDownTestItemItemMeta.setLore(lore);
        upDownTestItem.setItemMeta(upDownTestItemItemMeta);
        upDownTest.setUpDownListener((player, event, value) -> {
            player.sendMessage(ChatColor.BOLD + "Chests: [" + ChatColor.GOLD + value + ChatColor.RESET + "]");
            return true;
        });
        menu.add(upDownTest);

        upDownTest = new UpDownMenuItem(5 * 9 + 5, "Try Me TOO!", Material.DISPENSER, 2);
        upDownTestItem = upDownTest.getItem();
        upDownTestItemItemMeta = upDownTestItem.getItemMeta();
        upDownTestItemItemMeta.setLore(lore);
        upDownTestItem.setItemMeta(upDownTestItemItemMeta);
        upDownTest.setUpDownListener((player, event, value) -> {
            player.sendMessage(ChatColor.BOLD + "Dispensers: [" + ChatColor.GOLD + value + ChatColor.RESET + "]");
            return true;
        });
        menu.add(upDownTest);
*/
        //// END OF TESTS ////

        return multiPageMenu;
    }
}

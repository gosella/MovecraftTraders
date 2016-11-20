# Movecraft Traders

**This is work in progress**.

The command `/merchant [merchant name]` opens a menu to interact with a merchant. In the future, this action will be triggered by interacting with an NPC.

In the 'Player Inventory' mode, a click on a merchant's item can mean three different things according to the selector on the lower left part of the GUI:

- Fill the players inventory with as many items as he can buy.
- Buy a full stack of that item.
- Buy a single item.

Something similar occurs with the items on the Player's inventory: Sell all the items similar to the one clicked, sell the complete stack or just sell one item.

The merchants names and the items that it sells are defined in an Excel spreadsheet located at `MovecraftTraders/Merchants.xls` inside the `plugins` directory.

It contains two sheets: One for defining the "Items" (name, material, appearance, etc.) and one for defining the "Prices" of each item in every Merchant shop. Missing values in the later means that the item is not available on that shop. Right now, it assumes that every item is sold at the same price as is bought but the Prices sheet can be extended to include both prices.  



After editing the spreadsheet, the command `/merchant reload` reloads the info and updates the merchants without restating the server.

## Known Issues:

- The `TradersMain` class could use some refactoring.
- The plugin uses Apache POI to read the spreadsheet, ~~which I tried to shade into the plugin JAR but throws an Exception when the server reloads the plugins. Couldn't figure it out how to solve it~~. **THIS IS FIXED** changing the spreadsheet format from XLSX to XLS, reducing considerable the size of the JAR.
- The Autoload/unload interface ~~is almost not existent~~ has a non-functional prototype as a proof of concept for the UX.
- Still not integrated with Vault/Economy (but can be done in a couple of minutes).
- Needs to interact with the Citizens API to use an NPC.
- The merchant items has some issues related with the lore of the item and what is considered to be the same item. 
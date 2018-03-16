package de.paul1365972.slimefunenergyupgrade;

import java.math.BigDecimal;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.Item.CustomItem;
import me.mrCookieSlime.CSCoreLibPlugin.general.World.CustomSkull;
import me.mrCookieSlime.Slimefun.Lists.SlimefunItems;
import me.mrCookieSlime.Slimefun.Setup.SlimefunManager;
import me.mrCookieSlime.Slimefun.api.energy.ItemEnergy;

public class SlimefunEnergyUpgrades implements Listener {
	
	private Bonus SMALL = new Bonus(0, 10, 1);
	private Bonus MEDIUM = new Bonus(1, 25, 2);
	private Bonus BIG = new Bonus(2, 100, 4);
	private Bonus LARGE = new Bonus(3, 400, 8);
	private Bonus CARBONADO = new Bonus(4, 2000, 15);
	private Bonus THORIUM = new Bonus(5, Float.POSITIVE_INFINITY, -1);
	
	private ItemStack THORIUM_ITEM;
	
	public SlimefunEnergyUpgrades(JavaPlugin plugin) {
		Bukkit.getScheduler().runTask(plugin, new Runnable() {
			@Override
			public void run() {
				try {
					THORIUM_ITEM = new CustomItem(CustomSkull.getItem("eyJ0aW1lc3RhbXAiOjE0NjM1OTgzMTg2NDgsInByb2ZpbGVJZCI6ImQ2MmI1MjJkMTVjZjQyNWE4NTFlNmNjNDRkOGJlMDg5IiwicHJvZmlsZU5hbWUiOiJKb2huMDAwNzA4IiwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzQyN2QxYTYxODRjNjJkNGM0YTY3Zjg2MmI4ZTE5ZWMwMDFhYmU0YzdkODg5ZjIzMzQ5ZThkYWZlNmQwMzMifX19"), "&8Thorium", new String[] { "", "&2Radiation Level: HIGH", "&4&oHazmat Suit required" });
				} catch (Exception e) {
					e.printStackTrace();
				}
				Bukkit.getPluginManager().registerEvents(SlimefunEnergyUpgrades.this, plugin);
			}
		});
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void craft(PrepareItemCraftEvent e) {
		ItemStack result = craft(e.getInventory().getMatrix());
		if (result != null)
			e.getInventory().setResult(result);
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void craft(InventoryClickEvent e) {
		if (e.getInventory() instanceof CraftingInventory) {
			CraftingInventory inv = (CraftingInventory) e.getInventory();
			if (e.getRawSlot() != 0)
				return;
			if (craft(inv.getMatrix()) == null)
				return;
			e.getWhoClicked().getInventory().addItem(inv.getItem(0).clone());
			ItemStack[] items = inv.getMatrix();
			for (int i = 0; i < items.length; i++) {
				items[i].setAmount(items[i].getAmount() - 1);
			}
			inv.setMatrix(items);
			if (e.getWhoClicked() instanceof Player)
				((Player) e.getWhoClicked()).updateInventory();
			e.setCancelled(true);
		}
	}

	private ItemStack craft(ItemStack[] matrix) {
		if (matrix.length != 9)
			return null;
		ItemMeta meta;
		if (matrix[4] != null && matrix[4].hasItemMeta() && (meta = matrix[4].getItemMeta()) != null && meta.hasDisplayName()) {
			Bonus neededBonus = getBonus(matrix[0]);
			if (neededBonus == null)
				return null;
			for (int i = 1; i < 9; i++) {
				if (i == 4)
					continue;
				Bonus bonus = getBonus(matrix[i]);
				if (bonus == null || bonus.getId() != neededBonus.getId())
					return null;
			}
			ItemStack result = matrix[4].clone();
			if (upgrade(result, neededBonus.getIncrease(), neededBonus.getSlots()))
				return result;
		}
		return null;
	}

	private Bonus getBonus(ItemStack item) {
		if (item == null || item.getType() == Material.AIR)
			return null;
		if (SlimefunManager.isItemSimiliar(item, SlimefunItems.SMALL_CAPACITOR, false))
			return SMALL;
		else if (SlimefunManager.isItemSimiliar(item, SlimefunItems.MEDIUM_CAPACITOR, false))
			return MEDIUM;
		else if (SlimefunManager.isItemSimiliar(item, SlimefunItems.BIG_CAPACITOR, false))
			return BIG;
		else if (SlimefunManager.isItemSimiliar(item, SlimefunItems.LARGE_CAPACITOR, false))
			return LARGE;
		else if (SlimefunManager.isItemSimiliar(item, SlimefunItems.CARBONADO_EDGED_CAPACITOR, false))
			return CARBONADO;
		else if (SlimefunManager.isItemSimiliar(item, THORIUM_ITEM, false))
			return THORIUM;
		else
			return null;
	}

	private boolean upgrade(ItemStack item, float increase, int slots) {
		if (item == null || item.getType() == null || item.getType().equals(Material.AIR))
			return false;
		if (!item.hasItemMeta() || !item.getItemMeta().hasLore())
			return false;
		float currentMax = ItemEnergy.getMaxEnergy(item);
		float currentEnergy = ItemEnergy.getStoredEnergy(item);

		float max = currentMax + increase;

		List<String> lore = item.getItemMeta().getLore();
		int index = -1;
		int slotIndex = -1;
		for (int i = 0; i < lore.size(); i++) {
			String line = lore.get(i);
			if (line.startsWith(ChatColor.translateAlternateColorCodes('&', "&a\u21E7 &f")) && line.contains(" / ") && line.endsWith("Upgrades used")) {
				slotIndex = i;
			} else if (line.startsWith(ChatColor.translateAlternateColorCodes('&', "&c&o&8\u21E8 &e\u26A1 &7")) && line.contains(" / ") && line.endsWith(" J")) {
				index = i;
			}
		}
		if (index == -1)
			return false;
		
		if (slotIndex == -1) {
			if (slots == -1)
				slots = 20;
			lore.add(ChatColor.translateAlternateColorCodes('&', "&a\u21E7 &f" + slots + " / 20 &aUpgrades used"));
		} else {
			int newSlots = 0;
			if (slots == -1) {
				newSlots = 20;
			} else {
				String slotsLeft = lore.get(slotIndex).substring(ChatColor.translateAlternateColorCodes('&', "&a\u21E7 &f").length()).split("/")[0].trim();
				newSlots = slots + Integer.valueOf(slotsLeft);
				if (newSlots > 20)
					return false;
			}
			lore.set(slotIndex, ChatColor.translateAlternateColorCodes('&', "&a\u21E7 &f" + newSlots + " / 20 &aUpgrades used"));
		}
		
		float value = Float.isFinite(max) ? new BigDecimal(max).setScale(2, BigDecimal.ROUND_HALF_UP).floatValue() : max;
		lore.set(index, ChatColor.translateAlternateColorCodes('&', "&c&o&8\u21E8 &e\u26A1 &7") + currentEnergy + " / " + value + " J");
		ItemMeta im = item.getItemMeta();
		im.setLore(lore);
		item.setItemMeta(im);
		return true;
	}
	
	public class Bonus {
		private int id, slots;
		private float increase;
		
		public Bonus(int id, float increase, int slots) {
			this.id = id;
			this.increase = increase;
			this.slots = slots;
		}
		public int getId() {
			return id;
		}
		public float getIncrease() {
			return increase;
		}
		public int getSlots() {
			return slots;
		}
	}

}

package me.paperxiang.stormeye;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import java.util.Random;
import me.paperxiang.stormeye.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.bukkit.loot.LootTable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
public class StormInventory implements InventoryHolder {
    private final Inventory stormed;
    private final Inventory inventory;
    private final BukkitTask[] unlockTasks;
    private static final ItemStack OVERLAY;
    private static final long UNLOCK_DELAY = 13;
    static {
        OVERLAY = new ItemStack(Material.BARRIER);
        OVERLAY.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true).build());
    }
    public StormInventory(Inventory inventory) {
        this.inventory = inventory;
        stormed = Bukkit.createInventory(this, inventory.getType());
        for (int i = 0; i < stormed.getSize(); i++) {
            if (!Utils.isEmpty(inventory.getItem(i))) {
                stormed.setItem(i, OVERLAY);
            }
        }
        unlockTasks = new BukkitTask[inventory.getSize()];
    }
    public StormInventory(LootTable lootTable, Random random, Location location, InventoryType type) {
        this(inventoryFromLoot(lootTable, random, location, type));
    }
    private static Inventory inventoryFromLoot(LootTable lootTable, Random random, Location location, InventoryType type) {
        final Inventory inventory = Bukkit.createInventory(null, type);
        lootTable.fillInventory(inventory, random, new LootContext.Builder(location).build());
        return inventory;
    }
    public void open(HumanEntity human) {
        human.openInventory(stormed);
    }
    public void unlock(int slot) {
        if (unlockTasks[slot] == null) {
            unlockTasks[slot] = Bukkit.getScheduler().runTaskLater(StormEye.getInstance(), () -> stormed.setItem(slot, inventory.getItem(slot)), UNLOCK_DELAY);
        }
    }
    public void sync() {
        Bukkit.getScheduler().runTask(StormEye.getInstance(), () -> {
            for (int i = 0; i < stormed.getSize(); i++) {
                final ItemStack item = stormed.getItem(i);
                if (item == null || item.getType() != Material.BARRIER) {
                    inventory.setItem(i, item);
                }
            }
        });
    }
    @Override
    public @NotNull Inventory getInventory() {
        return stormed;
    }
}

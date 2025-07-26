package me.paperxiang.theannihilation.listeners;
import io.papermc.paper.block.TileStateInventoryHolder;
import me.paperxiang.theannihilation.TheAnnihilation;
import me.paperxiang.theannihilation.ui.EnchantmentsWindow;
import me.paperxiang.theannihilation.utils.ItemUtils;
import me.paperxiang.theannihilation.utils.Utils;
import net.momirealms.craftengine.bukkit.api.BukkitAdaptors;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.world.LootGenerateEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.PlayerInventory;
public final class InventoryListener implements Listener {
    private static final InventoryListener instance = new InventoryListener();
    private InventoryListener() {}
    public static void init() {
        Bukkit.getPluginManager().registerEvents(instance, TheAnnihilation.getInstance());
    }
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClick() == ClickType.SWAP_OFFHAND && event.getClickedInventory() instanceof PlayerInventory && !Utils.isEmpty(event.getCurrentItem())) {
            event.setCancelled(true);
            Bukkit.getScheduler().runTask(TheAnnihilation.getInstance(), () -> new EnchantmentsWindow(event.getView(), event.getCurrentItem()).open((Player) event.getWhoClicked()));
        }
    }
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onLootGenerate(LootGenerateEvent event) {
        final ItemBuildContext context = event.getEntity() instanceof final Player player ? ItemBuildContext.of(BukkitAdaptors.adapt(player)) : ItemBuildContext.EMPTY;
        event.getLoot().replaceAll(source -> ItemUtils.generateCustom(source, context));
        final InventoryHolder holder = event.getInventoryHolder();
        if (holder instanceof final TileStateInventoryHolder tileState) {
            Utils.markLootTable(event.getLootTable(), tileState.getPersistentDataContainer());
            tileState.update();
        }
        if (holder instanceof final Entity entity) {
            Utils.markLootTable(event.getLootTable(), entity.getPersistentDataContainer());
        }
    }
}

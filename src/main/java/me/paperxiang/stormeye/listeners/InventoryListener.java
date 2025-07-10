package me.paperxiang.stormeye.listeners;
import me.paperxiang.stormeye.StormEye;
import me.paperxiang.stormeye.ui.EnchantmentsWindow;
import me.paperxiang.stormeye.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.PlayerInventory;
public final class InventoryListener implements Listener {
    private static final InventoryListener instance = new InventoryListener();
    private InventoryListener() {}
    public static void init() {
        Bukkit.getPluginManager().registerEvents(instance, StormEye.getInstance());
    }
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClick() == ClickType.SWAP_OFFHAND && event.getClickedInventory() instanceof PlayerInventory && !Utils.isEmpty(event.getCurrentItem())) {
            event.setCancelled(true);
            Bukkit.getScheduler().runTask(StormEye.getInstance(), () -> new EnchantmentsWindow(event.getView(), event.getCurrentItem()).open((Player) event.getWhoClicked()));
        }
    }
}

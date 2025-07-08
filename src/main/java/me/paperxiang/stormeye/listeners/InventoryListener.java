package me.paperxiang.stormeye.listeners;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.paperxiang.stormeye.StormEye;
import me.paperxiang.stormeye.StormInventory;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Container;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
public final class InventoryListener implements Listener {
    private static final InventoryListener instance = new InventoryListener();
    private static final ConcurrentHashMap<UUID, Integer> openWindowIDs = new ConcurrentHashMap<>();
    private InventoryListener() {}
    public static void init() {
        Bukkit.getPluginManager().registerEvents(instance, StormEye.getInstance());
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(StormEye.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Server.OPEN_WINDOW) {
            @Override
            public void onPacketSending(PacketEvent event) {
                openWindowIDs.put(event.getPlayer().getUniqueId(), event.getPacket().getIntegers().readSafely(0));
            }
        });
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(StormEye.getInstance(), ListenerPriority.NORMAL, PacketType.Play.Server.CLOSE_WINDOW) {
            @Override
            public void onPacketSending(PacketEvent event) {
                openWindowIDs.put(event.getPlayer().getUniqueId(), 0);
            }
        });
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        openWindowIDs.put(event.getPlayer().getUniqueId(), 0);
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        openWindowIDs.remove(event.getPlayer().getUniqueId());
    }
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryOpen(InventoryOpenEvent event) {
        switch (event.getInventory().getHolder()) {
            case Container container -> {
                event.setCancelled(true);
                new StormInventory(event.getInventory()).open(event.getPlayer());
            }
            case null, default -> {}
        }
    }
    @SuppressWarnings("IsCancelled")
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        final Inventory inventory = event.getInventory();
        if (inventory.getHolder() instanceof final StormInventory storm) {
            if (event.getClickedInventory() == inventory) {
                if (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.BARRIER) {
                    event.setCancelled(true);
                    storm.unlock(event.getSlot());
                }
            }
            if (!event.isCancelled()) {
                storm.sync();
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        final Inventory inventory = event.getInventory();
        if (inventory.getHolder() instanceof final StormInventory storm) {
            storm.sync();
        }
    }
}

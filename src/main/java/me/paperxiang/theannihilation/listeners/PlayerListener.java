package me.paperxiang.theannihilation.listeners;
import me.paperxiang.theannihilation.TheAnnihilation;
import me.paperxiang.theannihilation.utils.InventoryUtils;
import me.paperxiang.theannihilation.utils.Mission;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
public final class PlayerListener implements Listener {
    private static final PlayerListener instance = new PlayerListener();
    private PlayerListener() {}
    public static void init() {
        Bukkit.getPluginManager().registerEvents(instance, TheAnnihilation.getInstance());
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerJoin(PlayerJoinEvent event) {
        InventoryUtils.init(event.getPlayer());
    }
    @EventHandler
    public void on(PlayerChatEvent event) {
        switch (event.getMessage()) {
            case "start" -> {
                final Mission mission = Mission.create("mooncore_caverns");
                mission.addPlayer(event.getPlayer().getUniqueId());
                mission.start();
            }
            default -> {}
        }
    }
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setKeepInventory(true);
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerQuit(PlayerQuitEvent event) {
        InventoryUtils.fina(event.getPlayer());
    }
}

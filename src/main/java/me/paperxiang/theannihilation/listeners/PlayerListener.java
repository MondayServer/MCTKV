package me.paperxiang.theannihilation.listeners;
import java.util.Optional;
import java.util.UUID;
import me.paperxiang.theannihilation.TheAnnihilation;
import me.paperxiang.theannihilation.utils.InventoryUtils;
import me.paperxiang.theannihilation.mission.Mission;
import me.paperxiang.theannihilation.utils.ScoreboardUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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
        final Player player = event.getPlayer();
        InventoryUtils.init(player);
        ScoreboardUtils.init(player);
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
        final Player player = event.getPlayer();
        final UUID uuid = player.getUniqueId();
        ScoreboardUtils.fina(player);
        Optional.ofNullable(Mission.getMission(uuid)).ifPresent(mission -> mission.removePlayer(uuid));
        InventoryUtils.fina(player);
    }
}

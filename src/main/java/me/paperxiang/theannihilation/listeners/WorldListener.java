package me.paperxiang.theannihilation.listeners;
import me.paperxiang.theannihilation.TheAnnihilation;
import me.paperxiang.theannihilation.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.spigotmc.event.player.PlayerSpawnLocationEvent;
public final class WorldListener implements Listener {
    private static final WorldListener instance = new WorldListener();
    private WorldListener() {}
    public static void init() {
        Bukkit.getPluginManager().registerEvents(instance, TheAnnihilation.getInstance());
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerSpawnLocationEvent(PlayerSpawnLocationEvent event) {
        event.setSpawnLocation(Utils.HUB);
    }
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChangedWorldEvent(PlayerChangedWorldEvent event) {
        final Player player = event.getPlayer();
        player.setGameMode(GameMode.ADVENTURE);
        player.clearActivePotionEffects();
        player.clearActiveItem();
        player.setArrowsInBody(0);
        player.setRemainingAir(300);
        player.setFireTicks(-20);
        player.setFreezeTicks(0);
        player.setHealth(20);
        player.setFoodLevel(20);
        player.setSaturation(5);
        player.setExhaustion(0);
        switch (player.getWorld().getName()) {
            case "world" -> {
                player.setInvulnerable(true);
            }
            case "the_annihilation_lobby" -> {
            }
            default -> {
                player.setInvulnerable(false);
            }
        }
    }
}

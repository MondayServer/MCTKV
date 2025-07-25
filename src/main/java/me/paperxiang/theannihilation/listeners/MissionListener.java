package me.paperxiang.theannihilation.listeners;
import com.destroystokyo.paper.event.player.PlayerStopSpectatingEntityEvent;
import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import java.util.Optional;
import me.paperxiang.theannihilation.TheAnnihilation;
import me.paperxiang.theannihilation.utils.ComponentUtils;
import me.paperxiang.theannihilation.utils.Mission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.persistence.PersistentDataType;
public final class MissionListener implements Listener {
    private static final NamespacedKey START_EVACUATION_FLAG = new NamespacedKey(TheAnnihilation.getInstance(), "start_evacuation");
    private static final NamespacedKey LAST_EVACUATING = new NamespacedKey(TheAnnihilation.getInstance(), "last_evacuating");
    private static final MissionListener instance = new MissionListener();
    private MissionListener() {}
    public static void init() {
        Bukkit.getPluginManager().registerEvents(instance, TheAnnihilation.getInstance());
    }
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityPortalEnter(EntityPortalEnterEvent event) {
        if (event.getEntity() instanceof final Player player && player.getPortalCooldown() <= 0 && Mission.isInMission(player.getUniqueId())) {
            final int now = Bukkit.getCurrentTick();
            final int last = player.getPersistentDataContainer().getOrDefault(LAST_EVACUATING, PersistentDataType.INTEGER, -2);
            if (now == last) {
                return;
            }
            final int start;
            if (now == last + 1) {
                start = player.getPersistentDataContainer().getOrDefault(START_EVACUATION_FLAG, PersistentDataType.INTEGER, 0);
            } else {
                player.getPersistentDataContainer().set(START_EVACUATION_FLAG, PersistentDataType.INTEGER, start = now);
            }
            player.getPersistentDataContainer().set(LAST_EVACUATING, PersistentDataType.INTEGER, now);
            player.sendActionBar(ComponentUtils.renderProgressBar("·", 32, NamedTextColor.GRAY, NamedTextColor.DARK_PURPLE, (float) (now - start) / player.getWorld().getGameRuleValue(GameRule.PLAYERS_NETHER_PORTAL_DEFAULT_DELAY)));
        }
    }
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        final Player player = event.getPlayer();
        final Player killer = player.getKiller();
        final boolean hasKiller = killer != null && !killer.getUniqueId().equals(player.getUniqueId());
        final Component deathMessage = Optional.ofNullable(event.deathScreenMessageOverride()).orElse(event.deathMessage());
        if (deathMessage != null) {
            player.sendActionBar(deathMessage);
            if (hasKiller) {
                killer.sendActionBar(deathMessage);
            }
        }
        Bukkit.getScheduler().runTask(TheAnnihilation.getInstance(), () -> {
            player.setGameMode(GameMode.SPECTATOR);
            player.setSpectatorTarget(killer);
        });
    }
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerStopSpectatingEntity(PlayerStopSpectatingEntityEvent event) {
    }
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityPortalReady(EntityPortalReadyEvent event) {
        if (event.getEntity() instanceof final Player player) {
            if (Mission.isInMission(player.getUniqueId()) && Mission.getMission(player.getUniqueId()).completeMission(player.getUniqueId())) {
                player.getPersistentDataContainer().remove(START_EVACUATION_FLAG);
                player.getPersistentDataContainer().remove(LAST_EVACUATING);
            }
        }
    }
}

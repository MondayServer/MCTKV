package me.paperxiang.stormeye.listeners;
import io.papermc.paper.event.entity.EntityPortalReadyEvent;
import me.paperxiang.stormeye.StormEye;
import me.paperxiang.stormeye.utils.ComponentUtils;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.persistence.PersistentDataType;
public final class PlayerListener implements Listener {
    private static final PlayerListener instance = new PlayerListener();
    private static final NamespacedKey START_EVACUATION_FLAG = new NamespacedKey(StormEye.getInstance(), "start_evacuation");
    private static final NamespacedKey LAST_EVACUATING = new NamespacedKey(StormEye.getInstance(), "last_evacuating");
    private PlayerListener() {}
    public static void init() {
        Bukkit.getPluginManager().registerEvents(instance, StormEye.getInstance());
    }
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setKeepInventory(true);
    }
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityPortalEnter(EntityPortalEnterEvent event) {
        if (event.getEntity() instanceof final Player player && player.getPortalCooldown() <= 0) {
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
    public void onEntityPortalReady(EntityPortalReadyEvent event) {
        if (event.getEntity() instanceof final Player player) {
            player.getPersistentDataContainer().remove(START_EVACUATION_FLAG);
            player.getPersistentDataContainer().remove(LAST_EVACUATING);
            player.sendActionBar(Component.text("成功撤离"));
            player.getWorld().sendMessage(Component.text("成功撤离"), ChatType.EMOTE_COMMAND.bind(player.displayName().hoverEvent(HoverEvent.showEntity(Key.key("minecraft:player"), player.getUniqueId(), player.displayName())).clickEvent(ClickEvent.suggestCommand("/tell " + player.getName() + " "))));
            player.showWinScreen();
        }
    }
}

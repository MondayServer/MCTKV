package me.paperxiang.theannihilation.utils;
import java.time.Duration;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import me.paperxiang.theannihilation.TheAnnihilation;
import net.kyori.adventure.chat.ChatType;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitTask;
public final class Mission {
    private MissionStage stage = MissionStage.STARTUP;
    private int startTime;
    private final int timeLimit = 1200;
    private final int id;
    private World world;
    private final String map;
    private final WorldHost worldHost;
    private final HashSet<UUID> players = new HashSet<>();
    private static final NamespacedKey BINDING = new NamespacedKey(TheAnnihilation.getInstance(), "binding");
    private Mission(int id, String map, WorldHost worldHost) {
        this.id = id;
        this.map = map;
        this.worldHost = worldHost;
        mapSidebarComponent = Component.translatable("sidebar.mission.map", TranslationArgument.component(Component.translatable("map." + map)));
    }
    public void loadWorld() {
        world = worldHost.load().orElseThrow();
    }
    public String getMap() {
        return map;
    }
    public void start() {
        stage = MissionStage.IN_PROGRESS;
        worldHost.addEntities(players.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).collect(Collectors.toUnmodifiableList()));
        for (UUID uuid : players) {
            final Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                continue;
            }
            player.showTitle(Title.title(Component.text("任务开始"), Component.empty(), Title.Times.times(Duration.ofMillis(250), Duration.ofMillis(2250), Duration.ofMillis(500))));
        }
        startTime = Bukkit.getCurrentTick();
        final BukkitTask task = Bukkit.getScheduler().runTaskTimer(TheAnnihilation.getInstance(), () -> {
            final int ticksLeft = startTime + timeLimit - Bukkit.getCurrentTick();
            timeSidebarComponent = Component.translatable("sidebar.mission.time", TranslationArgument.component(Component.text(String.format((Locale) null, "%1$02d:%2$02d", ticksLeft / 1200, ticksLeft / 20 % 60))));
        }, 1, 20);
        Bukkit.getScheduler().runTaskLater(TheAnnihilation.getInstance(), () -> {
            task.cancel();
            endMission();
        }, timeLimit);
    }
    public boolean completeMission(UUID uuid) {
        final Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return false;
        }
        player.sendActionBar(Component.text("成功撤离"));
        player.getWorld().sendMessage(Component.text("成功撤离"), ChatType.EMOTE_COMMAND.bind(player.displayName().hoverEvent(HoverEvent.showEntity(Key.key("minecraft:player"), player.getUniqueId(), player.displayName())).clickEvent(ClickEvent.suggestCommand("/tell " + player.getName() + " "))));
        sendToHub(uuid, true);
        return true;
    }
    public void endMission() {
        world.sendMessage(Component.text("任务失败"));
        players.forEach(uuid -> sendToHub(uuid, false));
        ended();
    }
    private void ended() {
        activeMissions.remove(this);
        worldHost.unload();
    }
    public void sendToHub(UUID uuid, boolean missionSuccess) {
        removePlayer(uuid);
        final Player player = Bukkit.getPlayer(uuid);
        if (player == null) {
            return;
        }
        final PlayerInventory inventory = player.getInventory();
        for (int i = 0; i < inventory.getSize(); i++) {
            final ItemStack item = inventory.getItem(i);
            if (item != null && !item.isEmpty()) {
                if (!(item.getPersistentDataContainer().has(BINDING, PersistentDataTypes.UUID) ? item.getPersistentDataContainer().get(BINDING, PersistentDataTypes.UUID).equals(uuid) : missionSuccess)) {
                    inventory.setItem(i, null);
                }
            }
        }
        player.teleportAsync(Bukkit.getWorld("world").getSpawnLocation());
    }
    private static final AtomicInteger missionCounter = new AtomicInteger();
    private static final ConcurrentHashMap<UUID, Mission> currentMissions = new ConcurrentHashMap<>();
    private static final HashSet<Mission> activeMissions = new HashSet<>();
    public static Mission create(String map) {
        final int id = missionCounter.getAndIncrement();
        final Mission mission = new Mission(id, map, WorldHost.multiverse(map, "mission" + id));
        mission.loadWorld();
        final MapInfo info = Utils.getMapInfo(map);
        Utils.reLoot(mission.world, info.minChunkX(), info.maxChunkX(), info.minChunkZ(), info.maxChunkZ());
        activeMissions.add(mission);
        return mission;
    }
    public static boolean isInMission(UUID uuid) {
        return currentMissions.containsKey(uuid);
    }
    public static Mission getMission(UUID uuid) {
        return currentMissions.get(uuid);
    }
    public void addPlayer(UUID uuid) {
        players.add(uuid);
        currentMissions.put(uuid, this);
    }
    public void removePlayer(UUID uuid) {
        currentMissions.remove(uuid);
        players.remove(uuid);
    }
    public static void fina() {
        activeMissions.forEach(Mission::endMission);
    }
    private final Component mapSidebarComponent;
    private Component timeSidebarComponent = Component.empty();
    public Component mapSidebarComponent() {
        return mapSidebarComponent;
    }
    public Component timeSidebarComponent() {
        return timeSidebarComponent;
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof final Mission mission) {
            return id == mission.id;
        }
        return false;
    }
    @Override
    public int hashCode() {
        return id;
    }
}

package me.paperxiang.theannihilation.utils;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.paperxiang.theannihilation.TheAnnihilation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslationArgument;
import net.megavex.scoreboardlibrary.api.ScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.exception.NoPacketAdapterAvailableException;
import net.megavex.scoreboardlibrary.api.noop.NoopScoreboardLibrary;
import net.megavex.scoreboardlibrary.api.sidebar.Sidebar;
import net.megavex.scoreboardlibrary.api.sidebar.component.ComponentSidebarLayout;
import net.megavex.scoreboardlibrary.api.sidebar.component.SidebarComponent;
import net.megavex.scoreboardlibrary.api.sidebar.component.animation.CollectionSidebarAnimation;
import org.bukkit.entity.Player;
public final class ScoreboardUtils {
    private static ScoreboardLibrary scoreboardLibrary;
    private static SidebarComponent title;
    private static final ConcurrentHashMap<UUID, PlayerSidebar> sidebars = new ConcurrentHashMap<>();
    public static void init() {
        try {
            scoreboardLibrary = ScoreboardLibrary.loadScoreboardLibrary(TheAnnihilation.getInstance());
        } catch (NoPacketAdapterAvailableException exception) {
            scoreboardLibrary = new NoopScoreboardLibrary();
            TheAnnihilation.logError("No scoreboard packet adapter available: " + exception.getMessage());
        }
        title = SidebarComponent.animatedComponent(new CollectionSidebarAnimation<>(List.of(SidebarComponent.staticLine(Component.translatable("sidebar.title")))));
    }
    public static void init(Player player) {
        final UUID uuid = player.getUniqueId();
        final PlayerSidebar playerSidebar = new PlayerSidebar(player);
        sidebars.put(uuid, playerSidebar);
        player.getScheduler().runAtFixedRate(TheAnnihilation.getInstance(), task -> {
            if (Mission.isInMission(uuid)) {
                playerSidebar.applyMission();
            } else {
                playerSidebar.applyHub();
            }
        }, null, 1, 1);
    }
    public static void fina(Player player) {
        sidebars.remove(player.getUniqueId()).close();
    }
    public static void fina() {
        scoreboardLibrary.close();
    }
    private static final class PlayerSidebar {
        private final Player player;
        private final Sidebar sidebar;
        private final ComponentSidebarLayout hubSidebar;
        private final ComponentSidebarLayout missionSidebar;
        private PlayerSidebar(Player player) {
            sidebar = scoreboardLibrary.createSidebar();
            sidebar.addPlayer(this.player = player);
            hubSidebar = new ComponentSidebarLayout(title, SidebarComponent.builder()
                .addStaticLine(Component.translatable("sidebar.info.header"))
                .addDynamicLine(() -> Component.translatable("sidebar.info.flops", TranslationArgument.numeric(0)))
                .addDynamicLine(() -> Component.translatable("sidebar.info.hashes", TranslationArgument.numeric(0)))
                .addStaticLine(Component.translatable("sidebar.footer"))
            .build());
            missionSidebar = new ComponentSidebarLayout(title, SidebarComponent.builder()
                .addStaticLine(Component.translatable("sidebar.mission.header"))
                .addDynamicLine(() -> Mission.getMission(player.getUniqueId()).mapSidebarComponent())
                .addDynamicLine(() -> Mission.getMission(player.getUniqueId()).timeSidebarComponent())
                .addBlankLine()
                .addStaticLine(Component.translatable("sidebar.info.header"))
                .addDynamicLine(() -> Component.translatable("sidebar.info.flops", TranslationArgument.numeric(0)))
                .addDynamicLine(() -> Component.translatable("sidebar.info.hashes", TranslationArgument.numeric(0)))
                .addStaticLine(Component.translatable("sidebar.footer"))
            .build());
        }
        public void applyHub() {
            hubSidebar.apply(sidebar);
        }
        public void applyMission() {
            missionSidebar.apply(sidebar);
        }
        public void close() {
            sidebar.close();
        }
        @Override
        public int hashCode() {
            return player.hashCode();
        }
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof final PlayerSidebar playerSidebar) {
                return player.equals(playerSidebar.player);
            }
            return false;
        }
    }
}

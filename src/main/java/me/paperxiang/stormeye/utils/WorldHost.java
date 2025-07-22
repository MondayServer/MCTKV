package me.paperxiang.stormeye.utils;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import me.paperxiang.stormeye.StormEye;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.mvplugins.multiverse.core.MultiverseCoreApi;
import org.mvplugins.multiverse.core.teleportation.AsyncSafetyTeleporter;
import org.mvplugins.multiverse.core.utils.result.AsyncAttemptsAggregate;
import org.mvplugins.multiverse.core.world.LoadedMultiverseWorld;
import org.mvplugins.multiverse.core.world.WorldManager;
import org.mvplugins.multiverse.core.world.options.CloneWorldOptions;
import org.mvplugins.multiverse.core.world.options.DeleteWorldOptions;
import org.mvplugins.multiverse.core.world.options.ImportWorldOptions;
public interface WorldHost {
    Optional<World> load();
    default void addEntity(Entity entity) {
        addEntities(Collections.singletonList(entity));
    }
    void addEntities(List<Entity> entities);
    default void removeEntity(Entity entity) {
        removeEntities(Collections.singletonList(entity));
    }
    void removeEntities(List<Entity> entities);
    void unload();
    static WorldHost multiverse(String map, String world) {
        return new MultiverseWorldHost(map, world);
    }
}
final class MultiverseWorldHost implements WorldHost {
    private final String map;
    private final String world;
    private LoadedMultiverseWorld multiverseWorld;
    private static final AsyncSafetyTeleporter safetyTeleporter = MultiverseCoreApi.get().getSafetyTeleporter();
    private static final WorldManager worldManager = MultiverseCoreApi.get().getWorldManager();
    private static final Random EXPONENTIAL_BACKUP_RANDOM = new Random();
    MultiverseWorldHost(String map, String world) {
        this.map = map;
        this.world = world;
    }
    @Override
    public Optional<World> load() {
        return worldManager.cloneWorld(CloneWorldOptions.fromTo(worldManager.getLoadedWorld(map).getOrElse(() -> worldManager.loadWorld(map).fold(failure -> worldManager.importWorld(ImportWorldOptions.worldName(map)).getOrNull(), loaded -> loaded)), world)).onSuccess(loaded -> multiverseWorld = loaded).fold(failure -> Optional.empty(), loaded -> loaded.getBukkitWorld().fold(Optional::empty, Optional::of));
    }
    @Override
    public void addEntities(List<Entity> entities) {
        tryAddEntities(entities, 1);
    }
    @Override
    public void removeEntities(List<Entity> entities) {

    }
    public void tryAddEntities(List<Entity> entities, int delay) {
        try {
            safetyTeleporter.to(Bukkit.getWorld(world).getSpawnLocation()).checkSafety(false).teleport(entities).onFailure(() -> {
                throw new RuntimeException();
            });
        } catch (RuntimeException exception) {
            StormEye.logError("Failed to add entities to " + world + "! Retrying in " + delay + " ticks");
            Bukkit.getScheduler().runTaskLater(StormEye.getInstance(), () -> tryAddEntities(entities, delay * 2 + EXPONENTIAL_BACKUP_RANDOM.nextInt(2)), delay);
        }
    }
    @Override
    public void unload() {
        tryUnload(1);
    }
    public void tryUnload(int delay) {
        multiverseWorld.getPlayers().fold(AsyncAttemptsAggregate::emptySuccess, players -> safetyTeleporter.to(Utils.HUB).checkSafety(false).teleport(players)).onSuccess(() -> worldManager.deleteWorld(DeleteWorldOptions.world(multiverseWorld)).onFailure(() -> {
            StormEye.logError("Failed to delete world " + world + "! Retrying in " + delay + " ticks");
            Bukkit.getScheduler().runTaskLater(StormEye.getInstance(), () -> tryUnload(delay * 2 + EXPONENTIAL_BACKUP_RANDOM.nextInt(2)), delay);
        })).onFailure(() -> {
            StormEye.logError("Failed to remove players from " + world + "! Retrying in " + delay + " ticks");
            Bukkit.getScheduler().runTaskLater(StormEye.getInstance(), () -> tryUnload(delay * 2 + EXPONENTIAL_BACKUP_RANDOM.nextInt(2)), delay);
        });
    }
}

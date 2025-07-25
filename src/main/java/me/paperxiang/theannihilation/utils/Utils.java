package me.paperxiang.theannihilation.utils;
import com.destroystokyo.paper.loottable.LootableInventory;
import io.papermc.paper.block.TileStateInventoryHolder;
import io.papermc.paper.math.BlockPosition;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import me.paperxiang.theannihilation.TheAnnihilation;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.Lootable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
public final class Utils {
    public static final Location HUB = Bukkit.getWorld("world").getSpawnLocation();
    private static final BlockFace[] neighbors = new BlockFace[]{BlockFace.WEST, BlockFace.EAST, BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH};
    private static final NamespacedKey FLOPS = new NamespacedKey(TheAnnihilation.getInstance(), "flops");
    private static final NamespacedKey FLOPS_TRANSIENT = new NamespacedKey(TheAnnihilation.getInstance(), "flops_transient");
    private static final NamespacedKey HASHES = new NamespacedKey(TheAnnihilation.getInstance(), "hashes");
    private static final NamespacedKey LOOT_TABLE = new NamespacedKey(TheAnnihilation.getInstance(), "loot_table");
    private static final EnumMap<MapType, ConcurrentHashMap<String, MapInfo>> maps = new EnumMap<>(MapType.class);
    private static final CommandSender IGNORED = Bukkit.createCommandSender(component -> {});
    private Utils() {}
    public static void init() {
        final World world = Bukkit.getWorld("world");
        world.setSimulationDistance(2);
        world.setViewDistance(2);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
        world.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
        final YamlConfiguration allMaps = YamlConfiguration.loadConfiguration(new InputStreamReader(TheAnnihilation.getInstance().getResource("maps.yml"), StandardCharsets.UTF_8));
        try {
            final Map<String, Object> values = allMaps.getValues(true);
            for (final MapType type : MapType.values()) {
                final ConcurrentHashMap<String, MapInfo> mapInfos = new ConcurrentHashMap<>();
                ((ConfigurationSection) values.get(type.getId())).getValues(true).forEach((name, data) -> {
                    if (data instanceof final ConfigurationSection section) {
                        final Map<String, Object> info = section.getValues(true);
                        final List<?> chunkX = (List<?>) info.get("chunk-x");
                        final List<?> chunkZ = (List<?>) info.get("chunk-z");
                        mapInfos.put(name, new MapInfo((Integer) chunkX.get(0), (Integer) chunkX.get(1), (Integer) chunkZ.get(0), (Integer) chunkZ.get(1)));
                    }
                });
                maps.put(type, mapInfos);
            }
        } catch (RuntimeException exception) {
            TheAnnihilation.logError("Error loading map data(Check your maps.yml)!: " + exception.getMessage());
        }
    }
    public static MapInfo getMapInfo(String map) {
        for (final MapType type : maps.keySet()) {
            final ConcurrentHashMap<String, MapInfo> infos = maps.get(type);
            if (infos.containsKey(map)) {
                return infos.get(map);
            }
        }
        return null;
    }
    public static void markLootTable(LootTable lootTable, PersistentDataContainer data) {
        data.set(LOOT_TABLE, PersistentDataType.STRING, lootTable.getKey().toString());
    }
    /**
     * @param world the world
     * @param minX min chunk x, inclusive
     * @param maxX max chunk x, exclusive
     * @param minZ min chunk z, inclusive
     * @param maxZ max chunk z, exclusive
     */
    public static void reLoot(World world, int minX, int maxX, int minZ, int maxZ) {
        for (Chunk chunk : getChunks(world, minX, maxX, minZ, maxZ)) {
            for (BlockState blockState : chunk.getTileEntities(block -> block.getState() instanceof TileStateInventoryHolder && block.getState() instanceof Lootable, false)) {
                final TileStateInventoryHolder tileState = (TileStateInventoryHolder) blockState;
                if (tileState.getPersistentDataContainer().has(LOOT_TABLE, PersistentDataType.STRING)) {
                    final Location location = tileState.getLocation();
                    Bukkit.dispatchCommand(IGNORED, "execute in " + world.getKey() + " run data remove block " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ() + " Items");
                    ((Lootable) blockState).setLootTable(Bukkit.getLootTable(NamespacedKey.fromString(tileState.getPersistentDataContainer().get(LOOT_TABLE, PersistentDataType.STRING))));
                }
            }
        }
    }
    private static Chunk[] getChunks(World world, int minX, int maxX, int minZ, int maxZ) {
        if (maxX <= minX) {
            throw new IllegalArgumentException("maxX should be greater than minX");
        }
        if (maxZ <= minZ) {
            throw new IllegalArgumentException("maxZ should be greater than minZ");
        }
        int cnt = 0;
        final Chunk[] chunks = new Chunk[(maxX - minX) * (maxZ - minZ)];
        for (int x = minX; x < maxX; x++) {
            for (int z = minZ; z < maxZ; z++) {
                chunks[cnt++] = world.getChunkAt(x, z);
            }
        }
        return chunks;
    }
    public static boolean isEmpty(ItemStack itemStack) {
        return itemStack == null || itemStack.isEmpty();
    }
    public static boolean isMaterial(ItemStack itemStack, Material material) {
        return itemStack != null && itemStack.getType() == material;
    }
    public static List<Block> connected(Block block, int limit) {
        return connected(block, block0 -> block0.getType() == block.getType(), limit);
    }
    @SuppressWarnings("UnstableApiUsage")
    public static List<Block> connected(Block block, Predicate<Block> canConnect, int limit) {
        final ArrayList<Block> connected = new ArrayList<>();
        final HashSet<BlockPosition> searched = new HashSet<>();
        final ArrayDeque<Block> searches = new ArrayDeque<>();
        searches.add(block);
        searched.add(block.getLocation().toBlock());
        while (!searches.isEmpty() && connected.size() < limit) {
            final Block block0 = searches.poll();
            if (canConnect.test(block0)) {
                connected.add(block0);
                for (final BlockFace face : neighbors) {
                    final Block neighbor = block0.getRelative(face);
                    final BlockPosition position = neighbor.getLocation().toBlock();
                    if (!searched.contains(position)) {
                        searched.add(position);
                        searches.add(neighbor);
                    }
                }
            }
        }
        return connected;
    }
}

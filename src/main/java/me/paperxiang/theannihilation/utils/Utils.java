package me.paperxiang.theannihilation.utils;
import com.destroystokyo.paper.loottable.LootableEntityInventory;
import de.tr7zw.nbtapi.NBT;
import io.papermc.paper.block.TileStateInventoryHolder;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.CustomModelData;
import io.papermc.paper.math.BlockPosition;
import io.papermc.paper.persistence.PersistentDataContainerView;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import me.paperxiang.theannihilation.TheAnnihilation;
import me.paperxiang.theannihilation.mission.MapInfo;
import me.paperxiang.theannihilation.mission.MapType;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityExhaustionEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootTable;
import org.bukkit.loot.Lootable;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
public final class Utils {
    public static final Location HUB = Bukkit.getWorld("world").getSpawnLocation();
    private static final BlockFace[] neighbors = new BlockFace[]{BlockFace.WEST, BlockFace.EAST, BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH};
    private static final NamespacedKey FLOPS = new NamespacedKey(TheAnnihilation.getInstance(), "flops");
    private static final NamespacedKey FLOPS_TRANSIENT = new NamespacedKey(TheAnnihilation.getInstance(), "flops_transient");
    private static final NamespacedKey HASHES = new NamespacedKey(TheAnnihilation.getInstance(), "hashes");
    private static final NamespacedKey LOOT_TABLE = new NamespacedKey(TheAnnihilation.getInstance(), "loot_table");
    private static final EnumMap<MapType, ConcurrentHashMap<String, MapInfo>> maps = new EnumMap<>(MapType.class);
    private Utils() {}
    public static void init() {
        Bukkit.getScheduler().runTaskTimer(TheAnnihilation.getInstance(), () -> {
            maximumNoDamageTicksModifiedMarked.stream().map(Bukkit::getEntity).filter(Objects::nonNull).map(entity -> (LivingEntity) entity).forEach(entity -> entity.setMaximumNoDamageTicks(0));
            maximumNoDamageTicksModifiedMarked.clear();
        }, 1, 1);
        configure(Bukkit.getWorld("world"), false);
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
    public static void configure(World world, boolean mission) {
        if (!mission) {
            world.setSimulationDistance(2);
            world.setViewDistance(2);
        }
        world.setGameRule(GameRule.DISABLE_RAIDS, true);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_FIRE_TICK, false);
        world.setGameRule(GameRule.DO_INSOMNIA, false);
        world.setGameRule(GameRule.DO_LIMITED_CRAFTING, true);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_PATROL_SPAWNING, false);
        world.setGameRule(GameRule.DO_TRADER_SPAWNING, false);
        world.setGameRule(GameRule.DO_VINES_SPREAD, false);
        world.setGameRule(GameRule.DO_WARDEN_SPAWNING, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.KEEP_INVENTORY, !mission);
        world.setGameRule(GameRule.LOCATOR_BAR, false);
        world.setGameRule(GameRule.NATURAL_REGENERATION, false);
        world.setGameRule(GameRule.PLAYERS_SLEEPING_PERCENTAGE, Integer.MAX_VALUE);
        world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
        world.setGameRule(GameRule.SPAWN_RADIUS, 0);
        world.setGameRule(GameRule.SPECTATORS_GENERATE_CHUNKS, false);
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
        for (final Chunk chunk : getChunks(world, minX, maxX, minZ, maxZ)) {
            for (final BlockState blockState : chunk.getTileEntities(block -> block.getState() instanceof TileStateInventoryHolder && block.getState() instanceof Lootable, false)) {
                final TileStateInventoryHolder tileState = (TileStateInventoryHolder) blockState;
                if (tileState.getPersistentDataContainer().has(LOOT_TABLE, PersistentDataType.STRING)) {
                    NBT.modify(tileState, nbt -> {
                        nbt.removeKey("Items");
                        nbt.removeKey("item");
                    });
                    ((Lootable) blockState).setLootTable(Bukkit.getLootTable(NamespacedKey.fromString(tileState.getPersistentDataContainer().get(LOOT_TABLE, PersistentDataType.STRING))));
                }
            }
            for (final Entity entity : chunk.getEntities()) {
                if (entity instanceof final LootableEntityInventory lootableEntity && entity.getPersistentDataContainer().has(LOOT_TABLE, PersistentDataType.STRING)) {
                    NBT.modify(entity, nbt -> {
                        nbt.removeKey("Items");
                    });
                    lootableEntity.setLootTable(Bukkit.getLootTable(NamespacedKey.fromString(entity.getPersistentDataContainer().get(LOOT_TABLE, PersistentDataType.STRING))));
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
    private static final HashSet<UUID> maximumNoDamageTicksModifiedMarked = new HashSet<>();
    public static void markMaximumNoDamageTicksModified(UUID uuid) {
        maximumNoDamageTicksModifiedMarked.add(uuid);
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
    private static final NamespacedKey CUSTOM_FOOD_TICK_TIMER = new NamespacedKey(TheAnnihilation.getInstance(), "food_tick_timer");
    public static final NamespacedKey CUSTOM_PROPERTIES = new NamespacedKey(TheAnnihilation.getInstance(), "properties");
    public static final NamespacedKey CUSTOM_PROPERTY_POWER = new NamespacedKey(TheAnnihilation.getInstance(), "power");
    public static final NamespacedKey CUSTOM_ITEM_USAGE = new NamespacedKey(TheAnnihilation.getInstance(), "item_usage");
    public static final byte CUSTOM_ITEM_USAGE_RANGED_WEAPON = 1;
    public static final NamespacedKey CUSTOM_ITEM_START_USE = new NamespacedKey(TheAnnihilation.getInstance(), "start_use");
    public static void init(Player player) {
        player.getScheduler().runAtFixedRate(TheAnnihilation.getInstance(), task -> {
            short foodTickTimer = player.getPersistentDataContainer().getOrDefault(CUSTOM_FOOD_TICK_TIMER, PersistentDataType.SHORT, (short) 0);
            if (player.getHealth() < player.getAttribute(Attribute.MAX_HEALTH).getValue()) {
                if (player.getSaturation() > 0 && player.getFoodLevel() >= 20) {
                    foodTickTimer++;
                    if (foodTickTimer >= 10) {
                        float min = Math.min(player.getSaturation(), 6.0f);
                        player.heal(min / 6.0f, EntityRegainHealthEvent.RegainReason.SATIATED);
                        causeFoodExhaustion(player, min, EntityExhaustionEvent.ExhaustionReason.REGEN);
                        foodTickTimer = 0;
                    }
                    player.getPersistentDataContainer().set(CUSTOM_FOOD_TICK_TIMER, PersistentDataType.SHORT, foodTickTimer);
                    return;
                }
                if (player.getFoodLevel() >= 14) {
                    foodTickTimer++;
                    if (foodTickTimer >= 40) {
                        player.heal(1.0f, EntityRegainHealthEvent.RegainReason.SATIATED);
                        causeFoodExhaustion(player, 3.0f, EntityExhaustionEvent.ExhaustionReason.REGEN);
                        foodTickTimer = 0;
                    }
                    player.getPersistentDataContainer().set(CUSTOM_FOOD_TICK_TIMER, PersistentDataType.SHORT, foodTickTimer);
                    return;
                }
            }
            player.getPersistentDataContainer().set(CUSTOM_FOOD_TICK_TIMER, PersistentDataType.SHORT, (short) 0);
        }, null, 1, 1);
        player.getScheduler().runAtFixedRate(TheAnnihilation.getInstance(), task -> {
            if (player.hasActiveItem()) {
                final ItemStack item = player.getActiveItem();
                item.editPersistentDataContainer(data -> {
                    if (data.has(CUSTOM_ITEM_USAGE, PersistentDataType.BYTE)) {
                        final int now = Bukkit.getCurrentTick();
                        final int usedTime;
                        final int startTime = data.getOrDefault(CUSTOM_ITEM_START_USE, PersistentDataType.INTEGER, -1);
                        final int vanillaEffectiveUseDuration = getVanillaEffectiveUseDuration(item, player), customEffectiveUseDuration = getCustomEffectiveUseDuration(item, player);
                        if (startTime < 0 || (now - startTime - 1) * vanillaEffectiveUseDuration / customEffectiveUseDuration + 1 != player.getActiveItemUsedTime()) {
                            data.set(CUSTOM_ITEM_START_USE, PersistentDataType.INTEGER, now - (usedTime = player.getActiveItemUsedTime()));
                        } else {
                            usedTime = now - startTime;
                        }
                        player.setActiveItemRemainingTime(item.getMaxItemUseDuration(player) - usedTime * vanillaEffectiveUseDuration / customEffectiveUseDuration);
                        switch (data.get(CUSTOM_ITEM_USAGE, PersistentDataType.BYTE)) {
                            case CUSTOM_ITEM_USAGE_RANGED_WEAPON -> {
                                item.setData(DataComponentTypes.CUSTOM_MODEL_DATA, CustomModelData.customModelData().build());
                            }
                            case null, default -> {}
                        }
                    }
                });
            }
        }, null, 1, 1);
    }
    private static void causeFoodExhaustion(HumanEntity human, float exhaustion, EntityExhaustionEvent.ExhaustionReason reason) {
        if (!human.isInvulnerable()) {
            @SuppressWarnings("UnstableApiUsage") final EntityExhaustionEvent event = new EntityExhaustionEvent(human, reason, exhaustion);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                human.setExhaustion(human.getExhaustion() + exhaustion);
            }
        }
    }
    private static int getVanillaEffectiveUseDuration(ItemStack item, LivingEntity entity) {
        return switch (item.getType()) {
            case BOW -> 20;
            case CROSSBOW -> 25 - item.getEnchantmentLevel(Enchantment.QUICK_CHARGE) * 5;
            case TRIDENT -> 10;
            default -> item.getMaxItemUseDuration(entity);
        };
    }
    public static int getCustomEffectiveUseDuration(ItemStack item, LivingEntity entity) {
        final PersistentDataContainerView data = item.getPersistentDataContainer();
        return switch (data.getOrDefault(CUSTOM_ITEM_USAGE, PersistentDataType.BYTE, (byte) 0)) {
            case CUSTOM_ITEM_USAGE_RANGED_WEAPON -> Math.round(20 / data.get(RangedUtils.RANGED_WEAPON_CHARGE_SPEED, PersistentDataType.FLOAT));
            default -> getVanillaEffectiveUseDuration(item, entity);
        };
    }
    public static Vector REMOVE_VERTICAL = new Vector(1, 0, 1);
}

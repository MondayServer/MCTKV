package me.paperxiang.stormeye.utils;
import io.papermc.paper.math.BlockPosition;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;
import me.paperxiang.stormeye.StormEye;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.ItemStack;
public final class Utils {
    private static final BlockFace[] neighbors = new BlockFace[]{BlockFace.WEST, BlockFace.EAST, BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH, BlockFace.SOUTH};
    private static final NamespacedKey FLOPS = new NamespacedKey(StormEye.getInstance(), "flops");
    private static final NamespacedKey FLOPS_TRANSIENT = new NamespacedKey(StormEye.getInstance(), "flops_transient");
    private static final NamespacedKey HASHES = new NamespacedKey(StormEye.getInstance(), "hashes");
    private Utils() {}
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

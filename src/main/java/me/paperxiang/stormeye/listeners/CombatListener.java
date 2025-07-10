package me.paperxiang.stormeye.listeners;
import de.tr7zw.nbtapi.NBT;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.tags.BlockTypeTagKeys;
import io.papermc.paper.registry.tag.Tag;
import me.paperxiang.stormeye.StormEye;
import me.paperxiang.stormeye.utils.Utils;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
public final class CombatListener implements Listener {
    @SuppressWarnings("UnstableApiUsage")
    private static final Tag<BlockType> PROJECTILE_BREAKS = RegistryAccess.registryAccess().getRegistry(RegistryKey.BLOCK).getTag(BlockTypeTagKeys.create(Key.key("storm_eye", "projectile_breaks")));
    private static final CombatListener instance = new CombatListener();
    private CombatListener() {}
    public static void init() {
        Bukkit.getPluginManager().registerEvents(instance, StormEye.getInstance());
    }
    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        final Block block = event.getHitBlock();
        if (block != null && PROJECTILE_BREAKS.contains(TypedKey.create(RegistryKey.BLOCK, block.getType().getKey()))) {
            Utils.connected(block, switch (event.getEntityType()) {
                case ARROW, SPECTRAL_ARROW, TRIDENT -> 4;
                case EGG, ENDER_PEARL, LLAMA_SPIT, SNOWBALL -> 1;
                default -> 0;
            }).forEach(Block::breakNaturally);
            final Projectile projectile = event.getEntity();
            if (projectile instanceof final AbstractArrow arrow) {
                if (!(arrow instanceof Trident) && arrow.getPierceLevel() <= 0) {
                    arrow.remove();
                } else {
                    Bukkit.getScheduler().runTask(StormEye.getInstance(), () -> NBT.modify(arrow, nbt -> {
                        nbt.setBoolean("inGround", false);
                    }));
                }
            }
        }
    }
}

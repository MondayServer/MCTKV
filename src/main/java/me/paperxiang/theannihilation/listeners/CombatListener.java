package me.paperxiang.theannihilation.listeners;
import de.tr7zw.nbtapi.NBT;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import io.papermc.paper.registry.TypedKey;
import io.papermc.paper.registry.keys.DamageTypeKeys;
import io.papermc.paper.registry.keys.tags.BlockTypeTagKeys;
import io.papermc.paper.registry.keys.tags.DamageTypeTagKeys;
import io.papermc.paper.registry.tag.Tag;
import java.util.Optional;
import me.paperxiang.theannihilation.TheAnnihilation;
import me.paperxiang.theannihilation.utils.MeleeUtils;
import me.paperxiang.theannihilation.utils.Utils;
import net.kyori.adventure.key.Key;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.BlockType;
import org.bukkit.damage.DamageSource;
import org.bukkit.damage.DamageType;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ThrowableProjectile;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
public final class CombatListener implements Listener {
    @SuppressWarnings("UnstableApiUsage")
    private static final Tag<BlockType> PROJECTILE_BREAKS = RegistryAccess.registryAccess().getRegistry(RegistryKey.BLOCK).getTag(BlockTypeTagKeys.create(Key.key("the_annihilation", "projectile_breaks")));
    @SuppressWarnings("UnstableApiUsage")
    private static final Tag<DamageType> INTERRUPTS_CONSUMPTION = RegistryAccess.registryAccess().getRegistry(RegistryKey.DAMAGE_TYPE).getTag(DamageTypeTagKeys.create(Key.key("the_annihilation", "interrupts_consumption")));
    @SuppressWarnings("UnstableApiUsage")
    private static final Tag<DamageType> BYPASSES_COOLDOWN = RegistryAccess.registryAccess().getRegistry(RegistryKey.DAMAGE_TYPE).getTag(DamageTypeTagKeys.create(Key.key(Key.MINECRAFT_NAMESPACE, "bypasses_cooldown")));
    private static final CombatListener instance = new CombatListener();
    private CombatListener() {}
    public static void init() {
        Bukkit.getPluginManager().registerEvents(instance, TheAnnihilation.getInstance());
    }
    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof ThrowableProjectile) {
            if (event.getHitEntity() instanceof final LivingEntity living) {
                final Vector v = event.getEntity().getVelocity();
                living.knockback(1, -v.getX(), -v.getZ());
            }
        }
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
                    Bukkit.getScheduler().runTask(TheAnnihilation.getInstance(), () -> NBT.modify(arrow, nbt -> {
                        nbt.setBoolean("inGround", false);
                    }));
                }
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction().isLeftClick()) {
            final Player player = event.getPlayer();
            if (MeleeUtils.canSweep(player)) {
                MeleeUtils.sweepAttack(player, null);
            }
        }
    }
    @SuppressWarnings("UnstableApiUsage")
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof final LivingEntity living) {
            final DamageSource source = event.getDamageSource();
            final DamageType damageType = source.getDamageType();
            final TypedKey<DamageType> damageTypeKey = DamageTypeKeys.create(damageType.key());
            final ItemStack activeItem = living.getActiveItem();
            if (BYPASSES_COOLDOWN.contains(damageTypeKey)) {
                living.setMaximumNoDamageTicks(0);
            } else if (damageType == DamageType.PLAYER_ATTACK) {
                living.setMaximumNoDamageTicks(Math.clamp((int) (16 / ((LivingEntity) source.getDirectEntity()).getAttribute(Attribute.ATTACK_SPEED).getValue()), 4, 10));
                Utils.markMaximumNoDamageTicksModified(living.getUniqueId());
            } else {
                living.setMaximumNoDamageTicks(10);
                Utils.markMaximumNoDamageTicksModified(living.getUniqueId());
            }
            if (INTERRUPTS_CONSUMPTION.contains(damageTypeKey) && activeItem.hasData(DataComponentTypes.FOOD)) {
                living.clearActiveItem();
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityCombust(EntityCombustEvent event) {
        if (event.getDuration() < 0 && !event.isCancelled()) {
            event.setCancelled(true);
            final Entity entity = event.getEntity();
            if (entity instanceof final LivingEntity living) {
                if (Optional.ofNullable(living.getEquipment()).map(EntityEquipment::getBoots).filter(item -> item.getType() == Material.LEATHER_BOOTS).isEmpty()) {
                    entity.setFreezeTicks(Math.max(entity.getFreezeTicks(), -Math.round(event.getDuration() * Bukkit.getServerTickManager().getTickRate())));
                }
            }
        }
    }
}

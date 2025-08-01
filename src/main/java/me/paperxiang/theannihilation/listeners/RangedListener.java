package me.paperxiang.theannihilation.listeners;
import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import io.papermc.paper.event.entity.EntityLoadCrossbowEvent;
import io.papermc.paper.persistence.PersistentDataContainerView;
import java.util.Optional;
import me.paperxiang.theannihilation.TheAnnihilation;
import me.paperxiang.theannihilation.utils.RangedUtils;
import me.paperxiang.theannihilation.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.util.Vector;
public final class RangedListener implements Listener {
    private static final RangedListener instance = new RangedListener();
    private RangedListener() {}
    public static void init() {
        Bukkit.getPluginManager().registerEvents(instance, TheAnnihilation.getInstance());
    }
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityLoadCrossbow(EntityLoadCrossbowEvent event) {
        Bukkit.getScheduler().runTask(TheAnnihilation.getInstance(), () -> {
            event.getEntity().completeUsingActiveItem();
            event.getEntity().completeUsingActiveItem();
        });
    }
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityShootBow(EntityShootBowEvent event) {
        final ItemStack bow = event.getBow();
        final Entity projectile = event.getProjectile();
        if (bow != null) {
            bow.editPersistentDataContainer(data -> {
                if (data.getOrDefault(Utils.CUSTOM_ITEM_USAGE, PersistentDataType.BYTE, (byte) 0) == Utils.CUSTOM_ITEM_USAGE_RANGED_WEAPON) {
                    final float charge = switch (bow.getType()) {
                        case CROSSBOW -> 0;
                        default -> data.get(RangedUtils.RANGED_WEAPON_CHARGE_SPEED, PersistentDataType.FLOAT) * (Bukkit.getCurrentTick() - data.get(Utils.CUSTOM_ITEM_START_USE, PersistentDataType.INTEGER)) / 20;
                    };
                    projectile.setVelocity(RangedUtils.shootVelocity(event.getEntity(), data.get(RangedUtils.RANGED_WEAPON_PROJECTILE_VELOCITY, PersistentDataType.FLOAT) * RangedUtils.power(bow.getType(), charge), data.get(RangedUtils.RANGED_WEAPON_ACCURACY, PersistentDataType.FLOAT) * RangedUtils.fatigue(bow.getType(), charge)));
                    if (projectile instanceof final AbstractArrow arrow) {
                        arrow.setCritical(RangedUtils.crit(bow.getType(), charge));
                    }
                }
                data.remove(Utils.CUSTOM_ITEM_START_USE);
            });//todo item model?
            //Bukkit.getScheduler().runTask(Charcoal.getInstance(), () -> bow.editMeta(meta -> meta.setCustomModelData(PUtils.getCustomModelData(bow, event.getEntity(), -1))));
            final int extraProjectileCount = bow.getEnchantmentLevel(Enchantment.MULTISHOT) * 2;
        }
        if (projectile instanceof final AbstractArrow arrow) {
            final PersistentDataContainer data = event.getEntity().getPersistentDataContainer();
            final double power;
            if (data.has(Utils.CUSTOM_PROPERTIES, PersistentDataType.TAG_CONTAINER)) {
                power = 1 + data.get(Utils.CUSTOM_PROPERTIES, PersistentDataType.TAG_CONTAINER).getOrDefault(Utils.CUSTOM_PROPERTY_POWER, PersistentDataType.FLOAT, 0f);
            } else {
                power = 1;
            }
            arrow.setDamage(arrow.getDamage() * power);
            arrow.setPickupStatus(AbstractArrow.PickupStatus.DISALLOWED);
            if(arrow instanceof Arrow arrow0 && arrow0.hasCustomEffects()) {
                for(final PotionEffect effect : arrow0.getCustomEffects()) {
                    if(!effect.isInfinite()) {
                        arrow0.addCustomEffect(effect.withDuration(Math.max(1, effect.getDuration() / 8)), true);
                    }
                }
            }
        }
    }
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerProjectileLaunch(PlayerLaunchProjectileEvent event) {
        final ItemStack item = event.getItemStack();
        final PersistentDataContainerView data = item.getPersistentDataContainer();
        data.copyTo(event.getProjectile().getPersistentDataContainer(), false);
        if (data.getOrDefault(Utils.CUSTOM_ITEM_USAGE, PersistentDataType.BYTE, (byte) 0) == Utils.CUSTOM_ITEM_USAGE_RANGED_WEAPON) {
            event.getProjectile().setVelocity(RangedUtils.shootVelocity(event.getPlayer(), data.get(RangedUtils.RANGED_WEAPON_PROJECTILE_VELOCITY, PersistentDataType.FLOAT), data.get(RangedUtils.RANGED_WEAPON_ACCURACY, PersistentDataType.FLOAT)));
        }
    }
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onProjectileHit(ProjectileHitEvent event) {
        final Projectile projectile = event.getEntity();
        if (projectile instanceof Egg || projectile instanceof EnderPearl || projectile instanceof Snowball) {
            if (event.getHitEntity() instanceof final Player player && !player.isInvulnerable()) {
                final Vector projectileVelocity = projectile.getVelocity();
                double x = -projectileVelocity.getX(), z = -projectileVelocity.getZ(), length;
                while ((length = x * x + z * z) < 1e-5) {
                    x = (Math.random() - Math.random()) * 0.01;
                    z = (Math.random() - Math.random()) * 0.01;
                }
                final double strength = Optional.ofNullable(player.getAttribute(Attribute.KNOCKBACK_RESISTANCE)).map(attribute -> 1 - attribute.getValue()).orElse(1d);
                final Vector oldVelocity = player.getVelocity();
                player.setVelocity(new Vector(oldVelocity.getX() / 2 - x * strength / length, player.isOnGround() ? Math.min(0.4, oldVelocity.getY() / 2 + 1) : oldVelocity.getY(), oldVelocity.getZ() / 2 - z * strength / length));
            }
        }
    }
}

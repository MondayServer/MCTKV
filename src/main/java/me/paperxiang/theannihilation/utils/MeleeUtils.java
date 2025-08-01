package me.paperxiang.theannihilation.utils;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.damage.CraftDamageSource;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.damage.DamageSource;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.BoundingBox;
public final class MeleeUtils {
    private MeleeUtils() {}
    public static double calculateSweepingDamage(Player player) {
        final float cooldown = player.getAttackCooldown();
        return (player.getAttribute(Attribute.ATTACK_DAMAGE).getValue() * player.getAttribute(Attribute.SWEEPING_DAMAGE_RATIO).getValue() + 1) * (0.8 * cooldown * cooldown + 0.2);
    }
    public static boolean canSweep(Player player) {
        return Tag.ITEMS_SWORDS.isTagged(player.getEquipment().getItemInMainHand().getType()) && player.getAttackCooldown() > 0.9 && !player.isSprinting();
    }
    private static Collection<Entity> sweepTargets(Player player, Entity targeted) {
        final BoundingBox boundingBox = targeted == null ? player.getBoundingBox().expand(1, 0.25, 1).shift(Math.sin(Math.toRadians(player.getYaw())) * -2, 0, Math.cos(Math.toRadians(player.getYaw())) * 2) : targeted.getBoundingBox().expand(1, 0.25, 1);
        final UUID playerId = player.getUniqueId();
        final UUID targetedId = targeted == null ? null : targeted.getUniqueId();
        return player.getWorld().getNearbyEntities(boundingBox, entity -> entity instanceof LivingEntity && !entity.getUniqueId().equals(targetedId) && !entity.getUniqueId().equals(playerId) && (!(entity instanceof final ArmorStand armorStand) || !armorStand.isMarker()));
    }
    public static boolean hasSweepTargets(Player player, Entity targeted) {
        return sweepTargets(player, targeted).stream().anyMatch(entity -> !(entity instanceof final HumanEntity human && human.getGameMode() == GameMode.SPECTATOR));
    }
    public static void sweepAttack(Player player, Entity targeted) {
        final Collection<Entity> targets = sweepTargets(player, targeted);
        if (targets.isEmpty()) {
            return;
        }
        final double damage = calculateSweepingDamage(player);
        final ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        final DamageSource source = new CraftDamageSource(Optional.ofNullable(nmsPlayer.getWeaponItem().getItem().getDamageSource(nmsPlayer)).orElse(nmsPlayer.damageSources().playerAttack(nmsPlayer)).knownCause(EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK));
        for (final Entity entity : targets) {
            ((LivingEntity) entity).damage(damage, source);
        }
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 1, 1);
        final double yawRadians = Math.toRadians(player.getYaw());
        final double xOffset = -Math.sin(yawRadians), zOffset = Math.cos(yawRadians);
        player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, player.getX() + xOffset, player.getY() + 0.5 * player.getHeight(), player.getZ() + zOffset, 0, xOffset, 0, zOffset, 0);
    }
}

package me.paperxiang.theannihilation.utils;
import java.util.Random;
import me.paperxiang.theannihilation.TheAnnihilation;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
public final class RangedUtils {
    public static final NamespacedKey RANGED_WEAPON_PROJECTILE_VELOCITY = new NamespacedKey(TheAnnihilation.getInstance(), "projectile_velocity");
    public static final NamespacedKey RANGED_WEAPON_CHARGE_SPEED = new NamespacedKey(TheAnnihilation.getInstance(), "charge_speed");
    public static final NamespacedKey RANGED_WEAPON_ACCURACY = new NamespacedKey(TheAnnihilation.getInstance(), "accuracy");
    private static final Random random = new Random();
    private RangedUtils() {}
    public static Vector shootVelocity(Vector direction, float force, float accuracy) {
        final double randomOffset = 0.0075 / accuracy;
        return direction.clone().normalize().add(new Vector(random.nextGaussian() * randomOffset, random.nextGaussian() * randomOffset, random.nextGaussian() * randomOffset)).multiply(force);
    }
    public static Vector shootVelocity(LivingEntity entity, float force, float accuracy) {
        return shootVelocity(entity, force, accuracy, 0);
    }
    public static Vector shootVelocity(LivingEntity entity, float force, float accuracy, double angle) {
        final Vector direction = entity.getEyeLocation().getDirection();
        final Vector rotationAxis = direction.getY() == 0 ? new Vector(0, 1, 0) : direction.clone().setY((direction.getX() * direction.getX() + direction.getZ() * direction.getZ()) / direction.getY()).normalize();
        return shootVelocity(direction.rotateAroundAxis(rotationAxis, angle), force, accuracy).add(entity.getVelocity().multiply(Utils.REMOVE_VERTICAL));
    }
    public static float power(Material material, float charge) {
        charge = Math.min(charge, 1);
        return switch (material) {
            case BOW -> charge * (charge + 2) / 3;
            default -> 1;
        };
    }
    public static float fatigue(Material material, float charge) {
        return switch (material) {
            case BOW -> charge > 1 ? 10 / (1 + (float) Math.exp(-1.5f * charge + 7.5f)) + 0.5f : 4 * charge * (charge - 4) + 12.5f;
            default -> 1;
        };
    }
    public static boolean crit(Material material, float charge) {
        return switch (material) {
            case BOW -> charge >= 1 && charge <= 3;
            case CROSSBOW -> true;
            default -> false;
        };
    }
}

package me.paperxiang.stormeye.listeners;
import me.paperxiang.stormeye.StormEye;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
public final class InventoryListener implements Listener {
    private static final InventoryListener instance = new InventoryListener();
    private InventoryListener() {}
    public static void init() {
        Bukkit.getPluginManager().registerEvents(instance, StormEye.getInstance());
    }
}

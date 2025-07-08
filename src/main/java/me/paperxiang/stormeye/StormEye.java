package me.paperxiang.stormeye;
import me.paperxiang.stormeye.listeners.CombatListener;
import me.paperxiang.stormeye.listeners.InventoryListener;
import me.paperxiang.stormeye.listeners.PlayerListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
public final class StormEye extends JavaPlugin {
    private static StormEye instance;
    private static Logger logger;
    @Override
    public void onLoad() {
        logger = getComponentLogger();
        instance = this;
        saveDefaultConfig();
    }
    @Override
    public void onEnable() {
        CombatListener.init();
        InventoryListener.init();
        PlayerListener.init();
        logInfo("StormEye initialized.");
    }
    @Override
    public void onDisable() {
        instance = null;
        logInfo("StormEye finalized.");
    }
    public static StormEye getInstance() {
        return instance;
    }
    public static void logInfo(final String info) {
        if (logger == null) {
            StormEyeBootstrap.logInfo(info);
            return;
        }
        logger.info(info);
    }
    public static void logWarning(final String warning) {
        if (logger == null) {
            StormEyeBootstrap.logWarning(warning);
            return;
        }
        logger.warn(warning);
    }
    public static void logError(final String error) {
        if (logger == null) {
            StormEyeBootstrap.logError(error);
            return;
        }
        logger.error(error);
    }
}

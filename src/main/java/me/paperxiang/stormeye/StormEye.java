package me.paperxiang.stormeye;
import fr.mrmicky.fastinv.FastInvManager;
import me.paperxiang.stormeye.i18n.I18n;
import me.paperxiang.stormeye.i18n.StormTranslator;
import me.paperxiang.stormeye.listeners.CombatListener;
import me.paperxiang.stormeye.listeners.InventoryListener;
import me.paperxiang.stormeye.listeners.MissionListener;
import me.paperxiang.stormeye.listeners.PlayerListener;
import me.paperxiang.stormeye.listeners.WorldListener;
import me.paperxiang.stormeye.utils.InventoryUtils;
import me.paperxiang.stormeye.utils.Mission;
import me.paperxiang.stormeye.utils.Utils;
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
        FastInvManager.register(this);
        I18n.init();
        StormTranslator.init();
        Utils.init();
        InventoryUtils.init();
        CombatListener.init();
        InventoryListener.init();
        MissionListener.init();
        PlayerListener.init();
        WorldListener.init();
        logInfo("StormEye initialized.");
        System.out.println(Utils.HUB);
    }
    @Override
    public void onDisable() {
        Mission.fina();
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

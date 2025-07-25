package me.paperxiang.theannihilation;
import fr.mrmicky.fastinv.FastInvManager;
import me.paperxiang.theannihilation.i18n.I18n;
import me.paperxiang.theannihilation.i18n.TATranslator;
import me.paperxiang.theannihilation.listeners.CombatListener;
import me.paperxiang.theannihilation.listeners.InventoryListener;
import me.paperxiang.theannihilation.listeners.MissionListener;
import me.paperxiang.theannihilation.listeners.PlayerListener;
import me.paperxiang.theannihilation.listeners.WorldListener;
import me.paperxiang.theannihilation.utils.InventoryUtils;
import me.paperxiang.theannihilation.utils.Mission;
import me.paperxiang.theannihilation.utils.Utils;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
public final class TheAnnihilation extends JavaPlugin {
    private static TheAnnihilation instance;
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
        TATranslator.init();
        Utils.init();
        InventoryUtils.init();
        CombatListener.init();
        InventoryListener.init();
        MissionListener.init();
        PlayerListener.init();
        WorldListener.init();
        logInfo("TheAnnihilation initialized.");
    }
    @Override
    public void onDisable() {
        Mission.fina();
        instance = null;
        logInfo("TheAnnihilation finalized.");
    }
    public static TheAnnihilation getInstance() {
        return instance;
    }
    public static void logInfo(final String info) {
        if (logger == null) {
            TheAnnihilationBootstrap.logInfo(info);
            return;
        }
        logger.info(info);
    }
    public static void logWarning(final String warning) {
        if (logger == null) {
            TheAnnihilationBootstrap.logWarning(warning);
            return;
        }
        logger.warn(warning);
    }
    public static void logError(final String error) {
        if (logger == null) {
            TheAnnihilationBootstrap.logError(error);
            return;
        }
        logger.error(error);
    }
}

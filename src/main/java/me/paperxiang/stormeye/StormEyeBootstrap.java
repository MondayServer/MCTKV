package me.paperxiang.stormeye;
import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.paperxiang.stormeye.utils.function.ThrowableSupplier;
import org.slf4j.Logger;
@SuppressWarnings("UnstableApiUsage")
public final class StormEyeBootstrap implements PluginBootstrap {
    private static BootstrapContext context;
    private static Logger logger;
    @Override
    public void bootstrap(BootstrapContext bootstrapContext) {
        logger = (context = bootstrapContext).getLogger();
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.DATAPACK_DISCOVERY, datapack -> ThrowableSupplier.supplyAndRunThrowable(() -> StormEyeBootstrap.class.getResource("/data").toURI(), uri -> datapack.registrar().discoverPack(context.getPluginMeta(), uri, "storm_eye", configurer -> configurer.autoEnableOnServerStart(true))));
        logInfo("StormEye bootstrapped.");
    }
    public static BootstrapContext getContext() {
        return context;
    }
    static void logInfo(final String info) {
        if (logger == null) {
            System.out.println(info);
            return;
        }
        logger.info(info);
    }
    static void logWarning(final String warning) {
        if (logger == null) {
            System.err.println(warning);
            return;
        }
        logger.warn(warning);
    }
    static void logError(final String error) {
        if (logger == null) {
            System.err.println(error);
            return;
        }
        logger.error(error);
    }
}

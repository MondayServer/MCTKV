package me.paperxiang.stormeye.i18n;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.paperxiang.stormeye.StormEye;
import me.paperxiang.stormeye.utils.ComponentUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.error.YAMLException;
public final class I18n {
    private static final ConcurrentHashMap<Locale, Map<String, Component>> translations = new ConcurrentHashMap<>();
    private static final Map<String, Component> EMPTY_STRING_COMPONENT_MAP = new HashMap<>(0);
    private static final Pattern LOCALE_PATTERN = Pattern.compile("locale(_[a-zA-Z]{2,8})?(_[a-zA-Z]{2}|[0-9]{3})?");
    private I18n() {}
    public static void init() {
        loadI18ns(StormEye.getInstance());
    }
    public static void writeDefaultConfigurableI18ns(JavaPlugin plugin) {
        final Path configurableI18nsPath = plugin.getDataFolder().toPath().resolve("configurable_locales.yml");
        try (final InputStream stream = plugin.getResource("configurable_locales.yml")) {
            Files.copy(stream, configurableI18nsPath);
        } catch (FileAlreadyExistsException ignored) {} catch (NullPointerException | IOException exception) {
            StormEye.logError("Error writing default configurable i18ns: " + exception.getMessage());
        }
        try {
            final YamlConfiguration configurableI18ns = YamlConfiguration.loadConfiguration(Files.newBufferedReader(configurableI18nsPath, StandardCharsets.UTF_8));
            final Path configurableI18nsFolder = plugin.getDataFolder().toPath().resolve("configurable_locales");
            try {
                Files.createDirectory(configurableI18nsFolder);
            } catch (FileAlreadyExistsException ignored) {}
            final Consumer<String> copyConfigurableI18n = i18n -> {
                try (final InputStream stream = plugin.getResource("configurable_locales/" + i18n + ".yml")) {
                    Files.copy(stream, configurableI18nsFolder.resolve(i18n + ".yml"));
                } catch (FileAlreadyExistsException ignored) {} catch (NullPointerException | IOException exception) {
                    StormEye.logError("Error writing default configurable i18ns: " + exception.getMessage());
                }
            };
            configurableI18ns.getStringList("locales").forEach(copyConfigurableI18n);
        } catch (NullPointerException | IOException exception) {
            StormEye.logError("Error writing default configurable i18ns: " + exception.getMessage());
        }
    }
    public static void loadI18ns(JavaPlugin plugin) {
        loadI18ns(YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("locales.yml"), StandardCharsets.UTF_8)), i18n -> YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("locales/" + i18n + ".yml"), StandardCharsets.UTF_8)));
    }
    public static void loadConfigurableI18ns(JavaPlugin plugin) {
        loadI18ns(YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "configurable_locales.yml")), i18n -> YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "configurable_locales" + File.separatorChar + i18n + ".yml")));
    }
    private static void loadI18ns(YamlConfiguration i18ns, Function<String, YamlConfiguration> getI18n) {
        try {
            i18ns.getStringList("locales").forEach(path -> {
                Matcher matcher = LOCALE_PATTERN.matcher(path);
                if (matcher.matches()) {
                    try {
                        final Locale locale = I18n.locale(Optional.ofNullable(matcher.group(1)).flatMap(language -> Optional.of(language.substring(1))).orElse(null), Optional.ofNullable(matcher.group(2)).flatMap(country -> Optional.of(country.substring(1))).orElse(null));
                        getI18n.apply(path).getValues(true).forEach((key, translation) -> registerTranslation(locale, key, ComponentUtils.parse(Objects.toString(translation, null))));
                    } catch (NullPointerException | YAMLException exception) {
                        StormEye.logError("Error loading locale: " + exception.getMessage());
                    }
                } else {
                    StormEye.logError("Error loading locale '" + path + "': Could not match locale");
                }
            });
        } catch (NullPointerException | YAMLException exception) {
            StormEye.logError("Error loading locales: " + exception.getMessage());
        }
    }
    public static void registerTranslation(Locale locale, String key, Component translation) {
        translations.putIfAbsent(locale, new ConcurrentHashMap<>());
        translations.get(locale).put(key, translation);
        StormTranslator.registerTranslation(locale, key, translation);
    }
    public static void sendMessage(CommandSender sender, String key, TranslationArgument... arguments) {
        sender.sendMessage(GlobalTranslator.render(Component.translatable(key, arguments), sender instanceof final Player player ? player.locale() : Locale.ROOT));
    }
    public static boolean isRoot(Locale locale) {
        return Locale.ROOT.equals(locale);
    }
    public static Locale fallback(Locale locale) {
        if (!locale.getCountry().isEmpty()) {
            return Locale.of(locale.getLanguage());
        }
        return Locale.ROOT;
    }
    public static Locale locale(String language, String country) {
        if (language == null) {
            return Locale.ROOT;
        }
        if (country == null) {
            return Locale.of(language);
        }
        return Locale.of(language, country);
    }
}

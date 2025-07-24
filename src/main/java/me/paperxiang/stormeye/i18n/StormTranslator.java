package me.paperxiang.stormeye.i18n;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.TranslationArgument;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.renderer.ComponentRenderer;
import net.kyori.adventure.text.renderer.TranslatableComponentRenderer;
import net.kyori.adventure.translation.GlobalTranslator;
import net.kyori.adventure.translation.Translator;
import net.kyori.adventure.util.TriState;
import org.jetbrains.annotations.NotNull;
public final class StormTranslator implements Translator {
    private static final Key NAME = Key.key("storm_eye", "main");
    private static final Map<String, Component> EMPTY_STRING_COMPONENT_MAP = Map.of();
    private static final ConcurrentHashMap<Locale, Map<String, Component>> translations = new ConcurrentHashMap<>();
    private static final StormTranslator instance = new StormTranslator();
    private static final ComponentRenderer<Locale> renderer = TranslatableComponentRenderer.usingTranslationSource(instance);
    private StormTranslator() {}
    public static void init() {
        GlobalTranslator.translator().addSource(instance);
    }
    public static void registerTranslation(Locale locale, String key, Component value) {
        translations.putIfAbsent(locale, new ConcurrentHashMap<>());
        translations.get(locale).put(key, value);
    }
    @Override
    public @NotNull Key name() {
        return NAME;
    }
    @Override
    public TriState hasAnyTranslations() {
        return translations.isEmpty() ? TriState.FALSE : TriState.TRUE;
    }
    @Override
    public MessageFormat translate(@NotNull String key, @NotNull Locale locale) {
        return null;
    }
    @Override
    public Component translate(@NotNull TranslatableComponent component, @NotNull Locale locale) {
        final Style style = translate(component.style(), locale);
        final String key = component.key();
        Map<String, Component> translations0;
        while (!(translations0 = translations.getOrDefault(locale, EMPTY_STRING_COMPONENT_MAP)).containsKey(key) && !I18n.isRoot(locale)) {
            locale = I18n.fallback(locale);
        }
        Component translation = translations0.get(key);
        if (translation != null) {
            for (int i = 0; i < component.arguments().size(); i++) {
                final TranslationArgument argument = component.arguments().get(i);
                translation = translation.replaceText(TextReplacementConfig.builder().matchLiteral("{" + i + "}").replacement(argument.value() instanceof TranslatableComponent translatable ? translate(translatable, locale) : argument).build());
            }
            return translation.applyFallbackStyle(style);
        }
        return translation;
    }
    private static Style translate(Style style, Locale locale) {
        if (style.hoverEvent() != null) {
            style = style.hoverEvent(style.hoverEvent().withRenderedValue(renderer, locale));
        }
        return style;
    }
}

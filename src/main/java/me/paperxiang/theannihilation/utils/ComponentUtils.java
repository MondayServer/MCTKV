package me.paperxiang.theannihilation.utils;
import it.unimi.dsi.fastutil.floats.Float2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectSortedMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectSortedMaps;
import it.unimi.dsi.fastutil.floats.FloatBidirectionalIterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.paperxiang.theannihilation.TheAnnihilation;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
public final class ComponentUtils {
    private ComponentUtils() {}
    public static void init() {
        Placeholders.instance.register();
    }
    public static Component parse(String string) {
        try {
            return MiniMessage.miniMessage().deserializeOrNull(string);
        } catch (ParsingException exception) {
            return LegacyComponentSerializer.legacySection().deserializeOrNull(string);
        }
    }
    public static Component renderProgressBar(String segment, int segments, TextColor color, TextColor progressedColor, float progress) {
        return new ProgressBar(segment, segments, color, Float2ObjectSortedMaps.singleton(progress, progressedColor)).render(progress);
    }
    public record ProgressBar(String segment, int segments, TextColor color, Float2ObjectSortedMap<TextColor> progressions) {
        public Component render(float progress) {
            final Float2ObjectSortedMap<TextColor> progressedColors = progressions;
            final TextComponent.Builder component = Component.text();
            int prevProgressedSegmentsWhole = 0;
            float prevProgressedSegmentsRemainder = 0;
            TextColor prevColor = null;
            for (final FloatBidirectionalIterator it = progressedColors.keySet().iterator(); it.hasNext(); ) {
                final float progressed = it.nextFloat();
                final float progressedSegments = Math.min(progress, progressed) * segments;
                final int progressedSegmentsWhole = (int) Math.floor(progressedSegments);
                final float progressedSegmentsRemainder = progressedSegments - progressedSegmentsWhole;
                final TextColor curColor = progressedColors.get(progressed);
                prevColor = prevColor == null ? curColor : TextColor.lerp(prevProgressedSegmentsRemainder / (progressedSegmentsWhole == prevProgressedSegmentsWhole ? progressedSegmentsRemainder : 1), curColor, prevColor);
                if (progressedSegmentsWhole > prevProgressedSegmentsWhole) {
                    component.append(Component.text(segment, prevColor));
                    component.append(Component.text(segment.repeat(progressedSegmentsWhole - prevProgressedSegmentsWhole - 1), curColor));
                }
                prevProgressedSegmentsRemainder = progressedSegmentsRemainder;
                prevProgressedSegmentsWhole = progressedSegmentsWhole;
            }
            if (segments > prevProgressedSegmentsWhole) {
                prevColor = prevColor == null ? color : TextColor.lerp(prevProgressedSegmentsRemainder, color, prevColor);
                component.append(Component.text(segment, prevColor));
                component.append(Component.text(segment.repeat(segments - prevProgressedSegmentsWhole - 1), color));
            }
            return component.build().compact();
        }
    }
    private static final class Placeholders extends PlaceholderExpansion {
        private static final Pattern PROGRESS_BAR_PLACEHOLDER = Pattern.compile("^progress_bar\\{(.+);([-+]?(?:\\d+\\.?\\d*|\\.\\d+)(?:[eE][-+]?\\d+)?)}$");
        private static final Pattern PROGRESS_BAR = Pattern.compile("^(.+);(\\+?[1-9]\\d*);(#[\\dA-Fa-f]{6}|black|dark_blue|dark_green|dark_aqua|dark_red|dark_purple|gold|gray|dark_gray|blue|green|aqua|red|light_purple|yellow|white)((?:;(?:#[\\dA-Fa-f]{6}|black|dark_blue|dark_green|dark_aqua|dark_red|dark_purple|gold|gray|dark_gray|blue|green|aqua|red|light_purple|yellow|white),([-+]?(?:\\d+\\.?\\d*|\\.\\d+)(?:[eE][-+]?\\d+)?))*)$");
        private static final ConcurrentHashMap<String, ProgressBar> parsedProgressBars = new ConcurrentHashMap<>();
        private static final MiniMessage miniMessage = MiniMessage.miniMessage();
        public static final Placeholders instance = new Placeholders();
        private Placeholders() {}
        @Override
        public @Nullable String onRequest(OfflinePlayer player, @NotNull String params) {
            System.out.println("Parsing " + params);
            final Matcher progressBarPlaceholderMatcher = PROGRESS_BAR_PLACEHOLDER.matcher(params);
            if (progressBarPlaceholderMatcher.matches()) {
                final String progressBarString = progressBarPlaceholderMatcher.group(1);
                final float progress;
                try {
                    progress = Float.parseFloat(progressBarPlaceholderMatcher.group(2));
                } catch (NumberFormatException exception) {
                    return null;
                }
                final ProgressBar progressBar;
                if (parsedProgressBars.containsKey(progressBarString)) {
                    progressBar = parsedProgressBars.get(progressBarString);
                } else {
                    final Matcher progressBarMatcher = PROGRESS_BAR.matcher(progressBarString);
                    if (progressBarMatcher.matches()) {
                        final String segment = progressBarMatcher.group(1);
                        final int segments;
                        try {
                            segments = Integer.parseInt(progressBarMatcher.group(2));
                        } catch (NumberFormatException exception) {
                            return null;
                        }
                        final TextColor color = parseTextColor(progressBarMatcher.group(3));
                        if (color == null) {
                            return null;
                        }
                        final Float2ObjectSortedMap<TextColor> progressions = new Float2ObjectAVLTreeMap<>();
                        final String[] progressionStrings = progressBarMatcher.group(4).split(";");
                        for (int i = 1; i < progressionStrings.length; i++) {
                            final String[] progressionData = progressionStrings[i].split(",");
                            final TextColor color0 = parseTextColor(progressionData[0]);
                            if (color0 == null) {
                                return null;
                            }
                            final float progress0;
                            try {
                                progress0 = Float.parseFloat(progressionData[1]);
                            } catch (NumberFormatException exception) {
                                return null;
                            }
                            progressions.put(progress0, color0);
                        }
                        parsedProgressBars.put(progressBarString, progressBar = new ProgressBar(segment, segments, color, progressions));
                    } else {
                        return null;
                    }
                }
                return miniMessage.serializeOrNull(progressBar.render(progress));
            }
            return null;
        }
        @Override
        public @NotNull List<String> getPlaceholders() {
            return super.getPlaceholders();
        }
        @Override
        public @NotNull String getIdentifier() {
            return "component-utils";
        }
        @Override
        public @NotNull String getAuthor() {
            return String.join(", ", TheAnnihilation.getInstance().getPluginMeta().getAuthors());
        }
        @Override
        public @NotNull String getVersion() {
            return TheAnnihilation.getInstance().getPluginMeta().getVersion();
        }
        @Override
        public boolean persist() {
            return true;
        }
        private static TextColor parseTextColor(String string) {
            return string.startsWith("#") ? TextColor.fromCSSHexString(string) : NamedTextColor.NAMES.value(string);
        }
    }
}

package me.paperxiang.stormeye.utils;
import it.unimi.dsi.fastutil.floats.Float2ObjectSortedMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectSortedMaps;
import it.unimi.dsi.fastutil.floats.FloatBidirectionalIterator;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
public final class ComponentUtils {
    private ComponentUtils() {}
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
}

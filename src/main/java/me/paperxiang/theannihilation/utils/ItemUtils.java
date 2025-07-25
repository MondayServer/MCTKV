package me.paperxiang.theannihilation.utils;
import java.util.Optional;
import me.paperxiang.theannihilation.TheAnnihilation;
import net.momirealms.craftengine.bukkit.api.CraftEngineItems;
import net.momirealms.craftengine.core.item.Item;
import net.momirealms.craftengine.core.item.ItemBuildContext;
import net.momirealms.craftengine.core.util.Key;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
public final class ItemUtils {
    private static final NamespacedKey CUSTOM_ITEM_KEY = new NamespacedKey(TheAnnihilation.getInstance(), "custom_item");
    private ItemUtils() {}
    public static ItemStack generateCustom(ItemStack source, ItemBuildContext context) {
        if (source != null && source.getType() == Material.STRUCTURE_VOID && source.getPersistentDataContainer().has(CUSTOM_ITEM_KEY, PersistentDataType.STRING)) {
            return Optional.ofNullable(source.getPersistentDataContainer().get(CUSTOM_ITEM_KEY, PersistentDataType.STRING)).map(key -> CraftEngineItems.byId(Key.of(key))).map(item -> item.buildItem(context)).map(Item::getItem).orElse(null);
        }
        return source;
    }
}

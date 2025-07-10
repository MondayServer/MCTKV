package me.paperxiang.stormeye.ui;
import java.util.function.Consumer;
import org.bukkit.event.inventory.InventoryClickEvent;
public final class UIUtils {
    public static final Consumer<InventoryClickEvent> CANCEL_CLICK = click -> click.setCancelled(true);
    private UIUtils() {}
}

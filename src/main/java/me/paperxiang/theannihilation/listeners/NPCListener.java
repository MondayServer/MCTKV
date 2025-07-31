package me.paperxiang.theannihilation.listeners;
import java.util.Optional;
import me.paperxiang.theannihilation.TheAnnihilation;
import me.paperxiang.theannihilation.utils.NPCUtils;
import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
public final class NPCListener implements Listener {
    private static final NPCListener instance = new NPCListener();
    private NPCListener() {}
    public static void init() {
        Bukkit.getPluginManager().registerEvents(instance, TheAnnihilation.getInstance());
    }
    @EventHandler(priority = EventPriority.HIGH)
	public void onNPCLeftClick(NPCLeftClickEvent event) {
        onNPCClick(event);
	}
    @EventHandler(priority = EventPriority.HIGH)
	public void onNPCRightClick(NPCRightClickEvent event) {
        onNPCClick(event);
	}
	private void onNPCClick(NPCClickEvent event) {
        Optional.ofNullable(event.getNPC().getTraitNullable(NPCUtils.ClickActionTrait.class)).ifPresent(trait -> trait.run(event.getClicker()));
	}
}

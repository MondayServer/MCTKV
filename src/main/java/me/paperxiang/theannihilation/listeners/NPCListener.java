package me.paperxiang.theannihilation.listeners;
import java.text.DecimalFormat;
import java.util.Optional;
import me.paperxiang.theannihilation.TheAnnihilation;
import me.paperxiang.theannihilation.utils.NPCUtils;
import net.citizensnpcs.api.event.NPCClickEvent;
import net.citizensnpcs.api.event.NPCDamageEvent;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslationArgument;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
public final class NPCListener implements Listener {
    private static final DecimalFormat DAMAGE_FORMAT = new DecimalFormat("0.00");
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
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageEvent event) {
        if (NPCUtils.transientNPCs.isNPC(event.getEntity())) {
            if (NPCUtils.transientNPCs.getNPC(event.getEntity()).hasTrait(NPCUtils.TargetDummyTrait.class)) {
                if (event.getDamageSource().getCausingEntity() instanceof final Player source) {
                    source.sendActionBar(Component.translatable("npc.target_dummy.damage", TranslationArgument.component(Component.text(DAMAGE_FORMAT.format(event.getDamage())))));
                }
            }
        }
    }
	private void onNPCClick(NPCClickEvent event) {
        Optional.ofNullable(event.getNPC().getTraitNullable(NPCUtils.ClickActionTrait.class)).ifPresent(trait -> trait.run(event.getClicker()));
	}
}

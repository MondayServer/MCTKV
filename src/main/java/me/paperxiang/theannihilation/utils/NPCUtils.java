package me.paperxiang.theannihilation.utils;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.function.Consumer;
import me.paperxiang.theannihilation.TheAnnihilation;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.api.trait.TraitInfo;
import net.citizensnpcs.api.trait.TraitName;
import net.citizensnpcs.api.trait.trait.Equipment;
import net.citizensnpcs.trait.SkinTrait;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
public final class NPCUtils {
    private static NPCRegistry transientNPCs;
    private NPCUtils() {}
    public static void init() {
        CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(ClickActionTrait.class));
        transientNPCs = CitizensAPI.createInMemoryNPCRegistry("server_npcs");
        final HashMap<String, NPC> npcs = new HashMap<>();
        final YamlConfiguration NPCsConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(TheAnnihilation.getInstance().getResource("npc.yml"), StandardCharsets.UTF_8));
        NPCsConfig.getValues(false).forEach((id, obj) -> {
            if (!(obj instanceof ConfigurationSection data)) {
                return;
            }
            final NPC npc = transientNPCs.createNPC(EntityType.fromName(data.getString("entity-type", "player")), data.getString("name", ""));
            npcs.put(id, npc);
            npc.setProtected(true);
            if (data.isConfigurationSection("skin")) {
                final ConfigurationSection skin = data.getConfigurationSection("skin");
                npc.getOrAddTrait(SkinTrait.class).setSkinPersistent(id, skin.getString("signature"), skin.getString("texture"));
            }
            if (data.isConfigurationSection("equipment")) {
                data.getConfigurationSection("equipment").getValues(false).forEach((slot, item) -> npc.getOrAddTrait(Equipment.class).set(Equipment.EquipmentSlot.valueOf(slot), Bukkit.getItemFactory().createItemStack(item.toString())));
            }
            if (data.isConfigurationSection("location")) {
                npc.spawn(Location.deserialize(data.getConfigurationSection("location").getValues(false)));
            }
        });
        final NPC selectMissionNPC = npcs.get("select_mission");
        selectMissionNPC.getOrAddTrait(ClickActionTrait.class).setAction(player -> player.chat("start"));
        selectMissionNPC.data().set(NPC.Metadata.USING_HELD_ITEM, true);
        final NPC lobbyNPC = npcs.get("lobby");
        lobbyNPC.getOrAddTrait(ClickActionTrait.class).setAction(player -> player.sendMessage("大厅"));
    }
    public static void fina() {
        transientNPCs.deregisterAll();
    }
    @TraitName("click_action")
    public static final class ClickActionTrait extends Trait {
        private Consumer<Player> action;
        public ClickActionTrait() {
            super("click_action");
        }
        public void setAction(Consumer<Player> action) {
            this.action = action;
        }
        public void run(Player player) {
            action.accept(player);
        }
    }
}

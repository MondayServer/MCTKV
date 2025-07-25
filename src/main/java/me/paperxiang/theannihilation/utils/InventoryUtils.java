package me.paperxiang.theannihilation.utils;
import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.ListenerOptions;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.destroystokyo.paper.loottable.LootableEntityInventory;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.paperxiang.theannihilation.TheAnnihilation;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.BlockInventoryHolder;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
public final class InventoryUtils {
    private static final ConcurrentHashMap<UUID, Window> openWindows = new ConcurrentHashMap<>();
    private static final ItemStack UNREVEALED;
    private static final ItemStack EMPTY = ItemStack.empty();
    private static final Random REVEAL_TIME_RANDOM = new Random();
    private static final int MIN_REVEAL_TIME = 3;
    private static final int MAX_REVEAL_TIME = 12;
    static {
        UNREVEALED = new ItemStack(Material.BARRIER);
        UNREVEALED.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true).build());
    }
    public static void init() {
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(TheAnnihilation.getInstance(), ListenerPriority.NORMAL, List.of(PacketType.Play.Server.OPEN_WINDOW), ListenerOptions.ASYNC) {
            @Override
            public void onPacketSending(PacketEvent event) {
                openWindows.get(event.getPlayer().getUniqueId()).open(event.getPacket().getIntegers().readSafely(0));
            }
        });
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(TheAnnihilation.getInstance(), ListenerPriority.NORMAL, List.of(PacketType.Play.Server.CLOSE_WINDOW), ListenerOptions.ASYNC) {
            @Override
            public void onPacketSending(PacketEvent event) {
                openWindows.get(event.getPlayer().getUniqueId()).open(0);
            }
        });
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(TheAnnihilation.getInstance(), ListenerPriority.NORMAL, List.of(PacketType.Play.Client.CLOSE_WINDOW), ListenerOptions.ASYNC) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                openWindows.get(event.getPlayer().getUniqueId()).open(0);
            }
        });
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(TheAnnihilation.getInstance(), ListenerPriority.NORMAL, List.of(PacketType.Play.Server.WINDOW_ITEMS, PacketType.Play.Server.SET_SLOT), ListenerOptions.SYNC) {
            @Override
            public void onPacketSending(PacketEvent event) {
                final Player player = event.getPlayer();
                if (!Mission.isInMission(player.getUniqueId())) {
                    return;
                }
                final PacketContainer packet = event.getPacket();
                final Window window = openWindows.get(player.getUniqueId());
                if (packet.getIntegers().readSafely(0) == window.id) {
                    window.updateState(packet.getIntegers().readSafely(1));
                    final Inventory inventory = player.getOpenInventory().getTopInventory();
                    if (mayHide(inventory)) {
                        if (packet.getType() == PacketType.Play.Server.WINDOW_ITEMS) {
                            final List<ItemStack> items = packet.getItemListModifier().readSafely(0);
                            for (int i = 0; i < inventory.getSize(); i++) {
                                if (!window.revealed(i) && !Utils.isEmpty(items.get(i))) {
                                    items.set(i, UNREVEALED);
                                }
                            }
                            packet.getItemListModifier().write(0, items);
                        } else {
                            if (!window.revealed(packet.getIntegers().readSafely(2)) && !Utils.isEmpty(packet.getItemModifier().readSafely(0))) {
                                packet.getItemModifier().write(0, UNREVEALED);
                            }
                        }
                    }
                }
            }
        });
        ProtocolLibrary.getProtocolManager().addPacketListener(new PacketAdapter(TheAnnihilation.getInstance(), ListenerPriority.NORMAL, List.of(PacketType.Play.Client.WINDOW_CLICK), ListenerOptions.SYNC) {
            @Override
            public void onPacketReceiving(PacketEvent event) {
                final Player player = event.getPlayer();
                if (!Mission.isInMission(player.getUniqueId())) {
                    return;
                }
                final PacketContainer packet = event.getPacket();
                final Window window = openWindows.get(player.getUniqueId());
                if (packet.getIntegers().readSafely(0) == window.id && packet.getIntegers().readSafely(1) == window.state) {
                    final int slot = packet.getShorts().readSafely(0);
                    final Inventory inventory = player.getOpenInventory().getTopInventory();
                    if (mayHide(inventory) && slot >= 0 && slot < inventory.getSize() && !window.revealed(slot)) {
                        event.setCancelled(true);
                        player.setItemOnCursor(player.getItemOnCursor());
                        final PacketContainer cancel = new PacketContainer(PacketType.Play.Server.SET_SLOT);
                        cancel.getIntegers().write(0, window.id);
                        cancel.getIntegers().write(1, window.state);
                        cancel.getIntegers().write(2, slot);
                        cancel.getItemModifier().write(0, Utils.isEmpty(inventory.getItem(slot)) ? EMPTY : UNREVEALED);
                        ProtocolLibrary.getProtocolManager().sendServerPacket(player, cancel);
                        window.scheduleReveal(slot, Bukkit.getScheduler().runTaskLaterAsynchronously(TheAnnihilation.getInstance(), () -> {
                            final PacketContainer reveal = new PacketContainer(PacketType.Play.Server.SET_SLOT);
                            reveal.getIntegers().write(0, window.id);
                            reveal.getIntegers().write(1, window.state);
                            reveal.getIntegers().write(2, slot);
                            reveal.getItemModifier().write(0, inventory.getItem(slot));
                            ProtocolLibrary.getProtocolManager().sendServerPacket(player, reveal);
                            window.reveal(slot);
                        }, REVEAL_TIME_RANDOM.nextInt(MIN_REVEAL_TIME, MAX_REVEAL_TIME)));
                    }
                }
            }
        });
    }
    private static boolean mayHide(Inventory inventory) {
        final InventoryHolder holder = inventory.getHolder();
        return holder instanceof BlockInventoryHolder || holder instanceof LootableEntityInventory;
    }
    private static final class Window {
        private int id = 0, state = 0;
        private final BitSet revealed = new BitSet();
        private final Int2ObjectOpenHashMap<BukkitTask> revealTasks = new Int2ObjectOpenHashMap<>();
        public void open(int id) {
            this.id = id;
            state = 0;
            revealed.clear();
            revealTasks.values().forEach(BukkitTask::cancel);
            revealTasks.clear();
        }
        public void updateState(int state) {
            this.state = state;
        }
        public void reveal(int slot) {
            revealed.set(slot);
        }
        public void scheduleReveal(int slot, BukkitTask task) {
            if (revealTasks.containsKey(slot)) {
                task.cancel();
            } else {
                revealTasks.put(slot, task);
            }
        }
        public boolean revealed(int slot) {
            return revealed.get(slot);
        }
    }
    public static void init(Player player) {
        openWindows.put(player.getUniqueId(), new Window());
    }
    public static void fina(Player player) {
        openWindows.remove(player.getUniqueId());
    }
}

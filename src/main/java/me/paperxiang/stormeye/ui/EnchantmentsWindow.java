package me.paperxiang.stormeye.ui;
import fr.mrmicky.fastinv.FastInv;
import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemEnchantments;
import io.papermc.paper.datacomponent.item.TooltipDisplay;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import me.paperxiang.stormeye.StormEye;
import me.paperxiang.stormeye.utils.Utils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.persistence.PersistentDataType;
@SuppressWarnings("UnstableApiUsage")
public final class EnchantmentsWindow extends FastInv {
    private final ItemStack item;
    private static final ItemStack BORDER_0 = new ItemStack(Material.GLASS_PANE);
    private static final ItemStack BORDER_1 = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
    private static final ItemStack BORDER_2 = new ItemStack(Material.RED_STAINED_GLASS_PANE);
    private static final ItemStack BORDER_3 = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
    private static final int[] BORDER_0_SLOTS = new int[] {4, 13, 22};
    private static final int[] BORDER_1_SLOTS = new int[] {0, 2, 18, 20};
    private static final int[] BORDER_2_SLOTS = new int[] {1, 9, 11, 19};
    private static final int[] BORDER_3_SLOTS = new int[] {23, 24, 25, 26};
    private static final int[] DEFAULT_UNAVAILABLE = new int[] {3, 12, 14, 15, 16, 17, 21};
    private static final int[] DEFAULT_UNACTIVATABLE = new int[] {5, 6, 7, 8};
    private static final ItemStack UNAVAILABLE_SLOT = new ItemStack(Material.ENCHANTED_BOOK);
    private static final ItemStack ACTIVATED = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
    private static final ItemStack DEACTIVATED = new ItemStack(Material.BLUE_STAINED_GLASS_PANE);
    private static final ItemStack UNACTIVATABLE = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
    private final Enchantment[] enchantments;
    private final int[] levels;
    private int activationFlags = 0;
    private final ActivationSlot[] activationSlots;
    static {
        BORDER_0.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true).build());
        BORDER_1.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true).build());
        BORDER_2.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true).build());
        BORDER_3.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true).build());
        UNAVAILABLE_SLOT.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, false);
        UNAVAILABLE_SLOT.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true).build());
        ACTIVATED.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        ACTIVATED.setData(DataComponentTypes.ITEM_NAME, Component.text("已激活"));
        DEACTIVATED.setData(DataComponentTypes.ITEM_NAME, Component.text("未激活"));
        UNACTIVATABLE.setData(DataComponentTypes.TOOLTIP_DISPLAY, TooltipDisplay.tooltipDisplay().hideTooltip(true).build());
    }
    public EnchantmentsWindow(ItemStack item) {
        super(inv -> Bukkit.createInventory(inv, InventoryType.CHEST, Component.text("物品附魔")));
        this.item = item;
        final int slots = writeDefaultEnchantmentSlots();
        enchantments = new Enchantment[slots];
        levels = new int[slots];
        activationSlots = new ActivationSlot[slots];
        setItem(10, item, UIUtils.CANCEL_CLICK);
        setItems(BORDER_0_SLOTS, BORDER_0, UIUtils.CANCEL_CLICK);
        setItems(BORDER_1_SLOTS, BORDER_1, UIUtils.CANCEL_CLICK);
        setItems(BORDER_2_SLOTS, BORDER_2, UIUtils.CANCEL_CLICK);
        setItems(BORDER_3_SLOTS, BORDER_3, UIUtils.CANCEL_CLICK);
        setItems(DEFAULT_UNAVAILABLE, UNAVAILABLE_SLOT, UIUtils.CANCEL_CLICK);
        setItems(DEFAULT_UNACTIVATABLE, UNACTIVATABLE, UIUtils.CANCEL_CLICK);
        int slot = 0;
        if (item.hasData(DataComponentTypes.ENCHANTMENTS)) {
            for (final Map.Entry<Enchantment, Integer> entry : item.getData(DataComponentTypes.ENCHANTMENTS).enchantments().entrySet()) {
                if (slot >= slots) {
                    break;
                }
                activationFlags |= 1 << slot;
                setItem(inventoryEnchantmentSlot(slot), asEnchantedBook(enchantments[slot] = entry.getKey(), levels[slot] = entry.getValue()), new EnchantmentSlot(slot));
                setItem(inventoryActivationSlot(slot), ACTIVATED, activationSlots[slot] = new ActivationSlot(slot++));
            }
        }
        if (item.hasData(DataComponentTypes.STORED_ENCHANTMENTS)) {
            for (final Map.Entry<Enchantment, Integer> entry : item.getData(DataComponentTypes.STORED_ENCHANTMENTS).enchantments().entrySet()) {
                if (slot >= slots) {
                    break;
                }
                setItem(inventoryEnchantmentSlot(slot), asEnchantedBook(enchantments[slot] = entry.getKey(), levels[slot] = entry.getValue()), new EnchantmentSlot(slot));
                setItem(inventoryActivationSlot(slot), DEACTIVATED, activationSlots[slot] = new ActivationSlot(slot++));
            }
        }
        for (; slot < slots; slot++) {
            setItem(inventoryEnchantmentSlot(slot), null, new EnchantmentSlot(slot));
            setItem(inventoryActivationSlot(slot), DEACTIVATED, activationSlots[slot] = new ActivationSlot(slot));
        }
    }
    private static ItemStack asEnchantedBook(Enchantment enchantment, int level) {
        final ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        book.setData(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantments.itemEnchantments(Map.of(enchantment, level)));
        return book;
    }
    private static final Random ENCHANTMENT_SLOT_RANDOM = new Random();
    private static final NamespacedKey ENCHANTMENT_SLOTS = new NamespacedKey(StormEye.getInstance(), "enchantment_slots");
    private static final NamespacedKey BUILTIN_ENCHANTMENTS = new NamespacedKey(StormEye.getInstance(), "builtin_enchantments");
    private int writeDefaultEnchantmentSlots() {
        if (!item.getPersistentDataContainer().has(ENCHANTMENT_SLOTS)) {
            item.editPersistentDataContainer(data -> data.set(ENCHANTMENT_SLOTS, PersistentDataType.INTEGER, ENCHANTMENT_SLOT_RANDOM.nextInt(2, 5)));
        }
        return item.getPersistentDataContainer().get(ENCHANTMENT_SLOTS, PersistentDataType.INTEGER);
    }
    private void addStoredEnchantment(Enchantment enchantment, int level) {
        item.setData(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantments.itemEnchantments().addAll(Optional.ofNullable(item.getData(DataComponentTypes.STORED_ENCHANTMENTS)).map(ItemEnchantments::enchantments).orElse(Map.of())).add(enchantment, level).build());
    }
    private void removeStoredEnchantment(Enchantment enchantment) {
        item.setData(DataComponentTypes.STORED_ENCHANTMENTS, ItemEnchantments.itemEnchantments(Optional.ofNullable(item.getData(DataComponentTypes.STORED_ENCHANTMENTS)).map(storedEnchantments -> {
            final Map<Enchantment, Integer> modifiableStoredEnchantments = new HashMap<>(storedEnchantments.enchantments());
            modifiableStoredEnchantments.remove(enchantment);
            return modifiableStoredEnchantments;
        }).orElse(Map.of())));
    }
    private boolean checkCompatible(ItemStack enchantedBook) {
        if (!Utils.isMaterial(enchantedBook, Material.ENCHANTED_BOOK)) {
            return false;
        }
        if (!enchantedBook.hasData(DataComponentTypes.STORED_ENCHANTMENTS)) {
            return false;
        }
        final Map<Enchantment, Integer> bookEnchantments = enchantedBook.getData(DataComponentTypes.STORED_ENCHANTMENTS).enchantments();
        if (bookEnchantments.size() != 1) {
            return false;
        }
        final Enchantment enchantment = bookEnchantments.keySet().iterator().next();
        return enchantment.canEnchantItem(item) && Arrays.stream(enchantments).noneMatch(enchantment::conflictsWith);
    }
    private static int inventoryActivationSlot(int slot) {
        return slot + 5;
    }
    private static int inventoryEnchantmentSlot(int slot) {
        return slot + 14;
    }
    @Override
    protected void onClick(InventoryClickEvent event) {
        if (event.getClickedInventory() != event.getInventory()) {
            event.setCancelled(false);
        }
    }
    @Override
    protected void onDrag(InventoryDragEvent event) {
        event.setCancelled(false);
        for (final Map.Entry<Integer, ItemStack> entry : event.getNewItems().entrySet()) {
            int slot = entry.getKey();
            if (event.getView().convertSlot(slot) == slot) {
                if (checkCompatible(entry.getValue())) {
                    Bukkit.getScheduler().runTask(StormEye.getInstance(), () -> updateEnchantment(slot - 14));
                } else {
                    event.getNewItems().remove(slot);
                }
            }
        }
    }
    private boolean isActivated(int slot) {
        return (activationFlags & 1 << slot) > 0;
    }
    private void flipActivated(int slot) {
        if (enchantments[slot] != null) {
            activationFlags ^= 1 << slot;
            final boolean activated = isActivated(slot);
            setItem(inventoryActivationSlot(slot), activated ? ACTIVATED : DEACTIVATED, activationSlots[slot]);
            if (activated) {
                removeStoredEnchantment(enchantments[slot]);
                item.addUnsafeEnchantment(enchantments[slot], levels[slot]);
            } else {
                item.removeEnchantment(enchantments[slot]);
                addStoredEnchantment(enchantments[slot], levels[slot]);
            }
        }
    }
    private final class ActivationSlot implements Consumer<InventoryClickEvent> {
        private final int slot;
        private ActivationSlot(int slot) {
            this.slot = slot;
        }
        @Override
        public void accept(InventoryClickEvent event) {
            event.setCancelled(true);
            flipActivated(slot);
        }
    }
    private void updateEnchantment(int slot) {
        if (enchantments[slot] != null) {
            removeStoredEnchantment(enchantments[slot]);
        }
        Optional.ofNullable(getInventory().getItem(inventoryEnchantmentSlot(slot))).map(item -> item.getData(DataComponentTypes.STORED_ENCHANTMENTS)).flatMap(storedEnchantments -> storedEnchantments.enchantments().entrySet().stream().findAny()).ifPresentOrElse(entry -> addStoredEnchantment(enchantments[slot] = entry.getKey(), levels[slot] = entry.getValue()), () -> {
            enchantments[slot] = null;
            levels[slot] = 0;
        });
    }
    private final class EnchantmentSlot implements Consumer<InventoryClickEvent> {
        private final int slot;
        private EnchantmentSlot(int slot) {
            this.slot = slot;
        }
        @Override
        public void accept(InventoryClickEvent event) {
            if (isActivated(slot)) {
                event.setCancelled(true);
                return;
            }
            switch (event.getAction()) {
                case HOTBAR_SWAP -> {
                    final int hotbar = event.getHotbarButton();
                    final PlayerInventory inventory = event.getWhoClicked().getInventory();
                    event.setCancelled(!checkCompatible(hotbar == -1 ? inventory.getItem(EquipmentSlot.OFF_HAND) : inventory.getItem(hotbar)));
                }
                case PLACE_ALL, PLACE_ONE, PLACE_SOME, SWAP_WITH_CURSOR -> event.setCancelled(!checkCompatible(event.getCursor()));
                case PLACE_FROM_BUNDLE -> event.setCancelled(!checkCompatible(event.getCursor().getData(DataComponentTypes.BUNDLE_CONTENTS).contents().getFirst()));
                case MOVE_TO_OTHER_INVENTORY, PICKUP_ALL, PICKUP_ALL_INTO_BUNDLE, PICKUP_HALF, PICKUP_ONE, PICKUP_SOME, PICKUP_SOME_INTO_BUNDLE -> event.setCancelled(false);
                default -> event.setCancelled(true);
            }
            if (!event.isCancelled()) {
                Bukkit.getScheduler().runTask(StormEye.getInstance(), () -> updateEnchantment(slot));
            }
        }
    }
}

package me.paperxiang.theannihilation.ui;
import fr.mrmicky.fastinv.FastInv;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
public final class HashTableWindow extends FastInv {
    public HashTableWindow(Player player) {
        super(inv -> Bukkit.createInventory(inv, InventoryType.CHEST));
    }
}

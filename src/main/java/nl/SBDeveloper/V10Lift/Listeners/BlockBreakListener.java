package nl.SBDeveloper.V10Lift.Listeners;

import nl.SBDeveloper.V10Lift.API.Objects.Floor;
import nl.SBDeveloper.V10Lift.API.Objects.Lift;
import nl.SBDeveloper.V10Lift.API.Objects.LiftBlock;
import nl.SBDeveloper.V10Lift.API.Objects.LiftSign;
import nl.SBDeveloper.V10Lift.Managers.DataManager;
import nl.SBDeveloper.V10Lift.V10LiftPlugin;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.Map;

public class BlockBreakListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        Block b = e.getBlock();
        if (V10LiftPlugin.getAPI().isRope(b)) {
            e.getPlayer().sendMessage(ChatColor.RED + "You can't do this! Remove the rope first.");
            e.setCancelled(true);
            return;
        }

        LiftBlock tlb = new LiftBlock(b.getWorld().getName(), b.getX(), b.getY(), b.getZ(), (String) null);
        for (Map.Entry<String, Lift> entry : DataManager.getLifts().entrySet()) {
            Lift lift = entry.getValue();
            if (lift.getBlocks().contains(tlb)) {
                e.getPlayer().sendMessage(ChatColor.RED + "You can't do this! Remove the lift first.");
                e.setCancelled(true);
                return;
            }

            for (Floor f : lift.getFloors().values()) {
                if (f.getDoorBlocks().contains(tlb)) {
                    e.getPlayer().sendMessage(ChatColor.RED + "You can't do this! Remove the door first.");
                    e.setCancelled(true);
                    return;
                }
            }

            if (!(b.getState() instanceof Sign)) continue;

            if (!lift.getSigns().contains(tlb)) continue;

            if (!lift.getOwners().contains(e.getPlayer().getUniqueId()) && !e.getPlayer().hasPermission("v10lift.admin")) {
                e.getPlayer().sendMessage(ChatColor.RED + "You can't do this!");
                e.setCancelled(true);
            } else {
                lift.getSigns().remove(tlb);
                e.getPlayer().sendMessage(ChatColor.YELLOW + "Lift sign removed!");
            }
        }
    }
}

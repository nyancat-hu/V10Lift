package nl.SBDeveloper.V10Lift.Commands;

import nl.SBDeveloper.V10Lift.API.Objects.Floor;
import nl.SBDeveloper.V10Lift.API.Objects.Lift;
import nl.SBDeveloper.V10Lift.API.Objects.LiftBlock;
import nl.SBDeveloper.V10Lift.API.Objects.LiftSign;
import nl.SBDeveloper.V10Lift.Managers.DataManager;
import nl.SBDeveloper.V10Lift.Utils.LocationSerializer;
import nl.SBDeveloper.V10Lift.V10LiftPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class V10LiftCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command cmd, @Nonnull String label, @Nonnull String[] args) {
        if (args.length == 0) {
            //v10lift
            return helpCommand(sender);
        } else if (args[0].equalsIgnoreCase("info") && args.length == 1) {
            //v10lift info
            return infoCommand(sender);
        } else if (args[0].equalsIgnoreCase("create") && (args.length == 1 || args.length == 2)) {
            //v10lift create || v10lift create <Name>
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You have to be a player to do this.");
            }
            if (sender.hasPermission("v10lift.build") || sender.hasPermission("v10lift.admin")) {
                return createCommand(sender, args);
            } else {
                sender.sendMessage(ChatColor.RED + "You don't have the permission to do this!");
            }
        } else if (args[0].equalsIgnoreCase("delete") && args.length == 2) {
            //v10lift delete <Name>
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You have to be a player to do this.");
            }
            if (sender.hasPermission("v10lift.build") || sender.hasPermission("v10lift.admin")) {
                return deleteCommand(sender, args);
            } else {
                sender.sendMessage(ChatColor.RED + "You don't have the permission to do this!");
            }
        } else if (args[0].equalsIgnoreCase("edit") && (args.length == 1 || args.length == 2)) {
            //v10lift edit <Name>
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You have to be a player to do this.");
            }
            if (sender.hasPermission("v10lift.build") || sender.hasPermission("v10lift.admin")) {
                return editCommand(sender, args);
            } else {
                sender.sendMessage(ChatColor.RED + "You don't have the permission to do this!");
            }
        } else if (args[0].equalsIgnoreCase("floor") && (args.length == 3 || args.length == 4)) {
            //v10lift floor add <Name> || v10lift floor del <Name> || v10lift floor rename <Old> <New>
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "You have to be a player to do this.");
            }
            if (sender.hasPermission("v10lift.build") || sender.hasPermission("v10lift.admin")) {
                return floorCommand(sender, args);
            } else {
                sender.sendMessage(ChatColor.RED + "You don't have the permission to do this!");
            }
        }
        return false;
    }

    private boolean floorCommand(CommandSender sender, String[] args) {
        Player p = (Player) sender;
        if (!DataManager.containsEditPlayer(p.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "First switch on the editor mode!");
            return true;
        }

        String liftName = DataManager.getEditPlayer(p.getUniqueId());
        if (!DataManager.containsLift(liftName)) {
            sender.sendMessage(ChatColor.RED + "That lift doesn't exists.");
            return true;
        }

        Lift lift = DataManager.getLift(liftName);
        if (args[1].equalsIgnoreCase("add")) {
            Block b = p.getLocation().getBlock();
            String floorName = args[2];
            int response = V10LiftPlugin.getAPI().addFloor(liftName, floorName, new Floor(b.getY() - 1, b.getWorld().getName()));
            switch (response) {
                case 0:
                    sender.sendMessage(ChatColor.GREEN + "Floor successfully added!");
                    break;
                case -2:
                    sender.sendMessage(ChatColor.RED + "That floor is too high!");
                    break;
                case -3:
                    sender.sendMessage(ChatColor.RED + "That floor already exists!");
                    break;
                default:
                    sender.sendMessage(ChatColor.RED + "Internal error!");
                    break;
            }
        } else if (args[1].equalsIgnoreCase("del")) {
            String floorName = args[2];
            if (!V10LiftPlugin.getAPI().removeFloor(liftName, floorName)) {
                sender.sendMessage(ChatColor.RED + "Internal error!");
            } else {
                sender.sendMessage(ChatColor.GREEN + "Floor successfully removed!");
            }
        } else if (args[1].equalsIgnoreCase("rename")) {
            if (args.length < 4) {
                sender.sendMessage(ChatColor.RED + "Please use: /v10lift floor rename <Old name> <New name>");
                return true;
            }

            String floorName = args[2];
            String newFloorName = args[3];
            int response = V10LiftPlugin.getAPI().renameFloor(liftName, floorName, newFloorName);
            switch (response) {
                case 0:
                    sender.sendMessage(ChatColor.GREEN + "Floor successfully renamed!");
                    break;
                case -2:
                    sender.sendMessage(ChatColor.RED + "That floor doesn't exists!");
                    break;
                case -3:
                    sender.sendMessage(ChatColor.RED + "That floor already exists!");
                    break;
                default:
                    sender.sendMessage(ChatColor.RED + "Internal error!");
                    break;
            }
        } else {
            //args[1] not found!
            return helpCommand(sender);
        }
        return true;
    }

    private boolean editCommand(@Nonnull CommandSender sender, @Nonnull String[] args) {
        Player p = (Player) sender;
        if (DataManager.containsEditPlayer(p.getUniqueId())) {
            //TURN OFF
            if (args.length < 2) {
                String liftName = DataManager.getEditPlayer(p.getUniqueId());
                if (!DataManager.containsLift(liftName)) {
                    sender.sendMessage(ChatColor.RED + "That lift doesn't exists.");
                    return true;
                }

                Lift lift = DataManager.getLift(liftName);
                DataManager.removeEditPlayer(p.getUniqueId());
                DataManager.removeInputEditsPlayer(p.getUniqueId());
                DataManager.removeInputRemovesPlayer(p.getUniqueId());
                DataManager.removeOfflineEditsPlayer(p.getUniqueId());
                DataManager.removeOfflineRemovesPlayer(p.getUniqueId());
                if (DataManager.containsBuilderPlayer(p.getUniqueId())) {
                    DataManager.removeBuilderPlayer(p.getUniqueId());
                    V10LiftPlugin.getAPI().sortLiftBlocks(liftName);
                }
                DataManager.removeRopeEditPlayer(p.getUniqueId());
                DataManager.removeRopeRemovesPlayer(p.getUniqueId());
                DataManager.removeDoorEditsPlayer(p.getUniqueId());

                BlockState bs;
                Sign sign;
                for (LiftBlock lb : lift.getBlocks()) {
                    bs = Objects.requireNonNull(Bukkit.getWorld(lb.getWorld()), "World is null at edit command").getBlockAt(lb.getX(), lb.getY(), lb.getZ()).getState();
                    if (!(bs instanceof Sign)) continue;
                    sign = (Sign) bs;
                    //TODO Add defaults
                    if (!sign.getLine(0).equalsIgnoreCase("[v10lift]")) continue;
                    sign.setLine(3, "");
                    sign.update();
                }

                Iterator<LiftSign> liter = lift.getSigns().iterator();
                while (liter.hasNext()) {
                    LiftSign ls = liter.next();
                    bs = Objects.requireNonNull(Bukkit.getWorld(ls.getWorld()), "World is null at edit command").getBlockAt(ls.getX(), ls.getY(), ls.getZ()).getState();
                    if (!(bs instanceof Sign)) {
                        Bukkit.getLogger().severe("[V10Lift] Wrong sign removed at: " + LocationSerializer.serialize(bs.getBlock().getLocation()));
                        liter.remove();
                        continue;
                    }
                    sign = (Sign) bs;
                    //TODO Add defaults
                    sign.setLine(3, ls.getOldText());
                    sign.update();
                    ls.setOldText(null);
                }
                sender.sendMessage(ChatColor.GREEN + "Editor turned off!");
            } else {
                sender.sendMessage(ChatColor.RED + "You are still in editor mode.");
                return true;
            }
        } else {
            //TURN ON
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Please use /v10lift edit <Name>");
                return true;
            }

            if (!DataManager.containsLift(args[1])) {
                sender.sendMessage(ChatColor.RED + "That lift doesn't exists.");
                return true;
            }

            Lift lift = DataManager.getLift(args[1]);
            if (!lift.getOwners().contains(p.getUniqueId()) && !p.hasPermission("v10lift.admin")) {
                sender.sendMessage(ChatColor.RED + "You don't have the permission to remove that lift.");
            }

            DataManager.addEditPlayer(p.getUniqueId(), args[1]);
            BlockState bs;
            Sign sign;
            for (LiftBlock lb : lift.getBlocks()) {
                bs = Objects.requireNonNull(Bukkit.getWorld(lb.getWorld()), "World is null at edit command").getBlockAt(lb.getX(), lb.getY(), lb.getZ()).getState();
                if (!(bs instanceof Sign)) continue;
                sign = (Sign) bs;
                //TODO Add defaults
                if (!sign.getLine(0).equalsIgnoreCase("[v10lift]")) continue;
                sign.setLine(3, ChatColor.RED + "Maintenance");
                sign.update();
            }

            Iterator<LiftSign> liter = lift.getSigns().iterator();
            while (liter.hasNext()) {
                LiftSign ls = liter.next();
                bs = Objects.requireNonNull(Bukkit.getWorld(ls.getWorld()), "World is null at edit command").getBlockAt(ls.getX(), ls.getY(), ls.getZ()).getState();
                if (!(bs instanceof Sign)) {
                    Bukkit.getLogger().severe("[V10Lift] Wrong sign removed at: " + LocationSerializer.serialize(bs.getBlock().getLocation()));
                    liter.remove();
                    continue;
                }
                sign = (Sign) bs;
                ls.setOldText(sign.getLine(3));
                //TODO Add defaults
                sign.setLine(3, ChatColor.RED + "Maintenance");
                sign.update();
            }
            sender.sendMessage(ChatColor.GREEN + "Editor turned on!");
        }
        return true;
    }

    private boolean deleteCommand(@Nonnull CommandSender sender, @Nonnull String[] args) {
        Player p = (Player) sender;
        if (!DataManager.containsLift(args[1])) {
            sender.sendMessage(ChatColor.RED + "That lift doesn't exists.");
            return true;
        }

        Lift lift = DataManager.getLift(args[1]);
        if (!lift.getOwners().contains(p.getUniqueId()) && !p.hasPermission("v10lift.admin")) {
            sender.sendMessage(ChatColor.RED + "You don't have the permission to remove that lift.");
        }

        //TODO Fix ignoring of result
        V10LiftPlugin.getAPI().removeLift(args[1]);

        sender.sendMessage(ChatColor.GREEN + "The lift " + args[1] + " is removed successfully!");
        return true;
    }

    private boolean createCommand(@Nonnull CommandSender sender, @Nonnull String[] args) {
        Player p = (Player) sender;
        if (DataManager.containsPlayer(p.getUniqueId())) {
            //Already building!!
            if (args.length < 2) {
                sender.sendMessage(ChatColor.RED + "Please use /v10lift create <Name>");
                return true;
            }

            ArrayList<Block> blocks = DataManager.getPlayer(p.getUniqueId());
            if (blocks.isEmpty()) {
                sender.sendMessage(ChatColor.RED + "Add blocks first!");
                return true;
            }

            if (!V10LiftPlugin.getAPI().createLift(p, args[1])) {
                sender.sendMessage(ChatColor.RED + "A lift with that name already exists.");
            }

            blocks.forEach(block -> V10LiftPlugin.getAPI().addBlockToLift(args[1], block));
            V10LiftPlugin.getAPI().sortLiftBlocks(args[1]);
            DataManager.removePlayer(p.getUniqueId());
            sender.sendMessage(ChatColor.GREEN + "The lift " + args[1] + " is created successfully!");
            p.performCommand("v10lift edit " + args[1]);
        } else {
            //Not building yet!!
            DataManager.addPlayer(p.getUniqueId());
            sender.sendMessage(ChatColor.GREEN + "Okay, now add all the blocks from the cab by right-clicking on the blocks.");
            sender.sendMessage(ChatColor.GREEN + "Then type: /v10lift create <Name>");
        }
        return true;
    }

    private boolean infoCommand(@Nonnull CommandSender sender) {
        sender.sendMessage("§1==================================");
        sender.sendMessage("§6V10Lift plugin made by §aSBDeveloper");
        sender.sendMessage("§6Version: " + V10LiftPlugin.getInstance().getDescription().getVersion());
        sender.sendMessage("§6Type /v10lift help for more information!");
        sender.sendMessage("§1==================================");
        return true;
    }

    private boolean helpCommand(@Nonnull CommandSender sender) {
        sender.sendMessage("§8V10Lift commands:");
        sender.sendMessage("§6/v10lift info§f: Gives you information about the plugin.");
        sender.sendMessage("§6/v10lift help§f: Gives you this help page.");
        return true;
    }

}

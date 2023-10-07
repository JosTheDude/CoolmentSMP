package plugins.nate.smp.listeners;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import plugins.nate.smp.utils.ChatUtils;

import java.util.Arrays;

public class ChestLockListener implements Listener {
    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        if (Arrays.stream(event.getLines()).anyMatch("[Lock]"::equalsIgnoreCase)) {
            Player player = event.getPlayer();
            Block block = event.getBlock();
            BlockData blockData = block.getBlockData();

            if (blockData instanceof WallSign || blockData instanceof org.bukkit.block.data.type.Sign && isChest(getAttachedBlock(block).getType())) {
                event.setLine(0, "[Locked]");
                event.setLine(1, player.getName());
                event.setLine(2, "");
                event.setLine(3, "");
                player.sendMessage(ChatUtils.coloredChat(ChatUtils.PREFIX + "&aChest locked"));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChestAccess(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK && isChest(event.getClickedBlock().getType())) {
            Sign attachedSign = getAttachedSign(event.getClickedBlock());
            if (attachedSign != null && "[Locked]".equals(attachedSign.getLine(0))) {
                if (!event.getPlayer().getName().equals(attachedSign.getLine(1)) && !event.getPlayer().hasPermission("smp.chestlock.bypass")) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(ChatUtils.coloredChat(ChatUtils.PREFIX + "&cThis chest is locked"));
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onLockedChestOrSignBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        if (isChest(block.getType())) {
            Sign attachedSign = getAttachedSign(block);
            if (attachedSign != null && "[Locked]".equals(attachedSign.getLine(0))) {
                if (!player.getName().equals(attachedSign.getLine(1)) && !player.hasPermission("smp.chestlock.bypass")) {
                    player.sendMessage(ChatUtils.coloredChat(ChatUtils.PREFIX + "&cThis chest is locked"));
                    event.setCancelled(true);
                }
            }
        } else if (block.getState() instanceof Sign sign && "[Locked]".equals(sign.getLine(0))) {
            Block attachedBlock = getAttachedBlock(block);
            if (attachedBlock != null && isChest(attachedBlock.getType())) {
                if (!player.getName().equals(sign.getLine(1)) && !player.hasPermission("smp.chestlock.bypass")) {
                    player.sendMessage(ChatUtils.coloredChat(ChatUtils.PREFIX + "&cYou cannot break a lock"));
                    event.setCancelled(true);
                }
            }
        }
    }

    private Block getAttachedBlock(Block block) {
        BlockData blockData = block.getBlockData();

        if (blockData instanceof WallSign wallSign) {
            BlockFace facing = wallSign.getFacing();
            return block.getRelative(facing.getOppositeFace());
        } else if (blockData instanceof Sign) {
            return block.getRelative(BlockFace.DOWN);
        }
        return null;
    }

    private Sign getAttachedSign(Block block) {
        for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST}) {
            Block relative = block.getRelative(face);
            if (relative.getBlockData() instanceof WallSign) {
                return (Sign) relative.getState();
            }
        }
        return null;
    }

    private boolean isChest(Material material) {
        return material == Material.CHEST || material == Material.TRAPPED_CHEST;
    }
}

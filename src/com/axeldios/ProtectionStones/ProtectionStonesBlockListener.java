package com.axeldios.ProtectionStones;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginManager;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ProtectionStonesBlockListener
  implements Listener
{
  public WorldGuardPlugin worldGuard;
  Logger log = Logger.getLogger("Minecraft");
  
  @EventHandler
  public void onBlockBreak(BlockBreakEvent event)
  {
    Player player = event.getPlayer();
    Block pb = event.getBlock();
    boolean ps = false;
    int end = ProtectionStones.blocks;
    boolean done = false;
    for (int i = 0; !done; i++) {
      if (Material.getMaterial(ProtectionStones.blockType[i]) == pb.getType())
      {
        done = true;
        ps = true;
      }
      else if (i > end)
      {
        done = true;
      }
    }
    if (ps)
    {
      World world = player.getWorld();
      PluginManager pm = Bukkit.getServer().getPluginManager();
      if (pm.getPlugin("WorldGuard") != null) {
        this.worldGuard = ((WorldGuardPlugin)Bukkit.getServer().getPluginManager().getPlugin("WorldGuard"));
      }
      RegionManager regionManager = this.worldGuard.getRegionManager(world);
      String psx = Double.toString(pb.getLocation().getX());
      String psy = Double.toString(pb.getLocation().getY());
      String psz = Double.toString(pb.getLocation().getZ());
      String id = "ps" + psx.substring(0, psx.indexOf(".")) + "x" + psy.substring(0, psy.indexOf(".")) + "y" + psz.substring(0, psz.indexOf(".")) + "z";
      if (this.worldGuard.canBuild(player, pb.getLocation()))
      {
        if (player.hasPermission("protectionstones.destroy"))
        {
          if (regionManager.getRegion(id) != null)
          {
            LocalPlayer localPlayer = this.worldGuard.wrapPlayer(player);
            if ((regionManager.getRegion(id).isOwner(localPlayer)) || (player.hasPermission("protectionstones.superowner")))
            {
              if (!ProtectionStones.noDrop)
              {
                ItemStack oreblock = new ItemStack(pb.getType(), 1);
                int freeSpace = 0;
                for (ItemStack i : player.getInventory()) {
                  if (i == null) {
                    freeSpace += oreblock.getType().getMaxStackSize();
                  } else if (i.getType() == oreblock.getType()) {
                    freeSpace += i.getType().getMaxStackSize() - i.getAmount();
                  }
                }
                if (freeSpace >= 1)
                {
                  PlayerInventory inventory = player.getInventory();
                  inventory.addItem(new ItemStack[] { oreblock });
                  pb.setType(Material.AIR);
                  regionManager.removeRegion(id);
                  saveRegionCheck(world);
                  






                  player.sendMessage(ChatColor.YELLOW + "This area is no longer protected.");
                }
                else
                {
                  player.sendMessage(ChatColor.RED + "You don't have enough room in your inventory.");
                }
              }
              else
              {
                pb.setType(Material.AIR);
                regionManager.removeRegion(id);
                saveRegionCheck(world);
                






                player.sendMessage(ChatColor.YELLOW + "This area is no longer protected.");
              }
              event.setCancelled(true);
            }
            else
            {
              player.sendMessage(ChatColor.YELLOW + "You are not the owner of this region.");
              event.setCancelled(true);
            }
          }
          else if (ProtectionStones.noSilkTouch)
          {
        	  if (event.getPlayer().getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH)){
        		  pb.breakNaturally();
        		  event.setCancelled(true);
        	  }
          }
          else
          {
            event.setCancelled(false);
          }
        }
        else {
          event.setCancelled(true);
        }
      }
      else {
        event.setCancelled(true);
      }
    }
  }
  
  @EventHandler(priority=EventPriority.HIGHEST, ignoreCancelled=true)
  public void onBlockPlace(BlockPlaceEvent event)
  {
    Player player = event.getPlayer();
    World world = player.getWorld();
    PluginManager pm = Bukkit.getServer().getPluginManager();
    if (pm.getPlugin("WorldGuard") != null) {
      this.worldGuard = ((WorldGuardPlugin)Bukkit.getServer().getPluginManager().getPlugin("WorldGuard"));
    }
    Block pb = event.getBlock();
    
    int size = 0;
    String blockName = "";
    int end = ProtectionStones.blocks;
    boolean done = false;
    for (int i = 0; !done; i++) {
      if (Material.getMaterial(ProtectionStones.blockType[i]) == pb.getType())
      {
        size = ProtectionStones.blockSize[i];
        blockName = ProtectionStones.blockType[i];
        done = true;
      }
      else if (i > end)
      {
        done = true;
      }
    }
    RegionManager regionManager = this.worldGuard.getRegionManager(world);
    if (size != 0)
    {
      int t = ProtectionStones.toggleList.indexOf(" " + player.getName() + " ");
      if (t != -1)
      {
        event.setCancelled(false);
        return;
      }
      int w = ProtectionStones.exclusionList.indexOf(" " + world.getName().toLowerCase() + " ");
      if (w != -1)
      {
        if (ProtectionStones.exclusionPlacement)
        {
          event.setCancelled(false);
        }
        else
        {
          player.sendMessage(ChatColor.RED + "No ProtectionStones can be placed in this world.");
          player.updateInventory();
          event.setCancelled(true);
        }
        return;
      }
      if (ProtectionStones.regionLimit == 0)
      {
        player.sendMessage(ChatColor.RED + "No ProtectionStones can be placed at this time.");
        event.setCancelled(true);
        return;
      }
      if (ProtectionStones.regionLimit == -2)
      {
        int g = 0;
        int theGroupLimit = 0;
        String theGroupBlocks = "";
        String theGroup = "";
        for (g = 1; g < 10; g++)
        {
          theGroup = "protectionstones.group" + g;
          if (player.hasPermission(theGroup))
          {
            theGroupLimit = ProtectionStones.groupLimit[g];
            theGroupBlocks = ProtectionStones.groupBlocks[g];
          }
        }
        LocalPlayer playerName = null;
        if (theGroupLimit != -1) {
          if (theGroupLimit > 0)
          {
            int count = -1;
            try
            {
              playerName = this.worldGuard.wrapPlayer(player);
              count = regionManager.getRegionCountOfPlayer(playerName);
            }
            catch (Exception localException) {}
            if (count >= theGroupLimit)
            {
              player.sendMessage(ChatColor.RED + "You have reached your limit on ProtectionStones you can place at this time.");
              event.setCancelled(true);
            }
          }
          else
          {
            player.sendMessage(ChatColor.RED + "You cannot place any ProtectionStones.");
            event.setCancelled(true);
            return;
          }
        }
        if (theGroupBlocks.indexOf(blockName) == -1)
        {
          player.sendMessage(ChatColor.RED + "You cannot place that ProtectionStones type.");
          event.setCancelled(true);
        }
      }
      else if (ProtectionStones.regionLimit != -1)
      {
        LocalPlayer playerName = null;
        int count = -1;
        try
        {
          playerName = this.worldGuard.wrapPlayer(player);
          count = regionManager.getRegionCountOfPlayer(playerName);
        }
        catch (Exception localException1) {}
        if (count >= ProtectionStones.regionLimit)
        {
          player.sendMessage(ChatColor.RED + "You have reached your limit on ProtectionStones you can place at this time.");
          event.setCancelled(true);
          return;
        }
      }
      if (size > 0) {
        if (this.worldGuard.canBuild(player, pb.getLocation()))
        {
          if (player.hasPermission("protectionstones.create"))
          {
            double x = pb.getLocation().getX();
            double y = pb.getLocation().getY();
            double z = pb.getLocation().getZ();
            Vector v1 = null;
            Vector v2 = null;
            if (ProtectionStones.skyBedrock)
            {
              int mapHeight = world.getMaxHeight() - 1;
              v1 = new Vector(x - size, 0.0D, z - size);
              v2 = new Vector(x + size, mapHeight, z + size);
            }
            else
            {
              v1 = new Vector(x - size, y - size, z - size);
              v2 = new Vector(x + size, y + size, z + size);
            }
            BlockVector min = v1.toBlockVector();
            BlockVector max = v2.toBlockVector();
            String psx = Double.toString(x);
            String psy = Double.toString(y);
            String psz = Double.toString(z);
            String id = "ps" + psx.substring(0, psx.indexOf(".")) + "x" + psy.substring(0, psy.indexOf(".")) + "y" + psz.substring(0, psz.indexOf(".")) + "z";
            ProtectedRegion region = new ProtectedCuboidRegion(id, min, max);
            String playerName = player.getName();
            region.getOwners().addPlayer(playerName);
            regionManager.addRegion(region);
            

            LocalPlayer thePlayer = this.worldGuard.wrapPlayer(player);
            boolean overLap = regionManager.overlapsUnownedRegion(region, thePlayer);
            boolean override = false;
            if (overLap)
            {
              if (ProtectionStones.priorityOverride)
              {
                Vector v = new Vector(x, y, z);
                List<String> idList = regionManager.getApplicableRegionsIDs(v);
                for (String currentID : idList) {
                  if (!currentID.equalsIgnoreCase(id))
                  {
                    int priority = regionManager.getRegion(currentID).getPriority();
                    if (priority == -1) {
                      override = true;
                    }
                  }
                }
              }
              if (!override)
              {
                player.sendMessage(ChatColor.RED + "You can't protect that area, it overlaps another region.");
                regionManager.removeRegion(id);
                saveRegionCheck(world);
                






                player.updateInventory();
                event.setCancelled(true);
                return;
              }
            }
            HashMap newFlags = new HashMap();
            for (int i = 0; i < DefaultFlag.flagsList.length; i++) {
              for (int j = 0; j <= ProtectionStones.flags; j++) {
                if (DefaultFlag.flagsList[i].getName().equalsIgnoreCase(ProtectionStones.flagName[j])) {
                  if (ProtectionStones.flagSetting[j] != null)
                  {
                    Object newValue = ProtectionStones.getFlagValue(DefaultFlag.flagsList[i], ProtectionStones.flagSetting[j]);
                    if ((DefaultFlag.flagsList[i].getName().equalsIgnoreCase("greeting")) || (DefaultFlag.flagsList[i].getName().equalsIgnoreCase("farewell"))) {
                      newValue = newValue.toString().replaceAll("%player%", player.getName());
                    }
                    newFlags.put(DefaultFlag.flagsList[i], newValue);
                  }
                  else
                  {
                    newFlags.put(DefaultFlag.flagsList[i], null);
                  }
                }
              }
            }
            region.setFlags(newFlags);
            region.setPriority(ProtectionStones.priorityDefault);
            saveRegionCheck(world);
            






            player.sendMessage(ChatColor.YELLOW + "This area is now protected.");
            if (ProtectionStones.autoHide)
            {
              pb.setTypeId(0);
              ItemStack ore = player.getItemInHand();
              ore.setAmount(ore.getAmount() - 1);
              player.setItemInHand(ore.getAmount() == 0 ? null : ore);
            }
          }
          else
          {
            player.sendMessage(ChatColor.RED + "You don't have permission to place a ProtectionStone.");
            event.setCancelled(true);
          }
        }
        else
        {
          player.sendMessage(ChatColor.RED + "You can't protect that area.");
          event.setCancelled(true);
        }
      }
    }
  }
  
  @EventHandler
  public void onPistonExtend(BlockPistonExtendEvent event)
  {
    if (ProtectionStones.blockPistons)
    {
      List<Block> pushedBlocks = event.getBlocks();
      Material theMaterial = null;
      int end = ProtectionStones.blocks;
      boolean done = false;
      if (pushedBlocks != null) {
    	  Iterator localIterator = pushedBlocks.iterator();
        while (localIterator.hasNext() && !done)
        {
          Block theBlock = (Block)localIterator.next();
          theMaterial = theBlock.getType();
          done = false;
          int i = 0;
          if (theMaterial == Material.getMaterial(ProtectionStones.blockType[i]))
          {
            event.setCancelled(true);
            done = true;
          }
          else if (i > end)
          {
            done = true;
          }
          i++;
        }
      }
    }
  }
  
  @EventHandler
  public void onPistonRetract(BlockPistonRetractEvent event)
  {
    if ((ProtectionStones.blockPistons) && 
      (event.isSticky()))
    {
      World world = event.getBlock().getWorld();
      Material theMaterial = world.getBlockAt(event.getRetractLocation()).getType();
      int end = ProtectionStones.blocks;
      boolean done = false;
      if (theMaterial != null)
      {
        done = false;
        for (int i = 0; !done; i++) {
          if (theMaterial == Material.getMaterial(ProtectionStones.blockType[i]))
          {
            event.setCancelled(true);
            done = true;
          }
          else if (i > end)
          {
            done = true;
          }
        }
      }
    }
  }
  
  protected void saveRegionCheck(World world)
  {
    if (ProtectionStones.regionSaveTimer == -1)
    {
      PluginManager pm = Bukkit.getServer().getPluginManager();
      if (pm.getPlugin("WorldGuard") != null) {
        this.worldGuard = ((WorldGuardPlugin)Bukkit.getServer().getPluginManager().getPlugin("WorldGuard"));
      }
      RegionManager regionManager = this.worldGuard.getRegionManager(world);
      try
      {
        regionManager.save();
      }
      catch (ProtectionDatabaseException e)
      {
        this.log.info("[ProtectionStones] WorldGuard Error [" + e + "] during Region File Save");
      }
    }
  }
}

package com.axeldios.ProtectionStones;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.sk89q.worldedit.BlockVector;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.domains.DefaultDomain;
import com.sk89q.worldguard.protection.databases.ProtectionDatabaseException;
import com.sk89q.worldguard.protection.flags.BooleanFlag;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.IntegerFlag;
import com.sk89q.worldguard.protection.flags.SetFlag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.StringFlag;
import com.sk89q.worldguard.protection.flags.VectorFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public class ProtectionStones
  extends JavaPlugin {
	public static String[] blockType = new String[17];
	public static int[] blockSize = new int[17];
	public static int blocks = -1;
	public static String[] flagName = new String[21];
	public static String[] flagSetting = new String[21];
	public static int flags = -1;
	public static int priorityDefault = 0;
	public static boolean skyBedrock;
	public static boolean autoHide;
	public static boolean noDrop;
	public static boolean noSilkTouch;
	public static boolean blockPistons;
	public static int regionLimit;
	public static int[] groupLimit = new int[10];
	public static String[] groupBlocks = new String[10];
	public static List<String> blocksList = null;
	public static List<String> flagsList = null;
	public static String toggleList = "";
	public static String exclusionList = "";
	public static boolean exclusionPlacement = false;
	public static int regionSaveTimer;
	public static boolean priorityOverride = false;
	public Map<CommandSender, Integer> viewTaskList = new HashMap();
	Logger log = Logger.getLogger("Minecraft");
	public WorldGuardPlugin worldGuard;
	public WorldEditPlugin worldEdit;

	public void onEnable() {
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(new ProtectionStonesBlockListener(), this);
		loadConfigFile();
	}

	private void loadConfigFile() {
		this.log.info("[ProtectionStones] Configuring...");
		blocksList = getConfig().getStringList("Blocks");
		flagsList = getConfig().getStringList("Flags");
		skyBedrock = getConfig().getBoolean("Region.SKYBEDROCK", false);
		autoHide = getConfig().getBoolean("Region.AUTOHIDE", false);
		noDrop = getConfig().getBoolean("Region.NODROP", false);
		noSilkTouch = getConfig().getBoolean("Region.NOSILKTOUCH", false);
		blockPistons = getConfig().getBoolean("Region.BLOCKPISTONS", false);
		priorityOverride = getConfig().getBoolean("Region.PRIORITYOVERRIDE",
				false);
		regionSaveTimer = getConfig().getInt("Region.SAVETIMER", -1);
		regionLimit = getConfig().getInt("Region.LIMIT", -1);
		groupLimit[1] = getConfig().getInt("Group.LIMIT1", 0);
		groupLimit[2] = getConfig().getInt("Group.LIMIT2", 1);
		groupLimit[3] = getConfig().getInt("Group.LIMIT3", 3);
		groupLimit[4] = getConfig().getInt("Group.LIMIT4", 7);
		groupLimit[5] = getConfig().getInt("Group.LIMIT5", -1);
		groupLimit[6] = getConfig().getInt("Group.LIMIT6", 0);
		groupLimit[7] = getConfig().getInt("Group.LIMIT7", 0);
		groupLimit[8] = getConfig().getInt("Group.LIMIT8", 0);
		groupLimit[9] = getConfig().getInt("Group.LIMIT9", 0);
		groupBlocks[1] = getConfig().getString("Group.BLOCKS1",
				"COAL_ORE LAPIS_ORE DIAMOND_ORE");
		groupBlocks[2] = getConfig().getString("Group.BLOCKS2",
				"COAL_ORE LAPIS_ORE DIAMOND_ORE");
		groupBlocks[3] = getConfig().getString("Group.BLOCKS3",
				"COAL_ORE LAPIS_ORE DIAMOND_ORE");
		groupBlocks[4] = getConfig().getString("Group.BLOCKS4",
				"COAL_ORE LAPIS_ORE DIAMOND_ORE");
		groupBlocks[5] = getConfig().getString("Group.BLOCKS5",
				"COAL_ORE LAPIS_ORE DIAMOND_ORE");
		groupBlocks[6] = getConfig().getString("Group.BLOCKS6",
				"COAL_ORE LAPIS_ORE DIAMOND_ORE");
		groupBlocks[7] = getConfig().getString("Group.BLOCKS7",
				"COAL_ORE LAPIS_ORE DIAMOND_ORE");
		groupBlocks[8] = getConfig().getString("Group.BLOCKS8",
				"COAL_ORE LAPIS_ORE DIAMOND_ORE");
		groupBlocks[9] = getConfig().getString("Group.BLOCKS9",
				"COAL_ORE LAPIS_ORE DIAMOND_ORE");
		exclusionList = getConfig().getString("Exclusion.WORLDS", "");
		exclusionPlacement = getConfig().getBoolean("Exclusion.PLACEMENT",
				false);
		priorityDefault = getConfig().getInt("Priority", 0);
		if (blocksList.isEmpty()) {
			String[] blocksListDefault = { "COAL_ORE 5", "LAPIS_ORE 10",
					"DIAMOND_ORE 20" };
			getConfig().set("Blocks", Arrays.asList(blocksListDefault));
			blocksList = getConfig().getStringList("Blocks");
		}
		if (flagsList.isEmpty()) {
			String[] flagsListDefault = { "use deny",
					"greeting Entering %player%'s protected area",
					"farewell Leaving %player%'s protected area" };
			getConfig().set("Flags", Arrays.asList(flagsListDefault));
			flagsList = getConfig().getStringList("Flags");
		}
		getConfig().set("Region.SKYBEDROCK", Boolean.valueOf(skyBedrock));
		getConfig().set("Region.AUTOHIDE", Boolean.valueOf(autoHide));
		getConfig().set("Region.NODROP", Boolean.valueOf(noDrop));
		getConfig().set("Region.NOSILKTOUCH", Boolean.valueOf(noSilkTouch));
		getConfig().set("Region.BLOCKPISTONS", Boolean.valueOf(blockPistons));
		getConfig().set("Region.LIMIT", Integer.valueOf(regionLimit));
		getConfig().set("Group.LIMIT1", Integer.valueOf(groupLimit[1]));
		getConfig().set("Group.LIMIT2", Integer.valueOf(groupLimit[2]));
		getConfig().set("Group.LIMIT3", Integer.valueOf(groupLimit[3]));
		getConfig().set("Group.LIMIT4", Integer.valueOf(groupLimit[4]));
		getConfig().set("Group.LIMIT5", Integer.valueOf(groupLimit[5]));
		getConfig().set("Group.LIMIT6", Integer.valueOf(groupLimit[6]));
		getConfig().set("Group.LIMIT7", Integer.valueOf(groupLimit[7]));
		getConfig().set("Group.LIMIT8", Integer.valueOf(groupLimit[8]));
		getConfig().set("Group.LIMIT9", Integer.valueOf(groupLimit[9]));
		getConfig().set("Group.BLOCKS1", groupBlocks[1]);
		getConfig().set("Group.BLOCKS2", groupBlocks[2]);
		getConfig().set("Group.BLOCKS3", groupBlocks[3]);
		getConfig().set("Group.BLOCKS4", groupBlocks[4]);
		getConfig().set("Group.BLOCKS5", groupBlocks[5]);
		getConfig().set("Group.BLOCKS6", groupBlocks[6]);
		getConfig().set("Group.BLOCKS7", groupBlocks[7]);
		getConfig().set("Group.BLOCKS8", groupBlocks[8]);
		getConfig().set("Group.BLOCKS9", groupBlocks[9]);
		getConfig().set("Exclusion.WORLDS", exclusionList);
		getConfig().set("Exclusion.PLACEMENT",
				Boolean.valueOf(exclusionPlacement));
		getConfig().set("Priority", Integer.valueOf(priorityDefault));
		getConfig().set("Region.SAVETIMER", Integer.valueOf(regionSaveTimer));
		getConfig().set("Region.PRIORITYOVERRIDE",
				Boolean.valueOf(priorityOverride));
		saveConfig();

		exclusionList = exclusionList.trim().toLowerCase();
		if (exclusionList.startsWith("'")) {
			exclusionList = exclusionList.substring(1);
		}
		if (exclusionList.endsWith("'")) {
			exclusionList = exclusionList.substring(0,
					exclusionList.length() - 1);
		}
		exclusionList = " " + exclusionList + " ";

		int i = -1;
		int w = 0;
		String l = "";
		for (String j : blocksList) {
			l = j.toString();
			w = l.indexOf(" ");
			if ((w > 0) && (w < l.length() - 1)) {
				i++;
				blockType[i] = l.substring(0, w);
				blockSize[i] = Integer.parseInt(l.substring(w + 1).trim());
			}
		}
		blocks = i;

		i = -1;
		w = 0;
		l = "";
		for (String j : flagsList) {
			l = j.toString();
			w = l.indexOf(" ");
			if ((w > 0) && (w < l.length() - 1)) {
				i++;
				flagName[i] = l.substring(0, w);
				flagSetting[i] = l.substring(w + 1);
			}
		}
		flags = i;
		if (regionSaveTimer > 0) {
			int ticks = regionSaveTimer * 1200;

			int i1 = getServer().getScheduler().scheduleSyncRepeatingTask(this,
					new Runnable() {
						public void run() {
							ProtectionStones.this.saveAllWorlds();
						}
					}, 6000L, ticks);
		}
		this.log.info("[ProtectionStones] ...Enabled");
	}

	public void onDisable() {
		saveAllWorlds();
		this.log.info("[ProtectionStones] Disabled");
	}

	public boolean onCommand(CommandSender sender, Command cmd,
			String commandLabel, String[] args) {
		Player player = null;
		if (!(sender instanceof Player)) {
			sender.sendMessage(ChatColor.RED
					+ "ProtectionStones Commands cannot be run from the console.");
			return true;
		}
		player = (Player) sender;
		if ((cmd.getName().equalsIgnoreCase("ps")) && (args.length >= 1)) {
			if (args[0].equalsIgnoreCase("help")) {
				sender.sendMessage(ChatColor.YELLOW
						+ "/ps info members|owners|flags|{blank}");
				sender.sendMessage(ChatColor.YELLOW
						+ "/ps add|remove {playername}");
				sender.sendMessage(ChatColor.YELLOW
						+ "/ps addowner|removeowner {playername}");
				sender.sendMessage(ChatColor.YELLOW
						+ "/ps flag {flagname} {setting|null}");
				sender.sendMessage(ChatColor.YELLOW + "/ps hide|unhide");
				sender.sendMessage(ChatColor.YELLOW + "/ps toggle");
				sender.sendMessage(ChatColor.YELLOW + "/ps view and /ps center");
				sender.sendMessage(ChatColor.YELLOW + "/ps reclaim");
				sender.sendMessage(ChatColor.YELLOW
						+ "/ps priority {number|null}");
				sender.sendMessage(ChatColor.YELLOW
						+ "/ps region count|list|remove|regen|disown {playername}");
				sender.sendMessage(ChatColor.YELLOW
						+ "/ps admin {version|settings|hide|unhide|");
				sender.sendMessage(ChatColor.YELLOW
						+ "           cleanup|lastlogon|lastlogons|stats}");
				return true;
			}
			World world = player.getWorld();
			PluginManager pm = Bukkit.getServer().getPluginManager();
			if (pm.getPlugin("WorldGuard") != null) {
				this.worldGuard = ((WorldGuardPlugin) Bukkit.getServer()
						.getPluginManager().getPlugin("WorldGuard"));
			}
			RegionManager regionManager = this.worldGuard
					.getRegionManager(world);
			if (args[0].equalsIgnoreCase("toggle")) {
				if (player.hasPermission("protectionstones.toggle")) {
					String playerName = " " + player.getName() + " ";
					int w = toggleList.indexOf(playerName);
					if (w == -1) {
						toggleList += playerName;
						sender.sendMessage(ChatColor.YELLOW
								+ "ProtectionStone placement turned off");
					} else {
						toggleList = toggleList.replace(" " + player.getName()
								+ " ", "");
						sender.sendMessage(ChatColor.YELLOW
								+ "ProtectionStone placement turned on");
					}
				} else {
					sender.sendMessage(ChatColor.RED
							+ "You don't have permission to use the toggle command");
				}
				return true;
			}
			int playerCount;
			int indexZ;
			if (args[0].equalsIgnoreCase("admin")) {
				if (player.hasPermission("protectionstones.admin")) {
					if (args.length > 1) {
						if (args[1].equalsIgnoreCase("settings")) {
							String ctext = getConfig().saveToString();
							sender.sendMessage(ctext.split("\n"));
						} else if (args[1].equalsIgnoreCase("version")) {
							sender.sendMessage(ChatColor.YELLOW
									+ "ProtectionStones "
									+ getDescription().getVersion());
							sender.sendMessage(ChatColor.YELLOW
									+ "CraftBukkit  " + Bukkit.getVersion());
							if (pm.getPlugin("WorldGuard") != null) {
								this.worldGuard = ((WorldGuardPlugin) Bukkit
										.getServer().getPluginManager()
										.getPlugin("WorldGuard"));
								sender.sendMessage(ChatColor.YELLOW
										+ "WorldGuard "
										+ this.worldGuard.getDescription()
												.getVersion());
							}
							if (pm.getPlugin("WorldEdit") != null) {
								this.worldEdit = ((WorldEditPlugin) Bukkit
										.getServer().getPluginManager()
										.getPlugin("WorldEdit"));
								sender.sendMessage(ChatColor.YELLOW
										+ "WorldEdit "
										+ this.worldEdit.getDescription()
												.getVersion());
							}
						} else if (args[1].equalsIgnoreCase("stats")) {
							if (args.length > 2) {
								String playerName = args[2];
								if (Bukkit.getOfflinePlayer(playerName)
										.getFirstPlayed() > 0L) {
									sender.sendMessage(ChatColor.YELLOW
											+ playerName + ":");
									sender.sendMessage(ChatColor.YELLOW
											+ "================");
									long firstPlayed = (System
											.currentTimeMillis() - Bukkit
											.getOfflinePlayer(playerName)
											.getFirstPlayed()) / 86400000L;
									sender.sendMessage(ChatColor.YELLOW
											+ "First played " + firstPlayed
											+ " days ago.");
									long lastPlayed = (System
											.currentTimeMillis() - Bukkit
											.getOfflinePlayer(playerName)
											.getLastPlayed()) / 86400000L;
									sender.sendMessage(ChatColor.YELLOW
											+ "Last played " + lastPlayed
											+ " days ago.");
									String banMessage = "Not Banned";
									if (Bukkit.getOfflinePlayer(playerName)
											.isBanned()) {
										banMessage = "Banned";
									}
									sender.sendMessage(ChatColor.YELLOW
											+ banMessage);
									int count = 0;
									try {
										LocalPlayer thePlayer = null;
										thePlayer = this.worldGuard
												.wrapPlayer(Bukkit
														.getPlayer(args[2]));
										count = regionManager
												.getRegionCountOfPlayer(thePlayer);
									} catch (Exception localException1) {
									}
									sender.sendMessage(ChatColor.YELLOW
											+ "Regions: " + count);
									sender.sendMessage(ChatColor.YELLOW
											+ "================");
								} else {
									sender.sendMessage(ChatColor.YELLOW
											+ "Player name not found.");
								}
								return true;
							}
							sender.sendMessage(ChatColor.YELLOW + "World:");
							sender.sendMessage(ChatColor.YELLOW
									+ "================");
							int count = 0;
							try {
								count = regionManager.size();
							} catch (Exception localException2) {
							}
							sender.sendMessage(ChatColor.YELLOW + "Regions: "
									+ count);
							sender.sendMessage(ChatColor.YELLOW
									+ "================");
						} else if (args[1].equalsIgnoreCase("lastlogon")) {
							if (args.length > 2) {
								if (Bukkit.getOfflinePlayer(args[2])
										.getFirstPlayed() > 0L) {
									String playerName = Bukkit
											.getOfflinePlayer(args[2])
											.getName();
									long lastPlayed = (System
											.currentTimeMillis() - Bukkit
											.getOfflinePlayer(playerName)
											.getLastPlayed()) / 86400000L;
									sender.sendMessage(ChatColor.YELLOW
											+ playerName + " last played "
											+ lastPlayed + " days ago.");
									if (Bukkit.getOfflinePlayer(playerName)
											.isBanned()) {
										sender.sendMessage(ChatColor.YELLOW
												+ playerName + " is banned.");
									}
								} else {
									sender.sendMessage(ChatColor.YELLOW
											+ "Player name not found.");
								}
							} else {
								sender.sendMessage(ChatColor.YELLOW
										+ "A player name is required.");
							}
						} else if (args[1].equalsIgnoreCase("lastlogons")) {
							int days = 0;
							if (args.length > 2) {
								days = Integer.parseInt(args[2]);
							}
							OfflinePlayer[] offlinePlayerList = getServer()
									.getOfflinePlayers();
							int playerCount1 = offlinePlayerList.length;
							int playerCounter = 0;
							sender.sendMessage(ChatColor.YELLOW + "" + days
									+ " Days Plus:");
							sender.sendMessage(ChatColor.YELLOW
									+ "================");
							for (int iii = 0; iii < playerCount1; iii++) {
								long lastPlayed = (System.currentTimeMillis() - offlinePlayerList[iii]
										.getLastPlayed()) / 86400000L;
								if (lastPlayed >= days) {
									playerCounter++;
									sender.sendMessage(ChatColor.YELLOW
											+ offlinePlayerList[iii].getName()
											+ " " + lastPlayed + " days");
								}
							}
							sender.sendMessage(ChatColor.YELLOW
									+ "================");
							sender.sendMessage(ChatColor.YELLOW + ""
									+ playerCounter + " Total Players Shown");
							sender.sendMessage(ChatColor.YELLOW + ""
									+ playerCount1 + " Total Players Checked");
						} else {
							Block blockToChange;
							if ((args[1].equalsIgnoreCase("hide"))
									|| (args[1].equalsIgnoreCase("unhide"))) {
								RegionManager mgr = this.worldGuard
										.getGlobalRegionManager().get(world);
								Map<String, ProtectedRegion> regions = mgr
										.getRegions();
								int regionSize = regions.size();
								String[] regionIDList = new String[regionSize];
								String blockMaterial = "AIR";
								int index = 0;
								for (String idname : regions.keySet()) {
									try {
										if (idname.substring(0, 2).equals("ps")) {
											regionIDList[index] = idname;
											index++;
										}
									} catch (Exception localException3) {
									}
								}
								if (index == 0) {
									sender.sendMessage(ChatColor.YELLOW
											+ "No ProtectionStones Regions Found");
								} else {
									for (int i = 0; i < index; i++) {
										int indexX = regionIDList[i]
												.indexOf("x");
										int indexY = regionIDList[i]
												.indexOf("y");
										int indexZ1 = regionIDList[i].length() - 1;
										int psx = Integer
												.parseInt(regionIDList[i]
														.substring(2, indexX));
										int psy = Integer
												.parseInt(regionIDList[i]
														.substring(indexX + 1,
																indexY));
										int psz = Integer
												.parseInt(regionIDList[i]
														.substring(indexY + 1,
																indexZ1));
										blockToChange = world.getBlockAt(psx,
												psy, psz);
										blockMaterial = "AIR";
										if (args[1].equalsIgnoreCase("unhide")) {
											Vector minVector = regionManager
													.getRegion(regionIDList[i])
													.getMinimumPoint();
											Vector maxVector = regionManager
													.getRegion(regionIDList[i])
													.getMaximumPoint();
											int minX = minVector.getBlockX();
											int maxX = maxVector.getBlockX();
											int size = (maxX - minX) / 2;
											int end = blocks;
											boolean done = false;
											for (int ii = 0; !done; ii++) {
												if (blockSize[ii] == size) {
													blockMaterial = blockType[ii];
													done = true;
												} else if (ii > end) {
													done = true;
												}
											}
										}
										blockToChange.setType(Material
												.getMaterial(blockMaterial));
									}
								}
								String hMessage = "hidden";
								if (args[1].equalsIgnoreCase("unhide")) {
									hMessage = "unhidden";
								}
								sender.sendMessage(ChatColor.YELLOW
										+ "All ProtectionStones have been "
										+ hMessage);
							} else if (args[1].equalsIgnoreCase("cleanup")) {
								if (args.length >= 3) {
									if ((args[2].equalsIgnoreCase("remove"))
											|| (args[2]
													.equalsIgnoreCase("regen"))
											|| (args[2]
													.equalsIgnoreCase("disown"))) {
										int days = 30;
										if (args.length > 3) {
											days = Integer.parseInt(args[3]);
										}
										sender.sendMessage(ChatColor.YELLOW
												+ "Cleanup " + args[2] + " "
												+ days + " days");
										sender.sendMessage(ChatColor.YELLOW
												+ "================");
										RegionManager mgr = this.worldGuard
												.getGlobalRegionManager().get(
														world);
										Map<String, ProtectedRegion> regions = mgr
												.getRegions();
										int size = regions.size();
										String name = "";
										int index = 0;
										String[] regionIDList = new String[size];
										OfflinePlayer[] offlinePlayerList = getServer()
												.getOfflinePlayers();
										playerCount = offlinePlayerList.length;
										for (int iii = 0; iii < playerCount; iii++) {
											long lastPlayed = (System
													.currentTimeMillis() - offlinePlayerList[iii]
													.getLastPlayed()) / 86400000L;
											if (lastPlayed >= days) {
												index = 0;
												name = offlinePlayerList[iii]
														.getName()
														.toLowerCase();
												for (String idname : regions
														.keySet()) {
													try {
														if (((ProtectedRegion) regions
																.get(idname))
																.getOwners()
																.getPlayers()
																.contains(name)) {
															regionIDList[index] = idname;
															index++;
														}
													} catch (Exception localException4) {
													}
												}
												if (index == 0) {
													sender.sendMessage(ChatColor.YELLOW
															+ "No regions found for "
															+ name);
												} else {
													sender.sendMessage(ChatColor.YELLOW
															+ args[2]
															+ ": "
															+ name);
													for (int i = 0; i < index; i++) {
														if (args[2]
																.equalsIgnoreCase("disown")) {
															DefaultDomain owners = regionManager
																	.getRegion(
																			regionIDList[i])
																	.getOwners();
															owners.removePlayer(name);
															regionManager
																	.getRegion(
																			regionIDList[i])
																	.setOwners(
																			owners);
														} else {
															if (args[2]
																	.equalsIgnoreCase("regen")) {
																if (pm.getPlugin("WorldEdit") != null) {
																	Bukkit.dispatchCommand(
																			sender,
																			"region select "
																					+ regionIDList[i]);
																	Bukkit.dispatchCommand(
																			sender,
																			"/regen");
																}
															} else if (regionIDList[i]
																	.substring(
																			0,
																			2)
																	.equals("ps")) {
																int indexX = regionIDList[i]
																		.indexOf("x");
																int indexY = regionIDList[i]
																		.indexOf("y");
																indexZ = regionIDList[i]
																		.length() - 1;
																int psx = Integer
																		.parseInt(regionIDList[i]
																				.substring(
																						2,
																						indexX));
																int psy = Integer
																		.parseInt(regionIDList[i]
																				.substring(
																						indexX + 1,
																						indexY));
																int psz = Integer
																		.parseInt(regionIDList[i]
																				.substring(
																						indexY + 1,
																						indexZ));
																Block blockToRemove = world
																		.getBlockAt(
																				psx,
																				psy,
																				psz);
																blockToRemove
																		.setTypeId(0);
															}
															mgr.removeRegion(regionIDList[i]);
														}
													}
												}
											}
										}
										saveRegionCheck(world);

										sender.sendMessage(ChatColor.YELLOW
												+ "================");
										sender.sendMessage(ChatColor.YELLOW
												+ "Completed " + args[2]
												+ " cleanup");
										return true;
									}
									sender.sendMessage(ChatColor.YELLOW
											+ "/ps admin cleanup {remove|regen|disown} {days}");
									return true;
								}
								sender.sendMessage(ChatColor.YELLOW
										+ "/ps admin cleanup {remove|regen|disown} {days}");
								return true;
							}
						}
					} else {
						sender.sendMessage(ChatColor.YELLOW
								+ "/ps admin {reload|version|settings|hide|unhide|");
						sender.sendMessage(ChatColor.YELLOW
								+ "            cleanup|lastlogon|lastlogons|stats}");
					}
				} else {
					sender.sendMessage(ChatColor.RED
							+ "You don't have permission to use the Admin Commands");
				}
				return true;
			}
			if (args[0].equalsIgnoreCase("region")) {
				if (args.length >= 3) {
					if (player.hasPermission("protectionstones.region")) {
						if (args[1].equalsIgnoreCase("count")) {
							LocalPlayer playerName = null;
							int count = 0;
							try {
								playerName = this.worldGuard.wrapPlayer(Bukkit
										.getPlayer(args[2]));
								count = regionManager
										.getRegionCountOfPlayer(playerName);
							} catch (Exception localException5) {
							}
							sender.sendMessage(ChatColor.YELLOW + args[2]
									+ "'s region count: " + count);
							return true;
						}
						String idname;
						/*if (args[1].equalsIgnoreCase("list")) {
							RegionManager mgr = this.worldGuard
									.getGlobalRegionManager().get(world);
							Map<String, ProtectedRegion> regions = mgr
									.getRegions();
							String name = args[2].toLowerCase();
							int size = regions.size();
							String[] regionIDList = new String[size];
							String regionMessage = "";
							int index = 0;
							for (playerCount = regions.keySet().iterator(); playerCount.hasNext();) {
								idname = (String) playerCount.next();
								try {
									if (((ProtectedRegion) regions.get(idname))
											.getOwners().getPlayers()
											.contains(name)) {
										regionIDList[index] = idname;
										regionMessage = regionMessage
												+ regionIDList[index] + ", ";
										index++;
									}
								} catch (Exception localException6) {
								}
							}
							if (index == 0) {
								sender.sendMessage(ChatColor.YELLOW
										+ "No regions found for " + name);
							} else {
								regionMessage = regionMessage.substring(0,
										regionMessage.length() - 2) + ".";
								sender.sendMessage(ChatColor.YELLOW + args[2]
										+ "'s regions: " + regionMessage);
							}
							return true;
						}*/
						if ((args[1].equalsIgnoreCase("remove"))
								|| (args[1].equalsIgnoreCase("regen"))
								|| (args[1].equalsIgnoreCase("disown"))) {
							RegionManager mgr = this.worldGuard
									.getGlobalRegionManager().get(world);
							Map<String, ProtectedRegion> regions = mgr
									.getRegions();
							String name = args[2].toLowerCase();
							int size = regions.size();
							String[] regionIDList = new String[size];
							int index = 0;
							for (String idname1 : regions.keySet()) {
								try {
									if (((ProtectedRegion) regions.get(idname1))
											.getOwners().getPlayers()
											.contains(name)) {
										regionIDList[index] = idname1;
										index++;
									}
								} catch (Exception localException7) {
								}
							}
							if (index == 0) {
								sender.sendMessage(ChatColor.YELLOW
										+ "No regions found for " + name);
							} else {
								for (int i = 0; i < index; i++) {
									if (args[1].equalsIgnoreCase("disown")) {
										DefaultDomain owners = regionManager
												.getRegion(regionIDList[i])
												.getOwners();
										owners.removePlayer(name);
										regionManager
												.getRegion(regionIDList[i])
												.setOwners(owners);
									} else {
										if (args[1].equalsIgnoreCase("regen")) {
											if (pm.getPlugin("WorldEdit") != null) {
												Bukkit.dispatchCommand(
														sender,
														"region select "
																+ regionIDList[i]);
												Bukkit.dispatchCommand(sender,
														"/regen");
											}
										} else if (regionIDList[i].substring(0,
												2).equals("ps")) {
											int indexX = regionIDList[i]
													.indexOf("x");
											int indexY = regionIDList[i]
													.indexOf("y");
											int indexZ1 = regionIDList[i]
													.length() - 1;
											int psx = Integer
													.parseInt(regionIDList[i]
															.substring(2,
																	indexX));
											int psy = Integer
													.parseInt(regionIDList[i]
															.substring(
																	indexX + 1,
																	indexY));
											int psz = Integer
													.parseInt(regionIDList[i]
															.substring(
																	indexY + 1,
																	indexZ1));
											Block blockToRemove = world
													.getBlockAt(psx, psy, psz);
											blockToRemove.setTypeId(0);
										}
										mgr.removeRegion(regionIDList[i]);
									}
								}
								sender.sendMessage(ChatColor.YELLOW + name
										+ "'s regions have been removed");
								saveRegionCheck(world);
							}
							return true;
						}
					} else {
						sender.sendMessage(ChatColor.RED
								+ "You don't have permission to use Region Commands");
					}
				} else {
					sender.sendMessage(ChatColor.YELLOW
							+ "/ps region {count|list|remove|regen|disown} {playername}");
					return true;
				}
				return true;
			}
			double x = player.getLocation().getX();
			double y = player.getLocation().getY();
			double z = player.getLocation().getZ();
			Vector v = new Vector(x, y, z);
			String id = "";
			List<String> idList = regionManager.getApplicableRegionsIDs(v);
			if (idList.size() == 1) {
				id = idList.toString();
				id = id.substring(1, id.length() - 1);
			} else {
				double distanceToPS = 10000.0D;
				double tempToPS = 0.0D;
				String namePSID = "";
				for (String currentID : idList) {
					if (currentID.substring(0, 2).equals("ps")) {
						int indexX = currentID.indexOf("x");
						int indexY = currentID.indexOf("y");
						int indexZ1 = currentID.length() - 1;
						double psx = Double.parseDouble(currentID.substring(2,
								indexX));
						double psy = Double.parseDouble(currentID.substring(
								indexX + 1, indexY));
						double psz = Double.parseDouble(currentID.substring(
								indexY + 1, indexZ1));
						Location psLocation = new Location(player.getWorld(),
								psx, psy, psz);
						tempToPS = player.getLocation().distance(psLocation);
						if (tempToPS < distanceToPS) {
							distanceToPS = tempToPS;
							namePSID = currentID;
						}
					}
				}
				id = namePSID;
			}
			LocalPlayer localPlayer = this.worldGuard.wrapPlayer(player);
			if (regionManager.getRegion(id) != null) {
				if ((regionManager.getRegion(id).isOwner(localPlayer))
						|| (player.hasPermission("protectionstones.superowner"))) {
					if (args[0].equalsIgnoreCase("add")) {
						if (player.hasPermission("protectionstones.members")) {
							if (args.length < 2) {
								sender.sendMessage(ChatColor.RED
										+ "This command requires a player name.");
								return true;
							}
							String playerName = args[1];

							DefaultDomain members = regionManager.getRegion(id)
									.getMembers();
							members.addPlayer(playerName);
							regionManager.getRegion(id).setMembers(members);
							saveRegionCheck(world);

							sender.sendMessage(ChatColor.YELLOW + playerName
									+ " has been added to your region.");
							return true;
						}
						sender.sendMessage(ChatColor.RED
								+ "You don't have permission to use Members Commands");

						return true;
					}
					if (args[0].equalsIgnoreCase("remove")) {
						if (player.hasPermission("protectionstones.members")) {
							if (args.length < 2) {
								sender.sendMessage(ChatColor.RED
										+ "This command requires a player name.");
								return true;
							}
							String playerName = args[1];

							DefaultDomain members = regionManager.getRegion(id)
									.getMembers();
							members.removePlayer(playerName);
							regionManager.getRegion(id).setMembers(members);
							saveRegionCheck(world);

							sender.sendMessage(ChatColor.YELLOW + playerName
									+ " has been removed from region.");
						} else {
							sender.sendMessage(ChatColor.RED
									+ "You don't have permission to use Members Commands");
						}
						return true;
					}
					if (args[0].equalsIgnoreCase("addowner")) {
						if (player.hasPermission("protectionstones.owners")) {
							if (args.length < 2) {
								sender.sendMessage(ChatColor.RED
										+ "This command requires a player name.");
								return true;
							}
							String playerName = args[1];

							DefaultDomain owners = regionManager.getRegion(id)
									.getOwners();
							owners.addPlayer(playerName);
							regionManager.getRegion(id).setOwners(owners);
							saveRegionCheck(world);

							sender.sendMessage(ChatColor.YELLOW + playerName
									+ " has been added to your region.");
							return true;
						}
						sender.sendMessage(ChatColor.RED
								+ "You don't have permission to use Owners Commands");

						return true;
					}
					if (args[0].equalsIgnoreCase("removeowner")) {
						if (player.hasPermission("protectionstones.owners")) {
							if (args.length < 2) {
								sender.sendMessage(ChatColor.RED
										+ "This command requires a player name.");
								return true;
							}
							String playerName = args[1];

							DefaultDomain owners = regionManager.getRegion(id)
									.getOwners();
							owners.removePlayer(playerName);
							regionManager.getRegion(id).setOwners(owners);
							saveRegionCheck(world);

							sender.sendMessage(ChatColor.YELLOW + playerName
									+ " has been removed from region.");
						} else {
							sender.sendMessage(ChatColor.RED
									+ "You don't have permission to use Owners Commands");
						}
						return true;
					}
					if (args[0].equalsIgnoreCase("priority")) {
						if (player.hasPermission("protectionstones.priority")) {
							if (args.length < 2) {
								int priority = regionManager.getRegion(id)
										.getPriority();
								sender.sendMessage(ChatColor.YELLOW
										+ "Priority: " + priority);
								return true;
							}
							int priority = Integer.valueOf(
									Integer.parseInt(args[1])).intValue();
							regionManager.getRegion(id).setPriority(priority);
							saveRegionCheck(world);

							sender.sendMessage(ChatColor.YELLOW
									+ "Priority has been set.");
						} else {
							sender.sendMessage(ChatColor.RED
									+ "You don't have permission to use Priority Commands");
						}
						return true;
					}
					if (args[0].equalsIgnoreCase("view")) {
						if (player.hasPermission("protectionstones.view")) {
							if (!this.viewTaskList.isEmpty()) {
								int playerTask = 0;
								try {
									playerTask = ((Integer) this.viewTaskList
											.get(sender)).intValue();
								} catch (Exception e) {
									playerTask = 0;
								}
								if ((playerTask != 0)
										&& (Bukkit.getScheduler()
												.isQueued(playerTask))) {
									return true;
								}
							}
							Vector minVector = regionManager.getRegion(id)
									.getMinimumPoint();
							Vector maxVector = regionManager.getRegion(id)
									.getMaximumPoint();
							final int minX = minVector.getBlockX();
							final int minY = minVector.getBlockY();
							final int minZ = minVector.getBlockZ();
							final int maxX = maxVector.getBlockX();
							final int maxY = maxVector.getBlockY();
							final int maxZ = maxVector.getBlockZ();
							double px = player.getLocation().getX();
							double py = player.getLocation().getY();
							double pz = player.getLocation().getZ();
							Vector playerVector = new Vector(px, py, pz);
							final int playerY = playerVector.getBlockY();
							final World theWorld = player.getWorld();
							final Player thePlayer = player;
							Location location = null;

							location = new Location(theWorld, minX, playerY,
									minZ);
							player.sendBlockChange(location,
									Material.GLOWSTONE, (byte) 0);
							location = new Location(theWorld, maxX, playerY,
									minZ);
							player.sendBlockChange(location,
									Material.GLOWSTONE, (byte) 0);
							location = new Location(theWorld, minX, playerY,
									maxZ);
							player.sendBlockChange(location,
									Material.GLOWSTONE, (byte) 0);
							location = new Location(theWorld, maxX, playerY,
									maxZ);
							player.sendBlockChange(location,
									Material.GLOWSTONE, (byte) 0);

							location = new Location(theWorld, minX, maxY, minZ);
							player.sendBlockChange(location,
									Material.GLOWSTONE, (byte) 0);
							location = new Location(theWorld, maxX, maxY, minZ);
							player.sendBlockChange(location,
									Material.GLOWSTONE, (byte) 0);
							location = new Location(theWorld, minX, maxY, maxZ);
							player.sendBlockChange(location,
									Material.GLOWSTONE, (byte) 0);
							location = new Location(theWorld, maxX, maxY, maxZ);
							player.sendBlockChange(location,
									Material.GLOWSTONE, (byte) 0);

							location = new Location(theWorld, minX, minY, minZ);
							player.sendBlockChange(location,
									Material.GLOWSTONE, (byte) 0);
							location = new Location(theWorld, maxX, minY, minZ);
							player.sendBlockChange(location,
									Material.GLOWSTONE, (byte) 0);
							location = new Location(theWorld, minX, minY, maxZ);
							player.sendBlockChange(location,
									Material.GLOWSTONE, (byte) 0);
							location = new Location(theWorld, maxX, minY, maxZ);
							player.sendBlockChange(location,
									Material.GLOWSTONE, (byte) 0);

							int taskID = getServer().getScheduler()
									.scheduleSyncDelayedTask(this,
											new Runnable() {
												public void run() {
													Location location = null;

													location = new Location(
															theWorld, minX,
															playerY, minZ);
													thePlayer.sendBlockChange(
															location, location
																	.getBlock()
																	.getType(),
															(byte) 0);
													location = new Location(
															theWorld, maxX,
															playerY, minZ);
													thePlayer.sendBlockChange(
															location, location
																	.getBlock()
																	.getType(),
															(byte) 0);
													location = new Location(
															theWorld, minX,
															playerY, maxZ);
													thePlayer.sendBlockChange(
															location, location
																	.getBlock()
																	.getType(),
															(byte) 0);
													location = new Location(
															theWorld, maxX,
															playerY, maxZ);
													thePlayer.sendBlockChange(
															location, location
																	.getBlock()
																	.getType(),
															(byte) 0);

													location = new Location(
															theWorld, minX,
															maxY, minZ);
													thePlayer.sendBlockChange(
															location, location
																	.getBlock()
																	.getType(),
															(byte) 0);
													location = new Location(
															theWorld, maxX,
															maxY, minZ);
													thePlayer.sendBlockChange(
															location, location
																	.getBlock()
																	.getType(),
															(byte) 0);
													location = new Location(
															theWorld, minX,
															maxY, maxZ);
													thePlayer.sendBlockChange(
															location, location
																	.getBlock()
																	.getType(),
															(byte) 0);
													location = new Location(
															theWorld, maxX,
															maxY, maxZ);
													thePlayer.sendBlockChange(
															location, location
																	.getBlock()
																	.getType(),
															(byte) 0);

													location = new Location(
															theWorld, minX,
															minY, minZ);
													thePlayer.sendBlockChange(
															location, location
																	.getBlock()
																	.getType(),
															(byte) 0);
													location = new Location(
															theWorld, maxX,
															minY, minZ);
													thePlayer.sendBlockChange(
															location, location
																	.getBlock()
																	.getType(),
															(byte) 0);
													location = new Location(
															theWorld, minX,
															minY, maxZ);
													thePlayer.sendBlockChange(
															location, location
																	.getBlock()
																	.getType(),
															(byte) 0);
													location = new Location(
															theWorld, maxX,
															minY, maxZ);
													thePlayer.sendBlockChange(
															location, location
																	.getBlock()
																	.getType(),
															(byte) 0);
												}
											}, 1200L);

							this.viewTaskList.put(sender,
									Integer.valueOf(taskID));
						} else {
							sender.sendMessage(ChatColor.RED
									+ "You don't have permission to use that command");
						}
						return true;
					}
					if (args[0].equalsIgnoreCase("center")) {
						if (player.hasPermission("protectionstones.center")) {
							if (!this.viewTaskList.isEmpty()) {
								int playerTask = 0;
								try {
									playerTask = ((Integer) this.viewTaskList
											.get(sender)).intValue();
								} catch (Exception e) {
									playerTask = 0;
								}
								if ((playerTask != 0)
										&& (Bukkit.getScheduler()
												.isQueued(playerTask))) {
									return true;
								}
							}
							Vector minVector = regionManager.getRegion(id)
									.getMinimumPoint();
							Vector maxVector = regionManager.getRegion(id)
									.getMaximumPoint();
							final int minX = minVector.getBlockX();
							int minY = minVector.getBlockY();
							final int minZ = minVector.getBlockZ();
							final int maxX = maxVector.getBlockX();
							int maxY = maxVector.getBlockY();
							final int maxZ = maxVector.getBlockZ();
							final int spacing = (maxX - minX) / 2;
							final int centerY = minY + (maxY - minY) / 2;
							double px = player.getLocation().getX();
							double py = player.getLocation().getY();
							double pz = player.getLocation().getZ();
							Vector playerVector = new Vector(px, py, pz);
							final int playerY = playerVector.getBlockY();
							final World theWorld = player.getWorld();
							final Player thePlayer = player;
							Location location = null;

							location = new Location(theWorld, minX + spacing,
									playerY, minZ + spacing);
							player.sendBlockChange(location,
									Material.GLOWSTONE, (byte) 0);

							location = new Location(theWorld, minX + spacing,
									centerY, minZ + spacing);
							player.sendBlockChange(location,
									Material.GLOWSTONE, (byte) 0);

							location = new Location(theWorld, minX - spacing
									- 1, playerY, minZ + spacing);
							player.sendBlockChange(location,
									Material.GLOWSTONE, (byte) 0);
							location = new Location(theWorld, minX - spacing
									- 1, centerY, minZ + spacing);
							player.sendBlockChange(location,
									Material.GLOWSTONE, (byte) 0);

							location = new Location(theWorld, maxX + spacing
									+ 1, playerY, minZ + spacing);
							player.sendBlockChange(location,
									Material.GLOWSTONE, (byte) 0);
							location = new Location(theWorld, maxX + spacing
									+ 1, centerY, minZ + spacing);
							player.sendBlockChange(location,
									Material.GLOWSTONE, (byte) 0);

							location = new Location(theWorld, minX + spacing,
									playerY, minZ - spacing - 1);
							player.sendBlockChange(location,
									Material.GLOWSTONE, (byte) 0);
							location = new Location(theWorld, minX + spacing,
									centerY, minZ - spacing - 1);
							player.sendBlockChange(location,
									Material.GLOWSTONE, (byte) 0);

							location = new Location(theWorld, minX + spacing,
									playerY, maxZ + spacing + 1);
							player.sendBlockChange(location,
									Material.GLOWSTONE, (byte) 0);
							location = new Location(theWorld, minX + spacing,
									centerY, maxZ + spacing + 1);
							player.sendBlockChange(location,
									Material.GLOWSTONE, (byte) 0);

							int taskID = getServer().getScheduler()
									.scheduleSyncDelayedTask(this,
											new Runnable() {
												public void run() {
													Location location = null;

													location = new Location(
															theWorld, minX
																	+ spacing,
															playerY, minZ
																	+ spacing);
													thePlayer.sendBlockChange(
															location, location
																	.getBlock()
																	.getType(),
															(byte) 0);

													location = new Location(
															theWorld, minX
																	+ spacing,
															centerY, minZ
																	+ spacing);
													thePlayer.sendBlockChange(
															location, location
																	.getBlock()
																	.getType(),
															(byte) 0);

													location = new Location(
															theWorld, minX
																	- spacing
																	- 1,
															playerY, minZ
																	+ spacing);
													thePlayer.sendBlockChange(
															location, location
																	.getBlock()
																	.getType(),
															(byte) 0);
													location = new Location(
															theWorld, minX
																	- spacing
																	- 1,
															centerY, minZ
																	+ spacing);
													thePlayer.sendBlockChange(
															location, location
																	.getBlock()
																	.getType(),
															(byte) 0);

													location = new Location(
															theWorld, maxX
																	+ spacing
																	+ 1,
															playerY, minZ
																	+ spacing);
													thePlayer.sendBlockChange(
															location, location
																	.getBlock()
																	.getType(),
															(byte) 0);
													location = new Location(
															theWorld, maxX
																	+ spacing
																	+ 1,
															centerY, minZ
																	+ spacing);
													thePlayer.sendBlockChange(
															location, location
																	.getBlock()
																	.getType(),
															(byte) 0);

													location = new Location(
															theWorld, minX
																	+ spacing,
															playerY, minZ
																	- spacing
																	- 1);
													thePlayer.sendBlockChange(
															location, location
																	.getBlock()
																	.getType(),
															(byte) 0);
													location = new Location(
															theWorld, minX
																	+ spacing,
															centerY, minZ
																	- spacing
																	- 1);
													thePlayer.sendBlockChange(
															location, location
																	.getBlock()
																	.getType(),
															(byte) 0);

													location = new Location(
															theWorld, minX
																	+ spacing,
															playerY, maxZ
																	+ spacing
																	+ 1);
													thePlayer.sendBlockChange(
															location, location
																	.getBlock()
																	.getType(),
															(byte) 0);
													location = new Location(
															theWorld, minX
																	+ spacing,
															centerY, maxZ
																	+ spacing
																	+ 1);
													thePlayer.sendBlockChange(
															location, location
																	.getBlock()
																	.getType(),
															(byte) 0);
												}
											}, 1200L);

							this.viewTaskList.put(sender,
									Integer.valueOf(taskID));
						} else {
							sender.sendMessage(ChatColor.RED
									+ "You don't have permission to use that command");
						}
						return true;
					}
					int end;
					if (args[0].equalsIgnoreCase("unhide")) {
						if (player.hasPermission("protectionstones.unhide")) {
							if (id.substring(0, 2).equals("ps")) {
								int indexX = id.indexOf("x");
								int indexY = id.indexOf("y");
								int indexZ1 = id.length() - 1;
								int psx = Integer.parseInt(id.substring(2,
										indexX));
								int psy = Integer.parseInt(id.substring(
										indexX + 1, indexY));
								int psz = Integer.parseInt(id.substring(
										indexY + 1, indexZ1));

								Vector minVector = regionManager.getRegion(id)
										.getMinimumPoint();
								Vector maxVector = regionManager.getRegion(id)
										.getMaximumPoint();
								int minX = minVector.getBlockX();
								int maxX = maxVector.getBlockX();
								int size = (maxX - minX) / 2;
								end = blocks;
								boolean done = false;
								String blockMaterial = null;
								for (int i = 0; !done; i++) {
									if (blockSize[i] == size) {
										blockMaterial = blockType[i];
										done = true;
									} else if (i > end) {
										done = true;
									}
								}
								if (blockMaterial != null) {
									Block blockToUnhide = world.getBlockAt(psx,
											psy, psz);
									blockToUnhide.setType(Material
											.getMaterial(blockMaterial));
								}
							} else {
								sender.sendMessage(ChatColor.YELLOW
										+ "Not a ProtectionStones Region");
							}
						} else {
							sender.sendMessage(ChatColor.RED
									+ "You don't have permission to use that command");
						}
						return true;
					}
					if (args[0].equalsIgnoreCase("hide")) {
						if (player.hasPermission("protectionstones.hide")) {
							if (id.substring(0, 2).equals("ps")) {
								int indexX = id.indexOf("x");
								int indexY = id.indexOf("y");
								int indexZ1 = id.length() - 1;
								int psx = Integer.parseInt(id.substring(2,
										indexX));
								int psy = Integer.parseInt(id.substring(
										indexX + 1, indexY));
								int psz = Integer.parseInt(id.substring(
										indexY + 1, indexZ1));

								Block blockToHide = world.getBlockAt(psx, psy,
										psz);
								blockToHide.setType(Material.AIR);
							} else {
								sender.sendMessage(ChatColor.YELLOW
										+ "Not a ProtectionStones Region");
							}
						} else {
							sender.sendMessage(ChatColor.RED
									+ "You don't have permission to use that command");
						}
						return true;
					}
					if (args[0].equalsIgnoreCase("flag")) {
						if (player.hasPermission("protectionstones.flags")) {
							if (args.length > 1) {
								boolean saveSettings = false;
								String flagName = args[1].toLowerCase();
								String flagValue = "";
								if (args.length > 2) {
									flagValue = args[2];
									for (int n = 3; n < args.length; n++) {
										flagValue = flagValue + " " + args[n];
									}
								} else if (args[1]
										.equalsIgnoreCase("deny-spawn")) {
									flagValue = "none";
								}
								if (flagName.equals("defaults")) {
									HashMap newFlags = new HashMap();
									for (int i = 0; i < DefaultFlag.flagsList.length; i++) {
										for (int j = 0; j <= flags; j++) {
											if (DefaultFlag.flagsList[i]
													.getName()
													.equalsIgnoreCase(flagName)) {
												if (flagSetting[j] != null) {
													Object newValue = getFlagValue(
															DefaultFlag.flagsList[i],
															flagSetting[j]);
													if ((DefaultFlag.flagsList[i]
															.getName()
															.equalsIgnoreCase("greeting"))
															|| (DefaultFlag.flagsList[i]
																	.getName()
																	.equalsIgnoreCase("farewell"))) {
														newValue = newValue
																.toString()
																.replaceAll(
																		"%player%",
																		player.getName());
													}
													newFlags.put(
															DefaultFlag.flagsList[i],
															newValue);
												} else {
													newFlags.put(
															DefaultFlag.flagsList[i],
															null);
												}
											}
										}
									}
									regionManager.getRegion(id).setFlags(
											newFlags);
									saveRegionCheck(world);

									sender.sendMessage(ChatColor.YELLOW
											+ "Flags set to defaults");
									return true;
								}
								HashMap theFlags = new HashMap();
								for (int i = 0; i < DefaultFlag.flagsList.length; i++) {
									for (int j = 0; j <= flags; j++) {
										if (DefaultFlag.flagsList[i].getName()
												.equalsIgnoreCase(flagName)) {
											String permFlag = "protectionstones.flag."
													+ flagName;
											if (player.hasPermission(permFlag)) {
												saveSettings = true;
												if (flagValue != null) {
													Object newValue = getFlagValue(
															DefaultFlag.flagsList[i],
															flagValue);
													if ((DefaultFlag.flagsList[i]
															.getName()
															.equalsIgnoreCase("greeting"))
															|| (DefaultFlag.flagsList[i]
																	.getName()
																	.equalsIgnoreCase("farewell"))) {
														newValue = newValue
																.toString()
																.replaceAll(
																		"%player%",
																		player.getName());
													}
													if (newValue != "") {
														theFlags.put(
																DefaultFlag.flagsList[i],
																newValue);
													}
												}
											}
										} else {
											theFlags.put(
													DefaultFlag.flagsList[i],
													regionManager
															.getRegion(id)
															.getFlag(
																	DefaultFlag.flagsList[i]));
										}
									}
								}
								if (saveSettings) {
									regionManager.getRegion(id).setFlags(
											theFlags);
									saveRegionCheck(world);

									sender.sendMessage(ChatColor.YELLOW
											+ flagName + " flag has been set.");
								} else {
									sender.sendMessage(ChatColor.RED
											+ "You don't have permission to set that flag");
								}
							} else {
								sender.sendMessage(ChatColor.RED
										+ "Use:  /ps flag {flagname} {flagvalue}");
							}
						} else {
							sender.sendMessage(ChatColor.RED
									+ "You don't have permission to use flag commands");
						}
						return true;
					}
					if (args[0].equalsIgnoreCase("info")) {
						if (args.length == 1) {
							if (player.hasPermission("protectionstones.info")) {
								if (id != "") {
									ProtectedRegion region = regionManager
											.getRegion(id);
									if (region != null) {
										sender.sendMessage(ChatColor.GRAY
												+ "================ PS Info ================");

										sender.sendMessage(ChatColor.BLUE
												+ "Region:"
												+ ChatColor.YELLOW
												+ id
												+ ChatColor.BLUE
												+ ", Priority: "
												+ ChatColor.YELLOW
												+ regionManager.getRegion(id)
														.getPriority());

										String myFlag = "";
										String myFlagValue = "";
										int n = DefaultFlag.flagsList.length;
										for (int i = 0; i < n; i++) {
											Flag<?> flag = DefaultFlag.flagsList[i];
											if (region.getFlag(flag) != null) {
												myFlagValue = region.getFlag(
														flag).toString();
												myFlag = myFlag
														+ flag.getName() + ": "
														+ myFlagValue + ", ";
											}
										}
										if (myFlag.length() > 2) {
											myFlag = myFlag.substring(0,
													myFlag.length() - 2)
													+ ".";
											sender.sendMessage(ChatColor.BLUE
													+ "Flags: "
													+ ChatColor.YELLOW + myFlag);
										} else {
											sender.sendMessage(ChatColor.BLUE
													+ "Flags: " + ChatColor.RED
													+ "(none)");
										}
										DefaultDomain owners = region
												.getOwners();
										String ownerNames = owners.getPlayers()
												.toString();
										if (ownerNames != "[]") {
											ownerNames = ownerNames.substring(
													1, ownerNames.length() - 1);
											sender.sendMessage(ChatColor.BLUE
													+ "Owners: "
													+ ChatColor.YELLOW
													+ ownerNames);
										} else {
											sender.sendMessage(ChatColor.BLUE
													+ "Owners: "
													+ ChatColor.RED
													+ "(no owners)");
										}
										DefaultDomain members = region
												.getMembers();
										String memberNames = members
												.getPlayers().toString();
										if (memberNames != "[]") {
											memberNames = memberNames
													.substring(1, memberNames
															.length() - 1);
											sender.sendMessage(ChatColor.BLUE
													+ "Members: "
													+ ChatColor.YELLOW
													+ memberNames);
										} else {
											sender.sendMessage(ChatColor.BLUE
													+ "Members: "
													+ ChatColor.RED
													+ "(no members)");
										}
										BlockVector min = region
												.getMinimumPoint();
										BlockVector max = region
												.getMaximumPoint();
										sender.sendMessage(ChatColor.BLUE
												+ "Bounds: " + ChatColor.YELLOW
												+ "(" + min.getBlockX() + ","
												+ min.getBlockY() + ","
												+ min.getBlockZ() + ") -> ("
												+ max.getBlockX() + ","
												+ max.getBlockY() + ","
												+ max.getBlockZ() + ")");
										return true;
									}
									sender.sendMessage(ChatColor.YELLOW
											+ "Region does not exist");
								} else {
									sender.sendMessage(ChatColor.YELLOW
											+ "No region found");
								}
							} else {
								sender.sendMessage(ChatColor.RED
										+ "You don't have permission to use the region info command");
							}
						} else if (args.length == 2) {
							if (args[1].equalsIgnoreCase("members")) {
								if (player
										.hasPermission("protectionstones.members")) {
									DefaultDomain members = regionManager
											.getRegion(id).getMembers();
									String memberNames = members.getPlayers()
											.toString();
									if (memberNames != "[]") {
										memberNames = memberNames.substring(1,
												memberNames.length() - 1);
										sender.sendMessage(ChatColor.BLUE
												+ "Members: "
												+ ChatColor.YELLOW
												+ memberNames);
									} else {
										sender.sendMessage(ChatColor.BLUE
												+ "Members: " + ChatColor.RED
												+ "(no members)");
									}
								} else {
									sender.sendMessage(ChatColor.RED
											+ "You don't have permission to use Members Commands");
								}
							} else if (args[1].equalsIgnoreCase("owners")) {
								if (player
										.hasPermission("protectionstones.owners")) {
									DefaultDomain owners = regionManager
											.getRegion(id).getOwners();
									String ownerNames = owners.getPlayers()
											.toString();
									if (ownerNames != "[]") {
										ownerNames = ownerNames.substring(1,
												ownerNames.length() - 1);
										sender.sendMessage(ChatColor.BLUE
												+ "Owners: " + ChatColor.YELLOW
												+ ownerNames);
									} else {
										sender.sendMessage(ChatColor.BLUE
												+ "Owners: " + ChatColor.RED
												+ "(no owners)");
									}
								} else {
									sender.sendMessage(ChatColor.RED
											+ "You don't have permission to use Owners Commands");
								}
							} else if (args[1].equalsIgnoreCase("flags")) {
								if (player
										.hasPermission("protectionstones.flags")) {
									String myFlag = "";
									String myFlagValue = "";
									int n = DefaultFlag.flagsList.length;
									for (int i = 0; i < n; i++) {
										Flag<?> flag = DefaultFlag.flagsList[i];
										if (regionManager.getRegion(id)
												.getFlag(flag) != null) {
											myFlagValue = regionManager
													.getRegion(id)
													.getFlag(flag).toString();
											myFlag = myFlag + flag.getName()
													+ ": " + myFlagValue + ", ";
										}
									}
									if (myFlag.length() > 2) {
										myFlag = myFlag.substring(0,
												myFlag.length() - 2)
												+ ".";
										sender.sendMessage(ChatColor.BLUE
												+ "Flags: " + ChatColor.YELLOW
												+ myFlag);
									} else {
										sender.sendMessage(ChatColor.BLUE
												+ "Flags: " + ChatColor.RED
												+ "(none)");
									}
								} else {
									sender.sendMessage(ChatColor.RED
											+ "You don't have permission to use Flags Commands");
								}
							} else {
								sender.sendMessage(ChatColor.RED
										+ "Use:  /ps info {flags|members|owners|{blank}}");
							}
						} else {
							sender.sendMessage(ChatColor.RED
									+ "Use:  /ps info {flags|members|owners|{blank}}");
						}
						return true;
					}
					if (args[0].equalsIgnoreCase("reclaim")) {
						if (player.hasPermission("protectionstones.destroy")) {
							if (id.substring(0, 2).equals("ps")) {
								if (!noDrop) {
									Vector minVector = regionManager.getRegion(
											id).getMinimumPoint();
									Vector maxVector = regionManager.getRegion(
											id).getMaximumPoint();
									int minX = minVector.getBlockX();
									int maxX = maxVector.getBlockX();
									int size = (maxX - minX) / 2;
									int end1 = blocks;
									boolean done = false;
									String blockMaterial = null;
									for (int i = 0; !done; i++) {
										if (blockSize[i] == size) {
											blockMaterial = blockType[i];
											done = true;
										} else if (i > end1) {
											done = true;
										}
									}
									if (blockMaterial != null) {
										ItemStack oreblock = new ItemStack(
												Material.getMaterial(blockMaterial),
												1);
										int freeSpace = 0;
										for (ItemStack i : player
												.getInventory()) {
											if (i == null) {
												freeSpace += oreblock.getType()
														.getMaxStackSize();
											} else if (i.getType() == oreblock
													.getType()) {
												freeSpace += i.getType()
														.getMaxStackSize()
														- i.getAmount();
											}
										}
										if (freeSpace >= 1) {
											PlayerInventory inventory = player
													.getInventory();
											inventory
													.addItem(new ItemStack[] { oreblock });

											int indexX = id.indexOf("x");
											int indexY = id.indexOf("y");
											int indexZ1 = id.length() - 1;
											int psx = Integer.parseInt(id
													.substring(2, indexX));
											int psy = Integer.parseInt(id
													.substring(indexX + 1,
															indexY));
											int psz = Integer.parseInt(id
													.substring(indexY + 1,
															indexZ1));
											Block reclaimBlock = world
													.getBlockAt(psx, psy, psz);
											if (reclaimBlock
													.getType()
													.name()
													.equalsIgnoreCase(
															blockMaterial)) {
												reclaimBlock
														.setType(Material.AIR);
											}
											regionManager.removeRegion(id);
											saveRegionCheck(world);

											player.sendMessage(ChatColor.YELLOW
													+ "This area is no longer protected.");
										} else {
											sender.sendMessage(ChatColor.RED
													+ "You don't have enough room in your inventory to reclaim.");
										}
									}
								} else {
									regionManager.removeRegion(id);
									saveRegionCheck(world);

									player.sendMessage(ChatColor.YELLOW
											+ "This area is no longer protected.");
								}
							}
						} else {
							sender.sendMessage(ChatColor.RED
									+ "You don't have permission to use the Reclaim Command");
						}
						return true;
					}
				} else {
					sender.sendMessage(ChatColor.RED
							+ "You are not the owner of this region.");
					return true;
				}
			} else {
				sender.sendMessage(ChatColor.YELLOW
						+ "You are not inside a PS Region.");
				return true;
			}
		}
		return false;
	}

	protected void setBlock(World theWorld, int x, int y, int z, int blockID) {
		Block blockToChange = theWorld.getBlockAt(x, y, z);
		blockToChange.setTypeId(blockID);
	}

	protected int getBlock(World theWorld, int x, int y, int z) {
		Block blockToReturn = theWorld.getBlockAt(x, y, z);
		return blockToReturn.getTypeId();
	}

	public static Object getFlagValue(Flag<?> flag, Object value) {
		if (value == null) {
			return null;
		}
		String valueString = value.toString().trim();
		if ((flag instanceof StateFlag)) {
			if (valueString.equalsIgnoreCase("allow")) {
				return StateFlag.State.ALLOW;
			}
			if (valueString.equalsIgnoreCase("deny")) {
				return StateFlag.State.DENY;
			}
			return null;
		}
		if ((flag instanceof StringFlag)) {
			return valueString;
		}
		if ((flag instanceof BooleanFlag)) {
			if (valueString.equalsIgnoreCase("true")) {
				return Boolean.valueOf(true);
			}
			if (valueString.equalsIgnoreCase("false")) {
				return Boolean.valueOf(false);
			}
			return null;
		}
		if ((flag instanceof IntegerFlag)) {
			try {
				return Integer.valueOf(Integer.parseInt(valueString));
			} catch (NumberFormatException e) {
				return null;
			}
		}
		if (((flag instanceof SetFlag))
				&& (flag.getName().equalsIgnoreCase("deny-spawn"))) {
			String[] values = valueString.split(",", 0);

			HashSet hashset = new HashSet();
			int n = values.length;
			for (int i = 0; i < n; i++) {
				EntityType creature = EntityType.fromName(values[i].trim());
				if (creature == null) {
					String alteredName = String.format(
							"%s%s",
							new Object[] {
									Character.valueOf(Character
											.toUpperCase(values[i].trim()
													.charAt(0))),
									values[i].substring(1) });
					creature = EntityType.fromName(alteredName);
				}
				if (creature == null) {
					return null;
				}
				hashset.add(creature);
			}
			return hashset;
		}
		if ((flag instanceof SetFlag)) {
			String[] values = valueString.split(",", 0);
			int n = values.length;

			HashSet set = new HashSet(n);
			for (int i = 0; i < n; i++) {
				set.add(values[i]);
			}
			return set;
		}
		if ((flag instanceof VectorFlag)) {
			String[] xyz = valueString.split(",", 0);
			try {
				return new Vector(Integer.parseInt(xyz[0]),
						Integer.parseInt(xyz[1]), Integer.parseInt(xyz[2]));
			} catch (NumberFormatException e) {
				return null;
			}
		}
		return value;
	}

	protected void saveRegionCheck(World world) {
		if (regionSaveTimer == -1) {
			PluginManager pm = Bukkit.getServer().getPluginManager();
			if (pm.getPlugin("WorldGuard") != null) {
				this.worldGuard = ((WorldGuardPlugin) Bukkit.getServer()
						.getPluginManager().getPlugin("WorldGuard"));
			}
			RegionManager regionManager = this.worldGuard
					.getRegionManager(world);
			try {
				regionManager.save();
			} catch (ProtectionDatabaseException e) {
				this.log.info("[ProtectionStones] WorldGuard Error [" + e
						+ "] during Region File Save");
			}
		}
	}

	public void saveAllWorlds() {
		PluginManager pm = Bukkit.getServer().getPluginManager();
		if (pm.getPlugin("WorldGuard") != null) {
			this.worldGuard = ((WorldGuardPlugin) Bukkit.getServer()
					.getPluginManager().getPlugin("WorldGuard"));
		}
		List<World> worlds = Bukkit.getServer().getWorlds();
		for (World theWorld : worlds) {
			RegionManager regionManager = this.worldGuard
					.getRegionManager(theWorld);
			this.log.info("[ProtectionStones] Saving " + theWorld.getName());
			try {
				regionManager.save();
			} catch (ProtectionDatabaseException e) {
				this.log.info("[ProtectionStones] WorldGuard Error [" + e
						+ "] during Region File Save");
			}
		}
	}
}

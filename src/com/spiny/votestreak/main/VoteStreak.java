/**
 * 
 */
/**
 * @author Elijah
 *
 */
package com.spiny.votestreak.main;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.spiny.util.file.CentralizedFileManager;
import com.spiny.util.file.IOMethod;
import com.spiny.util.misc.MapBuilder;
import com.spiny.util.player.PlayerUtil;
import com.spiny.util.unix.UTS;
import com.spiny.util.yaml.YamlIOMethod;
import com.spiny.util.yaml.YamlMapUtil;
import com.spiny.votestreak.voteLogger.VoteStreakVoteLogIOMethod;
import com.spiny.votestreak.voteLogger.VoteStreakVoteLogger;
import com.spiny.votestreak.voteLogger.VoteStreakVote;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

@SuppressWarnings("rawtypes")
public class VoteStreak extends JavaPlugin {
	
	//I've spent a lot of time abstracting and optimizing this plugin, so I guess I'm to blame if it looks too large for its types. Sorry if the lack of comments causes any trouble.
	
	public class VoteStreakInitializer {
		
		private static final String yamlDataPath = "players";
		private static final String yamlStreakPath = "streak";
		private static final String logPath = "log";
		private static final String decreaseTimePath = "decrease-time";
		private static final String requiredSitesPath = "required-sites";
		private static final String onlineOnlyPath = "online-only";
		private static final String chancePath = "chance";
		private static final String decreaseAmountPath = "decrease-amount";
		private static final String voteInfoPath = "votecommand";
		
		private YamlConfiguration genPlayerYaml() {
			YamlConfiguration config = new YamlConfiguration();
			config.set(yamlDataPath, VoteStreak.this.savedPlayerData);
			return config;
		}
		
		public VoteStreakInitializer() {
			this.start();
		}
		
		public void start() {
			
			VoteStreak.this.getDataFolder().mkdir();
			VoteStreak.this.saveDefaultConfig();
			
			try {
				VoteStreak.this.savedPlayerData = YamlMapUtil.getValues((ConfigurationSection) fileManager.get(playerDataPath), SerializablePlayerData.class);
			} catch(FileNotFoundException e) {
				fileManager.put(playerDataPath, this.genPlayerYaml());
			}

			PlayerUtil.setServer(VoteStreak.this.getServer());
			VoteStreak.this.getServer().getPluginManager().registerEvents(new VoteStreakVoteListener(), VoteStreak.this);
			
			VoteStreak.this.saveDefaultConfig();
			VoteStreak.this.streakCommands = VoteStreak.deserializeCommandSection(VoteStreak.this.getConfig().getConfigurationSection(yamlStreakPath));
			VoteStreak.this.chanceCommands = VoteStreak.deserializeCommandSection(VoteStreak.this.getConfig().getConfigurationSection(chancePath));
			VoteStreak.this.log = VoteStreak.this.getConfig().getBoolean(logPath);
			VoteStreak.this.onlineOnly = VoteStreak.this.getConfig().getBoolean(onlineOnlyPath);
			VoteStreak.this.decreaseTime = UTS.fromHours(VoteStreak.this.getConfig().getInt(decreaseTimePath)).toSeconds();
			VoteStreak.this.decreaseAmount = VoteStreak.this.getConfig().getInt(decreaseAmountPath);
			VoteStreak.this.requiredSites = VoteStreak.this.getConfig().getInt(requiredSitesPath);
			
			VoteStreak.this.voteInfo = VoteStreak.this.getConfig().getStringList(voteInfoPath);
			
			List<Integer> streak = new ArrayList<Integer>(VoteStreak.this.streakCommands.keySet());
			Collections.sort(streak, new Comparator<Integer>(){
				public int compare(Integer arg0, Integer arg1) {
					return arg1 - arg0;
				}
			});
			VoteStreak.this.maxStreak = streak.get(0);
			
		}
		
	}
	
	public class VoteStreakStringChanger {
		
		private static final String ADDRESS = "%site%";
		private static final String NAME = "%name%";
		private static final String SITES = "%sites%";
		private static final String STREAK = "%streak%";
		private static final String TIME = "%time%";
		
		private static final char COLOR_CODE = '&';
		
		public String changeForVote(String s, VoteStreakVote v) {
			String r = ChatColor.translateAlternateColorCodes(COLOR_CODE, s);
			return r.replaceAll(ADDRESS, v.getAddress()).replaceAll(NAME, v.getUsername());
		}
		
		public String changeForPlayer(String s, OfflinePlayer p) {
			SerializablePlayerData spd = VoteStreak.this.getSavedData(p);
			PlayerData pd = VoteStreak.this.getData(p);
			String r = ChatColor.translateAlternateColorCodes(COLOR_CODE, s);
			int h = UTS.fromSeconds(VoteStreak.this.decreaseTime).toHours();
			int c = UTS.currentTime().toHours();
			int l = UTS.fromSeconds(spd.lastVoteTime).toHours();
			int d = c - l;
			return r.replaceAll(SITES, String.valueOf(pd.getSitesVotedOn().size()))
					.replaceAll(STREAK, String.valueOf(spd.streak))
					.replaceAll(TIME, String.valueOf(h - d));
		}
		
	}
	
	public class VoteStreakVoteListener implements Listener {
		
		@EventHandler
		public void onVotifierEvent(VotifierEvent event) {
			Vote vote = event.getVote();
			long unixTime = 0;
			try {
				unixTime = Long.parseLong(vote.getTimeStamp());
			} catch(NumberFormatException e) {
				unixTime = UTS.fromCalender(toCalender(vote.getTimeStamp())).toSeconds();
			}
			OfflinePlayer p = PlayerUtil.getPlayerFromName(vote.getUsername());
			if(p == null) return;
			VoteStreak.this.onVote(p, vote.getServiceName(), unixTime);
		}
		
	}
	
	private static Calendar toCalender(String voteTime) {
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(SDF.parse(voteTime));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return cal;
	}
	
	private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss -SSSS");
	
	private static final String votelogPath = "votes.log";
	private static final String playerDataPath = "players.yml";
	
	private static final String COMMAND_FAIL_SENDER = ChatColor.RED + "You must be a player to use this command.";
	
	private static VoteStreak recentInstance;
	private static String name;
	
	private boolean log;
	private boolean onlineOnly;
	private int decreaseTime;
	private int decreaseAmount;
	private int requiredSites;
	private int maxStreak;
	private Map<Integer, List<String>> streakCommands;
	private Map<Integer, List<String>> chanceCommands;
	private List<String> voteInfo;
	
	private VoteStreakStringChanger stringChanger = new VoteStreakStringChanger();
	
	public static Map<String, IOMethod> ioMethodConfig = new MapBuilder<String, IOMethod>(new HashMap<String, IOMethod>()).withEntry("yml", new YamlIOMethod()).withEntry("log", new VoteStreakVoteLogIOMethod()).build();
	
	static {
		SerializablePlayerData.register();
	}
	
	private VoteStreakInitializer initializer;
	private Map<String, SerializablePlayerData> savedPlayerData = new HashMap<String, SerializablePlayerData>();
	private Map<OfflinePlayer, PlayerData> playerData = new HashMap<OfflinePlayer, PlayerData>();
	private CentralizedFileManager fileManager = new CentralizedFileManager(this.getDataFolder(), ioMethodConfig, "config.yml");
	
	public static void updateRecentInstanceRemotely() {
		recentInstance = (VoteStreak) Bukkit.getServer().getPluginManager().getPlugin(name);
	}
	
	public static SerializablePlayerData getSavedDataRemotely(OfflinePlayer p) {
		return recentInstance.getSavedData(p);
	}
	
	private static Map<Integer, List<String>> deserializeCommandSection(ConfigurationSection c) {
		Map<Integer, List<String>> m = new HashMap<Integer, List<String>>();
		for(String s : c.getValues(false).keySet()) {
			m.put(Integer.valueOf(s), c.getStringList(s));
		}
		return m;
	}
	
	private void updateRecentInstance() {
		recentInstance = this;
		name = this.getName();
	}
	
	public void onEnable() {
		initializer = new VoteStreakInitializer();
		this.updateRecentInstance();
	}

	public void onDisable() {
		fileManager.put(playerDataPath, YamlMapUtil.setValues(new YamlConfiguration(), savedPlayerData));
		fileManager.save();
	}
	
	public void reload() {
		initializer.start();
	}

	public void onVote(OfflinePlayer p, String voteSiteAddress, long unixTime) {
		
		if(!p.isOnline() && onlineOnly) return;
		SerializablePlayerData spd = this.getSavedData(p);
		VoteStreakVote vote = new VoteStreakVote(p.getName(), unixTime, voteSiteAddress);
		
		for(Entry<Integer, List<String>> e : chanceCommands.entrySet()) {
			double d = Math.random() * 100;
			if(d <= e.getKey()) {
				for(String command : e.getValue()) {
					this.evalCommand(command, vote);
				}
			}
		}
		
		PlayerData pd = this.getData(p);
		pd.getSitesVotedOn().add(voteSiteAddress);
		
		for(String command : streakCommands.get(spd.streak)) {
			this.evalCommand(command, vote);
		}
		
		if(pd.getSitesVotedOn().size() >= requiredSites && spd.streak < maxStreak) {
			spd.streak++;
			pd.getSitesVotedOn().clear();
		} else if(unixTime - spd.lastVoteTime > decreaseTime) {
			spd.streak -= this.decreaseAmount;
			if(spd.streak < 1) spd.streak = 1;
		}
		
		spd.lastVoteTime = (int) unixTime;
		
		if(!log) return;
		VoteStreakVoteLogger logger = new VoteStreakVoteLogger();
		try {
			logger = (VoteStreakVoteLogger) fileManager.get(votelogPath);
		} catch (FileNotFoundException e) {
			fileManager.put(votelogPath, logger);
		}
		logger.log(vote);
		
	}

	private SerializablePlayerData getSavedData(OfflinePlayer p) {
		String id = p.getUniqueId().toString();
		if(!savedPlayerData.containsKey(id)) initSavedData(p);
		return (SerializablePlayerData) savedPlayerData.get(id);
	}
	
	private PlayerData getData(OfflinePlayer p) {
		if(!playerData.containsKey(p)) initData(p);
		return playerData.get(p);
	}

	private void initSavedData(OfflinePlayer p) {
		SerializablePlayerData d = new SerializablePlayerData(1, p.getName(), UTS.currentTime().toSeconds());
		savedPlayerData.put(p.getUniqueId().toString(), d);
	}
	
	private void initData(OfflinePlayer p) {
		playerData.put(p, new PlayerData());
	}
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		switch(command.getName()) {
			case "simvote": {
				this.onVote(PlayerUtil.getPlayerFromName(args[0]), args[1], Long.valueOf(UTS.currentTime().sub(UTS.fromHours(Integer.valueOf(args[2]))).toSeconds()));
				return true;
			} case "vote": {
				if(!(sender instanceof OfflinePlayer)) {
					sender.sendMessage(COMMAND_FAIL_SENDER);
					return false;
				}
				OfflinePlayer p = (OfflinePlayer) sender;
				for(String s : voteInfo) {
					sender.sendMessage(stringChanger.changeForPlayer(s, p));
				}
			} default: {
				return false;
			}
		}
	}
	
	private void evalCommand(String command, VoteStreakVote v) {
		command = stringChanger.changeForVote(command, v);
		v.addCommand(command);
		this.getServer().dispatchCommand(this.getServer().getConsoleSender(), command);
	}
}

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
import com.spiny.util.string.StringFilter;
import com.spiny.util.string.StringFilterLayer;
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
			
			try {
				VoteStreak.this.savedPlayerData = YamlMapUtil.getValues((ConfigurationSection) fileManager.get(playerDataPath), SerializablePlayerData.class);
			} catch(FileNotFoundException e) {
				fileManager.put(playerDataPath, this.genPlayerYaml());
			}
			
			streakCommandFilter = new StringFilter(VoteStreak.streakFilterLayers);
			PlayerUtil.setServer(VoteStreak.this.getServer());
			VoteStreak.this.getServer().getPluginManager().registerEvents(new VoteStreakVoteListener(), VoteStreak.this);
			
			VoteStreak.this.saveDefaultConfig();
			VoteStreak.this.streakCommands = VoteStreak.deserializeCommandSection(VoteStreak.this.getConfig().getConfigurationSection(yamlStreakPath));
			VoteStreak.this.chanceCommands = VoteStreak.deserializeCommandSection(VoteStreak.this.getConfig().getConfigurationSection(chancePath));
			VoteStreak.this.log = VoteStreak.this.getConfig().getBoolean(logPath);
			VoteStreak.this.onlineOnly = VoteStreak.this.getConfig().getBoolean(onlineOnlyPath);
			VoteStreak.this.decreaseTime = VoteStreak.this.getConfig().getInt(decreaseTimePath);
			VoteStreak.this.decreaseAmount = VoteStreak.this.getConfig().getInt(decreaseAmountPath);
			VoteStreak.this.requiredSites = VoteStreak.this.getConfig().getInt(requiredSitesPath);
			
			List<Integer> streak = new ArrayList<Integer>(VoteStreak.this.streakCommands.keySet());
			Collections.sort(streak, new Comparator<Integer>(){
				public int compare(Integer arg0, Integer arg1) {
					return arg1 - arg0;
				}
			});
			VoteStreak.this.maxStreak = streak.get(0);
			
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
				unixTime = toCalender(vote.getTimeStamp()).getTimeInMillis() / 1000;
			}
			VoteStreak.this.onVote(PlayerUtil.getPlayerFromName(vote.getUsername()), vote.getServiceName(), unixTime);
		}

	}
	
	private static Calendar toCalender(String voteTime) {
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(sdf.parse(voteTime));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return cal;
	}
	
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss -SSSS");
	
	private static final String votelogPath = "votes.log";
	private static final String playerDataPath = "players.yml";
	
	private boolean log;
	private boolean onlineOnly;
	private int decreaseTime;
	private int decreaseAmount;
	private int requiredSites;
	private int maxStreak;
	private Map<Integer, List<String>> streakCommands;
	private Map<Integer, List<String>> chanceCommands;
	
	public static Map<String, IOMethod> ioMethodConfig = new MapBuilder<String, IOMethod>(new HashMap<String, IOMethod>()).withEntry("yml", new YamlIOMethod()).withEntry("log", new VoteStreakVoteLogIOMethod()).build();
	
	static {
		SerializablePlayerData.register();
	}
	
	private VoteStreakInitializer initializer;
	private Map<String, SerializablePlayerData> savedPlayerData = new HashMap<String, SerializablePlayerData>();
	private Map<OfflinePlayer, PlayerData> playerData = new HashMap<OfflinePlayer, PlayerData>();
	private CentralizedFileManager fileManager = new CentralizedFileManager(this.getDataFolder(), ioMethodConfig, "config.yml");
	private StringFilter streakCommandFilter;

	public void onEnable() {
		initializer = new VoteStreakInitializer();
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
		int hourDif = (int) Math.floor((unixTime - spd.lastVoteTime) / 3600);
		spd.lastVoteTime = (int) unixTime;
		
		for(Entry<Integer, List<String>> e : chanceCommands.entrySet()) {
			double d = Math.random() * 100;
			if(d <= e.getKey()) {
				for(String command : e.getValue()) {
					this.evalCommand(command, vote, p.getName(), voteSiteAddress);
				}
			}
		}
		
		if(hourDif > decreaseTime) {
			spd.streak -= this.decreaseAmount;
			if(spd.streak < 1) spd.streak = 1;
		}
		
		PlayerData pd = this.getData(p);
		pd.getSitesVotedOn().add(voteSiteAddress);
		for(String command : streakCommands.get(spd.streak)) {
			this.evalCommand(command, vote, p.getName(), voteSiteAddress);
		}
		if(pd.getSitesVotedOn().size() >= requiredSites && spd.streak < maxStreak) {
			spd.streak++;
			pd.getSitesVotedOn().clear();
		}
		
		if(!log) return;
		VoteStreakVoteLogger logger = new VoteStreakVoteLogger();
		try {
			logger = (VoteStreakVoteLogger) fileManager.get(votelogPath);
		} catch (FileNotFoundException e) {
			fileManager.put(votelogPath, logger);
		}
		logger.log(vote);
		
	}

	public SerializablePlayerData getSavedData(OfflinePlayer p) {
		String id = p.getUniqueId().toString();
		if(!savedPlayerData.containsKey(id)) initSavedData(p);
		return (SerializablePlayerData) savedPlayerData.get(id);
	}
	
	public PlayerData getData(OfflinePlayer p) {
		if(!playerData.containsKey(p)) initData(p);
		return playerData.get(p);
	}

	public void initSavedData(OfflinePlayer p) {
		SerializablePlayerData d = new SerializablePlayerData(1, p.getName(), 0);
		savedPlayerData.put(p.getUniqueId().toString(), d);
	}
	
	public void initData(OfflinePlayer p) {
		playerData.put(p, new PlayerData());
	}
	
	public static StringFilterLayer[] streakFilterLayers = new StringFilterLayer[]{new StringFilterLayer<String>(){
		public String filter(String username, String s) {
			s = s.replaceAll("%name%", username);
			return s;
		}
	}, new StringFilterLayer<String>(){
		public String filter(String address, String s) {
			s = s.replaceAll("%site%", address);
			return s;
		}
	}};
	
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(!command.getName().equalsIgnoreCase("simvote")) return false;
		OfflinePlayer p = PlayerUtil.getPlayerFromName(args[0]);
		this.onVote(p, args[1], Integer.valueOf(args[2]) * 3600);
		return true;
	}
	
	public static Map<Integer, List<String>> deserializeCommandSection(ConfigurationSection c) {
		Map<Integer, List<String>> m = new HashMap<Integer, List<String>>();
		for(String s : c.getValues(false).keySet()) {
			m.put(Integer.valueOf(s), c.getStringList(s));
		}
		return m;
	}
	
	private void evalCommand(String command, VoteStreakVote v, String senderName, String voteSiteAddress) {
		command = streakCommandFilter.filter(command, senderName, voteSiteAddress);
		v.addCommand(command);
		this.getServer().dispatchCommand(this.getServer().getConsoleSender(), command);
	}
}

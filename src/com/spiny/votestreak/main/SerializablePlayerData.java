package com.spiny.votestreak.main;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

import com.spiny.util.misc.MapBuilder;

public class SerializablePlayerData implements ConfigurationSerializable {
	
	private static final String streakPath = "streak";
	private static final String namePath = "name";
	private static final String lastVoteTimePath = "lastVoteTime";
	
	public static final String serializationAlias = "voteStreakPlayerData";
	
	public static synchronized void register() {
		ConfigurationSerialization.registerClass(SerializablePlayerData.class, serializationAlias);
	}
	
	public int streak;
	public String name;
	public int lastVoteTime;

	public SerializablePlayerData(int streak, String name, int lastVoteTime) {
		this.streak = streak;
		this.name = name;
		this.lastVoteTime = lastVoteTime;
	}

	@Override
	public Map<String, Object> serialize() {
		return new MapBuilder<String, Object>(new HashMap<String, Object>()).withEntry(ConfigurationSerialization.SERIALIZED_TYPE_KEY, serializationAlias).withEntry(streakPath, streak).withEntry(namePath,  name).withEntry(lastVoteTimePath,  lastVoteTime).build();
	}
	
	public static SerializablePlayerData deserialize(Map<String, Object> map) {
		return new SerializablePlayerData((int) map.get(streakPath), (String) map.get(namePath), (int) map.get(lastVoteTimePath));
	}
}

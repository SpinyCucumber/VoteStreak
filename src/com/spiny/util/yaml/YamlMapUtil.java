package com.spiny.util.yaml;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

public class YamlMapUtil {
	
	public static <T extends ConfigurationSection> T setValues(T s, Map<String, ? extends ConfigurationSerializable> m) {
		for(Entry<String, ? extends ConfigurationSerializable> e : m.entrySet()) {
			s.createSection(e.getKey(), e.getValue().serialize());
		}
		return s;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends ConfigurationSerializable> Map<String, T> getValues(ConfigurationSection c, Class<T> clazz) {
		Map<String, T> m = new HashMap<String, T>();
		for(Entry<String, Object> e : c.getValues(false).entrySet()) {
			m.put(e.getKey(), (T) e.getValue());
		}
		return m;
	}
}

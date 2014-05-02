package com.spiny.util.yaml;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;

import com.spiny.util.file.IOMethod;

public class YamlIOMethod implements IOMethod<YamlConfiguration> {

	@Override
	public YamlConfiguration load(File file) {
		return YamlConfiguration.loadConfiguration(file);
	}

	@Override
	public void save(File file, YamlConfiguration o) {
		try {
			o.save(file);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}

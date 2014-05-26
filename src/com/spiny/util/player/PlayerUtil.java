/**
 * 
 */
/**
 * @author Elijah
 *
 */
package com.spiny.util.player;

import org.bukkit.OfflinePlayer;
import org.bukkit.Server;

public class PlayerUtil {
	
	private static Server server;
	
	public static void setServer(Server newServer) {
		server = newServer;
	}
	
	public static OfflinePlayer getPlayerFromName(String name) throws NullPointerException {
		for(OfflinePlayer player : server.getOfflinePlayers()) {
			if(player.getName().startsWith(name)) return player;
		}
		return null;
	}
}
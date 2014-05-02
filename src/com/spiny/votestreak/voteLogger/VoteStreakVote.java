package com.spiny.votestreak.voteLogger;

import java.util.ArrayList;
import java.util.List;

public class VoteStreakVote {
	
	private List<String> commandsUsed = new ArrayList<String>();
	private String username;
	private int time;
	private String address;
	
	public VoteStreakVote(String username, int time, String address) {
		this.username = username;
		this.time = time;
		this.address = address;
	}
	
	public void addCommand(String c) {
		commandsUsed.add(c);
	}
	
	public String[] concat() {
		List<String> s = new ArrayList<String>();
		s.add("Vote " + this.hashCode());
		s.add("Username: " + username);
		s.add("Time: (Unix) " + String.valueOf(time));
		s.add("Site Address: " + address);
		s.add("Commands Used:");
		s.addAll(commandsUsed);
		return s.toArray(new String[s.size()]);
	}
}

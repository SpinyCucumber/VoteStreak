package com.spiny.votestreak.voteLogger;

import java.util.ArrayList;
import java.util.List;

public class VoteSerializer {
	
	public String[] serialize(VoteStreakVote v) {
		List<String> s = new ArrayList<String>();
		s.add("Vote " + this.hashCode());
		s.add("Username: " + v.getUsername());
		s.add("Time: (Unix) " + String.valueOf(v.getTime()));
		s.add("Site Address: " + v.getAddress());
		s.add("Commands Used:");
		s.addAll(v.getCommandsUsed());
		return s.toArray(new String[s.size()]);
	}
	
}

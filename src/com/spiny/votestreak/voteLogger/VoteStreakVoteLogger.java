/**
 * 
 */
/**
 * @author Elijah
 *
 */
package com.spiny.votestreak.voteLogger;

import java.util.ArrayList;
import java.util.List;

public class VoteStreakVoteLogger {
	
	private List<VoteStreakVote> votes = new ArrayList<VoteStreakVote>();
	
	public void log(VoteStreakVote vote) {
		votes.add(vote);
	}
	
	public List<VoteStreakVote> getVotes() {
		return votes;
	}
	
}
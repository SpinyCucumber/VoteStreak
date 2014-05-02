package com.spiny.votestreak.voteLogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import com.spiny.util.file.IOMethod;

public class VoteStreakVoteLogIOMethod implements IOMethod<VoteStreakVoteLogger> {

	@Override
	public VoteStreakVoteLogger load(File file) {
		return new VoteStreakVoteLogger();
	}

	@Override
	public void save(File file, VoteStreakVoteLogger o) {
		try {
		    BufferedWriter writer = new BufferedWriter(new FileWriter(file, true));
		    for(VoteStreakVote vote : o.getVotes()) {
		    	for(String s : vote.concat()) {
		    		writer.write(s);
		    		writer.newLine();
		    	}
		    	writer.newLine();
		    }
		    writer.close();
		} catch ( IOException e){
			e.printStackTrace();
		}
	}

}

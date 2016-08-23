package org.olat.course.highscore.manager;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.olat.core.id.Identity;
import org.olat.course.highscore.ui.HighScoreTableEntry;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.user.UserManager;
import org.springframework.stereotype.Service;

import edu.emory.mathcs.backport.java.util.Collections;

@Service
public class HighScoreManager {
	
	
	public double[] sortRankByScore (List<AssessmentEntry>  assessEntries,
			List<HighScoreTableEntry> allMembers, List<HighScoreTableEntry> ownIdMembers,
			List<List<HighScoreTableEntry>> allPodium, List<Integer> ownIdIndices,	
			int tableSize, Identity ownIdentity, UserManager userManager){

		for (AssessmentEntry assessmentEntry : assessEntries) {
			allMembers.add(new HighScoreTableEntry((assessmentEntry.getScore().floatValue()),
					userManager.getUserDisplayName(assessmentEntry.getIdentity()),
					assessmentEntry.getIdentity()));
		}
		assessEntries.clear();
		//2 step comparator, sorts by score then own Identity comes first
		Collections.sort(allMembers, new Comparator<HighScoreTableEntry>() {
			public int compare(HighScoreTableEntry a, HighScoreTableEntry b){
				int answer = Float.compare(b.getScore(), a.getScore());
				if (answer == 0){
					if (a.getIdentity().equals(ownIdentity))return -1;
					else if (b.getIdentity().equals(ownIdentity))return 1;
					else return 0;
				} else {
					return answer;
				}				
			}			
		});

		float buffer = -1;
		int index = 0, rank = 1;
		double[] allScores = new double[allMembers.size()];
		for (int j = 0; j < allMembers.size(); j++) {
			if (allMembers.get(j).getScore() < buffer){
				index++;
				rank = j + 1;
			}
			//first three position are put in separate lists,
			if (index < 3){
				allPodium.get(index).add(allMembers.get(j));
			}
			// finding position rank for own id
			if (allMembers.get(j).getIdentity().equals(ownIdentity)){
				ownIdIndices.add(j);
			}
			//setting rank for each member 
			allMembers.get(j).setRank(rank);
			buffer = allMembers.get(j).getScore();
			//adding scores for histogram
			allScores[j] = buffer;
		}
		//only getting member with own id for 2nd table
		ownIdMembers.addAll(allMembers.stream()
				.skip(tableSize)
				.filter(a -> a.getIdentity().equals(ownIdentity))
				.collect(Collectors.toList()));
		
		return allScores;
	}
}
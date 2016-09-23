/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.course.highscore.manager;
/**
 * Description:<br>
 * HighScoreManagerTest
 * Initial Date:  20.08.2016 <br>
 * @author fkiefer
 */
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.course.highscore.ui.HighScoreTableEntry;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.user.UserManager;
import org.springframework.stereotype.Service;

import edu.emory.mathcs.backport.java.util.Collections;

@Service
public class HighScoreManager {

	private static final OLog log = Tracing.createLoggerFor(HighScoreManager.class);
	
	public double[] sortRankByScore (List<AssessmentEntry>  assessEntries,
			List<HighScoreTableEntry> allMembers, List<HighScoreTableEntry> ownIdMembers,
			List<List<HighScoreTableEntry>> allPodium, List<Integer> ownIdIndices,	
			int tableSize, Identity ownIdentity, UserManager userManager){

		for (AssessmentEntry assessmentEntry : assessEntries) {
			float score = assessmentEntry.getScore() == null ? 0f : assessmentEntry.getScore().floatValue();
			allMembers.add(new HighScoreTableEntry(score, userManager.getUserDisplayName(assessmentEntry.getIdentity()),
					assessmentEntry.getIdentity()));
		}
		assessEntries.clear();
		//3 step comparator, sorts by score then own Identity comes first, last alphabetically
		Collections.sort(allMembers, new Comparator<HighScoreTableEntry>() {
			public int compare(HighScoreTableEntry a, HighScoreTableEntry b){
				int answer = Float.compare(b.getScore(), a.getScore());
				if (answer == 0){
					if (a.getIdentity().equals(ownIdentity))return -1;
					else if (b.getIdentity().equals(ownIdentity))return 1;
					else {
						return a.getName().compareTo(b.getName());
					}
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
		
		if (ownIdMembers.size() > 0)log.audit("2nd Highscore Table established");
		
		return allScores;
	}
}
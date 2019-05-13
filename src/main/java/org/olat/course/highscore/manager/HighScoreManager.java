/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.highscore.manager;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.id.Identity;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.course.highscore.model.HighScoreRankingResults;
import org.olat.course.highscore.ui.HighScoreTableEntry;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * The Class HighScoreManager.
 * Initial Date:  20.08.2016 <br>
 * @author fkiefer, fabian.kiefer@frentix.com, www.frentix.com
 */
@Service
public class HighScoreManager {

	private static final Logger log = Tracing.createLoggerFor(HighScoreManager.class);
	
	@Autowired
	private UserManager userManager;
	
	/**
	 * Sort rank by score, then by id and last alphabetically, 
	 * determine rank of each member dependent on score,
	 * decide whether there is a second table or not
	 */
	public HighScoreRankingResults sortRankByScore (List<AssessmentEntry>  assessEntries,
			List<HighScoreTableEntry> allMembers, List<HighScoreTableEntry> ownIdMembers,
			List<List<HighScoreTableEntry>> allPodium, List<Integer> ownIdIndices,	
			int tableSize, Identity ownIdentity){
		
		HighScoreTableEntry ownTableEntry = null;

		for (AssessmentEntry assessmentEntry : assessEntries) {
			float score = assessmentEntry.getScore() == null ? 0f : assessmentEntry.getScore().floatValue();
			HighScoreTableEntry tableEntry = new HighScoreTableEntry(score,
					userManager.getUserDisplayName(assessmentEntry.getIdentity()), assessmentEntry.getIdentity());
			if (tableEntry.getIdentity().equals(ownIdentity)) {
				ownTableEntry = tableEntry;
			}
			allMembers.add(tableEntry);
		}
		assessEntries.clear();
		//3 step comparator, sorts by score then own Identity comes first, last alphabetically
		Collections.sort(allMembers, new HighscoreComparator(ownIdentity));
		
		float buffer = -1;
		int index = 0;
//		int rank = 1;
		double[] allScores = new double[allMembers.size()];
		for (int j = 0; j < allMembers.size(); j++) {
			HighScoreTableEntry member = allMembers.get(j);
			if (member.getScore() < buffer){
				index++;
//				rank = j + 1;
			}
			//first three position are put in separate lists, exclude zero scorers
			if (index < 3 && member.getScore() > 0) {
				allPodium.get(index).add(member);
			}
			// finding position rank for own id
			if (member.getIdentity().equals(ownIdentity)){
				ownIdIndices.add(j);
			}
			//setting rank for each member 
			member.setRank(index + 1);
			buffer = member.getScore();
			//adding scores for histogram
			allScores[j] = buffer;
		}
		//only getting member with own id for 2nd table
		ownIdMembers.addAll(allMembers.stream()
				.skip(tableSize)
				.filter(a -> a.getIdentity().equals(ownIdentity))
				.collect(Collectors.toList()));
		
		if (ownIdMembers.size() > 0) {
			log.info(Tracing.M_AUDIT, "2nd Highscore Table established");
		}
		
		return new HighScoreRankingResults(allScores, ownTableEntry);
	}
	
	/**
	 * Process histogram data.
	 *
	 */
	public HighScoreRankingResults processHistogramData(double[] scores, Float lowerBorder, Float upperBorder) {
		try {
			long classwidth;
			// determine natural min, max and thus range
			double max = Math.ceil(Arrays.stream(scores).max().getAsDouble());
			double min = Math.floor(Arrays.stream(scores).min().getAsDouble());
			double range = max - min;
			// use original scores if range is too small else convert results to fit histogram
			if (range <= 20 && range <0) {
				classwidth = 1;
				return new HighScoreRankingResults(scores, classwidth, min);
			} else {
				if(lowerBorder == null) {
					lowerBorder = 0f;
				}
				if(upperBorder == null) {
					upperBorder = (float)max;
				}

				// decrease amount of possible classes to avoid overlapping of large numbers(condition) on x-axis 
				boolean largeNumbers = range > 100d || max >= 1000d;
				int maxnumberofclasses = largeNumbers ? 12 : 20;
				int minnumberofclasses = largeNumbers ? 4 : 5;
				int numberofclasses = 10;
				// primeRange increments range until a natural factor is found or upper/lower boundary is met
				boolean primeRange = true;
				// equalRangeExtend alternates between upper and lower boundary extension
				int equalRangeExtend = 0;
				// check if a value between 20 and 6 is a natural factor of the range
				// if true use it to calculate the class width
				while (primeRange) {
					for (int j = maxnumberofclasses; j > minnumberofclasses; j--) {
						if (range % j == 0) {
							numberofclasses = j;
							primeRange = false;
							break;
						}
					}
					if (!primeRange || range <= 0 || equalRangeExtend > 10E3) {
						break;
					} else if (min - 1 > lowerBorder && equalRangeExtend % 2 == 0) {
						min -= 1;
						range = max - min;
						equalRangeExtend++;
					} else if (max + 1 < upperBorder && equalRangeExtend % 2 == 1) {
						max += 1;
						range = max - min;
						equalRangeExtend++;
					} else {
						equalRangeExtend++;
					}
					// allow one extension if no borders are defined
					primeRange = upperBorder - lowerBorder > 0;
				}
				// steps can only be natural numbers 
				classwidth = Math.round(range / numberofclasses);
				// modified scores are calculated and saved
				double[] allScores = new double[scores.length];
				for (int i = 0; i < scores.length; i++) {
					// determine n-th class to fit the current score result
					double n = Math.ceil((scores[i] - min) / classwidth);
					// calculate higher score to fit the class width
					double newscore = min + (n * classwidth);
					allScores[i] = newscore;
				}
				return new HighScoreRankingResults(allScores, classwidth, min);
			}
		} catch (Exception e) {
			log.error("",e);
			return new HighScoreRankingResults(new double[] {0,1,2,3}, 1L, 0D);
		}
	}

	
	/**
	 * Calculate histogram cutvalue using results from the method (processHistogramData(double[]))
	 *
	 * @param score the score
	 * @return the double
	 */
	public double calculateHistogramCutvalue(double score, long classwidth, double min) {
		if (classwidth != 0) {
			// determine n-th class to fit the current score result
			double n = Math.ceil((score - min) / classwidth);
			// calculate higher score to fit the class width
			double cutvalue = min + (n * classwidth);
			return cutvalue;
		} else {
			return score;
		}		
	}
	
	
	/**
	 * The Class HighscoreComparator.
	 * 3 step comparator, sorts by score then own Identity comes first, last alphabetically
	 */
	private class HighscoreComparator implements Comparator<HighScoreTableEntry> {
		private Identity ownIdentity;

		public HighscoreComparator(Identity ownIdentity) {
			this.ownIdentity = ownIdentity;
		}

		@Override
		public int compare(HighScoreTableEntry a, HighScoreTableEntry b) {
			// 0) catch null values
			if (a == null || a.getIdentity() == null || a.getName() == null) return -1;
			if (b == null || b.getIdentity() == null || b.getName() == null) return -1;
			// 1) sort by score
			int answer = Float.compare(b.getScore(), a.getScore());
			if (answer == 0) {
				// 2) own Identity comes first
				if (a.getIdentity().equals(ownIdentity)) return -1;
				else if (b.getIdentity().equals(ownIdentity)) return 1;
				// 3) sort alphabetically
				else return a.getName().compareTo(b.getName());
			} else {
				return answer;
			}
		}
	}
}


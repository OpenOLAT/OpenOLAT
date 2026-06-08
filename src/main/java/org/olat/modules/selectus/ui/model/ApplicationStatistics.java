/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.model;

import java.util.Arrays;

/**
 * 
 * Initial date: 28 oct. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationStatistics {
	
	private int numOfAssignments = 0;
	private int numOfApplications = 0;
	private int notActive = 0;
	private int notRated = 0;
	private int abstention = 0;
	private int[] ratings = new int[4];
	
	public ApplicationStatistics() {
		Arrays.fill(ratings, 0, ratings.length, 0);
	}
	
	public int getAssignmentsDone() {
		return numOfAssignments - notRated;
	}
	
	public int getApplicationsDone() {
		return numOfApplications - notRated;
	}
	
	public int getNumOfAssignments() {
		return numOfAssignments;
	}
	
	public void incrementNumOfAssignments() {
		numOfAssignments++;
	}
	
	public int getNumOfApplications() {
		return numOfApplications;
	}
	
	public void incrementNumOfApplications() {
		numOfApplications++;
	}
	
	public int getNotActive() {
		return notActive;
	}
	
	public void incrementNotActive() {
		notActive++;
	}
	
	public int getNotRated() {
		return notRated;
	}
	
	public void incrementNotRated() {
		notRated++;
	}
	
	public int getAbstention() {
		return abstention;
	}
	
	public void incrementAbstention() {
		abstention++;
	}
	
	public void incrementRating(int rating) {
		ratings[rating]++;
	}
	
	public int getRating(int rating) {
		return ratings[rating];
	}
	
	public int getRatingPercent(int rating) {
		int r = getRating(rating);
		if(r == 0 || numOfAssignments == 0) {
			return 0;
		}
		float percent = (float)numOfAssignments / r;
		return Math.round(percent);
	}
	
	
	

}

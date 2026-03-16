/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
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

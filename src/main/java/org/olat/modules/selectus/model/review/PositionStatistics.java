/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model.review;

import java.util.ArrayList;
import java.util.List;

import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.PositionRef;

/**
 * 
 * Initial date: 20 févr. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionStatistics {
	
	private final PositionRef position;
	private final List<Reviewer> reviewers;
	private List<ApplicationStatistics> statistics = new ArrayList<>();
	
	public PositionStatistics(PositionRef position, List<Reviewer> reviewers) {
		this.position = position;
		this.reviewers = reviewers;
	}
	
	public PositionRef getPosition() {
		return position;
	}
	
	public List<Reviewer> getReviewers() {
		return reviewers;
	}
	
	public List<ApplicationStatistics> getApplicationsStatistics() {
		return statistics;
	}
	
	public ApplicationStatistics getApplicationStatistics(ApplicationRef appRef) {
		for(ApplicationStatistics appStats:statistics) {
			if(appStats.getApplication().getKey().equals(appRef.getKey())) {
				return appStats;
			}
		}
		return null;
	}
	
	public void addStatistics(ApplicationStatistics appStats) {
		statistics.add(appStats);
	}

}

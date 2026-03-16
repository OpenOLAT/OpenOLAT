/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.model;

/**
 * 
 * Initial date: 16.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationRefereeStats {
	
	private final Long key;
	private final int numOfExperts;
	private final int numOfSubmittedExperts;
	private final int numOfRecommendations;
	private final int numOfSubmittedRecommendations;
	private final int numOfComparativeExperts;
	private final int numOfSubmittedComparativeExperts;
	
	private final int totalSubmitted;
	
	public ApplicationRefereeStats(Long key, int numOfExperts, int numOfSubmittedExperts,
			int numOfRecommendations, int numOfSubmittedRecommendations,
			int numOfComparativeExperts, int numOfSubmittedComparativeExperts,
			int totalSubmitted) {
		this.key = key;
		this.numOfExperts = numOfExperts;
		this.numOfSubmittedExperts = numOfSubmittedExperts;
		this.numOfRecommendations = numOfRecommendations;
		this.numOfSubmittedRecommendations = numOfSubmittedRecommendations;
		this.numOfComparativeExperts = numOfComparativeExperts;
		this.numOfSubmittedComparativeExperts = numOfSubmittedComparativeExperts;
		this.totalSubmitted = totalSubmitted;
	}
	
	public Long getKey() {
		return key;
	}
	
	public int getNumOfSubmittedExperts() {
		return numOfSubmittedExperts;
	}

	public int getNumOfExperts() {
		return numOfExperts;
	}

	public int getNumOfRecommendations() {
		return numOfRecommendations;
	}

	public int getNumOfSubmittedRecommendations() {
		return numOfSubmittedRecommendations;
	}
	
	public int getNumOfComparativeExperts() {
		return numOfComparativeExperts;
	}

	public int getNumOfSubmittedComparativeExperts() {
		return numOfSubmittedComparativeExperts;
	}

	public int getTotalSubmitted() {
		return totalSubmitted;
	}
}

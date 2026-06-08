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

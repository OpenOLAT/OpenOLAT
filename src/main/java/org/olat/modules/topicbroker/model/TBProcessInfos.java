/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.modules.topicbroker.model;

/**
 * 
 * Initial date: 25 Jun 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBProcessInfos {
	
	private Integer numParticipants;
	private Integer numTopics;
	private Integer numSelections;
	private Integer numEnrollments;
	private Integer numEnrollmentsLeft;
	private Integer numEnrollmentsRequired;
	private Integer numWithdraws;
	private Integer priority;
	private Float cost;
	private Float budgetBefore;
	private Float budgetAfter;
	private Integer numTopicsMaxNotReached;
	private Integer numTopicsMinNotReached;
	private Integer numParticipantsMaxNotReached;
	
	public Integer getNumParticipants() {
		return numParticipants;
	}
	
	public void setNumParticipants(Integer numParticipants) {
		this.numParticipants = numParticipants;
	}
	
	public Integer getNumTopics() {
		return numTopics;
	}
	
	public void setNumTopics(Integer numTopics) {
		this.numTopics = numTopics;
	}
	
	public Integer getNumSelections() {
		return numSelections;
	}
	
	public void setNumSelections(Integer numSelections) {
		this.numSelections = numSelections;
	}
	
	public Integer getNumEnrollments() {
		return numEnrollments;
	}
	
	public void setNumEnrollments(Integer numEnrollments) {
		this.numEnrollments = numEnrollments;
	}
	
	public Integer getNumEnrollmentsLeft() {
		return numEnrollmentsLeft;
	}
	
	public void setNumEnrollmentsLeft(Integer numEnrollmentsLeft) {
		this.numEnrollmentsLeft = numEnrollmentsLeft;
	}
	
	public Integer getNumEnrollmentsRequired() {
		return numEnrollmentsRequired;
	}
	
	public void setNumEnrollmentsRequired(Integer numEnrollmentsRequired) {
		this.numEnrollmentsRequired = numEnrollmentsRequired;
	}
	
	public Integer getNumWithdraws() {
		return numWithdraws;
	}
	
	public void setNumWithdraws(Integer numWithdraws) {
		this.numWithdraws = numWithdraws;
	}
	
	public Integer getPriority() {
		return priority;
	}
	
	public void setPriority(Integer priority) {
		this.priority = priority;
	}
	
	public Float getCost() {
		return cost;
	}
	
	public void setCost(Float cost) {
		this.cost = cost;
	}
	
	public Float getBudgetBefore() {
		return budgetBefore;
	}
	
	public void setBudgetBefore(Float budgetBefore) {
		this.budgetBefore = budgetBefore;
	}
	
	public Float getBudgetAfter() {
		return budgetAfter;
	}
	
	public void setBudgetAfter(Float budgetAfter) {
		this.budgetAfter = budgetAfter;
	}
	
	public Integer getNumTopicsMaxNotReached() {
		return numTopicsMaxNotReached;
	}

	public void setNumTopicsMaxNotReached(Integer numTopicsMaxNotReached) {
		this.numTopicsMaxNotReached = numTopicsMaxNotReached;
	}

	public Integer getNumTopicsMinNotReached() {
		return numTopicsMinNotReached;
	}

	public void setNumTopicsMinNotReached(Integer numTopicsMinNotReached) {
		this.numTopicsMinNotReached = numTopicsMinNotReached;
	}

	public Integer getNumParticipantsMaxNotReached() {
		return numParticipantsMaxNotReached;
	}

	public void setNumParticipantsMaxNotReached(Integer numParticipantsMaxNotReached) {
		this.numParticipantsMaxNotReached = numParticipantsMaxNotReached;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("TBProcessInfos [");
		if (numParticipants != null) {
			builder.append("numParticipants=");
			builder.append(numParticipants);
			builder.append(", ");
		}
		if (numTopics != null) {
			builder.append("numTopics=");
			builder.append(numTopics);
			builder.append(", ");
		}
		if (numSelections != null) {
			builder.append("numSelections=");
			builder.append(numSelections);
			builder.append(", ");
		}
		if (numEnrollments != null) {
			builder.append("numEnrollments=");
			builder.append(numEnrollments);
			builder.append(", ");
		}
		if (numEnrollmentsLeft != null) {
			builder.append("numEnrollmentsLeft=");
			builder.append(numEnrollmentsLeft);
			builder.append(", ");
		}
		if (numEnrollmentsRequired != null) {
			builder.append("numEnrollmentsRequired=");
			builder.append(numEnrollmentsRequired);
			builder.append(", ");
		}
		if (numWithdraws != null) {
			builder.append("numWithdraws=");
			builder.append(numWithdraws);
			builder.append(", ");
		}
		if (priority != null) {
			builder.append("priority=");
			builder.append(priority);
			builder.append(", ");
		}
		if (cost != null) {
			builder.append("cost=");
			builder.append(cost);
			builder.append(", ");
		}
		if (budgetBefore != null) {
			builder.append("budgetBefore=");
			builder.append(budgetBefore);
			builder.append(", ");
		}
		if (budgetAfter != null) {
			builder.append("budgetAfter=");
			builder.append(budgetAfter);
			builder.append(", ");
		}
		if (numTopicsMaxNotReached != null) {
			builder.append("numTopicsMaxNotReached=");
			builder.append(numTopicsMaxNotReached);
			builder.append(", ");
		}
		if (numTopicsMinNotReached != null) {
			builder.append("numTopicsMinNotReached=");
			builder.append(numTopicsMinNotReached);
			builder.append(", ");
		}
		if (numParticipantsMaxNotReached != null) {
			builder.append("numParticipantsMaxNotReached=");
			builder.append(numParticipantsMaxNotReached);
		}
		builder.append("]");
		return builder.toString().replace(", ]", "]");
	}

	public static final TBProcessInfos ofParticipants(int numParticipants) {
		TBProcessInfos infos = new TBProcessInfos();
		infos.setNumParticipants(Integer.valueOf(numParticipants));
		return infos;
	}
	
	public static final TBProcessInfos ofTopics(int numTopics) {
		TBProcessInfos infos = new TBProcessInfos();
		infos.setNumTopics(Integer.valueOf(numTopics));
		return infos;
	}
	
	public static final TBProcessInfos ofEnrollments(int numEnrollments) {
		TBProcessInfos infos = new TBProcessInfos();
		infos.setNumEnrollments(Integer.valueOf(numEnrollments));
		return infos;
	}
	
	public static final TBProcessInfos ofPriority(int priority) {
		TBProcessInfos infos = new TBProcessInfos();
		infos.setPriority(Integer.valueOf(priority));
		return infos;
	}
	
	public static final TBProcessInfos ofProcessStart(int numParticipants, int numTopics, int selections) {
		TBProcessInfos infos = new TBProcessInfos();
		infos.setNumParticipants(Integer.valueOf(numParticipants));
		infos.setNumTopics(Integer.valueOf(numTopics));
		infos.setNumSelections(Integer.valueOf(selections));
		return infos;
	}
	
	public static final TBProcessInfos ofProcessEnd(int numEnrollments, int numWithdraws) {
		TBProcessInfos infos = new TBProcessInfos();
		infos.setNumEnrollments(Integer.valueOf(numEnrollments));
		infos.setNumWithdraws(Integer.valueOf(numWithdraws));
		return infos;
	}
	
	public static final TBProcessInfos ofUnpupularity(int numEnrollments, int numEnrollmentsRequired, int numEnrollmentsLeft) {
		TBProcessInfos infos = TBProcessInfos.ofEnrollments(numEnrollments);
		infos.setNumEnrollmentsRequired(Integer.valueOf(numEnrollmentsRequired));
		infos.setNumEnrollmentsLeft(Integer.valueOf(numEnrollmentsLeft));
		return infos;
	}
	
	public static final TBProcessInfos ofStats(long numTopicsMaxNotReached, long numTopicsMinNotReached, long numParticipantsMaxNotReached) {
		TBProcessInfos infos = new TBProcessInfos();
		infos.setNumTopicsMaxNotReached(Integer.valueOf((int)numTopicsMaxNotReached));
		infos.setNumTopicsMinNotReached(Integer.valueOf((int)numTopicsMinNotReached));
		infos.setNumParticipantsMaxNotReached(Integer.valueOf((int)numParticipantsMaxNotReached));
		return infos;
	}

}

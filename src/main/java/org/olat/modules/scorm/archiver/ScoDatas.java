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
package org.olat.modules.scorm.archiver;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 
 * Description:<br>
 * Hold the sco's datamodel of a user. 
 * 
 * <P>
 * Initial Date:  17 august 2009 <br>
 * @author srosse
 */
public class ScoDatas {
	private String itemId;
	private String username;
	private String rawScore;
	private String lessonStatus;
	private String comments;
	private String totalTime;
	private Date lastModifiedDate;
	
	private List<ScoInteraction> interactions = new ArrayList<>();
	private List<ScoObjective> objectives = new ArrayList<>();
	
	public ScoDatas(String itemId, String username) {
		this.itemId = itemId;
		this.username = username;
	}

	public String getItemId() {
		return itemId;
	}

	public String getUsername() {
		return username;
	}
	
	public Date getLastModifiedDate() {
		return lastModifiedDate;
	}

	public void setLastModifiedDate(Date lastModifiedDate) {
		this.lastModifiedDate = lastModifiedDate;
	}
	
	public String getTotalTime() {
		return totalTime;
	}

	public void setTotalTime(String totalTime) {
		this.totalTime = totalTime;
	}

	public String getLessonStatus() {
		return lessonStatus;
	}

	public void setLessonStatus(String lessonStatus) {
		this.lessonStatus = lessonStatus;
	}

	public String getRawScore() {
		return rawScore;
	}

	public void setRawScore(String rawScore) {
		this.rawScore = rawScore;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	public int getNumOfInteractions() {
		return interactions.size();
	}
	
	public int getNumOfObjectives() {
		return objectives.size();
	}
	
	public List<ScoObjective> getObjectives() {
		return objectives;
	}
	
	public ScoObjective getObjective(String objectiveId) {
		if(objectiveId == null) return null;
		for(ScoObjective objective:objectives) {
			if(objectiveId.equals(objective.getId())) {
				return objective;
			}
		}
		return null;
	}
	
	public ScoObjective getObjective(int i) {
		if(objectives.size() <= i) {
			for(int j=objectives.size(); j<=i; j++) {
				objectives.add(j, new ScoObjective(j));
			}
		}
		return objectives.get(i);
	}
	
	public List<ScoInteraction> getInteractions() {
		return interactions;
	}

	public ScoInteraction getInteraction(int i) {
		if(interactions.size() <= i) {
			for(int j=interactions.size(); j<=i; j++) {
				interactions.add(j, new ScoInteraction(j));
			}
		}
		return interactions.get(i);
	}
	
	public ScoInteraction getInteractionByID(String id) {
		for(ScoInteraction s:interactions) {
			if (s.getInteractionId().equals(id)) {
				return s;
			}
		}
		return null;
	}
}

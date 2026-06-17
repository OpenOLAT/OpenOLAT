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
package org.olat.modules.selectus.model.mail;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.SortKey;

import org.olat.modules.selectus.manager.ApplicationMailTemplate;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.Reference;

/**
 * 
 * Initial date: 20.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InvitationVariables {
	
	private Position position;
	private List<Reference> rows;
	private List<Reference> selectedReferences;
	private List<ApplicationLight> applications;
	
	private Date submissionDeadline;
	
	private ApplicationMailTemplate expertTemplate;
	private ApplicationMailTemplate recommendationTemplate;
	private ApplicationMailTemplate comparativeExpertTemplate;
	
	private SortKey sortKey;
	
	public Position getPosition() {
		return position;
	}
	
	public void setPosition(Position position) {
		this.position = position;
	}
	
	public Date getSubmissionDeadline() {
		return submissionDeadline;
	}

	public void setSubmissionDeadline(Date submissionDeadline) {
		this.submissionDeadline = submissionDeadline;
	}

	public List<Reference> getRows() {
		return rows;
	}
	
	public void setRows(List<Reference> rows) {
		this.rows = rows;
	}

	public List<Reference> getSelectedReferences() {
		return selectedReferences;
	}

	public void setSelectedReferences(List<Reference> selectedReferences) {
		this.selectedReferences = selectedReferences;
	}

	public ApplicationMailTemplate getExpertTemplate() {
		return expertTemplate;
	}

	public void setExpertTemplate(ApplicationMailTemplate expertTemplate) {
		this.expertTemplate = expertTemplate;
	}

	public ApplicationMailTemplate getComparativeExpertTemplate() {
		return comparativeExpertTemplate;
	}

	public void setComparativeExpertTemplate(ApplicationMailTemplate comparativeExpertTemplate) {
		this.comparativeExpertTemplate = comparativeExpertTemplate;
	}

	public List<ApplicationLight> getApplications() {
		return applications;
	}

	public void setApplications(List<ApplicationLight> applications) {
		this.applications = applications;
	}

	public ApplicationMailTemplate getRecommendationTemplate() {
		return recommendationTemplate;
	}

	public void setRecommendationTemplate(ApplicationMailTemplate recommendationTemplate) {
		this.recommendationTemplate = recommendationTemplate;
	}

	public SortKey getSortKey() {
		return sortKey;
	}

	public void setSortKey(SortKey sortKey) {
		this.sortKey = sortKey;
	}
}

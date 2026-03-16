/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
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

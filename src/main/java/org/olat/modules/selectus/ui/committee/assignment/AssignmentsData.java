/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.committee.assignment;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.id.Identity;

import org.olat.modules.selectus.AssignmentMethods;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.RecruitingMailTemplate;

/**
 * 
 * Initial date: 24 oct. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssignmentsData {
	
	private final Position position;
	private List<ApplicationLight> applications;
	
	private List<Identity> assigneeList;
	private Integer maximumAssignments;
	private Integer additionalAssignments;
	private Spreading spreading = Spreading.total;
	private AssignmentMethods assignmentMethod = AssignmentMethods.manual;
	private RecruitingMailTemplate mailTemplate;
	
	public AssignmentsData(Position position, List<ApplicationLight> applications, AssignmentMethods assignmentMethod) {
		this.position = position;
		this.applications = applications;
		this.assignmentMethod = assignmentMethod;
	}
	
	public Position getPosition() {
		return position;
	}
	
	public List<ApplicationLight> getApplications() {
		return new ArrayList<>(applications);
	}

	public List<Identity> getAssigneeList() {
		return assigneeList;
	}

	public void setAssigneeList(List<Identity> assigneeList) {
		this.assigneeList = assigneeList;
	}

	public AssignmentMethods getAssignmentMethod() {
		return assignmentMethod;
	}

	public void setAssignmentMethod(AssignmentMethods assignmentMethod) {
		this.assignmentMethod = assignmentMethod;
	}

	public Spreading getSpreading() {
		return spreading;
	}

	public void setSpreading(Spreading spreading) {
		this.spreading = spreading;
	}

	public Integer getMaximumAssignments() {
		return maximumAssignments;
	}

	public void setMaximumAssignments(Integer maximumAssignments) {
		this.maximumAssignments = maximumAssignments;
	}

	public Integer getAdditionalAssignments() {
		return additionalAssignments;
	}

	public void setAdditionalAssignments(Integer additionalAssignments) {
		this.additionalAssignments = additionalAssignments;
	}

	public RecruitingMailTemplate getMailTemplate() {
		return mailTemplate;
	}

	public void setMailTemplate(RecruitingMailTemplate mailTemplate) {
		this.mailTemplate = mailTemplate;
	}



	public enum Spreading {
		additional,
		total
		
	}

}

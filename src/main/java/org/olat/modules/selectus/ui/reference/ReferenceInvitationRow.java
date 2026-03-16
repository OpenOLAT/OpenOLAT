/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.reference;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.model.ReferenceType;

/**
 * 
 * Initial date: 8 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReferenceInvitationRow {
	
	private final Reference reference;
	private List<Application> applications;
	
	public ReferenceInvitationRow(Reference reference) {
		this.reference = reference;
	}
	
	public Long getReferenceKey() {
		return reference.getKey();
	}
	
	public Reference getReference() {
		return reference;
	}
	
	public ReferenceType getReferenceType() {
		return reference.getReferenceType();
	}
	
	public String getEmail() {
		return reference.getEmail();
	}

	public ReferenceStatus getReferenceStatus() {
		return reference.getReferenceStatus();
	}

	public Date getDateInvitation() {
		return reference.getDateInvitation();
	}

	public Date getSubmissionDeadline() {
		return reference.getSubmissionDeadline();
	}

	public Date getDateLastReminder() {
		return reference.getDateLastReminder();
	}

	public Application getApplication() {
		return reference.getApplication();
	}

	public List<Application> getApplications() {
		return applications;
	}

	public void setApplications(List<Application> applications) {
		this.applications = applications;
	}
	
	public void addApplication(Application application) {
		if(applications == null) {
			applications = new ArrayList<>();
		}
		applications.add(application);
	}
}

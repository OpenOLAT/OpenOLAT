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

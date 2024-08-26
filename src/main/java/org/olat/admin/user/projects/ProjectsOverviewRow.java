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
package org.olat.admin.user.projects;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectStatus;

/**
 * Initial date: Aug 23, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ProjectsOverviewRow {

	private final String title;
	private final String externalRef;
	private final ProjectStatus status;
	private final ProjProject project;
	private String translatedStatus;
	private String roles;
	private Date registrationDate;
	private Date lastActivityDate;
	private FormLink removeActionEl;

	public ProjectsOverviewRow(ProjProject project) {
		this.project = project;
		this.title = project.getTitle();
		this.externalRef = project.getExternalRef();
		this.status = project.getStatus();
	}

	public String getTitle() {
		return title;
	}

	public String getExternalRef() {
		return externalRef;
	}

	public ProjectStatus getStatus() {
		return status;
	}

	public String getRoles() {
		return roles;
	}

	public void setRoles(String roles) {
		this.roles = roles;
	}

	public Date getRegistrationDate() {
		return registrationDate;
	}

	public void setRegistrationDate(Date registrationDate) {
		this.registrationDate = registrationDate;
	}

	public Date getLastActivityDate() {
		return lastActivityDate;
	}

	public void setLastActivityDate(Date lastActivityDate) {
		this.lastActivityDate = lastActivityDate;
	}

	public FormLink getRemoveActionEl() {
		return removeActionEl;
	}

	public void setRemoveActionEl(FormLink removeActionEl) {
		this.removeActionEl = removeActionEl;
	}

	public String getTranslatedStatus() {
		return translatedStatus;
	}

	public void setTranslatedStatus(String translatedStatus) {
		this.translatedStatus = translatedStatus;
	}

	public ProjProject getProject() {
		return project;
	}
}

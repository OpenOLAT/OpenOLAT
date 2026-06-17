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

import java.util.List;

import org.olat.core.gui.components.form.flexible.FormItemCollection;
import org.olat.core.gui.components.form.flexible.elements.DownloadLink;
import org.olat.core.gui.components.form.flexible.elements.FormLink;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.ui.model.AppToCategory;

/**
 * 
 * Initial date: 15.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionReferenceRow {

	private String applicationUrl;
	private List<AppToCategory> tags;
	
	private final Reference reference;
	private final Application application;
	private final List<Application> applications;
	private final boolean refereeComment;
	
	private final FormLink sendLink;
	private final DownloadLink documentLink;
	private final FormItemCollection applicationsLinks;
	
	public PositionReferenceRow(Reference reference, String applicationUrl, List<Application> applications,
			boolean refereeComment, FormLink sendLink, DownloadLink documentLink, FormItemCollection applicationsLinks) {
		this.reference = reference;
		this.application = reference.getApplication();
		this.applications = applications;
		this.applicationUrl = applicationUrl;
		this.sendLink = sendLink;
		this.documentLink = documentLink;
		this.refereeComment = refereeComment;
		this.applicationsLinks = applicationsLinks;
	}

	public Reference getReference() {
		return reference;
	}
	
	public Application getApplication() {
		return application;
	}
	
	public List<Application> getApplications() {
		return applications;
	}
	
	public String getApplicationUrl() {
		return applicationUrl;
	}
	
	public boolean isRefereeCommentAvailable() {
		return refereeComment;
	}

	public FormLink getSendLink() {
		return sendLink;
	}
	
	public DownloadLink getDocumentLink() {
		return documentLink;
	}
	
	public FormItemCollection getApplicationsLinks() {
		return applicationsLinks;
	}

	public List<AppToCategory> getCategories() {
		return tags;
	}

	public void setCategorie(List<AppToCategory> tags) {
		this.tags = tags;
	}
}

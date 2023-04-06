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
package org.olat.core.commons.services.notifications.ui;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;

/**
 * Initial date: Mär 23, 2023
 *
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class NotificationSubscriptionRow {

	private Long key;
	private String section;
	private String addDesc;
	private FormToggle statusToggle;
	private FormLink learningResource;
	private FormLink subRes;
	private Date creationDate;
	private Date lastEmail;
	private FormLink deleteLink;

	public NotificationSubscriptionRow(String section, FormLink learningResource, FormLink subRes,
									   String addDesc, FormToggle statusToggle, Date creationDate,
									   Date lastEmail, FormLink deleteLink, Long subKey) {
		this.key = subKey;
		this.section = section;
		this.learningResource = learningResource;
		this.subRes = subRes;
		this.addDesc = addDesc;
		this.statusToggle = statusToggle;
		this.creationDate = creationDate;
		this.lastEmail = lastEmail;
		this.deleteLink = deleteLink;
	}

	public Long getKey() {
		return key;
	}

	public String getSection() {
		return section;
	}

	public FormLink getLearningResource() {
		return learningResource;
	}

	public FormLink getSubRes() {
		return subRes;
	}

	public String getAddDesc() {
		return addDesc;
	}

	public FormToggle getStatusToggle() {
		return statusToggle;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public Date getLastEmail() {
		return lastEmail;
	}

	public FormLink getDeleteLink() {
		return deleteLink;
	}
}

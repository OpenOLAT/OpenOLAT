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
package org.olat.modules.project.ui;

import java.util.Date;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjectStatus;

/**
 * 
 * Initial date: 23 Nov 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjProjectRow {
	
	private final Long key;
	private final String externalRef;
	private final String title;
	private final String teaser;
	private final ProjectStatus status;
	private String translatedStatus;
	private Date lastActivityDate;
	private String modified;
	private String ownersNames;
	private String url;
	private Component userPortraits;
	private FormLink selectLink;
	
	public ProjProjectRow(ProjProject project) {
		this.key = project.getKey();
		this.externalRef = project.getExternalRef();
		this.title = project.getTitle();
		this.teaser = project.getTeaser();
		this.status = project.getStatus();
	}

	public Long getKey() {
		return key;
	}

	public String getExternalRef() {
		return externalRef;
	}
	
	public String getTitle() {
		return title;
	}

	public String getTeaser() {
		return teaser;
	}
	
	public ProjectStatus getStatus() {
		return status;
	}

	public String getTranslatedStatus() {
		return translatedStatus;
	}

	public void setTranslatedStatus(String translatedStatus) {
		this.translatedStatus = translatedStatus;
	}

	public Date getLastActivityDate() {
		return lastActivityDate;
	}

	public void setLastActivityDate(Date lastActivityDate) {
		this.lastActivityDate = lastActivityDate;
	}

	public String getModified() {
		return modified;
	}

	public void setModified(String modified) {
		this.modified = modified;
	}

	public String getOwnersNames() {
		return ownersNames;
	}

	public void setOwnersNames(String ownersNames) {
		this.ownersNames = ownersNames;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getUserPortraitsName() {
		return userPortraits != null? userPortraits.getComponentName(): null;
	}

	public Component getUserPortraits() {
		return userPortraits;
	}

	public void setUserPortraits(Component userPortraits) {
		this.userPortraits = userPortraits;
	}

	public String getSelectLinkName() {
		return selectLink != null? selectLink.getComponent().getComponentName(): null;
	}

	public FormLink getSelectLink() {
		return selectLink;
	}

	public void setSelectLink(FormLink selectLink) {
		this.selectLink = selectLink;
	}
	
}

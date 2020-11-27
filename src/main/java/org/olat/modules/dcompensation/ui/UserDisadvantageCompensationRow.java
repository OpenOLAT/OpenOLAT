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
package org.olat.modules.dcompensation.ui;

import java.util.Date;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.modules.dcompensation.DisadvantageCompensation;
import org.olat.modules.dcompensation.DisadvantageCompensationStatusEnum;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 22 sept. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserDisadvantageCompensationRow {
	
	private final String creatorFullName;
	private final DisadvantageCompensation compensation;
	
	private final FormLink toolsLink;
	
	public UserDisadvantageCompensationRow(DisadvantageCompensation compensation,
			String creatorFullName, FormLink toolsLink) {
		this.compensation = compensation;
		this.creatorFullName = creatorFullName;
		this.toolsLink = toolsLink;
	}
	
	public Long getKey() {
		return compensation.getKey();
	}
	
	public String getCreatorFullName() {
		return creatorFullName;
	}
	
	public Date getCreationDate() {
		return compensation.getCreationDate();
	}
	
	public RepositoryEntryRef getEntry() {
		return compensation.getEntry();
	}
	
	public Long getEntryKey() {
		return compensation.getEntry().getKey();
	}
	
	public String getEntryDisplayName() {
		return compensation.getEntry().getDisplayname();
	}
	
	public String getEntryExternalRef() {
		return compensation.getEntry().getExternalRef();
	}
	
	public String getCourseElementId() {
		return compensation.getSubIdent();
	}
	
	public String getCourseElement() {
		return compensation.getSubIdentName();
	}
	
	public Integer getExtraTime() {
		return compensation.getExtraTime();
	}
	
	public String getApprovedBy() {
		return compensation.getApprovedBy();
	}
	
	public Date getApprovalDate() {
		return compensation.getApproval();
	}
	
	public DisadvantageCompensationStatusEnum getStatus() {
		return compensation.getStatusEnum();
	}
	
	public FormLink getToolsLink() {
		return toolsLink;
	}
	
	public DisadvantageCompensation getCompensation() {
		return compensation;
	}

}

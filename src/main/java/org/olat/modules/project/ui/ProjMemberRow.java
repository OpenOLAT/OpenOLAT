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

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Identity;
import org.olat.modules.project.ProjMemberInfo;

/**
 * 
 * Initial date: 1 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjMemberRow {

	private final Identity identity;
	private final Date lastVisitDate;
	private String translatedRoles;
	
	private FormLink toolsLink;
	
	public ProjMemberRow(ProjMemberInfo member) {
		this.identity = member.getIdentity();
		this.lastVisitDate = member.getLastVisitDate();
	}

	public Identity getIdentity() {
		return identity;
	}

	public String getTranslatedRoles() {
		return translatedRoles;
	}

	public void setTranslatedRoles(String translatedRoles) {
		this.translatedRoles = translatedRoles;
	}

	public Date getLastVisitDate() {
		return lastVisitDate;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}

	@Override
	public int hashCode() {
		return identity == null ? 2365913 : identity.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof ProjMemberRow) {
			ProjMemberRow row = (ProjMemberRow)obj;
			return identity != null && identity.equals(row.getIdentity());
		}
		return false;
	}
}

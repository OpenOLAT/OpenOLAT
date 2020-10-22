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
package org.olat.modules.curriculum.ui.member;

import java.util.Date;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.id.Identity;
import org.olat.group.ui.main.CourseMembership;

/**
 * 
 * Initial date: 19 oct. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumMemberRow implements IdentityRef {

	private Date firstTime;
	private final Identity identity;
	private final CourseMembership membership;
	
	private FormLink toolsLink;
	
	public CurriculumMemberRow(Identity identity, CourseMembership membership, Date firstTime) {
		this.identity = identity;
		this.membership = membership;
		this.firstTime = firstTime;
	}
	
	@Override
	public Long getKey() {
		return identity == null ? null : identity.getKey();
	}
	
	public Identity getIdentity() {
		return identity;
	}

	public CourseMembership getMembership() {
		return membership;
	}

	public Date getFirstTime() {
		return firstTime;
	}

	public void setFirstTime(Date firstTime) {
		this.firstTime = firstTime;
	}

	public FormLink getToolsLink() {
		return toolsLink;
	}

	public void setToolsLink(FormLink toolsLink) {
		this.toolsLink = toolsLink;
	}

	@Override
	public int hashCode() {
		return identity == null ? 2365912 : identity.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof CurriculumMemberRow) {
			CurriculumMemberRow row = (CurriculumMemberRow)obj;
			return identity != null && identity.equals(row.getIdentity());
		}
		return false;
	}
}

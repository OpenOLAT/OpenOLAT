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
package org.olat.modules.portfolio.model;

import org.olat.basesecurity.Invitation;
import org.olat.core.id.Identity;
import org.olat.modules.ceditor.Page;
import org.olat.modules.ceditor.ContentRoles;
import org.olat.modules.ceditor.ContentElement;
import org.olat.modules.ceditor.ContentElementType;
import org.olat.modules.portfolio.Section;

/**
 * 
 * Initial date: 16.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AccessRights {
	
	private Long binderKey;
	private Long sectionKey;
	private Long pageKey;
	
	private Identity identity;
	private Invitation invitation;
	private ContentRoles role;
	
	public AccessRights() {
		//
	}
	
	public Long getBinderKey() {
		return binderKey;
	}
	
	public void setBinderKey(Long binderKey) {
		this.binderKey = binderKey;
	}
	
	public Long getSectionKey() {
		return sectionKey;
	}
	
	public void setSectionKey(Long sectionKey) {
		this.sectionKey = sectionKey;
	}
	
	public Long getPageKey() {
		return pageKey;
	}
	
	public void setPageKey(Long pageKey) {
		this.pageKey = pageKey;
	}
	
	public ContentElementType getType() {
		if(pageKey != null) {
			return ContentElementType.page;
		} else if(sectionKey != null) {
			return ContentElementType.section;
		} else if(binderKey != null) {
			return ContentElementType.binder;
		}
		return null;
	}
	
	/**
	 * 
	 * @param element
	 * @return
	 */
	public boolean matchElementAndAncestors(ContentElement element) {
		if(element == null) {
			return false;
		}
		if(element.getType() == ContentElementType.page) {
			if(ContentElementType.page == getType() && pageKey != null && pageKey.equals(element.getKey())) {
				return true;
			}
			element = ((Page)element).getSection();
		}
		if(element == null) {
			return false;
		}
		if(element.getType() == ContentElementType.section) {
			if(ContentElementType.section == getType() && sectionKey != null && sectionKey.equals(element.getKey())) {
				return true;
			}
			element = ((Section)element).getBinder();
		}
		if(element == null) {
			return false;
		}
		if(element.getType() == getType() && element.getType() == ContentElementType.binder) {
			return binderKey != null && binderKey.equals(element.getKey());
		}
		return false;
	}
	
	public Identity getIdentity() {
		return identity;
	}
	
	public void setIdentity(Identity identity) {
		this.identity = identity;
	}
	
	public Invitation getInvitation() {
		return invitation;
	}

	public void setInvitation(Invitation invitation) {
		this.invitation = invitation;
	}

	public ContentRoles getRole() {
		return role;
	}
	
	public void setRole(ContentRoles role) {
		this.role = role;
	}
}

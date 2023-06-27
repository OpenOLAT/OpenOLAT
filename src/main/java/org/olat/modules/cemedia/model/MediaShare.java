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
package org.olat.modules.cemedia.model;

import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.group.BusinessGroup;
import org.olat.modules.cemedia.MediaToGroupRelation;
import org.olat.modules.cemedia.MediaToGroupRelation.MediaToGroupRelationType;

/**
 * 
 * Initial date: 7 juin 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaShare {
	
	private Identity user;
	private Organisation organisation;
	private BusinessGroup businessGroup;
	private MediaToGroupRelation relation;
	
	public MediaShare(MediaToGroupRelation relation, Identity user) {
		this.user = user;
		this.relation = relation;
	}
	
	public MediaShare(MediaToGroupRelation relation, BusinessGroup businessGroup) {
		this.relation = relation;
		this.businessGroup = businessGroup;
	}
	
	public MediaShare(MediaToGroupRelation relation, Organisation organisation) {
		this.relation = relation;
		this.organisation = organisation;
	}
	
	public MediaToGroupRelationType getType() {
		if(user != null) {
			return MediaToGroupRelationType.USER;
		} else if(businessGroup != null) {
			return MediaToGroupRelationType.BUSINESS_GROUP;
		} else if(organisation != null) {
			return MediaToGroupRelationType.ORGANISATION;
		}
		return relation.getType();
	}
	
	public Identity getUser() {
		return user;
	}

	public void setUser(Identity user) {
		this.user = user;
	}

	public Organisation getOrganisation() {
		return organisation;
	}

	public void setOrganisation(Organisation organisation) {
		this.organisation = organisation;
	}

	public BusinessGroup getBusinessGroup() {
		return businessGroup;
	}

	public void setBusinessGroup(BusinessGroup businessGroup) {
		this.businessGroup = businessGroup;
	}

	public MediaToGroupRelation getRelation() {
		return relation;
	}
	
	public void setRelation(MediaToGroupRelation relation) {
		this.relation = relation;
	}
}

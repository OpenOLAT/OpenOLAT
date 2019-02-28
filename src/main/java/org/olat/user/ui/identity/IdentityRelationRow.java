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
package org.olat.user.ui.identity;

import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.IdentityToIdentityRelationManagedFlag;
import org.olat.basesecurity.RelationRole;
import org.olat.core.id.Identity;
import org.olat.user.UserPropertiesRow;
import org.olat.user.propertyhandlers.UserPropertyHandler;

/**
 * 
 * Initial date: 30 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityRelationRow {

	private final Long relationKey;
	private final String relationLabel;
	private final RelationRole relationRole;
	private final IdentityToIdentityRelationManagedFlag[] managedFlags;
	
	private final UserPropertiesRow sourceIdentity;
	private final UserPropertiesRow targetIdentity;
	
	public IdentityRelationRow(Long relationKey, Identity sourceIdentity, Identity targetIdentity,
			RelationRole relationRole, String relationLabel, IdentityToIdentityRelationManagedFlag[] managedFlags,
			List<UserPropertyHandler> userPropertyHandlers, Locale locale) {
		this.relationRole = relationRole;
		this.relationKey = relationKey;
		this.managedFlags = managedFlags;
		this.relationLabel = relationLabel;
		this.sourceIdentity = new UserPropertiesRow(sourceIdentity, userPropertyHandlers, locale);
		this.targetIdentity = new UserPropertiesRow(targetIdentity, userPropertyHandlers, locale);
	}

	public  Long getRelationKey() {
		return relationKey;
	}
	
	public String getRelationLabel() {
		return relationLabel;
	}
	
	public String getRelationRoleName() {
		return relationRole.getRole();
	}
	
	public RelationRole getRelationRole() {
		return relationRole;
	}
	
	public IdentityToIdentityRelationManagedFlag[] getManagedFlags() {
		return managedFlags;
	}

	public UserPropertiesRow getSourceIdentity() {
		return sourceIdentity;
	}

	public UserPropertiesRow getTargetIdentity() {
		return targetIdentity;
	}

	@Override
	public int hashCode() {
		return relationKey.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) {
			return true;
		}
		if(obj instanceof IdentityRelationRow) {
			IdentityRelationRow row = (IdentityRelationRow)obj;
			return relationKey.equals(row.relationKey);
		}
		return false;
	}
}

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
public class IdentityRelationRow extends UserPropertiesRow {

	private final RelationRole relationRole;
	private final Long relationKey;
	private final boolean asSource;
	private final IdentityToIdentityRelationManagedFlag[] managedFlags;
	
	public IdentityRelationRow(boolean asSource, Long relationKey, Identity identity, RelationRole relationRole,
			IdentityToIdentityRelationManagedFlag[] managedFlags, List<UserPropertyHandler> userPropertyHandlers,
			Locale locale) {
		super(identity, userPropertyHandlers, locale);
		this.relationRole = relationRole;
		this.relationKey = relationKey;
		this.managedFlags = managedFlags;
		this.asSource = asSource;
	}
	
	public boolean isAsSource() {
		return asSource;
	}
	
	public  Long getRelationKey() {
		return relationKey;
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
}

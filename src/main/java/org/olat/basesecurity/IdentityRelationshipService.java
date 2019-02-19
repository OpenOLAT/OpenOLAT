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
package org.olat.basesecurity;

import java.util.List;
import java.util.Locale;

import org.olat.core.id.Identity;

/**
 * 
 * The service which manage identity to identity relationships. 
 * 
 * Initial date: 29 janv. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface IdentityRelationshipService {
	
	public RelationRole createRole(String role, List<RelationRight> rights);
	
	public RelationRole createRole(String role, String externalId, String externalRef,
			RelationRoleManagedFlag[] managedFlags, List<RelationRight> rights);
	
	public RelationRole updateRole(RelationRole relationRole, List<RelationRight> rights);
	
	public RelationRole getRole(Long key);
	
	public List<RelationRole> getAvailableRoles();
	
	public List<RelationRight> getAvailableRights();
	
	public String getTranslatedName(RelationRight right, Locale locale);
	
	public boolean isInUse(RelationRole relationRole);
	
	public void deleteRole(RelationRole role);

	public IdentityToIdentityRelation addRelation(Identity source, Identity target, RelationRole relationRole,
			String externalId, IdentityToIdentityRelationManagedFlag[] managedFlags);
	
	public void addRelations(Identity source, Identity target, List<RelationRole> relationRoles);
	
	public void removeRelation(IdentityRef source, IdentityRef target, RelationRole relationRole);
	
	public IdentityToIdentityRelation getRelation(Long relationKey);
	
	public void deleteRelation(IdentityToIdentityRelation relation);
	
	/**
	 * 
	 * @param asSource
	 * @return
	 */
	public List<IdentityToIdentityRelation> getRelationsAsSource(IdentityRef asSource);
	
	public List<IdentityToIdentityRelation> getRelationsAsTarget(IdentityRef asTarget);
	
	

}

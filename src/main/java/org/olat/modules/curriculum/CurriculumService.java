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
package org.olat.modules.curriculum;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.modules.curriculum.model.CurriculumElementMembershipChange;
import org.olat.modules.curriculum.model.CurriculumElementRepositoryEntryViews;
import org.olat.modules.curriculum.model.CurriculumMember;
import org.olat.modules.curriculum.model.CurriculumSearchParameters;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface CurriculumService {
	
	/**
	 * Create and persist a curriculum
	 * @param identifier The identifier
	 * @param displayName The name
	 * @param description The description
	 * @param organisation The organisation
	 * @return A persisted curriculum
	 */
	public Curriculum createCurriculum(String identifier, String displayName, String description, Organisation organisation);
	
	public Curriculum getCurriculum(CurriculumRef ref);
	
	public Curriculum updateCurriculum(Curriculum curriculum);
	
	public List<Curriculum> getCurriculums(CurriculumSearchParameters params);
	
	/**
	 * Get the list of members of the specified curriculum with their roles.
	 * 
	 * @param curriculum The curriculum
	 * @return A list of memberships
	 */
	public List<CurriculumMember> getMembers(CurriculumRef curriculum);
	
	/**
	 * Get the list of members of the specified curriculum with the specified
	 * role.
	 * 
	 * @param curriculum The curriculum
	 * @param role The role (mandatory)
	 * @return
	 */
	public List<Identity> getMembersIdentity(CurriculumRef curriculum, CurriculumRoles role);
	
	/**
	 * Add a member to the curriculum with the specified role.
	 * 
	 * @param curriculum The curriculum
	 * @param identity The identity which member
	 * @param role The role
	 */
	public void addMember(Curriculum curriculum, Identity identity, CurriculumRoles role);
	
	/**
	 * Remove a member of the curriculum with the specified role.
	 * 
	 * @param curriculum The curriculum
	 * @param identity The identity which loose its membership
	 * @param role The role
	 */
	public void removeMember(Curriculum curriculum, Identity member, CurriculumRoles role);
	
	/**
	 * Remove all memberships of the curriculum for the specified member.
	 * The method doesn't propagate to the elements of the curriculum.
	 *  
	 * @param curriculum The curriculum
	 * @param member The identity which loose its memberships
	 */
	public void removeMember(Curriculum curriculum, IdentityRef member);
	
	/**
	 * The list of all types available.
	 * 
	 * @return A list of curriculum element types
	 */
	public List<CurriculumElementType> getCurriculumElementTypes();
	
	/**
	 * Load the curriculum element type with the specified primary key.
	 * 
	 * @param ref The reference of the type
	 * @return A curriculum element type
	 */
	public CurriculumElementType getCurriculumElementType(CurriculumElementTypeRef typeRef);
	
	public CurriculumElementType createCurriculumElementType(String identifier, String displayName, String description, String externalId);

	/**
	 * Update only the curriculum element type with the allowed sub-types.
	 * 
	 * @param elementType The curriculum element type to update
	 * @return The merged curriculum element type
	 */
	public CurriculumElementType updateCurriculumElementType(CurriculumElementType elementType);
	
	/**
	 * Update only the curriculum element type and the relations to the allowed sub-types.
	 * 
	 * @param elementType The curriculum element type to updates
	 * @param allowedSubTypes The allowed sub-types
	 * @return A merged curriculum element type
	 */
	public CurriculumElementType updateCurriculumElementType(CurriculumElementType elementType, List<CurriculumElementType> allowedSubTypes);
	
	/**
	 * Add a sub-type in the list of allowed sub-types of the specified
	 * curriculum element type.
	 * 
	 * @param parentType The parent curriculum element type
	 * @param allowedSubType The sub-type to allow
	 */
	public void allowCurriculumElementSubType(CurriculumElementType parentType, CurriculumElementType allowedSubType);
	
	/**
	 * Remove a sub-type of the list of allowed sub-types in the specified
	 * curriculum element type.
	 * 
	 * @param parentType The parent curriculum element type
	 * @param allowedSubType The sub-type to remove
	 */
	public void disallowCurriculumElementSubType(CurriculumElementType parentType, CurriculumElementType disallowedSubType);
	
	
	public CurriculumElementType cloneCurriculumElementType(CurriculumElementTypeRef typeRef);
	
	public boolean deleteCurriculumElementType(CurriculumElementTypeRef typeRef);
	
	
	public CurriculumElement createCurriculumElement(String identifier, String displayName, Date beginDate, Date endDate,
			CurriculumElementRef parent, CurriculumElementType elementType, Curriculum curriculum);
	
	
	public CurriculumElement getCurriculumElement(CurriculumElementRef element);
	
	/**
	 * Return all the elements of a curriculum, but flat.
	 * 
	 * @param curriculum The curriculum
	 * @return A list of curriculum elements
	 */
	public List<CurriculumElement> getCurriculumElements(CurriculumRef curriculum);
	
	/**
	 * Retrieve the children elements of the specified curriculum element.
	 * 
	 * @param parentElement the parent element
	 * @return A list of curriculum elements
	 */
	public List<CurriculumElement> getCurriculumElements(CurriculumElementRef parentElement);
	
	/**
	 * Return all the curriculum elements linked to the specified repository entry.
	 * The method fetch the curriculums and organizations associated with elements.
	 * 
	 * @param entry A repository entry
	 * @return A list of curriculum elements 
	 */
	public List<CurriculumElement> getCurriculumElements(RepositoryEntry entry);
	
	/**
	 * Search curriculum elements in all curriculums. The search is an exact match (think about Syncher).
	 * 
	 * @param externalId The external id (optional)
	 * @param identifier The identifier (optional)
	 * @param key The primary (optional)
	 * @return A list of curriculum elements
	 */
	public List<CurriculumElement> searchCurriculumElements(String externalId, String identifier, Long key);

	/**
	 * Return the parent line of the specified curriculum element.
	 * 
	 * @param element A curriculum element
	 * @return A list of curriculum elements
	 */
	public List<CurriculumElement> getCurriculumElementParentLine(CurriculumElement element);
	
	public CurriculumElement updateCurriculumElement(CurriculumElement element);
	
	/**
	 * 
	 * @param elementToMove The element to move
	 * @param newParent The new parent or null if root
	 */
	public CurriculumElement moveCurriculumElement(CurriculumElement elementToMove, CurriculumElement newParent);
	
	/**
	 * The list of members of the specified curriculum element.
	 * 
	 * @param element The curriculum element
	 * @return The list of memberships
	 */
	public List<CurriculumMember> getMembers(CurriculumElement element);
	
	/**
	 * The list of members of the specified curriculum element with the specified role.
	 * 
	 * @param element The curriculum element
	 * @param role The role
	 * @return The list of memberships
	 */
	public List<Identity> getMembersIdentity(CurriculumElementRef element, CurriculumRoles role);
	
	public List<CurriculumElementMembership> getCurriculumElementMemberships(Collection<CurriculumElement> elements, Identity... identities);
	
	public void updateCurriculumElementMemberships(Identity doer, Roles roles, List<CurriculumElementMembershipChange> changes);
	
	/**
	 * Add a member with the specified role to the curriculum element. The
	 * inheritance mode of the membership is per default "none".
	 * 
	 * @param element The curriculum element
	 * @param member The identity to make a member of
	 * @param role The role of the member
	 */
	public void addMember(CurriculumElement element, Identity member, CurriculumRoles role);
	
	/**
	 * Remove a member of the curriculum element and discard all its roles.
	 * 
	 * @param element The curriculum element
	 * @param member The identity to remove
	 */
	public void removeMember(CurriculumElement element, IdentityRef member);
	
	/**
	 * Remove the membership of a user with the specified role.
	 * 
	 * @param element The curriculum element
	 * @param member The identity which loose the membership
	 * @param role The role
	 */
	public void removeMember(CurriculumElement element, IdentityRef member, CurriculumRoles role);
	
	/**
	 * Remove the members of the curriculum elements linked to the repository entry.<br>
	 * The method respect the managed flags!
	 * 
	 * @param entry The repository entry
	 * @param members The memberss
	 */
	public void removeMembers(CurriculumElement element, List<Identity> members);
	
	/**
	 * The all list of repository entries hold by the specified curriculum element.
	 * 
	 * @param element The curriculum element
	 * @return A list of repository entries
	 */
	public List<RepositoryEntry> getRepositoryEntries(CurriculumElementRef element);
	
	/**
	 * Check if the repository entry is already in relation with the specified
	 * curriculum element.
	 * 
	 * @param element The curriculum element
	 * @param entry The repository entry
	 * @return True if the repository entry and curriculum element share a group
	 */
	public boolean hasRepositoryEntry(CurriculumElement element, RepositoryEntryRef entry);
	
	/**
	 * This will add a relation between the curriculum element and the repository
	 * entry and it will add the base group of the curriculum to the set of groups
	 * of the repository entry.
	 * 
	 * 
	 * @param element The curriculum element
	 * @param entry The repository entry
	 * @param master If the relation is the master one
	 */
	public void addRepositoryEntry(CurriculumElement element, RepositoryEntryRef entry, boolean master);
	

	public void removeRepositoryEntry(CurriculumElement element, RepositoryEntryRef entry);
	
	/**
	 * The list of taxonomy levels of the curriculum element.
	 * 
	 * @param element The curriculum element
	 * @return A list of taxonomy levels
	 */
	public List<TaxonomyLevel> getTaxonomy(CurriculumElement element);
	
	/**
	 * Remove from the list the curriculum elements which are not manageable with the specified roles.
	 * @param elements A list of curriculum elements with the organization loaded
	 * @param roles The roles
	 * @return The list of curriculum elements which can be managed
	 */
	public List<CurriculumElement> filterElementsWithoutManagerRole(List<CurriculumElement> elements, Roles roles);
	
	
	public List<CurriculumElementRepositoryEntryViews> getCurriculumElements(Identity identity, Roles roles, CurriculumRef curriculum);

}

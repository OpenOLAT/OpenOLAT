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

import java.io.File;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.GroupMembershipStatus;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.mail.MailPackage;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.group.BusinessGroup;
import org.olat.modules.curriculum.model.CurriculumCopySettings;
import org.olat.modules.curriculum.model.CurriculumElementInfos;
import org.olat.modules.curriculum.model.CurriculumElementInfosSearchParams;
import org.olat.modules.curriculum.model.CurriculumElementKeyToRepositoryEntryKey;
import org.olat.modules.curriculum.model.CurriculumElementMembershipChange;
import org.olat.modules.curriculum.model.CurriculumElementMembershipHistory;
import org.olat.modules.curriculum.model.CurriculumElementMembershipHistorySearchParameters;
import org.olat.modules.curriculum.model.CurriculumElementRepositoryEntryViews;
import org.olat.modules.curriculum.model.CurriculumElementSearchInfos;
import org.olat.modules.curriculum.model.CurriculumElementSearchParams;
import org.olat.modules.curriculum.model.CurriculumElementWebDAVInfos;
import org.olat.modules.curriculum.model.CurriculumElementWithParents;
import org.olat.modules.curriculum.model.CurriculumInfos;
import org.olat.modules.curriculum.model.CurriculumMember;
import org.olat.modules.curriculum.model.CurriculumMemberStats;
import org.olat.modules.curriculum.model.CurriculumSearchParameters;
import org.olat.modules.curriculum.model.RepositoryEntryInfos;
import org.olat.modules.curriculum.model.SearchMemberParameters;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ResourceReservation;

/**
 * 
 * Initial date: 9 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface CurriculumService {
	
	public static final String DEFAULT_CURRICULUM_ELEMENT_TYPE = "default-curriculum-element-type";
	
	public static final String RESERVATION_PREFIX = "curriculum_";
	
	/**
	 * Create and persist a curriculum.
	 * 
	 * @param identifier The identifier
	 * @param displayName The name
	 * @param description The description
	 * @param lecturesEnabled Enable the calculation of absence quota
	 * @param organisation The organisation
	 * @return A persisted curriculum
	 */
	public Curriculum createCurriculum(String identifier, String displayName, String description,
			boolean lecturesEnabled, Organisation organisation);
	
	public Curriculum getCurriculum(CurriculumRef ref);
	
	public Curriculum updateCurriculum(Curriculum curriculum);

	public void deleteSoftlyCurriculum(CurriculumRef curriculum, Identity doer, boolean sendNotifications);
	
	
	public List<Curriculum> getCurriculums(Collection<? extends CurriculumRef> refs);
	
	public List<Curriculum> getCurriculums(CurriculumSearchParameters params);

	public CurriculumInfos getCurriculumWithInfos(CurriculumRef curriculum);
	
	public List<CurriculumInfos> getCurriculumsWithInfos(CurriculumSearchParameters params);
	
	/**
	 * The list of curriculums the identity participates.
	 * 
	 * @param identity The identity
	 * @return A list of curriculums
	 */
	public List<Curriculum> getMyCurriculums(Identity identity);
	
	public boolean hasCurriculums(IdentityRef identity);
	
	/**
	 * The list of curriculums the identity participates.
	 * 
	 * @param identity The identity
	 * @return A list of curriculums
	 */
	public List<CurriculumRef> getMyActiveCurriculumRefs(Identity identity);
	
	/**
	 * Get the list of members of the specified curriculum with their roles.
	 * 
	 * @param curriculum The curriculum
	 * @return A list of memberships
	 */
	public List<CurriculumMember> getCurriculumMembers(SearchMemberParameters params);
	
	/**
	 * Get the list of members of the specified curriculum with the specified
	 * role.
	 * 
	 * @param curriculum The curriculum
	 * @param role The role (mandatory)
	 * @return A list of identities
	 */
	public List<Identity> getMembersIdentity(CurriculumRef curriculum, CurriculumRoles role);
	
	/**
	 * Get the list of members with the specified role in the list of curriculum
	 * elements.
	 * 
	 * @param curriculumElementKeys A list of curriculum element keys
	 * @param role The role (mandatory)
	 * @return A list of identities
	 */
	public List<Identity> getMembersIdentity(List<Long> curriculumElementKeys, CurriculumRoles role);
	
	/**
	 * 
	 * @return
	 */
	public boolean hasRoleExpanded(CurriculumRef curriculum, IdentityRef identity, String... role);
	
	/**
	 * @param identity The identity
	 * @return true if the identity is manager of at least one curriculum
	 */
	public boolean isCurriculumOwner(IdentityRef identity);
	
	/**
	 * @param identity The identity
	 * @return true if the identity is manager of at least one curriculum or
	 * 		is element owner in a curriculum.
	 */
	public boolean isCurriculumOrElementOwner(IdentityRef identity);
	
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
	public void removeMember(Curriculum curriculum, IdentityRef member, CurriculumRoles role);
	
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
	
	public CurriculumElementType getDefaultCurriculumElementType();
	
	/**
	 * Load the curriculum element type with the specified primary key.
	 * 
	 * @param ref The reference of the type
	 * @return A curriculum element type
	 */
	public CurriculumElementType getCurriculumElementType(CurriculumElementTypeRef typeRef);
	
	public CurriculumElementType getCurriculumElementType(CurriculumElementRef element);
	
	/**
	 * 
	 * @param parentElement The parent of the element (can be null if root)
	 * @param element The element (can be null for new one)
	 * @return A list of possible types
	 */
	public List<CurriculumElementType> getAllowedCurriculumElementType(CurriculumElement parentElement, CurriculumElement element);
	
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
	
	
	public CurriculumElement createCurriculumElement(String identifier, String displayName,
			CurriculumElementStatus status, Date beginDate, Date endDate,
			CurriculumElementRef parent, CurriculumElementType elementType, CurriculumCalendars calendars,
			CurriculumLectures lectures, CurriculumLearningProgress learningProgress, Curriculum curriculum);
	
	/**
	 * 
	 * @param curriculum The curriculum of the cloned element
	 * @param parentElement The parent element for the new clone
	 * @param elementToClone The element to clone
	 * @param settings The settings to clone elements
	 * @return The root element
	 */
	public CurriculumElement copyCurriculumElement(Curriculum curriculum, CurriculumElement parentElement,
			CurriculumElement elementToClone, CurriculumCopySettings settings, Identity identity);
	
	public RepositoryEntry instantiateTemplate(RepositoryEntry template, CurriculumElement curriculumElement,
			String displayName, String externalRef, Date beginDate, Date endDate, Identity doer);
	
	
	public CurriculumElement getCurriculumElement(CurriculumElementRef element);
	
	public CurriculumElement getCurriculumElement(OLATResource resource);

	public List<CurriculumElement> getCurriculumElements(Collection<? extends CurriculumElementRef> elementRefs);
	
	/**
	 * The element will be flagged as deleted.
	 * 
	 * @param element The curriculum element to delete
	 * @return true if the element status is set as deleted, false if already deleted
	 */
	public boolean deleteSoftlyCurriculumElement(CurriculumElementRef element, Identity doer, boolean sendNotifications);
	
	/**
	 * Return all the elements of a curriculum, but flat.
	 * 
	 * @param curriculum The curriculum
	 * @param status List of status (mandatory)
	 * @return A list of curriculum elements
	 */
	public List<CurriculumElement> getCurriculumElements(CurriculumRef curriculum, CurriculumElementStatus[] status);
	
	public List<CurriculumElement> getCurriculumElementsByCurriculums(Collection<? extends CurriculumRef> curriculumRefs);
	
	/**
	 * Return all the elements of a curriculum, flat, with additional informations
	 * like the number of resources linked to the elements. List element in state
	 * active and inactive.
	 * 
	 * @param curriculum The search parameters
	 * @return A list of curriculum elements with additional informations
	 */
	public List<CurriculumElementInfos> getCurriculumElementsWithInfos(CurriculumElementInfosSearchParams searchParams);

	
	/**
	 * Retrieve the children elements of the specified curriculum element. The method
	 * returns all the children, inclusive the elements marked as deleted.
	 * 
	 * @param parentElement The parent element
	 * @return A list of curriculum elements
	 */
	public List<CurriculumElement> getCurriculumElementsChildren(CurriculumElementRef parentElement);
	
	/**
	 * 
	 * @param parentElement
	 * @return
	 */
	public boolean hasCurriculumElementChildren(CurriculumElementRef parentElement);
	

	public List<CurriculumElement> getCurriculumElementsDescendants(CurriculumElement parentElement);
	
	/**
	 * Returns the number of curriculum elements linked to the entry;
	 *
	 * @param entry
	 * @return
	 */
	public Long getCuriculumElementCount(RepositoryEntryRef entry);
	
	/**
	 * Return all the curriculum elements linked to the specified repository entry.
	 * The method fetch the curriculums and organizations associated with elements.
	 * 
	 * @param entry A repository entry
	 * @return A list of curriculum elements 
	 */
	public List<CurriculumElement> getCurriculumElements(RepositoryEntryRef entry);
	
	public boolean hasCurriculumElements(RepositoryEntryRef entry);
	
	
	public List<CurriculumElementWithParents> getOrderedCurriculumElementsTree(RepositoryEntryRef entry);
	
	/**
	 * Return the curriculum elements linked to the specified repository entry and a
	 * the specified identity.
	 *
	 * @param entry
	 * @param identity
	 * @param roles Restrict to this roles. If roles is null or empty, no restriction is active.
	 * @return
	 */
	public List<CurriculumElement> getCurriculumElements(RepositoryEntryRef entry, Identity identity,
			Collection<CurriculumRoles> roles);
	
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
	 * 
	 * @param params
	 * @return A list of curriculum elements
	 */
	public List<CurriculumElementSearchInfos> searchCurriculumElements(CurriculumElementSearchParams params);

	/**
	 * Return the parent line of the specified curriculum element.
	 * 
	 * @param element A curriculum element
	 * @return A list of curriculum elements
	 */
	public List<CurriculumElement> getCurriculumElementParentLine(CurriculumElement element);
	
	public CurriculumElement updateCurriculumElement(CurriculumElement element);
	
	public CurriculumElement updateCurriculumElementStatus(Identity doer, CurriculumElementRef element,
			CurriculumElementStatus newStatus, boolean updateChildren, MailPackage mailing);
	
	/**
	 * 
	 * @param elementToMove The element to move
	 * @param newParent The new parent or null if root
	 * @param siblingBefore The sibling before the new position or null if at the first place
	 */
	public CurriculumElement moveCurriculumElement(CurriculumElement elementToMove, CurriculumElement newParent,
			CurriculumElement siblingBefore, Curriculum targetCurriculum);
	
	/**
	 * Move a root curriculum element from a curriculum to an other. This operation
	 * is committed asap.
	 * 
	 * @param rootElement The curriculum element (must be a root one)
	 * @param curriculum The target curriculum
	 * @return The update element
	 */
	public CurriculumElement moveCurriculumElement(CurriculumElement rootElement, Curriculum curriculum);
	
	/**
	 * @param curriculumElement An element
	 * @return The implementation / root curriculum element of the specified element
	 */
	public CurriculumElement getImplementationOf(CurriculumElement curriculumElement);
	
	public List<CurriculumElement> getImplementations(Curriculum curriculum, CurriculumElementStatus... status);
	
	public VFSContainer getMediaContainer(CurriculumElement curriculumElement);

	public void storeCurriculumElemenFile(CurriculumElementRef element, CurriculumElementFileType type, File file, String filename, Identity savedBy);
	
	public void deleteCurriculumElemenFile(CurriculumElementRef element, CurriculumElementFileType type);

	public VFSLeaf getCurriculumElemenFile(CurriculumElementRef element, CurriculumElementFileType type);
	
	/**
	 * The list of members of the specified curriculum element.
	 * 
	 * @param element The curriculum element
	 * @return The list of memberships
	 */
	public List<CurriculumMember> getCurriculumElementsMembers(SearchMemberParameters params);
	
	public List<CurriculumMemberStats> getMembersWithStats(SearchMemberParameters params);
	
	/**
	 * The list of members of the specified curriculum element with the specified role.
	 * 
	 * @param element The curriculum element
	 * @param role The role
	 * @return The list of memberships
	 */
	public List<Identity> getMembersIdentity(CurriculumElementRef element, CurriculumRoles role);

	public List<Long> getMemberKeys(List<CurriculumElementRef> elements, String... roles);
	
	public List<CurriculumElementMembership> getCurriculumElementMemberships(Curriculum curriculum, Identity identity);
	
	public List<CurriculumElementMembership> getCurriculumElementMemberships(List<CurriculumElement> elements, List<Identity> identities);
	
	public List<CurriculumElementMembership> getCurriculumElementMemberships(Collection<? extends CurriculumElementRef> elements, Identity... identities);
	
	public void acceptPendingParticipation(ResourceReservation reservation, Identity identity, Identity actor);
	
	public void cancelPendingParticipation(ResourceReservation reservation, Identity identity, Identity actor, String adminNote);
	
	public void updateCurriculumElementMemberships(Identity doer, Roles roles, List<CurriculumElementMembershipChange> changes, MailPackage mailing);
	
	public List<CurriculumElementMembershipHistory> getCurriculumElementMembershipsHistory(CurriculumElementMembershipHistorySearchParameters params);
	
	/**
	 * Add a member with the specified role to the curriculum element. The
	 * inheritance mode of the membership is per default "none".
	 * 
	 * @param element The curriculum element
	 * @param member The identity to make a member of
	 * @param role The role of the member
	 */
	public void addMember(CurriculumElement element, Identity member, CurriculumRoles role, Identity actor);
	
	public void addMember(CurriculumElement element, Identity member, CurriculumRoles role, Identity actor, String note);
	
	public void addMemberReservation(CurriculumElement element, Identity member, CurriculumRoles role, Date expirationDate,
			Boolean userConfirmation, Identity actor, String note);
	
	public Date getDefaultReservationExpiration();
	
	public Map<Long, Long> getCurriculumElementKeyToNumParticipants(List<CurriculumElement> curriculumElements,
			boolean countReservations);
	
	public boolean isMaxParticipantsReached(CurriculumElement element);
	
	/**
	 * Remove a member of the curriculum element and discard all its roles.
	 * 
	 * @param element The curriculum element
	 * @param member The identity to remove
	 */
	public void removeMember(CurriculumElement element, Identity member, Identity actor);
	
	/**
	 * Remove the membership of a user with the specified role. The remove operation
	 * happens to the element and the descendants elements.
	 * 
	 * @param element The curriculum element
	 * @param member The identity which loose the membership
	 * @param role The role
	 * @param reason The new status
	 * @param actor The doer
	 * @param adminNote An optional administrative note
	 * 
	 */
	public boolean removeMember(CurriculumElement element, Identity member, CurriculumRoles role,
			GroupMembershipStatus reason, Identity actor, String adminNote);
	
	public boolean removeMemberReservation(CurriculumElement element, Identity member, CurriculumRoles role,
			GroupMembershipStatus reason, Identity actor, String adminNote);
	
	/**
	 * Remove the members of the curriculum elements linked to the repository entry.<br>
	 * The method respect the managed flags!
	 * 
	 * @param entry The repository entry
	 * @param members The memberss
	 */
	public void removeMembers(CurriculumElement element, List<Identity> members, boolean overrideManaged, Identity actor);
	
	/**
	 * Check if the curriculum element has at least one entry.
	 * 
	 * @param element The curriculum element
	 * @return true if the element has at least an entry.
	 */
	public boolean hasRepositoryEntries(CurriculumElementRef element);

	/**
	 * The all list of repository entries hold by the specified curriculum element.
	 * 
	 * @param element The curriculum element
	 * @return A list of repository entries
	 */
	public List<RepositoryEntry> getRepositoryEntries(CurriculumElementRef element);
	
	public Map<Long, Set<RepositoryEntry>> getCurriculumElementKeyToRepositoryEntries(Collection<? extends CurriculumElementRef> elements);
	
	public List<RepositoryEntryInfos> getRepositoryEntriesWithInfos(CurriculumElementRef element);
	
	/**
	 * The all list of templates hold by the specified curriculum element.
	 * 
	 * @param element The curriculum element
	 * @return A list of templates
	 */
	public List<RepositoryEntry> getRepositoryTemplates(CurriculumElementRef element);
	
	/**
	 * The all list of repository entries hold by the specified curriculum element and
	 * its descendants elements.
	 * 
	 * @param element The curriculum element
	 * @return A list of repository entries
	 */
	public List<RepositoryEntry> getRepositoryEntriesWithDescendants(CurriculumElement element);
	
	/**
	 * The all list of repository entries hold by the specified curriculum element and
	 * its descendants elements in which the identity is participant
	 * 
	 * @param element The curriculum element
	 * @param identity Specify the identity to check the permissions of the repository entries
	 * @return A list of repository entries
	 */
	public List<RepositoryEntry> getRepositoryEntriesOfParticipantWithDescendants(CurriculumElement element, Identity participant);
	
	/**
	 * The all list of repository entries hold by the specified curriculum element and
	 * its descendants elements.
	 * 
	 * @param element The curriculum element
	 * @param identity Specify the identity to check the permissions of the repository entries
	 * @return A list of repository entries with lectures enabled
	 */
	public List<RepositoryEntry> getRepositoryEntriesWithLectures(CurriculumElement element, Identity identity, boolean withDescendants);
	
	/**
	 * The list of repository entries hold by the specified curriculum.
	 * 
	 * @param curriculum The curriculum
	 * @param identity Specify the identity to check the permissions of the repository entries
	 * @return A list of repository entries with lectures enabled
	 */
	public List<RepositoryEntry> getRepositoryEntriesWithLectures(Curriculum curriculum, Identity identity);
	
	/**
	 * Check if the repository entry is already in relation with the specified
	 * curriculum element.
	 * 
	 * @param element The curriculum element
	 * @param entry The repository entry
	 * @return True if the repository entry and curriculum element share a group
	 */
	public boolean hasRepositoryEntry(CurriculumElement element, RepositoryEntryRef entry);

	public boolean hasRepositoryTemplate(CurriculumElement element, RepositoryEntryRef entry);
	
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
	public AddRepositoryEntry addRepositoryEntry(CurriculumElement element, RepositoryEntry entry, boolean moveLectureBlocks);
	
	public CurriculumElement getDefaultCurriculumElement(RepositoryEntryRef entry);
	
	public boolean addRepositoryTemplate(CurriculumElement element, RepositoryEntry template);
	
	public record AddRepositoryEntry(boolean entryAdded, boolean lectureBlockMoved) {
		//
	}

	/**
	 * remove linked relation between curriculumElement and repositoryEntry
	 *
	 * @param element
	 * @param entry
	 */
	public RemovedRepositoryEntry removeRepositoryEntry(CurriculumElement element, RepositoryEntry entry);
	
	public record RemovedRepositoryEntry(boolean entryRemoved, int lectureBlockMoved) {
		//
	}
	
	public void removeRepositoryTemplate(CurriculumElement element, RepositoryEntry entry);
	
	/**
	 * Remove the repository entry from all the curriculum elements.
	 * 
	 * @param entry
	 */
	public void removeRepositoryEntry(RepositoryEntry entry);
	
	/**
	 * The list of taxonomy levels of the curriculum element.
	 * 
	 * @param element The curriculum element
	 * @return A list of taxonomy levels
	 */
	public List<TaxonomyLevel> getTaxonomy(CurriculumElement element);
	
	public Map<Long, List<TaxonomyLevel>> getCurriculumElementKeyToTaxonomyLevels(List<? extends CurriculumElementRef> curriculumElements);
	
	public List<CurriculumElement> getCurriculumElements(TaxonomyLevelRef level);
	
	public long countCurriculumElements(List<? extends TaxonomyLevelRef> taxonomyLevels);
	
	public void updateTaxonomyLevels(CurriculumElement element, Collection<TaxonomyLevel> addedLevels, Collection<TaxonomyLevel> removedLevels);
	
	/**
	 * Remove from the list the curriculum elements which are not manageable with the specified roles.
	 * @param elements A list of curriculum elements with the organization loaded
	 * @param roles The roles
	 * @return The list of curriculum elements which can be managed
	 */
	public List<CurriculumElement> filterElementsWithoutManagerRole(List<CurriculumElement> elements, Roles roles);
	
	/**
	 * Returns only the curriculum where the specified identity is a member.
	 * 
	 * @param identity The member of the curriculum
	 * @param roles The roles
	 * @param curriculum The curriculum
	 * @param runtimeTypes Runtime types for the repository entries.   
	 * @return A list of views
	 */
	public List<CurriculumElementRepositoryEntryViews> getCurriculumElements(Identity identity, Roles roles,
			List<? extends CurriculumRef> curriculum, CurriculumElementStatus[] status,
			RepositoryEntryRuntimeType[] runtimeTypes, boolean participantsOnly);
	
	public List<CurriculumElementRepositoryEntryViews> getCurriculumElements(Identity identity, Roles roles, List<? extends CurriculumRef> curriculum, CurriculumElementStatus[] status);

	public List<CurriculumElementKeyToRepositoryEntryKey> getRepositoryEntryKeyToCurriculumElementKeys(List<? extends CurriculumElementRef> curriculumElements);
	
	public List<CurriculumElementWebDAVInfos> getCurriculumElementInfosForWebDAV(IdentityRef identity);

	/**
	 * Numbering of the implementation sub-tree.
	 * 
	 * @param rootElement Need to be an implementation / root curriculum element.
	 */
	public boolean numberRootCurriculumElement(CurriculumElement rootElement);

	/**
	 * Removes course-internal business groups relationships for 'courseEntry' of runtime type 'curricular'.

	 * @param courseEntry The repository entry of the course
	 * @return The list of course-internal business groups that are concerned (use this for deletion in the method below).
	 */
	public List<BusinessGroup> deleteInternalGroupMembershipsAndInvitations(RepositoryEntry courseEntry);

	/**
	 * Permanently removes the internal business groups 'internalGroups'.
	 *
	 * @param internalGroups The business groups to delete (including all dependencies).
	 * @param doer The person performing the delete action.
	 */
	public void deleteInternalGroups(List<BusinessGroup> internalGroups, Identity doer);
}

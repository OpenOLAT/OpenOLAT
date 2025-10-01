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
package org.olat.repository;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.repository.manager.RepositoryEntryAuditLogSearchParams;
import org.olat.repository.model.MembershipInfos;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams;
import org.olat.resource.OLATResource;



/**
 * To replace the repository manager
 *
 *
 * Initial date: 20.02.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface RepositoryService {

	public static final OLATResourceable REPOSITORY_EVENT_ORES = OresHelper.createOLATResourceableInstance("REPO-CHANGE", 1l);


	public RepositoryEntry create(Identity initialAuthor, String initialAuthorAlt,
			String resourceName, String displayname, String description,
			OLATResource resource, RepositoryEntryStatusEnum status,
			RepositoryEntryRuntimeType runtimeType, Organisation organisation);

	public RepositoryEntry copy(RepositoryEntry sourceEntry, Identity author, String displayname, String externalRef);
	
	public boolean canCopy(RepositoryEntry entryToCopy, Identity identity);

	public RepositoryEntry loadByKey(Long key);
	
	public RepositoryEntry loadBy(RepositoryEntryRef ref);
	
	public List<RepositoryEntry> loadByKeys(Collection<Long> keys);

	public List<RepositoryEntry> loadRepositoryForMetadata(RepositoryEntryStatusEnum status);

	public RepositoryEntry loadByResourceKey(Long key);

	public RepositoryEntry loadByResourceId(String resourceTypeName, Long resourceId);

	public List<RepositoryEntry> loadRepositoryEntriesByExternalId(String externalId);

	public List<RepositoryEntry> loadRepositoryEntriesByExternalRef(String externalRef);
	
	public List<RepositoryEntry> loadRepositoryEntriesLikeExternalRef(String externalRef);

	public List<RepositoryEntry> loadByResourceKeys(Collection<Long> keys);
	
	/**
	 * @param repositoryEntryKey The key of the repository entry
	 * @return The olat resource of the repository entry
	 */
	public OLATResource loadRepositoryEntryResource(Long repositoryEntryKey);
	

	/**
	 * @param softkey The soft key of the repository entry
	 * @return The olat resource of the repository entry
	 */
	public OLATResource loadRepositoryEntryResourceBySoftKey(String softkey);
	
	public List<RepositoryEntry> loadRepositoryEntries(int firstResult, int maxResult);

	public VFSLeaf getIntroductionImage(RepositoryEntryRef re);

	public VFSLeaf getIntroductionMovie(RepositoryEntry re);


	public RepositoryEntry update(RepositoryEntry re);

	/**
	 * Set the access to 0. The resource is not deleted on the database
	 * but the resource is removed from the catalog.
	 *
	 *
	 * @param entry
	 * @param owners If the owners need to be removed
	 */
	public RepositoryEntry deleteSoftly(RepositoryEntry entry, Identity deletedBy, boolean owners, boolean sendNotifications);

	/**
	 * The access is set to B.
	 * @param entry
	 * @param restoredBy
	 * @return
	 */
	public RepositoryEntry restoreRepositoryEntry(RepositoryEntry entry, Identity restoredBy);


	/**
	 * Delete the learning resource with all its attached resources.
	 * @param entry
	 * @param identity
	 * @param roles
	 * @param locale
	 * @return
	 */
	public ErrorList deletePermanently(RepositoryEntryRef entryRef, Identity identity, Roles roles, Locale locale);

	/**
	 * Delete only the database object
	 * @param entry
	 * @param doer
	 */
	public void deleteRepositoryEntryAndBaseGroups(RepositoryEntry entry, Identity doer);

	/**
	 * This will change the status of the repository entry to "closed" (statusCode=2).
	 *
	 * @param entry
	 * @param identity
	 * @param roles
	 * @param locale
	 * @return The closed repository entry
	 */
	public RepositoryEntry closeRepositoryEntry(RepositoryEntry entry, Identity closedBy, boolean sendNotifications);


	/**
	 * This will unclose a repositoryEntry (status change from closed to published)
	 *
	 * @param entry
	 * @param unclosedBy
	 * @return
	 */
	public RepositoryEntry uncloseRepositoryEntry(RepositoryEntry entry, Identity unclosedBy);


	/**
	 * Increment the launch counter and the last usage date.
	 *
	 * @param re The repository entry
	 */
	public void incrementLaunchCounter(RepositoryEntry re);

	/**
	 * Increment the download counter and the last usage date.
	 *
	 * @param re The repository entry
	 */
	public void incrementDownloadCounter(RepositoryEntry re);

	/**
	 * Update the last usage of the specified repository entry
	 * with a granularity of 1 minute.
	 *
	 * @param re The repository entry
	 */
	public void setLastUsageNowFor(RepositoryEntry re);

	public Group getDefaultGroup(RepositoryEntryRef ref);

	/**
	 *
	 * @param identity
	 * @param entry
	 * @return True if the identity is member of the repository entry and its attached business groups
	 */
	public boolean isMember(IdentityRef identity, RepositoryEntryRef entry);

	public void filterMembership(IdentityRef identity, Collection<Long> entries);

	/**
	 * Count the number of member with the specified role.
	 * 
	 * @param re The repository entry
	 * @param role The role (mandatory)
	 * @return
	 */
	public int countMembers(RepositoryEntryRef re, String role);
	
	/**
	 * 
	 * @param re The repository entry
	 * @param relationType The relation type (all, business groups...)
	 * @param role The role
	 * @return The number of members (the same user in a group and in repository entry count for one)
	 */
	public int countMembers(RepositoryEntryRef re, RepositoryEntryRelationType relationType, String role);

	/**
	 * Count all members (following up to business groups waiting list) with the following
	 * roles: owner, coach, participant, waiting 
	 * 
	 * @param res
	 * @param excludeMe Exclude to user which call the method (optional)
	 * @return
	 */
	public int countMembers(List<? extends RepositoryEntryRef> res, Identity excludeMe);
	
	public Map<Long, Long> getRepoKeyToCountMembers(List<? extends RepositoryEntryRef> res, String... roles);
	
	public Map<String, Long> getRoleToCountMemebers(RepositoryEntryRef re);

	public Map<String, Long> getRoleToCountRootMembers(RepositoryEntryRef re);

		/**
		 * Return the smallest enrollment date for each identity.
		 *
		 * @param re
		 * @param identities
		 * @param roles
		 * @return
		 */
	public Map<Long,Date> getEnrollmentDates(RepositoryEntryRef re, Collection<? extends IdentityRef> identities, String... roles);

	/**
	 * @param re The repository entry
	 * @return True if the configuration allowed user to leave the entry right now
	 */
	public boolean isParticipantAllowedToLeave(RepositoryEntry re);
	
	/**
	 * Check if the resource has some business groups, curriculum elements or
	 * some participants or coaches on the repository entry itself.
	 * 
	 * @param re The repository entry
	 * @return true if user management is used
	 */
	public boolean hasUserManaged(RepositoryEntryRef re);

	/**
	 * Get the members of the repository entry (the method doesn't
	 * follow automatically the business groups or other relations).
	 *
	 * @param re The repository entry
	 * @param relationType Type of relations to follow
	 * @param roles The roles to search for
	 * @return A list of identities which have at least one of the specified role
	 */
	public List<Identity> getMembers(RepositoryEntryRef re, RepositoryEntryRelationType relationType, String... roles);

	/**
	 * Get the members of a list of repository entries.
	 * 
	 * @param re The repository entry
	 * @param relationType Type of relations to follow
	 * @param roles The roles to search for
	 * @return A list of identities which have at least one of the specified role
	 */
	public List<Identity> getMembers(List<? extends RepositoryEntryRef> re, RepositoryEntryRelationType relationType, String... roles);
	
	public List<Long> getMemberKeys(RepositoryEntryRef re, RepositoryEntryRelationType relationType, String... roles);
	
	public List<MembershipInfos> getMemberships(List<RepositoryEntryRef> entries, String role);
	
	/**
	 * The participants the specified user can coach. The method is specific to
	 * the coach role, but follow the business groups and curriculums.
	 * 
	 * @param coach The coach
	 * @param re The repository entry
	 * @return A list of identities
	 */
	public List<Identity> getCoachedParticipants(IdentityRef coach, RepositoryEntryRef re);
	
	/**
	 * The coaches the specified participant. The method is specific to
	 * the participant role, but follow the business groups and curriculums.
	 * 
	 * @param participant
	 * @param re
	 * @return A list of identities
	 */
	public List<Identity> getAssignedCoaches(IdentityRef participant, RepositoryEntryRef re);

	/**
	 * Get the role in the specified resource, business group and curriculums are
	 * included in the query but not organizations.
	 *
	 * @return The list of roles (not deduplicated)
	 */
	public List<String> getRoles(Identity identity, RepositoryEntryRef re);

	/**
	 * Has specific role in the specified resource (doesn't follow the business groups).
	 *
	 * @return True if the specified role(s) was found.
	 */
	public boolean hasRole(Identity identity, RepositoryEntryRef re, String... roles);
	
	/**
	 * Has specific role in the specified resource (via the resource itself or a
	 * business group, organization or a curriculum element).
	 *
	 * @return True if the specified role(s) was found.
	 */
	public boolean hasRoleExpanded(Identity identity, RepositoryEntryRef re, String... roles);

	/**
	 * Has specific role in any resource (follow or not the business groups, organizations an).
	 *
	 * @return True if the specified role(s) was found.
	 */
	public boolean hasRoleExpanded(Identity identity, String... roles);


	public void addRole(Identity identity, RepositoryEntry re, String role);

	public void removeRole(Identity identity, RepositoryEntry re, String role);
	
	public boolean isTemplateInUse(RepositoryEntryRef template);

	public List<RepositoryEntry> searchByIdAndRefs(String id);

	public int countMyView(SearchMyRepositoryEntryViewParams params);

	/**
	 * The identity is mandatory for the search.
	 * @param params
	 * @param firstResult
	 * @param maxResults
	 * @return
	 */
	public List<RepositoryEntryMyView> searchMyView(SearchMyRepositoryEntryViewParams params, int firstResult, int maxResults);

	public int countAuthorView(SearchAuthorRepositoryEntryViewParams params);

	public RepositoryEntryAuthorViewResults searchAuthorView(SearchAuthorRepositoryEntryViewParams params, int firstResult, int maxResults);
	
	
	/**
	 * Hold the organizations via the group relation.
	 * 
	 * @param entry The repository entry
	 * @return A list of organization
	 */
	public List<Organisation> getOrganisations(RepositoryEntryRef entry);
	
	public List<Organisation> getOrganisations(Collection<? extends RepositoryEntryRef> entries);
	
	/**
	 * Add a link between organization and the specified repository entry.
	 * 
	 * @param entry The repository entry
	 * @param organisation The organization
	 */
	public void addOrganisation(RepositoryEntry entry, Organisation organisation);
	
	/**
	 * Remove the link between organization and repository entry
	 * @param entry The repository entry
	 * @param organisation The organization
	 */
	public void removeOrganisation(RepositoryEntry entry, Organisation organisation);
	
	/**
	 * 
	 * @param entry
	 * @return
	 */
	public List<OrganisationRef> getOrganisationReferences(RepositoryEntryRef entry);
	
	public Map<RepositoryEntryRef, List<Organisation>> getRepositoryEntryOrganisations(Collection<? extends RepositoryEntryRef> entries);
	
	/**
	 * Retrieve where the repository entry is linked in taxonomy.
	 * 
	 * @param entry A repository entry
	 * @return A list of taxonomy level
	 */
	public List<TaxonomyLevel> getTaxonomy(RepositoryEntryRef entry);
	
	/**
	 * Retrieve where the repository entries are linked to taxonomy. The method
	 * doesn't fetch any thing of the levels but returns the same instances of
	 * repository entries.
	 * 
	 * @param entries A list of repository entries
	 * @param fetchParents 
	 * @return A map entries to taxonomy level
	 */
	public Map<RepositoryEntryRef,List<TaxonomyLevel>> getTaxonomy(List<? extends RepositoryEntryRef> entries, boolean fetchParents);
	
	public Map<RepositoryEntryRef,AtomicLong> getNumOfTaxonomyLevels(List<? extends RepositoryEntryRef> entries);
	
	
	/**
	 * Add a link between a taxonomy level and the specified repository entry.
	 * 
	 * @param entry The repository entry
	 * @param level
	 */
	public void addTaxonomyLevel(RepositoryEntry entry, TaxonomyLevel level);
	
	/**
	 * Remove the link between the taxonomy level and repository entry
	 * @param entry The repository entry
	 * @param level
	 */
	public void removeTaxonomyLevel(RepositoryEntry entry, TaxonomyLevel level);
	
	/**
	 * Retrieve the list of repository entries link to a specific level
	 * of the taxonomy.
	 * 
	 * @param taxonomyLevel The taxonomy level
	 * @return A list of repository entries
	 */
	public List<RepositoryEntry> getRepositoryEntryByTaxonomy(TaxonomyLevelRef taxonomyLevel);
	
	/**
	 * Search the repository entries of the specified organization
	 * @param organisation An organization
	 * @return A list of repository entries
	 */
	public List<RepositoryEntry> getRepositoryEntryByOrganisation(OrganisationRef organisation);

	/**
	 * @return publisherData which is necessary for subscribing
	 */
	PublisherData getPublisherData();

	/**
	 *
	 * @return
	 */
	SubscriptionContext getSubscriptionContext();

	/**
	 * The repository entry need to be connected to the hibernate session
	 * to be properly serialized (without lazy loading exceptions).
	 * 
	 * @param repositoryEntry The repository entry to serialize
	 * @return The serialized XML 
	 */
	String toAuditXml(RepositoryEntry repositoryEntry);

	/**
	 * retrieve a repositoryEntry object by passing serialized data
	 *
	 * @param xml
	 * @return
	 */
	RepositoryEntry toAuditRepositoryEntry(String xml);

	/**
	 * log repositoryEntry changes
	 *
	 * @param action
	 * @param before
	 * @param after
	 * @param entry
	 * @param author
	 */
	void auditLog(RepositoryEntryAuditLog.Action action, String before, String after,
				  RepositoryEntry entry, Identity author);

	/**
	 * retrieve auditLogs for repositoryEntries
	 *
	 * @param searchParams
	 * @return auditLogs List
	 */
	List<RepositoryEntryAuditLog> getAuditLogs(RepositoryEntryAuditLogSearchParams searchParams);

	/**
	 * Performs a check on 'entry' without modifying it.
	 * Returns true if the runtimeType of the 'entry' can be switched to the desired 'runtimeType'.
	 *
	 * @param entry       The repositoryEntry to perform the check for.
	 * @param runtimeType The desired runtimeType.
	 * @return True if the runtimeType of the 'entry' could be changed to the desired 'runtimeType'.
	 */
	RuntimeTypeCheckDetails canSwitchTo(RepositoryEntry entry, RepositoryEntryRuntimeType runtimeType);

	/**
	 * Returns the set of runtime types that the 'entry' is allowed to switch to.
	 *
	 * @param entry The repositoryEntry to perform the check for.
	 * @return A set of runtime types that the 'entry' can be set to, plus result details of the check.
	 */
	RuntimeTypesAndCheckDetails allowedRuntimeTypes(RepositoryEntry entry);

	record RuntimeTypesAndCheckDetails(Set<RepositoryEntryRuntimeType> runtimeTypes, RuntimeTypeCheckDetails checkDetails) {}
	
	enum RuntimeTypeCheckDetails {
		ltiDeploymentExists,
		participantExists,
		coachExists,
		offerExists,
		ok,
		wrongState,
		curriculumElementExists,
		groupWithOffersOrLtiExists,
		groupWithOtherCoursesExists,
		isTemplate,
		lectureEnabled
	}
	
	/**
	 * Returns the set of runtime types that are possible for the provided 'entries'.
	 *
	 * The type 'curricular' will only be returned if the CurriculumModule is enabled.
	 *
	 * Returns all possible runtime types for all 'entries'. It is possible, that not all items in 'entries'
	 * can be set to all returned types. The returned set is the union of all possible values for the individual
	 * items in 'entries'.
	 *
	 * @param entries Repository entries to get the set of runtime types for.
	 * @return A set of runtimeType objects that are possible for the 'entries'.
	 */
	Set<RepositoryEntryRuntimeType> getPossibleRuntimeTypes(Collection<RepositoryEntry> entries);

	/**
	 * Returns the set of runtime types that are possible for the provided 'entry'.
	 *
	 * The type 'curricular' will only be returned if the CurriculumModule is enabled.
	 *
	 * @param entry Repository entries to get the set of runtime types for.
	 * @return A set o runtimeType objects that are possible for the 'entry'.
	 */
	Set<RepositoryEntryRuntimeType> getPossibleRuntimeTypes(RepositoryEntry entry);

	/**
	 * Returns the default runtime type for a 'resource'. If no knows default value can be determined, this
	 * method returns null.
	 *
	 * @param resource An Olat resource.
	 * @return The default value for the runtime type. Depends on the 'resource' and on the system configuration.
	 */
	RepositoryEntryRuntimeType getDefaultRuntimeType(OLATResource resource);

	/**
	 * Returns true if the course specified by 'entry' has a mixed setup: standalone runtime type and 
	 * referenced by a curriculum. This is a legacy constellation.

	 * @param entry Repository entry of a course.
	 * @return True if a mixed setup is detected.
	 */
	boolean isMixedSetup(RepositoryEntry entry);
}

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

import org.olat.basesecurity.Group;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Organisation;
import org.olat.core.id.Roles;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
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
			String resourceName, String displayname, String description, OLATResource resource, int access);

	public RepositoryEntry create(String initialAuthor, String resourceName,
			String displayname, String description, OLATResource resource);

	public RepositoryEntry copy(RepositoryEntry sourceEntry, Identity author, String displayname);

	public RepositoryEntry loadByKey(Long key);

	public RepositoryEntry loadByResourceKey(Long key);

	public List<RepositoryEntry> loadRepositoryEntriesByExternalId(String externalId);

	public List<RepositoryEntry> loadRepositoryEntriesByExternalRef(String externalRef);

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

	public VFSLeaf getIntroductionImage(RepositoryEntry re);

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
	public RepositoryEntry deleteSoftly(RepositoryEntry entry, Identity deletedBy, boolean owners);

	/**
	 * The access is set to B.
	 * @param entry
	 * @return
	 */
	public RepositoryEntry restoreRepositoryEntry(RepositoryEntry entry);


	/**
	 * Delete the learning resource with all its attached resources.
	 * @param entry
	 * @param identity
	 * @param roles
	 * @param locale
	 * @return
	 */
	public ErrorList deletePermanently(RepositoryEntry entry, Identity identity, Roles roles, Locale locale);

	/**
	 * Delete only the database object
	 * @param entry
	 */
	public void deleteRepositoryEntryAndBaseGroups(RepositoryEntry entry);

	/**
	 * This will change the status of the repository entry to "closed" (statusCode=2).
	 *
	 * @param entry
	 * @param identity
	 * @param roles
	 * @param locale
	 * @return The closed repository entry
	 */
	public RepositoryEntry closeRepositoryEntry(RepositoryEntry entry);


	public RepositoryEntry uncloseRepositoryEntry(RepositoryEntry entry);

	/**
	 * The unpublish will remove the users (coaches and participants) but will let
	 * the owners. Catalog entries will be removed and the relations to the business groups
	 * will be deleted.
	 *
	 * @param entry
	 * @return
	 */
	public RepositoryEntry unpublishRepositoryEntry(RepositoryEntry entry);

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

	public void filterMembership(IdentityRef identity, List<Long> entries);

	public int countMembers(RepositoryEntryRef re, String... roles);

	/**
	 * Count all members (following up to business groups wainting list)
	 * @param res
	 * @param excludeMe Exclude to user which call the method (optional)
	 * @return
	 */
	public int countMembers(List<? extends RepositoryEntryRef> res, Identity excludeMe);

	/**
	 * Return the smallest enrollment date.
	 *
	 * @param re
	 * @param identity
	 * @return
	 */
	public Date getEnrollmentDate(RepositoryEntryRef re, IdentityRef identity, String... roles);

	/**
	 * Return the smallest enrollment date.
	 *
	 * @param re
	 * @param identity
	 * @return
	 */
	public Map<Long,Date> getEnrollmentDates(RepositoryEntryRef re, String... roles);

	/**
	 * @param re The repository entry
	 * @return True if the configuration allowed user to leave the entry right now
	 */
	public boolean isParticipantAllowedToLeave(RepositoryEntry re);

	/**
	 * Return the primary keys of the authors
	 */
	public List<Long> getAuthors(RepositoryEntryRef re);

	/**
	 * Get the members of the repository entry (the method doesn't
	 * follow the business groups).
	 *
	 * @param re
	 * @param roles
	 * @return
	 */
	public List<Identity> getMembers(RepositoryEntryRef re, String... roles);

	/**
	 * Get the
	 * @param re
	 * @param followBusinessGroups
	 * @param roles
	 * @return
	 */
	public List<Identity> getMembers(List<? extends RepositoryEntryRef> re, RepositoryEntryRelationType relationType, String... roles);

	/**
	 * Return all the identities the specified role linked to a repository
	 * entry.
	 *
	 *
	 * @param rolle
	 * @return
	 */
	public List<Identity> getIdentitiesWithRole(String role);

	/**
	 * Get the role in the specified resource, business group are included in
	 * the query.
	 *
	 * @return The list of roles
	 */
	public List<String> getRoles(Identity identity, RepositoryEntryRef re);

	/**
	 * Has specific role in the specified resource (doesn't follow the business groups).
	 *
	 * @return True if the specified role(s) was found.
	 */
	public boolean hasRole(Identity identity, RepositoryEntryRef re, String... roles);

	/**
	 * Has specific role in any resource (follow or not the business groups).
	 *
	 * @return True if the specified role(s) was found.
	 */
	public boolean hasRole(Identity identity, boolean followBusinessGroups, String... roles);


	public void addRole(Identity identity, RepositoryEntry re, String role);

	public void removeRole(Identity identity, RepositoryEntry re, String role);

	public void removeMembers(RepositoryEntry re, String... roles);

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

	public List<RepositoryEntryAuthorView> searchAuthorView(SearchAuthorRepositoryEntryViewParams params, int firstResult, int maxResults);
	
	
	/**
	 * Add an organization to the repository entry.
	 * 
	 * @param entry The repository entry
	 * @param organisation The organisation
	 * @param master If the relation is "master"
	 * @return A merged repository entry
	 */
	public RepositoryEntry addOrganisation(RepositoryEntry entry, Organisation organisation, boolean master);
	
	
	
	
}

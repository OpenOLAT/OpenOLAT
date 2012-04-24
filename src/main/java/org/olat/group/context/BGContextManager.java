/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.group.context;

import java.util.Collection;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.group.BusinessGroup;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;

/**
 * Description:<BR/> Manager to manipulate business group contexts. A business
 * group context is a collection of business groups of the same business group
 * type and of business group areas that area associated with the business
 * groups. A business group context can be associated with multiple courses.
 * Every course has at least two business group contexts, the default learning
 * group context and the default right group context
 * <p>
 * Initial Date: Aug 25, 2004
 * 
 * @author gnaegi
 */
public interface BGContextManager {

	/**
	 * Creates a busines group context object and persists the object in the
	 * database
	 * 
	 * @param name Display name of the group context
	 * @param description
	 * @param groupType Business group type that this business group context can
	 *          contain
	 * @param owner The initial owner, the users who can manage the business group
	 *          context using the group context management tool
	 * @param defaultContext true: create as a default context, false: create as a
	 *          regular context
	 * @return The persisted business group context
	 */
	public abstract BGContext createAndPersistBGContext(String name, String description, String groupType, Identity owner,
			boolean defaultContext);

	/**
	 * Creates and persists a business group context as a copy of an existing
	 * business group context. The new created context will then be associated to
	 * the given OLATResource. The copy process will copy all business group areas
	 * and all business groups. The groups will be configured identically as the
	 * original groups but will not contain any users.
	 * 
	 * @param contextName The new context name
	 * @param resource The OALTResource that the new context should be associated
	 *          with using the group context management tool
	 * @param originalBgContext The original business group context that is uses
	 *          for the copy process
	 * @return The new copied business group context
	 */
	public abstract BGContext copyAndAddBGContextToResource(String contextName, OLATResource resource, BGContext originalBgContext);

	/**
	 * Updates a business group context in the database
	 * 
	 * @param bgContext
	 */
	public abstract void updateBGContext(BGContext bgContext);

	/**
	 * Deletes a business group context from the database
	 * 
	 * @param bgContext
	 */
	public abstract void deleteBGContext(BGContext bgContext);

	/**
	 * Find all groups from a business group context
	 * 
	 * @param bgContext the business group context or null to find groups that are
	 *          not within a business group context (e.b. buddygroups)
	 * @return A list containing business group contexts
	 */
	public abstract List<BusinessGroup> getGroupsOfBGContext(BGContext bgContext);
	
	/**
	 * Find all groups from a list of group contexts
	 * 
	 * @param bgContext the business group context or null to find groups that are
	 *          not within a business group context (e.b. buddygroups)
	 * @return A list containing business group contexts
	 */
	public abstract List<BusinessGroup> getGroupsOfBGContext(Collection<BGContext> bgContexts, int firstResult, int maxResults);

	/**
	 * Count the number of groups within a business group context
	 * 
	 * @param bgContext
	 * @return The number of groups
	 */
	public abstract int countGroupsOfBGContext(BGContext bgContext);

	/**
	 * Count the number of groups of a certain group type
	 * @param groupType
	 * @return
	 */
	public abstract int countGroupsOfType(String groupType);

	/**
	 * Find the identities that are owners of any group in the given business
	 * group context
	 * 
	 * @param bgContext
	 * @return A list of identities
	 */
	public abstract List getBGOwnersOfBGContext(BGContext bgContext);
	
	//fxdiff VCRP-2: access control
	public List<BusinessGroup> getBusinessGroupAsOwnerOfBGContext(Identity owner, BGContext bgContext);


	/**
	 * Count the number of identities that are owner of any group in the given
	 * business group context
	 * 
	 * @param bgContext
	 * @return The number of identities
	 */
	public abstract int countBGOwnersOfBGContext(BGContext bgContext);

	/**
	 * Find the identities that are participants of any group in the given
	 * business group context
	 * 
	 * @param bgContext
	 * @return A list of identities
	 */
	public abstract List getBGParticipantsOfBGContext(BGContext bgContext);
	
	//fxdiff VCRP-2: access control
	public List<BusinessGroup> getBusinessGroupAsParticipantOfBGContext(Identity participant, BGContext bgContext);

	/**
	 * Count the number of identities that are participants of any group in the
	 * given business group context
	 * 
	 * @param bgContext
	 * @return The number of identities
	 */
	public abstract int countBGParticipantsOfBGContext(BGContext bgContext);

	/**
	 * Check if the given identity is in this business group context
	 * 
	 * @param identity
	 * @param bgContext
	 * @param asOwner Flag to check if the user is in any group as owner
	 * @param asParticipant Flag to check if the user is in any group as
	 *          participant
	 * @return true if user is in any group with ghe given role, false otherwhise
	 */
	public abstract boolean isIdentityInBGContext(Identity identity, List<BGContext> bgContexts, boolean asOwner, boolean asParticipant);

	/**
	 * Find a business group in the given business group context
	 * 
	 * @param groupName
	 * @param bgContext
	 * @return The business group or null if no group found
	 */
	public abstract BusinessGroup findGroupOfBGContext(String groupName, BGContext bgContext);

	/**
	 * Find a business group in the given business group context where the given
	 * user is in the group as participant
	 * 
	 * @param identity
	 * @param groupName
	 * @param context
	 * @return The business group or null if no group found
	 */
	public abstract BusinessGroup findGroupAttendedBy(Identity identity, String groupName, BGContext context);

	// context to resource relation

	/**
	 * Creates a relation from a business group context to an OLATResource (e.g.
	 * course)
	 * 
	 * @param contextName The new context name
	 * @param resource The OALTResource that the new context should be associated
	 *          with
	 * @param initialOwner The initial owner. the users who can manage the
	 *          business group context using the group context management tool
	 * @param groupType The group type the context should be used for
	 * @param defaultContext true: create as a default context, false: create as a
	 *          regular context
	 * @return The new created business group context
	 */
	public abstract BGContext createAndAddBGContextToResource(String contextName, OLATResource resource, String groupType,
			Identity initialOwner, boolean defaultContext);

	/**
	 * Add a business group context to an OLATResource
	 * 
	 * @param bgContext
	 * @param resource
	 */
	public abstract void addBGContextToResource(BGContext bgContext, OLATResource resource);

	/**
	 * Find all business group contexts for the given OLATResource defaultContexts
	 * and nonDefaultContexts can both be true or partly be true, but not be both
	 * false
	 * 
	 * @param resource
	 * @param defaultContexts true: find default contexts
	 * @param nonDefaultContexts true: find non-default contexts
	 * @return A list of business group contexts
	 */
	public abstract List<BGContext> findBGContextsForResource(OLATResource resource, boolean defaultContexts, boolean nonDefaultContexts);

	/**
	 * Find all business group contexts for the given OLATResource with the given
	 * group type defaultContexts and nonDefaultContexts can both be true or
	 * partly be true, but not be both false
	 * 
	 * @param resource
	 * @param groupType
	 * @param defaultContexts true: find default contexts
	 * @param nonDefaultContexts true: find non-default contexts
	 * @return A list of business group contexts
	 */
	public abstract List<BGContext> findBGContextsForResource(OLATResource resource, String groupType, boolean defaultContexts,
			boolean nonDefaultContexts);

	/**
	 * Find all business group contexts for a specific user. This will find all
	 * contexts where the user is in the owner group and all context where the
	 * user is in the owner group of the olat resource that uses this context.
	 * defaultContexts and nonDefaultContexts can both be true or partly be true,
	 * but not be both false
	 * 
	 * @param identity
	 * @param defaultContexts true: find default contexts
	 * @param nonDefaultContexts true: find non-default contexts
	 * @return A list of business group contexts
	 */
	public abstract List<BGContext> findBGContextsForIdentity(Identity identity, boolean defaultContexts, boolean nonDefaultContexts);

	/**
	 * Find all OLATResources that are associated with the given business group
	 * context
	 * 
	 * @param bgContext
	 * @return A list of OLATResources
	 */
	public abstract List findOLATResourcesForBGContext(BGContext bgContext);

	/**
	 * Find all repository entries of the OLAT resources that have a relation to
	 * this group context. (see findOlatResourcesForBGContext)
	 * 
	 * @param bgContext
	 * @return List of repository entries
	 */
	public List<RepositoryEntry> findRepositoryEntriesForBGContext(BGContext bgContext);
	
	/**
	 * Find all repository entries of the OLAT resources that have a relation to
	 * this group context. (see findOlatResourcesForBGContext)
	 * 
	 * @param bgContext
	 * @param firstResult mandatory
	 * @param maxResults set to -1 if you want all entries
	 * @return
	 */
	//fxdiff VCRP-1,2: access control of resources
	public List<RepositoryEntry> findRepositoryEntriesForBGContext(Collection<BGContext> bgContext, int firstResult, int maxResults);
	
	/**
	 * Find all repository entries of the OLAT resources that have a relation to
	 * this group context. (see findOlatResourcesForBGContext)
	 * 
	 * @param bgContexts
	 * @param access
	 * @param asOwner
	 * @param asCoach
	 * @param asParticipant
	 * @param identity
	 * @return
	 */
	//fxdiff VCRP-1,2: access control of resources
	public List<RepositoryEntry> findRepositoryEntriesForBGContext(Collection<BGContext> bgContexts, int access, boolean asOwner, boolean asCoach,
			boolean asParticipant,  Identity identity);

	/**
	 * Remove the given business group context from this OLATResource
	 * 
	 * @param bgContext
	 * @param resource
	 */
	public abstract void removeBGContextFromResource(BGContext bgContext, OLATResource resource);

	/**
	 * Refresh the given bgContext
	 * 
	 * @param bgContext
	 * @return BGContext the updated context
	 */
	public BGContext loadBGContext(BGContext bgContext);

}
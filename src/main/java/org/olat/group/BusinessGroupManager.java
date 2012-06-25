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

package org.olat.group;


/**
 * Description: <br>
 * <b>Workflows within BusinessGroupManager: </b>
 * <ul>
 * <i><b>create a Buddy Group: </b> <br>
 * a new buddy group instance is created, made persistent. Owner is the
 * <code>Identity</code>, which triggered the creation. The Buddy Group must
 * be initialised with a title and Description, null is not allowed. </i> <i>
 * <b>find Buddy Groups: </b> <br>
 * find all the Buddy Groups associated with the given Identity. </i> <i>
 * <b>update a Buddy Group: </b> <br>
 * TODO: </i> <i><b>delete a Buddy Group: </b> <br>
 * TODO: </i>
 * </ul>
 * Initial Date: Jul 27, 2004
 * 
 * @author patrick
 */

public interface BusinessGroupManager {

	/*
	 * NOTES: find(...) -> may return empty list find used if subsequent steps
	 * follow, i.e. choosing get(...) -> one XXXobject, or null fetches object,
	 * which is directly used further.
	 */
	
	/**
	 * create a persistent BusinessGroup of the specified type and the provided
	 * parameters. The BusinessGroup can have a waiting-list.
	 * 
	 * @param type see BusinessGroup four TYPE constants
	 * @param identity
	 * @param name
	 * @param description
	 * @param minParticipants Minimum num of participants, null if undefined.
	 * @param maxParticipants Maximum number of participants, null if undefined.
	 * @param groupContext the group context or null if no context (e.g. buddy
	 *          groups)
	 * @param enableWaitinglist 
	 * @param enableAutoCloseRanks
	 * @return the created BusinessGroup
	 */
	//public BusinessGroup createAndPersistBusinessGroup(String type, Identity identity, String name, String description, Integer minParticipants,
	//		Integer maxParticipants, Boolean enableWaitinglist, Boolean enableAutoCloseRanks, BGContext groupContext);

	/**
	 * find the BusinessGroups list of type <code>type</code> associated with
	 * the supplied identity, where the identity is an Owner.
	 * 
	 * @param type Restrict find to this group type or null if not restricted to a
	 *          specific type
	 * @param identity
	 * @param bgContext Context or null if no context restriction should be
	 *          applied
	 * @return list of BusinessGroups, may be an empty list.
	 */
	//public List findBusinessGroupsOwnedBy(String type, Identity identity, BGContext bgContext);

	/**
	 * find the list of BuddyGroups associated with the supplied identity, where
	 * the identity is a Participiant.
	 * 
	 * @param type Restrict find to this group type or null if not restricted to a
	 *          specific type
	 * @param identity
	 * @param bgContext Context or null if no context restriction should be
	 *          applied
	 * @return list of BuddyGroups, may be an empty list.
	 */
	//public List<BusinessGroup> findBusinessGroupsAttendedBy(String type, Identity identity, BGContext bgContext);

	//public int countBusinessGroups(SearchBusinessGroupParams params, Identity identity, boolean ownedById, boolean attendedById, BGContext bgContext);
	
	//public List<BusinessGroup> findBusinessGroups(SearchBusinessGroupParams params, Identity identity, boolean ownedById, boolean attendedById, BGContext bgContext,
	//		int firstResult, int maxResults);

	
	/**
	 * @param currBusinessGroup
	 * @return The group or null if not found
	 */
	//public BusinessGroup findBusinessGroup(SecurityGroup secGroup);

	/**
	 * 
	 * @param nameOrDesc name or description of this group (put in a like search)
	 * @param type Restrict find to this group type or null if not restricted to a
	 *          specific type
	 * @return
	 */
	//public List<BusinessGroup> findBusinessGroup(String nameOrDesc, String type);
	
	/**
	 * commit the changes on a BusinessGroup instance to the persistence store
	 * 
	 * @param updatedBusinessGroup
	 */
	//public void updateBusinessGroup(BusinessGroup updatedBusinessGroup);

	/**
	 * delete a businessgroup from the persistence store
	 * 
	 * @param businessGroupTodelete
	 */
	//public void deleteBusinessGroup(BusinessGroup businessGroupTodelete);

	/**
	 * delete a business group and send a mail to all users in the group
	 * 
	 * @param businessGroupTodelete
	 * @param wControl
	 * @param ureq
	 * @param trans
	 * @param contactLists
	 */
	//public void deleteBusinessGroupWithMail(BusinessGroup businessGroupTodelete, WindowControl wControl, UserRequest ureq, Translator trans,
	//		List contactLists);

	/**
	 * delete all business groups from this list
	 * 
	 * @param businessGroups
	 */
	//public void deleteBusinessGroups(List businessGroups);

	/**
	 * Checks if an identity is in a specific business group, either as owner or
	 * as participant
	 * 
	 * @param identity The Identity
	 * @param groupName The group name
	 * @param groupContext The group context or null if group does not belong to a
	 *          group context (e.g. buddygroups)
	 * @return true if identity is in group, false otherwhise
	 */
	//public boolean isIdentityInBusinessGroup(Identity identity, String groupName, BGContext groupContext);

	/**
	 * @param identity
	 * @param businessGroup
	 * @return true if the given identity is in one or both security groups
	 *         (participants, owners)
	 */
	//public boolean isIdentityInBusinessGroup(Identity identity, BusinessGroup businessGroup);

	//public int countContacts(Identity identity);
	
	//public List<Identity> findContacts(Identity identity, int firstResult, int maxResults);

	/**
	 * @param currBusinessGroup
	 * @return The reloaded group
	 */
	//public BusinessGroup loadBusinessGroup(BusinessGroup currBusinessGroup);
	


	/**
	 * @param groupKey The group database key
	 * @param strict true: will throw exception if load failed false: will return
	 *          null if not found
	 * @return THe loaded group
	 */
	//public BusinessGroup loadBusinessGroup(Long groupKey, boolean strict);
	
	/**
	 * @param resource The OLAT resource
	 * @param strict true: will throw exception if load failed false: will return
	 *          null if not found
	 * @return The loaded group
	 */
	 //fxdiff VCRP-1,2: access control of resources
	//public BusinessGroup loadBusinessGroup(OLATResource resource, boolean strict);

	/**
	 * Create and persist a new business group based on a source group.
	 * 
	 * @param sourceBusinessGroup The group that will be used as the source group
	 *          and everything
	 * @param targetName
	 * @param targetDescription
	 * @param targetMin
	 * @param targetMax
	 * @param targetBgContext The group context that the target group will be
	 *          related to
	 * @param areaLookupMap The area lookup map (the target group will references
	 *          mapped areas) or null (target group will reference the same areas
	 *          as the original group)
	 * @param copyAreas
	 * @param copyCollabToolConfig
	 * @param copyRights
	 * @param copyOwners
	 * @param copyParticipants
	 * @param copyMemberVisibility
	 * @return BusinessGroup the copied group
	 */
	//public BusinessGroup copyBusinessGroup(BusinessGroup sourceBusinessGroup, String targetName, String targetDescription, Integer targetMin,
	//		Integer targetMax, BGContext targetBgContext, Map areaLookupMap, boolean copyAreas, boolean copyCollabToolConfig, boolean copyRights,
	//		boolean copyOwners, boolean copyParticipants, boolean copyMemberVisibility, boolean copyWaitingList);

	/**
	 * Adds a user to a group as owner and does all the magic that needs to be
	 * done: - add to secgroup (optional) - add to jabber roster - fire multi user event
	 * 
	 * @param wControl
	 * @param ureq the user request of the user who initiates the action
	 * @param trans used for mail text
	 * @param identity the user who should be added
	 * @param group
	 * @param flags the group configuration flag
	 * @param logger the user activity logger or null if nothing should be logged
	 * @param doOnlyPostAddingStuff true: user has already been added to the
	 *          security group, do only the other stuff, false: add user to
	 *          security group first
	 */
	//public void addOwnerAndFireEvent(Identity ureqIdentity, Identity identity, BusinessGroup group, BGConfigFlags flags, 
	//		boolean doOnlyPostAddingStuff);

	/**
	 * Adds a user to a group as participant and does all the magic that needs to
	 * be done: - add to secgroup (optional) - add to jabber roster - fire multi 
	 * user event
	 * 
	 * @param wControl
	 * @param ureq the user request of the user who initiates the action
	 * @param trans used for mail text
	 * @param identity the user who should be added
	 * @param group
	 * @param flags the group configuration flag
	 * @param logger the user activity logger or null if nothing should be logged
	 * @param doOnlyPostAddingStuff true: user has already been added to the
	 *          security group, do only the other stuff, false: add user to
	 *          security group first
	 */
	//public void addParticipantAndFireEvent(Identity ureqIdentity, Identity identity, BusinessGroup group, BGConfigFlags flags, 
	//		boolean doOnlyPostAddingStuff);

	/**
	 * Remove a user from a group as owner and does all the magic that needs to be
	 * done: - remove from secgroup (optional) - remove from jabber roster - fire 
	 * multi user event
	 * 
	 * @param wControl
	 * @param ureq the user request of the user who initiates the action
	 * @param trans used for mail text
	 * @param identity the user who should be removed
	 * @param group
	 * @param flags the group configuration flag
	 * @param logger the user activity logger or null if nothing should be logged
	 * @param doOnlyPostRemovingStuff true: user has already been removed from the
	 *          security group, do only the other stuff, false: remove user from
	 *          security group first
	 */
	//public void removeOwnerAndFireEvent(Identity ureqIdentity, Identity identity, BusinessGroup group, BGConfigFlags flags, 
	//		boolean doOnlyPostRemovingStuff);

	/**
	 * Remove a user from a group as participant and does all the magic that needs
	 * to be done: - remove from secgroup (optional) - remove from jabber roster -
	 * fire multi user event
	 * 
	 * @param wControl
	 * @param ureq the user request of the user who initiates the action
	 * @param trans used for mail text
	 * @param identity the user who should be removed
	 * @param group
	 * @param flags the group configuration flag
	 * @param logger the user activity logger or null if nothing should be logged
	 * @param doOnlyPostRemovingStuff true: user has already been removed from the
	 *          security group, do only the other stuff, false: remove user from
	 *          security group first
	 */
	//public void removeParticipantAndFireEvent(Identity ureqIdentity, Identity identity, BusinessGroup group, BGConfigFlags flags,
		//	boolean doOnlyPostRemovingStuff);

	/**
	 * Export group definitions to file.
	 * 
	 * @param context
	 * @param fExportFile
	 */
	//public void exportGroups(BGContext context, File fExportFile);

	/**
	 * Import previousely exported group definitions.
	 * 
	 * @param context
	 * @param fGroupExportXML
	 */
	//public void importGroups(BGContext context, File fGroupExportXML);

	/**
	 * Adds a user to a waiting-list of a group and does all the magic that needs to
	 * be done: - add to secgroup (optional) - add to jabber roster - send
	 * notification email - fire multi user event
	 * 
	 * @param wControl
	 * @param ureq the user request of the user who initiates the action
	 * @param trans used for mail text
	 * @param identity the user who should be added
	 * @param group
	 * @param flags the group configuration flag
	 * @param logger the user activity logger or null if nothing should be logged
	 * @param doOnlyPostAddingStuff true: user has already been added to the
	 *          security group, do only the other stuff, false: add user to
	 *          security group first
	 */
	//public void addToWaitingListAndFireEvent(Identity ureqIdentity, Identity identity, BusinessGroup group, boolean b);

	/**
	 * Remove a user from a waiting-list as participant and does all the magic that needs
	 * to be done: - remove from secgroup (optional) 
	 * send notification email - fire multi user event
	 * 
	 * @param wControl
	 * @param ureq the user request of the user who initiates the action
	 * @param trans used for mail text
	 * @param identity the user who should be removed
	 * @param group
	 * @param flags the group configuration flag
	 * @param logger the user activity logger or null if nothing should be logged
	 * @param doOnlyPostRemovingStuff true: user has already been removed from the
	 *          security group, do only the other stuff, false: remove user from
	 *          security group first
	 */
	//public void removeFromWaitingListAndFireEvent(Identity ureqIdentity, Identity identity, BusinessGroup waitingListGroup, boolean b);

	/**
	 * Move users from a waiting-list to participant-list.
	 * 
	 * @param identities
	 * @param windowControl
	 * @param ureq
	 * @param translator
	 * @param currBusinessGroup
	 * @param flags
	 * @param userActivityLogger
	 */
	//public BusinessGroupAddResponse moveIdenityFromWaitingListToParticipant(List<Identity> identities, Identity ureqIdentity, BusinessGroup currBusinessGroup, BGConfigFlags flags);

	/**
	 * Find all business-groups where the idenity is on the waiting-list.
	 * @param groupType
	 * @param identity
	 * @param bgContext
	 * @return List of BusinessGroup objects
	 */
	//public List findBusinessGroupsWithWaitingListAttendedBy(String groupType, Identity identity, BGContext bgContext);

	//fxdiff VCRP-1,2: access control of resources
	//public List<BusinessGroup> findBusinessGroups(Collection<String> types, Identity identityP, Long id, String name, String description, String owner);
	
	//public List<BusinessGroup> findBusinessGroups(Collection<Long> keys);
	
	/**
	 * Get postion of an idenity on a certain waiting-list 
	 * @param identity
	 * @param businessGroup
	 * @return 0=not found on waiting-list 
	 */
	//public int getPositionInWaitingListFor(Identity identity, BusinessGroup businessGroup);

	/**
	 * Get all business-groups.
	 * @return List of BusinessGroup objects
	 */
	//public List<BusinessGroup> getAllBusinessGroups();

  /**
   * Add a list of identity as owner to a business-group.
   * @param ureqIdentity       This identity triggered the method (typically identity of user-request).
   * @param addIdentities      List of identity
   * @param currBusinessGroup  Add list of identity to this business-group.
   * @param flags              Business-group configuration flags.
   * @param userActivityLogger Use this logger to log event.
   * @return
   */
	//public BusinessGroupAddResponse addOwnersAndFireEvent(Identity ureqIdentity, List<Identity> addIdentities, BusinessGroup currBusinessGroup, BGConfigFlags flags);

  /**
   * Add a list of identity as participant to a business-group.
   * @param ureqIdentity       This identity triggered the method (typically identity of user-request).
   * @param addIdentities      List of identity
   * @param currBusinessGroup  Add list of identity to this business-group.
   * @param flags              Business-group configuration flags.
   * @param userActivityLogger Use this logger to log event.
   * @return
   */
	//public BusinessGroupAddResponse addParticipantsAndFireEvent(Identity ureqIdentity, List<Identity> addIdentities, BusinessGroup currBusinessGroup, BGConfigFlags flags);

  /**
   * Remove a list of identity as owner from a business-group.
   * @param ureqIdentity       This identity triggered the method (typically identity of user-request).
   * @param addIdentities      List of identity
   * @param currBusinessGroup  Remove list of identity from this business-group.
   * @param flags              Business-group configuration flags.
   * @param userActivityLogger Use this logger to log event.
   */
	//public void removeOwnersAndFireEvent(Identity ureqIdentity, List<Identity> addIdentities, BusinessGroup currBusinessGroup, BGConfigFlags flags);

	//fxdiff VCRP-1,2: access control of resources
	//public void removeAndFireEvent(Identity ureqIdentity, List<Identity> addIdentities, SecurityGroup secGroup);

	
  /**
   * Remove a list of identity as participant from a business-group.
   * @param ureqIdentity       This identity triggered the method (typically identity of user-request).
   * @param identities         List of identity
   * @param currBusinessGroup  Remove list of identity from this business-group.
   * @param flags              Business-group configuration flags.
   * @param userActivityLogger Use this logger to log event.
   */
  //public void removeParticipantsAndFireEvent(Identity ureqIdentity, List<Identity> identities, BusinessGroup currBusinessGroup, BGConfigFlags flags);

  /**
   * Add a list of identity to waiting-list of a business-group.
   * @param ureqIdentity       This identity triggered the method (typically identity of user-request).
   * @param addIdentities      List of identity
   * @param currBusinessGroup  Add list of identity to this business-group.
   * @param flags              Business-group configuration flags.
   * @param userActivityLogger Use this logger to log event.
   * @return
   */
	//public BusinessGroupAddResponse addToWaitingListAndFireEvent(Identity ureqIdentity, List<Identity> addIdentities, BusinessGroup currBusinessGroup, BGConfigFlags flags);

  /**
   * Remove a list of identity from waiting-list of a business-group.
   * @param ureqIdentity        This identity triggered the method (typically identity of user-request).
   * @param identities          List of identity
   * @param currBusinessGroup   Remove list of identity from this business-group.
   * @param flags               Business-group configuration flags.
   * @param userActivityLogger  Use this logger to log event.
   */
	//public void removeFromWaitingListAndFireEvent(Identity ureqIdentity, List<Identity> identities, BusinessGroup currBusinessGroup, BGConfigFlags flags);

	//fxdiff VCRP-1,2: access control of resources
	//public BusinessGroupAddResponse addToSecurityGroupAndFireEvent(Identity ureqIdentity, List<Identity> addIdentities, SecurityGroup secGroup);
	
	//public void exportGroup(BusinessGroup businessGroup, File file);

	//public void archiveGroups(BGContext context, File exportFile);

	/**
	 * Set certain business-group as active (set last-usage and delete time stamp for 'SEND_DELETE_EMAIL_ACTION' in LifeCycleManager):
	 * @param currBusinessGroup
	 */
	//public BusinessGroup setLastUsageFor(BusinessGroup currBusinessGroup);

	/**
	 * Creates business-groups with certain name when no group in a given BGContext with this names already exists.
	 * @param allNames
	 * @param bgContext
	 * @param bgDesc
	 * @param bgMin
	 * @param bgMax
	 * @param enableWaitinglist
	 * @param enableAutoCloseRanks
	 * @return  Returns list of created business-groups or null if any groups-name already exist.
	 */
	//public Set<BusinessGroup> createUniqueBusinessGroupsFor(Set<String> allNames, BGContext bgContext, String bgDesc, Integer bgMin, Integer bgMax,
	//		Boolean enableWaitinglist, Boolean enableAutoCloseRanks);

	/**
	 * Extension-point method to register objects which have deletable group-data.
	 * Listener will be called in method deleteBusinessGroup.
	 * @param listener
	 */
	//public void registerDeletableGroupDataListener(DeletableGroupData listener);

	//public List<String> getDependingDeletablableListFor(BusinessGroup currentGroup, Locale locale);

}


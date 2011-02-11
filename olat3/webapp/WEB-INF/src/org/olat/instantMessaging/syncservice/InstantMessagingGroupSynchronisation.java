package org.olat.instantMessaging.syncservice;

import java.util.List;


public interface InstantMessagingGroupSynchronisation {

	/**
	 * Creates a shared buddy group where all members see each other on their
	 * rosters
	 * 
	 * @param groupId olat ressource id
	 * @param displayName name shown in the roster
	 * @param initialMembers array with olat usernames
	 */
	public abstract boolean createSharedGroup(String groupId, String displayName, List<String> initialMembers);
	
	/**
	 * creates an empty shared group
	 * @param groupId
	 * @param displayName
	 */
	public abstract boolean createSharedGroup(String groupId, String displayName);

	/**
	 * Rename shared buddy group on the IM server
	 * 
	 * @param groupId
	 * @param displayName
	 */
	public abstract boolean renameSharedGroup(String groupId, String displayName);

	/**
	 * @param groupId
	 */
	public abstract boolean deleteSharedGroup(String groupId);

	/**
	 * 
	 * @param groupId
	 * @param user
	 */
	public abstract boolean addUserToSharedGroup(String groupId, String username);

	/**
	 * @param groupId
	 * @param users list of usernames
	 */
	public abstract boolean addUsersToSharedGroup(String groupId, List<String> usernames);

	/**
	 * @param groupId
	 * @param username
	 */
	public abstract boolean removeUserFromSharedGroup(String groupId, String username);

	/**
	 * @param groupId
	 * @param users
	 */
	public abstract boolean removeUsersFromSharedGroup(String groupId, String[] users);

}
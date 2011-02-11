package org.olat.instantMessaging.syncservice;

import java.util.List;

import org.olat.core.id.Identity;
import org.olat.instantMessaging.ui.ConnectedUsersListEntry;

/**
 * 
 * Description:<br>
 * get information about all logged in users either locally or clusterWide depending on the impl.
 * 
 * <P>
 * Initial Date:  06.08.2008 <br>
 * @author guido
 */
public interface InstantMessagingSessionItems {
	
	/**
	 * returns a list of all connected users. If in single vm mode it collects the olat sessions if cluster wide
	 * the implementation asks the IM server for a list of all sessions.
	 * @param the identity of the current user which gets excluded from the cache
	 * @return the list
	 */
	public List<ConnectedUsersListEntry> getConnectedUsers(Identity currentUser);

}

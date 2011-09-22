package org.olat.instantMessaging.syncservice;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.ProviderManager;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.cache.n.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.instantMessaging.AdminUserConnection;
import org.olat.instantMessaging.IMConfig;
import org.olat.instantMessaging.ImPreferences;
import org.olat.instantMessaging.ImPrefsManager;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.syncservice.IMSessionItems.Item;
import org.olat.instantMessaging.ui.ConnectedUsersListEntry;

public class RemoteSessionsOnIMServerOverXMPP  implements InstantMessagingSessionItems {
	
	OLog log = Tracing.createLoggerFor(this.getClass());
	private AdminUserConnection adminUser;
	private CacheWrapper sessionItemsCache;
	private ImPrefsManager imPrefsManager;
	
	public RemoteSessionsOnIMServerOverXMPP(ImPrefsManager imPrefsManager) {
		this.imPrefsManager = imPrefsManager;
		ProviderManager providerMgr = ProviderManager.getInstance();
		//register iq handler
		providerMgr.addIQProvider("query", SessionItems.NAMESPACE, new SessionItems.Provider());
	}
	
	public void setConnection(AdminUserConnection adminUser) {
		this.adminUser = adminUser;
	}

	/**
	 * 
	 * @see org.olat.instantMessaging.AllOnlineUsers#getConnectedUsers()
	 */
	public List<ConnectedUsersListEntry> getConnectedUsers(Identity currentUser) {
		/**
		 * create a cache for the entries as looping over a few hundred entries need too much time.
		 * Every node has its own cache and therefore no need to inform each other
		 * o_clusterOK by guido
		 */
		if (sessionItemsCache == null) {
			synchronized (this) {
				sessionItemsCache = CoordinatorManager.getInstance().getCoordinator().getCacher().getOrCreateCache(this.getClass(), "items");
			}
		}
		String currentUsername = currentUser.getName();
		List<ConnectedUsersListEntry> entries = new ArrayList<ConnectedUsersListEntry>();
		IMSessionItems imSessions = (IMSessionItems) sendPacket(new SessionItems());
		List<IMSessionItems.Item> sessions = imSessions.getItems();
		
		for (Iterator<Item> iter = sessions.iterator(); iter.hasNext();) {
			IMSessionItems.Item item = iter.next();
			
			if (item.getResource().startsWith(IMConfig.RESOURCE)) {
				ConnectedUsersListEntry entry = (ConnectedUsersListEntry)sessionItemsCache.get(item.getUsername());
				if (entry != null && !item.getUsername().equals(currentUsername)) {
					entries.add(entry);
					log.debug("loading item from cache: "+item.getUsername());
				} else {
				
					Identity identity = BaseSecurityManager.getInstance().findIdentityByName(item.getUsername());
					if (identity != null) {
						identity = (Identity) DBFactory.getInstance().loadObject(identity);
						try {
							ImPreferences imPrefs = imPrefsManager.loadOrCreatePropertiesFor(identity);
							if ( (imPrefs != null) ) {
								entry = new ConnectedUsersListEntry(item.getUsername(), identity.getUser().getPreferences().getLanguage());
								entry.setName(identity.getUser().getProperty(UserConstants.LASTNAME, null));
								entry.setPrename(identity.getUser().getProperty(UserConstants.FIRSTNAME, null));
								entry.setShowAwarenessMessage(imPrefs.isAwarenessVisible());
								entry.setShowOnlineTime(imPrefs.isOnlineTimeVisible());
								entry.setAwarenessMessage(item.getPresenceMsg());
								entry.setInstantMessagingStatus(item.getPresenceStatus());
								entry.setLastActivity(item.getLastActivity());
								entry.setOnlineTime(item.getLoginTime());
								entry.setJabberId(InstantMessagingModule.getAdapter().getUserJid(item.getUsername()));
								entry.setVisibleToOthers(imPrefs.isVisibleToOthers());
								entry.setResource(item.getResource());
								entries.add(entry);
								
								//put in cache. Sync. is done by cache
								sessionItemsCache.put(item.getUsername(), entry);
							}
						} catch (AssertException ex) {
							log.warn("Can not load IM-Prefs for identity=" + identity, ex);
						}
					}
				}
			}
		} //end of loop
		return entries;
	}
	
	private IQ sendPacket(IQ packet) {
		XMPPConnection con = adminUser.getConnection();
		try {
			packet.setFrom(con.getUser());
			PacketCollector collector = con.createPacketCollector(new PacketIDFilter(packet.getPacketID()));
			con.sendPacket(packet);
			IQ response = (IQ) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());
			collector.cancel();
			
			if (response == null) {
				log.warn("Error while trying to get all sessions IM server. Response was null!");
				return null;
			}
			if (response.getError() != null) {
				log.warn("Error while trying to get all sessions IM server. "+response.getError().getMessage());
				return null;
			} else if (response.getType() == IQ.Type.ERROR) {
				//TODO:gs  handle conflict case when user already exists
				//System.out.println("error response: "+response.getChildElementXML());
				log.warn("Error while trying to get all sessions at IM server");
				return null;
			}
			return response;
		} catch (RuntimeException e) {
			log.warn("Error while trying to get all sessions at IM server");
			return null;
		} catch (Exception e) {
			log.warn("Error while trying to get all sessions at IM server");
			return null;
		}
	}

}

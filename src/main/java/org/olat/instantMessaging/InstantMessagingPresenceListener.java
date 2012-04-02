package org.olat.instantMessaging;

import java.util.List;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.RosterPacket;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
class InstantMessagingPresenceListener implements PacketListener {
	
	private XMPPConnection connection;
	private List<String> subscribedUsers;
	
	protected InstantMessagingPresenceListener(XMPPConnection connection, List<String> subscribedUsers) {
		this.connection = connection;
		this.subscribedUsers = subscribedUsers;
	}
	
	
	public void processPacket(Packet packet) {
		Presence presence = (Presence) packet;
		if (presence.getType() == Presence.Type.subscribe) {
			Presence response = new Presence(Presence.Type.subscribe);
			response.setTo(presence.getFrom());
			// System.out.println("subscribed to: "+presence.getFrom());
			connection.sendPacket(response);
			// ask also for subscription
			if (!subscribedUsers.contains(presence.getFrom())) {
				response = null;
				response = new Presence(Presence.Type.subscribe);
				response.setTo(presence.getFrom());
				connection.sendPacket(response);
				// update the roster with the new user
				RosterPacket rosterPacket = new RosterPacket();
				rosterPacket.setType(IQ.Type.SET);
				RosterPacket.Item item = new RosterPacket.Item(presence.getFrom(), InstantMessagingClient.parseName(presence.getFrom()));
				item.addGroupName(InstantMessagingClient.OLATBUDDIES);
				item.setItemType(RosterPacket.ItemType.both);
				// item.setItemStatus(RosterPacket.ItemStatus.fromString());
				rosterPacket.addRosterItem(item);
				connection.sendPacket(rosterPacket);
			}
		}
		if (presence.getType() == Presence.Type.subscribe) {
			subscribedUsers.add(presence.getFrom());
		}
	}

}

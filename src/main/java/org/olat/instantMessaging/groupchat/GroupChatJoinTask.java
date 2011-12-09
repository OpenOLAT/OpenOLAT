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
package org.olat.instantMessaging.groupchat;

import java.util.Date;

import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.RoomInfo;
import org.olat.core.gui.control.Event;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.instantMessaging.InstantMessagingEvent;

public class GroupChatJoinTask implements Runnable {

	MultiUserChat muc;
	XMPPConnection connection;
	String roomJID;
	String nickname;
	GenericEventListener listeningController;
	private PacketListener messageListener;
	private PacketListener participationsListener;
	String roomName;
	OLog log = Tracing.createLoggerFor(GroupChatJoinTask.class);
	private OLATResourceable ores;

	public GroupChatJoinTask(OLATResourceable ores, MultiUserChat muc, XMPPConnection connection, String roomJID, String nickname, String roomName,
			GenericEventListener listeningController) {
		this.ores = ores;
		this.muc = muc;
		this.connection = connection;
		this.roomJID = roomJID;
		this.roomName = roomName;
		this.nickname = nickname;
		this.listeningController = listeningController;
	}

	public void run() {
		//o_clusterOK by guido
		OLATResourceable syncOres = OresHelper.createOLATResourceableInstance("GroupChatJoinTask",ores.getResourceableId());
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(syncOres, new SyncerExecutor() {
			public void execute() {
				if (connection != null && connection.isAuthenticated()) {
					try {
						RoomInfo info = MultiUserChat.getRoomInfo(connection, roomJID);
						// throws an exception if the room does not exists
						muc.join(nickname); // log in anonymous
						addMessageListener();
						addParticipationsListener();
					} catch (XMPPException e) {
						try {
							if (e.getXMPPError().getCode() == 404) {
								muc.create(nickname);
								Form form = muc.getConfigurationForm();
								Form answer = form.createAnswerForm();
	
								FormField fieldRoomName = new FormField("muc#roomconfig_roomname");
								fieldRoomName.addValue(roomName);
								answer.addField(fieldRoomName);
	
								FormField fieldMaxUsers = new FormField("muc#roomconfig_maxusers");
								fieldMaxUsers.addValue("0");// 0 means unlimited
								answer.addField(fieldMaxUsers);
	
								muc.sendConfigurationForm(answer);
								addMessageListener();
								addParticipationsListener();
								
								listeningController.event(new Event("ready"));
								
							} else {
								log.warn("Could not join group chatroom (case 1): " + roomName + " for user: " + nickname, e);
								removeListeners();
							}
						} catch (XMPPException e1) {
							log.warn("Could not create group chatroom(case 2): " + roomName + " for user: " + nickname, e1);
						} catch (Exception e1) {
							/**
							 * this case happens when the servername is set to localhost or the servername is not configured
							 * the same as in OLAT config file and Openfire (need restart when chanched)
							 */
							log.warn("Could not create group chatroom(case 3): " + roomName + " for user: " + nickname, e1);
						}
					} catch (Exception e) {// we catch also nullpointers that may occure in smack lib.
						log.warn("Could not create group chatroom(case 4): " + roomName + " for user: " + nickname, e);
					}
				}
			}
		});
	}

	void removeListeners() {
		if (messageListener != null) muc.removeMessageListener(messageListener);
		if (participationsListener != null) muc.removeParticipantListener(participationsListener);
	}

	/**
	 * listens to new messages for this chatroom
	 */
	void addMessageListener() {
		messageListener = new PacketListener() {

			public void processPacket(Packet packet) {
				Message jabbmessage = (Message) packet;
				if (log.isDebug()) log.debug("processPacket Msg: to=" + jabbmessage.getTo() );
				jabbmessage.setProperty("receiveTime", new Long(new Date().getTime()));
				if ((jabbmessage.getType() == Message.Type.groupchat) && jabbmessage.getBody() != null) {
					listeningController.event(new InstantMessagingEvent(jabbmessage, "groupchat"));
				}
			}
		};
		muc.addMessageListener(messageListener);
	}

	/**
	 * listen to new people joining the room in realtime and and set the new
	 * content which sets the component to dirty which forces it to redraw
	 */
	void addParticipationsListener() {
		participationsListener = new PacketListener() {

			public void processPacket(Packet packet) {
				Presence presence = (Presence) packet;
				if (log.isDebug()) log.debug("processPacket Presence: to=" + presence.getTo() + " , " );
				if (presence.getFrom() != null) {
					listeningController.event(new InstantMessagingEvent(presence, "participant"));
				}
			}
		};
		muc.addParticipantListener(participationsListener);

	}

	protected PacketListener getMessageListener() {
		return messageListener;
	}

	protected PacketListener getParticipationsListener() {
		return participationsListener;
	}

}

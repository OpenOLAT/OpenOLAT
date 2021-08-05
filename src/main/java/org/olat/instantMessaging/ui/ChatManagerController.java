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
package org.olat.instantMessaging.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.OLATResourceable;
import org.olat.instantMessaging.CloseInstantMessagingEvent;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.model.Buddy;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * ChatManagerController: Manages peer to peer chats
 * 
 * <P>
 * Initial Date:  05.05.2008 <br>
 * @author guido
 */
public class ChatManagerController extends BasicController {

	private final VelocityContainer container;
	private final Map<Long, ChatController> chats = new HashMap<>();

	@Autowired
	private InstantMessagingService imService;

	public ChatManagerController(UserRequest ureq, WindowControl control) {
		super(ureq, control);
		
		container = createVelocityContainer("chats");
		container.contextPut("chats", chats);
		putInitialPanel(container);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//no events
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source instanceof ChatController) {
			if(event instanceof CloseInstantMessagingEvent) {
				CloseInstantMessagingEvent close = (CloseInstantMessagingEvent)event;
				
				Long chatId = close.getChatId();
				chats.remove(chatId);
				Component c = container.getComponent(chatId.toString());
				container.remove(c);
				
				ChatController chatCtr = (ChatController)source;
				imService.unlistenChat(getIdentity(), chatCtr.getOlatResourceable(), chatCtr);
			}
			//forward event also to main controller
			fireEvent(ureq, event);
		}
	}

	@Override
	protected void doDispose() {
		chats.clear();
	}
	
	/**
	 * Close the chats windows
	 */
	protected void closeAllChats() {
		List<Long> chatKeys = new ArrayList<>(chats.keySet());
		for(Long chatKey :chatKeys) {
			closeChat(chatKey);
		}
		chats.clear();
	}
	
	protected void closeChat(OLATResourceable ores) {
		closeChat(ores.getResourceableId());
	}
	
	private void closeChat(Long chatKey) {
		Component p = container.getComponent(chatKey.toString());
		if(p != null) {
			container.remove(p);
		}
		ChatController ctrl = chats.get(chatKey);
		if(ctrl != null) {
			ctrl.closeChat();
		}
		chats.remove(chatKey);
	}
	
	public void createChat(UserRequest ureq, Buddy buddy) {	
		if (buddy == null) return;
		OLATResourceable ores = imService.getPrivateChatResource(getIdentity().getKey(), buddy.getIdentityKey());
		if(chats.containsKey(ores.getResourceableId())) {
			return;
		}

		int offsetX = 100 + (chats.size() * 10);
		int offsetY = 100 + (chats.size() * 5);
		String roomName = translate("im.chat.with") + ": " + buddy.getName();
		ChatController chat = new ChatController(ureq, getWindowControl(), ores, roomName, buddy.getIdentityKey(), false, 400, 320, offsetX, offsetY);
		listenTo(chat);
		container.put(chat.getOlatResourceable().getResourceableId().toString(), chat.getInitialComponent());
		chats.put(chat.getOlatResourceable().getResourceableId(), chat);
	}

	public void createGroupChat(UserRequest ureq, OLATResourceable ores, String roomName, boolean vip) {
		if (ores == null || chats.containsKey(ores.getResourceableId())) {
			return; // chat with this resource is already ongoing
		}
		
		int offsetX = 100 + (chats.size() * 10);
		int offsetY = 100 + (chats.size() * 5);
		ChatController chat = new ChatController(ureq, getWindowControl(), ores, roomName, null, vip, 550, 320, offsetX, offsetY);
		listenTo(chat);
		container.put(chat.getOlatResourceable().getResourceableId().toString(), chat.getInitialComponent());
		chats.put(chat.getOlatResourceable().getResourceableId(), chat);
	}

	/**
	 * check whether already a chat is running for this buddy
	 * @param jabberId
	 * @return
	 */
	public boolean hasRunningChat(OLATResourceable chatResource) {
		return chats.containsKey(chatResource.getResourceableId());
	}
}
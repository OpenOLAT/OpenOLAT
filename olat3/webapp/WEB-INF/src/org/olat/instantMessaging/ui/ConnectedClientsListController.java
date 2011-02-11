/**
* OLAT - Online Learning and Training<br />
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br />
* you may not use this file except in compliance with the License.<br />
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br />
* software distributed under the License is distributed on an "AS IS" BASIS, <br />
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br />
* See the License for the specific language governing permissions and <br />
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br />
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.instantMessaging.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Formatter;
import org.olat.core.util.event.EventBus;
import org.olat.core.util.event.MultiUserEvent;
import org.olat.core.util.resource.OresHelper;
import org.olat.instantMessaging.InstantMessaging;
import org.olat.instantMessaging.InstantMessagingClient;
import org.olat.instantMessaging.InstantMessagingConstants;
import org.olat.instantMessaging.InstantMessagingModule;
import org.olat.instantMessaging.rosterandchat.ChatManagerController;

/**
 * Controller for a user list, where all users are shown together with real name
 * and last status message. What is visible or not is controlled by the users
 * preferences settings.<br />
 * 
 * @author Guido Schnider
 *         Initial Date: 15.08.2004 <br />
 */

public class ConnectedClientsListController extends BasicController {
	private VelocityContainer content = createVelocityContainer("connectedclientslist");
	private Link refreshButton;
	private Locale locale;
	private Formatter f;
	private Map<String, String> lastActivity = new HashMap<String, String>();
	private List<ConnectedUsersListEntry> entries = new ArrayList<ConnectedUsersListEntry>();

	private TableController tableCtr;
	private ConnectedUsersTableModel tableModel;
	private ChatManagerController chatMgrCtrl;
	private InstantMessaging im = InstantMessagingModule.getAdapter();

	private List allChats;
	private EventBus singleUserEventCenter;
	
	/**
	 * 
	 * @param ureq
	 * @param wControl
	 */
	public ConnectedClientsListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		//this controller gets rendered in a new window, we have to make sure polling works also in this window
		JSAndCSSComponent jsComp = new JSAndCSSComponent("pollintervall", this.getClass(), null, null, false, null, InstantMessagingModule.getIDLE_POLLTIME());
		content.put("polling", jsComp);
		locale = ureq.getLocale();
		f = Formatter.getInstance(locale);
		refreshButton = LinkFactory.createButtonSmall("command.refresh", content, this);
		updateUI(ureq, true);
		chatMgrCtrl = new ChatManagerController(ureq, wControl);
		content.put("chats", chatMgrCtrl.getInitialComponent());
		listenTo(chatMgrCtrl);
		
		this.singleUserEventCenter = ureq.getUserSession().getSingleUserEventCenter();
		allChats = (List) ureq.getUserSession().getEntry("chats");
		if (allChats == null) {
			allChats = new ArrayList();
			ureq.getUserSession().putEntry("chats", allChats);
		}
		allChats.add(Integer.toString(hashCode()));
		singleUserEventCenter.fireEventToListenersOf(new MultiUserEvent("ChatWindowOpened"), OresHelper.createOLATResourceableType(InstantMessaging.class));

		putInitialPanel(content);
	}

	/**
	 * rebuild the user list with the latest entries
	 */
	private void updateUI(UserRequest ureq, boolean init) {
		if (init) {
			TableGuiConfiguration tableConfig = new TableGuiConfiguration();
			removeAsListenerAndDispose(tableCtr);
			tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
			listenTo(tableCtr);
			tableModel = new ConnectedUsersTableModel(getTranslator(), InstantMessagingModule.isEnabled());
			tableModel.addColumnDescriptors(tableCtr);
			tableCtr.setTableDataModel(tableModel);
			tableCtr.setSortColumn(1, true);
			content.put("usertable", tableCtr.getInitialComponent());
		}

		entries.clear();
		int invisibleUsers = 0;
		List<ConnectedUsersListEntry> l = im.getAllConnectedUsers(ureq.getIdentity());
		
		if (l == null) {
			content.contextPut("invisibleUsers", 0);
			return;
		}
		
		List<ConnectedUsersListEntry> m = new ArrayList<ConnectedUsersListEntry>();
		for (Iterator<ConnectedUsersListEntry> it = l.iterator(); it.hasNext();) {
			ConnectedUsersListEntry entry = it.next();
			if (!entry.isVisible()){ 
				invisibleUsers++;
			} else {
				m.add(entry);
			}
		}
		tableModel.setEntries(m);
		tableCtr.modelChanged();
		//TODO:gs TODO get invisible users by looping in GUI
		content.contextPut("invisibleUsers", invisibleUsers);
		content.contextPut("havelist", Boolean.TRUE);
		
	}

	/**
	 * translate the default status messages for the list of logged in users
	 * @param statusMsg
	 * @return
	 */
	private String translatedDefautStatusMsg(String statusMsg) {
		if(statusMsg.equals(InstantMessagingConstants.PRESENCE_MODE_AVAILABLE)) {
			return translate("presence.available");
		} else if(statusMsg.equals(InstantMessagingConstants.PRESENCE_MODE_CHAT)) {
			return translate("presence.chat");
		} else if(statusMsg.equals(InstantMessagingConstants.PRESENCE_MODE_AWAY)) {
			return translate("presence.away");
		} else if(statusMsg.equals(InstantMessagingConstants.PRESENCE_MODE_XAWAY)) {
			return translate("presence.xa");
		} else if(statusMsg.equals(InstantMessagingConstants.PRESENCE_MODE_DND)) {
			return translate("presence.dnd");
		}
		return statusMsg;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == tableCtr) {
			InstantMessagingClient client = im.getClientManager().getInstantMessagingClient(ureq.getIdentity().getName());
			boolean chattingAllowed = false;
			if (client != null && !client.isChatDisabled()) chattingAllowed = true;
			
			if (chattingAllowed) {
				TableEvent te = (TableEvent) event;
				int row = te.getRowId();
				ConnectedUsersListEntry entry = tableModel.getEntryAt(row);
				chatMgrCtrl.createChat(ureq, getWindowControl(), entry.getJabberId());
			} else {
				showInfo("im.chat.forbidden");
			}
		}
	}
	
	

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (event.getCommand().equals("close")) {
			doDispose();
			return;
		}
		if (source == refreshButton) {
			updateUI(ureq, false);
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		allChats.remove(Integer.toString(hashCode()));
		singleUserEventCenter.fireEventToListenersOf(new MultiUserEvent("ChatWindowClosed"), OresHelper.createOLATResourceableType(InstantMessaging.class));
	}

}
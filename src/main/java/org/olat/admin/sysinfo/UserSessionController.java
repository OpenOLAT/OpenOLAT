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

package org.olat.admin.sysinfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.olat.admin.sysinfo.model.UserSessionView;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.BreadcrumbPanelAware;
import org.olat.core.gui.components.table.BooleanColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.UserSession;
import org.olat.core.util.session.UserSessionManager;
import org.olat.instantMessaging.InstantMessagingService;
import org.olat.instantMessaging.OpenInstantMessageEvent;
import org.olat.instantMessaging.model.Buddy;
import org.olat.user.UserManager;

/**
 *  Initial Date:  01.09.2004
 *  @author Mike Stock
 */

public class UserSessionController extends BasicController implements BreadcrumbPanelAware {
	
	private VelocityContainer myContent;
	private TableController tableCtr;
	private UserSessionTableModel usessTableModel;

	private BreadcrumbPanel stackController;
	private final UserSessionManager sessionManager;
	private final InstantMessagingService imService;
	

	/**
	 * Controlls user session in admin view.
	 * 
	 * @param ureq
	 * @param wControl
	 */
	public UserSessionController(UserRequest ureq, WindowControl wControl) { 
		super(ureq, wControl);
		
		imService = CoreSpringFactory.getImpl(InstantMessagingService.class);
		sessionManager = CoreSpringFactory.getImpl(UserSessionManager.class);
		
		myContent = createVelocityContainer("sessions");
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("sess.last", 0, null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("sess.first", 1, null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("sess.identity", 2, null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("sess.authprovider", 3, null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("sess.fqdn", 4, null, getLocale()));		
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("sess.lastClick", 5, null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("sess.access", 9, null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("sess.duration", 6, null, getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("sess.mode", 7, null, getLocale()));
		tableCtr.addColumnDescriptor(new StaticColumnDescriptor("sess.details", "sess.details", translate("sess.details")));
		tableCtr.addColumnDescriptor(new BooleanColumnDescriptor("sess.chat", 8, "sess.chat", translate("sess.chat"), null));

		listenTo(tableCtr);
		reset();
		myContent.put("sessiontable", tableCtr.getInitialComponent());
		putInitialPanel(myContent);
	}

	@Override
	public void setBreadcrumbPanel(BreadcrumbPanel stackPanel) {
		this.stackController = stackPanel;
	}

	/**
	 * Re-initialize this controller. Fetches sessions again.
	 */
	public void reset() {
		Collection<UserSession> authUserSessions = sessionManager.getAuthenticatedUserSessions();
		List<UserSessionView> authUserSessionViews = new ArrayList<>(authUserSessions.size());
		for(UserSession authUserSession:authUserSessions) {
			if(authUserSession != null) {
				authUserSessionViews.add(new UserSessionView(authUserSession));
			}
		}
		usessTableModel = new UserSessionTableModel(authUserSessionViews, getIdentity().getKey());
		tableCtr.setTableDataModel(usessTableModel);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == tableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent)event;
				int selRow = te.getRowId();
				// session info (we only have authenticated sessions here
				UserSessionView usessw = (UserSessionView)tableCtr.getTableDataModel().getObject(selRow);
				if("sess.chat".equals(te.getActionId())) {
					Buddy buddy = imService.getBuddyById(usessw.getIdentityKey());
					OpenInstantMessageEvent e = new OpenInstantMessageEvent(buddy);
					ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, InstantMessagingService.TOWER_EVENT_ORES);
				} else if("sess.details".equals(te.getActionId())) {
					UserSession usess = usessw.getUserSession();
					UserSessionDetailsController detailsCtrl = new UserSessionDetailsController(ureq, getWindowControl(), usess);
					listenTo(detailsCtrl);
					
					String username = usess.getIdentity() == null ? "-"
							: UserManager.getInstance().getUserDisplayName(usess.getIdentity());
					stackController.pushController(username, detailsCtrl);
				}
			}
		}
	}
}
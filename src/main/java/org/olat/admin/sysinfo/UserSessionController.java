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
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.stack.StackedController;
import org.olat.core.gui.components.stack.StackedControllerAware;
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
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.UserSession;
import org.olat.core.util.session.UserSessionManager;
import org.olat.user.UserManager;

/**
 *  Initial Date:  01.09.2004
 *  @author Mike Stock
 */

public class UserSessionController extends BasicController implements StackedControllerAware {
	
	private VelocityContainer myContent;
	private TableController tableCtr;
	//private Formatter f;
	private UserSessionTableModel usessTableModel;
	private DialogBoxController dialogController;
	//private int selRow;
	private Link backLink, sessKillButton;

	private Panel myPanel;
	private final UserSessionManager sessionManager;
	private StackedController stackController;
	
	/**
	 * Timeframe in minutes is needed to calculate the last klicks from users in OLAT. 
	 */
	private static final int LAST_KLICK_TIMEFRAME = 5;
	private static final long DIFF = 1000 * 60 * LAST_KLICK_TIMEFRAME; // milliseconds of klick difference
	/**
	 * Controlls user session in admin view.
	 * 
	 * @param ureq
	 * @param wControl
	 */
	public UserSessionController(UserRequest ureq, WindowControl wControl) { 
		super(ureq, wControl);

		sessionManager = CoreSpringFactory.getImpl(UserSessionManager.class);
		
		//f = Formatter.getInstance(ureq.getLocale());

		myContent = createVelocityContainer("sessions");
		
		backLink = LinkFactory.createLinkBack(myContent, this);
		sessKillButton = LinkFactory.createButton("sess.kill", myContent, this);
		
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("sess.last", 0, null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("sess.first", 1, null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("sess.identity", 2, null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("sess.authprovider", 3, null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("sess.fqdn", 4, null, ureq.getLocale()));		
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("sess.access", 5, null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("sess.duration", 6, null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("sess.mode", 7, null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new StaticColumnDescriptor("sess.details", "table.action", translate("sess.details")));
		listenTo(tableCtr);
		reset();
		myContent.put("sessiontable", tableCtr.getInitialComponent());
		myPanel = putInitialPanel(myContent);
	}

	@Override
	public void setStackedController(StackedController stackPanel) {
		this.stackController = stackPanel;
	}

	/**
	 * Re-initialize this controller. Fetches sessions again.
	 */
	public void reset() {
		List<UserSession> authUserSessions = new ArrayList<UserSession>(sessionManager.getAuthenticatedUserSessions());
		usessTableModel = new UserSessionTableModel(authUserSessions);
		tableCtr.setTableDataModel(usessTableModel);
		// view number of user - lastKlick <= LAST_KLICK_TIMEFRAME min
		long now = System.currentTimeMillis();
		int counter = 0;
		for (UserSession usess : authUserSessions) {
			long lastklick = usess.getSessionInfo() == null ? -1 : usess.getSessionInfo().getLastClickTime();
			if ((now - lastklick) <= DIFF) {
				counter++;
			}
		}
		myContent.contextPut("scount", String.valueOf(counter));
		myContent.contextPut("minutes", String.valueOf(LAST_KLICK_TIMEFRAME));
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (source == backLink){
			myPanel.popContent();
			reset();
		} else if (source == sessKillButton){
			dialogController = activateYesNoDialog(ureq, null, translate("sess.kill.sure"), dialogController);
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == dialogController) {
			if (DialogBoxUIFactory.isYesEvent(event)) { 
				/*UserSession usess = (UserSession) usessTableModel.getObject(selRow);
				SessionInfo sessInfo = usess.getSessionInfo();
				if (usess.isAuthenticated()) {
					HttpSession session = sessInfo.getSession();
					if (session!=null) {
						try{
							session.invalidate();
						} catch(IllegalStateException ise) {
							// thrown when session already invalidated. fine. ignore.
						}
					}
					showInfo("sess.kill.done", sessInfo.getLogin() );
				}*/
				reset();
			}
		}
		else if (source == tableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent)event;
				int selRow = te.getRowId();
				// session info (we only have authenticated sessions here
				UserSession usess = (UserSession) tableCtr.getTableDataModel().getObject(selRow);
				UserSessionDetailsController detailsCtrl = new UserSessionDetailsController(ureq, getWindowControl(), usess);
				listenTo(detailsCtrl);
				
				String username = usess.getIdentity() == null ? "-"
						: UserManager.getInstance().getUserDisplayName(usess.getIdentity().getUser());
				stackController.pushController(username, detailsCtrl);
				
				
				//if (!usess.isAuthenticated()) throw new AssertException("usersession was not authenticated!?");
				
				
				
				
				
				
				
				
				
				/*VelocityContainer sesDetails = createVelocityContainer("sessionDetails");
				sesDetails.contextPut("us", usess);
				SessionInfo sessInfo = usess.getSessionInfo();
				sesDetails.contextPut("si", sessInfo);
				boolean isAuth = usess.isAuthenticated();
				sesDetails.contextPut("isauth", isAuth? "yes" : "-- NOT AUTHENTICATED!");

				long creatTime = -1;
				long lastAccessTime = -1; 
				
				boolean success = false;
				if (isAuth) {
					try {
						HttpSession se = sessInfo.getSession();
						creatTime = se.getCreationTime();
						lastAccessTime = se.getLastAccessedTime();
						success = true;
					} catch (Exception ise) {
						// nothing to do
					}
				}
				
				if (success) {
					sesDetails.contextPut("created", f.formatDateAndTime(new Date(creatTime)));
					sesDetails.contextPut("lastaccess", f.formatDateAndTime(new Date(lastAccessTime)));
				} else {
					sesDetails.contextPut("created", " -- this session has been invalidated --");
					sesDetails.contextPut("lastaccess", " -- this session has been invalidated --");
				}
				
				if (success) {
					// lock information
					String username = sessInfo.getLogin();
					List<String> lockList = new ArrayList<String>();
					List<LockEntry> locks = CoordinatorManager.getInstance().getCoordinator().getLocker().adminOnlyGetLockEntries();
					Formatter f = Formatter.getInstance(ureq.getLocale());
					for (LockEntry entry : locks) {
						if (entry.getOwner().getName().equals(username)) {
							lockList.add(entry.getKey()+" "+f.formatDateAndTime(new Date(entry.getLockAquiredTime())));
						}
					}					
					sesDetails.contextPut("locklist", lockList);
	
					// user environment
					sesDetails.contextPut("env", usess.getIdentityEnvironment());
	
					// GUI statistics
					Windows ws = Windows.getWindows(usess);
					StringBuilder sb = new StringBuilder();
					for (Iterator<Window> iterator = ws.getWindowIterator(); iterator.hasNext();) {
						Window window = iterator.next();
						sb.append("- Window ").append(window.getDispatchID()).append(" dispatch info: ").append(window.getLatestDispatchComponentInfo()).append("<br />");
					}
					sb.append("<br />");
					sesDetails.contextPut("guistats", sb.toString());
				}
				sesDetails.put("backLink", backLink);
				sesDetails.put("sess.kill", sessKillButton);
				
				myPanel.pushContent(sesDetails);
				*/
			}
		}
	}

	protected void doDispose() {
		// DialogBoxController and TableController get disposed by BasicController
	}
}
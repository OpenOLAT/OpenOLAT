/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.admin.sysinfo;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.Window;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.Formatter;
import org.olat.core.util.SessionInfo;
import org.olat.core.util.UserSession;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.LockEntry;

/**
 * 
 * Initial date: 15.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserSessionDetailsController extends BasicController {
	
	private final Link sessKillButton;
	private final VelocityContainer sesDetails;
	private DialogBoxController dialogController;
	
	private final UserSession usess;
	
	public UserSessionDetailsController(UserRequest ureq, WindowControl wControl, UserSession usess) {
		super(ureq, wControl);
		
		this.usess = usess;
		
		sesDetails = createVelocityContainer("sessionDetails");
		sesDetails.contextPut("us", usess);
		SessionInfo sessInfo = usess.getSessionInfo();
		sesDetails.contextPut("si", sessInfo);
		boolean isAuth = usess.isAuthenticated();
		sesDetails.contextPut("isauth", isAuth? "yes" : "-- NOT AUTHENTICATED!");

		long creatTime = -1;
		long lastAccessTime = -1; 
		long lastClickTime = -1;
		
		boolean success = false;
		if (isAuth) {
			try {
				HttpSession se = sessInfo.getSession();
				creatTime = se.getCreationTime();
				lastAccessTime = se.getLastAccessedTime();
				lastClickTime = sessInfo.getLastClickTime();
				success = true;
			} catch (Exception ise) {
				// nothing to do
			}
		}

		if (success) {
			Formatter f = Formatter.getInstance(ureq.getLocale());
			sesDetails.contextPut("created", f.formatDateAndTime(new Date(creatTime)));
			sesDetails.contextPut("lastaccess", f.formatDateAndTime(new Date(lastAccessTime)));
			sesDetails.contextPut("lastclick", f.formatDateAndTime(new Date(lastClickTime)));
		} else {
			sesDetails.contextPut("created", " -- this session has been invalidated --");
			sesDetails.contextPut("lastaccess", " -- this session has been invalidated --");
		}
		
		if (success) {
			// lock information
			Long identityKey = sessInfo.getIdentityKey();
			List<String> lockList = new ArrayList<>();
			List<LockEntry> locks = CoordinatorManager.getInstance().getCoordinator().getLocker().adminOnlyGetLockEntries();
			Formatter f = Formatter.getInstance(ureq.getLocale());
			for (LockEntry entry : locks) {
				if (entry.getOwner().getKey().equals(identityKey)) {
					lockList.add(entry.getKey() + " " + f.formatDateAndTime(new Date(entry.getLockAquiredTime())));
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
		sessKillButton = LinkFactory.createButton("sess.kill", sesDetails, this);
		sesDetails.put("sess.kill", sessKillButton);
		putInitialPanel(sesDetails);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == sessKillButton){
			dialogController = activateYesNoDialog(ureq, null, translate("sess.kill.sure"), dialogController);
		}
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == dialogController) {
			if (DialogBoxUIFactory.isYesEvent(event)) { 
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
					showInfo("sess.kill.done", sessInfo.getIdentityKey().toString());
				}
			}
		}
	}
}

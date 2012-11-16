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

import org.olat.admin.AdminModule;
import org.olat.basesecurity.AuthHelper;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.session.UserSessionManager;
import org.olat.core.util.session.UserSessionModule;

/**
 * 
 * Initial date: 15.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserSessionConfigAdminController extends FormBasicController {
	private IntegerElement sessionTimeoutEl;
	private IntegerElement sessionTimeoutAuthEl;
	private IntegerElement maxSessionsEl;
	private IntegerElement nbrSessionsEl;
	
	private DialogBoxController invalidateAllConfirmController;
	private DialogBoxController blockLoginConfirmController;
	private DialogBoxController rejectDMZRequestsConfirmController;
	
	private FormLink saveLink, invalidateOldSessionLink, invalidateAllSessionLink;
	private FormLink allowLoginLink, blockLoginLink;
	private FormLink rejectDMZRequestsLink, allowDMZRequestsLink;
	
	private final UserSessionModule sessionModule;
	private final UserSessionManager sessionManager;
	
	public UserSessionConfigAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		sessionModule = CoreSpringFactory.getImpl(UserSessionModule.class);
		sessionManager = CoreSpringFactory.getImpl(UserSessionManager.class);
		
		initForm(ureq);		
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		int sessionTimeout = sessionModule.getSessionTimeout();
		sessionTimeoutEl = uifactory.addIntegerElement("session.timeout", "session.timeout.label", sessionTimeout, formLayout);
		int initialSessionAuth = sessionModule.getSessionTimeoutAuthenticated();
		sessionTimeoutAuthEl = uifactory.addIntegerElement("session.timeout.auth", "session.timeout.auth.label", initialSessionAuth, formLayout);
		int maxSessions = AdminModule.getMaxSessions();
		maxSessionsEl = uifactory.addIntegerElement("max.sessions", "max.sessions.label", maxSessions, formLayout);
		
		FormLayoutContainer buttonsLayout = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsLayout);
		saveLink = uifactory.addFormLink("save", "save", null, buttonsLayout, Link.BUTTON);

		uifactory.addSpacerElement("spacer-1", formLayout, false);
		
		nbrSessionsEl = uifactory.addIntegerElement("nbr.session", "nbr.session.label", 0, formLayout);
		FormLayoutContainer buttonsLayout2 = FormLayoutContainer.createButtonLayout("buttons2", getTranslator());
		formLayout.add(buttonsLayout2);
		invalidateOldSessionLink = uifactory.addFormLink("kill.old", "oldest.session.button", null, buttonsLayout2, Link.BUTTON);
		invalidateAllSessionLink = uifactory.addFormLink("kill.all", "session.admin.invalidate.all.link", null, buttonsLayout2, Link.BUTTON);

		uifactory.addSpacerElement("spacer-2", formLayout, false);
		
		FormLayoutContainer buttonsLayout3 = FormLayoutContainer.createButtonLayout("buttons3", getTranslator());
		formLayout.add(buttonsLayout3);
		blockLoginLink = uifactory.addFormLink("block.login.all", "session.admin.block.login.link", null, buttonsLayout3, Link.BUTTON);
		allowLoginLink = uifactory.addFormLink("allow.login.all", "session.admin.allow.login.link", null, buttonsLayout3, Link.BUTTON);
		updateLoginBlock();
		
		@SuppressWarnings("deprecation")
		boolean clusterMode = CoordinatorManager.getInstance().getCoordinator().isClusterMode();
		if (clusterMode) {
			rejectDMZRequestsLink = uifactory.addFormLink("block.dmz.node", "session.admin.reject.dmz.requests.link", null, buttonsLayout3, Link.BUTTON);
			allowDMZRequestsLink = uifactory.addFormLink("allow.dmz.node", "session.admin.allow.dmz.requests.link", null, buttonsLayout3, Link.BUTTON);
			updateDmzBlock();
		}
	}
	
	private void updateDmzBlock() {
		rejectDMZRequestsLink.setVisible(!AuthHelper.isRejectDMZRequests());
		allowDMZRequestsLink.setVisible(AuthHelper.isRejectDMZRequests());
	}
	
	private void updateLoginBlock() {
		blockLoginLink.setVisible(!AdminModule.isLoginBlocked());
		allowLoginLink.setVisible(AdminModule.isLoginBlocked());
	}
	
	@Override
	protected void doDispose() {
		//empty		
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == saveLink) {
			int sessionTimeout = sessionTimeoutEl.getIntValue();
			sessionModule.setSessionTimeout(sessionTimeout);
			int sessionTimeoutAuth = sessionTimeoutAuthEl.getIntValue();
			sessionModule.setSessionTimeoutAuthenticated(sessionTimeoutAuth);
			int maxSessions = maxSessionsEl.getIntValue();
			AdminModule.setMaxSessions(maxSessions);
			sessionManager.setGlobalSessionTimeout(sessionTimeoutAuth);
		} else if(source == invalidateOldSessionLink) {
			int nbrSessions = nbrSessionsEl.getIntValue();
			int nbrOfInvalidatedSessions = sessionManager.invalidateOldestSessions(nbrSessions);
			showInfo("invalidate.session.done", Integer.toString(nbrOfInvalidatedSessions));
		} else if(source == invalidateAllSessionLink) {
			invalidateAllConfirmController = activateYesNoDialog(ureq, null, translate("invalidate.all.sure"), invalidateAllConfirmController);
		} else if(source == blockLoginLink) {	
			blockLoginConfirmController = activateYesNoDialog(ureq, null, translate("block.login.sure"), blockLoginConfirmController);
		} else if(source == allowLoginLink) {
			AdminModule.setLoginBlocked(false);
			updateLoginBlock();
		} else if(source == rejectDMZRequestsLink) {
			rejectDMZRequestsConfirmController = activateYesNoDialog(ureq, null, translate("reject.dmz.requests.sure"), rejectDMZRequestsConfirmController);
		} else if(source == allowDMZRequestsLink) {
			AdminModule.setRejectDMZRequests(false);
			updateDmzBlock();
			showInfo("allow.dmz.requests.done");
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == invalidateAllConfirmController) {
			if (DialogBoxUIFactory.isYesEvent(event)) { 
				int nbrOfInvalidatedSessions = sessionManager.invalidateAllSessions();	
				showInfo("invalidate.session.done", Integer.toString(nbrOfInvalidatedSessions));
			}
		} else if (source == blockLoginConfirmController) {
			if (DialogBoxUIFactory.isYesEvent(event)) { 
				AdminModule.setLoginBlocked(true);
				showInfo("block.login.done");
			}
			updateLoginBlock();
		} else if (source == rejectDMZRequestsConfirmController) {
			if (DialogBoxUIFactory.isYesEvent(event)) { 
				AdminModule.setRejectDMZRequests(true);
				showInfo("reject.dmz.requests.done");
			}
			updateDmzBlock();
		}
	}
}
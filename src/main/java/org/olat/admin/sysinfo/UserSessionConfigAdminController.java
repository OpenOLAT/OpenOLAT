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

import org.apache.commons.lang.math.NumberUtils;
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
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
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

	private CloseableModalController cmc;
	private DialogBoxController invalidateAllConfirmController;
	private DialogBoxController rejectDMZRequestsConfirmController;
	private BlockLoginConfirmationController blockLoginConfirmController;
	
	private FormLink saveLink, invalidateOldSessionLink, invalidateAllSessionLink;
	private FormLink allowLoginLink, blockLoginLink;
	private FormLink rejectDMZRequestsLink, allowDMZRequestsLink;
	
	private final AdminModule adminModule;
	private final UserSessionModule sessionModule;
	private final UserSessionManager sessionManager;
	
	public UserSessionConfigAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		adminModule = CoreSpringFactory.getImpl(AdminModule.class);
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
		int maxSessions = adminModule.getMaxSessions();
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
		blockLoginLink.setVisible(!adminModule.isLoginBlocked());
		allowLoginLink.setVisible(adminModule.isLoginBlocked());
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == saveLink) {
			// make sure the value is really an integer, user current value as default
			sessionTimeoutEl.setIntValue(NumberUtils.toInt(sessionTimeoutEl.getValue(), sessionModule.getSessionTimeout())); 
			int sessionTimeout = sessionTimeoutEl.getIntValue();
			sessionModule.setSessionTimeout(sessionTimeout);
			// make sure the value is really an integer, user current value as default
			sessionTimeoutAuthEl.setIntValue(NumberUtils.toInt(sessionTimeoutAuthEl.getValue(), sessionModule.getSessionTimeoutAuthenticated())); 
			int sessionTimeoutAuth = sessionTimeoutAuthEl.getIntValue();
			sessionModule.setSessionTimeoutAuthenticated(sessionTimeoutAuth);
			// make sure the value is really an integer, user 0 as default to indicate no limitation
			maxSessionsEl.setIntValue(NumberUtils.toInt(maxSessionsEl.getValue(), 0)); 
			int maxSessions = maxSessionsEl.getIntValue();
			adminModule.setMaxSessions(maxSessions);
			sessionManager.setGlobalSessionTimeout(sessionTimeoutAuth);
		} else if(source == invalidateOldSessionLink) {
			// make sure the value is really an integer, user 0 as default
			nbrSessionsEl.setIntValue(NumberUtils.toInt(nbrSessionsEl.getValue(), 0)); 
			int nbrSessions = nbrSessionsEl.getIntValue();
			int nbrOfInvalidatedSessions = sessionManager.invalidateOldestSessions(nbrSessions);
			showInfo("invalidate.session.done", Integer.toString(nbrOfInvalidatedSessions));
		} else if(source == invalidateAllSessionLink) {
			invalidateAllConfirmController = activateYesNoDialog(ureq, null, translate("invalidate.all.sure"), invalidateAllConfirmController);
		} else if(source == blockLoginLink) {	
			doConfirmLoginBlocked(ureq);
		} else if(source == allowLoginLink) {
			adminModule.setLoginBlocked(false, true);
			updateLoginBlock();
		} else if(source == rejectDMZRequestsLink) {
			rejectDMZRequestsConfirmController = activateYesNoDialog(ureq, null, translate("reject.dmz.requests.sure"), rejectDMZRequestsConfirmController);
		} else if(source == allowDMZRequestsLink) {
			adminModule.setRejectDMZRequests(false);
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
			if (event == Event.DONE_EVENT) { 
				boolean persist = blockLoginConfirmController.isPersist();
				adminModule.setLoginBlocked(true, persist);
				showInfo("block.login.done");
			}
			cmc.deactivate();
			updateLoginBlock();
		} else if (source == rejectDMZRequestsConfirmController) {
			if (DialogBoxUIFactory.isYesEvent(event)) { 
				adminModule.setRejectDMZRequests(true);
				showInfo("reject.dmz.requests.done");
			}
			updateDmzBlock();
		} else if(source == cmc) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(blockLoginConfirmController);
		removeAsListenerAndDispose(cmc);
		blockLoginConfirmController = null;
		cmc = null;
	}
	
	private void doConfirmLoginBlocked(UserRequest ureq) {
		blockLoginConfirmController = new BlockLoginConfirmationController(ureq, getWindowControl());
		listenTo(blockLoginConfirmController);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), blockLoginConfirmController.getInitialComponent(), true, translate("block.login.title"));
		cmc.activate();
		listenTo(cmc);
	}
}
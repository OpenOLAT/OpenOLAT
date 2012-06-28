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

package org.olat.admin.instantMessaging;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormLinkImpl;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.instantMessaging.AdminUserConnection;
import org.olat.instantMessaging.InstantMessaging;
import org.olat.instantMessaging.InstantMessagingModule;

/**
 *
 * @author gnaegi <www.goodsolutions.ch>
 * @author guido
 * Initial Date:  Aug 2, 2006
 * Description:
 * Instant messaging server administration task within olat
 * 
 */
public class InstantMessagingAdminController extends FormBasicController {
	
	private FormLink doSyncButton;
	private IntegerElement idlePollTime;
	private IntegerElement chatPollTime;
	private FormSubmit submit;
	private FormLinkImpl checkPlugin;
	private FormLinkImpl reconnectAdminUser;
	private FormLink doUserSyncButton;

	public InstantMessagingAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "index");
		//
		initForm(this.flc, this, ureq);
	}
		
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		InstantMessaging im = InstantMessagingModule.getAdapter();
		if (source == doSyncButton) {
			showInfo("imadmin.sync.cmd.dosync.caption");
			boolean allOk = im.synchronizeBusinessGroupsWithIMServer();
			if (!allOk) {
				refreshAndSetConnectionStatus();
				showError("imadmin.sync.failed");
				doSyncButton.setEnabled(false);
			}
		} else if (source == checkPlugin) {
			String ok= im.checkServerPlugin();
			showInfo("imadmin.plugin.version", ok);
		} else if (source == reconnectAdminUser) {
			try {
				im.resetAdminConnection();
			} catch (Exception e) {
				refreshAndSetConnectionStatus();
				getWindowControl().setError("Connection not possible: " + e.getMessage());
				return;
			} 
			refreshAndSetConnectionStatus();
			doSyncButton.setEnabled(true);
			showInfo("imadmin.plugin.admin.connection.done");
		} else if (source == doUserSyncButton) {
			String result = im.synchronizeAllOLATUsers();
			getWindowControl().setWarning(result);
		}
	}
	
	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// nothing to dispose
	}

	
	@Override
	protected void formOK(UserRequest ureq) {
		// TODO Auto-generated method stub
		InstantMessagingModule.setCHAT_POLLTIME(chatPollTime.getIntValue());
		InstantMessagingModule.setIDLE_POLLTIME(idlePollTime.getIntValue());
	}

	private void refreshAndSetConnectionStatus(){
		AdminUserConnection connection = InstantMessagingModule.getAdapter().getAdminUserConnection();
		boolean connectionSuccessfull = (connection!=null && connection.getConnection()!= null && connection.getConnection().isConnected());
		flc.contextPut("IMConnectionStatus", connectionSuccessfull);
	}


	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer mainLayout = FormLayoutContainer.createDefaultFormLayout("mainLayout", getTranslator());
		formLayout.add(mainLayout);
		
		String imServerName = InstantMessagingModule.getAdapter().getConfig().getServername();
		String imAdminUsername = InstantMessagingModule.getAdapter().getConfig().getAdminName();
		String imAdminPw = InstantMessagingModule.getAdapter().getConfig().getAdminPassword();
		
		
		flc.contextPut("IMServerAdminGUI", imServerName);
		flc.contextPut("IMServerAdminUsername", imAdminUsername);
		flc.contextPut("IMServerAdminPw", imAdminPw);
		
		refreshAndSetConnectionStatus();
		
		checkPlugin = new FormLinkImpl("imadmin.plugin.check");
		checkPlugin.setCustomEnabledLinkCSS("b_button");
		formLayout.add(checkPlugin);
		
		reconnectAdminUser = new FormLinkImpl("imadmin.plugin.admin.reconnect");
		reconnectAdminUser.setCustomEnabledLinkCSS("b_button");
		formLayout.add(reconnectAdminUser);

		doSyncButton = new FormLinkImpl("imadmin.sync.cmd.dosync");
		doSyncButton.setCustomEnabledLinkCSS("b_button");
		doSyncButton.setCustomDisabledLinkCSS("b_button b_button_disabled");
		formLayout.add(doSyncButton);
		
		doUserSyncButton = uifactory.addFormLink("sync.all.users", formLayout, Link.BUTTON);
				
		idlePollTime = uifactory.addIntegerElement("idlepolltime", "imadming.idlepolltime", InstantMessagingModule.getIDLE_POLLTIME(), mainLayout);
		idlePollTime.setExampleKey("imadming.idlepolltime.default", new String[]{""+InstantMessagingModule.getAdapter().getConfig().getIdlePolltime()});
		idlePollTime.showExample(true);
		
		chatPollTime = uifactory.addIntegerElement("chatpolltime", "imadming.chatpolltime", InstantMessagingModule.getCHAT_POLLTIME(), mainLayout);
		chatPollTime.setExampleKey("imadming.chatpolltime.default", new String[]{""+InstantMessagingModule.getAdapter().getConfig().getChatPolltime()});
		chatPollTime.showExample(true);
		
		submit = new FormSubmit("subm","submit");
		
		mainLayout.add(idlePollTime);
		mainLayout.add(chatPollTime);
		mainLayout.add(submit);
		
	}

}

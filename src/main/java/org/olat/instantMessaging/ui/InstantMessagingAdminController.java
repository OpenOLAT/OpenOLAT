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

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
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

	private MultipleSelectionElement imEnabledEl;
	private MultipleSelectionElement imEnableGroupEl;
	private MultipleSelectionElement imEnableCourseEl;
	private MultipleSelectionElement imEnablePrivateEl;
	private MultipleSelectionElement imEnableOnlineUsersEl;
	private MultipleSelectionElement imEnableGroupPeersEl;
	
	private static String[] enabledKeys = new String[]{"on"};
	
	private final InstantMessagingModule imModule;

	public InstantMessagingAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "admin");
		
		imModule = CoreSpringFactory.getImpl(InstantMessagingModule.class);
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//enable all
		FormLayoutContainer moduleFlc = FormLayoutContainer.createDefaultFormLayout("flc_module", getTranslator());
		formLayout.add(moduleFlc);
	
		String[] enabledValues = new String[]{ translate("enabled") };
		imEnabledEl = uifactory.addCheckboxesHorizontal("im.module.enabled", moduleFlc, enabledKeys, enabledValues, null);
		imEnabledEl.select(enabledKeys[0], imModule.isEnabled());
		imEnabledEl.addActionListener(listener, FormEvent.ONCHANGE);
		
		//options
		FormLayoutContainer optionsFlc = FormLayoutContainer.createDefaultFormLayout("flc_options", getTranslator());
		formLayout.add(optionsFlc);

		imEnableGroupEl = uifactory.addCheckboxesHorizontal("im.module.enabled.group", optionsFlc, enabledKeys, enabledValues, null);
		imEnableGroupEl.select(enabledKeys[0], imModule.isGroupEnabled());
		imEnableGroupEl.addActionListener(listener, FormEvent.ONCHANGE);
		
		imEnableCourseEl = uifactory.addCheckboxesHorizontal("im.module.enabled.course", optionsFlc, enabledKeys, enabledValues, null);
		imEnableCourseEl.select(enabledKeys[0], imModule.isCourseEnabled());
		imEnableCourseEl.addActionListener(listener, FormEvent.ONCHANGE);
		
		imEnablePrivateEl = uifactory.addCheckboxesHorizontal("im.module.enabled.private", optionsFlc, enabledKeys, enabledValues, null);
		imEnablePrivateEl.select(enabledKeys[0], imModule.isPrivateEnabled());
		imEnablePrivateEl.addActionListener(listener, FormEvent.ONCHANGE);
		
		imEnableOnlineUsersEl = uifactory.addCheckboxesHorizontal("im.module.enabled.onlineusers", optionsFlc, enabledKeys, enabledValues, null);
		imEnableOnlineUsersEl.select(enabledKeys[0], imModule.isOnlineUsersEnabled());
		imEnableOnlineUsersEl.addActionListener(listener, FormEvent.ONCHANGE);
		
		imEnableGroupPeersEl = uifactory.addCheckboxesHorizontal("im.module.enabled.grouppeers", optionsFlc, enabledKeys, enabledValues, null);
		imEnableGroupPeersEl.select(enabledKeys[0], imModule.isGroupPeersEnabled());
		imEnableGroupPeersEl.addActionListener(listener, FormEvent.ONCHANGE);
	}
	
	protected void doDispose() {
		// nothing to dispose
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == imEnabledEl) {
			imModule.setEnabled(imEnabledEl.isSelected(0));
		} else if(source == imEnableGroupEl) {
			imModule.setGroupEnabled(imEnableGroupEl.isSelected(0));
		} else if(source == imEnableCourseEl) {
			imModule.setCourseEnabled(imEnableCourseEl.isSelected(0));
		} else if(source == imEnablePrivateEl) {
			imModule.setPrivateEnabled(imEnablePrivateEl.isSelected(0));
		} else if(source == imEnableOnlineUsersEl) {
			imModule.setOnlineUsersEnabled(imEnableOnlineUsersEl.isSelected(0));
		} else if(source == imEnableGroupPeersEl) {
			imModule.setGroupPeersEnabled(imEnableGroupPeersEl.isSelected(0));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
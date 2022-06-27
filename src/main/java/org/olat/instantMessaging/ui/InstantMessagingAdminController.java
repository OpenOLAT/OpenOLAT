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
	private MultipleSelectionElement imEnableGroupAnonymEl;
	private MultipleSelectionElement imEnableGroupAnonymDefaultEl;
	private MultipleSelectionElement imEnableCourseEl;
	private MultipleSelectionElement imEnableCourseAnonymEl;
	private MultipleSelectionElement imEnableCourseAnonymDefaultEl;
	private MultipleSelectionElement imEnablePrivateEl;
	private MultipleSelectionElement imEnableGroupPeersEl;
	private MultipleSelectionElement imEnableOnlineStatusEl;
	
	private static String[] enabledKeys = new String[]{"on"};
	
	private final InstantMessagingModule imModule;

	public InstantMessagingAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		
		imModule = CoreSpringFactory.getImpl(InstantMessagingModule.class);
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//enable all
		FormLayoutContainer moduleFlc = FormLayoutContainer.createDefaultFormLayout("flc_module", getTranslator());
		moduleFlc.setFormTitle(translate("im.module.enable.title"));
		moduleFlc.setFormContextHelp("manual_admin/administration/Instant_Messaging/");
		formLayout.add(moduleFlc);
	
		String[] enabledValues = new String[]{ translate("enabled") };
		imEnabledEl = uifactory.addCheckboxesHorizontal("im.module.enabled", moduleFlc, enabledKeys, enabledValues);
		imEnabledEl.select(enabledKeys[0], imModule.isEnabled());
		imEnabledEl.addActionListener(FormEvent.ONCHANGE);
		
		//options
		FormLayoutContainer chatOptionsFlc = FormLayoutContainer.createDefaultFormLayout("flc_chatOptions", getTranslator());
		chatOptionsFlc.setFormTitle(translate("im.module.options.chat.title"));
		formLayout.add(chatOptionsFlc);

		imEnableGroupEl = uifactory.addCheckboxesHorizontal("im.module.enabled.group", chatOptionsFlc, enabledKeys, enabledValues);
		imEnableGroupEl.select(enabledKeys[0], imModule.isGroupEnabled());
		imEnableGroupEl.addActionListener(FormEvent.ONCHANGE);

		imEnableGroupAnonymEl = uifactory.addCheckboxesHorizontal("im.module.enabled.group.anonym", chatOptionsFlc, enabledKeys, enabledValues);
		imEnableGroupAnonymEl.select(enabledKeys[0], imModule.isGroupAnonymEnabled());
		imEnableGroupAnonymEl.addActionListener(FormEvent.ONCHANGE);

		imEnableGroupAnonymDefaultEl = uifactory.addCheckboxesHorizontal("im.module.enabled.group.anonym.default", chatOptionsFlc, enabledKeys, enabledValues);
		imEnableGroupAnonymDefaultEl.select(enabledKeys[0], imModule.isGroupAnonymDefaultEnabled());
		imEnableGroupAnonymDefaultEl.addActionListener(FormEvent.ONCHANGE);

		uifactory.addSpacerElement("spacer", chatOptionsFlc, true);
		
		imEnableCourseEl = uifactory.addCheckboxesHorizontal("im.module.enabled.course", chatOptionsFlc, enabledKeys, enabledValues);
		imEnableCourseEl.select(enabledKeys[0], imModule.isCourseEnabled());
		imEnableCourseEl.addActionListener(FormEvent.ONCHANGE);

		imEnableCourseAnonymEl = uifactory.addCheckboxesHorizontal("im.module.enabled.course.anonym", chatOptionsFlc, enabledKeys, enabledValues);
		imEnableCourseAnonymEl.select(enabledKeys[0], imModule.isCourseAnonymEnabled());
		imEnableCourseAnonymEl.addActionListener(FormEvent.ONCHANGE);

		imEnableCourseAnonymDefaultEl = uifactory.addCheckboxesHorizontal("im.module.enabled.course.anonym.default", chatOptionsFlc, enabledKeys, enabledValues);
		imEnableCourseAnonymDefaultEl.select(enabledKeys[0], imModule.isCourseAnonymDefaultEnabled());
		imEnableCourseAnonymDefaultEl.addActionListener(FormEvent.ONCHANGE);

		FormLayoutContainer messageOptionsFlc = FormLayoutContainer.createDefaultFormLayout("flc_messageOptions", getTranslator());
		messageOptionsFlc.setFormTitle(translate("im.module.options.message.title"));
		formLayout.add(messageOptionsFlc);
		
 		imEnablePrivateEl = uifactory.addCheckboxesHorizontal("im.module.enabled.private", messageOptionsFlc, enabledKeys, enabledValues);
		imEnablePrivateEl.select(enabledKeys[0], imModule.isPrivateEnabled());
		imEnablePrivateEl.addActionListener(FormEvent.ONCHANGE);
		
		imEnableGroupPeersEl = uifactory.addCheckboxesHorizontal("im.module.enabled.grouppeers", messageOptionsFlc, enabledKeys, enabledValues);
		imEnableGroupPeersEl.select(enabledKeys[0], imModule.isGroupPeersEnabled());
		imEnableGroupPeersEl.addActionListener(FormEvent.ONCHANGE);
		
		imEnableOnlineStatusEl = uifactory.addCheckboxesHorizontal("im.module.enabled.onlineStatus", messageOptionsFlc, enabledKeys, enabledValues);
		imEnableOnlineStatusEl.select(enabledKeys[0], imModule.isOnlineStatusEnabled());
		imEnableOnlineStatusEl.addActionListener(FormEvent.ONCHANGE);
		
		// update GUI dependencies
		updateDependencies();
	}
	
	private void updateDependencies() {
		// disable all options when the module is turned off
		if (imEnabledEl.isSelected(0)) {
			imEnableGroupEl.setEnabled(true);
			imEnableGroupAnonymEl.setEnabled(true);
			if (imEnableGroupAnonymEl.isSelected(0)) { 
				imEnableGroupAnonymDefaultEl.setEnabled(true);
			} else {
				imEnableGroupAnonymDefaultEl.setEnabled(false);				
			}
			imEnableCourseEl.setEnabled(true);
			imEnableCourseAnonymEl.setEnabled(true);
			if (imEnableCourseAnonymEl.isSelected(0)) { 
				imEnableCourseAnonymDefaultEl.setEnabled(true);
			} else {
				imEnableCourseAnonymDefaultEl.setEnabled(false);
			}
			imEnablePrivateEl.setEnabled(true);
			imEnableGroupPeersEl.setEnabled(true);
			imEnableOnlineStatusEl.setEnabled(true);
		} else {
			// everything is disabled
			imEnableGroupEl.setEnabled(false);
			imEnableGroupAnonymEl.setEnabled(false);
			imEnableGroupAnonymDefaultEl.setEnabled(false);
			imEnableCourseEl.setEnabled(false);
			imEnableCourseAnonymEl.setEnabled(false);
			imEnableCourseAnonymDefaultEl.setEnabled(false);
			imEnablePrivateEl.setEnabled(false);
			imEnableGroupPeersEl.setEnabled(false);
			imEnableOnlineStatusEl.setEnabled(false);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == imEnabledEl) {
			imModule.setEnabled(imEnabledEl.isSelected(0));
		} else if(source == imEnableGroupEl) {
			imModule.setGroupEnabled(imEnableGroupEl.isSelected(0));
		} else if(source == imEnableGroupAnonymEl) {
			imModule.setGroupAnonymEnabled(imEnableGroupAnonymEl.isSelected(0));
		} else if(source == imEnableGroupAnonymDefaultEl) {
			imModule.setGroupAnonymDefaultEnabled(imEnableGroupAnonymDefaultEl.isSelected(0));
		} else if(source == imEnableCourseEl) {
			imModule.setCourseEnabled(imEnableCourseEl.isSelected(0));
		} else if(source == imEnableCourseAnonymEl) {
			imModule.setCourseAnonymEnabled(imEnableCourseAnonymEl.isSelected(0));
		} else if(source == imEnableCourseAnonymDefaultEl) {
			imModule.setCourseAnonymDefaultEnabled(imEnableCourseAnonymDefaultEl.isSelected(0));
		} else if(source == imEnablePrivateEl) {
			imModule.setPrivateEnabled(imEnablePrivateEl.isSelected(0));
		} else if(source == imEnableGroupPeersEl) {
			imModule.setGroupPeersEnabled(imEnableGroupPeersEl.isSelected(0));
		} else if(source == imEnableOnlineStatusEl) {
			imModule.setOnlineStatusEnabled(imEnableOnlineStatusEl.isSelected(0));
		}
		// update GUI dependencies
		updateDependencies();
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
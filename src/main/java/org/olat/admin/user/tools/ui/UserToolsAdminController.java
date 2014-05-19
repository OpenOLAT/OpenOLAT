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
package org.olat.admin.user.tools.ui;

import java.util.List;
import java.util.Set;

import org.olat.admin.user.tools.UserTool;
import org.olat.admin.user.tools.UserToolsModule;
import org.olat.core.extensions.ExtManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserToolsAdminController extends FormBasicController {
	
	private final List<UserTool> userTools;
	
	private MultipleSelectionElement availableEl, presetEl;
	
	@Autowired
	private ExtManager extManager;
	@Autowired
	private UserToolsModule userToolsModule;
	
	public UserToolsAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		userTools = userToolsModule.getAllUserTools(ureq);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("usertools");
		setFormDescription("usertools.description");
		setFormContextHelp("org.olat.admin.user.tools.ui", "admin-usertools.html", "help.hover.adminusertools");

		int numOfTools = userTools.size();
		String[] toolKeys = new String[numOfTools];
		String[] toolValues = new String[numOfTools];
		for(int i=0; i<numOfTools; i++) {
			UserTool userTool = userTools.get(i);
			toolKeys[i] = userTool.getKey();
			toolValues[i] = userTool.getLabel();
		}

		availableEl = uifactory.addCheckboxesVertical("available.tools", "available.tools", formLayout, toolKeys, toolValues, null, 1);
		availableEl.addActionListener(FormEvent.ONCHANGE);
		
		if(!userToolsModule.isUserToolsDisabled()) {
			Set<String> aToolSet = userToolsModule.getAvailableUserToolSet();
			if(aToolSet.isEmpty()) {
				for(String toolKey:toolKeys) {
					availableEl.select(toolKey, true);
				}
			} else {
				for(String toolKey:toolKeys) {
					if(aToolSet.contains(toolKey)) {
						availableEl.select(toolKey, true);
					}
				}
			}
		}

		presetEl = uifactory.addCheckboxesVertical("preset.tools", "preset.tools", formLayout, toolKeys, toolValues, null, 1);
		presetEl.addActionListener(FormEvent.ONCHANGE);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(availableEl == source) {
			//update defaultSet;
			doPersist();
		} else if(presetEl == source) {
			doPersist();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doPersist() {
		String aTools;
		if(availableEl.isAtLeastSelected(1)) {
			StringBuilder sb = new StringBuilder();
			for(String selectedKey:availableEl.getSelectedKeys()) {
				if(sb.length() > 0) sb.append(",");
				sb.append(selectedKey);
			}
			aTools = sb.toString();
		} else {
			aTools = "none";
		}
		
		String preset;
		if(presetEl.isAtLeastSelected(1)) {
			StringBuilder sb = new StringBuilder();
			for(String selectedKey:presetEl.getSelectedKeys()) {
				if(sb.length() > 0) sb.append(",");
				sb.append(selectedKey);
			}
			preset = sb.toString();
		} else {
			preset = "none";
		}
		
		userToolsModule.setAvailableUserTools(aTools);
		userToolsModule.setDefaultPresetOfUserTools(preset);
	}
}

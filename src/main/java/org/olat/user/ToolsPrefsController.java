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
package org.olat.user;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.olat.admin.user.tools.UserTool;
import org.olat.admin.user.tools.UserToolsModule;
import org.olat.core.extensions.ExtManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowManager;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.prefs.PreferencesFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16.05.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ToolsPrefsController extends FormBasicController {
	
	private final boolean enabled;
	private final Preferences prefs;
	private final Identity tobeChangedIdentity;
	private final List<UserTool> userTools;
	
	private MultipleSelectionElement presetEl;
	
	@Autowired
	private ExtManager extManager;
	@Autowired
	private UserToolsModule userToolsModule;
	
	public ToolsPrefsController(UserRequest ureq, WindowControl wControl, Identity changeableIdentity) {
		super(ureq, wControl);
		tobeChangedIdentity = changeableIdentity;
		if (ureq.getIdentity().equalsByPersistableKey(tobeChangedIdentity)) {
			prefs = ureq.getUserSession().getGuiPreferences();
		} else {
			prefs = PreferencesFactory.getInstance().getPreferencesFor(tobeChangedIdentity, false);			
		}
		enabled = !userToolsModule.isUserToolsDisabled();
		
		if(enabled) {
			Set<String> aToolSet = userToolsModule.getAvailableUserToolSet();
			userTools = userToolsModule.getAllUserTools(ureq);
			if(aToolSet.size() > 0) {
				for(Iterator<UserTool> it=userTools.iterator(); it.hasNext(); ) {
					if(!aToolSet.contains(it.next().getKey())) {
						it.remove();
					}
				}
			}
		} else {
			userTools = Collections.emptyList();
		}
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("usertools.title");
		setFormDescription("usertools.descr");
		setFormContextHelp("org.olat.user", "usertools-prefs.html", "help.hover.usertools");

		String[] toolKeys;
		String[] toolValues;
		if(enabled) {
			int numOfTools = userTools.size();
			toolKeys = new String[numOfTools];
			toolValues = new String[numOfTools];
			for(int i=0; i<numOfTools; i++) {
				UserTool userTool = userTools.get(i);
				toolKeys[i] = userTool.getKey();
				toolValues[i] = userTool.getLabel();
			}
		} else {
			toolKeys = new String[0];
			toolValues = new String[0];
		}
		presetEl = uifactory.addCheckboxesVertical("usertools.set", "usertools.set", formLayout, toolKeys, toolValues, null, 1);
		presetEl.addActionListener(FormEvent.ONCHANGE);
		presetEl.setEnabled(enabled);
		
		String selectedTools = (String)prefs.get(WindowManager.class, "user-tools");
		if(!StringHelper.containsNonWhitespace(selectedTools)) {
			selectedTools = userToolsModule.getDefaultPresetOfUserTools();
		}
		if(StringHelper.containsNonWhitespace(selectedTools)) {
			String[] selectedToolArr = selectedTools.split(",");
			for(String toolKey:toolKeys) {
				for(String selectedTool:selectedToolArr) {
					if(toolKey.equals(selectedTool)) {
						presetEl.select(toolKey, true);
					}
				}
			}
		}
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(presetEl == source) {
			doSave();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doSave() {
		StringBuilder sb = new StringBuilder();
		if(presetEl.isAtLeastSelected(1)) {
			for(String selectedKey:presetEl.getSelectedKeys()) {
				if(sb.length() > 1) sb.append(",");
				sb.append(selectedKey);
			}
		} else {
			sb.append("none");
		}
		prefs.put(WindowManager.class, "user-tools", sb.toString());
		prefs.save();
	}
}

/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.sharepoint.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.modules.sharepoint.SharePointModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 mai 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SharePointConfigurationController extends FormBasicController {
	
	private FormToggle moduleEnabledEl;
	
	private TextElement excludeSitesAndDrivesEl;
	private TextElement excludeLabelsEl;
	
	@Autowired
	private SharePointModule sharePointModule;
	
	public SharePointConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
		updateUI();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("sharepoint.title");
		setFormInfo("sharepoint.intro");
		
		moduleEnabledEl = uifactory.addToggleButton("sharepoint.enable", "sharepoint.enable", translate("on"), translate("off"), formLayout);
		moduleEnabledEl.toggle(sharePointModule.isEnabled());
		moduleEnabledEl.addActionListener(FormEvent.ONCHANGE);
		
		String exclusionSitesAndDrives = toTextArea(sharePointModule.getExcludeSitesAndDrives());
		excludeSitesAndDrivesEl = uifactory.addTextAreaElement("exclusion.sites", "exclusion.sites", 4000, 4, 60, false, false, false, exclusionSitesAndDrives, formLayout);
		
		String exclusionLabels = toTextArea(sharePointModule.getExcludeLabels());
		excludeLabelsEl = uifactory.addTextAreaElement("exclusion.labels", "exclusion.labels", 4000, 4, 60, false, false, false, exclusionLabels, formLayout);
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	private void updateUI() {
		boolean enabled = moduleEnabledEl.isOn();
		excludeSitesAndDrivesEl.setVisible(enabled);
		excludeLabelsEl.setVisible(enabled);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(moduleEnabledEl == source) {
			updateUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = moduleEnabledEl.isOn();
		sharePointModule.setEnabled(enabled);
		
		if(enabled) {
			List<String> excludeSitesAndDrives = toList(excludeSitesAndDrivesEl.getValue());
			sharePointModule.setExcludeSitesAndDrives(excludeSitesAndDrives);
			
			List<String> exclusionLabels = toList(excludeLabelsEl.getValue());
			sharePointModule.setExcludeLabels(exclusionLabels);
		}
	}
	
	private List<String> toList(String val) {
		if(StringHelper.containsNonWhitespace(val)) {
			String[] arrayString = val.split("[\r\n]");
			List<String> list = new ArrayList<>();
			for(String string:arrayString) {
				if(StringHelper.containsNonWhitespace(string)) {
					list.add(string);
				}
			}
			return list;
		}
		return List.of();
	}
	
	private String toTextArea(List<String> list) {
		if(list == null || list.isEmpty()) return "";
		return String.join("\n", list);
	}
}

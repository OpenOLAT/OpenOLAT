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
package org.olat.course.assessment.ui.inspection;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentInspectionConfiguration;
import org.olat.course.assessment.AssessmentInspectionService;
import org.olat.course.assessment.ui.mode.AssessmentModeEditAccessController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 15 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentInspectionConfigurationEditAccessController extends FormBasicController {
	
	private FormToggle ipsEl;
	private TextElement ipListEl;
	
	private AssessmentInspectionConfiguration configuration;
	
	@Autowired
	private AssessmentInspectionService inspectionService;
	
	public AssessmentInspectionConfigurationEditAccessController(UserRequest ureq, WindowControl wControl,
			AssessmentInspectionConfiguration configuration) {
		super(ureq, wControl, Util.createPackageTranslator(AssessmentModeEditAccessController.class, ureq.getLocale()));
		this.configuration = configuration;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//ips
		ipsEl = uifactory.addToggleButton("ips", "mode.ips", translate("on"), translate("off"), formLayout);
		ipsEl.addActionListener(FormEvent.ONCHANGE);
		ipsEl.toggle(configuration != null && configuration.isRestrictAccessIps());

		String ipList = configuration == null ? null : configuration.getIpList();
		ipListEl = uifactory.addTextAreaElement("mode.ips.list", "mode.ips.list", 16000, 4, 60, false, false, ipList, formLayout);
		ipListEl.setMaxLength(16000);
		ipListEl.setVisible(ipsEl.isOn());
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == ipsEl) {
			ipListEl.setVisible(ipsEl.isOn());
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		save();
		fireEvent(ureq, Event.CHANGED_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void save() {
		if(configuration.getKey() != null) {
			configuration = inspectionService.getConfigurationById(configuration.getKey());
		}
		
		boolean restrictIps = ipsEl.isOn();
		configuration.setRestrictAccessIps(restrictIps);
		if(restrictIps) {
			configuration.setIpList(ipListEl.getValue());
		} else {
			configuration.setIpList(null);
		}

		configuration = inspectionService.saveConfiguration(configuration);
	}
}

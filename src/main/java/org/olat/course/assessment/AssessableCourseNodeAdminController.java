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
package org.olat.course.assessment;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.course.CourseModule;
import org.olat.course.config.CourseConfig;
import org.olat.course.nodeaccess.NodeAccessProviderIdentifier;
import org.olat.course.nodeaccess.NodeAccessService;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * 
 * Initial date: 08.05.2017<br>
 * @author fkiefer
 *
 */
public class AssessableCourseNodeAdminController extends FormBasicController {
	
	private static final String[] onKeys = new String[]{ "on" };
	private final String[] onValues;
	
	private MultipleSelectionElement infoBoxEl;
	private MultipleSelectionElement changeLogEl;
	private MultipleSelectionElement disclaimerEnabledEl;
	
	private SingleSelection courseDefaultTypeEl;
	
	private FormLink inviteeLink;

	@Autowired
	private CourseModule courseModule;
	@Autowired
	private NodeAccessService nodeAccessService;
	
	public AssessableCourseNodeAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		
		onValues = new String[]{ translate("on") };

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		// General Course Settings
		FormLayoutContainer courseSettings = FormLayoutContainer.createDefaultFormLayout("courseSettings", getTranslator());
		courseSettings.setFormTitle(translate("admin.course.type.settings"));
		courseSettings.setRootForm(mainForm);
		
		SelectionValues nodeAccessKV = new SelectionValues();
		StringBuilder helpText = new StringBuilder(1024);
		for (NodeAccessProviderIdentifier identifier : nodeAccessService.getNodeAccessProviderIdentifer()) {
			String title = identifier.getDisplayName(getLocale());
			nodeAccessKV.add(SelectionValues.entry(identifier.getType(), title));
			helpText.append("<strong>").append(title).append("</strong><br>").append(identifier.getToolTipHelpText(getLocale())).append("<br><br>");
		}
		
		courseDefaultTypeEl = uifactory.addRadiosVertical("course.default.type", courseSettings, nodeAccessKV.keys(), nodeAccessKV.values());
		courseDefaultTypeEl.setHelpText(helpText.toString());
		courseDefaultTypeEl.addActionListener(FormEvent.ONCHANGE);
		
		String defaultCourseType = courseModule.getCourseTypeDefault();
		if (!StringHelper.containsNonWhitespace(defaultCourseType) || !nodeAccessKV.containsKey(defaultCourseType)) {
			defaultCourseType = CourseConfig.NODE_ACCESS_TYPE_DEFAULT;
		}
		courseDefaultTypeEl.select(defaultCourseType, true);
		
		formLayout.add(courseSettings);
		
		// Assessable course node settings
		FormLayoutContainer assessableCourseNodeSettings = FormLayoutContainer.createDefaultFormLayout("assessableCourseNodeSettings", getTranslator());
		assessableCourseNodeSettings.setRootForm(mainForm);
		assessableCourseNodeSettings.setFormTitle(translate("admin.assessable.coursenode"));
		
		infoBoxEl = uifactory.addCheckboxesHorizontal("admin.info.box", assessableCourseNodeSettings, onKeys, onValues);
		infoBoxEl.addActionListener(FormEvent.ONCHANGE);
		if (courseModule.isDisplayInfoBox()) {
			infoBoxEl.select(onKeys[0], true);
		}
		
		changeLogEl = uifactory.addCheckboxesHorizontal("admin.user.changelog", assessableCourseNodeSettings, onKeys, onValues);
		changeLogEl.addActionListener(FormEvent.ONCHANGE);
		if (courseModule.isDisplayChangeLog()) {
			changeLogEl.select(onKeys[0], true);
		}
		
		disclaimerEnabledEl = uifactory.addCheckboxesHorizontal("admin.disclaimer.enabled", assessableCourseNodeSettings, onKeys, onValues);
		disclaimerEnabledEl.addActionListener(FormEvent.ONCHANGE);
		if (courseModule.isDisclaimerEnabled()) {
			disclaimerEnabledEl.select(onKeys[0], true);
		}
		
		formLayout.add(assessableCourseNodeSettings);
		
		// Links to other settings
		FormLayoutContainer otherSettings = FormLayoutContainer.createDefaultFormLayout("otherSettings", getTranslator());
		otherSettings.setFormTitle(translate("admin.assessable.other.settings"));
		formLayout.add(otherSettings);
		inviteeLink = uifactory.addFormLink("course.login", "course.login.invitee", "course.login", otherSettings, Link.LINK);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == infoBoxEl) {
			courseModule.setDisplayInfoBox(infoBoxEl.isSelected(0));
		} else if (source == changeLogEl) {
			courseModule.setDisplayChangeLog(changeLogEl.isSelected(0));
		} else if (source == disclaimerEnabledEl) {
			courseModule.setDisclaimerEnabled(disclaimerEnabledEl.isSelected(0));
		} else if (source == courseDefaultTypeEl) {
			courseModule.setCourseTypeDefault(courseDefaultTypeEl.getSelectedKey());
		} else if(inviteeLink == source) {
			String invitationSettingsPath = "[AdminSite:0][loginadmin:0][Invitation:0]";
			NewControllerFactory.getInstance().launch(invitationSettingsPath, ureq, getWindowControl());
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// nothing
	}

}

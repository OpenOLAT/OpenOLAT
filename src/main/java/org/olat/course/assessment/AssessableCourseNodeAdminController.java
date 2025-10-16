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
import org.olat.core.gui.components.util.SelectionValues.SelectionValue;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.course.CourseModule;
import org.olat.repository.RepositoryService;
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

	private SingleSelection courseExecEl;
	private SingleSelection designEl;
	private MultipleSelectionElement infoBoxEl;
	private MultipleSelectionElement changeLogEl;
	private MultipleSelectionElement disclaimerEnabledEl;
	private MultipleSelectionElement efficiencyStatementEnabledEl;
	
	private FormLink inviteeLink;

	@Autowired
	private CourseModule courseModule;
	
	public AssessableCourseNodeAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		
		onValues = new String[]{ translate("on") };

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer courseExecCont = FormLayoutContainer.createDefaultFormLayout("courseExec", getTranslator());
		courseExecCont.setRootForm(mainForm);
		courseExecCont.setFormTitle(translate("course.execution.period"));
		courseExecCont.setFormContextHelp("manual_admin/administration/Modules_Course/");
		formLayout.add(courseExecCont);
		initCourseExecPeriodOptions(courseExecCont);

		FormLayoutContainer courseSettings = FormLayoutContainer.createVerticalFormLayout("courseSettings", getTranslator());
		courseSettings.setFormTitle(translate("admin.course.design.settings"));
		courseSettings.setRootForm(mainForm);
		formLayout.add(courseSettings);
		
		SelectionValues designKV = new SelectionValues();
		designKV.add(new SelectionValue(CourseModule.COURSE_TYPE_PATH, translate("course.design.path"), translate("course.design.path.desc"),"o_course_design_path_icon", null, true));
		designKV.add(new SelectionValue(CourseModule.COURSE_TYPE_PROGRESS, translate("course.design.progress"), translate("course.design.progress.desc"),"o_course_design_progress_icon", null, true));
		designKV.add(new SelectionValue(CourseModule.COURSE_TYPE_CLASSIC, translate("course.design.classic"), translate("course.design.classic.desc"),"o_course_design_classic_icon", null, true));
		designEl = uifactory.addCardSingleSelectHorizontal("course.design", "admin.course.design.default", formLayout, designKV);
		designEl.setElementCssClass("o_course_design");
		designEl.addActionListener(FormEvent.ONCHANGE);
		String defaultCourseType = courseModule.getCourseTypeDefault();
		if (!designEl.containsKey(defaultCourseType)) {
			defaultCourseType = CourseModule.COURSE_TYPE_PATH;
		}
		designEl.select(defaultCourseType, true);
		
		
		// Assessable course node settings
		FormLayoutContainer assessableCourseNodeSettings = FormLayoutContainer.createDefaultFormLayout("assessableCourseNodeSettings", getTranslator());
		assessableCourseNodeSettings.setRootForm(mainForm);
		assessableCourseNodeSettings.setFormTitle(translate("admin.assessable.coursenode"));
		assessableCourseNodeSettings.setElementCssClass("o_block_top");
		
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

		efficiencyStatementEnabledEl = uifactory.addCheckboxesHorizontal("admin.efficiency.statement.enabled", assessableCourseNodeSettings, onKeys, onValues);
		efficiencyStatementEnabledEl.addActionListener(FormEvent.ONCHANGE);
		if (courseModule.isEfficiencyStatementEnabled()) {
			efficiencyStatementEnabledEl.select(onKeys[0], true);
		}
		
		formLayout.add(assessableCourseNodeSettings);
		
		// Links to other settings
		FormLayoutContainer otherSettings = FormLayoutContainer.createDefaultFormLayout("otherSettings", getTranslator());
		otherSettings.setFormTitle(translate("admin.assessable.other.settings"));
		formLayout.add(otherSettings);
		inviteeLink = uifactory.addFormLink("course.login", "course.login.invitee", "course.login", otherSettings, Link.LINK);
	}

	private void initCourseExecPeriodOptions(FormLayoutContainer formLayoutContainer) {
		String[] dateKeys = new String[]{ "none", "private", "public"};
		String[] dateValues = new String[] {
				translate("cif.dates.none"),
				translate("cif.dates.private"),
				translate("cif.dates.public")
		};

		courseExecEl = uifactory.addRadiosVertical("cif.dates", "cif.dates", formLayoutContainer, dateKeys, dateValues);
		courseExecEl.setHelpText(translate("cif.dates.help"));
		courseExecEl.select(courseModule.getCourseExecutionDefault(), true);
		courseExecEl.addActionListener(FormEvent.ONCHANGE);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == infoBoxEl) {
			courseModule.setDisplayInfoBox(infoBoxEl.isSelected(0));
		} else if (source == changeLogEl) {
			courseModule.setDisplayChangeLog(changeLogEl.isSelected(0));
		} else if (source == disclaimerEnabledEl) {
			courseModule.setDisclaimerEnabled(disclaimerEnabledEl.isSelected(0));
		} else if (source == efficiencyStatementEnabledEl) {
			courseModule.setEfficiencyStatementEnabled(efficiencyStatementEnabledEl.isSelected(0));
		} else if (source == designEl) {
			courseModule.setCourseTypeDefault(designEl.getSelectedKey());
		} else if (inviteeLink == source) {
			String invitationSettingsPath = "[AdminSite:0][loginadmin:0][Invitation:0]";
			NewControllerFactory.getInstance().launch(invitationSettingsPath, ureq, getWindowControl());
		} else if (source == courseExecEl) {
			courseModule.setCourseExecutionDefault(courseExecEl.getSelectedKey());
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// nothing
	}

}

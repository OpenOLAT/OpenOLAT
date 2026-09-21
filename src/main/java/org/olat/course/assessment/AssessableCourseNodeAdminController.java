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

import java.util.Collection;
import java.util.stream.Collectors;

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
import org.olat.core.gui.components.form.flexible.impl.FormSection;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.course.CourseModule;
import org.olat.modules.curriculum.TaughtBy;
import org.olat.modules.curriculum.ui.CurriculumAdminConfigurationController;
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
	private static final String INFO_BOX_KEY = "infobox";
	private static final String CHANGE_LOG_KEY = "changelog";
	private static final String EVENTS_KEY = "events";
	private static final String MEET_TEACHERS_KEY = "meetteachers";
	private static final String CERTIFICATE_KEY = "certificate";
	private static final String ON_KEY = "on";
	private static final String OFF_KEY = "off";
	private static final String[] onOffKeys = new String[]{ ON_KEY, OFF_KEY };

	private SingleSelection courseExecEl;
	private SingleSelection designEl;
	private MultipleSelectionElement assessmentOptionsEl;
	private MultipleSelectionElement disclaimerEnabledEl;
	private SingleSelection efficiencyStatementEnabledEl;
	private MultipleSelectionElement defaultShowInfoEl;
	private MultipleSelectionElement defaultTaughtByEl;
	private FormSection defaultSettingsCont;
	private FormSection courseRelatedConfigCont;

	private FormLink inviteeLink;
	private FormLink usageLink;

	@Autowired
	private CourseModule courseModule;

	public AssessableCourseNodeAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(CurriculumAdminConfigurationController.class, getLocale(), getTranslator()));
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("module.settings");
		setFormContextHelp("manual_admin/administration/Modules_Course/");

		String[] enableCourseOptionValues = new String[]{ translate("admin.disclaimer.enabled") };
		disclaimerEnabledEl = uifactory.addCheckboxesHorizontal("admin.enable.course.option", formLayout, onKeys, enableCourseOptionValues);
		disclaimerEnabledEl.addActionListener(FormEvent.ONCHANGE);
		if (courseModule.isDisclaimerEnabled()) {
			disclaimerEnabledEl.select(onKeys[0], true);
		}

		SelectionValues assessmentOptionsPK = new SelectionValues();
		assessmentOptionsPK.add(SelectionValues.entry(INFO_BOX_KEY, translate("admin.info.box")));
		assessmentOptionsPK.add(SelectionValues.entry(CHANGE_LOG_KEY, translate("admin.user.changelog")));
		assessmentOptionsEl = uifactory.addCheckboxesVertical("admin.enable.assessment.option", "admin.enable.assessment.option",
				formLayout, assessmentOptionsPK.keys(), assessmentOptionsPK.values(), 1);
		assessmentOptionsEl.addActionListener(FormEvent.ONCHANGE);
		assessmentOptionsEl.select(INFO_BOX_KEY, courseModule.isDisplayInfoBox());
		assessmentOptionsEl.select(CHANGE_LOG_KEY, courseModule.isDisplayChangeLog());

		defaultSettingsCont = uifactory.addFormSection("defaultSettings", translate("default.settings"), formLayout, FormSection.Level.SUB_TITLE);

		initCourseExecPeriodOptions(defaultSettingsCont);

		SelectionValues designKV = new SelectionValues();
		designKV.add(SelectionValues.entry(CourseModule.COURSE_TYPE_PATH, translate("course.design.path")));
		designKV.add(SelectionValues.entry(CourseModule.COURSE_TYPE_PROGRESS, translate("course.design.progress")));
		designKV.add(SelectionValues.entry(CourseModule.COURSE_TYPE_CLASSIC, translate("course.design.classic")));
		designEl = uifactory.addRadiosVertical("course.design", "admin.course.design.default", defaultSettingsCont,
				designKV.keys(), designKV.values());
		designEl.addActionListener(FormEvent.ONCHANGE);
		String defaultCourseType = courseModule.getCourseTypeDefault();
		if (!designEl.containsKey(defaultCourseType)) {
			defaultCourseType = CourseModule.COURSE_TYPE_PATH;
		}
		designEl.select(defaultCourseType, true);

		uifactory.addSpacerElement("defaultSettingsSpacer", defaultSettingsCont, false);

		efficiencyStatementEnabledEl = uifactory.addRadiosHorizontal("admin.efficiency.statement.enabled", "admin.efficiency.statement.enabled",
				defaultSettingsCont, onOffKeys, new String[]{ translate("on"), translate("off") });
		efficiencyStatementEnabledEl.addActionListener(FormEvent.ONCHANGE);
		efficiencyStatementEnabledEl.select(courseModule.isEfficiencyStatementEnabled() ? ON_KEY : OFF_KEY, true);

		uifactory.addSpacerElement("infoPageSpacer", defaultSettingsCont, false);

		SelectionValues showInfoPK = new SelectionValues();
		showInfoPK.add(SelectionValues.entry(EVENTS_KEY, translate("cif.events")));
		showInfoPK.add(SelectionValues.entry(MEET_TEACHERS_KEY, translate("cif.meet.your.teachers")));
		showInfoPK.add(SelectionValues.entry(CERTIFICATE_KEY, translate("details.certificate")));
		defaultShowInfoEl = uifactory.addCheckboxesVertical("default.show.info", "cif.display.on.info.page", defaultSettingsCont,
				showInfoPK.keys(), showInfoPK.values(), 1);
		defaultShowInfoEl.setHelpText(translate("cif.display.on.info.page.help"));
		defaultShowInfoEl.addActionListener(FormEvent.ONCHANGE);
		defaultShowInfoEl.select(EVENTS_KEY, courseModule.isDefaultShowLectures());
		defaultShowInfoEl.select(MEET_TEACHERS_KEY, !courseModule.getDefaultTaughtBys().isEmpty());
		defaultShowInfoEl.select(CERTIFICATE_KEY, courseModule.isDefaultShowCertificate());

		SelectionValues taughtByPK = new SelectionValues();
		TaughtBy.ALL.forEach(taughtBy -> taughtByPK.add(SelectionValues.entry(taughtBy.name(), translate("cif.role." + taughtBy.name()))));
		defaultTaughtByEl = uifactory.addCheckboxesVertical("default.taught.by", "cif.taught.by", defaultSettingsCont,
				taughtByPK.keys(), taughtByPK.values(), 1);
		defaultTaughtByEl.setHelpText(translate("cif.taught.by.help"));
		defaultTaughtByEl.addActionListener(FormEvent.ONCHANGE);
		courseModule.getDefaultTaughtBys().forEach(taughtBy -> defaultTaughtByEl.select(taughtBy.name(), true));

		courseRelatedConfigCont = uifactory.addFormSection("courseRelatedConfig", translate("admin.assessable.other.settings"), formLayout, FormSection.Level.SUB_TITLE);
		inviteeLink = uifactory.addFormLink("admin.link.invitation", "admin.link.invitation.path", "admin.link.invitation", courseRelatedConfigCont, Link.LINK);
		inviteeLink.setIconLeftCSS("o_icon o_icon_jump_to o_icon-fw");
		usageLink = uifactory.addFormLink("admin.link.usage", "admin.link.usage.path", "curriculum.default.course.runtime.type", courseRelatedConfigCont, Link.LINK);
		usageLink.setIconLeftCSS("o_icon o_icon_jump_to o_icon-fw");
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
		if (source == assessmentOptionsEl) {
			Collection<String> selectedOptions = assessmentOptionsEl.getSelectedKeys();
			courseModule.setDisplayInfoBox(selectedOptions.contains(INFO_BOX_KEY));
			courseModule.setDisplayChangeLog(selectedOptions.contains(CHANGE_LOG_KEY));
		} else if (source == disclaimerEnabledEl) {
			courseModule.setDisclaimerEnabled(disclaimerEnabledEl.isSelected(0));
		} else if (source == efficiencyStatementEnabledEl) {
			courseModule.setEfficiencyStatementEnabled(ON_KEY.equals(efficiencyStatementEnabledEl.getSelectedKey()));
		} else if (source == designEl) {
			courseModule.setCourseTypeDefault(designEl.getSelectedKey());
		} else if (inviteeLink == source) {
			String invitationSettingsPath = "[AdminSite:0][loginadmin:0][Invitation:0]";
			NewControllerFactory.getInstance().launch(invitationSettingsPath, ureq, getWindowControl());
		} else if (usageLink == source) {
			String usageSettingsPath = "[AdminSite:0][curriculum:0]";
			NewControllerFactory.getInstance().launch(usageSettingsPath, ureq, getWindowControl());
		} else if (source == courseExecEl) {
			courseModule.setCourseExecutionDefault(courseExecEl.getSelectedKey());
		} else if (source == defaultShowInfoEl) {
			Collection<String> selectedInfo = defaultShowInfoEl.getSelectedKeys();
			courseModule.setDefaultShowLectures(selectedInfo.contains(EVENTS_KEY));
			courseModule.setDefaultShowCertificate(selectedInfo.contains(CERTIFICATE_KEY));
		} else if (source == defaultTaughtByEl) {
			courseModule.setDefaultTaughtBys(defaultTaughtByEl.getSelectedKeys().stream()
					.map(TaughtBy::valueOf).collect(Collectors.toSet()));
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// nothing
	}

}

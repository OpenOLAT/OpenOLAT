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
package org.olat.modules.curriculum.ui;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormSection;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.TaughtBy;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12 févr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumAdminConfigurationController extends FormBasicController {

	private static final String[] onKeys = new String[] { "on" };
	private static final String OUTLINE_KEY = "outline";
	private static final String EVENTS_KEY = "events";
	private static final String MEET_TEACHERS_KEY = "meetteachers";
	private static final String CERTIFICATE_KEY = "certificate";

	private FormToggle enableEl;
	private MultipleSelectionElement curriculumMyCoursesEl;
	private MultipleSelectionElement linkedTaxonomiesEl;
	private FormSection configurationCont;
	private FormSection defaultSettingsCont;
	private SingleSelection defaultCourseRuntimeEl;
	private MultipleSelectionElement defaultShowInfoEl;
	private MultipleSelectionElement defaultTaughtByEl;

	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private TaxonomyService taxonomyService;

	public CurriculumAdminConfigurationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(CoreSpringFactory.class, getLocale(), getTranslator()));

		initForm(ureq);
		update();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("module.settings");
		setFormContextHelp("manual_admin/administration/Modules_Course_Planner/");

		enableEl = uifactory.addToggleButton("curriculum.admin.module", "curriculum.admin.module",
				translate("on"), translate("off"), formLayout);
		enableEl.addActionListener(FormEvent.ONCHANGE);
		enableEl.toggle(curriculumModule.isEnabled());

		String[] onValues = new String[] { translate("curriculum.in.my.courses.enabled") };
		curriculumMyCoursesEl = uifactory.addCheckboxesHorizontal("curriculum.admin.enable.option", formLayout, onKeys, onValues);
		curriculumMyCoursesEl.addActionListener(FormEvent.ONCHANGE);
		curriculumMyCoursesEl.setEvaluationOnlyVisible(true);
		if(curriculumModule.isCurriculumInMyCourses()) {
			curriculumMyCoursesEl.select(onKeys[0], true);
		}

		configurationCont = uifactory.addFormSection("configuration", translate("configuration"), formLayout, FormSection.Level.SUB_TITLE);

		List<Taxonomy> taxonomies = taxonomyService.getTaxonomyList();
		String[] taxonomyKeys = taxonomies.stream().map(taxonomy -> taxonomy.getKey().toString()).toArray(String[]::new);
		String[] taxonomyNames = taxonomies.stream().map(Taxonomy::getDisplayName).toArray(String[]::new);

		linkedTaxonomiesEl = uifactory.addCheckboxesVertical("taxonomy.linked.elements", configurationCont, taxonomyKeys, taxonomyNames, 1);
		curriculumModule.getTaxonomyRefs().stream().forEach(taxonomy -> linkedTaxonomiesEl.select(taxonomy.getKey().toString(), true));
		linkedTaxonomiesEl.addActionListener(FormEvent.ONCHANGE);
		linkedTaxonomiesEl.setEvaluationOnlyVisible(true);

		defaultSettingsCont = uifactory.addFormSection("defaultSettings", translate("default.settings"), formLayout, FormSection.Level.SUB_TITLE);

		String[] runtimeTypeKeys = new String[] { RepositoryEntryRuntimeType.standalone.name(), RepositoryEntryRuntimeType.curricular.name() };
		String[] runtimeTypeValues = new String[] {
				translate("curriculum.runtime.type." + RepositoryEntryRuntimeType.standalone.name() + ".title"),
				translate("curriculum.runtime.type." + RepositoryEntryRuntimeType.curricular.name() + ".title") };
		defaultCourseRuntimeEl = uifactory.addRadiosVertical("curriculum.default.course.runtime.type",
				"curriculum.default.course.runtime.type", defaultSettingsCont, runtimeTypeKeys, runtimeTypeValues);
		defaultCourseRuntimeEl.select(curriculumModule.getDefaultCourseRuntimeType().name(), true);
		defaultCourseRuntimeEl.addActionListener(FormEvent.ONCHANGE);

		uifactory.addSpacerElement("defaultSettingsSpacer", defaultSettingsCont, false);

		SelectionValues showInfoPK = new SelectionValues();
		showInfoPK.add(SelectionValues.entry(OUTLINE_KEY, translate("infos.outline")));
		showInfoPK.add(SelectionValues.entry(EVENTS_KEY, translate("cif.events")));
		showInfoPK.add(SelectionValues.entry(MEET_TEACHERS_KEY, translate("cif.meet.your.teachers")));
		showInfoPK.add(SelectionValues.entry(CERTIFICATE_KEY, translate("details.certificate")));
		defaultShowInfoEl = uifactory.addCheckboxesVertical("default.show.info", "cif.display.on.info.page", defaultSettingsCont,
				showInfoPK.keys(), showInfoPK.values(), 1);
		defaultShowInfoEl.setHelpText(translate("cif.display.on.info.page.help"));
		defaultShowInfoEl.addActionListener(FormEvent.ONCHANGE);
		defaultShowInfoEl.setEvaluationOnlyVisible(true);
		defaultShowInfoEl.select(OUTLINE_KEY, curriculumModule.isDefaultShowOutline());
		defaultShowInfoEl.select(EVENTS_KEY, curriculumModule.isDefaultShowLectures());
		defaultShowInfoEl.select(MEET_TEACHERS_KEY, !curriculumModule.getDefaultTaughtBys().isEmpty());
		defaultShowInfoEl.select(CERTIFICATE_KEY, curriculumModule.isDefaultShowCertificate());

		SelectionValues taughtByPK = new SelectionValues();
		TaughtBy.ALL.forEach(taughtBy -> taughtByPK.add(SelectionValues.entry(taughtBy.name(), translate("cif.role." + taughtBy.name()))));
		defaultTaughtByEl = uifactory.addCheckboxesVertical("default.taught.by", "cif.taught.by", defaultSettingsCont,
				taughtByPK.keys(), taughtByPK.values(), 1);
		defaultTaughtByEl.setHelpText(translate("cif.taught.by.help"));
		defaultTaughtByEl.addActionListener(FormEvent.ONCHANGE);
		defaultTaughtByEl.setEvaluationOnlyVisible(true);
		curriculumModule.getDefaultTaughtBys().forEach(taughtBy -> defaultTaughtByEl.select(taughtBy.name(), true));
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(enableEl == source) {
			curriculumModule.setEnabled(enableEl.isOn());
			update();
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else if(curriculumMyCoursesEl == source) {
			curriculumModule.setCurriculumInMyCourses(curriculumMyCoursesEl.isAtLeastSelected(1));
		} else if (linkedTaxonomiesEl == source) {
			curriculumModule.setLinkedTaxonomies(linkedTaxonomiesEl.getSelectedKeys());
		} else if (defaultCourseRuntimeEl == source) {
			RepositoryEntryRuntimeType runtimeType = RepositoryEntryRuntimeType.valueOf(defaultCourseRuntimeEl.getSelectedKey());
			curriculumModule.setDefaultCourseRuntimeType(runtimeType);
		} else if (defaultShowInfoEl == source) {
			Collection<String> selectedInfo = defaultShowInfoEl.getSelectedKeys();
			curriculumModule.setDefaultShowOutline(selectedInfo.contains(OUTLINE_KEY));
			curriculumModule.setDefaultShowLectures(selectedInfo.contains(EVENTS_KEY));
			curriculumModule.setDefaultShowCertificate(selectedInfo.contains(CERTIFICATE_KEY));
		} else if (defaultTaughtByEl == source) {
			curriculumModule.setDefaultTaughtBys(defaultTaughtByEl.getSelectedKeys().stream()
					.map(TaughtBy::valueOf).collect(Collectors.toSet()));
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void update() {
		boolean enabled = enableEl.isOn();
		curriculumMyCoursesEl.setVisible(enabled);
		configurationCont.setVisible(enabled);
		defaultSettingsCont.setVisible(enabled);
		linkedTaxonomiesEl.setVisible(enabled);
		defaultShowInfoEl.setVisible(enabled);
		defaultTaughtByEl.setVisible(enabled);
	}
}

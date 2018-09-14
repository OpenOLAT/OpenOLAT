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
package org.olat.modules.quality.analysis.ui;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.OrganisationModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.ui.CurriculumTreeModel;
import org.olat.modules.quality.analysis.AnalysisSearchParameter;
import org.olat.modules.quality.analysis.QualityAnalysisService;
import org.olat.modules.quality.ui.QualityUIFactory;
import org.olat.modules.quality.ui.QualityUIFactory.KeysValues;
import org.olat.user.ui.organisation.OrganisationTreeModel;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 04.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FilterController extends FormBasicController {

	private DateChooser dateRangeFromEl;
	private DateChooser dateRangeToEl;
	private MultipleSelectionElement organisationEl;
	private MultipleSelectionElement curriculumEl;
	private MultipleSelectionElement curriculumElementEl;
	private StaticTextElement countFilteredEl;
	
	private final AnalysisSearchParameter searchParams;
	
	@Autowired
	private QualityAnalysisService analysisService;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private CurriculumModule curriculumModule;

	public FilterController(UserRequest ureq, WindowControl wControl, AnalysisSearchParameter searchParams) {
		super(ureq, wControl);
		this.searchParams = searchParams;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		dateRangeFromEl = uifactory.addDateChooser("filter.date.range.from", null, formLayout);
		dateRangeFromEl.addActionListener(FormEvent.ONCHANGE);
		
		dateRangeToEl = uifactory.addDateChooser("filter.date.range.to", null, formLayout);
		dateRangeToEl.addActionListener(FormEvent.ONCHANGE);

		organisationEl = uifactory.addCheckboxesDropdown("filter.organisations", formLayout);
		organisationEl.addActionListener(FormEvent.ONCLICK);
		
		curriculumEl = uifactory.addCheckboxesDropdown("filter.curriculums", formLayout);
		curriculumEl.addActionListener(FormEvent.ONCLICK);

		curriculumElementEl = uifactory.addCheckboxesDropdown("filter.curriculum.elements", formLayout);
		curriculumElementEl.addActionListener(FormEvent.ONCLICK);
		
		countFilteredEl = uifactory.addStaticTextElement("filter.count", "", formLayout);
		
		setSelectionValues();
	}
	
	private void setSelectionValues() {
		setOrganisationValues();
		setCurriculumValues();
		setCurriculumElementValues();
		setCountFiltered();
	}
	
	private void setOrganisationValues() {
		if (!organisationModule.isEnabled()) {
			organisationEl.setVisible(false);
			return;
		}
		
		Collection<String> selectedKeys = organisationEl.getSelectedKeys();
		
		AnalysisSearchParameter orgSearchParams = new AnalysisSearchParameter();
		orgSearchParams.setFormEntryRef(searchParams.getFormEntryRef());
		List<Organisation> organisations = analysisService.loadContextOrganisations(orgSearchParams);
		OrganisationTreeModel organisationModel = new OrganisationTreeModel();
		organisationModel.loadTreeModel(organisations);
		
		KeysValues keysValues = QualityUIFactory.getTopicOrganisationKeysValues(organisationModel, null);
		organisationEl.setKeysAndValues(keysValues.getKeys(), keysValues.getValues());
		for (String key: selectedKeys) {
			organisationEl.select(key, true);
		}
	}

	private void setCurriculumValues() {
		if (!curriculumModule.isEnabled()) {
			curriculumEl.setVisible(false);
			return;
		}
		
		Collection<String> selectedKeys = curriculumEl.getSelectedKeys();
		
		AnalysisSearchParameter curriculumSearchParams = searchParams.clone();
		curriculumSearchParams.setCurriculumRefs(null);
		curriculumSearchParams.setCurriculumElementRefs(null);
		List<Curriculum> curriculums = analysisService.loadContextCurriculums(curriculumSearchParams);
		KeysValues keysValues = QualityUIFactory.getCurriculumKeysValues(curriculums, null);
		curriculumEl.setKeysAndValues(keysValues.getKeys(), keysValues.getValues());
		for (String key: selectedKeys) {
			curriculumEl.select(key, true);
		}
	}

	private void setCurriculumElementValues() {
		if (!curriculumModule.isEnabled()) {
			curriculumElementEl.setVisible(false);
			return;
		}
		
		Collection<String> selectedKeys = curriculumEl.getSelectedKeys();

		AnalysisSearchParameter curriculumElementSearchParams = searchParams.clone();
		Collection<String> curriculumKeys = curriculumEl.isAtLeastSelected(1)
				? curriculumEl.getSelectedKeys()
				: curriculumEl.getKeys();
		List<? extends CurriculumRef> curriculumRefs = curriculumKeys.stream()
				.map(key -> QualityUIFactory.getCurriculumRef(key))
				.collect(toList());
		curriculumElementSearchParams.setCurriculumRefs(curriculumRefs);
		curriculumElementSearchParams.setCurriculumElementRefs(null);
		List<CurriculumElement> curriculumElements = analysisService.loadContextCurriculumElements(curriculumElementSearchParams, true);
		
		CurriculumTreeModel curriculumTreeModel = new CurriculumTreeModel();
		curriculumTreeModel.loadTreeModel(curriculumElements);
		KeysValues keysValues = QualityUIFactory.getCurriculumElementKeysValues(curriculumTreeModel, null);
		curriculumElementEl.setKeysAndValues(keysValues.getKeys(), keysValues.getValues());
		for (String key: selectedKeys) {
			curriculumElementEl.select(key, true);
		}
	}
	
	private void setCountFiltered() {
		Long count = analysisService.loadFilterDataCollectionCount(searchParams);
		countFilteredEl.setValue(count.toString());
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		 if (source == dateRangeFromEl) {
			doFiltered(ureq);
		} else if (source == dateRangeToEl) {
			doFiltered(ureq);
		} else if (source == organisationEl) {
			doFiltered(ureq);
		} else if (source == curriculumEl) {
			doFiltered(ureq);
		} else if (source == curriculumElementEl) {
			doFiltered(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void doFiltered(UserRequest ureq) {
		getSearchParams();
		setSelectionValues();
		fireEvent(ureq, new AnalysisFilterEvent(searchParams));
	}

	private void getSearchParams() {
		getSearchParamOrganisations();
		getSearchParamCurriculums();
		getSearchParamCurriculumElements();
		getSearchParamDateRangeFrom();
		getSearchParamDateRangeTo();
	}

	private void getSearchParamOrganisations() {
		if (organisationModule.isEnabled() && organisationEl.isAtLeastSelected(1)) {
			List<OrganisationRef> organisationRefs = organisationEl.getSelectedKeys().stream()
					.map(key -> QualityUIFactory.getOrganisationRef(key))
					.collect(toList());
			searchParams.setOrganisationRefs(organisationRefs);
		} else {
			searchParams.setOrganisationRefs(null);
		}
	}

	private void getSearchParamCurriculums() {
		if (curriculumEl.isEnabled() && curriculumEl.isAtLeastSelected(1)) {
			Collection<CurriculumRef> curriculumRefs = curriculumEl.getSelectedKeys().stream()
					.map(key -> QualityUIFactory.getCurriculumRef(key))
					.collect(toList());
			searchParams.setCurriculumRefs(curriculumRefs);
		} else {
			searchParams.setCurriculumRefs(null);
		}
	}

	private void getSearchParamCurriculumElements() {
		if (curriculumEl.isEnabled() && curriculumElementEl.isAtLeastSelected(1)) {
			List<CurriculumElementRef> curriculumElementRefs = curriculumElementEl.getSelectedKeys().stream()
					.map(key -> QualityUIFactory.getCurriculumElementRef(key))
					.collect(toList());
			searchParams.setCurriculumElementRefs(curriculumElementRefs);
		} else {
			searchParams.setCurriculumElementRefs(null);
		}
	}

	private void getSearchParamDateRangeFrom() {
		Date dateRangeFrom = dateRangeFromEl.getDate();
		searchParams.setDateRangeFrom(dateRangeFrom);
	}

	private void getSearchParamDateRangeTo() {
		Date dateRangeTo = dateRangeToEl.getDate();
		searchParams.setDateRangeTo(dateRangeTo);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}

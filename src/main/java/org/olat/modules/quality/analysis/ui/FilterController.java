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
import static org.olat.core.gui.translator.TranslatorHelper.translateAll;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.IdentityShort;
import org.olat.basesecurity.OrganisationModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
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
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.user.ui.organisation.OrganisationTreeModel;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 04.09.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FilterController extends FormBasicController {

	private static final String[] WITH_USER_INFOS_KEYS = new String[] {"filter.with.user.informations"};

	private DateChooser dateRangeFromEl;
	private DateChooser dateRangeToEl;
	private MultipleSelectionElement topicIdentityEl;
	private MultipleSelectionElement topicOrganisationEl;
	private MultipleSelectionElement topicCurriculumEl;
	private MultipleSelectionElement topicCurriculumElementEl;
	private MultipleSelectionElement topicRepositoryEl;
	private MultipleSelectionElement contextOrganisationEl;
	private MultipleSelectionElement contextCurriculumEl;
	private MultipleSelectionElement contextCurriculumElementEl;
	private MultipleSelectionElement withUserInformationsEl;
	
	private final AnalysisSearchParameter searchParams;
	
	@Autowired
	private QualityAnalysisService analysisService;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private CurriculumModule curriculumModule;

	public FilterController(UserRequest ureq, WindowControl wControl, AnalysisSearchParameter searchParams) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.searchParams = searchParams;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_qual_ana_filter");
		
		FormLayoutContainer dateRange = FormLayoutContainer.createHorizontalFormLayout("dateRange", getTranslator());
		flc.add("dateRange", dateRange);
		dateRange.setElementCssClass("o_date_range");
		dateRangeFromEl = uifactory.addDateChooser("filter.date.range.from", null, dateRange);
		dateRangeFromEl.setElementCssClass("o_date_range_from");
		dateRangeFromEl.addActionListener(FormEvent.ONCHANGE);
		
		dateRangeToEl = uifactory.addDateChooser("filter.date.range.to", null, dateRange);
		dateRangeToEl.setElementCssClass("o_date_range_to");
		dateRangeToEl.addActionListener(FormEvent.ONCHANGE);

		topicIdentityEl = uifactory.addCheckboxesDropdown("filter.topic.identities", formLayout);
		topicIdentityEl.addActionListener(FormEvent.ONCLICK);
		
		topicOrganisationEl = uifactory.addCheckboxesDropdown("filter.topic.organisations", formLayout);
		topicOrganisationEl.addActionListener(FormEvent.ONCLICK);
		
		topicCurriculumEl = uifactory.addCheckboxesDropdown("filter.topic.curriculums", formLayout);
		topicCurriculumEl.addActionListener(FormEvent.ONCLICK);

		topicCurriculumElementEl = uifactory.addCheckboxesDropdown("filter.topic.curriculum.elements", formLayout);
		topicCurriculumElementEl.addActionListener(FormEvent.ONCLICK);

		topicRepositoryEl = uifactory.addCheckboxesDropdown("filter.topic.repositories", formLayout);
		topicRepositoryEl.addActionListener(FormEvent.ONCLICK);
		
		contextOrganisationEl = uifactory.addCheckboxesDropdown("filter.context.organisations", formLayout);
		contextOrganisationEl.addActionListener(FormEvent.ONCLICK);
		
		contextCurriculumEl = uifactory.addCheckboxesDropdown("filter.context.curriculums", formLayout);
		contextCurriculumEl.addActionListener(FormEvent.ONCLICK);

		contextCurriculumElementEl = uifactory.addCheckboxesDropdown("filter.context.curriculum.elements", formLayout);
		contextCurriculumElementEl.addActionListener(FormEvent.ONCLICK);
		
		withUserInformationsEl = uifactory.addCheckboxesVertical("filter.with.user.informations.label", formLayout,
				WITH_USER_INFOS_KEYS, translateAll(getTranslator(), WITH_USER_INFOS_KEYS), 1);
		withUserInformationsEl.addActionListener(FormEvent.ONCLICK);
		
		setSelectionValues();
	}
	
	private void setSelectionValues() {
		setTopicIdentityValues();
		setTopicOrganisationValues();
		setTopicCurriculumValues();
		setTopicCurriculumElementValues();
		setTopicRepositoryValues();
		setContextOrganisationValues();
		setContextCurriculumValues();
		setContextCurriculumElementValues();
	}
	
	private void setTopicIdentityValues() {
		Collection<String> selectedKeys = topicIdentityEl.getSelectedKeys();
		
		AnalysisSearchParameter searchParamsClone = searchParams.clone();
		searchParamsClone.setTopicIdentityRefs(null);
		List<IdentityShort> identities = analysisService.loadTopicIdentity(searchParamsClone);
		
		KeysValues keysValues = QualityUIFactory.getIdentityKeysValues(identities);
		topicIdentityEl.setKeysAndValues(keysValues.getKeys(), keysValues.getValues());
		for (String key: selectedKeys) {
			topicIdentityEl.select(key, true);
		}
	}
	
	private void setTopicOrganisationValues() {
		if (!organisationModule.isEnabled()) {
			topicOrganisationEl.setVisible(false);
			return;
		}
		
		Collection<String> selectedKeys = topicOrganisationEl.getSelectedKeys();
		
		AnalysisSearchParameter searchParamsClone = searchParams.clone();
		searchParamsClone.setTopicOrganisationRefs(null);
		List<Organisation> organisations = analysisService.loadTopicOrganisations(searchParamsClone, true);
		
		KeysValues keysValues = QualityUIFactory.getOrganisationFlatKeysValues(organisations, null);
		topicOrganisationEl.setKeysAndValues(keysValues.getKeys(), keysValues.getValues());
		for (String key: selectedKeys) {
			topicOrganisationEl.select(key, true);
		}
	}

	private void setTopicCurriculumValues() {
		if (!curriculumModule.isEnabled()) {
			topicCurriculumEl.setVisible(false);
			return;
		}
		
		Collection<String> selectedKeys = topicCurriculumEl.getSelectedKeys();
		
		AnalysisSearchParameter searchParamsClone = searchParams.clone();
		searchParamsClone.setTopicCurriculumRefs(null);
		List<Curriculum> curriculums = analysisService.loadTopicCurriculums(searchParamsClone);
		KeysValues keysValues = QualityUIFactory.getCurriculumKeysValues(curriculums, null);
		topicCurriculumEl.setKeysAndValues(keysValues.getKeys(), keysValues.getValues());
		for (String key: selectedKeys) {
			topicCurriculumEl.select(key, true);
		}
	}

	private void setTopicCurriculumElementValues() {
		if (!curriculumModule.isEnabled()) {
			contextCurriculumElementEl.setVisible(false);
			return;
		}
		
		Collection<String> selectedKeys = topicCurriculumEl.getSelectedKeys();

		AnalysisSearchParameter curriculumElementSearchParams = searchParams.clone();
		curriculumElementSearchParams.setTopicCurriculumElementRefs(null);
		List<CurriculumElement> curriculumElements = analysisService.loadTopicCurriculumElements(curriculumElementSearchParams);
		
		KeysValues keysValues = QualityUIFactory.getCurriculumElementFlatKeysValues(curriculumElements, null);
		topicCurriculumElementEl.setKeysAndValues(keysValues.getKeys(), keysValues.getValues());
		for (String key: selectedKeys) {
			topicCurriculumElementEl.select(key, true);
		}
	}
	
	private void setTopicRepositoryValues() {
		Collection<String> selectedKeys = topicRepositoryEl.getSelectedKeys();
		
		AnalysisSearchParameter searchParamsClone = searchParams.clone();
		searchParamsClone.setTopicRepositoryRefs(null);
		List<RepositoryEntry> entries = analysisService.loadTopicRepositoryEntries(searchParamsClone);
		
		KeysValues keysValues = QualityUIFactory.getRepositoryEntriesFlatKeysValues(entries);
		topicRepositoryEl.setKeysAndValues(keysValues.getKeys(), keysValues.getValues());
		for (String key: selectedKeys) {
			topicRepositoryEl.select(key, true);
		}
	}

	private void setContextOrganisationValues() {
		if (!organisationModule.isEnabled() || !curriculumModule.isEnabled()) {
			contextOrganisationEl.setVisible(false);
			return;
		}
		
		Collection<String> selectedKeys = contextOrganisationEl.getSelectedKeys();
		
		AnalysisSearchParameter searchParamsClone = searchParams.clone();
		searchParamsClone.setContextOrganisationRefs(null);
		searchParamsClone.setContextCurriculumRefs(null);
		searchParamsClone.setContextCurriculumElementRefs(null);
		List<Organisation> organisations = analysisService.loadContextOrganisations(searchParamsClone, true);
		OrganisationTreeModel organisationModel = new OrganisationTreeModel();
		organisationModel.loadTreeModel(organisations);
		
		KeysValues keysValues = QualityUIFactory.getOrganisationKeysValues(organisationModel, null);
		contextOrganisationEl.setKeysAndValues(keysValues.getKeys(), keysValues.getValues());
		for (String key: selectedKeys) {
			contextOrganisationEl.select(key, true);
		}
	}

	private void setContextCurriculumValues() {
		if (!curriculumModule.isEnabled()) {
			contextCurriculumEl.setVisible(false);
			return;
		}
		
		Collection<String> selectedKeys = contextCurriculumEl.getSelectedKeys();
		
		AnalysisSearchParameter curriculumSearchParams = searchParams.clone();
		curriculumSearchParams.setContextCurriculumRefs(null);
		curriculumSearchParams.setContextCurriculumElementRefs(null);
		List<Curriculum> curriculums = analysisService.loadContextCurriculums(curriculumSearchParams);
		KeysValues keysValues = QualityUIFactory.getCurriculumKeysValues(curriculums, null);
		contextCurriculumEl.setKeysAndValues(keysValues.getKeys(), keysValues.getValues());
		for (String key: selectedKeys) {
			contextCurriculumEl.select(key, true);
		}
	}

	private void setContextCurriculumElementValues() {
		if (!curriculumModule.isEnabled()) {
			contextCurriculumElementEl.setVisible(false);
			return;
		}
		
		Collection<String> selectedKeys = contextCurriculumEl.getSelectedKeys();

		AnalysisSearchParameter curriculumElementSearchParams = searchParams.clone();
		Collection<String> curriculumKeys = contextCurriculumEl.isAtLeastSelected(1)
				? contextCurriculumEl.getSelectedKeys()
				: contextCurriculumEl.getKeys();
		List<? extends CurriculumRef> curriculumRefs = curriculumKeys.stream()
				.map(key -> QualityUIFactory.getCurriculumRef(key))
				.collect(toList());
		curriculumElementSearchParams.setContextCurriculumRefs(curriculumRefs);
		curriculumElementSearchParams.setContextCurriculumElementRefs(null);
		List<CurriculumElement> curriculumElements = analysisService.loadContextCurriculumElements(curriculumElementSearchParams, true);
		
		CurriculumTreeModel curriculumTreeModel = new CurriculumTreeModel();
		curriculumTreeModel.loadTreeModel(curriculumElements);
		KeysValues keysValues = QualityUIFactory.getCurriculumElementKeysValues(curriculumTreeModel, null);
		contextCurriculumElementEl.setKeysAndValues(keysValues.getKeys(), keysValues.getValues());
		for (String key: selectedKeys) {
			contextCurriculumElementEl.select(key, true);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		 if (source == dateRangeFromEl) {
			doFiltered(ureq);
		} else if (source == dateRangeToEl) {
			doFiltered(ureq);
		} else if (source == topicIdentityEl) {
			doFiltered(ureq);
		} else if (source == topicOrganisationEl) {
			doFiltered(ureq);
		} else if (source == topicCurriculumEl) {
			doFiltered(ureq);
		} else if (source == topicCurriculumElementEl) {
			doFiltered(ureq);
		} else if (source == topicRepositoryEl) {
			doFiltered(ureq);
		} else if (source == contextOrganisationEl) {
			doFiltered(ureq);
		} else if (source == contextCurriculumEl) {
			doFiltered(ureq);
		} else if (source == contextCurriculumElementEl) {
			doFiltered(ureq);
		} else if (source == withUserInformationsEl) {
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
		getSearchParamDateRangeFrom();
		getSearchParamDateRangeTo();
		getSearchParamTopicIdentitys();
		getSearchParamTopicOrganisations();
		getSearchParamTopicCurriculums();
		getSearchParamTopicCurriculumElements();
		getSearchParamTopicRepositorys();
		getSearchParamContextOrganisations();
		getSearchParamContextCurriculums();
		getSearchParamContextCurriculumElements();
		getSearchParamWithUserInfosOnly();
	}

	private void getSearchParamDateRangeFrom() {
		Date dateRangeFrom = dateRangeFromEl.getDate();
		searchParams.setDateRangeFrom(dateRangeFrom);
	}

	private void getSearchParamDateRangeTo() {
		Date dateRangeTo = dateRangeToEl.getDate();
		searchParams.setDateRangeTo(dateRangeTo);
	}

	private void getSearchParamTopicIdentitys() {
		if (topicIdentityEl.isAtLeastSelected(1)) {
			List<IdentityRef> identityRefs = topicIdentityEl.getSelectedKeys().stream()
					.map(key -> QualityUIFactory.getIdentityRef(key))
					.collect(toList());
			searchParams.setTopicIdentityRefs(identityRefs);
		} else {
			searchParams.setTopicIdentityRefs(null);
		}
	}
	
	private void getSearchParamTopicOrganisations() {
		if (organisationModule.isEnabled() && curriculumModule.isEnabled() && topicOrganisationEl.isAtLeastSelected(1)) {
			List<OrganisationRef> organisationRefs = topicOrganisationEl.getSelectedKeys().stream()
					.map(key -> QualityUIFactory.getOrganisationRef(key))
					.collect(toList());
			searchParams.setTopicOrganisationRefs(organisationRefs);
		} else {
			searchParams.setTopicOrganisationRefs(null);
		}
	}

	private void getSearchParamTopicCurriculums() {
		if (topicCurriculumEl.isEnabled() && topicCurriculumEl.isAtLeastSelected(1)) {
			Collection<CurriculumRef> curriculumRefs = topicCurriculumEl.getSelectedKeys().stream()
					.map(key -> QualityUIFactory.getCurriculumRef(key))
					.collect(toList());
			searchParams.setTopicCurriculumRefs(curriculumRefs);
		} else {
			searchParams.setTopicCurriculumRefs(null);
		}
	}
	
	private void getSearchParamTopicCurriculumElements() {
		if (topicCurriculumEl.isEnabled() && topicCurriculumElementEl.isAtLeastSelected(1)) {
			List<CurriculumElementRef> curriculumElementRefs = topicCurriculumElementEl.getSelectedKeys().stream()
					.map(key -> QualityUIFactory.getCurriculumElementRef(key))
					.collect(toList());
			searchParams.setTopicCurriculumElementRefs(curriculumElementRefs);
		} else {
			searchParams.setTopicCurriculumElementRefs(null);
		}
	}
	
	private void getSearchParamTopicRepositorys() {
		if (topicRepositoryEl.isAtLeastSelected(1)) {
			List<RepositoryEntryRef> entryRefs = topicRepositoryEl.getSelectedKeys().stream()
					.map(key -> QualityUIFactory.getRepositoryEntryRef(key))
					.collect(toList());
			searchParams.setTopicRepositoryRefs(entryRefs);
		} else {
			searchParams.setTopicRepositoryRefs(null);
		}
	}

	private void getSearchParamContextOrganisations() {
		if (organisationModule.isEnabled() && contextOrganisationEl.isAtLeastSelected(1)) {
			List<OrganisationRef> organisationRefs = contextOrganisationEl.getSelectedKeys().stream()
					.map(key -> QualityUIFactory.getOrganisationRef(key))
					.collect(toList());
			searchParams.setContextOrganisationRefs(organisationRefs);
		} else {
			searchParams.setContextOrganisationRefs(null);
		}
	}

	private void getSearchParamContextCurriculums() {
		if (contextCurriculumEl.isEnabled() && contextCurriculumEl.isAtLeastSelected(1)) {
			Collection<CurriculumRef> curriculumRefs = contextCurriculumEl.getSelectedKeys().stream()
					.map(key -> QualityUIFactory.getCurriculumRef(key))
					.collect(toList());
			searchParams.setContextCurriculumRefs(curriculumRefs);
		} else {
			searchParams.setContextCurriculumRefs(null);
		}
	}

	private void getSearchParamContextCurriculumElements() {
		if (contextCurriculumEl.isEnabled() && contextCurriculumElementEl.isAtLeastSelected(1)) {
			List<CurriculumElementRef> curriculumElementRefs = contextCurriculumElementEl.getSelectedKeys().stream()
					.map(key -> QualityUIFactory.getCurriculumElementRef(key))
					.collect(toList());
			searchParams.setContextCurriculumElementRefs(curriculumElementRefs);
		} else {
			searchParams.setContextCurriculumElementRefs(null);
		}
	}
	
	private void getSearchParamWithUserInfosOnly() {
		boolean withUserInfosOnly = withUserInformationsEl.isAtLeastSelected(1);
		searchParams.setWithUserInfosOnly(withUserInfosOnly);
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

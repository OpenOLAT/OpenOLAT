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
import static org.olat.core.gui.components.util.KeyValues.VALUE_ASC;
import static org.olat.core.gui.components.util.KeyValues.entry;
import static org.olat.core.gui.translator.TranslatorHelper.translateAll;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
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
import org.olat.core.gui.components.util.KeyValues;
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
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.SessionInformations;
import org.olat.modules.quality.analysis.AnalysisSearchParameter;
import org.olat.modules.quality.analysis.AvailableAttributes;
import org.olat.modules.quality.analysis.QualityAnalysisService;
import org.olat.modules.quality.ui.QualityUIFactory;
import org.olat.modules.quality.ui.QualityUIFactory.KeysValues;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.model.TaxonomyLevelRefImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.user.ui.organisation.OrganisationTreeModel;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 04.09.2018<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class FilterController extends FormBasicController {

	private static final String[] WITH_USER_INFOS_KEYS = new String[] { "filter.with.user.informations" };

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
	private MultipleSelectionElement contextTaxonomyLevelEl;
	private MultipleSelectionElement contextLocationEl;
	private MultipleSelectionElement seriesIndexEl;
	private MultipleSelectionElement withUserInformationsEl;

	private final AnalysisSearchParameter searchParams;
	private final AvailableAttributes availableAttributes;
	private final boolean sessionInformationsAvailable;

	@Autowired
	private QualityAnalysisService analysisService;
	@Autowired
	private OrganisationModule organisationModule;
	@Autowired
	private CurriculumModule curriculumModule;

	public FilterController(UserRequest ureq, WindowControl wControl, Form form, AnalysisSearchParameter searchParams,
			AvailableAttributes availableAttributes) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		this.searchParams = searchParams;
		this.availableAttributes = availableAttributes;
		this.sessionInformationsAvailable = getSessionInformationAvailable(form);
		initForm(ureq);
	}

	private boolean getSessionInformationAvailable(Form evaluationForm) {
		for (AbstractElement element : evaluationForm.getElements()) {
			if (element instanceof SessionInformations) {
				return true;
			}
		}
		return false;
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

		contextTaxonomyLevelEl = uifactory.addCheckboxesDropdown("filter.context.taxonomy.level", formLayout);
		contextTaxonomyLevelEl.addActionListener(FormEvent.ONCLICK);

		contextLocationEl = uifactory.addCheckboxesDropdown("filter.context.location", formLayout);
		contextLocationEl.addActionListener(FormEvent.ONCLICK);

		seriesIndexEl = uifactory.addCheckboxesDropdown("filter.series.index", formLayout);
		seriesIndexEl.addActionListener(FormEvent.ONCLICK);

		withUserInformationsEl = uifactory.addCheckboxesVertical("filter.with.user.informations.label", formLayout,
				WITH_USER_INFOS_KEYS, translateAll(getTranslator(), WITH_USER_INFOS_KEYS), 1);
		withUserInformationsEl.addActionListener(FormEvent.ONCLICK);
		withUserInformationsEl.setVisible(sessionInformationsAvailable);

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
		setContextTaxonomyLevelValues();
		setContextLocationValues();
		setSeriesIndexValues();
	}

	private void setTopicIdentityValues() {
		if (!availableAttributes.isTopicIdentity()) {
			topicIdentityEl.setVisible(false);
		}

		Collection<String> selectedKeys = topicIdentityEl.getSelectedKeys();

		AnalysisSearchParameter searchParamsClone = searchParams.clone();
		searchParamsClone.setTopicIdentityRefs(null);
		List<IdentityShort> identities = analysisService.loadTopicIdentity(searchParamsClone);

		KeysValues keysValues = QualityUIFactory.getIdentityKeysValues(identities);
		topicIdentityEl.setKeysAndValues(keysValues.getKeys(), keysValues.getValues());
		for (String key : selectedKeys) {
			topicIdentityEl.select(key, true);
		}
	}

	private void setTopicOrganisationValues() {
		if (!availableAttributes.isTopicOrganisation() || !organisationModule.isEnabled()) {
			topicOrganisationEl.setVisible(false);
			return;
		}

		Collection<String> selectedKeys = topicOrganisationEl.getSelectedKeys();

		AnalysisSearchParameter searchParamsClone = searchParams.clone();
		searchParamsClone.setTopicOrganisationRefs(null);
		List<Organisation> organisations = analysisService.loadTopicOrganisations(searchParamsClone, true);

		KeysValues keysValues = QualityUIFactory.getOrganisationFlatKeysValues(organisations, null);
		topicOrganisationEl.setKeysAndValues(keysValues.getKeys(), keysValues.getValues());
		for (String key : selectedKeys) {
			topicOrganisationEl.select(key, true);
		}
	}

	private void setTopicCurriculumValues() {
		if (!availableAttributes.isTopicCurriculum() || !curriculumModule.isEnabled()) {
			topicCurriculumEl.setVisible(false);
			return;
		}

		Collection<String> selectedKeys = topicCurriculumEl.getSelectedKeys();

		AnalysisSearchParameter searchParamsClone = searchParams.clone();
		searchParamsClone.setTopicCurriculumRefs(null);
		List<Curriculum> curriculums = analysisService.loadTopicCurriculums(searchParamsClone);
		KeysValues keysValues = QualityUIFactory.getCurriculumKeysValues(curriculums, null);
		topicCurriculumEl.setKeysAndValues(keysValues.getKeys(), keysValues.getValues());
		for (String key : selectedKeys) {
			topicCurriculumEl.select(key, true);
		}
	}

	private void setTopicCurriculumElementValues() {
		if (!availableAttributes.isTopicCurriculumElement() || !curriculumModule.isEnabled()) {
			contextCurriculumElementEl.setVisible(false);
			return;
		}

		Collection<String> selectedKeys = topicCurriculumEl.getSelectedKeys();

		AnalysisSearchParameter curriculumElementSearchParams = searchParams.clone();
		curriculumElementSearchParams.setTopicCurriculumElementRefs(null);
		List<CurriculumElement> curriculumElements = analysisService
				.loadTopicCurriculumElements(curriculumElementSearchParams);

		KeysValues keysValues = QualityUIFactory.getCurriculumElementFlatKeysValues(curriculumElements, null);
		topicCurriculumElementEl.setKeysAndValues(keysValues.getKeys(), keysValues.getValues());
		for (String key : selectedKeys) {
			topicCurriculumElementEl.select(key, true);
		}
	}

	private void setTopicRepositoryValues() {
		if (!availableAttributes.isTopicRepository()) {
			topicRepositoryEl.setVisible(false);
		}

		Collection<String> selectedKeys = topicRepositoryEl.getSelectedKeys();

		AnalysisSearchParameter searchParamsClone = searchParams.clone();
		searchParamsClone.setTopicRepositoryRefs(null);
		List<RepositoryEntry> entries = analysisService.loadTopicRepositoryEntries(searchParamsClone);

		KeysValues keysValues = QualityUIFactory.getRepositoryEntriesFlatKeysValues(entries);
		topicRepositoryEl.setKeysAndValues(keysValues.getKeys(), keysValues.getValues());
		for (String key : selectedKeys) {
			topicRepositoryEl.select(key, true);
		}
	}

	private void setContextOrganisationValues() {
		if (!availableAttributes.isContextOrganisation() || !organisationModule.isEnabled()
				|| !curriculumModule.isEnabled()) {
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
		for (String key : selectedKeys) {
			contextOrganisationEl.select(key, true);
		}
	}

	private void setContextCurriculumValues() {
		if (!availableAttributes.isContextCurriculum() || !curriculumModule.isEnabled()) {
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
		for (String key : selectedKeys) {
			contextCurriculumEl.select(key, true);
		}
	}

	private void setContextCurriculumElementValues() {
		if (!availableAttributes.isContextCurriculumElement() || !curriculumModule.isEnabled()) {
			contextCurriculumElementEl.setVisible(false);
			return;
		}

		Collection<String> selectedKeys = contextCurriculumEl.getSelectedKeys();

		AnalysisSearchParameter curriculumElementSearchParams = searchParams.clone();
		Collection<String> curriculumKeys = contextCurriculumEl.isAtLeastSelected(1)
				? contextCurriculumEl.getSelectedKeys()
				: contextCurriculumEl.getKeys();
		List<? extends CurriculumRef> curriculumRefs = curriculumKeys.stream()
				.map(key -> QualityUIFactory.getCurriculumRef(key)).collect(toList());
		curriculumElementSearchParams.setContextCurriculumRefs(curriculumRefs);
		curriculumElementSearchParams.setContextCurriculumElementRefs(null);
		List<CurriculumElement> curriculumElements = analysisService
				.loadContextCurriculumElements(curriculumElementSearchParams, true);

		CurriculumTreeModel curriculumTreeModel = new CurriculumTreeModel();
		curriculumTreeModel.loadTreeModel(curriculumElements);
		KeysValues keysValues = QualityUIFactory.getCurriculumElementKeysValues(curriculumTreeModel, null);
		contextCurriculumElementEl.setKeysAndValues(keysValues.getKeys(), keysValues.getValues());
		for (String key : selectedKeys) {
			contextCurriculumElementEl.select(key, true);
		}
	}

	private void setContextTaxonomyLevelValues() {
		if (!availableAttributes.isContextTaxonomyLevel()) {
			contextTaxonomyLevelEl.setVisible(false);
			return;
		}

		Collection<String> selectedKeys = contextOrganisationEl.getSelectedKeys();

		AnalysisSearchParameter searchParamsClone = searchParams.clone();
		searchParamsClone.setContextTaxonomyLevelRefs(null);
		List<TaxonomyLevel> levels = analysisService.loadContextTaxonomyLevels(searchParamsClone, true);

		KeyValues keyValues = new KeyValues();
		// Create the key / value pairs and sort them according to the hierarchical
		// structure.
		for (TaxonomyLevel level : levels) {
			String key = Long.toString(level.getKey());
			ArrayList<String> names = new ArrayList<>();
			QualityUIFactory.addParentTaxonomyLevelNames(names, level);
			Collections.reverse(names);
			String value = String.join(" / ", names);
			keyValues.add(entry(key, value));
		}
		keyValues.sort(VALUE_ASC);

		// Replace with the intended value (but keep the sort order).
		for (TaxonomyLevel level : levels) {
			String key = Long.toString(level.getKey());
			String intendedLevel = QualityUIFactory.getIntendedTaxonomyLevel(level);
			keyValues.replaceOrPut(entry(key, intendedLevel));
		}

		contextTaxonomyLevelEl.setKeysAndValues(keyValues.keys(), keyValues.values());
		for (String key : selectedKeys) {
			contextTaxonomyLevelEl.select(key, true);
		}
	}

	private void setContextLocationValues() {
		if (!availableAttributes.isContextLocation()) {
			contextLocationEl.setVisible(false);
			return;
		}

		Collection<String> selectedKeys = contextLocationEl.getSelectedKeys();

		AnalysisSearchParameter locationSearchParams = searchParams.clone();
		locationSearchParams.setContextLocations(null);
		List<String> locations = analysisService.loadContextLocations(locationSearchParams);
		locations.sort(String::compareToIgnoreCase);
		String[] locs = locations.stream().toArray(String[]::new);
		contextLocationEl.setKeysAndValues(locs, locs);
		for (String key : selectedKeys) {
			contextLocationEl.select(key, true);
		}
	}

	private void setSeriesIndexValues() {
		if (!availableAttributes.isSeriesIndex()) {
			seriesIndexEl.setVisible(false);
			return;
		}

		Collection<String> selectedKeys = seriesIndexEl.getSelectedKeys();

		AnalysisSearchParameter clonedSearchParams = searchParams.clone();
		clonedSearchParams.setSeriesIndexes(null);
		int maxSerieIndex = analysisService.loadMaxSeriesIndex(clonedSearchParams);
		KeyValues keyValues = new KeyValues();
		for (int i = 1; i <= maxSerieIndex; i++) {
			String key = String.valueOf(i);
			String value = translate("filter.series.index.value", new String[] { key });
			keyValues.add(entry(key, value));
		}
		seriesIndexEl.setKeysAndValues(keyValues.keys(), keyValues.values());
		for (String key : selectedKeys) {
			seriesIndexEl.select(key, true);
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
		} else if (source == contextTaxonomyLevelEl) {
			doFiltered(ureq);
		} else if (source == contextLocationEl) {
			doFiltered(ureq);
		} else if (source == seriesIndexEl) {
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
		getSearchParamContextTaxonomyLevels();
		getSearchParamContextLocations();
		getSearchParamSeriesIndex();
		getSearchParamWithUserInfosOnly();
	}

	private void getSearchParamDateRangeFrom() {
		Date dateRangeFrom = dateRangeFromEl.getDate();
		searchParams.setDateRangeFrom(dateRangeFrom);
	}

	private void getSearchParamDateRangeTo() {
		Date dateRangeTo = dateRangeToEl.getDate();

		if (dateRangeTo != null) {
			// Include the whole day
			Calendar endOfDay = new GregorianCalendar();
			endOfDay.setTime(dateRangeTo);
			endOfDay.set(Calendar.HOUR_OF_DAY, 0);
			endOfDay.set(Calendar.MINUTE, 0);
			endOfDay.set(Calendar.SECOND, 0);
			endOfDay.set(Calendar.MILLISECOND, 0);
			endOfDay.add(Calendar.DAY_OF_MONTH, 1);
			searchParams.setDateRangeTo(endOfDay.getTime());
		} else {
			searchParams.setDateRangeTo(null);
		}
	}

	private void getSearchParamTopicIdentitys() {
		if (topicIdentityEl.isVisible() && topicIdentityEl.isAtLeastSelected(1)) {
			List<IdentityRef> identityRefs = topicIdentityEl.getSelectedKeys().stream()
					.map(key -> QualityUIFactory.getIdentityRef(key)).collect(toList());
			searchParams.setTopicIdentityRefs(identityRefs);
		} else {
			searchParams.setTopicIdentityRefs(null);
		}
	}

	private void getSearchParamTopicOrganisations() {
		if (topicOrganisationEl.isVisible() && topicOrganisationEl.isAtLeastSelected(1)) {
			List<OrganisationRef> organisationRefs = topicOrganisationEl.getSelectedKeys().stream()
					.map(key -> QualityUIFactory.getOrganisationRef(key)).collect(toList());
			searchParams.setTopicOrganisationRefs(organisationRefs);
		} else {
			searchParams.setTopicOrganisationRefs(null);
		}
	}

	private void getSearchParamTopicCurriculums() {
		if (topicCurriculumEl.isVisible() && topicCurriculumEl.isAtLeastSelected(1)) {
			Collection<CurriculumRef> curriculumRefs = topicCurriculumEl.getSelectedKeys().stream()
					.map(key -> QualityUIFactory.getCurriculumRef(key)).collect(toList());
			searchParams.setTopicCurriculumRefs(curriculumRefs);
		} else {
			searchParams.setTopicCurriculumRefs(null);
		}
	}

	private void getSearchParamTopicCurriculumElements() {
		if (topicCurriculumElementEl.isVisible() && topicCurriculumElementEl.isAtLeastSelected(1)) {
			List<CurriculumElementRef> curriculumElementRefs = topicCurriculumElementEl.getSelectedKeys().stream()
					.map(key -> QualityUIFactory.getCurriculumElementRef(key)).collect(toList());
			searchParams.setTopicCurriculumElementRefs(curriculumElementRefs);
		} else {
			searchParams.setTopicCurriculumElementRefs(null);
		}
	}

	private void getSearchParamTopicRepositorys() {
		if (topicRepositoryEl.isVisible() && topicRepositoryEl.isAtLeastSelected(1)) {
			List<RepositoryEntryRef> entryRefs = topicRepositoryEl.getSelectedKeys().stream()
					.map(key -> QualityUIFactory.getRepositoryEntryRef(key)).collect(toList());
			searchParams.setTopicRepositoryRefs(entryRefs);
		} else {
			searchParams.setTopicRepositoryRefs(null);
		}
	}

	private void getSearchParamContextOrganisations() {
		if (contextOrganisationEl.isVisible() && contextOrganisationEl.isAtLeastSelected(1)) {
			List<OrganisationRef> organisationRefs = contextOrganisationEl.getSelectedKeys().stream()
					.map(key -> QualityUIFactory.getOrganisationRef(key)).collect(toList());
			searchParams.setContextOrganisationRefs(organisationRefs);
		} else {
			searchParams.setContextOrganisationRefs(null);
		}
	}

	private void getSearchParamContextCurriculums() {
		if (contextCurriculumEl.isVisible() && contextCurriculumEl.isAtLeastSelected(1)) {
			Collection<CurriculumRef> curriculumRefs = contextCurriculumEl.getSelectedKeys().stream()
					.map(key -> QualityUIFactory.getCurriculumRef(key)).collect(toList());
			searchParams.setContextCurriculumRefs(curriculumRefs);
		} else {
			searchParams.setContextCurriculumRefs(null);
		}
	}

	private void getSearchParamContextCurriculumElements() {
		if (contextCurriculumElementEl.isVisible() && contextCurriculumElementEl.isAtLeastSelected(1)) {
			List<CurriculumElementRef> curriculumElementRefs = contextCurriculumElementEl.getSelectedKeys().stream()
					.map(key -> QualityUIFactory.getCurriculumElementRef(key)).collect(toList());
			searchParams.setContextCurriculumElementRefs(curriculumElementRefs);
		} else {
			searchParams.setContextCurriculumElementRefs(null);
		}
	}

	private void getSearchParamContextTaxonomyLevels() {
		if (contextTaxonomyLevelEl.isVisible() && contextTaxonomyLevelEl.isAtLeastSelected(1)) {
			List<TaxonomyLevelRef> curriculumElementRefs = contextTaxonomyLevelEl.getSelectedKeys().stream()
					.map(Long::parseLong).map(TaxonomyLevelRefImpl::new).collect(toList());
			searchParams.setContextTaxonomyLevelRefs(curriculumElementRefs);
		} else {
			searchParams.setContextTaxonomyLevelRefs(null);
		}
	}

	private void getSearchParamContextLocations() {
		if (contextLocationEl.isVisible() && contextLocationEl.isAtLeastSelected(1)) {
			Collection<String> locations = contextLocationEl.getSelectedKeys();
			searchParams.setContextLocations(locations);
		} else {
			searchParams.setContextLocations(null);
		}
	}

	private void getSearchParamSeriesIndex() {
		if (seriesIndexEl.isVisible() && seriesIndexEl.isAtLeastSelected(1)) {
			Collection<Integer> seriesIndexes = seriesIndexEl.getSelectedKeys().stream().map(Integer::valueOf)
					.collect(toList());
			searchParams.setSeriesIndexes(seriesIndexes);
		} else {
			searchParams.setSeriesIndexes(null);
		}
	}

	private void getSearchParamWithUserInfosOnly() {
		boolean withUserInfosOnly = withUserInformationsEl.isVisible() && withUserInformationsEl.isAtLeastSelected(1);
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

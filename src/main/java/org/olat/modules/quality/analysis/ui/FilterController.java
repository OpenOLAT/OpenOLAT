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
import static org.olat.core.gui.components.util.SelectionValues.VALUE_ASC;
import static org.olat.core.gui.components.util.SelectionValues.entry;
import static org.olat.core.gui.translator.TranslatorHelper.translateAll;
import static org.olat.modules.quality.analysis.ui.AnalysisUIFactory.translateRole;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.OrganisationModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.AutoCompletionMultiSelection;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Organisation;
import org.olat.core.id.OrganisationRef;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumElementTypeRef;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.forms.model.xml.AbstractElement;
import org.olat.modules.forms.model.xml.Form;
import org.olat.modules.forms.model.xml.SessionInformations;
import org.olat.modules.quality.QualityContextRole;
import org.olat.modules.quality.analysis.AnalysisSearchParameter;
import org.olat.modules.quality.analysis.AvailableAttributes;
import org.olat.modules.quality.analysis.QualityAnalysisService;
import org.olat.modules.quality.ui.QualityUIFactory;
import org.olat.modules.quality.ui.QualityUIFactory.KeysValues;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
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

	private FormLayoutContainer dateRangeCont;
	private DateChooser dateRangeFromEl;
	private DateChooser dateRangeToEl;
	private TopicIdentitySource topicIdentitySource;
	private AutoCompletionMultiSelection topicIdentityEl;
	private MultipleSelectionElement topicOrganisationEl;
	private MultipleSelectionElement topicCurriculumEl;
	private MultipleSelectionElement topicCurriculumElementEl;
	private TopicRepositoryEntrySource topicRepositoryEntrySource;
	private AutoCompletionMultiSelection topicRepositoryEl;
	private MultipleSelectionElement contextExecutorOrganisationEl;
	private MultipleSelectionElement contextCurriculumEl;
	private ContextCurriculumElementSource contextCurriculumElementSource;
	private AutoCompletionMultiSelection contextCurriculumElementEl;
	private MultipleSelectionElement contextCurriculumElementTypeEl;
	private MultipleSelectionElement contextCurriculumOrganisationEl;
	private MultipleSelectionElement contextTaxonomyLevelEl;
	private MultipleSelectionElement contextLocationEl;
	private MultipleSelectionElement seriesIndexEl;
	private MultipleSelectionElement contextRoleEl;
	private MultipleSelectionElement withUserInformationsEl;
	
	private StaticTextElement dateRangeFromRoEl;
	private StaticTextElement dateRangeToRoEl;
	private StaticTextElement topicIdentityRoEl;
	private StaticTextElement topicOrganisationRoEl;
	private StaticTextElement topicCurriculumRoEl;
	private StaticTextElement topicCurriculumElementRoEl;
	private StaticTextElement topicRepositoryRoEl;
	private StaticTextElement contextExecutorOrganisationRoEl;
	private StaticTextElement contextCurriculumRoEl;
	private StaticTextElement contextCurriculumElementRoEl;
	private StaticTextElement contextCurriculumElementTypeRoEl;
	private StaticTextElement contextCurriculumOrganisationRoEl;
	private StaticTextElement contextTaxonomyLevelRoEl;
	private StaticTextElement contextLocationRoEl;
	private StaticTextElement seriesIndexRoEl;
	private StaticTextElement contextRoleRoEl;
	private StaticTextElement withUserInformationsRoEl;
	private StaticTextElement noFilterSelectedRoEl;

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
		setTranslator(Util.createPackageTranslator(TaxonomyUIFactory.class, getLocale(), getTranslator()));
		this.searchParams = searchParams;
		this.availableAttributes = availableAttributes;
		this.sessionInformationsAvailable = getSessionInformationAvailable(form);
		initForm(ureq);
		setReadOnly(false);
		setSelectionValues();
		initSelection();
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

		dateRangeCont = FormLayoutContainer.createHorizontalFormLayout("dateRange", getTranslator());
		flc.add("dateRange", dateRangeCont);
		dateRangeCont.setElementCssClass("o_date_range");
		dateRangeFromEl = uifactory.addDateChooser("filter.date.range.from", null, dateRangeCont);
		dateRangeFromEl.setElementCssClass("o_date_range_from");
		dateRangeFromEl.addActionListener(FormEvent.ONCHANGE);

		dateRangeToEl = uifactory.addDateChooser("filter.date.range.to", null, dateRangeCont);
		dateRangeToEl.setElementCssClass("o_date_range_to");
		dateRangeToEl.addActionListener(FormEvent.ONCHANGE);
		
		topicIdentitySource = new TopicIdentitySource(analysisService);
		topicIdentityEl = uifactory.addAutoCompletionMultiSelection("filter.topic.identities", formLayout,
				getWindowControl(), topicIdentitySource);
		topicIdentityEl.setSearchPlaceholder(translate("filter.topic.identities.search.placeholder"));

		topicOrganisationEl = uifactory.addCheckboxesDropdown("filter.topic.organisations", formLayout);
		topicOrganisationEl.addActionListener(FormEvent.ONCLICK);

		topicCurriculumEl = uifactory.addCheckboxesDropdown("filter.topic.curriculums", formLayout);
		topicCurriculumEl.addActionListener(FormEvent.ONCLICK);

		topicCurriculumElementEl = uifactory.addCheckboxesDropdown("filter.topic.curriculum.elements", formLayout);
		topicCurriculumElementEl.addActionListener(FormEvent.ONCLICK);

		topicRepositoryEntrySource = new TopicRepositoryEntrySource(analysisService);
		topicRepositoryEl = uifactory.addAutoCompletionMultiSelection("filter.topic.repositories", formLayout,
				getWindowControl(), topicRepositoryEntrySource);
		topicRepositoryEl.setSearchPlaceholder(translate("filter.topic.repositories.search.placeholder"));
		
		contextExecutorOrganisationEl = uifactory.addCheckboxesDropdown("filter.context.organisations", formLayout);
		contextExecutorOrganisationEl.addActionListener(FormEvent.ONCLICK);

		contextCurriculumEl = uifactory.addCheckboxesDropdown("filter.context.curriculums", formLayout);
		contextCurriculumEl.addActionListener(FormEvent.ONCLICK);
		
		contextCurriculumElementSource = new ContextCurriculumElementSource(analysisService);
		contextCurriculumElementEl = uifactory.addAutoCompletionMultiSelection("filter.context.curriculum.elements", formLayout,
				getWindowControl(), contextCurriculumElementSource);
		contextCurriculumElementEl.setSearchPlaceholder(translate("filter.context.curriculum.elements.search.placeholder"));

		contextCurriculumElementTypeEl = uifactory.addCheckboxesDropdown("filter.context.curriculum.element.types", formLayout);
		contextCurriculumElementTypeEl.addActionListener(FormEvent.ONCLICK);

		contextCurriculumOrganisationEl = uifactory.addCheckboxesDropdown("filter.context.curriculum.organisations", formLayout);
		contextCurriculumOrganisationEl.addActionListener(FormEvent.ONCLICK);

		contextTaxonomyLevelEl = uifactory.addCheckboxesDropdown("filter.context.taxonomy.level", formLayout);
		contextTaxonomyLevelEl.addActionListener(FormEvent.ONCLICK);

		contextLocationEl = uifactory.addCheckboxesDropdown("filter.context.location", formLayout);
		contextLocationEl.addActionListener(FormEvent.ONCLICK);

		seriesIndexEl = uifactory.addCheckboxesDropdown("filter.series.index", formLayout);
		seriesIndexEl.addActionListener(FormEvent.ONCLICK);
		
		contextRoleEl = uifactory.addCheckboxesDropdown("filter.context.role", formLayout);
		contextRoleEl.addActionListener(FormEvent.ONCLICK);

		withUserInformationsEl = uifactory.addCheckboxesVertical("filter.with.user.informations.label", formLayout,
				WITH_USER_INFOS_KEYS, translateAll(getTranslator(), WITH_USER_INFOS_KEYS), 1);
		withUserInformationsEl.addActionListener(FormEvent.ONCLICK);
		
		// Read only
		dateRangeFromRoEl = uifactory.addStaticTextElement("filter.date.range.from.ro", "filter.date.range.from", null, formLayout);
		dateRangeToRoEl = uifactory.addStaticTextElement("filter.date.range.to.ro", "filter.date.range.to", null, formLayout);
		topicIdentityRoEl = uifactory.addStaticTextElement("filter.topic.identities.ro", "filter.topic.identities", null, formLayout);
		topicOrganisationRoEl = uifactory.addStaticTextElement("filter.topic.organisations.ro", "filter.topic.organisations", null, formLayout);
		topicCurriculumRoEl = uifactory.addStaticTextElement("filter.topic.curriculums.ro", "filter.topic.curriculums", null, formLayout);
		topicCurriculumElementRoEl = uifactory.addStaticTextElement("filter.topic.curriculum.elements.ro", "filter.topic.curriculum.elements", null, formLayout);
		topicRepositoryRoEl = uifactory.addStaticTextElement("filter.topic.repositories.ro", "filter.topic.repositories", null, formLayout);
		contextExecutorOrganisationRoEl = uifactory.addStaticTextElement("filter.context.organisations.ro", "filter.context.organisations", null, formLayout);
		contextCurriculumRoEl = uifactory.addStaticTextElement("filter.context.curriculums.ro", "filter.context.curriculums", null, formLayout);
		contextCurriculumElementRoEl = uifactory.addStaticTextElement("filter.context.curriculum.elements.ro", "filter.context.curriculum.elements", null, formLayout);
		contextCurriculumElementTypeRoEl = uifactory.addStaticTextElement("filter.context.curriculum.element.types.ro", "filter.context.curriculum.element.types", null, formLayout);
		contextCurriculumOrganisationRoEl = uifactory.addStaticTextElement("filter.context.curriculum.organisations.ro", "filter.context.curriculum.organisations", null, formLayout);
		contextTaxonomyLevelRoEl = uifactory.addStaticTextElement("filter.context.taxonomy.level.ro", "filter.context.taxonomy.level", null, formLayout);
		contextLocationRoEl = uifactory.addStaticTextElement("filter.context.location.ro", "filter.context.location", null, formLayout);
		seriesIndexRoEl = uifactory.addStaticTextElement("filter.series.index.ro", "filter.series.index", null, formLayout);
		contextRoleRoEl = uifactory.addStaticTextElement("filter.context.role.ro", "filter.context.role", null, formLayout);
		withUserInformationsRoEl = uifactory.addStaticTextElement("filter.with.user.informations.label.ro", "filter.with.user.informations.label", null, formLayout);
		noFilterSelectedRoEl = uifactory.addStaticTextElement("filter.no.selected.ro", "filter.no.selected.label", translate("filter.no.selected.ro"), formLayout);
	}
	
	void setReadOnly(boolean readOnly) {
		// selection
		boolean selection = !readOnly;
		dateRangeCont.setVisible(selection);
		dateRangeFromEl.setVisible(selection);
		dateRangeToEl.setVisible(selection);
		topicIdentityEl.setVisible(selection && availableAttributes.isTopicIdentity());
		topicOrganisationEl.setVisible(selection && availableAttributes.isTopicOrganisation() && organisationModule.isEnabled());
		topicCurriculumEl.setVisible(selection && availableAttributes.isTopicCurriculum() && curriculumModule.isEnabled());
		topicCurriculumElementEl.setVisible(selection && availableAttributes.isTopicCurriculumElement() && curriculumModule.isEnabled());
		topicRepositoryEl.setVisible(selection && availableAttributes.isTopicRepository());
		contextExecutorOrganisationEl.setVisible(selection && availableAttributes.isContextExecutorOrganisation() && organisationModule.isEnabled());
		contextCurriculumEl.setVisible(selection && availableAttributes.isContextCurriculum() && curriculumModule.isEnabled());
		contextCurriculumElementEl.setVisible(selection && availableAttributes.isContextCurriculumElement() && curriculumModule.isEnabled());
		contextCurriculumElementTypeEl.setVisible(selection && availableAttributes.isContextCurriculumElementType() && curriculumModule.isEnabled());
		contextCurriculumOrganisationEl.setVisible(selection && availableAttributes.isContextCurriculumOrganisation()
				&& organisationModule.isEnabled() && curriculumModule.isEnabled());
		contextTaxonomyLevelEl.setVisible(selection && availableAttributes.isContextTaxonomyLevel());
		contextLocationEl.setVisible(selection && availableAttributes.isContextLocation());
		seriesIndexEl.setVisible(selection && availableAttributes.isSeriesIndex());
		contextRoleEl.setVisible(selection);
		withUserInformationsEl.setVisible(selection && sessionInformationsAvailable);
		
		// read only
		showReadOnly(readOnly, dateRangeFromRoEl, dateRangeFromEl);
		showReadOnly(readOnly, dateRangeToRoEl, dateRangeToEl);
		showReadOnly(readOnly, topicIdentityRoEl, topicIdentityEl);
		showReadOnly(readOnly, topicOrganisationRoEl, topicOrganisationEl);
		showReadOnly(readOnly, topicCurriculumRoEl, topicCurriculumEl);
		showReadOnly(readOnly, topicCurriculumElementRoEl, topicCurriculumElementEl);
		showReadOnly(readOnly, topicRepositoryRoEl, topicRepositoryEl);
		showReadOnly(readOnly, contextExecutorOrganisationRoEl, contextExecutorOrganisationEl);
		showReadOnly(readOnly, contextCurriculumRoEl, contextCurriculumEl);
		showReadOnly(readOnly, contextCurriculumElementRoEl, contextCurriculumElementEl);
		showReadOnly(readOnly, contextCurriculumElementTypeRoEl, contextCurriculumElementTypeEl);
		showReadOnly(readOnly, contextCurriculumOrganisationRoEl, contextCurriculumOrganisationEl);
		showReadOnly(readOnly, contextTaxonomyLevelRoEl, contextTaxonomyLevelEl);
		showReadOnly(readOnly, contextLocationRoEl, contextLocationEl);
		showReadOnly(readOnly, seriesIndexRoEl, seriesIndexEl);
		showReadOnly(readOnly, contextRoleRoEl, contextRoleEl);
		
		if (readOnly && withUserInformationsEl.isAtLeastSelected(1)) {
			String value = withUserInformationsEl.getSelectedValues().get(0);
			withUserInformationsRoEl.setValue(value);
			withUserInformationsRoEl.setVisible(true);
		} else {
			withUserInformationsRoEl.setVisible(false);
		}
		
		noFilterSelectedRoEl.setVisible(readOnly && !isAtLeastOneRoVisible());
	}

	private void showReadOnly(boolean readOnly, StaticTextElement roEl, DateChooser dateEl) {
		if (readOnly && dateEl.getDate() != null) {
			String value = Formatter.getInstance(getLocale()).formatDate(dateEl.getDate());
			roEl.setValue(value);
			roEl.setVisible(true);
		} else {
			roEl.setVisible(false);
		}
	}
	
	private void showReadOnly(boolean readOnly, StaticTextElement roEl, MultipleSelectionElement selectEl) {
		if (readOnly && selectEl.isAtLeastSelected(1)) {
			List<String> selectedValues = selectEl.getSelectedValues();
			String value = QualityUIFactory.toHtmlList(selectedValues);
			roEl.setValue(value);
			roEl.setVisible(true);
		} else {
			roEl.setVisible(false);
		}
	}
	
	private void showReadOnly(boolean readOnly, StaticTextElement roEl, AutoCompletionMultiSelection selectEl) {
		if (readOnly && selectEl.getSelectedKeysSize() > 0) {
			SelectionValues selection = selectEl.getSelection();
			String value = QualityUIFactory.toHtmlList(selection);
			roEl.setValue(value);
			roEl.setVisible(true);
		} else {
			roEl.setVisible(false);
		}
	}
	
	private boolean isAtLeastOneRoVisible() {
		return dateRangeFromRoEl.isVisible()
			|| dateRangeToRoEl.isVisible()
			|| topicIdentityRoEl.isVisible()
			|| topicOrganisationRoEl.isVisible()
			|| topicCurriculumRoEl.isVisible()
			|| topicCurriculumElementRoEl.isVisible()
			|| topicRepositoryRoEl.isVisible()
			|| contextExecutorOrganisationRoEl.isVisible()
			|| contextCurriculumRoEl.isVisible()
			|| contextCurriculumElementRoEl.isVisible()
			|| contextCurriculumElementTypeRoEl.isVisible()
			|| contextCurriculumOrganisationRoEl.isVisible()
			|| contextTaxonomyLevelRoEl.isVisible()
			|| contextLocationRoEl.isVisible()
			|| seriesIndexRoEl.isVisible()
			|| contextRoleRoEl.isVisible()
			|| withUserInformationsRoEl.isVisible()
			|| noFilterSelectedRoEl.isVisible();
	}

	private void setSelectionValues() {
		setTopicIdentityValues();
		setTopicOrganisationValues();
		setTopicCurriculumValues();
		setTopicCurriculumElementValues();
		setTopicRepositoryValues();
		setContextExecutorOrganisationValues();
		setContextCurriculumValues();
		setContextCurriculumElementValues();
		setContextCurriculumElementTypeValues();
		setContextCurriculumOrganisationValues();
		setContextTaxonomyLevelValues();
		setContextLocationValues();
		setSeriesIndexValues();
		setContextRoleValues();
	}
	
	private void initSelection() {
		initDateRangeSelection();
		initTopicIdentitySelection();
		initTopicOrganisationSelection();
		initTopicCurriculumSelection();
		initTopicCurriculumElementSelection();
		initTopicRepositorySelection();
		initContextExecutorOrganisationSelection();
		initContextCurriculumSelection();
		initContextCurriculumElementSelection();
		initContextCurriculumElementTypeSelection();
		initContextCurriculumOrganisationSelection();
		initContextTaxonomyLevelSelection();
		initContextLocationSelection();
		initSeriesIndexSelection();
		initContextRoleSelection();
		initWithUserInfosOnlySelection();
	}

	private void initDateRangeSelection() {
		if (dateRangeFromEl.isVisible() && searchParams.getDateRangeFrom() != null) {
			dateRangeFromEl.setDate(searchParams.getDateRangeFrom());
		}
		if (dateRangeToEl.isVisible() && searchParams.getDateRangeTo() != null) {
			dateRangeToEl.setDate(searchParams.getDateRangeTo());
		}
	}

	private void setTopicIdentityValues() {
		if (!topicIdentityEl.isVisible()) return;

		Collection<String> selectedKeys = topicIdentityEl.getSelectedKeys();
		
		AnalysisSearchParameter searchParamsClone = searchParams.clone();
		searchParamsClone.setTopicIdentityRefs(null);
		topicIdentitySource.setSearchParams(searchParamsClone);
		
		topicIdentityEl.setSelectedKeys(selectedKeys);
	}
	
	private void initTopicIdentitySelection() {
		if (!topicIdentityEl.isVisible()) return;
		
		Collection<? extends IdentityRef> topicIdentityRefs = searchParams.getTopicIdentityRefs();
		if (topicIdentityRefs != null && !topicIdentityRefs.isEmpty()) {
			List<String> selectedKeys = topicIdentityRefs.stream()
					.map(QualityUIFactory::getIdentityKey)
					.collect(Collectors.toList());
			topicIdentityEl.setSelectedKeys(selectedKeys);
		}
	}

	private void setTopicOrganisationValues() {
		if (!topicOrganisationEl.isVisible()) return;

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

	private void initTopicOrganisationSelection() {
		if (!topicOrganisationEl.isVisible()) return;
		
		Collection<? extends OrganisationRef> topicOrganisationRefs = searchParams.getTopicOrganisationRefs();
		if (topicOrganisationRefs != null && !topicOrganisationRefs.isEmpty()) {
			Set<String> keys = topicOrganisationEl.getKeys();
			for (OrganisationRef organisationRef : topicOrganisationRefs) {
				String key = QualityUIFactory.getOrganisationKey(organisationRef);
				if (keys.contains(key)) {
					topicOrganisationEl.select(key, true);
				}
			}
		}
	}

	private void setTopicCurriculumValues() {
		if (!topicCurriculumEl.isVisible()) return;

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
	
	private void initTopicCurriculumSelection() {
		if (!topicCurriculumEl.isVisible()) return;
		
		Collection<? extends CurriculumRef> topicCurriculumRefs = searchParams.getTopicCurriculumRefs();
		if (topicCurriculumRefs != null && !topicCurriculumRefs.isEmpty()) {
			Set<String> keys = topicCurriculumEl.getKeys();
			for (CurriculumRef curriculumRef : topicCurriculumRefs) {
				String key = QualityUIFactory.getCurriculumKey(curriculumRef);
				if (keys.contains(key)) {
					topicCurriculumEl.select(key, true);
				}
			}
		}
	}

	private void setTopicCurriculumElementValues() {
		if (!topicCurriculumElementEl.isVisible()) return;

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
	
	private void initTopicCurriculumElementSelection() {
		if (!topicCurriculumElementEl.isVisible()) return;
		
		Collection<? extends CurriculumElementRef> topicCurriculumElementRefs = searchParams.getTopicCurriculumElementRefs();
		if (topicCurriculumElementRefs != null && !topicCurriculumElementRefs.isEmpty()) {
			Set<String> keys = topicCurriculumElementEl.getKeys();
			for (CurriculumElementRef curriculumElementRef : topicCurriculumElementRefs) {
				String key = QualityUIFactory.getCurriculumElementKey(curriculumElementRef);
				if (keys.contains(key)) {
					topicCurriculumElementEl.select(key, true);
				}
			}
		}
	}

	private void setTopicRepositoryValues() {
		if (!topicRepositoryEl.isVisible()) return;
		
		Collection<String> selectedKeys = topicRepositoryEl.getSelectedKeys();
		
		AnalysisSearchParameter searchParamsClone = searchParams.clone();
		searchParamsClone.setTopicRepositoryRefs(null);
		topicRepositoryEntrySource.setSearchParams(searchParamsClone);
		
		topicRepositoryEl.setSelectedKeys(selectedKeys);
	}
	
	private void initTopicRepositorySelection() {
		if (!topicRepositoryEl.isVisible()) return;
		
		Collection<? extends RepositoryEntryRef> topicRepositoryRefs = searchParams.getTopicRepositoryRefs();
		if (topicRepositoryRefs != null && !topicRepositoryRefs.isEmpty()) {
			List<String> selectedKeys = topicRepositoryRefs.stream()
					.map(QualityUIFactory::getRepositoryEntryKey)
					.collect(Collectors.toList());
			topicRepositoryEl.setSelectedKeys(selectedKeys);
		}
	}

	private void setContextExecutorOrganisationValues() {
		if (!contextExecutorOrganisationEl.isVisible()) return;

		Collection<String> selectedKeys = contextExecutorOrganisationEl.getSelectedKeys();

		AnalysisSearchParameter searchParamsClone = searchParams.clone();
		searchParamsClone.setContextOrganisationRefs(null);
		List<Organisation> organisations = analysisService.loadContextExecutorOrganisations(searchParamsClone, true);
		OrganisationTreeModel organisationModel = new OrganisationTreeModel();
		organisationModel.loadTreeModel(organisations);

		KeysValues keysValues = QualityUIFactory.getOrganisationKeysValues(organisationModel, null);
		contextExecutorOrganisationEl.setKeysAndValues(keysValues.getKeys(), keysValues.getValues());
		for (String key : selectedKeys) {
			contextExecutorOrganisationEl.select(key, true);
		}
	}
	
	private void initContextExecutorOrganisationSelection() {
		if (!contextExecutorOrganisationEl.isVisible()) return;
		
		Collection<? extends OrganisationRef> contextExecutorOrganisationRefs = searchParams.getContextOrganisationRefs();
		if (contextExecutorOrganisationRefs != null && !contextExecutorOrganisationRefs.isEmpty()) {
			Set<String> keys = contextExecutorOrganisationEl.getKeys();
			for (OrganisationRef executorOrganisationRef : contextExecutorOrganisationRefs) {
				String key = QualityUIFactory.getOrganisationKey(executorOrganisationRef);
				if (keys.contains(key)) {
					contextExecutorOrganisationEl.select(key, true);
				}
			}
		}
	}

	private void setContextCurriculumValues() {
		if (!contextCurriculumEl.isVisible()) return;

		Collection<String> selectedKeys = contextCurriculumEl.getSelectedKeys();

		AnalysisSearchParameter curriculumSearchParams = searchParams.clone();
		curriculumSearchParams.setContextCurriculumRefs(null);
		List<Curriculum> curriculums = analysisService.loadContextCurriculums(curriculumSearchParams);
		KeysValues keysValues = QualityUIFactory.getCurriculumKeysValues(curriculums, null);
		contextCurriculumEl.setKeysAndValues(keysValues.getKeys(), keysValues.getValues());
		for (String key : selectedKeys) {
			contextCurriculumEl.select(key, true);
		}
	}
	
	private void initContextCurriculumSelection() {
		if (!contextCurriculumEl.isVisible()) return;
		
		Collection<? extends CurriculumRef> contextCurriculumRefs = searchParams.getContextCurriculumRefs();
		if (contextCurriculumRefs != null && !contextCurriculumRefs.isEmpty()) {
			Set<String> keys = contextCurriculumEl.getKeys();
			for (CurriculumRef curriculumRef : contextCurriculumRefs) {
				String key = QualityUIFactory.getCurriculumKey(curriculumRef);
				if (keys.contains(key)) {
					contextCurriculumEl.select(key, true);
				}
			}
		}
	}
	
	private void setContextCurriculumElementValues() {
		if (!contextCurriculumElementEl.isVisible()) return;

		Collection<String> selectedKeys = contextCurriculumElementEl.getSelectedKeys();
		
		AnalysisSearchParameter curriculumElementSearchParams = searchParams.clone();
		Collection<String> curriculumKeys = contextCurriculumEl.isAtLeastSelected(1)
				? contextCurriculumEl.getSelectedKeys()
				: contextCurriculumEl.getKeys();
		List<? extends CurriculumRef> curriculumRefs = curriculumKeys.stream()
				.map(QualityUIFactory::getCurriculumRef).collect(toList());
		curriculumElementSearchParams.setContextCurriculumRefs(curriculumRefs);
		curriculumElementSearchParams.setContextCurriculumElementRefs(null);
		contextCurriculumElementSource.setSearchParams(curriculumElementSearchParams);
		
		contextCurriculumElementEl.setSelectedKeys(selectedKeys);
	}
	
	private void initContextCurriculumElementSelection() {
		if (!contextCurriculumElementEl.isVisible()) return;
		
		Collection<? extends CurriculumElementRef> contextCurriculumElementRefs = searchParams.getContextCurriculumElementRefs();
		if (contextCurriculumElementRefs != null && !contextCurriculumElementRefs.isEmpty()) {
			List<String> selectedKeys = contextCurriculumElementRefs.stream()
					.map(QualityUIFactory::getCurriculumElementKey)
					.collect(Collectors.toList());
			contextCurriculumElementEl.setSelectedKeys(selectedKeys);
		}
	}

	private void setContextCurriculumElementTypeValues() {
		if (!contextCurriculumElementTypeEl.isVisible()) return;

		Collection<String> selectedKeys = contextCurriculumElementTypeEl.getSelectedKeys();

		AnalysisSearchParameter clonedSearchParams = searchParams.clone();
		Collection<String> curriculumKeys = contextCurriculumEl.isAtLeastSelected(1)
				? contextCurriculumEl.getSelectedKeys()
				: contextCurriculumEl.getKeys();
		List<? extends CurriculumRef> curriculumRefs = curriculumKeys.stream()
				.map(QualityUIFactory::getCurriculumRef).collect(toList());
		clonedSearchParams.setContextCurriculumRefs(curriculumRefs);
		clonedSearchParams.setContextCurriculumElementTypeRefs(null);
		List<CurriculumElementType> curriculumElementTypes = analysisService
				.loadContextCurriculumElementTypes(clonedSearchParams);

		KeysValues keysValues = QualityUIFactory.getCurriculumElementTypeKeysValues(curriculumElementTypes);
		contextCurriculumElementTypeEl.setKeysAndValues(keysValues.getKeys(), keysValues.getValues());
		for (String key : selectedKeys) {
			contextCurriculumElementTypeEl.select(key, true);
		}
	}
	
	private void initContextCurriculumElementTypeSelection() {
		if (!contextCurriculumElementTypeEl.isVisible()) return;
		
		Collection<? extends CurriculumElementTypeRef> contextCurriculumElementTypeRefs = searchParams.getContextCurriculumElementTypeRefs();
		if (contextCurriculumElementTypeRefs != null && !contextCurriculumElementTypeRefs.isEmpty()) {
			Set<String> keys = contextCurriculumElementTypeEl.getKeys();
			for (CurriculumElementTypeRef curriculumElementTypeRef : contextCurriculumElementTypeRefs) {
				String key = QualityUIFactory.getCurriculumElementTypeKey(curriculumElementTypeRef);
				if (keys.contains(key)) {
					contextCurriculumElementTypeEl.select(key, true);
				}
			}
		}
	}

	private void setContextCurriculumOrganisationValues() {
		if (!contextCurriculumOrganisationEl.isVisible()) return;

		Collection<String> selectedKeys = contextCurriculumOrganisationEl.getSelectedKeys();

		AnalysisSearchParameter searchParamsClone = searchParams.clone();
		searchParamsClone.setContextCurriculumOrganisationRefs(null);
		searchParamsClone.setContextCurriculumRefs(null);
		searchParamsClone.setContextCurriculumElementRefs(null);
		searchParamsClone.setContextCurriculumElementTypeRefs(null);
		List<Organisation> organisations = analysisService.loadContextCurriculumOrganisations(searchParamsClone, true);
		OrganisationTreeModel organisationModel = new OrganisationTreeModel();
		organisationModel.loadTreeModel(organisations);

		KeysValues keysValues = QualityUIFactory.getOrganisationKeysValues(organisationModel, null);
		contextCurriculumOrganisationEl.setKeysAndValues(keysValues.getKeys(), keysValues.getValues());
		for (String key : selectedKeys) {
			contextCurriculumOrganisationEl.select(key, true);
		}
	}
	
	private void initContextCurriculumOrganisationSelection() {
		if (!contextCurriculumOrganisationEl.isVisible()) return;
		
		Collection<? extends OrganisationRef> contextCurriculumOrganisationRefs = searchParams.getContextCurriculumOrganisationRefs();
		if (contextCurriculumOrganisationRefs != null && !contextCurriculumOrganisationRefs.isEmpty()) {
			Set<String> keys = contextCurriculumOrganisationEl.getKeys();
			for (OrganisationRef curriculumOrganisationRef : contextCurriculumOrganisationRefs) {
				String key = QualityUIFactory.getOrganisationKey(curriculumOrganisationRef);
				if (keys.contains(key)) {
					contextCurriculumOrganisationEl.select(key, true);
				}
			}
		}
	}
	
	private void setContextTaxonomyLevelValues() {
		if (!contextTaxonomyLevelEl.isVisible()) return;

		Collection<String> selectedKeys = contextTaxonomyLevelEl.getSelectedKeys();

		AnalysisSearchParameter searchParamsClone = searchParams.clone();
		searchParamsClone.setContextTaxonomyLevelRefs(null);
		List<TaxonomyLevel> levels = analysisService.loadContextTaxonomyLevels(searchParamsClone, true);

		SelectionValues keyValues = new SelectionValues();
		// Create the key / value pairs and sort them according to the hierarchical
		// structure.
		for (TaxonomyLevel level : levels) {
			String key = Long.toString(level.getKey());
			ArrayList<String> names = new ArrayList<>();
			QualityUIFactory.addParentTaxonomyLevelNames(getTranslator(), names, level);
			Collections.reverse(names);
			String value = String.join(" / ", names);
			keyValues.add(entry(key, value));
		}
		keyValues.sort(VALUE_ASC);

		// Replace with the intended value (but keep the sort order).
		for (TaxonomyLevel level : levels) {
			String key = QualityUIFactory.getTaxonomyLevelKey(level);
			String intendedLevel = QualityUIFactory.getIntendedTaxonomyLevel(getTranslator(), level);
			keyValues.replaceOrPut(entry(key, intendedLevel));
		}

		contextTaxonomyLevelEl.setKeysAndValues(keyValues.keys(), keyValues.values());
		for (String key : selectedKeys) {
			contextTaxonomyLevelEl.select(key, true);
		}
	}
	
	private void initContextTaxonomyLevelSelection() {
		if (!contextTaxonomyLevelEl.isVisible()) return;
		
		Collection<? extends TaxonomyLevelRef> contextTaxonomyLevelRefs = searchParams.getContextTaxonomyLevelRefs();
		if (contextTaxonomyLevelRefs != null && !contextTaxonomyLevelRefs.isEmpty()) {
			Set<String> keys = contextTaxonomyLevelEl.getKeys();
			for (TaxonomyLevelRef taxonomyLevelRef : contextTaxonomyLevelRefs) {
				String key = QualityUIFactory.getTaxonomyLevelKey(taxonomyLevelRef);
				if (keys.contains(key)) {
					contextTaxonomyLevelEl.select(key, true);
				}
			}
		}
	}

	private void setContextLocationValues() {
		if (!contextLocationEl.isVisible()) return;

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
	
	private void initContextLocationSelection() {
		if (!contextLocationEl.isVisible()) return;
		
		Collection<String> contextLocations = searchParams.getContextLocations();
		if (contextLocations != null && !contextLocations.isEmpty()) {
			Set<String> keys = contextLocationEl.getKeys();
			for (String key : contextLocations) {
				if (keys.contains(key)) {
					contextLocationEl.select(key, true);
				}
			}
		}
	}

	private void setSeriesIndexValues() {
		if (!seriesIndexEl.isVisible()) return;

		Collection<String> selectedKeys = seriesIndexEl.getSelectedKeys();

		AnalysisSearchParameter clonedSearchParams = searchParams.clone();
		clonedSearchParams.setSeriesIndexes(null);
		Integer maxSerieIndex = analysisService.loadMaxSeriesIndex(clonedSearchParams);
		maxSerieIndex = maxSerieIndex != null? maxSerieIndex: 0;
		SelectionValues keyValues = new SelectionValues();
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
	
	private void initSeriesIndexSelection() {
		if (!seriesIndexEl.isVisible()) return;
		
		Collection<Integer> seriesIndexes = searchParams.getSeriesIndexes();
		if (seriesIndexes != null && !seriesIndexes.isEmpty()) {
			Set<String> keys = seriesIndexEl.getKeys();
			for (Integer seriesIndex : seriesIndexes) {
				String key = String.valueOf(seriesIndex);
				if (keys.contains(key)) {
					seriesIndexEl.select(key, true);
				}
			}
		}
	}

	private void setContextRoleValues() {
		Collection<String> selectedKeys = contextRoleEl.getSelectedKeys();

		AnalysisSearchParameter clonedSearchParams = searchParams.clone();
		clonedSearchParams.setContextRoles(null);
		List<QualityContextRole> roles = analysisService.loadContextRoles(clonedSearchParams);
		SelectionValues kv = new SelectionValues();
		for (QualityContextRole role: QualityContextRole.values()) {
			if (roles.contains(role)) {
				kv.add(entry(role.name(), translateRole(getTranslator(), role)));
			}
		}
		contextRoleEl.setKeysAndValues(kv.keys(), kv.values());
		for (String key : selectedKeys) {
			contextRoleEl.select(key, true);
		}
	}

	private void initContextRoleSelection() {
		if (!contextRoleEl.isVisible()) return;
		
		Collection<QualityContextRole> contextRoles = searchParams.getContextRoles();
		if (contextRoles != null && !contextRoles.isEmpty()) {
			Set<String> keys = contextRoleEl.getKeys();
			for (QualityContextRole contextRole : contextRoles) {
				String key = contextRole.name();
				if (keys.contains(key)) {
					contextRoleEl.select(key, true);
				}
			}
		}
	}

	private void initWithUserInfosOnlySelection() {
		if (withUserInformationsEl.isVisible() && searchParams.isWithUserInfosOnly()) {
			withUserInformationsEl.select(withUserInformationsEl.getKey(0), true);
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
		} else if (source == contextExecutorOrganisationEl) {
			doFiltered(ureq);
		} else if (source == contextCurriculumEl) {
			doFiltered(ureq);
		} else if (source == contextCurriculumElementEl) {
			doFiltered(ureq);
		} else if (source == contextCurriculumElementTypeEl) {
			doFiltered(ureq);
		} else if (source == contextCurriculumOrganisationEl) {
			doFiltered(ureq);
		} else if (source == contextTaxonomyLevelEl) {
			doFiltered(ureq);
		} else if (source == contextLocationEl) {
			doFiltered(ureq);
		} else if (source == seriesIndexEl) {
			doFiltered(ureq);
		} else if (source == contextRoleEl) {
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
		getSearchParamContextExecutorOrganisations();
		getSearchParamContextCurriculums();
		getSearchParamContextCurriculumElements();
		getSearchParamContextCurriculumElementTypes();
		getSearchParamContextCurriculumOrganisations();
		getSearchParamContextTaxonomyLevels();
		getSearchParamContextLocations();
		getSearchParamSeriesIndex();
		getSearchParamContextRole();
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
		if (topicIdentityEl.isVisible() && topicIdentityEl.getSelectedKeysSize() > 0) {
			List<IdentityRef> identityRefs = topicIdentityEl.getSelectedKeys().stream()
					.map(QualityUIFactory::getIdentityRef).collect(toList());
			searchParams.setTopicIdentityRefs(identityRefs);
		} else {
			searchParams.setTopicIdentityRefs(null);
		}
	}

	private void getSearchParamTopicOrganisations() {
		if (topicOrganisationEl.isVisible() && topicOrganisationEl.isAtLeastSelected(1)) {
			List<OrganisationRef> organisationRefs = topicOrganisationEl.getSelectedKeys().stream()
					.map(QualityUIFactory::getOrganisationRef).collect(toList());
			searchParams.setTopicOrganisationRefs(organisationRefs);
		} else {
			searchParams.setTopicOrganisationRefs(null);
		}
	}

	private void getSearchParamTopicCurriculums() {
		if (topicCurriculumEl.isVisible() && topicCurriculumEl.isAtLeastSelected(1)) {
			Collection<CurriculumRef> curriculumRefs = topicCurriculumEl.getSelectedKeys().stream()
					.map(QualityUIFactory::getCurriculumRef).collect(toList());
			searchParams.setTopicCurriculumRefs(curriculumRefs);
		} else {
			searchParams.setTopicCurriculumRefs(null);
		}
	}

	private void getSearchParamTopicCurriculumElements() {
		if (topicCurriculumElementEl.isVisible() && topicCurriculumElementEl.isAtLeastSelected(1)) {
			List<CurriculumElementRef> curriculumElementRefs = topicCurriculumElementEl.getSelectedKeys().stream()
					.map(QualityUIFactory::getCurriculumElementRef).collect(toList());
			searchParams.setTopicCurriculumElementRefs(curriculumElementRefs);
		} else {
			searchParams.setTopicCurriculumElementRefs(null);
		}
	}

	private void getSearchParamTopicRepositorys() {
		if (topicRepositoryEl.isVisible() && topicRepositoryEl.getSelectedKeysSize() > 0) {
			List<RepositoryEntryRef> entryRefs = topicRepositoryEl.getSelectedKeys().stream()
					.map(QualityUIFactory::getRepositoryEntryRef).collect(toList());
			searchParams.setTopicRepositoryRefs(entryRefs);
		} else {
			searchParams.setTopicRepositoryRefs(null);
		}
	}

	private void getSearchParamContextExecutorOrganisations() {
		if (contextExecutorOrganisationEl.isVisible() && contextExecutorOrganisationEl.isAtLeastSelected(1)) {
			List<OrganisationRef> organisationRefs = contextExecutorOrganisationEl.getSelectedKeys().stream()
					.map(QualityUIFactory::getOrganisationRef).collect(toList());
			searchParams.setContextOrganisationRefs(organisationRefs);
		} else {
			searchParams.setContextOrganisationRefs(null);
		}
	}

	private void getSearchParamContextCurriculums() {
		if (contextCurriculumEl.isVisible() && contextCurriculumEl.isAtLeastSelected(1)) {
			Collection<CurriculumRef> curriculumRefs = contextCurriculumEl.getSelectedKeys().stream()
					.map(QualityUIFactory::getCurriculumRef).collect(toList());
			searchParams.setContextCurriculumRefs(curriculumRefs);
		} else {
			searchParams.setContextCurriculumRefs(null);
		}
	}

	private void getSearchParamContextCurriculumElements() {
		if (contextCurriculumElementEl.isVisible() && contextCurriculumElementEl.getSelectedKeysSize() > 0) {
			List<CurriculumElementRef> curriculumElementRefs = contextCurriculumElementEl.getSelectedKeys().stream()
					.map(QualityUIFactory::getCurriculumElementRef).collect(toList());
			searchParams.setContextCurriculumElementRefs(curriculumElementRefs);
		} else {
			searchParams.setContextCurriculumElementRefs(null);
		}
	}
	
	private void getSearchParamContextCurriculumElementTypes() {
		if (contextCurriculumElementTypeEl.isVisible() && contextCurriculumElementTypeEl.isAtLeastSelected(1)) {
			List<CurriculumElementTypeRef> curriculumElementTypeRefs = contextCurriculumElementTypeEl.getSelectedKeys().stream()
					.map(QualityUIFactory::getCurriculumElementTypeRef).collect(toList());
			searchParams.setContextCurriculumElementTypeRefs(curriculumElementTypeRefs);
		} else {
			searchParams.setContextCurriculumElementTypeRefs(null);
		}
	}
	
	private void getSearchParamContextCurriculumOrganisations() {
		if (contextCurriculumOrganisationEl.isVisible() && contextCurriculumOrganisationEl.isAtLeastSelected(1)) {
			List<OrganisationRef> organisationRefs = contextCurriculumOrganisationEl.getSelectedKeys().stream()
					.map(QualityUIFactory::getOrganisationRef).collect(toList());
			searchParams.setContextCurriculumOrganisationRefs(organisationRefs);
		} else {
			searchParams.setContextCurriculumOrganisationRefs(null);
		}
	}

	private void getSearchParamContextTaxonomyLevels() {
		if (contextTaxonomyLevelEl.isVisible() && contextTaxonomyLevelEl.isAtLeastSelected(1)) {
			List<TaxonomyLevelRef> curriculumElementRefs = contextTaxonomyLevelEl.getSelectedKeys().stream()
					.map(QualityUIFactory::getTaxonomyLevelRef).collect(toList());
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
	
	private void getSearchParamContextRole() {
		if (contextRoleEl.isVisible() && contextRoleEl.isAtLeastSelected(1)) {
			Collection<QualityContextRole> roles = contextRoleEl.getSelectedKeys().stream()
					.map(QualityContextRole::valueOf)
					.collect(toList());
			searchParams.setContextRoles(roles);
		} else {
			searchParams.setContextRoles(null);
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
}

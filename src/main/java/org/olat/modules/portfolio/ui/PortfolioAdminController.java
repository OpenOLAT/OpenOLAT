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
package org.olat.modules.portfolio.ui;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import org.olat.collaboration.CollaborationToolsFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyService;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * 
 * Description:<br>
 * allows to admin the ePortfolio-module
 * 
 * <P>
 * Initial Date:  21.07.2010 <br>
 * @author: srosse
 */
public class PortfolioAdminController extends FormBasicController  {
	
	private static final String BINDER_CREATE_LEARNER = "portfolio.user.can.create.binder";
	private static final String BINDER_CREATE_TEMPLATE = "portfolio.user.can.create.binder.template";
	private static final String BINDER_CREATE_COURSE = "portfolio.user.can.create.binder.course";
	private static final String[] BINDER_CREATE_KEYS = new String[] {
			BINDER_CREATE_LEARNER,
			BINDER_CREATE_TEMPLATE,
			BINDER_CREATE_COURSE
	};
	private static final String SECTION_OVERVIEW_ENABLED = "section.overview.enabled";
	private static final String SECTION_ENTRIES_ENABLED = "section.entries.enabled";
	private static final String SECTION_HISTORY_ENABLED = "section.history.enabled";
	private static final String[] SECTION_VISIBILITY_KEYS = new String[] {
			SECTION_OVERVIEW_ENABLED,
			SECTION_ENTRIES_ENABLED,
			SECTION_HISTORY_ENABLED
	};
	private static final String ENTRIES_SEARCH_ENABLED = "entries.search.enabled";
	private static final String ENTRIES_TIMELINE_ENABLED = "entries.timeline.enabled";
	private static final String[] ENTRIES_ELEMENTS_KEYS = new String[] {
			ENTRIES_SEARCH_ENABLED,
			ENTRIES_TIMELINE_ENABLED
	};
	private static final String COMMENTS_OVERVIEW_ENABLED = "comments.overview.enabled";
	private static final String COMMENTS_ENTRIES_ENABLED = "comments.entries.enabled";
	private static final String[] COMMENTS_KEYS = new String[] {
			COMMENTS_OVERVIEW_ENABLED,
			COMMENTS_ENTRIES_ENABLED
	};
	private static final String ENTRIES_BOTH_ENABLED = "entries.both.enabled";
	private static final String ENTRIES_LIST_ENABLED = "entries.list.enabled";
	private static final String ENTRIES_TABLE_ENABLED = "entries.table.enabled";
	private static final String[] ENTRIES_VIEW_KEYS = new String[] {
			ENTRIES_TABLE_ENABLED,
			ENTRIES_LIST_ENABLED,
			ENTRIES_BOTH_ENABLED
	};

	private static String[] enabledKeys = new String[]{ "on" };

	private SingleSelection entryPointEl;
	private MultipleSelectionElement portfolioEnabled;
	private MultipleSelectionElement createBinderEl;
	private MultipleSelectionElement sectionVisibilityEl;
	private MultipleSelectionElement entryElementsVisibilityEl;
	private MultipleSelectionElement commentsVisibilityEl;
	private SingleSelection entriesViewEl;
	private MultipleSelectionElement taxonomyLinkingEnbaledEl;
	private MultipleSelectionElement linkedTaxonomiesEl;

	@Autowired
	private PortfolioV2Module portfolioV2Module;
	@Autowired
	private TaxonomyService taxonomyService;
	
	public PortfolioAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("portfolio.title");
		setFormContextHelp("manual_admin/administration/eAssessment_ePortfolio/");

		String[] enabledValues = new String[] { translate("enabled")};
		
		portfolioEnabled = uifactory.addCheckboxesVertical("portfolio.module.enabled", formLayout, enabledKeys, enabledValues, 1);
		if(portfolioV2Module.isEnabled()) {
			portfolioEnabled.select(enabledKeys[0], true);
		}
		portfolioEnabled.addActionListener(FormEvent.ONCHANGE);

		createBinderEl = uifactory.addCheckboxesVertical("portfolio.user.create.binder", formLayout, BINDER_CREATE_KEYS,
				translateKeys(BINDER_CREATE_KEYS), 1);
		createBinderEl.addActionListener(FormEvent.ONCHANGE);
		
		String[] entryPointKeys = new String[] { PortfolioV2Module.ENTRY_POINT_TOC, PortfolioV2Module.ENTRY_POINT_ENTRIES };
		String[] entryPointValues = new String[]{ translate("binder.entry.point.toc"), translate("binder.entry.point.entries") };
		entryPointEl = uifactory.addDropdownSingleselect("binder.entry.point", "binder.entry.point", formLayout, entryPointKeys, entryPointValues, null);
		entryPointEl.addActionListener(FormEvent.ONCHANGE);
		String entryPoint = portfolioV2Module.getBinderEntryPoint();
		for(String entryPointKey:entryPointKeys) {
			if(entryPointKey.equals(entryPoint)) {
				entryPointEl.select(entryPointKey, true);
			}
		}
		if(!entryPointEl.isOneSelected()) {
			entryPointEl.select(entryPointKeys[0], true);
		}
		
		sectionVisibilityEl = uifactory.addCheckboxesVertical("section.enabled", formLayout, SECTION_VISIBILITY_KEYS,
				translateKeys(SECTION_VISIBILITY_KEYS), 1);
		sectionVisibilityEl.addActionListener(FormEvent.ONCHANGE);
		
		entryElementsVisibilityEl = uifactory.addCheckboxesVertical("entries.elements.enabled", formLayout,
				ENTRIES_ELEMENTS_KEYS, translateKeys(ENTRIES_ELEMENTS_KEYS), 1);
		entryElementsVisibilityEl.addActionListener(FormEvent.ONCHANGE);
		
		commentsVisibilityEl = uifactory.addCheckboxesVertical("comments.enabled", formLayout, COMMENTS_KEYS,
				translateKeys(COMMENTS_KEYS), 1);
		commentsVisibilityEl.addActionListener(FormEvent.ONCHANGE);
		
		entriesViewEl = uifactory.addDropdownSingleselect("entries.view", formLayout, ENTRIES_VIEW_KEYS,
				translateKeys(ENTRIES_VIEW_KEYS));
		String selectedKey = ENTRIES_BOTH_ENABLED;
		if (portfolioV2Module.isEntriesTableEnabled() && !portfolioV2Module.isEntriesListEnabled()) {
			selectedKey = ENTRIES_TABLE_ENABLED;
		} else if (!portfolioV2Module.isEntriesTableEnabled() && portfolioV2Module.isEntriesListEnabled()) {
			selectedKey = ENTRIES_LIST_ENABLED;
		}
		entriesViewEl.select(selectedKey, true);
		entriesViewEl.addActionListener(FormEvent.ONCHANGE);
		
		taxonomyLinkingEnbaledEl = uifactory.addCheckboxesVertical("taxonomy.linking.enabled", formLayout, enabledKeys, enabledValues, 1);
		if(portfolioV2Module.isTaxonomyLinkingEnabled()) {
			taxonomyLinkingEnbaledEl.select(enabledKeys[0], true);
		}
		taxonomyLinkingEnbaledEl.addActionListener(FormEvent.ONCHANGE);
		
		List<Taxonomy> taxonomies = taxonomyService.getTaxonomyList();
		String[] taxonomyKeys = taxonomies.stream().map(taxonomy -> taxonomy.getKey().toString()).toArray(String[]::new);
		String[] taxonomyNames = taxonomies.stream().map(taxonomy -> taxonomy.getDisplayName()).toArray(String[]::new);
		
		linkedTaxonomiesEl = uifactory.addCheckboxesVertical("taxonomy.linked.elements", formLayout, taxonomyKeys, taxonomyNames, 1);
		if (portfolioV2Module.getLinkedTaxonomies() != null) {
			portfolioV2Module.getLinkedTaxonomies().stream().forEach(taxonomy -> linkedTaxonomiesEl.select(taxonomy.getKey().toString(), true));
		}
		linkedTaxonomiesEl.addActionListener(FormEvent.ONCHANGE);
		
		updateV2UI();
	}

	private void updateV2UI() {
		boolean enabled = portfolioV2Module.isEnabled();
		entryPointEl.setVisible(enabled);
		createBinderEl.setVisible(enabled);
		sectionVisibilityEl.setVisible(enabled);
		entryElementsVisibilityEl.setVisible(enabled);
		commentsVisibilityEl.setVisible(enabled);
		entriesViewEl.setVisible(enabled);
		taxonomyLinkingEnbaledEl.setVisible(enabled);
		linkedTaxonomiesEl.setVisible(enabled && portfolioV2Module.isTaxonomyLinkingEnabled());
		
		if (enabled) {
			createBinderEl.select(BINDER_CREATE_LEARNER, portfolioV2Module.isLearnerCanCreateBinders());
			createBinderEl.select(BINDER_CREATE_TEMPLATE, portfolioV2Module.isCanCreateBindersFromTemplate());
			createBinderEl.select(BINDER_CREATE_COURSE, portfolioV2Module.isCanCreateBindersFromCourse());
			sectionVisibilityEl.select(SECTION_OVERVIEW_ENABLED, portfolioV2Module.isOverviewEnabled());
			sectionVisibilityEl.select(SECTION_ENTRIES_ENABLED, portfolioV2Module.isEntriesEnabled());
			sectionVisibilityEl.select(SECTION_HISTORY_ENABLED, portfolioV2Module.isHistoryEnabled());
			entryElementsVisibilityEl.select(ENTRIES_SEARCH_ENABLED, portfolioV2Module.isEntriesSearchEnabled());
			entryElementsVisibilityEl.select(ENTRIES_TIMELINE_ENABLED, portfolioV2Module.isEntriesTimelineEnabled());
			commentsVisibilityEl.select(COMMENTS_OVERVIEW_ENABLED, portfolioV2Module.isOverviewCommentsEnabled());
			commentsVisibilityEl.select(COMMENTS_ENTRIES_ENABLED, portfolioV2Module.isEntriesCommentsEnabled());
			taxonomyLinkingEnbaledEl.select(enabledKeys[0], portfolioV2Module.isTaxonomyLinkingEnabled());
			
			if (portfolioV2Module.isTaxonomyLinkingEnabled() && portfolioV2Module.getLinkedTaxonomies() != null) {
				portfolioV2Module.getLinkedTaxonomies().stream().forEach(taxonomy -> linkedTaxonomiesEl.select(taxonomy.getKey().toString(), true));
			}
		}
	}
	
	private String[] translateKeys(String[] keys) {
		return Stream.of(keys)
				.map(key -> getTranslator().translate(key))
				.toArray(String[]::new);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
		if(source == portfolioEnabled) {
			boolean enabled = portfolioEnabled.isSelected(0);
			portfolioV2Module.setEnabled(enabled);
			updateV2UI();
			CollaborationToolsFactory.getInstance().initAvailableTools();
		} else if(createBinderEl == source) {
			Collection<String>  selectedCreateBinder = createBinderEl.getSelectedKeys();
			boolean learnerCanCreateBinders = selectedCreateBinder.contains(BINDER_CREATE_LEARNER);
			portfolioV2Module.setLearnerCanCreateBinders(learnerCanCreateBinders);
			boolean canCreateBindersFromTemplate = selectedCreateBinder.contains(BINDER_CREATE_TEMPLATE);
			portfolioV2Module.setCanCreateBindersFromTemplate(canCreateBindersFromTemplate);
			boolean canCreateBindersFromCourse = selectedCreateBinder.contains(BINDER_CREATE_COURSE);
			portfolioV2Module.setCanCreateBindersFromCourse(canCreateBindersFromCourse);
		} else if(entryPointEl == source) {
			if(entryPointEl.isOneSelected()) {
				String selectedKey = entryPointEl.getSelectedKey();
				if (validateEntryPoint(selectedKey)) {
					portfolioV2Module.setBinderEntryPoint(selectedKey);
				}
			}
		} else if (sectionVisibilityEl == source) {
			Collection<String> selectedSectionVisibility = sectionVisibilityEl.getSelectedKeys();
			boolean historyEnabled = selectedSectionVisibility.contains(SECTION_HISTORY_ENABLED);
			portfolioV2Module.setHistoryEnabled(historyEnabled);
			boolean overviewEnabled = selectedSectionVisibility.contains(SECTION_OVERVIEW_ENABLED);
			boolean entriesEnabled = selectedSectionVisibility.contains(SECTION_ENTRIES_ENABLED);
			if (validateSectionVisibility(overviewEnabled, entriesEnabled) ) {
				portfolioV2Module.setOverviewEnabled(overviewEnabled);
				portfolioV2Module.setEntriesEnabled(entriesEnabled);
			}
		} else if (entryElementsVisibilityEl == source) {
			Collection<String> selectedElementsVisibility = entryElementsVisibilityEl.getSelectedKeys();
			boolean entiresSearchEnabled = selectedElementsVisibility.contains(ENTRIES_SEARCH_ENABLED);
			portfolioV2Module.setEntriesSearchEnabled(entiresSearchEnabled);
			boolean entriesTimelineEnabled = selectedElementsVisibility.contains(ENTRIES_TIMELINE_ENABLED);
			portfolioV2Module.setEntriesTimelineEnabled(entriesTimelineEnabled);
		} else if (commentsVisibilityEl == source) {
			Collection<String> selectedCommentsVisibilty = commentsVisibilityEl.getSelectedKeys();
			boolean overviewCommentsEnabled = selectedCommentsVisibilty.contains(COMMENTS_OVERVIEW_ENABLED);
			portfolioV2Module.setOverviewCommentsEnabled(overviewCommentsEnabled);
			boolean entriesCommentsEnabled = selectedCommentsVisibilty.contains(COMMENTS_ENTRIES_ENABLED);
			portfolioV2Module.setEntriesCommentsEnabled(entriesCommentsEnabled);
		} else if(entriesViewEl == source && entriesViewEl.isOneSelected()) {
			String selectedKey = entriesViewEl.getSelectedKey();
			boolean entriesTableEnabled = true;
			boolean entriesListEnabled = true;
			if (ENTRIES_TABLE_ENABLED.equals(selectedKey)) {
				entriesListEnabled = false;
			} else if (ENTRIES_LIST_ENABLED.equals(selectedKey)) {
				entriesTableEnabled = false;
			}
			portfolioV2Module.setEntriesTableEnabled(entriesTableEnabled);
			portfolioV2Module.setEntriesListEnabled(entriesListEnabled);
		} else if (taxonomyLinkingEnbaledEl == source) {
			boolean enabled = taxonomyLinkingEnbaledEl.isSelected(0);
			portfolioV2Module.setTaxonomyLinkingEnabled(enabled);
			updateV2UI();
		} else if (linkedTaxonomiesEl == source) {
			portfolioV2Module.setLinkedTaxonomies(linkedTaxonomiesEl.getSelectedKeys());
		}
	}

	private boolean validateEntryPoint(String selectedKey) {
		if (PortfolioV2Module.ENTRY_POINT_TOC.equals(selectedKey) && !portfolioV2Module.isOverviewEnabled()) {
			entryPointEl.select(PortfolioV2Module.ENTRY_POINT_ENTRIES, true);
			showWarning("binder.entry.point.not.available");
			return false;
		} else if (PortfolioV2Module.ENTRY_POINT_ENTRIES.equals(selectedKey) && !portfolioV2Module.isEntriesEnabled()) {
			entryPointEl.select(PortfolioV2Module.ENTRY_POINT_TOC, true);
			showWarning("binder.entry.point.not.available");
			return false;
		} 
		return true;
	}

	private boolean validateSectionVisibility(boolean overviewEnabled, boolean entriesEnabled) {
		if (!overviewEnabled && entryPointEl.isOneSelected() && PortfolioV2Module.ENTRY_POINT_TOC.equals(entryPointEl.getSelectedKey())) {
			sectionVisibilityEl.select(SECTION_OVERVIEW_ENABLED, true);
			showWarning("section.disable.not.allowed");
			return false;
		} else if (!entriesEnabled && entryPointEl.isOneSelected() && PortfolioV2Module.ENTRY_POINT_ENTRIES.equals(entryPointEl.getSelectedKey())) {
			sectionVisibilityEl.select(SECTION_ENTRIES_ENABLED, true);
			showWarning("section.disable.not.allowed");
			return false;
		}
		return true;
	}
}

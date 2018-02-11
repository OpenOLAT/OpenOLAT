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
package org.olat.portfolio.ui;

import java.util.ArrayList;
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
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.portfolio.EPArtefactHandler;
import org.olat.portfolio.PortfolioModule;
import org.olat.portfolio.ui.filter.PortfolioFilterController;
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
	private static String[] enabledPortfolioKeys = new String[]{ "on", "legacy"};

	private SingleSelection entryPointEl;
	private FormLayoutContainer wizardFlc;
	private MultipleSelectionElement portfoliosEnabled;
	private MultipleSelectionElement createBinderEl;
	private final List<MultipleSelectionElement> handlersEnabled = new ArrayList<>();
	private MultipleSelectionElement copyrightStepCB;
	private MultipleSelectionElement reflexionStepCB;
	private MultipleSelectionElement sectionVisibilityEl;
	private MultipleSelectionElement entryElementsVisibilityEl;
	private MultipleSelectionElement commentsVisibilityEl;
	private SingleSelection entriesViewEl;

	@Autowired
	private PortfolioModule portfolioModule;
	@Autowired
	private PortfolioV2Module portfolioV2Module;
	
	public PortfolioAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "adminconfig");
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//module configuration
		FormLayoutContainer moduleFlc = FormLayoutContainer.createDefaultFormLayout("flc_module", getTranslator());
		formLayout.add(moduleFlc);

		String[] enabledValues = new String[] { translate("enabled")};
		
		String[] enabledPortfolioValues = new String[] { translate("enabled"), translate("portfolio.v1.module.enabled") };
		portfoliosEnabled = uifactory.addCheckboxesVertical("portfolio.module.enabled", moduleFlc, enabledPortfolioKeys, enabledPortfolioValues, 1);
		if(portfolioModule.isEnabled() || portfolioV2Module.isEnabled()) {
			portfoliosEnabled.select(enabledPortfolioKeys[0], true);
		}
		if(portfolioModule.isEnabled()) {
			portfoliosEnabled.select(enabledPortfolioKeys[1], true);
		}
		portfoliosEnabled.addActionListener(FormEvent.ONCHANGE);

		createBinderEl = uifactory.addCheckboxesVertical("portfolio.user.create.binder", moduleFlc, BINDER_CREATE_KEYS,
				translateKeys(BINDER_CREATE_KEYS), 1);
		createBinderEl.addActionListener(FormEvent.ONCHANGE);
		
		String[] entryPointKeys = new String[] { PortfolioV2Module.ENTRY_POINT_TOC, PortfolioV2Module.ENTRY_POINT_ENTRIES };
		String[] entryPointValues = new String[]{ translate("binder.entry.point.toc"), translate("binder.entry.point.entries") };
		entryPointEl = uifactory.addDropdownSingleselect("binder.entry.point", "binder.entry.point", moduleFlc, entryPointKeys, entryPointValues, null);
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
		
		sectionVisibilityEl = uifactory.addCheckboxesVertical("section.enabled", moduleFlc, SECTION_VISIBILITY_KEYS,
				translateKeys(SECTION_VISIBILITY_KEYS), 1);
		sectionVisibilityEl.addActionListener(FormEvent.ONCHANGE);
		
		entryElementsVisibilityEl = uifactory.addCheckboxesVertical("entries.elements.enabled", moduleFlc,
				ENTRIES_ELEMENTS_KEYS, translateKeys(ENTRIES_ELEMENTS_KEYS), 1);
		entryElementsVisibilityEl.addActionListener(FormEvent.ONCHANGE);
		
		commentsVisibilityEl = uifactory.addCheckboxesVertical("comments.enabled", moduleFlc, COMMENTS_KEYS,
				translateKeys(COMMENTS_KEYS), 1);
		commentsVisibilityEl.addActionListener(FormEvent.ONCHANGE);
		
		entriesViewEl = uifactory.addDropdownSingleselect("entries.view", moduleFlc, ENTRIES_VIEW_KEYS,
				translateKeys(ENTRIES_VIEW_KEYS));
		String selectedKey = ENTRIES_BOTH_ENABLED;
		if (portfolioV2Module.isEntriesTableEnabled() && !portfolioV2Module.isEntriesListEnabled()) {
			selectedKey = ENTRIES_TABLE_ENABLED;
		} else if (!portfolioV2Module.isEntriesTableEnabled() && portfolioV2Module.isEntriesListEnabled()) {
			selectedKey = ENTRIES_LIST_ENABLED;
		}
		entriesViewEl.select(selectedKey, true);
		entriesViewEl.addActionListener(FormEvent.ONCHANGE);
		
		//handlers configuration
		FormLayoutContainer handlersFlc = FormLayoutContainer.createDefaultFormLayout("flc_handlers", getTranslator());
		formLayout.add(handlersFlc);

		List<EPArtefactHandler<?>> handlers = portfolioModule.getAllAvailableArtefactHandlers();
		for(EPArtefactHandler<?> handler:handlers) {
			Translator handlerTrans = handler.getHandlerTranslator(getTranslator());
			handlersFlc.setTranslator(handlerTrans);
			String handlerClass = PortfolioFilterController.HANDLER_PREFIX + handler.getClass().getSimpleName() + PortfolioFilterController.HANDLER_TITLE_SUFFIX;
			MultipleSelectionElement handlerEnabled = uifactory.addCheckboxesHorizontal(handlerClass, handlersFlc, enabledKeys, enabledValues);
			handlerEnabled.select(enabledKeys[0], handler.isEnabled());
			handlerEnabled.setUserObject(handler);
			handlerEnabled.addActionListener(FormEvent.ONCHANGE);
			handlersEnabled.add(handlerEnabled);
		}
		
		// configure steps in artefact collection wizard
		wizardFlc = FormLayoutContainer.createDefaultFormLayout("flc_wizard", getTranslator());
		formLayout.add(wizardFlc);	
		copyrightStepCB = uifactory.addCheckboxesHorizontal("wizard.step.copyright", wizardFlc, enabledKeys, enabledValues);
		copyrightStepCB.select(enabledKeys[0], portfolioModule.isCopyrightStepEnabled());
		copyrightStepCB.addActionListener(FormEvent.ONCHANGE);
		
		reflexionStepCB = uifactory.addCheckboxesHorizontal("wizard.step.reflexion", wizardFlc, enabledKeys, enabledValues);
		reflexionStepCB.select(enabledKeys[0], portfolioModule.isReflexionStepEnabled());
		reflexionStepCB.addActionListener(FormEvent.ONCHANGE);
		wizardFlc.setVisible(portfoliosEnabled.isSelected(1));
		
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
		}
	}
	
	private String[] translateKeys(String[] keys) {
		return Stream.of(keys)
				.map(key -> getTranslator().translate(key))
				.toArray(String[]::new);
	}
	
	@Override
	protected void doDispose() {
		//auto-disposed
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
		if(source == portfoliosEnabled) {
			boolean enabled = portfoliosEnabled.isSelected(0);
			if(enabled) {
				portfolioModule.setEnabled(portfoliosEnabled.isSelected(1));
				portfolioV2Module.setEnabled(true);
			} else {
				portfolioModule.setEnabled(false);
				portfolioV2Module.setEnabled(false);
			}
			// update collaboration tools list

			wizardFlc.setVisible(portfoliosEnabled.isSelected(1));
			updateV2UI();
			CollaborationToolsFactory.getInstance().initAvailableTools();
		} else if(handlersEnabled.contains(source)) {
			EPArtefactHandler<?> handler = (EPArtefactHandler<?>)source.getUserObject();
			boolean enabled = ((MultipleSelectionElement)source).isSelected(0);
			portfolioModule.setEnableArtefactHandler(handler, enabled);
		} else if(source == reflexionStepCB){
			boolean enabled = reflexionStepCB.isSelected(0);
			portfolioModule.setReflexionStepEnabled(enabled);
		} else if(source == copyrightStepCB){
			boolean enabled = copyrightStepCB.isSelected(0);
			portfolioModule.setCopyrightStepEnabled(enabled);
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

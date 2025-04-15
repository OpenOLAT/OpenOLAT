/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.repository.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Util;
import org.olat.repository.DefaultCycleBadgeRenderer;
import org.olat.repository.LifecycleModule;
import org.olat.repository.RepositoryManager;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.repository.ui.LifecycleDataModel.LCCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 10.06.2013<br>
 *
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class LifecycleAdminController extends FormBasicController {

	private static final String TAB_ID_ALL = "All";
	private static final String TAB_ID_RELEVANT = "Relevant";
	private static final String TAB_ID_DEFAULT = "Default";
	private static final String TAB_ID_PAST = "Past";

	private final Map<RepositoryEntryLifecycle, FormLink> toolsLinks = new HashMap<>();

	private FormLink createLifeCycle;
	private FormToggle lifecycleEnabledToggleEl;

	private FlexiTableElement tableEl;
	private LifecycleDataModel model;

	private CloseableModalController cmc;
	private LifecycleEditController editCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	private ToolsController toolsCtrl;

	@Autowired
	private RepositoryEntryLifecycleDAO entryLifecycleDAO;
	@Autowired
	private LifecycleModule lifecycleModule;

	public LifecycleAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "lifecycles_admin");
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));

		initForm(ureq);
		reloadModel();
		toggleLifeCycle();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer toggleCont = FormLayoutContainer.createDefaultFormLayout("lifecycleToggleContainer", getTranslator());
		formLayout.add(toggleCont);

		lifecycleEnabledToggleEl = uifactory.addToggleButton("lifecycle.enabled", "lifecycle.enabled", translate("on"), translate("off"), toggleCont);
		lifecycleEnabledToggleEl.toggle(lifecycleModule.isEnabled());
		lifecycleEnabledToggleEl.addActionListener(FormEvent.ONCHANGE);

		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LCCols.softkey));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LCCols.label));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LCCols.validFrom));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LCCols.validTo));

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LCCols.defaultCycle, new DefaultCycleBadgeRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(LCCols.usages));

		DefaultFlexiColumnModel editColumn = new DefaultFlexiColumnModel(
				LCCols.edit.i18nKey(), LCCols.edit.ordinal(), "edit-lifecycle",
				new BooleanCellRenderer(new StaticFlexiCellRenderer("", "edit-lifecycle", null, "o_icon-lg o_icon_edit", translate("edit")), null));
		editColumn.setIconHeader("o_icon o_icon-fw o_icon-lg o_icon_edit");
		editColumn.setHeaderLabel(translate("edit"));
		editColumn.setAlwaysVisible(true);
		editColumn.setExportable(false);
		columnsModel.addFlexiColumnModel(editColumn);

		columnsModel.addFlexiColumnModel(new ActionsColumnModel(LCCols.tools));

		model = new LifecycleDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "cycles", model, getTranslator(), formLayout);
		tableEl.setRendererType(FlexiTableRendererType.classic);
		tableEl.setCustomizeColumns(false);
		tableEl.setSortEnabled(true);
		tableEl.setEmptyTableSettings("lifecycle.table.empty.desc", null, "o_icon_calendar");

		initFiltersPresets(ureq);

		createLifeCycle = uifactory.addFormLink("create.lifecycle", formLayout, Link.BUTTON);
		createLifeCycle.setIconLeftCSS("o_icon o_icon-fw o_icon_add");
	}

	private void initFiltersPresets(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>(4);

		FlexiFiltersTab tabAll = FlexiFiltersTabFactory.tabWithFilters(TAB_ID_ALL, translate("lifecycle.filter.all"), TabSelectionBehavior.clear, List.of());
		tabAll.setFiltersExpanded(true);
		tabs.add(tabAll);

		FlexiFiltersTab tabRelevant = FlexiFiltersTabFactory.tabWithFilters(TAB_ID_RELEVANT, translate("lifecycle.filter.relevant"), TabSelectionBehavior.clear, List.of());
		tabs.add(tabRelevant);

		FlexiFiltersTab tabDefault = FlexiFiltersTabFactory.tabWithFilters(TAB_ID_DEFAULT, translate("lifecycle.filter.default"), TabSelectionBehavior.clear, List.of());
		tabs.add(tabDefault);

		FlexiFiltersTab tabPast = FlexiFiltersTabFactory.tabWithFilters(TAB_ID_PAST, translate("lifecycle.filter.past"), TabSelectionBehavior.clear, List.of());
		tabs.add(tabPast);

		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, tabRelevant);
	}


	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == createLifeCycle) {
			doEdit(ureq, null);
		} else if (source == tableEl) {
			if (event instanceof SelectionEvent se
					&& "edit-lifecycle".equals(se.getCommand())) {
				RepositoryEntryLifecycle row = model.getObject(se.getIndex());
				doEdit(ureq, row);
			} else if (event instanceof FlexiTableFilterTabEvent) {
				reloadModel();
			}
		} else if (source == lifecycleEnabledToggleEl) {
			if (isTimePeriodsBeingUsed()) {
				showWarning("lifecycle.disable");
				lifecycleEnabledToggleEl.toggleOn();
			} else {
				lifecycleModule.setEnabled(lifecycleEnabledToggleEl.isOn());
				toggleLifeCycle();
			}
		} else if (source instanceof FormLink formLink) {
			String cmd = formLink.getCmd();
			if ("tools".equals(cmd)) {
				doOpenTools(ureq, formLink);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	private void toggleLifeCycle() {
		tableEl.setVisible(lifecycleEnabledToggleEl.isOn());
		createLifeCycle.setVisible(lifecycleEnabledToggleEl.isOn());
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == editCtrl) {
			if (event == Event.DONE_EVENT) {
				reloadModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if (source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(toolsCalloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(editCtrl);
		removeAsListenerAndDispose(cmc);
		toolsCalloutCtrl = null;
		toolsCtrl = null;
		editCtrl = null;
		cmc = null;
	}

	private boolean isTimePeriodsBeingUsed() {
		List<RepositoryEntryLifecycle> lifecycles = entryLifecycleDAO.loadPublicLifecycle();
		Map<Long, Long> counts = entryLifecycleDAO.countRepositoryEntriesForLifecycles(lifecycles);

		// Sum up all usage counts
		return counts.values().stream().mapToLong(Long::longValue).sum() > 0;
	}


	private void reloadModel() {
		List<RepositoryEntryLifecycle> lifecycles = entryLifecycleDAO.loadPublicLifecycle();
		Map<Long, Long> usageMap = entryLifecycleDAO.countRepositoryEntriesForLifecycles(lifecycles);

		// Filter based on selected tab
		String selectedTabId = tableEl.getSelectedFilterTab() != null
				? tableEl.getSelectedFilterTab().getId() : TAB_ID_ALL;

		Date now = new Date();

		List<RepositoryEntryLifecycle> filtered = lifecycles.stream().filter(
				cycle -> switch (selectedTabId) {
			case TAB_ID_RELEVANT -> (isRelevantCycle(cycle, now));
			case TAB_ID_DEFAULT -> cycle.isDefaultPublicCycle();
			case TAB_ID_PAST -> isPastCycle(cycle, now);
			default -> true;
		}).toList();

		toolsLinks.clear();
		for (RepositoryEntryLifecycle cycle : lifecycles) {
			FormLink toolsLink = ActionsColumnModel.createLink(uifactory, getTranslator());
			toolsLink.setUserObject(cycle);
			toolsLink.setElementCssClass("o_lifecycle_tools_" + cycle.getKey());
			toolsLinks.put(cycle, toolsLink);
		}

		model.setObjects(filtered);
		model.setUsageCounts(usageMap);
		model.setToolsLinks(toolsLinks);
		tableEl.reset();
	}

	private boolean isPastCycle(RepositoryEntryLifecycle cycle, Date now) {
		return cycle.getValidTo() != null && cycle.getValidTo().before(now);
	}

	private boolean isRelevantCycle(RepositoryEntryLifecycle cycle, Date now) {
		boolean isDefault = cycle.isDefaultPublicCycle();

		boolean isCurrent =
				(cycle.getValidFrom() == null || !cycle.getValidFrom().after(now)) &&
						(cycle.getValidTo() == null || !cycle.getValidTo().before(now));

		boolean isFuture =
				cycle.getValidFrom() != null && cycle.getValidFrom().after(now);

		return isDefault || isCurrent || isFuture;
	}

	private void doOpenTools(UserRequest ureq, FormLink link) {
		toolsCtrl = new ToolsController(ureq, getWindowControl(), (RepositoryEntryLifecycle) link.getUserObject());
		listenTo(toolsCtrl);

		toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(toolsCalloutCtrl);
		toolsCalloutCtrl.activate();
	}

	private void doShowDeleteWarning(Long numberOfEntries, RepositoryEntryLifecycle lifecycle) {
		showWarning("delete.lifecycle.used", new String[]{lifecycle.getSoftKey(), lifecycle.getLabel(), String.valueOf(numberOfEntries)});
	}

	private void doDelete(RepositoryEntryLifecycle lifecycle) {
		entryLifecycleDAO.deleteLifecycle(lifecycle);
		reloadModel();
	}

	private void doEdit(UserRequest ureq, RepositoryEntryLifecycle lifecycle) {
		removeAsListenerAndDispose(editCtrl);
		editCtrl = new LifecycleEditController(ureq, getWindowControl(), lifecycle);
		listenTo(editCtrl);

		String title = translate("add.lifecycle");
		if (lifecycle != null) {
			title = translate("edit.lifecycle");
		}
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				editCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}

	private class ToolsController extends BasicController {

		private final VelocityContainer mainVC;
		private final Link editLink;
		private final Link deleteLink;
		private final RepositoryEntryLifecycle row;

		public ToolsController(UserRequest ureq, WindowControl wControl, RepositoryEntryLifecycle row) {
			super(ureq, wControl);
			this.row = row;

			mainVC = createVelocityContainer("tools");

			List<String> links = new ArrayList<>(2);

			editLink = addLink("edit", "o_icon_edit", links);
			deleteLink = addLink("delete", "o_icon_delete_item", links);
			mainVC.contextPut("links", links);

			putInitialPanel(mainVC);
		}

		private Link addLink(String name, String iconCss, List<String> links) {
			Link link = LinkFactory.createLink(name, name, getTranslator(), mainVC, this, Link.LINK);
			mainVC.put(name, link);
			links.add(name);
			link.setIconLeftCSS("o_icon o_icon-fw " + iconCss);
			return link;
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if (deleteLink == source) {
				close();
				long numberOfEntries = entryLifecycleDAO.countRepositoryEntriesWithLifecycle(row);
				if (numberOfEntries > 0) {
					doShowDeleteWarning(numberOfEntries, row);
				} else {
					doDelete(row);
				}
			} else if (editLink == source) {
				close();
				doEdit(ureq, row);
			}
		}

		private void close() {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		}
	}
}

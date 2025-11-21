/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.repository.ui.list;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableDateRangeFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableOneClickSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumSecurityCallbackFactory;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.ui.CurriculumComposerController;
import org.olat.modules.curriculum.ui.component.CurriculumStatusCellRenderer;
import org.olat.repository.RepositoryManager;
import org.olat.repository.manager.RepositoryEntryMyImplementationsQueries;
import org.olat.repository.ui.list.ImplementationsListDataModel.ImplementationsCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 23 avr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ImplementationsListController extends FormBasicController implements Activateable2 {

	private final String CMD_SELECT  = "iselect";
	
	private static final String ALL_TAB_ID = "All";
	private static final String ACTIVE_TAB_ID = "Active";
	private static final String FAVORITE_TAB_ID = "Marks";
	private static final String FINISHED_TAB_ID = "Finished";
	private static final String RELEVANT_TAB_ID = "Relevant";
	private static final String PREPARATION_TAB_ID = "Preparation";
	
	static final String FILTER_MARKED = "Marked";
	static final String FILTER_STATUS = "Status";
	static final String FILTER_PERIOD = "Period";
	static final String FILTER_CURRICULUM = "Curriculum";
	
	private FlexiFiltersTab allTab;
	private FlexiFiltersTab activeTab;
	private FlexiFiltersTab favoriteTab;
	private FlexiFiltersTab finishedTab;
	private FlexiFiltersTab relevantTab;
	private FlexiFiltersTab preparationTab;
	
	private FlexiTableElement tableEl;
	private ImplementationsListDataModel tableModel;
	private final BreadcrumbedStackedPanel stackPanel;
	
	private final List<GroupRoles> asRoles;
	private final boolean onlyParticipant;
	private final boolean withTitle;
	private final String helpUrl;
	
	private ImplementationController implementationCtrl;

	@Autowired
	private MarkManager markManager;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private RepositoryEntryMyImplementationsQueries myImplementationsQueries;
	
	public ImplementationsListController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel,
			List<GroupRoles> asRoles, boolean withTitle, String helpUrl) {
		super(ureq, wControl, "implementations");
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(),
				Util.createPackageTranslator(CurriculumComposerController.class, getLocale(), getTranslator())));
		this.stackPanel = stackPanel;
		this.withTitle = withTitle;
		this.helpUrl = helpUrl;
		this.asRoles = asRoles;
		onlyParticipant = asRoles.size() == 1 && asRoles.contains(GroupRoles.participant);
		
		initForm(ureq);
		loadModel();
		
		if(tableModel.hasMarked()) {
			tableEl.setSelectedFilterTab(ureq, favoriteTab);
		} else if(allTab != null) {
			tableEl.setSelectedFilterTab(ureq, allTab);
		} else if(activeTab != null) {
			tableEl.setSelectedFilterTab(ureq, activeTab);
		}
		
		filterModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			layoutCont.contextPut("withTitle", Boolean.valueOf(withTitle));
			layoutCont.contextPut("helpUrl", helpUrl);
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		DefaultFlexiColumnModel markColModel = new DefaultFlexiColumnModel(ImplementationsCols.mark);
		markColModel.setIconHeader("o_icon o_icon_bookmark_header o_icon-lg");
		markColModel.setExportable(false);
		columnsModel.addFlexiColumnModel(markColModel);
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ImplementationsCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImplementationsCols.displayName, CMD_SELECT));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImplementationsCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(!onlyParticipant, ImplementationsCols.curriculum,
				new CurriculumCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImplementationsCols.lifecycleStart,
				new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImplementationsCols.lifecycleEnd,
				new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(!onlyParticipant, ImplementationsCols.elementStatus,
				new CurriculumStatusCellRenderer(getTranslator())));
		
		tableModel = new ImplementationsListDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_coursetable");
		tableEl.setSearchEnabled(true);
		
		initFilter();
		initFilterPresets();
	}
	
	private void initFilter() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		SelectionValues markedKeyValue = new SelectionValues();
		markedKeyValue.add(SelectionValues.entry(FILTER_MARKED, translate("search.mark")));
		filters.add(new FlexiTableOneClickSelectionFilter(translate("search.mark"),
				FILTER_MARKED, markedKeyValue, true));
		
		List<Curriculum> curriculums = loadCurriculumsForFilter();
		if(!curriculums.isEmpty()) {
			SelectionValues curriculumValues = new SelectionValues();
			for(Curriculum cur:curriculums) {
				String key = cur.getKey().toString();
				String value = StringHelper.escapeHtml(cur.getDisplayName());
				if(StringHelper.containsNonWhitespace(cur.getIdentifier())) {
					value += " <small class=\"mute\"> \u00B7 " + StringHelper.escapeHtml(cur.getIdentifier()) + "</small>";
				}
				curriculumValues.add(SelectionValues.entry(key, value));
			}
			
			FlexiTableMultiSelectionFilter curriculumFilter = new FlexiTableMultiSelectionFilter(translate("filter.curriculum"),
					FILTER_CURRICULUM, curriculumValues, true);
			filters.add(curriculumFilter);
		}
    	
		SelectionValues statusValues = new SelectionValues();
		if(!onlyParticipant) {
			statusValues.add(SelectionValues.entry(CurriculumElementStatus.preparation.name(), translate("filter.preparation")));
		}
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.provisional.name(), translate("filter.provisional")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.confirmed.name(), translate("filter.confirmed")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.active.name(), translate("filter.active")));
		if(!onlyParticipant) {
			statusValues.add(SelectionValues.entry(CurriculumElementStatus.cancelled.name(), translate("filter.cancelled")));
		}
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.finished.name(), translate("filter.finished")));
		FlexiTableMultiSelectionFilter statusFilter = new FlexiTableMultiSelectionFilter(translate("filter.status"),
				FILTER_STATUS, statusValues, true);
		filters.add(statusFilter);
		
		FlexiTableDateRangeFilter periodFilter = new FlexiTableDateRangeFilter(translate("filter.date.range"),
				FILTER_PERIOD, true, false, getLocale());
		filters.add(periodFilter);

		tableEl.setFilters(true, filters, false, false);
	}
	
	private void initFilterPresets() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		favoriteTab = FlexiFiltersTabFactory.tabWithImplicitFilters(FAVORITE_TAB_ID, translate("filter.mark"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_MARKED, FILTER_MARKED)));
		tabs.add(favoriteTab);

		if(onlyParticipant) {
			activeTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ACTIVE_TAB_ID, translate("filter.active") ,
					TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS,
							List.of(CurriculumElementStatus.provisional.name(), CurriculumElementStatus.confirmed.name(),
									CurriculumElementStatus.active.name()))));
			tabs.add(activeTab);
		} else {
			allTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ALL_TAB_ID, translate("filter.all"),
					TabSelectionBehavior.nothing, List.of());
			tabs.add(allTab);
			
			relevantTab = FlexiFiltersTabFactory.tabWithImplicitFilters(RELEVANT_TAB_ID, translate("filter.relevant"),
					TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS,
							List.of(CurriculumElementStatus.provisional.name(), CurriculumElementStatus.confirmed.name(),
									CurriculumElementStatus.active.name()))));
			tabs.add(relevantTab);
			
			preparationTab = FlexiFiltersTabFactory.tabWithImplicitFilters(PREPARATION_TAB_ID, translate("filter.preparation"),
					TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS,
							List.of(CurriculumElementStatus.preparation.name()))));
			tabs.add(preparationTab);
		}
		
		finishedTab = FlexiFiltersTabFactory.tabWithImplicitFilters(FINISHED_TAB_ID, translate("filter.finished"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS,
						List.of(CurriculumElementStatus.finished.name()))));
		tabs.add(finishedTab);


		tableEl.setFilterTabs(true, tabs);
	}
	
	private void loadModel() {
		Set<Long> markedElementKeys = markManager.getMarkResourceIds(getIdentity(), "CurriculumElement", List.of());
		List<CurriculumElement> implementations = myImplementationsQueries.searchImplementations(getIdentity(),
				false, asRoles, null);
		List<ImplementationRow> rows = new ArrayList<>();
		for(CurriculumElement implementation:implementations) {
			boolean marked = markedElementKeys.contains(implementation.getKey());
			Curriculum curriculum = implementation.getCurriculum();
			rows.add(forgeRow(implementation, curriculum, marked));
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private ImplementationRow forgeRow(CurriculumElement implementation, Curriculum curriculum, boolean marked) {
		FormLink markLink = uifactory.addFormLink("mark_" + implementation.getResource().getKey(), "mark", "", tableEl, Link.NONTRANSLATED);
		decoratedMarkLink(markLink, marked);
		
		ImplementationRow row = new ImplementationRow(implementation, curriculum, markLink);
		markLink.setUserObject(row);
		return row;
	}
	
	private void filterModel() {
		tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
		tableEl.reset(true, true, true);
	}
	
	private List<Curriculum> loadCurriculumsForFilter() {
		return myImplementationsQueries.getCurriculums(getIdentity(), asRoles, null);
	}
	
	private void decoratedMarkLink(FormLink markLink, boolean marked) {
		markLink.setIconLeftCSS(marked ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
		markLink.setTitle(translate(marked ? "details.bookmark.remove" : "details.bookmark"));
		markLink.setAriaLabel(translate(marked ? "details.bookmark.remove" : "details.bookmark"));
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry entry = entries.get(0);
		String type = entry.getOLATResourceable().getResourceableTypeName();
		if("CurriculumElement".equalsIgnoreCase(type)) {
			ImplementationRow row = tableModel.getObjectByKey(entry.getOLATResourceable().getResourceableId());
			if(row != null) {
				doOpenImplementation(ureq, row);
			}
		} else if(ALL_TAB_ID.equalsIgnoreCase(type) && allTab != null) {
			tableEl.setSelectedFilterTab(ureq, allTab);
			filterModel();
		} else if(ACTIVE_TAB_ID.equalsIgnoreCase(type) && activeTab != null) {
			tableEl.setSelectedFilterTab(ureq, activeTab);
			filterModel();
		} else if(FAVORITE_TAB_ID.equalsIgnoreCase(type) && favoriteTab != null) {
			tableEl.setSelectedFilterTab(ureq, favoriteTab);
			filterModel();
		} else if(RELEVANT_TAB_ID.equalsIgnoreCase(type) && relevantTab != null) {
			tableEl.setSelectedFilterTab(ureq, relevantTab);
			filterModel();
		} else if(FINISHED_TAB_ID.equalsIgnoreCase(type) && finishedTab != null) {
			tableEl.setSelectedFilterTab(ureq, finishedTab);
			filterModel();
		} else if(PREPARATION_TAB_ID.equalsIgnoreCase(type) && preparationTab != null) {
			tableEl.setSelectedFilterTab(ureq, preparationTab);
			filterModel();
		} else if(allTab != null) {
			tableEl.setSelectedFilterTab(ureq, allTab);
			filterModel();
		} else if(activeTab != null) {
			tableEl.setSelectedFilterTab(ureq, activeTab);
			filterModel();
		}
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(implementationCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				fireEvent(ureq, event);
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				ImplementationRow row = tableModel.getObject(se.getIndex());
				doOpenImplementation(ureq, row);
			} else if(event instanceof FlexiTableFilterTabEvent || event instanceof FlexiTableSearchEvent) {
				filterModel();
			}
		} else if(source instanceof FormLink link && link.getUserObject() instanceof ImplementationRow row) {
			if("mark".equals(link.getCmd())) {
				boolean marked = doMark(ureq, row);
				decoratedMarkLink(link, marked);
				link.getComponent().setDirty(true);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenImplementation(UserRequest ureq, ImplementationRow row) {
		CurriculumElement element = curriculumService.getCurriculumElement(row);
		
		CurriculumSecurityCallback secCallback = CurriculumSecurityCallbackFactory.createDefaultCallback();
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("Implementation", element.getKey());
		WindowControl swControl = addToHistory(ureq, ores, null);
		implementationCtrl = new ImplementationController(ureq, swControl, stackPanel,
				element.getCurriculum(), element, asRoles, secCallback);
		listenTo(implementationCtrl);
		stackPanel.pushController(element.getDisplayName(), implementationCtrl);
	}
	
	private boolean doMark(UserRequest ureq, ImplementationRow row) {
		OLATResourceable item = OresHelper.createOLATResourceableInstance("CurriculumElement", row.getKey());
		String businessPath = "[MyCoursesSite:0][CurriculumElement:" + item.getResourceableId() + "]";

		boolean marked;
		if(markManager.isMarked(item, getIdentity(), null)) {
			markManager.removeMark(item, getIdentity(), null);
			marked = false;
		} else {
			markManager.setMark(item, getIdentity(), null, businessPath);
			marked = true;
		}
		fireEvent(ureq, Event.CHANGED_EVENT);
		return marked;
	}
}

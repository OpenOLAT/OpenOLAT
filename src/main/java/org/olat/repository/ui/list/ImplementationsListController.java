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

import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumSecurityCallbackFactory;
import org.olat.modules.curriculum.CurriculumService;
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
	
	static final String FAVORITE_TAB = "Marks";
	static final String ACTIVE_TAB = "Active";
	static final String FINISHED_TAB = "Finished";
	private final String CMD_SELECT  = "iselect";
	
	private FlexiFiltersTab favoriteTab;
	private FlexiFiltersTab activeTab;
	private FlexiFiltersTab finishedTab;
	
	private FlexiTableElement tableEl;
	private ImplementationsListDataModel tableModel;
	private final BreadcrumbedStackedPanel stackPanel;
	
	private ImplementationController implementationCtrl;

	@Autowired
	private MarkManager markManager;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private RepositoryEntryMyImplementationsQueries myImplementationsQueries;
	
	public ImplementationsListController(UserRequest ureq, WindowControl wControl, BreadcrumbedStackedPanel stackPanel) {
		super(ureq, wControl, "implementations");
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		this.stackPanel = stackPanel;
		
		initForm(ureq);
		loadModel();
		
		if(tableModel.hasMarked()) {
			tableModel.filter(FAVORITE_TAB);
			tableEl.setSelectedFilterTab(ureq, favoriteTab);
		} else {
			tableModel.filter(ACTIVE_TAB);
			tableEl.setSelectedFilterTab(ureq, activeTab);
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		DefaultFlexiColumnModel markColModel = new DefaultFlexiColumnModel(ImplementationsCols.mark);
		markColModel.setIconHeader("o_icon o_icon_bookmark_header o_icon-lg");
		markColModel.setExportable(false);
		columnsModel.addFlexiColumnModel(markColModel);
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ImplementationsCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImplementationsCols.displayName, CMD_SELECT));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImplementationsCols.externalRef));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImplementationsCols.lifecycleStart));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ImplementationsCols.lifecycleEnd));
		
		tableModel = new ImplementationsListDataModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_coursetable");
		tableEl.setSearchEnabled(true);
		
		initFilterPresets();
	}
	
	private void initFilterPresets() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		favoriteTab = FlexiFiltersTabFactory.tabWithImplicitFilters(FAVORITE_TAB, translate("filter.mark"),
				TabSelectionBehavior.nothing, List.of());
		tabs.add(favoriteTab);
		
		activeTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ACTIVE_TAB, translate("filter.active"),
				TabSelectionBehavior.nothing, List.of());
		tabs.add(activeTab);
		
		finishedTab = FlexiFiltersTabFactory.tabWithImplicitFilters(FINISHED_TAB, translate("filter.finished"),
				TabSelectionBehavior.nothing, List.of());
		tabs.add(finishedTab);

		tableEl.setFilterTabs(true, tabs);
	}
	
	private void loadModel() {
		Set<Long> markedElementKeys = markManager.getMarkResourceIds(getIdentity(), "CurriculumElement", List.of());
		List<CurriculumElement> implementations = myImplementationsQueries.searchImplementations(getIdentity(), false);
		List<ImplementationRow> rows = new ArrayList<>();
		for(CurriculumElement implementation:implementations) {
			boolean marked = markedElementKeys.contains(implementation.getKey());
			rows.add(forgeRow(implementation, marked));
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private ImplementationRow forgeRow(CurriculumElement implementation, boolean marked) {
		FormLink markLink = uifactory.addFormLink("mark_" + implementation.getResource().getKey(), "mark", "", tableEl, Link.NONTRANSLATED);
		decoratedMarkLink(markLink, marked);
		
		ImplementationRow row = new ImplementationRow(implementation, markLink);
		markLink.setUserObject(row);
		return row;
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
			} else if(event instanceof FlexiTableFilterTabEvent te) {
				tableModel.filter(te.getTab().getId());
				tableEl.reset(true, true, true);
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
		implementationCtrl = new ImplementationController(ureq, swControl, stackPanel, element.getCurriculum(), element, secCallback);
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

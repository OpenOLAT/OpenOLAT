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

import java.util.ArrayList;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StickyActionColumnModel;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementSearchInfos;
import org.olat.modules.curriculum.model.CurriculumElementSearchParams;
import org.olat.modules.curriculum.ui.CurriculumElementSearchDataModel.SearchCols;
import org.olat.modules.curriculum.ui.event.CurriculumSearchEvent;
import org.olat.modules.curriculum.ui.event.SelectReferenceEvent;
import org.olat.repository.RepositoryEntryRef;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 Oct 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumSearchManagerController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private CurriculumElementSearchDataModel tableModel;
	private final TooledStackedPanel toolbarPanel;
	
	private ToolsController toolsCtrl;
	private ReferencesController referencesCtrl;
	private CurriculumSearchController searchCtrl;
	private CloseableCalloutWindowController toolsCalloutCtrl;
	
	private int counter = 0;
	private final CurriculumSecurityCallback secCallback;
	
	@Autowired
	private CurriculumService curriculumService;
	
	public CurriculumSearchManagerController(UserRequest ureq, WindowControl wControl,
			TooledStackedPanel toolbarPanel, CurriculumSecurityCallback secCallback) {
		super(ureq, wControl, "curriculum_element_search");
		this.toolbarPanel = toolbarPanel;
		this.secCallback = secCallback;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		searchCtrl = new CurriculumSearchController(ureq, getWindowControl(), mainForm);
		searchCtrl.setEnabled(false);
		listenTo(searchCtrl);

		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, SearchCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SearchCols.curriculum));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SearchCols.displayName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SearchCols.identifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SearchCols.externalId));
		DateFlexiCellRenderer dateRenderer = new DateFlexiCellRenderer(getLocale());
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SearchCols.beginDate, dateRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SearchCols.endDate, dateRenderer));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SearchCols.typeDisplayName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(SearchCols.resources));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select", translate("select"), "select"));
		
		if(secCallback.canEditCurriculum()) {
			StickyActionColumnModel toolsCol = new StickyActionColumnModel(SearchCols.tools);
			toolsCol.setExportable(false);
			toolsCol.setAlwaysVisible(true);
			columnsModel.addFlexiColumnModel(toolsCol);
		}
		
		tableModel = new CurriculumElementSearchDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 50, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setSearchEnabled(true);
		tableEl.setExtendedSearch(searchCtrl);
		tableEl.setEmptyTableSettings("table.search.curriculum.empty", null, "o_icon_curriculum_element");
		tableEl.setAndLoadPersistedPreferences(ureq, "cur-curriculum-search-manage");
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(searchCtrl == source) {
			if(event instanceof CurriculumSearchEvent) {
				CurriculumSearchEvent searchEvent = (CurriculumSearchEvent)event;
				doSearch(searchEvent);
			}
		} else if (referencesCtrl == source) {
			toolsCalloutCtrl.deactivate();
			cleanUp();
			if(event instanceof SelectReferenceEvent) {
				launch(ureq, ((SelectReferenceEvent)event).getEntry());
			}
		} else if(toolsCalloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(referencesCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);
		toolsCalloutCtrl = null;
		referencesCtrl = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("select".equals(cmd)) {
					CurriculumElementSearchRow row = tableModel.getObject(se.getIndex());
					doEditCurriculumElement(ureq, row, null);
				}
				
			} else if(event instanceof FlexiTableSearchEvent) {
				FlexiTableSearchEvent se = (FlexiTableSearchEvent)event;
				doQuickSearch(se.getSearch());
			}
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("tools".equals(cmd)) {
				doOpenTools(ureq, (CurriculumElementSearchRow)link.getUserObject(), link);
			} else if("resources".equals(cmd)) {
				doOpenReferences(ureq, (CurriculumElementSearchRow)link.getUserObject(), link);
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doQuickSearch(String searchString) {
		CurriculumElementSearchParams params = new CurriculumElementSearchParams(getIdentity());
		params.setSearchString(searchString);
		
		List<CurriculumElementSearchInfos> elements = curriculumService.searchCurriculumElements(params);
		List<CurriculumElementSearchRow> rows = new ArrayList<>(elements.size());
		for(CurriculumElementSearchInfos element:elements) {
			CurriculumElementSearchRow row = forgeRow(element);
			rows.add(row);
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private void doSearch(CurriculumSearchEvent searchEvent) {
		CurriculumElementSearchParams params = new CurriculumElementSearchParams(getIdentity());
		params.setElementId(searchEvent.getElementId());
		params.setElementText(searchEvent.getElementText());
		params.setElementBeginDate(searchEvent.getElementBegin());
		params.setElementEndDate(searchEvent.getElementEnd());
		params.setEntryId(searchEvent.getEntryId());
		params.setEntryText(searchEvent.getEntryText());
		
		List<CurriculumElementSearchInfos> elements = curriculumService.searchCurriculumElements(params);
		List<CurriculumElementSearchRow> rows = new ArrayList<>(elements.size());
		for(CurriculumElementSearchInfos element:elements) {
			CurriculumElementSearchRow row = forgeRow(element);
			rows.add(row);
		}
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private CurriculumElementSearchRow forgeRow(CurriculumElementSearchInfos element) {
		FormLink toolsLink = uifactory.addFormLink("tools_" + (++counter), "tools", "", null, null, Link.NONTRANSLATED);
		toolsLink.setIconLeftCSS("o_icon o_icon_actions o_icon-fws o_icon-lg");
		
		FormLink resourcesLink = null;
		if(element.getNumOfResources() > 0) {
			resourcesLink = uifactory.addFormLink("resources_" + (++counter), "resources", String.valueOf(element.getNumOfResources()),
					null, null, Link.NONTRANSLATED);
		}
		CurriculumElementSearchRow row = new CurriculumElementSearchRow(element.getCurriculumElement(), resourcesLink, toolsLink);
		
		toolsLink.setUserObject(row);
		if(resourcesLink != null) {
			resourcesLink.setUserObject(row);
		}
		return row;
	}
	
	private void doEditCurriculumElement(UserRequest ureq, CurriculumElementSearchRow row, List<ContextEntry> entries) {
		CurriculumElement element = curriculumService.getCurriculumElement(row);
		if(element == null) {
			tableEl.reloadData();
			showWarning("warning.curriculum.element.deleted");
		} else {
			Curriculum curriculum = row.getCurriculum();
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance(CurriculumElement.class, row.getKey()), null);
			EditCurriculumElementOverviewController editCtrl = new EditCurriculumElementOverviewController(ureq, swControl, element, curriculum, secCallback);
			listenTo(editCtrl);
			toolbarPanel.pushController(row.getDisplayName(), editCtrl);
			editCtrl.activate(ureq, entries, null);
		}
	}
	
	private void doOpenTools(UserRequest ureq, CurriculumElementSearchRow row, FormLink link) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		CurriculumElement element = curriculumService.getCurriculumElement(row);
		if(element == null) {
			tableEl.reloadData();
			showWarning("warning.curriculum.element.deleted");
		} else {
			toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
			listenTo(toolsCtrl);
	
			toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
			listenTo(toolsCalloutCtrl);
			toolsCalloutCtrl.activate();
		}
	}
	
	private void doOpenReferences(UserRequest ureq, CurriculumElementSearchRow row, FormLink link) {
		removeAsListenerAndDispose(referencesCtrl);
		removeAsListenerAndDispose(toolsCalloutCtrl);

		CurriculumElement element = curriculumService.getCurriculumElement(row);
		if(element == null ) {
			tableEl.reloadData();
			showWarning("warning.curriculum.element.deleted");
		} else {
			referencesCtrl = new ReferencesController(ureq, getWindowControl(), getTranslator(), element);
			listenTo(referencesCtrl);
	
			toolsCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
					referencesCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
			listenTo(toolsCalloutCtrl);
			toolsCalloutCtrl.activate();
		}
	}
	
	private void launch(UserRequest ureq, RepositoryEntryRef ref) {
		String businessPath = "[RepositoryEntry:" + ref.getKey() + "]";
		if(!NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl())) {
			tableEl.reloadData();
		}
	}
	
	private class ToolsController extends BasicController {
		
		private final VelocityContainer mainVC;
		private Link editLink;
		
		private CurriculumElementSearchRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl,
				CurriculumElementSearchRow row) {
			super(ureq, wControl);
			this.row = row;
			
			mainVC = createVelocityContainer("tools");
			
			List<String> links = new ArrayList<>(4);
			
			//edit
			editLink = addLink("edit", "o_icon_edit", links);
			mainVC.contextPut("links", links);
			
			putInitialPanel(mainVC);
		}
		
		private Link addLink(String name, String iconCss, List<String> links) {
			Link link = LinkFactory.createLink(name, name, getTranslator(), mainVC, this, Link.LINK);
			mainVC.put(name, link);
			links.add(name);
			link.setIconLeftCSS("o_icon " + iconCss);
			return link;
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(editLink == source) {
				close();
				doEditCurriculumElement(ureq, row, null);
			}
		}
		
		private void close() {
			toolsCalloutCtrl.deactivate();
			cleanUp();
		}
	}
}

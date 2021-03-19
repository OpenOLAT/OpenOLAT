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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRenderEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.BinderConfiguration;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.Category;
import org.olat.modules.portfolio.CategoryToElement;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageStatus;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.ui.component.TimelinePoint;
import org.olat.modules.portfolio.ui.event.PageSelectionEvent;
import org.olat.modules.portfolio.ui.model.PortfolioElementRow;

/**
 * 
 * Initial date: 3 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SelectPageListController extends AbstractPageListController {

	private Section currentSection; 
	private boolean isMultiSelect;
	
	public SelectPageListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel, Section currentSection, BinderSecurityCallback secCallback, boolean isMultiSelect) {
		super(ureq, wControl, stackPanel, secCallback, BinderConfiguration.createSelectPagesConfig(), "select_pages", false, false, true);
		
		initPageListController(ureq, currentSection, isMultiSelect);
	}
	
	public SelectPageListController(UserRequest ureq, WindowControl wControl, Form rootForm, Section currentSection, BinderSecurityCallback secCallback, boolean isMultiSelect) {
		super(ureq, wControl, rootForm, secCallback, BinderConfiguration.createSelectPagesConfig(), "select_pages", false, false, true);
		
		initPageListController(ureq, currentSection, isMultiSelect);
	}
	
	public void initPageListController(UserRequest ureq, Section currentSection, boolean isMultiSelect) {
		this.currentSection = currentSection;
		this.isMultiSelect = isMultiSelect;

		initForm(ureq);
		loadModel(ureq, null);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.classic);
		super.loadCompetencesFilter();
		super.loadCategoriesFilter();
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initForm(formLayout, listener, ureq);
		tableEl.setMultiSelect(isMultiSelect);
		tableEl.setSelectAllEnable(isMultiSelect);
	}

	@Override
	public void initTools() {
		//
	}
	
	@Override
	protected void initColumns(FlexiTableColumnModel columnsModel) {
		super.initColumns(columnsModel);
		
		if (!isMultiSelect) {
			DefaultFlexiColumnModel selectCol = new DefaultFlexiColumnModel("select", translate("select"), "select-page");
			selectCol.setAlwaysVisible(true);
			columnsModel.addFlexiColumnModel(selectCol);
		}
	}

	@Override
	protected String getTimelineSwitchPreferencesName() {
		return "select-entries";
	}

	@Override
	protected void loadModel(UserRequest ureq, String searchString) {
		Map<Long,Long> numberOfCommentsMap = portfolioService.getNumberOfCommentsOnOwnedPage(getIdentity());
		
		List<CategoryToElement> categorizedElements = portfolioService.getCategorizedOwnedPages(getIdentity());
		Map<OLATResourceable,List<Category>> categorizedElementMap = new HashMap<>();
		for(CategoryToElement categorizedElement:categorizedElements) {
			List<Category> categories = categorizedElementMap.get(categorizedElement.getCategorizedResource());
			if(categories == null) {
				categories = new ArrayList<>();
				categorizedElementMap.put(categorizedElement.getCategorizedResource(), categories);
			}
			categories.add(categorizedElement.getCategory());
		}
		
		List<Assignment> assignments = portfolioService.searchOwnedAssignments(getIdentity());
		Map<Page,List<Assignment>> pageToAssignments = new HashMap<>();
		for(Assignment assignment:assignments) {
			Page page = assignment.getPage();
			List<Assignment> assignmentList;
			if(pageToAssignments.containsKey(page)) {
				assignmentList = pageToAssignments.get(page);
			} else {
				assignmentList = new ArrayList<>();
				pageToAssignments.put(page, assignmentList);
			}
			assignmentList.add(assignment);
		}
		
		List<Page> pages = portfolioService.searchOwnedPages(getIdentity(), searchString);
		List<PortfolioElementRow> rows = new ArrayList<>(pages.size());
		List<TimelinePoint> points = new ArrayList<>(pages.size());
		Set<Long> pageBodyKeys = new HashSet<>();
		for (Page page : pages) {
			if(page.getPageStatus() == PageStatus.deleted || pageBodyKeys.contains(page.getBody().getKey())) {
				continue;
			}
			
			List<Assignment> assignmentList = pageToAssignments.get(page);
			PortfolioElementRow row = forgePageRow(ureq, page, null, assignmentList, categorizedElementMap, numberOfCommentsMap, true);
			rows.add(row);
			if(page.getSection() != null) {
				Section section = page.getSection();
				row.setMetaSectionTitle(section.getTitle());
				if(section.getBinder() != null) {
					row.setMetaBinderTitle(section.getBinder().getTitle());
				}
			}

			String s = page.getPageStatus() == null ? "draft" : page.getPageStatus().name();
			points.add(new TimelinePoint(page.getKey().toString(), page.getTitle(), page.getCreationDate(), s));
			pageBodyKeys.add(page.getBody().getKey());
		}

		disposeRows();//clean up the posters
		model.setFlat(true);
		model.setObjects(rows);
		tableEl.reset();
		tableEl.reloadData();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof FlexiTableRenderEvent) {
				FlexiTableRenderEvent re = (FlexiTableRenderEvent)event;
				if(re.getRendererType() == FlexiTableRendererType.custom) {
					tableEl.sort(new SortKey(null, false));
				}
			} else if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if("select-page".equals(se.getCommand())) {
					PortfolioElementRow row = model.getObject(se.getIndex());
					doOpenPage(ureq, row.getPage(), true);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void doOpenPage(UserRequest ureq, Page reloadedPage, boolean newElement) {
		fireEvent(ureq, new PageSelectionEvent(reloadedPage, currentSection));
	}
}

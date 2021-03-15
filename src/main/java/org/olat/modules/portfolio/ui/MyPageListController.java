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
import java.util.List;
import java.util.Map;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRenderEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.OLATResourceable;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.BinderConfiguration;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.Category;
import org.olat.modules.portfolio.CategoryToElement;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PageStatus;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.manager.PortfolioServiceSearchOptions;
import org.olat.modules.portfolio.ui.component.TimelinePoint;
import org.olat.modules.portfolio.ui.model.PortfolioElementRow;

/**
 * 
 * Initial date: 09.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MyPageListController extends AbstractPageListController {
	
	private Link newEntryLink;
	
	private CloseableModalController cmc;
	private PageMetadataEditController newPageCtrl;

	public MyPageListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			BinderSecurityCallback secCallback) {
		super(ureq, wControl, stackPanel, secCallback, BinderConfiguration.createMyPagesConfig(), "my_pages",
				false, true, true);

		initForm(ureq);
		loadModel(ureq, null);
		loadCategoriesFilter();
		loadCompetenciesFilter();
	}

	@Override
	public void initTools() {
		newEntryLink = LinkFactory.createToolLink("new.page", translate("create.new.page"), this);
		newEntryLink.setIconLeftCSS("o_icon o_icon-lg o_icon_new_portfolio");
		newEntryLink.setElementCssClass("o_sel_pf_new_entry");
		stackPanel.addTool(newEntryLink, Align.right);
	}
	
	@Override
	protected String getTimelineSwitchPreferencesName() {
		return "entries-timeline-switch";
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
		
		FormLink newEntryButton = uifactory.addFormLink("new.entry." + (++counter), "new.entry", "create.new.page", null, flc, Link.BUTTON);
		newEntryButton.setCustomEnabledLinkCSS("btn btn-primary");
		
		PortfolioServiceSearchOptions options = new PortfolioServiceSearchOptions(null, null, searchString, activeCompetenceFilters, activeCategoryFilters);
		options.setOwner(getIdentity());
		List<Page> pages = portfolioService.getPages(options);
		List<PortfolioElementRow> rows = new ArrayList<>(pages.size());
		List<TimelinePoint> points = new ArrayList<>(pages.size());
		for (Page page : pages) {
			if(page.getPageStatus() == PageStatus.deleted) {
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
			
			row.setNewFloatingEntryLink(newEntryButton);

			String s = page.getPageStatus() == null ? "draft" : page.getPageStatus().name();
			points.add(new TimelinePoint(page.getKey().toString(), page.getTitle(), page.getCreationDate(), s));
		}

		timelineEl.setPoints(points);
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
				String cmd = se.getCommand();
				if("select-page".equals(cmd)) {
					PortfolioElementRow row = model.getObject(se.getIndex());
					doOpenRow(ureq, row, false);
				}
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("new.entry".equals(cmd)) {
				doCreateNewPage(ureq);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(newEntryLink == source) {
			doCreateNewPage(ureq);
		}
		super.event(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(newPageCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel(ureq, null);
				doOpenPage(ureq, newPageCtrl.getPage(), true);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(newPageCtrl);
		removeAsListenerAndDispose(cmc);
		newPageCtrl = null;
		cmc = null;
	}
	
	protected void doCreateNewPage(UserRequest ureq) {
		if(guardModalController(newPageCtrl)) return;
		
		newPageCtrl = new PageMetadataEditController(ureq, getWindowControl(), secCallback,
				null, true, (Section)null, true, null);
		listenTo(newPageCtrl);
		
		String title = translate("create.new.page");
		cmc = new CloseableModalController(getWindowControl(), null, newPageCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
}

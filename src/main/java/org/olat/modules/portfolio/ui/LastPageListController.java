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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.link.Link;
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
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.ui.event.OpenMyPagesEvent;
import org.olat.modules.portfolio.ui.event.OpenPageEvent;
import org.olat.modules.portfolio.ui.model.PortfolioElementRow;

/**
 * 
 * Initial date: 18 sept. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LastPageListController extends AbstractPageListController {
	
	private final int numOfPages;
	
	private FormLink allPagesLink;
	
	public LastPageListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			BinderSecurityCallback secCallback, int numOfPages) {
		super(ureq, wControl, stackPanel, secCallback, BinderConfiguration.createMyPagesConfig(), "last_pages",
				false, true, true);
		this.numOfPages = numOfPages;

		initForm(ureq);
		loadModel(ureq, null);
	}
	
	public boolean hasPages() {
		return model.getRowCount() > 0;
	}
	
	@Override
	protected String getTimelineSwitchPreferencesName() {
		return "last-pages";
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initForm(formLayout, listener, ureq);
		
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setSearchEnabled(false);
		
		allPagesLink = uifactory.addFormLink("all.pages", "all.pages", "all.pages", null, formLayout, Link.LINK | Link.NONTRANSLATED);
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
		
		List<Page> pages = portfolioService.searchOwnedLastPages(getIdentity(), numOfPages);
		List<PortfolioElementRow> rows = new ArrayList<>(pages.size());
		for (Page page : pages) {
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
		}

		disposeRows();//clean up the posters
		model.setFlat(true);
		model.setObjects(rows);
		tableEl.reset();
		tableEl.reloadData();
		
		int numOfOwnedPages = portfolioService.countOwnedPages(getIdentity());
		String allPagesKey = (numOfPages <= 1 ? "all.page" : "all.pages");
		allPagesLink.setI18nKey(translate(allPagesKey, new String[] { Integer.toString(numOfOwnedPages)} ));
		allPagesLink.setIconRightCSS("o_icon o_icon_start");
	}
	
	

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(allPagesLink == source) {
			fireEvent(ureq, new OpenMyPagesEvent());
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void doOpenPage(UserRequest ureq, Page reloadedPage, boolean newElement) {
		fireEvent(ureq, new OpenPageEvent(reloadedPage));
	}
}

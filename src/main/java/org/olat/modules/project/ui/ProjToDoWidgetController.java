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
package org.olat.modules.project.ui;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.persistence.SortKey;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.project.ProjProject;
import org.olat.modules.project.ProjProjectSecurityCallback;
import org.olat.modules.project.ProjToDoSearchParams;
import org.olat.modules.project.ProjectStatus;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTaskSearchParams;
import org.olat.modules.todo.ui.ToDoTaskDataModel.ToDoTaskCols;
import org.olat.modules.todo.ui.ToDoTaskRow;

/**
 * 
 * Initial date: 27 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjToDoWidgetController extends ProjToDoListController {
	
	private static final Integer NUM_MAX_ROWS = 6;
	private static final Set<ToDoTaskCols> COLS = Set.of(ToDoTaskCols.title, ToDoTaskCols.priority,
			ToDoTaskCols.dueDate, ToDoTaskCols.tags);
	
	private FormLink titleLink;
	private FormLink createLink;
	private FormLink showAllLink;

	private final ProjectBCFactory bcFactory;
	
	public ProjToDoWidgetController(UserRequest ureq, WindowControl wControl, ProjectBCFactory bcFactory, ProjProject project,
			ProjProjectSecurityCallback secCallback, Date lastVisitDate, MapperKey avatarMapperKey) {
		super(ureq, wControl, "todo_widget", avatarMapperKey, project, secCallback, lastVisitDate);
		this.bcFactory = bcFactory;
		
		initForm(ureq);
		doSelectFilterTab(null);
		reload(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initForm(formLayout, listener, ureq);
		
		titleLink = uifactory.addFormLink("todo.widget.title", formLayout);
		titleLink.setIconRightCSS("o_icon o_icon_start");
		titleLink.setElementCssClass("o_link_plain");
		String url = bcFactory.getNotesUrl(project);
		titleLink.setUrl(url);
		
		createLink = uifactory.addFormLink("todo.create", "", null, formLayout, Link.BUTTON + Link.NONTRANSLATED);
		createLink.setIconLeftCSS("o_icon o_icon_add");
		createLink.setTitle(translate("todo.create"));
		createLink.setGhost(true);
		createLink.setVisible(secCallback.canCreateNotes());
		
		showAllLink = uifactory.addFormLink("todo.show.all", "", null, formLayout, Link.LINK + Link.NONTRANSLATED);
		showAllLink.setUrl(url);
	}
	
	@Override
	protected ToDoTaskSearchParams createSearchParams() {
		ToDoTaskSearchParams searchParams = super.createSearchParams();
		searchParams.setStatus(List.of(ToDoStatus. open, ToDoStatus.inProgress));
		return searchParams;
	}
	
	@Override
	protected boolean isVisible(ToDoTaskCols col) {
		return COLS.contains(col);
	}
	
	@Override
	protected boolean isFiltersEnabled() {
		return false;
	}

	@Override
	protected boolean isNumOfRowsEnabled() {
		return false;
	}
	
	@Override
	protected boolean isCustomizeColumns() {
		return false;
	}
	
	@Override
	protected boolean isShowDetails() {
		return false;
	}
	
	@Override
	protected String getEmptyMessageI18nKey() {
		return "todo.widget.empty.message";
	}

	@Override
	protected void applyFilters(List<ToDoTaskRow> rows) {
		applyFilterMy(rows);
	}

	@Override
	protected SortKey getSortKey() {
		return new SortKey(ToDoTaskCols.dueDate.name(), true);
	}
	
	@Override
	protected Integer getMaxRows() {
		return NUM_MAX_ROWS;
	}

	@Override
	public void reload(UserRequest ureq) {
		super.reload(ureq);
		
		ProjToDoSearchParams searchParams = new ProjToDoSearchParams();
		searchParams.setProject(project);
		searchParams.setStatus(List.of(ProjectStatus.active));
		long count = projectService.getToDosCount(searchParams);
		
		showAllLink.setI18nKey(translate("todo.show.all", String.valueOf(count)));
		showAllLink.setVisible(count > 0);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == titleLink || source == showAllLink) {
			fireEvent(ureq, ProjProjectDashboardController.SHOW_ALL);
		} else if (source == createLink) {
			doCreateToDoTask(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}

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
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import org.olat.core.commons.services.commentAndRating.CommentAndRatingDefaultSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.ui.UserCommentsController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.portfolio.AssessmentSection;
import org.olat.modules.portfolio.Assignment;
import org.olat.modules.portfolio.AssignmentStatus;
import org.olat.modules.portfolio.AssignmentType;
import org.olat.modules.portfolio.BinderConfiguration;
import org.olat.modules.portfolio.BinderSecurityCallback;
import org.olat.modules.portfolio.Category;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.ui.PageListDataModel.PageCols;
import org.olat.modules.portfolio.ui.component.TimelineElement;
import org.olat.modules.portfolio.ui.model.AssignmentPageRow;
import org.olat.modules.portfolio.ui.model.PageRow;
import org.olat.modules.portfolio.ui.renderer.StatusCellRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 09.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractPageListController extends FormBasicController
implements Activateable2, TooledController, FlexiTableComponentDelegate {

	protected TimelineElement timelineEl;
	private FormLink timelineSwitchOnButton, timelineSwitchOffButton;
	
	protected FlexiTableElement tableEl;
	protected PageListDataModel model;
	protected final TooledStackedPanel stackPanel;
	
	private PageRunController pageCtrl;
	private CloseableModalController cmc;
	private UserCommentsController commentsCtrl;
	private AssignmentEditController editAssignmentCtrl;
	
	protected int counter;
	protected final boolean withSections;
	protected final BinderConfiguration config;
	protected final BinderSecurityCallback secCallback;
	
	@Autowired
	protected PortfolioService portfolioService;
	
	public AbstractPageListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			BinderSecurityCallback secCallback, BinderConfiguration config, String vTemplate, boolean withSections) {
		super(ureq, wControl, vTemplate);
		this.config = config;
		this.stackPanel = stackPanel;
		this.secCallback = secCallback;
		this.withSections = withSections;
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		timelineEl = new TimelineElement("timeline");
		timelineEl.setContainerId("o_portfolio_entries_timeline");
		formLayout.add("timeline", timelineEl);
		initTimeline();
		
		if(config.isTimeline()) {
			timelineSwitchOnButton = uifactory.addFormLink("timeline.switch.on", formLayout, Link.BUTTON_SMALL);
			timelineSwitchOnButton.setIconLeftCSS("o_icon o_icon-sm o_icon_toggle_on");
			timelineSwitchOnButton.setElementCssClass("o_sel_timeline_on");
			
			timelineSwitchOffButton = uifactory.addFormLink("timeline.switch.off", formLayout, Link.BUTTON_SMALL); 
			timelineSwitchOffButton.setIconLeftCSS("o_icon o_icon-sm o_icon_toggle_off");
			timelineSwitchOffButton.setElementCssClass("o_sel_timeline_off");
			doSwitchTimelineOn();
		} else {
			flc.contextPut("timelineSwitch", Boolean.FALSE);
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, PageCols.key, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PageCols.title, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PageCols.date, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PageCols.status, new StatusCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PageCols.publicationDate, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, PageCols.open, null));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, PageCols.newEntry, null));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, PageCols.newAssignment, null));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, PageCols.comment, null));
	
		model = new PageListDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		tableEl.setSearchEnabled(true);
		tableEl.setCustomizeColumns(true);
		tableEl.setElementCssClass("o_binder_page_listing");
		tableEl.setEmtpyTableMessageKey("table.sEmptyTable");
		tableEl.setPageSize(24);
		VelocityContainer row = createVelocityContainer("page_row");
		row.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		tableEl.setRowRenderer(row, this);
		tableEl.setCssDelegate(new DefaultFlexiTableCssDelegate());
		tableEl.setAndLoadPersistedPreferences(ureq, "page-list");
	}
	
	@Override
	public void initTools() {
		//
	}
	
	private void initTimeline() {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MILLISECOND, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.add(Calendar.DATE, 1);
		timelineEl.setEndTime(cal.getTime());
		cal.add(Calendar.YEAR, -2);
		timelineEl.setStartTime(cal.getTime());
	}
	
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		PageRow pageRow = model.getObject(row);
		List<Component> components = new ArrayList<>(4);
		if(pageRow.hasAssignments()) {
			for(AssignmentPageRow assignmentRow:pageRow.getAssignments()) {
				if(assignmentRow.getEditLink() != null) {
					components.add(assignmentRow.getEditLink().getComponent());
				}
			}	
		}
		return components;
	}
	
	protected abstract void loadModel(String searchString);
	
	protected PageRow forgeRow(Page page, AssessmentSection assessmentSection, List<Assignment> assignments,
			boolean firstOfSection,
			Map<OLATResourceable,List<Category>> categorizedElementMap, Map<Long,Long> numberOfCommentsMap) {

		PageRow row = new PageRow(page, page.getSection(), assessmentSection, firstOfSection, config.isAssessable());
		String openLinkId = "open_" + (++counter);
		FormLink openLink = uifactory.addFormLink(openLinkId, "open.full", "open.full.page", null, flc, Link.BUTTON_SMALL);
		openLink.setIconRightCSS("o_icon o_icon_start");
		openLink.setPrimary(true);
		row.setOpenFormLink(openLink);
		openLink.setUserObject(row);
		addAssignmentsToRow(row, assignments);
		addCategoriesToRow(row, categorizedElementMap);
		
		if(numberOfCommentsMap != null) {
			Long numOfComments = numberOfCommentsMap.get(page.getKey());
			if(numOfComments != null) {
				row.setNumOfComments(numOfComments.longValue());
			} else {
				row.setNumOfComments(0);
			}
		} else {
			row.setNumOfComments(0);
		}
		
		if(secCallback.canComment(page)) {
			String commentLinkId = "comment_" + (++counter);
			
			String title;
			if(row.getNumOfComments() == 1) {
				title = translate("comment.one");
			} else if(row.getNumOfComments() > 1) {
				title = translate("comment.several", new String[]{ Long.toString(row.getNumOfComments()) });
			} else {
				title = translate("comment.zero");
			}
			FormLink commentLink = uifactory.addFormLink(commentLinkId, "comment", title, null, flc, Link.LINK | Link.NONTRANSLATED);
			commentLink.setCustomEnabledLinkCSS("btn btn-sm o_portfolio_comment");
			commentLink.setUserObject(row);
			row.setCommentFormLink(commentLink);
		}
		return row;
	}
	
	protected PageRow forgeRow(Section section, AssessmentSection assessmentSection, List<Assignment> assignments,
			boolean firstOfSection,
			Map<OLATResourceable,List<Category>> categorizedElementMap) {
		
		PageRow row = new PageRow(null, section, assessmentSection, firstOfSection, config.isAssessable());
		String openLinkId = "open_" + (++counter);
		FormLink openLink = uifactory.addFormLink(openLinkId, "open.full", "open.full.page", null, flc, Link.BUTTON_SMALL);
		openLink.setIconRightCSS("o_icon o_icon_start");
		openLink.setPrimary(true);
		row.setOpenFormLink(openLink);
		openLink.setUserObject(row);
		addAssignmentsToRow(row, assignments);
		addCategoriesToRow(row, categorizedElementMap);
		return row;
	}
	
	private void addAssignmentsToRow(PageRow row, List<Assignment> assignments) {
		if(assignments != null && assignments.size() > 0) {
			List<AssignmentPageRow> assignmentRows = new ArrayList<>();
			for(Assignment assignment:assignments) {
				AssignmentPageRow assignmentRow = new AssignmentPageRow(assignment);
				
				if(secCallback.canAddAssignment()) {
					if(assignment.getTemplateReference() == null) {
						FormLink editLink = uifactory.addFormLink("edit_assign_" + (++counter), "edit.assignment", "edit", null, flc, Link.LINK);
						editLink.setUserObject(assignmentRow);
						assignmentRow.setEditLink(editLink);
					}
				} else {
					if(assignment.getAssignmentStatus() == AssignmentStatus.notStarted) {
						FormLink startLink = uifactory.addFormLink("create_assign_" + (++counter), "start.assignment", "create.start.assignment", null, flc, Link.LINK);
						startLink.setUserObject(assignmentRow);
						assignmentRow.setEditLink(startLink);
					} else {
						FormLink openLink = uifactory.addFormLink("open_assign_" + (++counter), "open.assignment", "open", null, flc, Link.LINK);
						openLink.setUserObject(assignmentRow);
						assignmentRow.setEditLink(openLink);
					}
				}
				
				assignmentRows.add(assignmentRow);
			}
			row.setAssignments(assignmentRows);
		}
	}
	
	private void addCategoriesToRow(PageRow row, Map<OLATResourceable,List<Category>> categorizedElementMap) {
		if(categorizedElementMap != null) {
			if(row.getPage() != null) {
				OLATResourceable ores = OresHelper.createOLATResourceableInstance(Page.class, row.getPage().getKey());
				row.setPageCategories(getCategories(ores, categorizedElementMap));
			}
		}
	}
	
	private List<String> getCategories(OLATResourceable ores, Map<OLATResourceable,List<Category>> categorizedElementMap) {
		List<String> strings = null;
		List<Category> categories = categorizedElementMap.get(ores);
		if(categories != null && categories.size() > 0) {
			strings = new ArrayList<>(categories.size());
			for(Category category:categories) {
				strings.add(category.getName());
			}
		}
		return strings;
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String resName = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Page".equalsIgnoreCase(resName) || "Entry".equalsIgnoreCase(resName)) {
			Long resId = entries.get(0).getOLATResourceable().getResourceableId();
			for(PageRow row :model.getObjects()) {
				if(row.getKey().equals(resId)) {
					doOpenPage(ureq, row);
					break;
				}
			}
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(pageCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				loadModel(null);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		} else if(commentsCtrl == source) {
			if(event == Event.CHANGED_EVENT || "comment_count_changed".equals(event.getCommand())) {
				loadModel(null);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(editAssignmentCtrl == source) {
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				loadModel(null);
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
		removeAsListenerAndDispose(editAssignmentCtrl);
		removeAsListenerAndDispose(commentsCtrl);
		removeAsListenerAndDispose(cmc);
		editAssignmentCtrl = null;
		commentsCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof FlexiTableSearchEvent) {
				FlexiTableSearchEvent se = (FlexiTableSearchEvent)event;
				loadModel(se.getSearch());
			}
		} else if(timelineSwitchOnButton == source) {
			doSwitchTimelineOff();
		} else if(timelineSwitchOffButton == source) {
			doSwitchTimelineOn();
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("open.full".equals(cmd)) {
				PageRow row = (PageRow)link.getUserObject();
				doOpenPage(ureq, row);
			} else if("comment".equals(cmd)) {
				PageRow row = (PageRow)link.getUserObject();
				doComment(ureq, row.getPage());
			} else if("edit.assignment".equals(cmd)) {
				AssignmentPageRow row = (AssignmentPageRow)link.getUserObject();
				doEditAssignment(ureq, row);
			} else if("start.assignment".equals(cmd)) {
				AssignmentPageRow row = (AssignmentPageRow)link.getUserObject();
				doStartAssignment(ureq, row);
			} else if("open.assignment".equals(cmd)) {
				AssignmentPageRow row = (AssignmentPageRow)link.getUserObject();
				doOpenAssignment(ureq, row);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doSwitchTimelineOn() {
		timelineSwitchOnButton.setVisible(true);
		timelineSwitchOffButton.setVisible(false);
		flc.contextPut("timelineSwitch", Boolean.TRUE);
	}
	
	private void doSwitchTimelineOff() {
		timelineSwitchOnButton.setVisible(false);
		timelineSwitchOffButton.setVisible(true);
		flc.contextPut("timelineSwitch", Boolean.FALSE);
	}
	
	private void doStartAssignment(UserRequest ureq, AssignmentPageRow row) {
		Assignment assignment = row.getAssignment();
		Assignment startedAssigment = portfolioService.startAssignment(assignment, getIdentity());
		row.setAssignment(startedAssigment);
		doOpenPage(ureq, startedAssigment.getPage());
	}
	
	private void doOpenAssignment(UserRequest ureq, AssignmentPageRow row) {
		Assignment assignment = row.getAssignment();
		if(assignment.getAssignmentType() == AssignmentType.essay) {
			Page page = assignment.getPage();
			Page reloadedPage = portfolioService.getPageByKey(page.getKey());
			doOpenPage(ureq, reloadedPage);
		} else {
			showWarning("not.implemented");
		}
	}
	
	private void doEditAssignment(UserRequest ureq, AssignmentPageRow row) {
		if(editAssignmentCtrl != null) return;
		
		Assignment assignment = row.getAssignment();
		editAssignmentCtrl = new AssignmentEditController(ureq, getWindowControl(), assignment);
		listenTo(editAssignmentCtrl);
		
		String title = translate("edit.assignment");
		cmc = new CloseableModalController(getWindowControl(), null, editAssignmentCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doComment(UserRequest ureq, Page page) {
		CommentAndRatingSecurityCallback commentSecCallback = new CommentAndRatingDefaultSecurityCallback(getIdentity(), false, false);
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Page.class, page.getKey());
		commentsCtrl = new UserCommentsController(ureq, getWindowControl(), ores, null, commentSecCallback);
		listenTo(commentsCtrl);
		
		String title = translate("comment.title");
		cmc = new CloseableModalController(getWindowControl(), null, commentsCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	protected void doOpenPage(UserRequest ureq, PageRow row) {
		Page reloadedPage = portfolioService.getPageByKey(row.getKey());
		doOpenPage(ureq, reloadedPage);
	}
	
	protected void doOpenPage(UserRequest ureq, Page reloadedPage) {
		OLATResourceable pageOres = OresHelper.createOLATResourceableInstance("Entry", reloadedPage.getKey());
		WindowControl swControl = addToHistory(ureq, pageOres, null);
		pageCtrl = new PageRunController(ureq, swControl, stackPanel, secCallback, reloadedPage);
		listenTo(pageCtrl);
		
		String displayName = StringHelper.escapeHtml(reloadedPage.getTitle());
		stackPanel.pushController(displayName, pageCtrl);
	}
}
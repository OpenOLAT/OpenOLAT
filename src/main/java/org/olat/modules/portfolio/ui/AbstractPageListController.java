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

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.commons.services.commentAndRating.CommentAndRatingDefaultSecurityCallback;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.ReadOnlyCommentsSecurityCallback;
import org.olat.core.commons.services.commentAndRating.ui.UserCommentsController;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.image.ImageComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.control.winmgr.ScrollTopCommand;
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.logging.activity.ThreadLocalUserActivityLogger;
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
import org.olat.modules.portfolio.PageImageAlign;
import org.olat.modules.portfolio.PageStatus;
import org.olat.modules.portfolio.PortfolioLoggingAction;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.portfolio.Section;
import org.olat.modules.portfolio.SectionStatus;
import org.olat.modules.portfolio.ui.PageListDataModel.PageCols;
import org.olat.modules.portfolio.ui.component.CategoriesCellRenderer;
import org.olat.modules.portfolio.ui.component.TimelineElement;
import org.olat.modules.portfolio.ui.event.ClosePageEvent;
import org.olat.modules.portfolio.ui.event.PageDeletedEvent;
import org.olat.modules.portfolio.ui.event.PageRemovedEvent;
import org.olat.modules.portfolio.ui.event.SelectPageEvent;
import org.olat.modules.portfolio.ui.model.PortfolioElementRow;
import org.olat.modules.portfolio.ui.renderer.PortfolioElementCellRenderer;
import org.olat.modules.portfolio.ui.renderer.SharedPageStatusCellRenderer;
import org.olat.modules.portfolio.ui.renderer.StatusCellRenderer;
import org.olat.util.logging.activity.LoggingResourceable;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 09.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractPageListController extends FormBasicController
implements Activateable2, TooledController, FlexiTableComponentDelegate {
	
	public static final int PICTURE_WIDTH = 970 * 2;	// max width for large images: 1294 * 75% , x2 for high res displays
	public static final int PICTURE_HEIGHT = 230 * 2 ; 	// max size for large images, see CSS, x2 for high res displays

	protected TimelineElement timelineEl;
	private FormLink timelineSwitchOnButton;
	private FormLink timelineSwitchOffButton;
	
	protected FlexiTableElement tableEl;
	protected PageListDataModel model;
	protected final TooledStackedPanel stackPanel;
	protected final VelocityContainer rowVC;
	
	protected PageRunController pageCtrl;
	private CloseableModalController cmc;
	private UserCommentsController commentsCtrl;
	private AssignmentEditController editAssignmentCtrl;
	private AssignmentMoveController moveAssignmentCtrl;
	private DialogBoxController confirmCloseSectionCtrl;
	private DialogBoxController confirmReopenSectionCtrl;
	private DialogBoxController confirmDeleteAssignmentCtrl;
	
	protected int counter;
	protected final boolean flatList;
	protected final boolean withSections;
	protected final boolean withComments;
	protected final BinderConfiguration config;
	protected final BinderSecurityCallback secCallback;
	
	@Autowired
	protected PortfolioService portfolioService;
	@Autowired
	private PortfolioV2Module portfolioV2Module;
	
	public AbstractPageListController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			BinderSecurityCallback secCallback, BinderConfiguration config, String vTemplate,
			boolean withSections, boolean withComments, boolean flatList) {
		super(ureq, wControl, vTemplate);
		this.config = config;
		this.flatList = flatList;
		this.stackPanel = stackPanel;
		this.secCallback = secCallback;
		this.withSections = withSections;
		this.withComments = withComments;
		rowVC = createVelocityContainer("portfolio_element_row");
	}
	
	public int getNumOfPages() {
		int count = 0;
		if(model != null) {
			List<PortfolioElementRow> rows = model.getObjects();
			for(PortfolioElementRow row:rows) {
				if(row.isPage()) {
					count++;
				}
			}
		}
		return count;
	}
	
	public PortfolioElementRow getFirstPage() {
		if(model != null) {
			List<PortfolioElementRow> rows = model.getObjects();
			for(PortfolioElementRow row:rows) {
				if(row.isPage()) {
					return row;
				}
			}
		}
		return null;
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		timelineEl = new TimelineElement("timeline");
		timelineEl.setContainerId("o_portfolio_entries_timeline_" + timelineEl.getComponent().getDispatchID());
		formLayout.add("timeline", timelineEl);
		initTimeline();
		
		String preferencesName = getTimelineSwitchPreferencesName();
		if(portfolioV2Module.isEntriesTimelineEnabled() && config.isTimeline()) {
			timelineSwitchOnButton = uifactory.addFormLink("timeline.switch.on", formLayout, Link.BUTTON_SMALL);
			timelineSwitchOnButton.setIconLeftCSS("o_icon o_icon-sm o_icon_toggle_on");
			timelineSwitchOnButton.setElementCssClass("o_sel_timeline_on");
			
			timelineSwitchOffButton = uifactory.addFormLink("timeline.switch.off", formLayout, Link.BUTTON_SMALL); 
			timelineSwitchOffButton.setIconLeftCSS("o_icon o_icon-sm o_icon_toggle_off");
			timelineSwitchOffButton.setElementCssClass("o_sel_timeline_off");
			
			Object prefs = ureq.getUserSession().getGuiPreferences().get(this.getClass(), preferencesName, "on");
			if("on".equals(prefs)) {
				doSwitchTimelineOn(ureq, false);
			} else {
				doSwitchTimelineOff(ureq, false);
			}
		} else {
			flc.contextPut("timelineSwitch", Boolean.FALSE);
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		initColumns(columnsModel);
	
		model = new PageListDataModel(columnsModel, getLocale());
		String mapperThumbnailUrl = registerCacheableMapper(ureq, "page-list", new PageImageMapper(model, portfolioService));
		
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		if (portfolioV2Module.isEntriesListEnabled() && portfolioV2Module.isEntriesTableEnabled()) {
			tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		} else if (portfolioV2Module.isEntriesTableEnabled()) {
			tableEl.setAvailableRendererTypes(FlexiTableRendererType.classic);
		} else {
			tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom);
		}
		tableEl.setSearchEnabled(portfolioV2Module.isEntriesSearchEnabled());
		tableEl.setCustomizeColumns(true);
		String cssClass = "o_binder_page_listing " + (flatList ? "o_binder_page_flat_listing" : "o_binder_page_tree_listing");
		tableEl.setElementCssClass(cssClass);
		tableEl.setEmtpyTableMessageKey("table.sEmptyTable");
		tableEl.setPageSize(24);
		rowVC.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		rowVC.contextPut("mapperThumbnailUrl", mapperThumbnailUrl);
		tableEl.setRowRenderer(rowVC, this);
		tableEl.setCssDelegate(new DefaultFlexiTableCssDelegate());
		FlexiTableRendererType renderType = portfolioV2Module.isEntriesListEnabled() ? FlexiTableRendererType.custom: FlexiTableRendererType.classic;
		tableEl.setRendererType(renderType);
		tableEl.setAndLoadPersistedPreferences(ureq, "page-list-v2-".concat(preferencesName));
	}
	
	protected void initColumns(FlexiTableColumnModel columnsModel) {
		if(flatList) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PageCols.type,
				new PortfolioElementCellRenderer(getTranslator())));
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, PageCols.key, "select-page"));
		if(flatList) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PageCols.title, "select-page"));
		} else {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PageCols.title, "select-page",
				new PortfolioElementCellRenderer(getTranslator())));
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PageCols.date, "select-page"));
		if(secCallback.canPageUserInfosStatus()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PageCols.viewerStatus, new SharedPageStatusCellRenderer(getTranslator())));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PageCols.pageStatus, new StatusCellRenderer(getTranslator())));
		} else {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PageCols.status, new StatusCellRenderer(getTranslator())));
		}
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PageCols.publicationDate, "select-page"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PageCols.categories, new CategoriesCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, PageCols.section/*, "select-section"*/));
		if(secCallback.canNewAssignment()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.up", PageCols.up.ordinal(), "up",
					new BooleanCellRenderer(
							new StaticFlexiCellRenderer(translate("up"), "up"), null)));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.down", PageCols.down.ordinal(), "down",
					new BooleanCellRenderer(
							new StaticFlexiCellRenderer(translate("down"), "down"), null)));
		}
		if(!secCallback.canNewAssignment() && withComments) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(PageCols.comment));
		}
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
		cal.add(Calendar.YEAR, -1);
		timelineEl.setStartTime(cal.getTime());
	}
	
	@Override
	public final Iterable<Component> getComponents(int row, Object rowObject) {
		PortfolioElementRow elRow = model.getObject(row);
		List<Component> components = new ArrayList<>(4);
		if(elRow.getNewEntryLink() != null) {
			components.add(elRow.getNewEntryLink().getComponent());
		}
		if(elRow.getNewFloatingEntryLink() != null) {
			components.add(elRow.getNewFloatingEntryLink().getComponent());
		}
		if(elRow.getNewAssignmentLink() != null) {
			components.add(elRow.getNewAssignmentLink().getComponent());
		}
		if(elRow.getOpenFormItem() != null) {
			components.add(elRow.getOpenFormItem().getComponent());
		}
		if(elRow.getReopenSectionLink() != null) {
			components.add(elRow.getReopenSectionLink().getComponent());
		}
		if(elRow.getCloseSectionLink() != null) {
			components.add(elRow.getCloseSectionLink().getComponent());
		}
		if(elRow.getEditAssignmentLink() != null) {
			components.add(elRow.getEditAssignmentLink().getComponent());
		}
		if(elRow.getDeleteAssignmentLink() != null) {
			components.add(elRow.getDeleteAssignmentLink().getComponent());
		}
		if(elRow.getUpAssignmentLink() != null) {
			components.add(elRow.getUpAssignmentLink().getComponent());
		}
		if(elRow.getDownAssignmentLink() != null) {
			components.add(elRow.getDownAssignmentLink().getComponent());
		}
		if(elRow.getMoveAssignmentLink() != null) {
			components.add(elRow.getMoveAssignmentLink().getComponent());
		}
		if(elRow.getInstantiateAssignmentLink() != null) {
			components.add(elRow.getInstantiateAssignmentLink().getComponent());
		}
		if(elRow.getCommentFormLink() != null) {
			components.add(elRow.getCommentFormLink().getComponent());
		}
		if(elRow.getPoster() != null) {
			components.add(elRow.getPoster());
		}
		if(elRow.getStartSelection() != null) {
			components.add(elRow.getStartSelection().getComponent());
		}
		return components;
	}
	
	protected abstract void loadModel(UserRequest ureq, String searchString);
	
	protected void disposeRows() {
		List<PortfolioElementRow> rows = model.getObjects();
		if(rows != null && !rows.isEmpty()) {
			for(PortfolioElementRow row:rows) {
				if(row.getPoster() != null) {
					row.getPoster().dispose();
					row.setPoster(null);
				}
			}
		}
	}
	
	protected PortfolioElementRow forgeSectionRow(Section section, AssessmentSection assessmentSection,
			List<Assignment> assignments, Map<OLATResourceable, List<Category>> categorizedElementMap) {
		
		PortfolioElementRow row = new PortfolioElementRow(section, assessmentSection,
				config.isAssessable(), (assignments != null && !assignments.isEmpty()));
		String openLinkId = "open_" + (++counter);
		FormLink openLink = uifactory.addFormLink(openLinkId, "open.full", "open.full.page", null, flc, Link.BUTTON_SMALL);
		openLink.setIconRightCSS("o_icon o_icon_start");
		openLink.setPrimary(true);
		row.setOpenFormLink(openLink);
		openLink.setUserObject(row);
		addCategoriesToRow(row, categorizedElementMap);

		if (assignments != null && secCallback.canViewPendingAssignments(section)
				&& secCallback.canInstantiateAssignment()) {
			List<Assignment> startableAssignments = assignments.stream()
					.filter(ass -> ass.getAssignmentStatus() == AssignmentStatus.notStarted)
					.filter(ass -> ass.getPage() == null)
					.collect(Collectors.toList());
			if (!startableAssignments.isEmpty()) {
				String[] keys = new String[startableAssignments.size() + 1];
				String[] values = new String[startableAssignments.size() + 1];
				keys[0] = "start.assignment.hint";
				values[0] = translate("start.assignment.hint");
				int count = 1;
				for (Assignment assignment: startableAssignments) {
					keys[count] = Long.toString(assignment.getKey());
					values[count] = assignment.getTitle();
					count++;
				}
		
				SingleSelection startEl = uifactory.addDropdownSingleselect("assignments_" + (++counter), "", flc, keys,
						values, null);
				startEl.setDomReplacementWrapperRequired(false);
				startEl.addActionListener(FormEvent.ONCHANGE);
				row.setStartSelection(startEl);
			}
		}
		return row;
	}
	
	protected PortfolioElementRow forgePendingAssignmentRow(Assignment assignment, Section section, List<Assignment> assignments) {
		int index = assignments == null ? 0 : assignments.indexOf(assignment);
		PortfolioElementRow row = new PortfolioElementRow(assignment, section, index);
		if(secCallback.canInstantiateAssignment()) {
			if(assignment.getAssignmentStatus() == AssignmentStatus.notStarted) {
				String title = assignment.getTitle();
				FormLink startLink = uifactory.addFormLink("create_assign_" + (++counter), "start.assignment", title, null, flc, Link.NONTRANSLATED);
				startLink.setUserObject(row);
				startLink.setIconLeftCSS("o_icon o_icon_assignment o_icon-fw");
				row.setInstantiateAssignmentLink(startLink);
			}
		} else if(secCallback.canNewAssignment()) {
			if(assignment.getTemplateReference() == null) {
				FormLink editLink = uifactory.addFormLink("edit_assign_" + (++counter), "edit.assignment", "edit", null, flc, Link.BUTTON);
				editLink.setUserObject(row);
				row.setEditAssignmentLink(editLink);
				
				FormLink deleteLink = uifactory.addFormLink("del_assign_" + (++counter), "delete.assignment", "delete", null, flc, Link.BUTTON);
				deleteLink.setUserObject(row);
				row.setDeleteAssignmentLink(deleteLink);
				
				FormLink moveLink = uifactory.addFormLink("move_assign_" + (++counter), "move.assignment", "move", null, flc, Link.BUTTON);
				moveLink.setUserObject(row);
				row.setMoveAssignmentLink(moveLink);
				
				FormLink upLink = uifactory.addFormLink("up_assign_" + (++counter), "up.assignment", "", null, flc, Link.BUTTON | Link.NONTRANSLATED);
				upLink.setIconLeftCSS("o_icon o_icon o_icon-lg o_icon_move_up");
				upLink.setEnabled(index > 0);
				upLink.setUserObject(row);
				row.setUpAssignmentLink(upLink);
				
				FormLink downLink = uifactory.addFormLink("down_assign_" + (++counter), "down.assignment", "", null, flc, Link.BUTTON | Link.NONTRANSLATED);
				downLink.setIconLeftCSS("o_icon o_icon o_icon-lg o_icon_move_down");
				downLink.setUserObject(row);
				downLink.setEnabled(assignments != null && index + 1 != assignments.size());
				row.setDownAssignmentLink(downLink);
			}
		}
		return row;
	}
	
	protected PortfolioElementRow forgePageRow(UserRequest ureq, Page page, AssessmentSection assessmentSection, List<Assignment> assignments,
			Map<OLATResourceable,List<Category>> categorizedElementMap, Map<Long,Long> numberOfCommentsMap, boolean selectElement) {

		Section section = page.getSection();
		PortfolioElementRow row = new PortfolioElementRow(page, assessmentSection, config.isAssessable());
		String openLinkId = "open_" + (++counter);
		FormLink openLink = uifactory.addFormLink(openLinkId, "open.full", "open.full.page", null, flc, Link.BUTTON_SMALL);
		openLink.setIconRightCSS("o_icon o_icon_start");
		openLink.setEnabled(selectElement);
		openLink.setPrimary(true);
		row.setOpenFormLink(openLink);
		openLink.setUserObject(row);
		addCategoriesToRow(row, categorizedElementMap);
		if(assignments != null) {
			for(Assignment assignment:assignments) {
				if(page.equals(assignment.getPage())) {
					row.setAssignment(assignment);
				}
			}
		}
		
		decorateImage(ureq, row, page);
		
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
		
		if(secCallback.canCloseSection(section)) {
			if(SectionStatus.isClosed(section)) {
				FormLink reopenLink = uifactory.addFormLink("ropens_" + (++counter), "reopen.section", "reopen.section", null, flc, Link.BUTTON_SMALL);
				reopenLink.setUserObject(row);
				row.setReopenSectionLink(reopenLink);
			} else {
				FormLink closeLink = uifactory.addFormLink("closes_" + (++counter), "close.section", "close.section", null, flc, Link.BUTTON_SMALL);
				closeLink.setUserObject(row);
				row.setCloseSectionLink(closeLink);
			}
		}
		
		if(portfolioV2Module.isEntriesCommentsEnabled() && secCallback.canComment(page)) {
			String title;
			String cssClass = "o_icon o_icon-fw o_icon_comments";
			if(row.getNumOfComments() == 1) {
				title = translate("comment.one");
			} else if(row.getNumOfComments() > 1) {
				title = translate("comment.several", new String[]{ Long.toString(row.getNumOfComments()) });
			} else {
				title = translate("comment.zero");
				cssClass += "_none";
			}
			FormLink commentLink = uifactory.addFormLink("com_" + (++counter), "comment", title, null, flc, Link.LINK | Link.NONTRANSLATED);
			commentLink.setIconLeftCSS(cssClass);
			commentLink.setUserObject(row);
			row.setCommentFormLink(commentLink);
		}
		return row;
	}
	
	private void decorateImage(UserRequest ureq, PortfolioElementRow row, Page page) {
		if(StringHelper.containsNonWhitespace(page.getImagePath())) {
			File posterImage = portfolioService.getPosterImage(page);
			if(page.getImageAlignment() == PageImageAlign.background) {
				String imageUrl = "page/" + page.getKey() + "/" + page.getImagePath();
				row.setImageUrl(imageUrl);
			} else {
				// alignment is right
				ImageComponent imageCmp = new ImageComponent(ureq.getUserSession(), "poster");
				imageCmp.setMedia(posterImage);
				imageCmp.setMaxWithAndHeightToFitWithin(PICTURE_WIDTH, PICTURE_HEIGHT);
				row.setPoster(imageCmp);
			}
		}
	}
	
	private void addCategoriesToRow(PortfolioElementRow row, Map<OLATResourceable,List<Category>> categorizedElementMap) {
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
		if(categories != null && !categories.isEmpty()) {
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
			PortfolioElementRow activatedRow = null;
			for(PortfolioElementRow row :model.getObjects()) {
				if(row.getKey() != null && row.getKey().equals(resId)) {
					activatedRow = row;
					break;
				}
			}
			if(activatedRow != null) {
				doOpenRow(ureq, activatedRow, false);
			}
		} else if("Section".equalsIgnoreCase(resName)) {
			Long resId = entries.get(0).getOLATResourceable().getResourceableId();
			for(PortfolioElementRow row :model.getObjects()) {
				if(row.getSection() != null && row.getSection().getKey().equals(resId)) {
					//doOpenPage(ureq, row);
					break;
				}
			}
		}
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(pageCtrl == source) {
			if(event == Event.CHANGED_EVENT || event instanceof ClosePageEvent) {
				loadModel(ureq, null);
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else if(event instanceof PageRemovedEvent) {
				loadModel(ureq, null);
				stackPanel.popUpToController(this);
				fireEvent(ureq, Event.CHANGED_EVENT);
			} else if(event instanceof PageDeletedEvent) {
				loadModel(ureq, null);
				fireEvent(ureq, event);
			} else if(event instanceof SelectPageEvent) {
				SelectPageEvent spe = (SelectPageEvent)event;
				if(SelectPageEvent.NEXT_PAGE.equals(spe.getCommand())) {
					doNextPage(ureq, pageCtrl.getPage());
				} else if(SelectPageEvent.PREVIOUS_PAGE.equals(spe.getCommand())) {
					doPreviousPage(ureq, pageCtrl.getPage());
				} else if(SelectPageEvent.ALL_PAGES.equals(spe.getCommand())) {
					doAllPages();
				}
			}
		} else if(commentsCtrl == source) {
			if(event == Event.CHANGED_EVENT || "comment_count_changed".equals(event.getCommand())) {
				loadModel(ureq, null);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(editAssignmentCtrl == source || moveAssignmentCtrl == source) {
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				loadModel(ureq, null);
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmCloseSectionCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				PortfolioElementRow row = (PortfolioElementRow)confirmCloseSectionCtrl.getUserObject();
				doClose(ureq, row);
			}
		} else if(confirmReopenSectionCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				PortfolioElementRow row = (PortfolioElementRow)confirmReopenSectionCtrl.getUserObject();
				doReopen(ureq, row);
			}	
		} else if(confirmDeleteAssignmentCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				PortfolioElementRow row = (PortfolioElementRow)confirmDeleteAssignmentCtrl.getUserObject();
				doDelete(row);
				loadModel(ureq, null);
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editAssignmentCtrl);
		removeAsListenerAndDispose(moveAssignmentCtrl);
		removeAsListenerAndDispose(commentsCtrl);
		removeAsListenerAndDispose(cmc);
		editAssignmentCtrl = null;
		moveAssignmentCtrl = null;
		commentsCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof FlexiTableSearchEvent) {
				FlexiTableSearchEvent se = (FlexiTableSearchEvent)event;
				loadModel(ureq, se.getSearch());
			} else if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				if("up".equals(cmd)) {
					PortfolioElementRow row = model.getObject(se.getIndex());
					if(row.isPendingAssignment()) {
						doMoveUpAssignment(ureq, row);
					}
				} else if("down".equals(cmd)) {
					PortfolioElementRow row = model.getObject(se.getIndex());
					if(row.isPendingAssignment()) {
						doMoveDownAssignment(ureq, row);
					}
				}
			}
		} else if(timelineSwitchOnButton == source) {
			doSwitchTimelineOff(ureq, true);
		} else if(timelineSwitchOffButton == source) {
			doSwitchTimelineOn(ureq, true);
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			String cmd = link.getCmd();
			if("open.full".equals(cmd)) {
				PortfolioElementRow row = (PortfolioElementRow)link.getUserObject();
				doOpenRow(ureq, row, false);
			} else if("comment".equals(cmd)) {
				PortfolioElementRow row = (PortfolioElementRow)link.getUserObject();
				doComment(ureq, row.getPage());
			} else if("close.section".equals(cmd)) {
				PortfolioElementRow row = (PortfolioElementRow)link.getUserObject();
				doConfirmCloseSection(ureq, row);
			} else if("reopen.section".equals(cmd)) {
				PortfolioElementRow row = (PortfolioElementRow)link.getUserObject();
				doConfirmReopenSection(ureq, row);
			} else if("edit.assignment".equals(cmd)) {
				PortfolioElementRow row = (PortfolioElementRow)link.getUserObject();
				doEditAssignment(ureq, row);
			} else if("delete.assignment".equals(cmd)) {
				PortfolioElementRow row = (PortfolioElementRow)link.getUserObject();
				doConfirmDeleteAssignment(ureq, row);
			} else if("move.assignment".equals(cmd)) {
				PortfolioElementRow row = (PortfolioElementRow)link.getUserObject();
				doMoveAssignment(ureq, row);
			} else if("start.assignment".equals(cmd)) {
				PortfolioElementRow row = (PortfolioElementRow)link.getUserObject();
				doStartAssignment(ureq, row);
			} else if("open.assignment".equals(cmd)) {
				PortfolioElementRow row = (PortfolioElementRow)link.getUserObject();
				doOpenAssignment(ureq, row);
			} else if("up.assignment".equals(cmd)) {
				PortfolioElementRow row = (PortfolioElementRow)link.getUserObject();
				doMoveUpAssignment(ureq, row);
			} else if("down.assignment".equals(cmd)) {
				PortfolioElementRow row = (PortfolioElementRow)link.getUserObject();
				doMoveDownAssignment(ureq, row);
			}
		} else if(source instanceof SingleSelection) {
			SingleSelection startAssignment = (SingleSelection) source;
			if(startAssignment.isOneSelected()) {
				String selectedKey = startAssignment.getSelectedKey();
				try {
					Long key = Long.parseLong(selectedKey);
					doStartAssignment(ureq, key);
				} catch (Exception e) {
					//
				}
			}
		} else if(source == flc) {
			if("ONCLICK".equals(event.getCommand())) {
				String category = ureq.getParameter("tag_select");
				if(StringHelper.containsNonWhitespace(category)) {
					tableEl.quickSearch(ureq, category);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doConfirmCloseSection(UserRequest ureq, PortfolioElementRow row) {
		String title = translate("close.section.confirm.title");
		String text = translate("close.section.confirm.descr", new String[]{ row.getSectionTitle() });
		confirmCloseSectionCtrl = activateYesNoDialog(ureq, title, text, confirmCloseSectionCtrl);
		confirmCloseSectionCtrl.setUserObject(row);
	}
	
	private void doClose(UserRequest ureq, PortfolioElementRow row) {
		Section section = row.getSection();
		section = portfolioService.changeSectionStatus(section, SectionStatus.closed, getIdentity());
		ThreadLocalUserActivityLogger.log(PortfolioLoggingAction.PORTFOLIO_SECTION_CLOSE, getClass(),
				LoggingResourceable.wrap(section));
		loadModel(ureq, null);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doConfirmReopenSection(UserRequest ureq, PortfolioElementRow row) {
		String title = translate("reopen.section.confirm.title");
		String text = translate("reopen.section.confirm.descr", new String[]{ row.getSectionTitle() });
		confirmReopenSectionCtrl = activateYesNoDialog(ureq, title, text, confirmReopenSectionCtrl);
		confirmReopenSectionCtrl.setUserObject(row);
	}
	
	private void doReopen(UserRequest ureq, PortfolioElementRow row) {
		Section section = row.getSection();
		section = portfolioService.changeSectionStatus(section, SectionStatus.inProgress, getIdentity());
		ThreadLocalUserActivityLogger.log(PortfolioLoggingAction.PORTFOLIO_SECTION_REOPEN, getClass(),
				LoggingResourceable.wrap(section));
		loadModel(ureq, null);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doSwitchTimelineOn(UserRequest ureq, boolean savePreferences) {
		timelineSwitchOnButton.setVisible(true);
		timelineSwitchOffButton.setVisible(false);
		flc.contextPut("timelineSwitch", Boolean.TRUE);
		if(savePreferences) {
			ureq.getUserSession().getGuiPreferences().putAndSave(this.getClass(), getTimelineSwitchPreferencesName(), "on");
		}
	}
	
	private void doSwitchTimelineOff(UserRequest ureq, boolean savePreferences) {
		timelineSwitchOnButton.setVisible(false);
		timelineSwitchOffButton.setVisible(true);
		flc.contextPut("timelineSwitch", Boolean.FALSE);
		if(savePreferences) {
			ureq.getUserSession().getGuiPreferences().putAndSave(this.getClass(), getTimelineSwitchPreferencesName(), "off");
		}
	}
	
	protected abstract String getTimelineSwitchPreferencesName();
	
	protected Assignment doStartAssignment(UserRequest ureq, PortfolioElementRow row) {
		return doStartAssignment(ureq, row.getAssignment().getKey());
	}
	
	private Assignment doStartAssignment(UserRequest ureq, Long assignmentKey) {
		Assignment startedAssigment = portfolioService.startAssignment(assignmentKey, getIdentity());
		doOpenPage(ureq, startedAssigment.getPage(), true);
		loadModel(ureq, null);
		return startedAssigment;
	}
	
	private void doOpenAssignment(UserRequest ureq, PortfolioElementRow row) {
		Assignment assignment = row.getAssignment();
		if(assignment.getAssignmentType() == AssignmentType.essay
				|| assignment.getAssignmentType() == AssignmentType.document) {
			Page page = assignment.getPage();
			Page reloadedPage = portfolioService.getPageByKey(page.getKey());
			doOpenPage(ureq, reloadedPage, false);
		} else {
			showWarning("not.implemented");
		}
	}
	
	private void doMoveUpAssignment(UserRequest ureq, PortfolioElementRow row) {
		Assignment assigment = row.getAssignment();
		Section section = assigment.getSection();
		section = portfolioService.moveUpAssignment(section, assigment);
		loadModel(ureq, null);
	}
	
	private void doMoveDownAssignment(UserRequest ureq, PortfolioElementRow row) {
		Assignment assigment = row.getAssignment();
		Section section = row.getSection();
		section = portfolioService.moveDownAssignment(section, assigment);
		loadModel(ureq, null);
	}
	
	protected void doEditAssignment(UserRequest ureq, PortfolioElementRow row) {
		if(guardModalController(editAssignmentCtrl)) return;
		
		Assignment assignment = row.getAssignment();
		editAssignmentCtrl = new AssignmentEditController(ureq, getWindowControl(), assignment);
		listenTo(editAssignmentCtrl);
		
		String title = translate("edit.assignment");
		cmc = new CloseableModalController(getWindowControl(), null, editAssignmentCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doMoveAssignment(UserRequest ureq, PortfolioElementRow row) {
		if(guardModalController(moveAssignmentCtrl)) return;
		
		Assignment assignment = row.getAssignment();
		moveAssignmentCtrl = new AssignmentMoveController(ureq, getWindowControl(), assignment, row.getSection());
		listenTo(moveAssignmentCtrl);
		
		String title = translate("move.assignment");
		cmc = new CloseableModalController(getWindowControl(), null, moveAssignmentCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doConfirmDeleteAssignment(UserRequest ureq, PortfolioElementRow row) {
		boolean inUse = portfolioService.isAssignmentInUse(row.getAssignment());
		
		String text;
		String[] assignmentTitle = new String[]{ StringHelper.escapeHtml(row.getAssignmentTitle()) };
		if(inUse) {
			text = translate("delete.assignment.in.use.confirm.descr", assignmentTitle);
		} else {
			text = translate("delete.assignment.confirm.descr", assignmentTitle);
		}
		String title = translate("delete.assignment.confirm.title");
		confirmDeleteAssignmentCtrl = activateYesNoDialog(ureq, title, text, confirmDeleteAssignmentCtrl);
		confirmDeleteAssignmentCtrl.setUserObject(row);
	}
	
	private void doDelete(PortfolioElementRow row) {
		if(row.isPendingAssignment()) {
			Assignment assignment = row.getAssignment();
			portfolioService.deleteAssignment(assignment);
		}
	}
	
	private void doComment(UserRequest ureq, Page page) {
		CommentAndRatingSecurityCallback commentSecCallback;
		if(PageStatus.isClosed(page)) {
			commentSecCallback = new ReadOnlyCommentsSecurityCallback();
		} else {
			commentSecCallback = new CommentAndRatingDefaultSecurityCallback(getIdentity(), false, false);
		}
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Page.class, page.getKey());
		commentsCtrl = new UserCommentsController(ureq, getWindowControl(), ores, null, null, commentSecCallback);
		listenTo(commentsCtrl);
		
		String title = translate("comment.title");
		cmc = new CloseableModalController(getWindowControl(), null, commentsCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	protected void doOpenRow(UserRequest ureq, PortfolioElementRow row, boolean newElement) {
		if(row.isPage()) {
			Page reloadedPage = portfolioService.getPageByKey(row.getKey());
			doOpenPage(ureq, reloadedPage, newElement);
		} else if(row.isPendingAssignment()) {
			if(secCallback.canNewAssignment()) {
				doEditAssignment(ureq, row);
			}
		}
	}
	
	protected void doPreviousPage(UserRequest ureq, Page currentPage) {
		List<PortfolioElementRow> rows = model.getObjects();
		Page selectedPage = currentPage;
		for(int i=0; i<rows.size(); i++) {
			PortfolioElementRow row = rows.get(i);
			if(row.isPage() && currentPage.equals(row.getPage()) && i > 0 && rows.get(i-1).isPage()) {
				selectedPage = rows.get(i - 1).getPage();
			}
		}

		stackPanel.popUpToController(this);
		Page reloadedPage = portfolioService.getPageByKey(selectedPage.getKey());
		doOpenPage(ureq, reloadedPage, false);
		getWindowControl().getWindowBackOffice().sendCommandTo(new ScrollTopCommand());
	}
	
	protected void doNextPage(UserRequest ureq, Page currentPage) {
		List<PortfolioElementRow> rows = model.getObjects();
		Page selectedPage = currentPage;
		for(int i=0; i<rows.size(); i++) {
			PortfolioElementRow row = rows.get(i);
			if(row.isPage() && currentPage.equals(row.getPage()) && i+1 < rows.size() && rows.get(i+1).isPage()) {
				selectedPage = rows.get(i+1).getPage();
			}
		}

		stackPanel.popUpToController(this);
		Page reloadedPage = portfolioService.getPageByKey(selectedPage.getKey());
		doOpenPage(ureq, reloadedPage, false);
		getWindowControl().getWindowBackOffice().sendCommandTo(new ScrollTopCommand());
	}
	
	protected void doAllPages() {
		stackPanel.popController(pageCtrl);
		getWindowControl().getWindowBackOffice().sendCommandTo(new ScrollTopCommand());
	}
	
	protected void doOpenPage(UserRequest ureq, Page reloadedPage, boolean newElement) {
		OLATResourceable pageOres = OresHelper.createOLATResourceableInstance("Entry", reloadedPage.getKey());
		WindowControl swControl = addToHistory(ureq, pageOres, null);
		
		boolean openInEditMode = newElement || (secCallback.canEditPage(reloadedPage)
				&& (reloadedPage.getPageStatus() == null || reloadedPage.getPageStatus() == PageStatus.draft || reloadedPage.getPageStatus() == PageStatus.inRevision));
		pageCtrl = new PageRunController(ureq, swControl, stackPanel, secCallback, reloadedPage, openInEditMode);
		listenTo(pageCtrl);
		
		if(reloadedPage.getSection() != null) {
			Section section = reloadedPage.getSection();
			stackPanel.pushController(section.getTitle(), null, new ListSection(section));
		}
		stackPanel.pushController(reloadedPage.getTitle(), pageCtrl);
		
		List<PortfolioElementRow> rows = model.getObjects();
		int numOfRows = rows.size();
		for(int i=0; i<numOfRows; i++) {
			PortfolioElementRow row = rows.get(i);
			if(row.isPage() && reloadedPage.equals(row.getPage())) {
				boolean hasPrevious = (i > 0 && rows.get(i-1).isPage());	
				boolean hasNext = (i + 1 < numOfRows && rows.get(i + 1).isPage());
				pageCtrl.initPaging(hasPrevious, hasNext);	
			}
		}
	}
	
	protected List<PortfolioElementRow> getSelectedRows() {
		Set<Integer> indexes = tableEl.getMultiSelectedIndex();
		List<PortfolioElementRow> selectedRows = new ArrayList<>(indexes.size());
		for(Integer index:indexes) {
			PortfolioElementRow row = model.getObject(index.intValue());
			selectedRows.add(row);
		}
		return selectedRows;
	}
	
	public static final class ListSection {
		
		private final Section section;
		
		public ListSection(Section section) {
			this.section = section;
		}

		@Override
		public int hashCode() {
			return section == null ? 7346576 : section.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj instanceof ListSection) {
				ListSection ls = (ListSection) obj;
				return section != null && section.equals(ls.section);
			}
			return true;
		}
	}
	
	private static final class PageImageMapper implements Mapper {
		
		private final PageListDataModel model;
		private final PortfolioService portfolioService;

		public PageImageMapper(PageListDataModel model, PortfolioService portfolioService) {
			this.model = model;
			this.portfolioService = portfolioService;
		}

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			MediaResource mr = null;
			
			String path = relPath;
			if(path.startsWith("/page/")) {
				path = path.substring(6, path.length());
				
				int index = path.indexOf('/');
				if(index > 0) {
					String pageKey = path.substring(0, index);
					Long key = Long.valueOf(pageKey);
					for(int i=model.getRowCount(); i-->0; ) {
						PortfolioElementRow row = model.getObject(i);
						if(row.isPage() && row.getPage().getKey().equals(key)) {
							File posterImage = portfolioService.getPosterImage(row.getPage());
							mr = new FileMediaResource(posterImage);
							break;
						}
					}
				}
			}
			
			if(mr == null) {
				mr = new NotFoundMediaResource();
			}
			return mr;
		}
		
	}
}
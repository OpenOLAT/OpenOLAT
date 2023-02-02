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
package org.olat.course.nodes.videotask.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableClassicRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableTextFilter;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.VideoTaskCourseNode;
import org.olat.course.nodes.videotask.ui.VideoTaskAssessmentDetailsTableModel.DetailsCols;
import org.olat.course.nodes.videotask.ui.components.DurationFlexiCellRenderer;
import org.olat.course.nodes.videotask.ui.components.PercentCellRenderer;
import org.olat.course.nodes.videotask.ui.components.SelectionCellRenderer;
import org.olat.course.nodes.videotask.ui.components.VideoTaskSessionRowComparator;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.video.VideoAssessmentService;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoSegment;
import org.olat.modules.video.VideoSegmentCategory;
import org.olat.modules.video.VideoSegments;
import org.olat.modules.video.VideoTaskSegmentSelection;
import org.olat.modules.video.VideoTaskSession;
import org.olat.modules.video.model.VideoTaskCategoryScore;
import org.olat.modules.video.model.VideoTaskScore;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
abstract class AbstractVideoTaskSessionListController extends FormBasicController {
	
	public static final int CATEGORY_PROPS_OFFSET = 500;
	
	private FormLink playAllButton;
	private FlexiTableElement tableEl;
	private final TooledStackedPanel stackPanel;
	protected VideoTaskAssessmentDetailsTableModel tableModel;
	
	private int count = 0;
	protected final Float maxScore;
	protected final Float cutValue;
	protected final RepositoryEntry entry;
	protected final VideoSegments segments;
	protected final RepositoryEntry videoEntry;
	protected final CourseEnvironment courseEnv;
	protected final VideoTaskCourseNode courseNode;
	protected final List<String> categoriesIds;
	protected final List<VideoSegmentCategory> categories;
	private List<SelectionCellRenderer> selectionCellRenderers;
	
	private ToolsController toolsCtrl;
	protected CloseableModalController cmc;
	private VideoTaskAssessmentPlayController playCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	private ConfirmRevalidateTaskController confirmRevalidateCtrl;
	private ConfirmInvalidateTaskController confirmInvalidateCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	protected VideoManager videoManager;
	@Autowired
	protected VideoAssessmentService videoAssessmentService;

	AbstractVideoTaskSessionListController(UserRequest ureq, WindowControl wControl, String page, TooledStackedPanel stackPanel,
			VideoTaskCourseNode courseNode, CourseEnvironment courseEnv) {
		super(ureq, wControl, page);
		this.courseEnv = courseEnv;
		this.stackPanel = stackPanel;
		this.courseNode = courseNode;
		entry = courseEnv.getCourseGroupManager().getCourseEntry();

		Float max = (Float) courseNode.getModuleConfiguration().get(MSCourseNode.CONFIG_KEY_SCORE_MAX);
		maxScore = max != null ? max : MSCourseNode.CONFIG_DEFAULT_SCORE_MAX;
		cutValue = (Float) courseNode.getModuleConfiguration().get(MSCourseNode.CONFIG_KEY_PASSED_CUT_VALUE);
		
		videoEntry = courseNode.getReferencedRepositoryEntry();
		segments = videoManager.loadSegments(videoEntry.getOlatResource());
		List<String> categoriesIdsLists = courseNode.getModuleConfiguration().getList(VideoTaskEditController.CONFIG_KEY_CATEGORIES, String.class);
		categoriesIds =  List.copyOf(categoriesIdsLists);
		categories = new ArrayList<>(categoriesIds.size());
		for(VideoSegmentCategory category:segments.getCategories()) {
			if(categoriesIds.contains(category.getId())) {
				categories.add(category);
			}
		}
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		playAllButton = uifactory.addFormLink("play.all", formLayout, Link.BUTTON);
		playAllButton.setIconLeftCSS("o_icon o_icon_video_play");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		initColumnsIds(columnsModel);
		
		// Categories
		int colCount = CATEGORY_PROPS_OFFSET;
		List<String> legends = new ArrayList<>();
		selectionCellRenderers = new ArrayList<>(categories.size());
		for(VideoSegmentCategory category:categories) {
			int colIndex = colCount++;
			String header = category.getLabel();
			int numOfSegments = 0;
			for(VideoSegment segment:segments.getSegments()) {
				if(category.getId().equals(segment.getCategoryId())) {
					numOfSegments++;
				}
			}
			
			DefaultFlexiColumnModel catColModel = new DefaultFlexiColumnModel(true, header, colIndex, null, true, "cat-" + colIndex);
			catColModel.setHeaderLabel(translate("table.header.category", header, Integer.toString(numOfSegments)));
			SelectionCellRenderer renderer = new SelectionCellRenderer();
			selectionCellRenderers.add(renderer);
			catColModel.setCellRenderer(renderer);
			columnsModel.addFlexiColumnModel(catColModel);
			legends.add(category.getLabelAndTitle());
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DetailsCols.duration, new DurationFlexiCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DetailsCols.scorePercent, new PercentCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DetailsCols.play.i18nHeaderKey(), DetailsCols.play.ordinal(), "play",
				new BooleanCellRenderer(
						new StaticFlexiCellRenderer(null, "play", false, false, "", "o_icon o_icon_video_play", null), 
						null)));
		
		DefaultFlexiColumnModel toolsCol = new DefaultFlexiColumnModel(DetailsCols.tools);
		toolsCol.setAlwaysVisible(true);
		toolsCol.setExportable(false);
		toolsCol.setIconHeader("o_icon o_icon_actions o_icon-fw o_icon-lg");
		columnsModel.addFlexiColumnModel(toolsCol);
		
		tableModel = new VideoTaskAssessmentDetailsTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.external, FlexiTableRendererType.classic);
		tableEl.setExternalRenderer(new FlexiTableClassicRenderer(), "o_icon_list_num");
		tableEl.getClassicTypeButton().setIconLeftCSS("o_icon o_icon_media");
		tableEl.setCssDelegate(tableModel);

		if(formLayout instanceof FormLayoutContainer layoutCont) {
			layoutCont.contextPut("legends", legends);
		}
	}
	
	protected void initColumnsIds(FlexiTableColumnModel columnsModel) {
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, DetailsCols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DetailsCols.attempt));
	}
	
	protected void initFilters(boolean withIdentities, int maxAttempts) {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>(4);
		
		if(withIdentities) {
			filters.add(new FlexiTableTextFilter(translate("filter.identity"), VideoTaskAssessmentDetailsTableModel.FILTER_IDENTITY, true));
		}
		
		SelectionValues attemptsValues = new SelectionValues();
		for(int i=maxAttempts; i-->1;) {
			String attempt = Integer.toString(i);
			attemptsValues.add(SelectionValues.entry(attempt, attempt));
		}
		FlexiTableMultiSelectionFilter attemptsFilter = new FlexiTableMultiSelectionFilter(translate("filter.attempts"),
				VideoTaskAssessmentDetailsTableModel.FILTER_ATTEMPTS, attemptsValues, true);
		filters.add(attemptsFilter);
		
		SelectionValues performanceValues = new SelectionValues();
		performanceValues.add(SelectionValues.entry(VideoTaskAssessmentDetailsTableModel.FILTER_PERFORMANCE_HIGH, translate("performance.high")));
		performanceValues.add(SelectionValues.entry(VideoTaskAssessmentDetailsTableModel.FILTER_PERFORMANCE_MEDIUM, translate("performance.medium")));
		performanceValues.add(SelectionValues.entry(VideoTaskAssessmentDetailsTableModel.FILTER_PERFORMANCE_LOW, translate("performance.low")));
		FlexiTableMultiSelectionFilter performanceFilter = new FlexiTableMultiSelectionFilter(translate("filter.performance"),
				VideoTaskAssessmentDetailsTableModel.FILTER_PERFORMANCE, performanceValues, true);
		filters.add(performanceFilter);

		tableEl.setFilters(true, filters, false, true);
	}
	
	protected List<Identity> getIdentities() {
		Set<Identity> identitiesSet = tableModel.getObjects().stream()
				.filter(session -> session.getAssessedIdentity() != null)
				.map(VideoTaskSessionRow::getAssessedIdentity)
				.collect(Collectors.toSet());
		return new ArrayList<>(identitiesSet);
	}
	
	protected List<VideoTaskSessionRow> getVideoTaskSessionRows() {
		return tableModel.getObjects();
	}
	
	protected List<String> getCategoriesIds() {
		return categories.stream()
				.map(VideoSegmentCategory::getId)
				.toList();
	}
	
	protected abstract void loadModel();
	
	protected void loadModel(List<VideoTaskSession> taskSessions) {
		List<VideoTaskSegmentSelection> selections = videoAssessmentService.getTaskSegmentSelections(taskSessions);
		Map<VideoTaskSession,List<VideoTaskSegmentSelection>> mapSelections = taskSessions.stream()
				.collect(Collectors.toMap(session -> session, s -> new ArrayList<>(), (u, v) -> u));
		for(VideoTaskSegmentSelection selection:selections) {
			List<VideoTaskSegmentSelection> taskSelections = mapSelections.get(selection.getTaskSession());
			if(taskSelections != null) {
				taskSelections.add(selection);
			}
		}
		
		List<String> selectedCategoriesIds = categories.stream()
				.map(VideoSegmentCategory::getId)
				.toList();
		
		long maxAttempts = 0;
		List<VideoTaskSessionRow> rows = new ArrayList<>(taskSessions.size());
		for(VideoTaskSession taskSession:taskSessions) {
			VideoTaskSessionRow row = forgeRow(taskSession, selectedCategoriesIds, mapSelections);
			rows.add(row);
			
			if(row.getAttempt() > maxAttempts) {
				maxAttempts = row.getAttempt();
			}
		}
		
		for(int i=0; i<selectionCellRenderers.size(); i++) {
			int maxCorrect = 0;
			int maxNotCorrect = 0;
			
			for(VideoTaskSessionRow row:rows) {
				VideoTaskCategoryScore scoring = row.getCategoryScoring(i);
				maxCorrect = Math.max(maxCorrect, scoring.correct());
				maxNotCorrect = Math.max(maxNotCorrect, scoring.notCorrect());
			}
			
			SelectionCellRenderer renderer = selectionCellRenderers.get(i);
			renderer.setMaxCorrect(maxCorrect);
			renderer.setMaxNotCorrect(maxNotCorrect);
		}
		
		Collections.sort(rows, new VideoTaskSessionRowComparator());
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
		flc.contextPut("rowCount", Integer.toString(rows.size()));
	}
	
	private VideoTaskSessionRow forgeRow(VideoTaskSession taskSession, List<String> selectedCategoriesIds,
			Map<VideoTaskSession,List<VideoTaskSegmentSelection>> mapSelections) {
		
		List<VideoTaskSegmentSelection> taskSelections = mapSelections.get(taskSession);
		VideoTaskScore scoring = videoAssessmentService
				.calculateScore(segments, selectedCategoriesIds, maxScore.doubleValue(), taskSelections);
		VideoTaskCategoryScore[] categoryScoring = videoAssessmentService.calculateScorePerCategory(categories, taskSelections);
		String fullName = userManager.getUserDisplayName(taskSession.getIdentity());
		VideoTaskSessionRow row = new VideoTaskSessionRow(taskSession, taskSession.getIdentity(), fullName, scoring, categoryScoring);
		
		FormLink tools = uifactory.addFormLink("tools_" + (++count), "tools", "", null, flc, Link.LINK | Link.NONTRANSLATED);
		tools.setIconLeftCSS("o_icon o_icon_actions o_icon-fw o_icon-lg");
		tools.setAriaLabel(translate("tools"));
		row.setToolsButton(tools);
		tools.setUserObject(row);
		
		return row;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(playCtrl == source) {
			if(event == Event.BACK_EVENT) {
				stackPanel.popController(playCtrl);
				cleanUp();
			}
		} else if(toolsCtrl == source) {
			if(event == Event.DONE_EVENT) {
				calloutCtrl.deactivate();
				cleanUp();
			}
		} else if(confirmInvalidateCtrl == source || confirmRevalidateCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				loadModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		}
		else if(calloutCtrl == source || cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	protected void cleanUp() {
		removeAsListenerAndDispose(confirmRevalidateCtrl);
		removeAsListenerAndDispose(confirmInvalidateCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(playCtrl);
		confirmRevalidateCtrl = null;
		confirmInvalidateCtrl = null;
		calloutCtrl = null;
		toolsCtrl = null;
		playCtrl = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(playAllButton == source) {
			doPlayAll(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se && "play".equals(se.getCommand())) {
				doPlay(ureq, tableModel.getObject(se.getIndex()));
			} else if(event instanceof FlexiTableSearchEvent) {
				doSearch();
			}
		} else if(source instanceof FormLink link) {
			if("tools".equals(link.getCmd()) && link.getUserObject() instanceof VideoTaskSessionRow row) {
				doTools(ureq, link, row);
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doPlay(UserRequest ureq, VideoTaskSessionRow row) {
		removeAsListenerAndDispose(playCtrl);
		
		playCtrl = new VideoTaskAssessmentPlayController(ureq, getWindowControl(),
				videoEntry, List.of(row.getTaskSession()));
		listenTo(playCtrl);
		stackPanel.pushController(translate("play"), playCtrl);
	}
	
	private void doPlayAll(UserRequest ureq) {
		removeAsListenerAndDispose(playCtrl);
		
		List<VideoTaskSession> taskSessions = tableModel.getObjects().stream()
				.map(VideoTaskSessionRow::getTaskSession)
				.toList();
		playCtrl = new VideoTaskAssessmentPlayController(ureq, getWindowControl(),
				videoEntry, taskSessions);
		listenTo(playCtrl);

		stackPanel.pushController(translate("play"), playCtrl);
	}
	
	private void doSearch() {
		List<FlexiTableFilter> filters = tableEl.getFilters();
		tableModel.filter(null, filters);
		tableEl.reset(true, true, true);
	}
	
	private void doTools(UserRequest ureq, FormLink link, VideoTaskSessionRow row) {
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		
		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);

		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	protected abstract void doInvalidate(UserRequest ureq, VideoTaskSessionRow taskSession);
	
	protected void doInvalidate(UserRequest ureq, VideoTaskSessionRow taskSession,
			UserCourseEnvironment assessedUserCourseEnv) {
		boolean lastSession = taskSession.getTaskSession().equals(tableModel.getLastSession());
		confirmInvalidateCtrl = new ConfirmInvalidateTaskController(ureq, getWindowControl(),
				taskSession.getTaskSession(), lastSession, courseNode, assessedUserCourseEnv);
		listenTo(confirmInvalidateCtrl);
		
		String title = translate("confirm.invalidate.title");
		cmc = new CloseableModalController(getWindowControl(), "close", confirmInvalidateCtrl.getInitialComponent(),
				true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	protected abstract void doRevalidate(UserRequest ureq, VideoTaskSessionRow taskSession);
	
	protected void doRevalidate(UserRequest ureq, VideoTaskSessionRow taskSession,
			UserCourseEnvironment assessedUserCourseEnv) {
		confirmRevalidateCtrl = new ConfirmRevalidateTaskController(ureq, getWindowControl(),
				taskSession.getTaskSession(), courseNode, assessedUserCourseEnv);
		listenTo(confirmRevalidateCtrl);
		
		String title = translate("confirm.revalidate.title");
		cmc = new CloseableModalController(getWindowControl(), "close", confirmRevalidateCtrl.getInitialComponent(),
				true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private class ToolsController extends BasicController {
		
		private Link revalidateLink;
		private Link invalidateLink;
		
		private final VideoTaskSessionRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, VideoTaskSessionRow row) {
			super(ureq, wControl);
			this.row = row;
			
			VelocityContainer mainVC = createVelocityContainer("details_tools");
			if(row.getTaskSession().isCancelled()) {
				revalidateLink = LinkFactory.createLink("revalidate.session", mainVC, this);
				revalidateLink.setIconLeftCSS("o_icon o_icon-fw o_icon_log");
			} else {
				invalidateLink = LinkFactory.createLink("invalidate.session", mainVC, this);
				invalidateLink.setIconLeftCSS("o_icon o_icon-fw o_icon_log");
			}
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			fireEvent(ureq, Event.DONE_EVENT);
			if(invalidateLink == source) {
				doInvalidate(ureq, row);
			} else if(revalidateLink == source) {
				doRevalidate(ureq, row);
			}
		}
	}
}

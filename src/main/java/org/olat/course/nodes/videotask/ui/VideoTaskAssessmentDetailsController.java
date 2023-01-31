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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableClassicRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TimeFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
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
import org.olat.course.nodes.videotask.ui.VideoTaskSessionRow.CategoryColumn;
import org.olat.course.nodes.videotask.ui.components.PercentCellRenderer;
import org.olat.course.nodes.videotask.ui.components.SelectionCellRenderer;
import org.olat.course.nodes.videotask.ui.components.VideoTaskSessionComparator;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.video.VideoAssessmentService;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoSegment;
import org.olat.modules.video.VideoSegmentCategory;
import org.olat.modules.video.VideoSegments;
import org.olat.modules.video.VideoTaskSegmentSelection;
import org.olat.modules.video.VideoTaskSession;
import org.olat.modules.video.model.VideoTaskScore;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 janv. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoTaskAssessmentDetailsController extends FormBasicController {
	
	public static final int CATEGORY_PROPS_OFFSET = 500;
	
	private FlexiTableElement tableEl;
	private final TooledStackedPanel stackPanel;
	private VideoTaskAssessmentDetailsTableModel tableModel;
	
	private final Identity assessedIdentity;
	private final RepositoryEntry videoEntry;
	private final UserCourseEnvironment assessedUserCourseEnv;
	
	private int count = 0;
	private final Float maxScore;
	private final RepositoryEntry entry;
	private final VideoSegments segments;
	private final VideoTaskCourseNode courseNode;
	private final List<VideoSegmentCategory> categories;
	private List<SelectionCellRenderer> selectionCellRenderers;
	
	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private VideoTaskAssessmentPlayController playCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	private ConfirmRevalidateTaskController confirmRevalidateCtrl;
	private ConfirmInvalidateTaskController confirmInvalidateCtrl;
	
	@Autowired
	private VideoManager videoManager;
	@Autowired
	private VideoAssessmentService videoAssessmentService;

	public VideoTaskAssessmentDetailsController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			VideoTaskCourseNode courseNode, UserCourseEnvironment assessedUserCourseEnv) {
		super(ureq, wControl, "assessment_details");
		this.stackPanel = stackPanel;
		this.courseNode = courseNode;
		entry = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		this.assessedUserCourseEnv = assessedUserCourseEnv;
		assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
		
		Float max = (Float) courseNode.getModuleConfiguration().get(MSCourseNode.CONFIG_KEY_SCORE_MAX);
		maxScore = max != null ? max : MSCourseNode.CONFIG_DEFAULT_SCORE_MAX;
		
		videoEntry = courseNode.getReferencedRepositoryEntry();
		segments = videoManager.loadSegments(videoEntry.getOlatResource());
		List<String> categoriesIds = courseNode.getModuleConfiguration().getList(VideoTaskEditController.CONFIG_KEY_CATEGORIES, String.class);
		categories = new ArrayList<>(categoriesIds.size());
		for(VideoSegmentCategory category:segments.getCategories()) {
			if(categoriesIds.contains(category.getId())) {
				categories.add(category);
			}
		}
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, DetailsCols.id));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DetailsCols.attempt));
		
		// Categories
		int count = CATEGORY_PROPS_OFFSET;
		selectionCellRenderers = new ArrayList<>(categories.size());
		for(VideoSegmentCategory category:categories) {
			int colIndex = count++;
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
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(DetailsCols.duration, new TimeFlexiCellRenderer(getLocale())));
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
	}
	
	private void loadModel() {
		List<VideoTaskSession> taskSessions = videoAssessmentService.getTaskSessions(entry, courseNode.getIdent(), assessedIdentity);
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
		
		List<VideoTaskSessionRow> rows = new ArrayList<>(taskSessions.size());
		for(VideoTaskSession taskSession:taskSessions) {
			VideoTaskSessionRow row = forgeRow(taskSession, selectedCategoriesIds, mapSelections);
			rows.add(row);
		}
		
		for(int i=0; i<selectionCellRenderers.size(); i++) {
			int maxCorrect = 0;
			int maxNotCorrect = 0;
			
			for(VideoTaskSessionRow row:rows) {
				CategoryColumn scoring = row.getCategoryScoring(i);
				maxCorrect = Math.max(maxCorrect, scoring.getCorrect());
				maxNotCorrect = Math.max(maxNotCorrect, scoring.getNotCorrect());
			}
			
			SelectionCellRenderer renderer = selectionCellRenderers.get(i);
			renderer.setMaxCorrect(maxCorrect);
			renderer.setMaxNotCorrect(maxNotCorrect);
		}
		
		Collections.sort(rows, new VideoTaskSessionRowComparator());
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private VideoTaskSessionRow forgeRow(VideoTaskSession taskSession, List<String> selectedCategoriesIds,
			Map<VideoTaskSession,List<VideoTaskSegmentSelection>> mapSelections) {
		
		List<VideoTaskSegmentSelection> taskSelections = mapSelections.get(taskSession);
		VideoTaskScore scoring = videoAssessmentService
				.calculateScore(segments, selectedCategoriesIds, maxScore.doubleValue(), taskSelections);
		CategoryColumn[] categoryScoring = calculateScoring(taskSelections);
		VideoTaskSessionRow row = new VideoTaskSessionRow(taskSession, scoring, categoryScoring);
		
		FormLink tools = uifactory.addFormLink("tools_" + (++count), "tools", "", null, flc, Link.LINK | Link.NONTRANSLATED);
		tools.setIconLeftCSS("o_icon o_icon_actions o_icon-fw o_icon-lg");
		tools.setAriaLabel(translate("tools"));
		row.setToolsButton(tools);
		tools.setUserObject(row);
		
		return row;
	}
	
	private CategoryColumn[] calculateScoring(List<VideoTaskSegmentSelection> taskSelections) {
		CategoryColumn[] scoring = new CategoryColumn[categories.size()];
		for(int i=0; i<categories.size(); i++) {
			VideoSegmentCategory category = categories.get(i);
			CategoryColumn col = new CategoryColumn();
			
			int correct = 0;
			int notCorrect = 0;
			for(VideoTaskSegmentSelection selection:taskSelections) {
				if(category.getId().equals(selection.getCategoryId())) {
					if(selection.getCorrect() != null && selection.getCorrect().booleanValue()) {
						correct++;
					} else {
						notCorrect++;
					}
				}	
			}

			col.setCategory(category);
			col.setCorrect(correct);
			col.setNotCorrect(notCorrect);
			scoring[i] = col;
		}
		return scoring;
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
	
	private void cleanUp() {
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
		if(tableEl == source) {
			if(event instanceof SelectionEvent se && "play".equals(se.getCommand())) {
				doPlay(ureq, tableModel.getObject(se.getIndex()));
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
				videoEntry, row.getTaskSession(), assessedIdentity);
		listenTo(playCtrl);

		stackPanel.pushController(translate("play"), playCtrl);
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
	
	private void doInvalidate(UserRequest ureq, VideoTaskSessionRow taskSession) {
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
	
	private void doRevalidate(UserRequest ureq, VideoTaskSessionRow taskSession) {
		confirmRevalidateCtrl = new ConfirmRevalidateTaskController(ureq, getWindowControl(),
				taskSession.getTaskSession(), courseNode, assessedUserCourseEnv);
		listenTo(confirmRevalidateCtrl);
		
		String title = translate("confirm.revalidate.title");
		cmc = new CloseableModalController(getWindowControl(), "close", confirmRevalidateCtrl.getInitialComponent(),
				true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	public static class VideoTaskSessionRowComparator implements Comparator<VideoTaskSessionRow> {
		
		private final VideoTaskSessionComparator comparator = new VideoTaskSessionComparator(true);

		@Override
		public int compare(VideoTaskSessionRow o1, VideoTaskSessionRow o2) {
			return comparator.compare(o1.getTaskSession(), o2.getTaskSession());
		}
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

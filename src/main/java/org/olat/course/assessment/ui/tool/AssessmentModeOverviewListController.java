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
package org.olat.course.assessment.ui.tool;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.AssessmentModeCoordinationService;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.AssessmentModeNotificationEvent;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.model.AssessmentModeStatistics;
import org.olat.course.assessment.model.TransientAssessmentMode;
import org.olat.course.assessment.ui.mode.AssessmentModeHelper;
import org.olat.course.assessment.ui.mode.AssessmentModeListController;
import org.olat.course.assessment.ui.mode.ChangeAssessmentModeEvent;
import org.olat.course.assessment.ui.mode.ModeStatusCellRenderer;
import org.olat.course.assessment.ui.mode.TimeCellRenderer;
import org.olat.course.assessment.ui.tool.AssessmentModeOverviewListTableModel.ModeCols;
import org.olat.course.assessment.ui.tool.component.AssessmentModeProgressionItem;
import org.olat.course.assessment.ui.tool.event.AssessmentModeStatusEvent;
import org.olat.course.assessment.ui.tool.event.CourseNodeEvent;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeConfiguration;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.PortfolioCourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.model.LecturesBlockSearchParameters;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Small list of the assessment planed today and in the future for the
 * coaches.
 * 
 * Initial date: 15 déc. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeOverviewListController extends FormBasicController implements FlexiTableComponentDelegate, GenericEventListener {

	private static final OLATResourceable ASSESSMENT_MODE_ORES = OresHelper.createOLATResourceableType(AssessmentMode.class);
	
	private FlexiTableElement tableEl;
	private AssessmentModeOverviewListTableModel model;

	private CloseableModalController cmc;
	private DialogBoxController startDialogBox;
	private ConfirmStopAssessmentModeController stopCtrl;
	
	private int count = 0;
	private final RepositoryEntry courseEntry;
	private final AssessmentToolSecurityCallback assessmentCallback;

	@Autowired
	private LectureService lectureService;
	@Autowired
	private AssessmentModeManager asssessmentModeManager;
	@Autowired
	private AssessmentModeCoordinationService assessmentModeCoordinationService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	public AssessmentModeOverviewListController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry courseEntry, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl, "assessment_modes", Util.createPackageTranslator(AssessmentModeListController.class, ureq.getLocale()));
		this.courseEntry = courseEntry;
		this.assessmentCallback = assessmentCallback;
		initForm(ureq);
		loadModel();
		
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.registerFor(this, getIdentity(), ASSESSMENT_MODE_ORES);
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.registerFor(this, getIdentity(), AssessmentModeNotificationEvent.ASSESSMENT_MODE_NOTIFICATION);
	}
	
	public int getNumOfAssessmentModes() {
		return model == null ? 0 : model.getRowCount();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ModeCols.status, new ModeStatusCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ModeCols.name));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ModeCols.begin));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ModeCols.end));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ModeCols.leadTime, new TimeCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ModeCols.followupTime, new TimeCellRenderer(getTranslator())));

		model = new AssessmentModeOverviewListTableModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 10, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(false);
		tableEl.setNumOfRowsEnabled(false);
		
		VelocityContainer row = createVelocityContainer("assessment_mode_overview_row");
		row.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		AssessmentModeHelper helper = new AssessmentModeHelper(getTranslator());
		row.contextPut("helper", helper);
		tableEl.setRowRenderer(row, this);
		tableEl.setRendererType(FlexiTableRendererType.custom);
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> cmps = new ArrayList<>();
		if(rowObject instanceof AssessmentModeOverviewRow) {
			AssessmentModeOverviewRow mode = (AssessmentModeOverviewRow)rowObject;
			if(mode.getActionButton() != null) {
				cmps.add(mode.getActionButton().getComponent());
			}
			List<FormLink> elementLinks = mode.getElementLinks();
			for(FormLink elementLink:elementLinks) {
				cmps.add(elementLink.getComponent());
			}
			if(mode.getWaitBarItem() != null) {
				cmps.add(mode.getWaitBarItem().getComponent());
			}
		}
		return cmps;
	}

	public void loadModel() {
		synchronized(model) { 
			Date today = CalendarUtils.removeTime(new Date());
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DATE, 5);
			Date until = CalendarUtils.endOfDay(cal.getTime());
			
			LecturesBlockSearchParameters searchParams = new LecturesBlockSearchParameters();
			searchParams.setStartDate(today);
			searchParams.setEndDate(until);
			
			List<LectureBlock> lectures = lectureService.getLectureBlocks(courseEntry, getIdentity());
			Set<Long> teachedLectures = lectures.stream().map(LectureBlock::getKey).collect(Collectors.toSet());
	
			List<AssessmentMode> modes = asssessmentModeManager.getPlannedAssessmentMode(courseEntry, today, until);
			List<AssessmentModeOverviewRow> rows = new ArrayList<>();
			for(AssessmentMode mode:modes) {
				rows.add(forgeRow(mode, today, teachedLectures));
			}
			model.setObjects(rows);
			tableEl.reset(true, true, true);
		}
	}
	
	private AssessmentModeOverviewRow forgeRow(AssessmentMode mode, Date today, Set<Long> teachedLectures) {
		long startTime = CalendarUtils.removeTime(mode.getBegin()).getTime();
		long endTime = CalendarUtils.endOfDay(mode.getEnd()).getTime();
		long todayTime = today.getTime();
		boolean isToday = startTime <= todayTime && endTime >= todayTime;

		Calendar cal = Calendar.getInstance();
		long now = cal.getTimeInMillis();
		cal.setTime(mode.getEnd());
		long endInMillseconds = cal.getTimeInMillis() - now;
		boolean endSoon = (endInMillseconds < (5l * 60l * 1000l))
				&& (mode.getStatus() == Status.assessment || mode.getStatus() == Status.followup);
		
		AssessmentModeOverviewRow row = new AssessmentModeOverviewRow(mode, isToday, endSoon, endInMillseconds);
		
		LectureBlock block = mode.getLectureBlock();
		boolean allowToStartStop = assessmentCallback.canStartStopAllAssessments()
				|| (block != null && teachedLectures.contains(block.getKey()));

		if(mode.isManualBeginEnd() && allowToStartStop) {
			if(assessmentModeCoordinationService.canStart(mode)) {
				String id = "start_" + (++count);
				FormLink startButton = uifactory.addFormLink(id, "start", "start", null, flc, Link.BUTTON_SMALL);
				startButton.setDomReplacementWrapperRequired(false);
				startButton.setIconLeftCSS("o_icon o_icon-fw o_as_mode_assessment");
				startButton.setUserObject(row);
				flc.add(id, startButton);
				forgeStatistics(mode, row);
				row.setActionButton(startButton);
			} else if(assessmentModeCoordinationService.canStop(mode)) {
				String id = "end_" + (++count);
				FormLink endButton = uifactory.addFormLink(id, "end", "end", null, flc, Link.BUTTON_SMALL);
				endButton.setDomReplacementWrapperRequired(false);
				endButton.setIconLeftCSS("o_icon o_icon-fw o_as_mode_stop");
				if(assessmentModeCoordinationService.isDisadvantageCompensationExtensionTime(mode)) {
					endButton.setIconRightCSS("o_icon o_icon-fw o_icon_disadvantage_compensation");
				}
				endButton.setUserObject(row);
				flc.add(id, endButton);
				forgeStatistics(mode, row);
				row.setActionButton(endButton);
			}
		} else if (mode.getStatus() == AssessmentMode.Status.leadtime
				|| mode.getStatus() == AssessmentMode.Status.assessment
				|| mode.getStatus() == AssessmentMode.Status.followup) {
			forgeStatistics(mode, row);
		} else {
			row.setWaitBarItem(null);
		}
		
		String elements = mode.getElementList();
		if(StringHelper.containsNonWhitespace(elements)) {
			ICourse course = CourseFactory.loadCourse(courseEntry);
			for(String element:elements.split("[,]")) {
				CourseNode node = course.getRunStructure().getNode(element);
				AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseEntry, node);
				if(assessmentConfig.isAssessable() && !(node instanceof STCourseNode) && !(node instanceof PortfolioCourseNode)) {
					String id = "element_" + (++count);
					FormLink elementButton = uifactory.addFormLink(id, "element", node.getShortTitle(), null, flc, Link.LINK | Link.NONTRANSLATED);
					elementButton.setDomReplacementWrapperRequired(false);
					CourseNodeConfiguration cnConfig = CourseNodeFactory.getInstance()
							.getCourseNodeConfigurationEvenForDisabledBB(node.getType());
					elementButton.setIconLeftCSS("o_icon ".concat(cnConfig.getIconCSSClass()));
					elementButton.setUserObject(node);
					flc.add(id, elementButton);
					row.addElementLink(elementButton);
				}
			}
		}

		return row;
	}
	
	private void forgeStatistics(AssessmentMode mode, AssessmentModeOverviewRow row) {
		AssessmentModeStatistics statistics = assessmentModeCoordinationService.getStatistics(mode);
		if(statistics != null) {
			statistics.setStatus(mode.getStatus());// direct from the database
			
			String id = "wait_" + (++count);
			AssessmentModeProgressionItem waitBarItem = new AssessmentModeProgressionItem(id, mode, getTranslator());
			waitBarItem.setMax(statistics.getNumPlanned());
			waitBarItem.setActual(statistics.getNumInOpenOlat());
			row.setWaitBarItem(waitBarItem);
			flc.add(waitBarItem);
		}
	}

	@Override
	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.deregisterFor(this, ASSESSMENT_MODE_ORES);
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.deregisterFor(this, AssessmentModeNotificationEvent.ASSESSMENT_MODE_NOTIFICATION);
	}

	@Override
	public void event(Event event) {
		if(event instanceof ChangeAssessmentModeEvent) {
			processChangeAssessmentModeEvents((ChangeAssessmentModeEvent)event);
		} else if(event instanceof AssessmentModeNotificationEvent) {
			processChangeAssessmentModeEvents((AssessmentModeNotificationEvent)event);
		}
	}
	
	private void processChangeAssessmentModeEvents(ChangeAssessmentModeEvent event) {
		try {
			List<AssessmentModeOverviewRow> rows = model.getObjects();
			for(AssessmentModeOverviewRow row:rows) {
				if(event.getAssessmentModeKey().equals(row.getAssessmentMode().getKey())) {
					loadModel();
				}	
			}
		} catch (Exception e) {
			logError("", e);
		}
	}
	
	private void processChangeAssessmentModeEvents(AssessmentModeNotificationEvent event) {
		try {
			TransientAssessmentMode assessmentMode = event.getAssessementMode();
			Long entryKey = assessmentMode.getRepositoryEntryKey();
			if(courseEntry.getKey().equals(entryKey)) {
				List<AssessmentModeOverviewRow> rows = model.getObjects();
				for(AssessmentModeOverviewRow row:rows) {
					if(assessmentMode.getModeKey().equals(row.getAssessmentMode().getKey())
							&& (!Objects.equals(assessmentMode.getStatus(), row.getAssessmentMode().getStatus())
									|| !Objects.equals(assessmentMode.getEndStatus(), row.getAssessmentMode().getEndStatus()))) {
						loadModel();
					}	
				}
			}
		} catch (Exception e) {
			logError("", e);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(startDialogBox == source) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				doStart((AssessmentModeOverviewRow)startDialogBox.getUserObject());
				fireEvent(ureq, new AssessmentModeStatusEvent());
			}
		} else if(stopCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel();
				fireEvent(ureq, new AssessmentModeStatusEvent());
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(stopCtrl);
		removeAsListenerAndDispose(cmc);
		stopCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("start".equals(link.getCmd())) {
				doConfirmStart(ureq, (AssessmentModeOverviewRow)link.getUserObject());
			} else if("end".equals(link.getCmd())) {
				doConfirmStop(ureq, (AssessmentModeOverviewRow)link.getUserObject());
			} else if("element".equals(link.getCmd())) {
				doJumpTo(ureq, (CourseNode)link.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doConfirmStart(UserRequest ureq, AssessmentModeOverviewRow mode) {
		String title = translate("confirm.start.title");
		String text = translate("confirm.start.text");
		startDialogBox = activateYesNoDialog(ureq, title, text, startDialogBox);
		startDialogBox.setUserObject(mode);
	}

	private void doStart(AssessmentModeOverviewRow row) {
		AssessmentMode mode = asssessmentModeManager.getAssessmentModeById(row.getAssessmentMode().getKey());
		if(mode == null) {
			showWarning("warning.assessment.mode.already.deleted");
		} else {
			assessmentModeCoordinationService.startAssessment(mode);
			getLogger().info(Tracing.M_AUDIT, "Start assessment mode : {} ({}) in course: {} ({})",
					mode.getName(), mode.getKey(), courseEntry.getDisplayname(), courseEntry.getKey());
		}
		loadModel();
	}
	
	private void doConfirmStop(UserRequest ureq, AssessmentModeOverviewRow row) {
		if(guardModalController(stopCtrl)) return;

		AssessmentMode mode = asssessmentModeManager.getAssessmentModeById(row.getAssessmentMode().getKey());
		if(mode == null) {
			showWarning("warning.assessment.mode.already.deleted");
			loadModel();
		} else {
			stopCtrl = new ConfirmStopAssessmentModeController(ureq, getWindowControl(), mode);
			listenTo(stopCtrl);
			
			String title = translate("confirm.stop.title");
			cmc = new CloseableModalController(getWindowControl(), "close", stopCtrl.getInitialComponent(), true, title, true);
			cmc.activate();
			listenTo(cmc);
		}
	}

	private void doJumpTo(UserRequest ureq, CourseNode node) {
		fireEvent(ureq, new CourseNodeEvent(CourseNodeEvent.SELECT_COURSE_NODE, node.getIdent()));
	}
}

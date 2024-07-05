/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.ui.tool;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.commons.calendar.CalendarUtils;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
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
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.logging.Tracing;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.EndStatus;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.AssessmentModeCoordinationService;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.AssessmentModeNotificationEvent;
import org.olat.course.assessment.model.TransientAssessmentMode;
import org.olat.course.assessment.ui.mode.AssessmentModeListController;
import org.olat.course.assessment.ui.mode.ChangeAssessmentModeEvent;
import org.olat.course.assessment.ui.mode.ModeStatusCellRenderer;
import org.olat.course.assessment.ui.tool.event.AssessmentModeStatusEvent;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.lecture.LectureBlock;
import org.olat.modules.lecture.LectureService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 25 févr. 2021<br>
 *
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 */
public class StopAssessmentWarningController extends BasicController implements GenericEventListener {

	private Link stopAssessmentMode;
	private Link startAssessmentMode;
	private Link infoLink;

	private final VelocityContainer mainVC;
	private final TooledStackedPanel stackPanel;
	private final RepositoryEntry courseEntry;
	private final AssessmentToolSecurityCallback assessmentCallback;
	private List<AssessmentMode> assessmentModes;

	private CloseableModalController cmc;
	private DialogBoxController startDialogBox;
	private ConfirmStopAssessmentModeController stopCtrl;
	private CloseableCalloutWindowController eventCalloutCtrl;
	private AssessmentModeDetailsController assessmentModeDetailsCtrl;

	@Autowired
	private LectureService lectureService;
	@Autowired
	private AssessmentModeManager assessmentModeManager;
	@Autowired
	private AssessmentModeCoordinationService assessmentModeCoordinationService;

	public StopAssessmentWarningController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
										   RepositoryEntry courseEntry, List<AssessmentMode> modes, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl);
		this.stackPanel = stackPanel;
		this.courseEntry = courseEntry;
		this.assessmentCallback = assessmentCallback;
		mainVC = createVelocityContainer("assessment_mode_warn");
		assessmentModes = modes;
		assessmentModeMessage(modes);
		putInitialPanel(mainVC);

		CoordinatorManager.getInstance().getCoordinator().getEventBus()
				.registerFor(this, getIdentity(), ChangeAssessmentModeEvent.ASSESSMENT_MODE_ORES);
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
				.registerFor(this, getIdentity(), AssessmentModeNotificationEvent.ASSESSMENT_MODE_NOTIFICATION);
	}

	@Override
	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
				.deregisterFor(this, ChangeAssessmentModeEvent.ASSESSMENT_MODE_ORES);
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
				.deregisterFor(this, AssessmentModeNotificationEvent.ASSESSMENT_MODE_NOTIFICATION);
		super.doDispose();
	}

	private void reloadAssessmentModeMessage() {
		assessmentModes = assessmentModeManager.getCurrentAssessmentMode(courseEntry, new Date());
		assessmentModeMessage(assessmentModes);
	}

	private void assessmentModeMessage(List<AssessmentMode> modes) {
		// filter closed assessment mode
		modes = modes.stream()
				.filter(m -> !(Status.end.equals(m.getStatus()) && EndStatus.all.equals(m.getEndStatus())))
				.toList();

		List<LectureBlock> lectures = lectureService.getLectureBlocks(courseEntry, getIdentity());
		Set<Long> taughtLectures = lectures.stream().map(LectureBlock::getKey).collect(Collectors.toSet());

		if (modes.size() == 1) {
			AssessmentMode mode = modes.get(0);
			LectureBlock block = mode.getLectureBlock();
			boolean allowToStartStop = assessmentCallback.canStartStopAllAssessments()
					|| (block != null && taughtLectures.contains(block.getKey()));

			assessmentModeMessageFormatting(mode, mainVC);
			if (mode.isManualBeginEnd() && allowToStartStop) {
				if (assessmentModeCoordinationService.canStart(mode)) {
					String label = translate("assessment.tool.start");
					if (startAssessmentMode == null) {
						startAssessmentMode = LinkFactory.createCustomLink("assessment.start", "start", label, Link.BUTTON_SMALL | Link.NONTRANSLATED, mainVC, this);
					}
					startAssessmentMode.setIconLeftCSS("o_icon o_icon-fw o_icon_status_in_progress");
					startAssessmentMode.setUserObject(mode);
				} else if (canStopAssessmentMode(mode)) {
					if (startAssessmentMode != null) {
						startAssessmentMode.setVisible(false);
					}
					String label = translate("assessment.tool.stop");
					if (stopAssessmentMode == null) {
						stopAssessmentMode = LinkFactory.createCustomLink("assessment.stop", "stop", label, Link.BUTTON_SMALL | Link.NONTRANSLATED, mainVC, this);
					}
					stopAssessmentMode.setIconLeftCSS("o_icon o_icon-fw o_as_mode_stop");
					if (assessmentModeCoordinationService.isDisadvantageCompensationExtensionTime(mode)) {
						stopAssessmentMode.setIconRightCSS("o_icon o_icon-fw o_icon_disadvantage_compensation");
					}
					stopAssessmentMode.setUserObject(mode);
				} else {
					if (startAssessmentMode != null) {
						startAssessmentMode.setVisible(false);
					}
					if (stopAssessmentMode != null) {
						stopAssessmentMode.setVisible(false);
					}
				}
			}
			mainVC.contextPut("modeStatus", mode.getStatus().name());
		} else if (modes.size() > 1) {
			if (startAssessmentMode != null) {
				startAssessmentMode.setVisible(false);
			}
			if (stopAssessmentMode != null) {
				stopAssessmentMode.setVisible(false);
			}
			assessmentModeMessageFormatting(mainVC, modes);
		} else if (stackPanel != null) {
			stackPanel.removeMessageComponent();
		}
	}

	@Override
	public void event(Event event) {
		if (event instanceof ChangeAssessmentModeEvent came) {
			processChangeAssessmentModeEvents(came);
		} else if (event instanceof AssessmentModeNotificationEvent amne) {
			processChangeAssessmentModeEvents(amne);
		}
	}

	private void processChangeAssessmentModeEvents(AssessmentModeNotificationEvent event) {
		try {
			if (event.getAssessementMode() instanceof TransientAssessmentMode aMode) {
				processChangeAssessmentModeEvents(aMode.getModeKey(), aMode.getRepositoryEntryKey(),
						aMode.getEnd(), aMode.getFollowupTime(), aMode.getStatus(), aMode.getEndStatus());
			}
		} catch (Exception e) {
			logError("", e);
		}
	}

	private void processChangeAssessmentModeEvents(ChangeAssessmentModeEvent event) {
		try {
			processChangeAssessmentModeEvents(event.getAssessmentModeKey(), event.getEntryKey(),
					event.getEnd(), event.getFollowUpTime(), event.getStatus(), event.getEndStatus());
		} catch (Exception e) {
			logError("", e);
		}
	}

	private void processChangeAssessmentModeEvents(Long assessmentModeKey, Long entryKey,
												   Date end, int followUptime, Status status, EndStatus endStatus) {
		try {
			List<AssessmentMode> currentModes = assessmentModes;
			if (courseEntry.getKey().equals(entryKey)) {
				if (currentModes != null && !currentModes.isEmpty()) {
					for (AssessmentMode currentMode : currentModes) {
						if (assessmentModeKey.equals(currentMode.getKey())) {
							if (needReload(currentMode, end, followUptime, status, endStatus)) {
								reloadAssessmentModeMessage();
							}
							return;
						}
					}
				}

				reloadAssessmentModeMessage();
			}
		} catch (Exception e) {
			logError("", e);
		}
	}

	private boolean needReload(AssessmentMode currentMode, Date end, int followUpTime, Status status, EndStatus endStatus) {
		return currentMode == null || currentMode.getStatus() != status || currentMode.getEndStatus() != endStatus
				|| !DateUtils.isSameDate(currentMode.getEnd(), end) || currentMode.getFollowupTime() != followUpTime;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (startAssessmentMode == source) {
			doConfirmStart(ureq, (AssessmentMode) startAssessmentMode.getUserObject());
		} else if (stopAssessmentMode == source) {
			doConfirmStop(ureq, (AssessmentMode) stopAssessmentMode.getUserObject());
		} else if (infoLink == source) {
			doOpenEventCallout(ureq, (List<AssessmentMode>) infoLink.getUserObject());
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (startDialogBox == source) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				doStart((AssessmentMode) startDialogBox.getUserObject());
				fireEvent(ureq, new AssessmentModeStatusEvent());
			}
		} else if (stopCtrl == source) {
			if (event == Event.DONE_EVENT) {
				doAfterStop(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		} else if (eventCalloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(assessmentModeDetailsCtrl);
		removeAsListenerAndDispose(eventCalloutCtrl);
		removeControllerListener(stopCtrl);
		removeAsListenerAndDispose(cmc);
		assessmentModeDetailsCtrl = null;
		eventCalloutCtrl = null;
		stopCtrl = null;
		cmc = null;
	}

	private void doOpenEventCallout(UserRequest ureq, List<AssessmentMode> modes) {
		if (eventCalloutCtrl != null && assessmentModeDetailsCtrl != null) return;

		removeAsListenerAndDispose(eventCalloutCtrl);
		removeAsListenerAndDispose(assessmentModeDetailsCtrl);

		assessmentModeDetailsCtrl = new AssessmentModeDetailsController(ureq, getWindowControl(), modes);
		listenTo(assessmentModeDetailsCtrl);

		Component eventCmp = assessmentModeDetailsCtrl.getInitialComponent();
		eventCalloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(), eventCmp, infoLink.getDispatchID(),
				null, true, "o_cal_event_callout");
		listenTo(eventCalloutCtrl);
		eventCalloutCtrl.activate();
	}

	private boolean canStopAssessmentMode(AssessmentMode mode) {
		if (mode.isManualBeginEnd()) {
			if (assessmentCallback.canStartStopAllAssessments()) {
				return assessmentModeCoordinationService.canStop(mode);
			} else if (mode.getLectureBlock() != null) {
				List<Identity> teachers = lectureService.getTeachers(mode.getLectureBlock());
				return teachers.contains(getIdentity())
						&& assessmentModeCoordinationService.canStop(mode);
			}
		}
		return false;
	}

	private void assessmentModeMessageFormatting(AssessmentMode mode, VelocityContainer warn) {
		Formatter formatter = Formatter.getInstance(getLocale());

		Date begin = mode.getBegin();
		Date end = mode.getEnd();
		String start;
		String stop;
		if (CalendarUtils.isSameDay(begin, end)) {
			start = formatter.formatTimeShort(begin);
			stop = formatter.formatTimeShort(end);
		} else {
			start = formatter.formatDateAndTime(begin);
			stop = formatter.formatDateAndTime(end);
		}

		infoLink = LinkFactory.createLink("mode_info", "mode_info", "CMD_SCORE_DESC", "<i class='o_icon o_icon_info_badge'> </i>", null, null, this, Link.NONTRANSLATED);
		infoLink.setUserObject(Collections.singletonList(mode));
		StringOutput status = new StringOutput();
		ModeStatusCellRenderer modeStatusCellRenderer = new ModeStatusCellRenderer(Util.createPackageTranslator(AssessmentModeListController.class, getLocale()));
		modeStatusCellRenderer.renderStatus(mode.getStatus(), mode.getEndStatus(), status);

		String[] args = new String[]{
				start,
				stop,
				formatter.formatDateAndTime(mode.getEndWithFollowupTime()),
				mode.isManualBeginEnd() ? translate("mode.beginend.manual") : translate("mode.beginend.automatic"),
				formatter.formatTimeShort(mode.getEndWithFollowupTime())
		};

		String i18nMessage;
		if (mode.getStatus().equals(Status.leadtime) && mode.getLeadTime() > 0) {
			i18nMessage = "assessment.mode.now.leadtime";
		} else if (mode.getStatus().equals(Status.followup) && mode.getFollowupTime() > 0) {
			i18nMessage = "assessment.mode.now.followup";
		} else {
			i18nMessage = "assessment.mode.now";
		}

		String message = translate(i18nMessage, args);
		warn.put("infoLink", infoLink);
		warn.contextPut("status", status);
		warn.contextPut("title", StringHelper.escapeHtml(mode.getName()));
		warn.contextPut("message", message);
	}

	private void assessmentModeMessageFormatting(VelocityContainer warn, List<AssessmentMode> modes) {
		warn.contextRemove("title");
		infoLink = LinkFactory.createLink("mode_info", "mode_info", "CMD_SCORE_DESC", "<i class='o_icon o_icon_info_badge'> </i>", null, null, this, Link.NONTRANSLATED);
		infoLink.setUserObject(modes);
		String assessmentToolUrl = Settings.getServerContextPathURI() + "/url/RepositoryEntry/" + courseEntry.getKey() + "/assessmentToolv2/0";
		String i18nMessagePart1 = "assessment.mode.several.part1";
		String messagePart1 = translate(i18nMessagePart1);
		String i18nMessagePart2 = "assessment.mode.several.part2";
		String messagePart2 = translate(i18nMessagePart2, assessmentToolUrl);

		warn.put("infoLink", infoLink);
		warn.contextPut("message", messagePart1);
		warn.contextPut("message2", messagePart2);
		warn.contextPut("modeStatus", "several");
	}

	private void doConfirmStart(UserRequest ureq, AssessmentMode mode) {
		String title = translate("confirm.start.title");
		String text = translate("confirm.start.text");
		startDialogBox = activateYesNoDialog(ureq, title, text, startDialogBox);
		startDialogBox.setUserObject(mode);
	}

	private void doConfirmStop(UserRequest ureq, AssessmentMode mode) {
		if (guardModalController(stopCtrl)) return;

		mode = assessmentModeManager.getAssessmentModeById(mode.getKey());
		if (mode == null) {
			showWarning("warning.assessment.mode.already.deleted");
			doAfterStop(ureq);
		} else {
			stopCtrl = new ConfirmStopAssessmentModeController(ureq, getWindowControl(), mode);
			listenTo(stopCtrl);

			String title = translate("confirm.stop.title");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), stopCtrl.getInitialComponent(), true, title, true);
			cmc.activate();
			listenTo(cmc);
		}
	}

	private void doStart(AssessmentMode mode) {
		if (mode == null) {
			showWarning("warning.assessment.mode.already.deleted");
		} else {
			assessmentModeCoordinationService.startAssessment(mode);
			getLogger().info(Tracing.M_AUDIT, "Start assessment mode : {} ({}) in course: {} ({})",
					mode.getName(), mode.getKey(), courseEntry.getDisplayname(), courseEntry.getKey());
		}
		reloadAssessmentModeMessage();
	}

	private void doAfterStop(UserRequest ureq) {
		reloadAssessmentModeMessage();
		fireEvent(ureq, new AssessmentModeStatusEvent());
	}
}

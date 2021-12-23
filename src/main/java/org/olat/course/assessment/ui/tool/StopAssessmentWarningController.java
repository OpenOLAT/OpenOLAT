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

import java.util.Date;
import java.util.List;
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
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.EndStatus;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.AssessmentModeCoordinationService;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.AssessmentModeNotificationEvent;
import org.olat.course.assessment.model.TransientAssessmentMode;
import org.olat.course.assessment.ui.mode.ChangeAssessmentModeEvent;
import org.olat.course.assessment.ui.tool.event.AssessmentModeStatusEvent;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.lecture.LectureService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StopAssessmentWarningController extends BasicController implements GenericEventListener {

	
	private Link stopAssessmentMode;
	private final VelocityContainer mainVC;
	private final TooledStackedPanel stackPanel;

	private final RepositoryEntry courseEntry;
	private final AssessmentToolSecurityCallback assessmentCallback;
	private List<AssessmentMode> assessmentModes;
	
	private CloseableModalController cmc;
	private ConfirmStopAssessmentModeController stopCtrl;
	
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
				.collect(Collectors.toList());
		
		if(modes.size() == 1) {
			AssessmentMode mode = modes.get(0);
			assessmemntModeMessageFormatting(mode, mainVC);
			if(canStopAssessmentMode(mode)) {
				String modeName = mode.getName();
				String label = translate("assessment.tool.stop", new String[] { StringHelper.escapeHtml(modeName) });
				if(stopAssessmentMode == null) {
					stopAssessmentMode = LinkFactory.createCustomLink("assessment.stop", "stop", label, Link.BUTTON_SMALL | Link.NONTRANSLATED, mainVC, this);
				}
				stopAssessmentMode.setIconLeftCSS("o_icon o_icon-fw o_as_mode_stop");
				if(assessmentModeCoordinationService.isDisadvantageCompensationExtensionTime(mode)) {
					stopAssessmentMode.setIconRightCSS("o_icon o_icon-fw o_icon_disadvantage_compensation");
				}
				stopAssessmentMode.setUserObject(mode);
			}
		} else if(modes.size() > 1) {
			assessmemntModeMessageFormatting(modes, mainVC);
		} else if(stackPanel != null) {
			stackPanel.removeMessageComponent();
		}
	}
	
	@Override
	public void event(Event event) {
		if(event instanceof ChangeAssessmentModeEvent) {
			processChangeAssessmentModeEvents((ChangeAssessmentModeEvent)event);
		} else if(event instanceof AssessmentModeNotificationEvent) {
			processChangeAssessmentModeEvents((AssessmentModeNotificationEvent)event);
		}
	}
	
	private void processChangeAssessmentModeEvents(AssessmentModeNotificationEvent event) {
		try {
			TransientAssessmentMode aMode = event.getAssessementMode();
			processChangeAssessmentModeEvents(aMode.getModeKey(), aMode.getRepositoryEntryKey(), aMode.getStatus(), aMode.getEndStatus());
		} catch (Exception e) {
			logError("", e);
		}
	}
	
	private void processChangeAssessmentModeEvents(ChangeAssessmentModeEvent event) {
		try {
			processChangeAssessmentModeEvents(event.getAssessmentModeKey(), event.getEntryKey(), event.getStatus(), event.getEndStatus());
		} catch (Exception e) {
			logError("", e);
		}
	}
	
	private void processChangeAssessmentModeEvents(Long assessmentModeKey, Long entryKey, Status status, EndStatus endStatus) {
		try {
			List<AssessmentMode> currentModes = assessmentModes;
			if(courseEntry.getKey().equals(entryKey)) {
				if(currentModes != null && !currentModes.isEmpty()) {
					for(AssessmentMode currentMode:currentModes) {
						if(assessmentModeKey.equals(currentMode.getKey())) {
							if(needReload(currentMode, status, endStatus)) {
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
	
	private boolean needReload(AssessmentMode currentMode, Status status, EndStatus endStatus) {
		return currentMode == null || currentMode.getStatus() != status || currentMode.getEndStatus() != endStatus;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(stopAssessmentMode == source) {
			doConfirmStop(ureq, (AssessmentMode)stopAssessmentMode.getUserObject());
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(stopCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doAfterStop(ureq);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeControllerListener(stopCtrl);
		removeAsListenerAndDispose(cmc);
		stopCtrl = null;
		cmc = null;
	}

	private boolean canStopAssessmentMode(AssessmentMode mode) {
		if(mode.isManualBeginEnd()) {
			if(assessmentCallback.canStartStopAllAssessments()) {
				return assessmentModeCoordinationService.canStop(mode);
			} else if(mode.getLectureBlock() != null) {
				List<Identity> teachers = lectureService.getTeachers(mode.getLectureBlock());
				return teachers.contains(getIdentity())
						&& assessmentModeCoordinationService.canStop(mode);
			}
		}
		return false;
	}
	
	private void assessmemntModeMessageFormatting(AssessmentMode mode, VelocityContainer warn) {
		Formatter formatter = Formatter.getInstance(getLocale());
		
		Date begin = mode.getBeginWithLeadTime();
		Date end = mode.getEnd();
		String start;
		String stop;
		if(CalendarUtils.isSameDay(begin, end)) {
			start = formatter.formatTimeShort(begin);
			stop = formatter.formatTimeShort(end);
		} else {
			start = formatter.formatDateAndTime(begin);
			stop = formatter.formatDateAndTime(end);
		}
		
		String[] args = new String[] {
				StringHelper.escapeHtml(mode.getName()),
				start,
				stop,
				Integer.toString(mode.getFollowupTime())
			};
		
		String i18nMessage;
		if(mode.isManualBeginEnd() && mode.getFollowupTime() > 0) {
			i18nMessage = "assessment.mode.now.manual.followup";
		} else if(mode.isManualBeginEnd()) {
			i18nMessage = "assessment.mode.now.manual";
		} else if(mode.getFollowupTime() > 0) {
			i18nMessage = "assessment.mode.now.auto.followup";
		} else {
			i18nMessage = "assessment.mode.now.auto";
		}
		String message = translate(i18nMessage, args);
		warn.contextPut("message", message);
	}
		
	private void assessmemntModeMessageFormatting(List<AssessmentMode> modes, VelocityContainer warn) {	
		Date begin = getBeginOfModes(modes);
		Date end = getEndOfModes(modes);
		
		long numOfManualModes = modes.stream()
				.filter(AssessmentMode::isManualBeginEnd)
				.count();
		int followUp = getMaxFollowUp(modes);

		String start;
		String stop;
		Formatter formatter = Formatter.getInstance(getLocale());
		if(CalendarUtils.isSameDay(begin, end)) {
			start = formatter.formatTimeShort(begin);
			stop = formatter.formatTimeShort(end);
		} else {
			start = formatter.formatDateAndTime(begin);
			stop = formatter.formatDateAndTime(end);
		}
		String[] args = new String[] { "", start, stop, Integer.toString(followUp) };// mimic other args
		
		boolean fullManual = numOfManualModes == modes.size();
		boolean mixed = numOfManualModes > 0 && numOfManualModes != modes.size();

		String i18nMessage;
		if(fullManual && followUp > 0) {
			i18nMessage = "assessment.mode.several.now.manual.followup";
		} else if(fullManual) {
			i18nMessage = "assessment.mode.several.now.manual";
		} else if(mixed && followUp > 0) {
			i18nMessage = "assessment.mode.several.now.mixed.followup";
		} else if(mixed) {
			i18nMessage = "assessment.mode.several.now.mixed";
		} else if(followUp > 0) {// full auto
			i18nMessage = "assessment.mode.several.now.auto.followup";
		} else {
			i18nMessage = "assessment.mode.several.now.auto";
		}

		String message = translate(i18nMessage, args);
		warn.contextPut("message", message);
	}
	
	private int getMaxFollowUp(List<AssessmentMode> modes) {
		int followUp = 0;
		for(AssessmentMode mode:modes) {
			if(mode.getFollowupTime() > followUp) {
				followUp = mode.getFollowupTime();
			}
		}
		return followUp;
	}
	
	private Date getBeginOfModes(List<AssessmentMode> modes) {
		Date start = null;
		for(AssessmentMode mode:modes) {
			Date begin = mode.getBeginWithLeadTime();
			if(start == null || (begin != null && begin.before(start))) {
				start = begin;
			}
		}
		return start;
	}
	
	private Date getEndOfModes(List<AssessmentMode> modes) {
		Date stop = null;
		for(AssessmentMode mode:modes) {
			Date end = mode.getEnd();
			if(stop == null || (end != null && end.after(stop))) {
				stop = end;
			}
		}
		return stop;
	}
	
	private void doConfirmStop(UserRequest ureq, AssessmentMode mode) {
		if(guardModalController(stopCtrl)) return;
		
		mode = assessmentModeManager.getAssessmentModeById(mode.getKey());
		if(mode == null) {
			showWarning("warning.assessment.mode.already.deleted");
			doAfterStop(ureq);
		} else {
			stopCtrl = new ConfirmStopAssessmentModeController(ureq, getWindowControl(), mode);
			listenTo(stopCtrl);
	
			String title = translate("confirm.stop.title");
			cmc = new CloseableModalController(getWindowControl(), "close", stopCtrl.getInitialComponent(), true, title, true);
			cmc.activate();
			listenTo(cmc);
		}
	}
	
	private void doAfterStop(UserRequest ureq) {
		reloadAssessmentModeMessage();
		fireEvent(ureq, new AssessmentModeStatusEvent());
	}
}

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
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.EndStatus;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.AssessmentModeCoordinationService;
import org.olat.course.assessment.AssessmentModeManager;
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
public class StopAssessmentWarningController extends BasicController {
	
	private Link stopAssessmentMode;
	private final VelocityContainer mainVC;
	private final TooledStackedPanel stackPanel;

	private final RepositoryEntry courseEntry;
	private final AssessmentToolSecurityCallback assessmentCallback;
	
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
		assessmentModeMessage(modes);
		putInitialPanel(mainVC);
	}

	@Override
	protected void doDispose() {
		//
	}
	private void reloadAssessmentModeMessage() {
		List<AssessmentMode> modes = assessmentModeManager.getCurrentAssessmentMode(courseEntry, new Date());
		assessmentModeMessage(modes);
	}
	
	private void assessmentModeMessage(List<AssessmentMode> modes) {
		// filter closed assessment mode
		modes = modes.stream()
				.filter(m -> !(Status.end.equals(m.getStatus()) && EndStatus.all.equals(m.getEndStatus())))
				.collect(Collectors.toList());
		
		if(modes.size() == 1) {
			AssessmentMode mode = modes.get(0);
			assessmemntModeMessageFormatting("assessment.mode.now",  modes, mainVC);
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
			assessmemntModeMessageFormatting("assessment.mode.several.now",  modes, mainVC);
		} else if(stackPanel != null) {
			stackPanel.removeMessageComponent();
		}
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
		if(assessmentCallback.canStartStopAllAssessments()) {
			return assessmentModeCoordinationService.canStop(mode);
		} else if(mode.getLectureBlock() != null) {
			List<Identity> teachers = lectureService.getTeachers(mode.getLectureBlock());
			return teachers.contains(getIdentity())
					&& assessmentModeCoordinationService.canStop(mode);
		}
		return false;
	}
	
	private void assessmemntModeMessageFormatting(String i18nMessage, List<AssessmentMode> modes, VelocityContainer warn) {
		Date begin = getBeginOfModes(modes);
		Date end = getEndOfModes(modes);
		
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
		warn.contextPut("message", translate(i18nMessage, new String[] { start, stop }));
	}
	
	private Date getBeginOfModes(List<AssessmentMode> modes) {
		Date start = null;
		for(AssessmentMode mode:modes) {
			Date begin = mode.getBegin();
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

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
package org.olat.course.assessment.ui.inspection;

import java.io.File;
import java.net.URI;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.commons.fullWebApp.LockRequest;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.htmlheader.jscss.JSAndCSSComponent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.Coordinator;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentInspection;
import org.olat.course.assessment.AssessmentInspectionChangeEvent;
import org.olat.course.assessment.AssessmentInspectionService;
import org.olat.course.assessment.AssessmentInspectionStatusEnum;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.model.TransientAssessmentInspection;
import org.olat.course.assessment.ui.inspection.elements.AssessmentInspectionTimerFormItem;
import org.olat.course.assessment.ui.inspection.elements.InspectionTimesUpEvent;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21AssessmentResultsOptions;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.ui.AssessmentResultController;
import org.olat.ims.qti21.ui.AssessmentTestDisplayController;
import org.olat.ims.qti21.ui.AssessmentTestSessionComparator;
import org.olat.ims.qti21.ui.ResourcesMapper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 janv. 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentInspectionMainController extends BasicController implements GenericEventListener {
	
	private final VelocityContainer mainVC;
	private final Link closeInspectionButton;
	
	private AssessmentInspection inspection;
	private final long startupEffectiveDuration;
	private final Date controllerCreationDate = new Date();
	private final InspectionStatus inspectionStatus = new InspectionStatus();
	
	private TimerController timerCtrl;
	private CloseableModalController cmc;
	private AssessmentResultController resultCtrl;
	private ConfirmCloseInspectionController confirmCloseCtrl;

	@Autowired
	private Coordinator coordinator;
	@Autowired
	protected QTI21Service qtiService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private AssessmentInspectionService inspectionService;
	
	public AssessmentInspectionMainController(UserRequest ureq, WindowControl wControl, AssessmentInspection inspection) {
		super(ureq, wControl);
		this.inspection = inspection;
		startupEffectiveDuration = inspection.getEffectiveDuration() == null ? 0l : inspection.getEffectiveDuration().longValue();
		
		mainVC = createVelocityContainer("inspection_main");
		
		String options = inspection.getConfiguration().getOverviewOptions();
		RepositoryEntry courseEntry = repositoryService.loadBy(inspection.getConfiguration().getRepositoryEntry());
		ICourse course = CourseFactory.loadCourse(courseEntry);
		CourseNode courseNode = course.getRunStructure().getNode(inspection.getSubIdent());
		if(courseNode instanceof IQTESTCourseNode testNode) {
			initTestResults(ureq, courseEntry, testNode, options);
		}
		closeInspectionButton = LinkFactory.createButton("close.inspection", mainVC, this);
		closeInspectionButton.setCustomEnabledLinkCSS("btn btn-default btn-primary");
		mainVC.put("close.inspection", closeInspectionButton);
		putInitialPanel(mainVC);
		
		OLATResourceable sessionOres = OresHelper
        		.createOLATResourceableInstance(AssessmentInspection.class, inspection.getKey());
        coordinator.getEventBus().registerFor(this, getIdentity(), sessionOres);
	}

	private void initTestResults(UserRequest ureq, RepositoryEntry courseEntry, IQTESTCourseNode testNode, String options) {
		List<AssessmentTestSession> sessions = qtiService.getAssessmentTestSessions(courseEntry, testNode.getIdent(), getIdentity(), true);
		Collections.sort(sessions, new AssessmentTestSessionComparator(true));
		
		AssessmentTestSession session = null;
		for(AssessmentTestSession sess:sessions) {
			if(sess.getFinishTime() != null || sess.getTerminationTime() != null ) {
				session = sess;
				break;// last finished session
			}
		}
		
		if(session != null) {
			FileResourceManager frm = FileResourceManager.getInstance();
			File fUnzippedDirRoot = frm.unzipFileResource(session.getTestEntry().getOlatResource());
			URI assessmentObjectUri = qtiService.createAssessmentTestUri(fUnzippedDirRoot);
			File submissionDir = qtiService.getSubmissionDirectory(session);
			String mapperUri = registerCacheableMapper(ureq, "QTI21DetailsResources::" + session.getKey(),
					new ResourcesMapper(assessmentObjectUri, fUnzippedDirRoot, submissionDir));
			
			QTI21AssessmentResultsOptions resultsOptions = QTI21AssessmentResultsOptions.parseString(options);
			resultCtrl = new AssessmentResultController(ureq, getWindowControl(), getIdentity(), false,
					session, fUnzippedDirRoot, mapperUri, null, resultsOptions, false, true, false);
			listenTo(resultCtrl);
			mainVC.put("results", resultCtrl.getInitialComponent());
			
			timerCtrl = new TimerController(ureq, getWindowControl());
			listenTo(timerCtrl);
			mainVC.put("timer", timerCtrl.getInitialComponent());
		}
	}
	
	private boolean isInspectionEnded() {
		return inspection != null && (inspection.getInspectionStatus().isEnded()
				|| (inspection.getToDate() != null && inspection.getToDate().before(new Date())));
	}
	
	@Override
	protected void doDispose() {
		if(inspection != null) {
			inspection = inspectionService.getInspection(inspection.getKey());
			if(inspection.getInspectionStatus() == AssessmentInspectionStatusEnum.inProgress) {
				long durationSeconds = (new Date().getTime() - controllerCreationDate.getTime()) / 1000;
				inspectionService.pauseInspection(getIdentity(), inspection, durationSeconds);
			}
		}
	}

	@Override
	public void event(Event event) {
		if(event instanceof AssessmentInspectionChangeEvent aice && aice.sameInspection(inspection)) {
			processInspectionChanges();
		}
	}
	
	private void processInspectionChanges() {
		inspection = inspectionService.getInspection(inspection.getKey());
		if(timerCtrl != null) {
			timerCtrl.extraTime();
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(timerCtrl == source) {
			if(event instanceof InspectionTimesUpEvent) {
				doTimeUpInspection(ureq);
			}
		} else if(confirmCloseCtrl == source) {
			cmc.deactivate();
			cleanUp();
			if(event == Event.DONE_EVENT) {
				doClose(ureq);
			}
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(confirmCloseCtrl);
		removeAsListenerAndDispose(cmc);
		confirmCloseCtrl = null;
		cmc = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(closeInspectionButton == source) {
			doConfirmClose(ureq);
		}
	}
	
	private void doConfirmClose(UserRequest ureq) {
		confirmCloseCtrl = new ConfirmCloseInspectionController(ureq, getWindowControl(), inspection);
		listenTo(confirmCloseCtrl);
		
		String title = translate("close.inspection");
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmCloseCtrl.getInitialComponent(), true, title);
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doClose(UserRequest ureq) {
		long durationSeconds = (ureq.getRequestTimestamp().getTime() - controllerCreationDate.getTime()) / 1000;
		inspection = inspectionService.endInspection(getIdentity(), inspection, durationSeconds, getIdentity());
		
		final OLATResourceable ores = OresHelper.createOLATResourceableInstance(AssessmentInspection.class,
				inspection.getKey());
		getWindowControl().getWindowBackOffice().getWindow().getDTabs()
			.closeDTab(ureq, ores, null);
		getWindowControl().getWindowBackOffice().getChiefController()
			.unlock(ureq);
		NewControllerFactory.getInstance().launch("[MyCoursesSite:0]", ureq, getWindowControl());
	}
	
	private void doTimeUpInspection(UserRequest ureq) {
		long durationSeconds = (ureq.getRequestTimestamp().getTime() - controllerCreationDate.getTime()) / 1000;
		inspection = inspectionService.endInspection(getIdentity(), inspection, durationSeconds, getIdentity());
		
		final LockRequest lockRequest = ureq.getUserSession().getLockMode();
		final OLATResourceable ores = OresHelper.createOLATResourceableInstance(AssessmentInspection.class,
				inspection.getKey());
		getWindowControl().getWindowBackOffice().getWindow().getDTabs()
			.closeDTab(ureq, ores, null);
		
		if(lockRequest instanceof TransientAssessmentInspection transientInspection) {
			transientInspection.setStatus(Status.followup);
			getWindowControl().getWindowBackOffice().getChiefController()
				.unlockResource(ureq, lockRequest);
		}
	}
	
	private class TimerController extends FormBasicController {
		
		private AssessmentInspectionTimerFormItem timerEl;
		
		public TimerController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl, "inspection_timer", Util.createPackageTranslator(AssessmentTestDisplayController.class, ureq.getLocale()));
			
			initForm(ureq);
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			if(formLayout instanceof FormLayoutContainer layoutCont) {
				timerEl = new AssessmentInspectionTimerFormItem("inspectiontimer", inspectionStatus);
				layoutCont.add(timerEl);
				
				String[] jss = new String[] {
						"js/jquery/qti/jquery.qtiTimer.js"
				};
				JSAndCSSComponent js = new JSAndCSSComponent("js", jss, null);
				layoutCont.put("js", js);
			}
		}
		
		@Override
		protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
			if(timerEl == source && event instanceof InspectionTimesUpEvent e) {
				fireEvent(ureq, e);
			}
			super.formInnerEvent(ureq, source, event);
		}
		
		public void extraTime() {
			timerEl.getComponent().setDirty(true);
		}

		@Override
		protected void formOK(UserRequest ureq) {
			//
		}
	}
	
	public class InspectionStatus {
		
		public boolean isTimeLimit() {
			return inspection != null;
		}
		
		public boolean isEnded() {
			return isInspectionEnded();
		}

		private Long getInspectionMaxTimeLimit() {
			Long leadingTimeInMilliSeconds = getLeadingTimeEndInspectionOption();
			long leadingDuration = Long.MAX_VALUE;
			if(leadingTimeInMilliSeconds != null) {
				leadingDuration = leadingTimeInMilliSeconds.longValue() / 1000;
			}
			
			if(inspection.getConfiguration() != null) {
				int extra = inspection.getExtraTime() == null ? 0 : inspection.getExtraTime().intValue();
				int timeLimits = inspection.getConfiguration().getDuration() + extra;
				return Math.min(leadingDuration, timeLimits);
			}
			return null;
		}
		
		private Long getLeadingTimeEndInspectionOption() {
			if(inspection != null && inspection.getToDate() != null) {
				Date endTestDate = inspection.getToDate();
				long diff = endTestDate.getTime() - controllerCreationDate.getTime();
				return Long.valueOf(diff);
			}
			return null;// default is a year
		}
		
		/**
		 * @return The inspection duration in milliseconds
		 */
		public long getInspectionDuration(Date now) {
			if(inspection == null) {
				return -1;
			}

	        final long durationDelta = now.getTime() - controllerCreationDate.getTime(); 
	        // Effective duration is in seconds
	        return (startupEffectiveDuration * 1000l) + durationDelta;
		}
		
		public String getInspectionEndTime(Date now) {
			Long timeLimits = getInspectionMaxTimeLimit();
			if(timeLimits != null) {
				long inspectionDuration = getInspectionDuration(now);
				if(inspectionDuration < 0) {
					inspectionDuration = 0;
				}
				
				// gets a calendar using the default time zone and locale.
				Calendar calendar = Calendar.getInstance(); 
				calendar.setTime(now);
				calendar.add(Calendar.MILLISECOND, -(int)inspectionDuration);
				calendar.add(Calendar.SECOND, timeLimits.intValue() + 1);// +1 to compensante some rounding errors
				return Formatter.getInstance(getLocale()).formatTimeShort(calendar.getTime());
			}
			return "";
		}
		
		/**
		 * @return A duration in milliseconds
		 */
		public long getInspectionMaximumTimeLimits() {
			long maxDuration = -1l;
			Long timeLimits = getInspectionMaxTimeLimit();
			if(timeLimits != null) {
				maxDuration = timeLimits.longValue() * 1000;
			}
			return maxDuration;
		}
	}
}

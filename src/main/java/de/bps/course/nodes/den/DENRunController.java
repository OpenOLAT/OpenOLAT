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
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
 */
package de.bps.course.nodes.den;

import java.util.List;

import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Util;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.modules.ModuleConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import de.bps.course.nodes.DENCourseNode;

 /**
  * Date enrollment run controller
  * @author skoeber
  */
public class DENRunController extends BasicController implements GenericEventListener {

	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(DENRunController.class);

	private DENCourseNode courseNode;
	private DENStatus status;

	//objects for run view
	private DENRunTableDataModel runTableData;
	private List<KalendarEvent> runTableDataList;
	private TableController runDENTable;
	private Link manageDatesBtn;
	private Link enrollmentListBtn;

	
	private CloseableModalController manageDatesModalCntrll, listParticipantsModalCntrll;

	private DENManager denManager;
	private ContextualSubscriptionController csc;
	private SubscriptionContext subsContext;
	
	private final boolean enrollmentEnabled;
	private final boolean cancelEnrollEnabled;
	
	private VelocityContainer runVC;
	private OLATResourceable ores;
	
	@Autowired
	private NotificationsManager notificationsManager;

	/**
	 * Standard constructor for Date Enrollment run controller
	 * @param ureq
	 * @param wControl
	 * @param moduleConfig
	 * @param denCourseNode
	 * @param userCourseEnv
	 */
	public DENRunController(UserRequest ureq, WindowControl wControl, ModuleConfiguration moduleConfig, UserCourseEnvironment userCourseEnv, DENCourseNode denCourseNode) {
		super(ureq, wControl);
		this.courseNode = denCourseNode;
		
		ICourse course = CourseFactory.loadCourse(userCourseEnv.getCourseEnvironment().getCourseResourceableId());
		ores = course;
		cancelEnrollEnabled = ((Boolean) moduleConfig.get(DENCourseNode.CONF_CANCEL_ENROLL_ENABLED)).booleanValue();
		enrollmentEnabled = !userCourseEnv.isCourseReadOnly();
		
		denManager = DENManager.getInstance();

		//prepare table for run view
		createOrUpdateDateTable(ureq, denCourseNode);
		runDENTable = denManager.createRunDatesTable(ureq, wControl, getTranslator(), runTableData);
		listenTo(runDENTable);
		
		runVC = new VelocityContainer("dateVC", VELOCITY_ROOT + "/run.html", getTranslator(), this);
		
		//show only the options for managing dates and participants if user is admin or course coach
		if(userCourseEnv.isAdmin() || userCourseEnv.isCoach()) {
			// subscription
			subsContext = new SubscriptionContext(course, courseNode.getIdent());
			// if sc is null, then no subscription is desired
			if (subsContext != null) {
				String businessPath = wControl.getBusinessControl().getAsString();
				PublisherData pdata = new PublisherData(OresHelper.calculateTypeName(DENCourseNode.class), String.valueOf(course.getResourceableId()), businessPath);
				csc = new ContextualSubscriptionController(ureq, getWindowControl(), subsContext, pdata);
				runVC.put("subscription", csc.getInitialComponent());
			}
			
			manageDatesBtn = LinkFactory.createButton("config.dates", runVC, this);
			manageDatesBtn.setIconLeftCSS("o_icon o_icon-fw o_icon_calendar");
			manageDatesBtn.setVisible(!userCourseEnv.isCourseReadOnly());
			enrollmentListBtn = LinkFactory.createButton("run.enrollment.list", runVC, this);
			enrollmentListBtn.setIconLeftCSS("o_icon o_icon-fw o_icon_user");
		}
		
		runVC.put("datesTable", runDENTable.getInitialComponent());

		putInitialPanel(runVC);
	}
	
	/**
	 * prepare table for run view
	 * @param ureq
	 * @param wControl
	 * @param denCourseNode
	 * @param initialize
	 */
	private void createOrUpdateDateTable(UserRequest ureq, DENCourseNode denCourseNode) {
		//prepare table for run view
		runTableDataList = denManager.getDENEvents(ores.getResourceableId(), denCourseNode.getIdent());
		runTableData = new DENRunTableDataModel(runTableDataList, getIdentity(), denCourseNode, cancelEnrollEnabled, enrollmentEnabled, getTranslator());
	}
	
	@Override
	protected void doDispose() {
		if(runTableData != null) runTableData = null;
		if(runTableDataList != null) runTableDataList = null;
		if(csc != null) {
			removeAsListenerAndDispose(csc);
			csc = null;
		}
		if(runDENTable != null) {
			removeAsListenerAndDispose(runDENTable);
			runDENTable = null;
		}
		if(manageDatesModalCntrll != null) {
			removeAsListenerAndDispose(manageDatesModalCntrll);
			manageDatesModalCntrll = null;
		}
		if(listParticipantsModalCntrll != null) {
			removeAsListenerAndDispose(listParticipantsModalCntrll);
			listParticipantsModalCntrll = null;
		}
	}

	@Override
	public void event(Event event) {
		//nothing to do
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == manageDatesBtn) {
			//management of dates
			removeAsListenerAndDispose(manageDatesModalCntrll);
			DENManageDatesController datesCtr = new DENManageDatesController(ureq, getWindowControl(), ores, courseNode);
			listenTo(datesCtr);
			manageDatesModalCntrll = new CloseableModalController(getWindowControl(), "close", datesCtr.getInitialComponent(), true, translate("config.dates"));
			manageDatesModalCntrll.activate();
			listenTo(manageDatesModalCntrll);
			
		} else if(source == enrollmentListBtn) {
			//list of participants
			removeAsListenerAndDispose(listParticipantsModalCntrll);
			DENManageParticipantsController partsCtr = new DENManageParticipantsController(ureq, getWindowControl(), ores, courseNode, !enrollmentEnabled);
			listenTo(partsCtr);
			listParticipantsModalCntrll = new CloseableModalController(getWindowControl(), "close", partsCtr.getInitialComponent(), true, translate("dates.table.list"));
			listParticipantsModalCntrll.activate();
			listenTo(listParticipantsModalCntrll);
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(runDENTable == source) {
			//the link to enroll or cancel enrollment is clicked
			if(event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent tableEvent = (TableEvent)event;
				KalendarEvent calEvent = runTableData.getObject(tableEvent.getRowId());
				if(tableEvent.getActionId().equals(DENRunTableDataModel.CMD_ENROLL_IN_DATE)) {
					//do enroll
					status = denManager.doEnroll(ureq.getIdentity(), calEvent, ores, courseNode);
					if(!status.isEnrolled()) showError();
				} else if(tableEvent.getActionId().equals(DENRunTableDataModel.CMD_ENROLLED_CANCEL)) {
					//cancel enrollment
					status = denManager.cancelEnroll(ureq.getIdentity(), calEvent, ores);
					if(!status.isCancelled()) showError();
				}
				createOrUpdateDateTable(ureq, courseNode);
				runDENTable.setTableDataModel(runTableData);
				fireEvent(ureq, Event.DONE_EVENT);
				// inform subscription context about changes
				notificationsManager.markPublisherNews(subsContext, ureq.getIdentity(), true);
				// </OPAL-122>
			}
		} 
	}

	private void showError() {
		String message = status.getErrorMessage();
		if(DENStatus.ERROR_ALREADY_ENROLLED.equals(message)) {
			getWindowControl().setError(translate("enrollment.error.enrolled"));
		} else if(DENStatus.ERROR_NOT_ENROLLED.equals(message)) {
			getWindowControl().setError(translate("enrollment.error.notenrolled"));
		} else if(DENStatus.ERROR_PERSISTING.equals(message)) {
			getWindowControl().setError(translate("enrollment.error.persisting"));
		} else if(DENStatus.ERROR_GENERAL.equals(message)) {
			getWindowControl().setError(translate("enrollment.error.general"));
		} else if(DENStatus.ERROR_FULL.equals(message)) {
			getWindowControl().setError(translate("enrollment.error.full"));
		}
	}

}

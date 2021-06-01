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
package org.olat.course.nodes.pf.ui;

import java.util.Date;

import org.olat.core.commons.modules.bc.FolderRunController;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.nodes.PFCourseNode;
import org.olat.course.nodes.pf.manager.PFManager;
import org.olat.course.nodes.pf.manager.PFView;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.resource.OLATResource;
import org.springframework.beans.factory.annotation.Autowired;
/**
*
* @author Fabian Kiefer, fabian.kiefer@frentix.com, http://www.frentix.com
*
*/
public class PFParticipantController extends BasicController {
	
	private static final long TWO_DAYS_IN_MILLISEC = 2l * 24l * 60l * 60l * 1000l;
	private static final long ONE_DAY_IN_MILLISEC = 24l * 60l * 60l * 1000l;
	
	private TimerComponent timerCmp;
	private VelocityContainer mainVC;
	private FolderRunController folderRunController;
	private ContextualSubscriptionController contextualSubscriptionCtr;
	
	private final PFView pfView;
	private final boolean isCoach;
	private final boolean readOnly;
	private final PFCourseNode pfNode;
	private final CourseEnvironment courseEnv;
	private final Identity assessedIdentity;

	@Autowired
	private PFManager pfManager;

	public PFParticipantController(UserRequest ureq, WindowControl wControl, PFCourseNode pfNode,
			UserCourseEnvironment userCourseEnv, Identity assessedIdentity, PFView view, boolean isCoach, boolean readOnly) {

		super(ureq, wControl);	
		mainVC = createVelocityContainer("participant");
		
		this.pfNode = pfNode;
		this.isCoach = isCoach;
		this.assessedIdentity = assessedIdentity;
		courseEnv = userCourseEnv.getCourseEnvironment();
		this.readOnly = readOnly || userCourseEnv.isCourseReadOnly();
				
		if (!(userCourseEnv.isCoach() || userCourseEnv.isAdmin()) && !(isCoach && readOnly)) {
			OLATResource course = courseEnv.getCourseGroupManager().getCourseResource();
			String businessPath = wControl.getBusinessControl().getAsString();
			SubscriptionContext subsContext = new SubscriptionContext(course, pfNode.getIdent());
			PublisherData publisherData = new PublisherData(OresHelper.calculateTypeName(PFCourseNode.class),
					String.valueOf(course.getResourceableId()), businessPath);
			contextualSubscriptionCtr = new ContextualSubscriptionController(ureq, getWindowControl(), subsContext,
					publisherData);
			listenTo(contextualSubscriptionCtr);
			mainVC.put("contextualSubscription", contextualSubscriptionCtr.getInitialComponent());			
		}

		initLimitMessages(ureq);

		//CourseFreeze
		pfView = view == null ? pfManager.providePFView(pfNode) : view;
		String path;
		switch(pfView) {
			case displayDrop:
				path = translate("drop.box");
				break;
			case displayReturn:
				path = translate("return.box");
				break;
			default:
				path = null;
				break;
		}
		initFolderController(ureq, path);
		
		putInitialPanel(mainVC);	
	}

	private void initLimitMessages(UserRequest ureq) {
		if (pfNode.hasLimitCountConfigured()) {
			mainVC.contextPut("limit", pfNode.getLimitCount());			
		}
		
		if(pfNode.hasDropboxTimeFrameConfigured() && pfNode.getDateStart() != null && pfNode.getDateEnd() != null) {
			Date start = pfNode.getDateStart();
			Date end = pfNode.getDateEnd();
			Date now = ureq.getRequestTimestamp();

			Formatter formatter = Formatter.getInstance(getLocale());
			String[] args = new String[] {
				formatter.formatDate(start), 		// 0 start date
				formatter.formatTimeShort(start), 	// 1 start time
				formatter.formatDate(end),			// 2 end date
				formatter.formatTimeShort(end)		// 3 end time
			};
			
			String i18nKey;
			String cssClass;
			if(now.before(start)) {
				cssClass = "o_info";
				i18nKey = "msg.period.before";
			} else if(now.after(start) && now.before(end)) {
				long timeDiff = end.getTime() - now.getTime();
				if(timeDiff <= 0) {
					cssClass = "o_info";
					i18nKey = "msg.period.after";
				} else if(timeDiff > TWO_DAYS_IN_MILLISEC) {// 2 days		
					cssClass = "o_info";
					i18nKey = "msg.period.within";
				} else if(timeDiff > ONE_DAY_IN_MILLISEC) {				
					cssClass = "o_warning";
					i18nKey = "msg.period.within";
				} else {
					cssClass = "o_error";
					i18nKey = "msg.period.within.oneday";
					timerCmp = new TimerComponent("timer", end);
					timerCmp.addListener(this);
					mainVC.put("timer", timerCmp);
				}
			} else {
				cssClass = "o_info";
				i18nKey = "msg.period.after";
			}
			
			String msg = translate(i18nKey, args);
			mainVC.contextPut("msg", msg);
			mainVC.contextPut("msgCssClass", cssClass);
		}
	}
	
	private void initFolderController(UserRequest ureq, String path) {
		removeAsListenerAndDispose(folderRunController);

		VFSContainer frc = pfManager.provideParticipantFolder(pfNode, pfView, getTranslator(), courseEnv,
				assessedIdentity, isCoach, readOnly);
		folderRunController = new FolderRunController(frc, false, false, false, false, ureq, getWindowControl(), null, null, null);

		folderRunController.disableSubscriptionController();
		listenTo(folderRunController);
		mainVC.put("folder", folderRunController.getInitialComponent());
		folderRunController.activatePath(ureq, path);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(timerCmp == source) {
			if(event instanceof TimesUpEvent) {
				// recalculatSecurityCallback(ureq);
				initLimitMessages(ureq);
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(folderRunController == source) {
			recalculatSecurityCallback(ureq);
		}
	}
	
	private void recalculatSecurityCallback(UserRequest ureq) {
		String path = folderRunController.getCurrentContainerPath();
		initFolderController(ureq, path);
	}

	/**
	 * Remove the subscription panel but let the subscription context active
	 */
	public void disableSubscriptionController() {
		if (contextualSubscriptionCtr != null) {
			mainVC.remove(contextualSubscriptionCtr.getInitialComponent());
		}
	}	
}

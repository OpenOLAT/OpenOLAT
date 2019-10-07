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

import org.olat.core.commons.modules.bc.FolderRunController;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.ui.ContextualSubscriptionController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
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

	private VelocityContainer mainVC;
	private FolderRunController folderRunController;
	private ContextualSubscriptionController contextualSubscriptionCtr;
	
	@Autowired
	private PFManager pfManager;

	@SuppressWarnings("incomplete-switch")
	public PFParticipantController(UserRequest ureq, WindowControl wControl, PFCourseNode pfNode,
			UserCourseEnvironment userCourseEnv, Identity identity, boolean isCoach, boolean readOnly) {
		super(ureq, wControl);	
		mainVC = createVelocityContainer("participant");
		
		CourseEnvironment courseEnv = userCourseEnv.getCourseEnvironment();
		
		if (pfNode.hasLimitCountConfigured()){
			mainVC.contextPut("limit", pfNode.getLimitCount());			
		}
				
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
		//CourseFreeze
		readOnly = readOnly ? true : userCourseEnv.isCourseReadOnly();
		
		PFView pfView = pfManager.providePFView(pfNode);
		VFSContainer frc = pfManager.provideParticipantFolder(pfNode, pfView, getTranslator(),courseEnv,
				identity, isCoach, readOnly);
		folderRunController = new FolderRunController(frc, false, false, false, false, ureq, wControl, null, null, null);
		folderRunController.disableSubscriptionController();
		listenTo(folderRunController);
		mainVC.put("folder", folderRunController.getInitialComponent());
		
		switch (pfView) {
		case displayDrop:
			folderRunController.activatePath(ureq, translate("drop.box"));
			break;
		case displayReturn:
			folderRunController.activatePath(ureq, translate("return.box"));
			break;
		}
		
		putInitialPanel(mainVC);
		
	}

	@Override
	protected void doDispose() {
		
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		
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

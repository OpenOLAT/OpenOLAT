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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.nodes.PFCourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
/**
*
* @author Fabian Kiefer, fabian.kiefer@frentix.com, http://www.frentix.com
*
*/
public class PFPreviewController extends BasicController {

	public PFPreviewController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
	}
	
	public PFPreviewController(UserRequest ureq, WindowControl wControl, PFCourseNode pfNode, 
			UserCourseEnvironment userCourseEnv) {
		super(ureq, wControl);

		VelocityContainer previewVC = createVelocityContainer("preview");

		previewVC.contextPut("drop", pfNode.hasParticipantBoxConfigured());
		previewVC.contextPut("return", pfNode.hasCoachBoxConfigured());
		previewVC.contextPut("limit", pfNode.hasLimitCountConfigured());
		String timeframe = pfNode.hasDropboxTimeFrameConfigured() ?
				pfNode.getDateStart().toString() + " - " + pfNode.getDateEnd().toString() : "-";
		previewVC.contextPut("timeframe", timeframe);
		
		PFParticipantController participantController = new PFParticipantController(ureq, getWindowControl(), 
				pfNode, userCourseEnv, getIdentity(), null, true, true);	
		listenTo(participantController);
		participantController.disableSubscriptionController();
		previewVC.put("folder", participantController.getInitialComponent());
		
		putInitialPanel(previewVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}

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
package org.olat.modules.assessment.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.course.assessment.ui.tool.AssessedIdentityLargeInfosController;
import org.olat.modules.assessment.ui.event.AssessmentFormEvent;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 09.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessedIdentityRepositoryEntryController extends BasicController implements AssessedIdentityController {
	
	private final Identity assessedIdentity;

	private final TooledStackedPanel stackPanel;
	private final VelocityContainer identityAssessmentVC;

	private Controller detailsCtrl;
	private AssessmentForm assessmentForm;
	private AssessedIdentityLargeInfosController infosController;

	@Autowired
	private RepositoryHandlerFactory repositoryHandlerFactory;
	
	public AssessedIdentityRepositoryEntryController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry assessableEntry, Identity assessedIdentity, AssessableResource element) {
		super(ureq, wControl);
		
		this.stackPanel = stackPanel;
		this.assessedIdentity = assessedIdentity;
		
		identityAssessmentVC = createVelocityContainer("identity_personal_infos");
		identityAssessmentVC.contextPut("user", assessedIdentity.getUser());
		
		infosController = new AssessedIdentityLargeInfosController(ureq, wControl, assessedIdentity);
		listenTo(infosController);
		identityAssessmentVC.put("identityInfos", infosController.getInitialComponent());
		
		RepositoryHandler handler = repositoryHandlerFactory.getRepositoryHandler(assessableEntry);
		if(handler.supportsAssessmentDetails()) {
			detailsCtrl = handler.createAssessmentDetailsController(assessableEntry, ureq, getWindowControl(), stackPanel, assessedIdentity);
			listenTo(detailsCtrl);
			identityAssessmentVC.put("details", detailsCtrl.getInitialComponent());
		}
		
		assessmentForm = new AssessmentForm(ureq, getWindowControl(), assessedIdentity, assessableEntry, element);
		listenTo(assessmentForm);
		identityAssessmentVC.put("assessmentForm", assessmentForm.getInitialComponent());
		
		putInitialPanel(identityAssessmentVC);
	}

	@Override
	public Identity getAssessedIdentity() {
		return assessedIdentity;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(assessmentForm == source) {
			if(event instanceof AssessmentFormEvent) {
				AssessmentFormEvent afe = (AssessmentFormEvent)event;
				if(afe.isClose()) {
					stackPanel.popController(assessmentForm);
					fireEvent(ureq, Event.DONE_EVENT);
				} else {
					fireEvent(ureq, Event.CHANGED_EVENT);
				}
			} else if(event == Event.CANCELLED_EVENT) {
				stackPanel.popController(assessmentForm);
			}
		} else if(detailsCtrl == source) {
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				assessmentForm.reloadData();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}

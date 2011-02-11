/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/
package ch.unizh.portal.zsuz;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.mail.MailTemplate;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.mail.MailerWithTemplate;

/**
 * Description:<br>
 * TODO: patrickb Class Description for ZentralstellePortletRunController
 * 
 * <P>
 * Initial Date:  06.06.2008 <br>
 * @author patrickb
 */
class ZentralstellePortletRunController extends BasicController {

	private VelocityContainer mainVC;
	private Link startWizardButton;
	private StepsMainRunController smrc;

	protected ZentralstellePortletRunController(UserRequest ureq, WindowControl control) {
		super(ureq, control);
		mainVC = createVelocityContainer("mainzusz");
		startWizardButton = LinkFactory.createButton("startWizard", mainVC, this);
		putInitialPanel(mainVC);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose()
	 */
	@Override
	protected void doDispose() {
	// nothing to dispose

	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	@Override
	@SuppressWarnings("unused")
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == startWizardButton){
			Step startStep = new ZsuzStepUserData(ureq);
			
			StepRunnerCallback finishCallback = new StepRunnerCallback() {
				@SuppressWarnings({"synthetic-access","unused"})
				public Step execute(UserRequest ureq2, WindowControl control, StepsRunContext runContext) {
					MailTemplate mailtemplate = (MailTemplate)runContext.get("mailtemplate");
					//fetch data from runContext and send eMail with it
					MailerWithTemplate mailer = MailerWithTemplate.getInstance();
					Identity replyto = (Identity)runContext.get("replyto");
					MailerResult mr = mailer.sendMail(ureq2.getIdentity(), null, null, mailtemplate, replyto);
					logAudit("DRUCKEREI-TEMPLATE-ERSTELLT", null);
					if(mr.getReturnCode() == MailerResult.OK){
						return StepsMainRunController.DONE_UNCHANGED;
					}else{
						return Step.NOSTEP;
					}
				}

			};
			smrc = new StepsMainRunController(ureq, getWindowControl(), startStep, finishCallback,null, translate("wizard.title"));
			listenTo(smrc);
			getWindowControl().pushAsModalDialog(smrc.getInitialComponent());
		}
	}

	/**
	 * 
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	@SuppressWarnings("unused")
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == smrc){
			//remove wizard
			getWindowControl().pop();
			removeAsListenerAndDispose(smrc);
			if (event == Event.CANCELLED_EVENT) {
				// cancelled nothing to do
			} else if (event == Event.DONE_EVENT) {
				showInfo("email.sent");
			}
		}
	}
	
}

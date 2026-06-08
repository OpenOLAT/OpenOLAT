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
package org.olat.modules.selectus.ui.feedback.appsfeedback;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.ApplicationDetailsController;
import org.olat.modules.selectus.ui.ApplicationDocumentsController;

/**
 * 
 * Initial date: 4 mai 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ApplicationMemberFeedbackDetailsController extends FormBasicController {
	
	private FormLink nextButton;
	
	private Position position;
	private Application application;
	private final boolean preview;
	private final RecruitingPositionSecurityCallback secCallback;
	
	public ApplicationMemberFeedbackDetailsController(UserRequest ureq, WindowControl wControl,
			Position position, Application application, RecruitingPositionSecurityCallback secCallback, boolean preview) {
		super(ureq, wControl, "feedback_application");
		this.preview = preview;
		this.position = position;
		this.application = application;
		this.secCallback = secCallback;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		ApplicationDetailsController detailsController = new ApplicationDetailsController(ureq, getWindowControl(),
				position, application, secCallback, mainForm);
		listenTo(detailsController);
		formLayout.add("details", detailsController.getInitialFormItem());
		
		ApplicationDocumentsController documentsController = new ApplicationDocumentsController(ureq, getWindowControl(),
				position, application, secCallback, preview, mainForm);
		listenTo(documentsController);
		if(documentsController.getNumOfDocuments() > 0) {
			formLayout.add("documents", documentsController.getInitialFormItem());
		}
		nextButton = uifactory.addFormLink("next", formLayout, Link.BUTTON);
		nextButton.setVisible(application.getKey() != null && !preview);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(nextButton == source) {
			fireEvent(ureq, Event.DONE_EVENT);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	

}

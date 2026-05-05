/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
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

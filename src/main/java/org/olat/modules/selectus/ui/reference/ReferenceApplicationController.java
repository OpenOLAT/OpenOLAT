/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.reference;

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
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.ApplicationDetailsController;
import org.olat.modules.selectus.ui.ApplicationDocumentsController;

/**
 * 
 * Initial date: 16.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReferenceApplicationController extends FormBasicController {

	private FormLink nextButton;

	private Position position;
	private final boolean preview;
	private Application application;
	private final RecruitingPositionSecurityCallback secCallback;
	
	@Autowired
	private RecruitingService recruitingService;
	
	public ReferenceApplicationController(UserRequest ureq, WindowControl wControl,
			Position position, Application application, RecruitingPositionSecurityCallback secCallback, boolean preview) {
		super(ureq, wControl, "reference_reviewer_application");
		this.preview = preview;
		this.position = position;
		this.application = application.getKey() == null && preview ? application : recruitingService.getApplicationWithAttributes(application);
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
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}

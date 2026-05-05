/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.feedback.publicfeedback;

import java.util.Date;
import java.util.List;

import org.olat.core.commons.fullWebApp.BaseFullWebappController;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.MainLayoutBasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.dispatcher.PublicFeedbackDispatcher;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.model.PublicFeedback;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Initial date: 30 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PublicFeedbackMainController extends MainLayoutBasicController implements Activateable2 {
	
	private final VelocityContainer layoutMainVC;
	
	private Position position;
	private Application application;
	
	private PublicFeedbackSubmissionController submissionCtrl;
	
	@Autowired
	private RecruitingService recruitingService;
	
	public PublicFeedbackMainController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		
		String page = velocity_root + "/public_feedback_main.html";
		layoutMainVC = new VelocityContainer("vc_main", page, getTranslator(), this);
		layoutMainVC.put("content", new Panel("empty"));
		
		wControl.getWindowBackOffice().getChiefController().addBodyCssClass("fx_r_public_feedback");
		
		// we arrived at destination
		UserSession usess = ureq.getUserSession();
		usess.removeEntryFromNonClearedStore("redirect-bc");
		
		// beautify the URL
		ChiefController cc = wControl.getWindowBackOffice().getChiefController();
		if(cc instanceof BaseFullWebappController) {
			((BaseFullWebappController)cc).setStartBusinessPath("[publicfeedback:0]");
		}

		putInitialPanel(layoutMainVC);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		String id = getFeedbackId(ureq);
		Identity identity = ureq.getIdentity();
		if(StringHelper.containsNonWhitespace(id) && identity != null) {
			application = recruitingService.getApplicationByPublicFeedbackKey(id);
			if(application != null) {
				position =  application.getPosition();
				if(isValid()) {
					init(ureq);
				} else {
					doErrorMsg(ureq);
				}
			} else {
				doErrorMsg(ureq);
			}
		} else {
			doErrorMsg(ureq);
		}
	}
	
	private boolean isValid() {
		if(application == null || position.getStatus() == null 
				|| PositionStatus.valueOf(position.getStatus()) == PositionStatus.closed) {
			return false;
		}
		Date deadline = application.getPublicFeedbackDeadline();
		return deadline == null || RecruitingHelper.endOfDay(deadline).after(new Date());
	}
	
	private String getFeedbackId(UserRequest ureq) {
		String id = (String)ureq.getUserSession().getEntry(PublicFeedbackDispatcher.PUBLIC_FEEDBACK_ID);
		if(StringHelper.containsNonWhitespace(id)) {
			return id;
		}
		
		String requestUri = ureq.getHttpReq().getRequestURI();
		String uriPrefix = ureq.getUriPrefix();
		if(uriPrefix.length() < requestUri.length()) {
			requestUri = requestUri.substring(uriPrefix.length());
		}
		
		if(requestUri.startsWith("/")) {
			requestUri = requestUri.substring(1, requestUri.length());
		}
		
		int slashFromRestUrlIndex = requestUri.indexOf('/');
		if(slashFromRestUrlIndex > 0 && slashFromRestUrlIndex + 1 < requestUri.length()) {
			requestUri = requestUri.substring(slashFromRestUrlIndex + 1, requestUri.length());
		}
		return requestUri;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == submissionCtrl) {
			if(event == Event.DONE_EVENT) {
				doFinished(ureq, submissionCtrl.getFeedback());
				ureq.getUserSession().removeEntryFromNonClearedStore(PublicFeedbackDispatcher.PUBLIC_FEEDBACK_ID);
			}
		}
	}
	
	private void init(UserRequest ureq) {
		if(application == null) {
			doErrorMsg(ureq);
		} else {
			if(position.getStatus() == null ||  PositionStatus.valueOf(position.getStatus()) == PositionStatus.closed) {
				doErrorMsg(ureq);
			} else {
				doFeedback(ureq);
			}
		}
	}
	
	private void doFeedback(UserRequest ureq) {
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance("publicfeedback", 0l), null);
		submissionCtrl = new PublicFeedbackSubmissionController(ureq, swControl, position, application);
		listenTo(submissionCtrl);
		layoutMainVC.put("content", submissionCtrl.getInitialComponent());
	}
	
	private void doFinished(UserRequest ureq, PublicFeedback feedback) {
		PublicFeedbackFinishController finishCtrl = new PublicFeedbackFinishController(ureq, getWindowControl(), feedback);
		listenTo(finishCtrl);
		layoutMainVC.put("content", finishCtrl.getInitialComponent());
	}
	
	private void doErrorMsg(UserRequest ureq) {
		PublicFeedbackWarningController warningCtrl = new PublicFeedbackWarningController(ureq, getWindowControl(), position, application);
		listenTo(warningCtrl);
		layoutMainVC.put("content", warningCtrl.getInitialComponent());
	}
}

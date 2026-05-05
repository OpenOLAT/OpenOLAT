/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.application;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.ParallelApplicationScope;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionLight;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.ui.ApplicationDetailsController;
import org.olat.modules.selectus.ui.ApplicationDocumentsController;
import org.olat.modules.selectus.ui.ApplicationNotesController;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.application.ApplicationOverviewController.PositionInfo;
import org.olat.modules.selectus.ui.events.DecisionEvent;
import org.olat.modules.selectus.ui.events.FinalDecisionChangeEvent;

/**
 * 
 * Description:<br>
 * This controller contains the details and documents of an application.<br>
 * Important: the categories controller need and has the form submit.
 * <P>
 * Initial Date:  9 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ApplicationController extends FormBasicController {
	
	private ApplicationStatusController statusController;
	private ApplicationCategoriesController categoriesController;
	
	private Position position;
	private Application application;
	private final RecruitingPositionSecurityCallback secCallback;

	@Autowired
	private AuditService auditService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService erFrontendManager;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public ApplicationController(UserRequest ureq, WindowControl wControl, Position position, Application application,
			RecruitingPositionSecurityCallback secCallback) {
		super(ureq, wControl, "application", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
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
				position, application, secCallback, false, mainForm);
		listenTo(documentsController);
		formLayout.add("documents", documentsController.getInitialFormItem());
		
		if(recruitingModule.isApplicationAssignmentsEnabled()) {
			ApplicationAssignmentsController assignementsController = new ApplicationAssignmentsController(ureq, getWindowControl(),
					position, application, secCallback, mainForm);
			listenTo(assignementsController);
			formLayout.add("assignments", assignementsController.getInitialFormItem());
		}
		
		statusController = new ApplicationStatusController(ureq, getWindowControl(), application, secCallback, mainForm);
		listenTo(statusController);
		formLayout.add("status", statusController.getInitialFormItem());
		
		// Important!
		// The categories control need the form submit
		if(recruitingModule.isCategoriesEnabledFor(position)) {
			categoriesController = new ApplicationCategoriesController(ureq, getWindowControl(), position, application, secCallback, mainForm);
			listenTo(categoriesController);
			formLayout.add("categories", categoriesController.getInitialFormItem());
		}
		
		if(secCallback.canNotes()) {
			ApplicationNotesController notesController = new ApplicationNotesController(ureq, getWindowControl(), application, mainForm);
			listenTo(notesController);
			formLayout.add("notes", notesController.getInitialFormItem());
		}
		
		if(secCallback.canViewParalellApplications() && formLayout instanceof FormLayoutContainer) {
			ParallelApplicationScope scope = recruitingModule.getParallelApplicationScope();
			List<PositionLight> parallelApps =  erFrontendManager.getParallelApplications(application, position, scope);
			if(parallelApps != null && !parallelApps.isEmpty()) {
				List<PositionInfo> infos = new ArrayList<>(parallelApps.size());
				for(PositionLight parallelApp:parallelApps) {
					String positionTitle = parallelApp.getMLTitle(getLocale());
					if(recruitingModule.isPositionPlannigIdEnabled()) {
						infos.add(new PositionInfo(parallelApp.getPlaningsNumber(), positionTitle));
					} else {
						infos.add(new PositionInfo(positionTitle));
					}
				}
				((FormLayoutContainer)formLayout).contextPut("parallelApplications", infos);
			}
		}
	}
	
	public void updateApplication(Application updatedApplication) {
		this.application = updatedApplication;
		statusController.updateApplication(updatedApplication);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(categoriesController == source) {
			if(event == Event.CHANGED_EVENT) {
				fireEvent(ureq, event);
			}
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof SingleSelection decisionEl && "edit.decision".equals(decisionEl.getName())) {
			if(decisionEl.isOneSelected()) {
				doChangeDecision(ureq, decisionEl.getSelectedKey());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doChangeDecision(UserRequest ureq, String key) {
		try {
			int decision = Integer.parseInt(key);
			Integer before = application.getDecision();
			erFrontendManager.setDecision(application, decision);
			
			String messageI18n = "audit.log.application.update.decision";
			String[] args = new String[] { salutationGenerator.getTitleFullname(application, getLocale()), application.getId().toString() };
			auditService.auditApplicationDecisionLog(Action.update, ActionTarget.decision, before, decision, messageI18n, args, getTranslator(), position, application, getIdentity());
			
			FinalDecisionChangeEvent fde = new FinalDecisionChangeEvent(application.getKey(), decision, getIdentity().getKey());
			CoordinatorManager.getInstance().getCoordinator().getEventBus().fireEventToListenersOf(fde, position);
			fireEvent(ureq, new DecisionEvent());
		} catch (NumberFormatException e) {
			logError("", e);
		}
	}
	
}

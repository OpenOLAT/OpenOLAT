/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.review;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SelectusReviewService;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.review.PositionReviewDefinition;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.events.NewPositionSavedEvent;
import org.olat.modules.selectus.ui.position.PositionEditableController;

/**
 * 
 * Initial date: 3 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReviewConfigurationController extends BasicController implements PositionEditableController {
	
	private final ReviewEditConfigurationController configurationCtrl;
	private final ReviewElementsConfigurationController elementsCtrl;
	
	private Position position;
	private PositionReviewDefinition positionReviewDefinition;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private AuditService auditService;
	@Autowired
	private SelectusReviewService reviewService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	
	public ReviewConfigurationController(UserRequest ureq, WindowControl wControl,
			Position position, PositionReviewDefinition positionReviewDefinition, boolean readOnly) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		this.positionReviewDefinition = positionReviewDefinition;
		
		VelocityContainer mainVC = createVelocityContainer("configuration");
		
		configurationCtrl = new ReviewEditConfigurationController(ureq, wControl, position, positionReviewDefinition, readOnly);
		listenTo(configurationCtrl);
		mainVC.put("configuration", configurationCtrl.getInitialComponent());
		
		elementsCtrl = new ReviewElementsConfigurationController(ureq, wControl, positionReviewDefinition, readOnly);
		listenTo(elementsCtrl);
		mainVC.put("elements", elementsCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}
	
	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public void updatePosition(Position updatedPosition) {
		this.position = updatedPosition;
		configurationCtrl.updatePosition(updatedPosition);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == configurationCtrl || source == elementsCtrl) {
			if(event == Event.DONE_EVENT) {
				commit(ureq);
			} else if(event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, event);
			}
		}
		
		super.event(ureq, source, event);
	}
	
	private void commit(UserRequest ureq) {
		Event doneEvent = Event.DONE_EVENT;
		if(position.getKey() != null) {
			position = recruitingService.getPosition(position.getKey());
		} else {
			doneEvent = new NewPositionSavedEvent();
		}

		positionReviewDefinition = reviewService.getReviewDefinition(positionReviewDefinition);
		
		String before = auditService.toAuditXml(positionReviewDefinition);
		configurationCtrl.commit(position, positionReviewDefinition);

		positionReviewDefinition = reviewService.saveReviewDefinition(positionReviewDefinition);
		elementsCtrl.updateReviewDefinition(positionReviewDefinition);
		positionReviewDefinition.getElements().size();
		position = recruitingService.savePosition(position);
		dbInstance.commit();
		getLogger().info(Tracing.M_AUDIT, "Update reviews configuration: {}", positionReviewDefinition);
		
		String after = auditService.toAuditXml(positionReviewDefinition);
		if(!before.equals(after)) {
			String messageI18n = "audit.log.position.change.configuration";
			String[] messageArgs = new String[] { position.getMLTitle(recruitingModule.getPositionDefaultLocale()) };
			auditService.auditPositionLog(Action.changeConfiguration, ActionTarget.position, before, after,
					messageI18n, messageArgs, getTranslator(), position, getIdentity());
		}
		
		fireEvent(ureq, doneEvent);
	}

}
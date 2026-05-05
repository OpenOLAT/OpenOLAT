/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.position;

import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SelectusReviewService;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.review.PositionReviewDefinition;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.comment.CommitteeCommentEditConfigurationController;
import org.olat.modules.selectus.ui.review.ReviewConfigurationController;

/**
 * 
 * 
 * Initial date: 27 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditEvaluationsConfigurationController extends BasicController implements Activateable2, PositionEditableController {

	private TabbedPane tabPane;
	
	private Position position;
	
	private ReviewConfigurationController reviewCtrl;
	private CommitteeCommentEditConfigurationController commentCtrl;
	private final PositionEditDecisionToolConfigurationController decisionToolCtrl;

	@Autowired
	private DB dbInstance;
	@Autowired
	private SelectusReviewService reviewService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService recruitingService;
	
	public PositionEditEvaluationsConfigurationController(UserRequest ureq, WindowControl wControl, Position position, boolean readOnly) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;

		decisionToolCtrl = new PositionEditDecisionToolConfigurationController(ureq, getWindowControl(), position, readOnly);
		listenTo(decisionToolCtrl);

		if(recruitingModule.isReviewEnabled() || recruitingModule.isApplicationsCommitteeCommentEnabled()) {
			tabPane = new TabbedPane("evalTabPane", getLocale());
			tabPane.addListener(this);
			tabPane.setElementCssClass("o_sel_edit_position_evaluation_tab");
			
			if(recruitingModule.isReviewEnabled()) {
				PositionReviewDefinition positionReviewDefinition = reviewService.getReviewDefinition(position.getReviewDefinition());
				if(positionReviewDefinition == null) {
					//create and persist
					if(position.getKey() != null) {
						position = recruitingService.getPosition(position.getKey());
					}
					positionReviewDefinition = reviewService.createReviewDefinition();
					positionReviewDefinition = reviewService.saveReviewDefinition(positionReviewDefinition);
					position.setReviewDefinition(positionReviewDefinition);
					this.position = recruitingService.savePosition(position);
					dbInstance.commit();
					decisionToolCtrl.updatePosition(this.position);
				}
				reviewCtrl = new ReviewConfigurationController(ureq, getWindowControl(), this.position, positionReviewDefinition, readOnly);
				tabPane.addTab(translate("edit.reviews"), reviewCtrl);
			}
			
			tabPane.addTab(translate("edit.decision"), decisionToolCtrl);
	
			if(recruitingModule.isApplicationsCommitteeCommentEnabled()) {	
				tabPane.addTab(ureq, translate("edit.committee.comment"), uureq -> {
					commentCtrl = new CommitteeCommentEditConfigurationController(uureq, getWindowControl(), this.position, readOnly);
					listenTo(commentCtrl);
					return commentCtrl.getInitialComponent();
				});
			}
			putInitialPanel(tabPane);
		} else {
			putInitialPanel(decisionToolCtrl.getInitialComponent());
		}
	}
	
	@Override
	public Position getPosition() {
		return position;
	}
	
	@Override
	public void updatePosition(Position updatedPosition) {
		this.position = updatedPosition;
		decisionToolCtrl.updatePosition(updatedPosition);
		if(reviewCtrl != null) {
			reviewCtrl.updatePosition(updatedPosition);
		}
		if(commentCtrl != null) {
			commentCtrl.updatePosition(updatedPosition);
		}
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("tab".equalsIgnoreCase(type)) {
			tabPane.activate(ureq, entries, state);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == tabPane) {
			tabPane.addToHistory(ureq, getWindowControl());
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source instanceof PositionEditableController) {
			position = ((PositionEditableController)source).getPosition();
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}
}

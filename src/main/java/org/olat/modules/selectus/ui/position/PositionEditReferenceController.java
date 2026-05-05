/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.position;

import java.util.List;

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
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.PositionController;

/**
 * 
 * Initial date: 13.09.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditReferenceController extends BasicController implements Activateable2, PositionEditableController {
	
	private final TabbedPane tabPane;
	
	private Position position;
	
	private final PositionEditExpertsController expertsCtrl;
	private final PositionEditRefereesController refereesCtrl;
	private final PositionEditComparativeAssessmentExpertsController comparativeAssessmentExpertsCtrl;
	
	@Autowired
	private RecruitingModule recruitingModule;
	
	public PositionEditReferenceController(UserRequest ureq, WindowControl wControl, Position position, boolean readOnly) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		
		tabPane = new TabbedPane("evalTabPane", getLocale());
		tabPane.setElementCssClass("o_sel_edit_position_evaluation_tab");
		tabPane.setHideDisabledTab(true);
		tabPane.addListener(this);
		
		expertsCtrl = new PositionEditExpertsController(ureq, getWindowControl(), position, readOnly);
		listenTo(expertsCtrl);

		refereesCtrl = new PositionEditRefereesController(ureq, getWindowControl(), position, readOnly);
		listenTo(refereesCtrl);
		
		comparativeAssessmentExpertsCtrl = new PositionEditComparativeAssessmentExpertsController(ureq, getWindowControl(), position, readOnly);
		listenTo(comparativeAssessmentExpertsCtrl);

		if(recruitingModule.isReferenceEnabled()) {
			tabPane.addTab(translate("edit.step.referees"), refereesCtrl);
			tabPane.addTab(translate("edit.step.experts"), expertsCtrl);
		}
		if(recruitingModule.isComparativeAssessmentExpertsEnabled()) {
			tabPane.addTab(translate("edit.step.comparative.experts"), comparativeAssessmentExpertsCtrl);
		}

		putInitialPanel(tabPane);
	}

	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public void updatePosition(Position updatedPosition) {
		this.position = updatedPosition;
		expertsCtrl.updatePosition(updatedPosition);
		refereesCtrl.updatePosition(updatedPosition);
		comparativeAssessmentExpertsCtrl.updatePosition(updatedPosition);
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
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source instanceof PositionEditableController) {
			position = ((PositionEditableController)source).getPosition();
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == tabPane) {
			tabPane.addToHistory(ureq, getWindowControl());
		}
	}
}
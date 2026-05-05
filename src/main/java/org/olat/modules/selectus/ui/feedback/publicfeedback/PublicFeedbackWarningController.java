/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.feedback.publicfeedback;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.Util;

import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Initial date: 30 mars 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PublicFeedbackWarningController extends BasicController {
	
	public PublicFeedbackWarningController(UserRequest ureq, WindowControl wControl, Position position, Application application) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));

		Component mainVC = null;
		if(position == null || application == null) {	
			mainVC = createVelocityContainer("feedback_not_found");
		} else if((application.getPublicFeedbackDeadline() != null
				&& RecruitingHelper.endOfDay(application.getPublicFeedbackDeadline()).before(new Date()))
				|| !application.isPublicFeedbackEnabled()) {
			mainVC = createVelocityContainer("feedback_deadline");
		} else if(position.getStatus() == null
				|| PositionStatus.valueOf(position.getStatus()) == PositionStatus.closed) {
			mainVC = createVelocityContainer("position_closed");
		} else {
			mainVC = new Panel("empty");
		}

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}

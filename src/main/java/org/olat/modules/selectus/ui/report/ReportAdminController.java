/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.report;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

import org.olat.modules.selectus.model.PositionApplicationAttributeTabEnum;
import org.olat.modules.selectus.ui.position.PositionEditAdditionalAttributesController;

/**
 * 
 * Initial date: 30 oct. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ReportAdminController extends BasicController {
	
	private final PositionEditAdditionalAttributesController attributesCtrl;
	
	public ReportAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		VelocityContainer mainVC = createVelocityContainer("report_admin");
		
		attributesCtrl = new PositionEditAdditionalAttributesController(ureq, wControl, PositionApplicationAttributeTabEnum.global);
		listenTo(attributesCtrl);
		
		attributesCtrl.setFormTitleTranslated(translate("reporting.admin.title"));
		attributesCtrl.setFormInfoTranslated(translate("reporting.admin.desc"));
		
		mainVC.put("attributes", attributesCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
}

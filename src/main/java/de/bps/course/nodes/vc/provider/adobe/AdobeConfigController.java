// <OLATCE-103>
/**
 * 
 * BPS Bildungsportal Sachsen GmbH<br>
 * Bahnhofstrasse 6<br>
 * 09111 Chemnitz<br>
 * Germany<br>
 * 
 * Copyright (c) 2005-2010 by BPS Bildungsportal Sachsen GmbH<br>
 * http://www.bps-system.de<br>
 * 
 * All rights reserved.
 */
package de.bps.course.nodes.vc.provider.adobe;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;

/**
 * 
 * Description:<br>
 * Config controller for Adobe Connect implementation
 * 
 * <P>
 * Initial Date:  05.01.2011 <br>
 * @author skoeber
 */
public class AdobeConfigController extends BasicController {

	private VelocityContainer editVC;
	private AdobeEditForm editForm;

	protected AdobeConfigController(UserRequest ureq, WindowControl wControl, String roomId, AdobeConnectProvider adobe, AdobeConnectConfiguration config) {
		super(ureq, wControl);
		
		this.editForm = new AdobeEditForm(ureq, wControl, adobe.isShowOptions(), config);
		listenTo(editForm);
		
		editVC = createVelocityContainer("edit");
		editVC.put("editForm", editForm.getInitialComponent());

		putInitialPanel(editVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// nothing to do
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == editForm) {
			fireEvent(ureq, event);
		}
	}

	@Override
	protected void doDispose() {
		if (editForm != null) {
			removeAsListenerAndDispose(editForm);
			editForm = null;
		}
	}

}
// </OLATCE-103>
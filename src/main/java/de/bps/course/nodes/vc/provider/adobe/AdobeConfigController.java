// <OLATCE-103>
/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * BPS Bildungsportal Sachsen GmbH, http://www.bps-system.de
 * <p>
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
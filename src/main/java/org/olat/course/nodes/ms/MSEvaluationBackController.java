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
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.ms;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.forms.EvaluationFormSession;
import org.olat.modules.forms.ui.EvaluationFormExecutionController;

/**
 * 
 * Initial date: 26 sept. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MSEvaluationBackController extends BasicController {
	
	private final Link backLink;
	
	private final EvaluationFormExecutionController editExecutionCtrl;
	
	public MSEvaluationBackController(UserRequest ureq, WindowControl wControl, EvaluationFormSession session) {
		super(ureq, wControl);
		
		VelocityContainer mainVC = createVelocityContainer("edit_back");
		
		backLink = LinkFactory.createLinkBack(mainVC, this);
		mainVC.put("back", backLink);

		editExecutionCtrl = new EvaluationFormExecutionController(ureq, getWindowControl(), null, null, session, null, null, false, true, false, false, null);
		listenTo(editExecutionCtrl);
		mainVC.put("execution", editExecutionCtrl.getInitialComponent());
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(backLink == source) {
			fireEvent(ureq, Event.BACK_EVENT);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editExecutionCtrl == source) {
			fireEvent(ureq, event);
		}
	}
}

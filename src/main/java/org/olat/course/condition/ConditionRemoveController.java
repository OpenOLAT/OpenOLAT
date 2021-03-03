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
package org.olat.course.condition;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;

/**
 * 
 * Initial date: 1 Mar 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ConditionRemoveController extends BasicController {
	
	public static final Event REMOVE = new Event("remove.conditions");

	private final Link removeLink;
	private DialogBoxController confirmRemoveCtrl;

	public ConditionRemoveController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		VelocityContainer mainVC = createVelocityContainer("remove");
		
		removeLink = LinkFactory.createButton("remove.conditions", mainVC, this);
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == removeLink) {
			doConfirmRemove(ureq);
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		 if (source == confirmRemoveCtrl) {
			if (DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				fireEvent(ureq, REMOVE);
			}
		}
		super.event(ureq, source, event);
	}

	private void doConfirmRemove(UserRequest ureq) {
		String title = translate("remove.conditions.confirm.title");
		String text = translate("remove.conditions.confirm.message");
		confirmRemoveCtrl = activateYesNoDialog(ureq, title, text, confirmRemoveCtrl);
	}

	@Override
	protected void doDispose() {
		//
	}

}

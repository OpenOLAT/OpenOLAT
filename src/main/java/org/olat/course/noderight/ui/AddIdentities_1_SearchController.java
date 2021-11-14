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
package org.olat.course.noderight.ui;

import java.util.Collections;
import java.util.List;

import org.olat.admin.user.UserSearchFlexiController;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;


/**
 * 
 * Initial date: 29 Oct 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AddIdentities_1_SearchController extends StepFormBasicController {
	
	private UserSearchFlexiController searchController;
	
	private final AddIdentitiesContext context; 

	public AddIdentities_1_SearchController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_BAREBONE, null);

		context = (AddIdentitiesContext) getFromRunContext("context");
		searchController = new UserSearchFlexiController(ureq, wControl, rootForm);
		listenTo(searchController);
		initForm(ureq);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (event instanceof SingleIdentityChosenEvent) {
			SingleIdentityChosenEvent e = (SingleIdentityChosenEvent) event;
			Identity identity = e.getChosenIdentity();
			List<Identity> identities = Collections.singletonList(identity);
			context.setIdentities(identities);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		} else if (event instanceof MultiIdentityChosenEvent) {
			MultiIdentityChosenEvent e = (MultiIdentityChosenEvent) event;
			List<Identity> identities = e.getChosenIdentities();
			context.setIdentities(identities);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		} else {
			super.event(ureq, source, event);
		}
	}

	@Override
	protected void formNext(UserRequest ureq) {
		List<Identity> identities = searchController.getSelectedIdentities();
		context.setIdentities(identities);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add("search", searchController.getInitialFormItem());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
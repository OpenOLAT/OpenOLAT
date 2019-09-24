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
package org.olat.modules.qpool.ui.wizard;

import java.util.ArrayList;
import java.util.Collection;
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
 * Initial date: 25.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ImportAuthorBySearchController extends StepFormBasicController {
	private UserSearchFlexiController searchController;

	public ImportAuthorBySearchController(UserRequest ureq, WindowControl wControl, Form rootForm, StepsRunContext runContext) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_CUSTOM, "import_search");

		searchController = new UserSearchFlexiController(ureq, wControl, rootForm);
		listenTo(searchController);
		
		initForm (ureq);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(event instanceof SingleIdentityChosenEvent) {
			SingleIdentityChosenEvent e = (SingleIdentityChosenEvent)event;
			String key = e.getChosenIdentity().getKey().toString();
			addToRunContext("keys", Collections.singletonList(key));
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		} else if(event instanceof MultiIdentityChosenEvent) {
			MultiIdentityChosenEvent e = (MultiIdentityChosenEvent)event;
			Collection<String> keys = new ArrayList<>();
			for(Identity identity: e.getChosenIdentities()) {
				keys.add(identity.getKey().toString());
			}
			addToRunContext("keys", keys);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		} else {
			super.event(ureq, source, event);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formNext(UserRequest ureq) {
		List<Identity> identities = searchController.getSelectedIdentities();
		Collection<String> keys = new ArrayList<>();
		for(Identity identity: identities) {
			keys.add(identity.getKey().toString());
		}
		addToRunContext("keys", keys);
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add("search", searchController.getInitialFormItem());
	}

	@Override
	protected void doDispose() {
		//
	}
}
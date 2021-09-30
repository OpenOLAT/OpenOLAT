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
package org.olat.course.learningpath.obligation;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.admin.user.UserSearchFlexiController;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.events.MultiIdentityChosenEvent;
import org.olat.basesecurity.events.SingleIdentityChosenEvent;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;

/**
 * 
 * Initial date: 20 Sep 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class IdentityExceptionalObligationController extends FormBasicController
		implements ExceptionalObligationController {

	private UserSearchFlexiController userSearchCtrl;
	
	private Set<Identity> selectedIdentity = new HashSet<>();
	
	public IdentityExceptionalObligationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		userSearchCtrl = new UserSearchFlexiController(ureq, getWindowControl(), mainForm, null, true, true);
		listenTo(userSearchCtrl);
		initForm(ureq);
	}

	@Override
	public List<ExceptionalObligation> getExceptionalObligations() {
		return selectedIdentity.stream()
				.map(this::createExceptionalObligation)
				.collect(Collectors.toList());
	}

	private ExceptionalObligation createExceptionalObligation(IdentityRef identityRef) {
		IdentityExceptionalObligation exceptionalObligation = new IdentityExceptionalObligation();
		exceptionalObligation.setType(IdentityExceptionalObligationHandler.TYPE);
		exceptionalObligation.setIdentityRef(identityRef);
		return exceptionalObligation;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add(userSearchCtrl.getInitialFormItem());
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == userSearchCtrl) {
			if (event instanceof SingleIdentityChosenEvent) {
				Identity identity = ((SingleIdentityChosenEvent) event).getChosenIdentity();
				selectedIdentity.add(identity);
				fireEvent(ureq, Event.DONE_EVENT);
			} else if (event instanceof MultiIdentityChosenEvent) {
				selectedIdentity.addAll(userSearchCtrl.getSelectedIdentities());
				fireEvent(ureq, Event.DONE_EVENT);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}

}

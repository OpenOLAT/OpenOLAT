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
package org.olat.group.ui.main;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;


/**
 * 
 * Initial date: 27.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DedupMembersConfirmationController extends FormBasicController {
	
	private static final String[] keys = { "coaches", "participants" };
	
	private MultipleSelectionElement typEl;
	private final int numOfDuplicates;
	
	public DedupMembersConfirmationController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "dedup");
		this.numOfDuplicates = -1;
		initForm(ureq);
	}

	public DedupMembersConfirmationController(UserRequest ureq, WindowControl wControl, int numOfDuplicates) {
		super(ureq, wControl, "dedup");
		this.numOfDuplicates = numOfDuplicates;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(numOfDuplicates > -1 && formLayout instanceof FormLayoutContainer) {
			((FormLayoutContainer)formLayout).contextPut("numOfDuplicates", Integer.toString(numOfDuplicates));
		}
		
		FormLayoutContainer optionsCont = FormLayoutContainer.createDefaultFormLayout("options", getTranslator());
		formLayout.add(optionsCont);
		formLayout.add("options", optionsCont);
		String[] values = new String[] {
				translate("dedup.members.coaches"), translate("dedup.members.particpants")
		};
		typEl = uifactory.addCheckboxesVertical("typ", "dedup.members.typ", optionsCont, keys, values, 1);
		typEl.select(keys[0], true);
		typEl.select(keys[1], true);

		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonCont);
		formLayout.add("buttons", buttonCont);
		uifactory.addFormSubmitButton("ok", buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
	}
	
	public boolean isDedupCoaches() {
		return typEl.isSelected(0);
	}
	
	public boolean isDedupParticipants() {
		return typEl.isSelected(1);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
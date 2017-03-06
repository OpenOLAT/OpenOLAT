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
package org.olat.course.assessment.ui.tool;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.assessment.ui.AssessedIdentityElementRow;

/**
 * 
 * Initial date: 3 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmUserVisibilityController extends FormBasicController {
	
	private static final String[] visibilityKeys = new String[] { "visible", "hidden" };
	
	private SingleSelection visibilityEl;
	private final List<AssessedIdentityElementRow> rows;
	
	public ConfirmUserVisibilityController(UserRequest ureq, WindowControl wControl, List<AssessedIdentityElementRow> rows) {
		super(ureq, wControl);
		this.rows = rows;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String[] visibilityValues = new String[]{
				translate("user.visibility.visible.select"), translate("user.visibility.hidden.select")
		};
		visibilityEl = uifactory.addRadiosVertical("user.visibility", "user.visibility", formLayout, visibilityKeys, visibilityValues);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("change.visibility", buttonsCont);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	public Boolean getVisibility() {
		return visibilityEl.isSelected(0);
	}
	
	public List<AssessedIdentityElementRow> getRows() {
		return rows;
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

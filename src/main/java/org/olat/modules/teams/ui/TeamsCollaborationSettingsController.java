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
package org.olat.modules.teams.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 9 juin 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TeamsCollaborationSettingsController extends FormBasicController {
	
	private static final String[] keys = new String[] { "owner", "all" };

	private FormSubmit submit;
	private SingleSelection meetingsAccessEl;
	
	private String access;

	/**
	 * Initializes this controller.
	 * 
	 * @param ureq The user request.
	 * @param wControl The window control.
	 * @param access The access text.
	 */
	public TeamsCollaborationSettingsController(UserRequest ureq, WindowControl wControl, String access) {
		super(ureq, wControl);
		this.access = access;
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("collaboration.access.title");
		formLayout.setElementCssClass("o_sel_collaboration_teams");

		String[] values = new String[] {
				translate("collaboration.access.owners.coaches"),
				translate("collaboration.access.all")
		};
		meetingsAccessEl = uifactory.addRadiosVertical("collaboration.access", "collaboration.access", formLayout, keys, values);
		
		boolean selected = false;
		if(StringHelper.containsNonWhitespace(access)) {
			for(String key:keys) {
				if(key.equals(access)) {
					meetingsAccessEl.select(key, true);
					selected = true;
				}
			}
		}
		if(!selected) {
			meetingsAccessEl.select(keys[0], true);
		}

		// Create submit button
		submit = uifactory.addFormSubmitButton("submit", formLayout);
		submit.setElementCssClass("o_sel_collaboration_teams_save");
	}
	
	public void setEnabled(boolean enabled) {
		submit.setVisible(enabled);
	}

	@Override
	protected void doDispose() {
		// Nothing to dispose.
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	/**
	 * Returns the access property value.
	 *
	 * @return the access property value
	 */
	public String getAccessPropertyValue() {
		return meetingsAccessEl.getSelectedKey();
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if (!meetingsAccessEl.isOneSelected()) {
			meetingsAccessEl.setErrorKey("", null);
			allOk &= false;
		}
		
		return allOk;
	}
}

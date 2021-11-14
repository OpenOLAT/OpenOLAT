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

package org.olat.collaboration;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * Provides a controller for entering an information text for group members
 * using FlexiForms.
 * 
 * @author twuersch
 * 
 */
public class NewsFormController extends FormBasicController {

	private FormSubmit submit;
	private SingleSelection newsAccessEl;
	private String access;

	/**
	 * Initializes this controller.
	 * 
	 * @param ureq The user request.
	 * @param wControl The window control.
	 * @param access The information text.
	 */
	public NewsFormController(UserRequest ureq, WindowControl wControl, String access) {
		super(ureq, wControl);
		this.access = access;
		initForm(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formNOK(UserRequest ureq) {
		fireEvent(ureq, Event.FAILED_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("news.content");
		formLayout.setElementCssClass("o_sel_collaboration_news");

		
		String[] keys = new String[] { "owner", "all" };
		String values[] = new String[] {
				// can use folder's translation keys
				translate("folder.access.owners"),
				translate("folder.access.all")
		};
		newsAccessEl = uifactory.addRadiosVertical("news.access", "news.access", formLayout, keys, values);

		switch (access) {
		case "all":
			newsAccessEl.select(access, true);
			break;
		case "owner":
			newsAccessEl.select(access, true);
			break;
		default:
			newsAccessEl.select("owner", true);
		}

		// Create submit button
		submit = uifactory.addFormSubmitButton("submit", formLayout);
		submit.setElementCssClass("o_sel_collaboration_news_save");
	}
	
	public void setEnabled(boolean enabled) {
		submit.setVisible(enabled);
	}


	/**
	 * Returns the access property value.
	 *
	 * @return the access property value
	 */
	public String getAccessPropertyValue() {
		return newsAccessEl.getSelectedKey();
	}
	

	/**
	 * @see org.olat.core.gui.components.form.flexible.impl.FormBasicController#validateFormLogic(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean newsOk = true;
		if (!newsAccessEl.isOneSelected()) {
			newsOk &= false;
		}
		return newsOk && super.validateFormLogic(ureq);
	}
}

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
package org.olat.course.nodes.sp;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * This form implements the security settings for the single page course node configuration
 *
 * Initial date: 18.12.2014<br>
 * @author dfurrer, dirk.furrer@frentix.com, http://www.frentix.com
 */
public class SecuritySettingsForm extends FormBasicController {

	private SelectionElement allowRelativeLinks;
	private boolean isOn;
	private boolean choachesAllowedToEdit;
	private SelectionElement allowCoachEdit;

	/**
	 * @param ureq
	 * @param wControl
	 * @param allowRelativeLinksConfig
	 *            true: page is link relative to course root folder; false: page
	 *            is relative to base directory
	 * @param choachesAllowedToEdit
	 *            true: course coaches can open page editor in course run
	 */
	public SecuritySettingsForm(UserRequest ureq, WindowControl wControl, boolean allowRelativeLinksConfig, boolean choachesAllowedToEdit) {
			super(ureq, wControl);
			this.isOn = allowRelativeLinksConfig;
			this.choachesAllowedToEdit = choachesAllowedToEdit;
			initForm (ureq);
	}

	/**
	 * @return Boolean new configuration
	 */
	public Boolean getAllowRelativeLinksConfig(){
		return Boolean.valueOf(allowRelativeLinks.isSelected(0));
	}
	
	/**
	 * @return Boolean new configuration
	 */
	public Boolean getAllowCoachEditConfig(){
		return Boolean.valueOf(allowCoachEdit.isSelected(0));
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		fireEvent (ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// no explicit submit button, DONE event fired every time the checkbox is clicked
	}


	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("fieldset.allowRelativeLinksForm");
		allowRelativeLinks = uifactory.addCheckboxesHorizontal("allowRelativeLinks", "allowRelativeLinks", formLayout, new String[] {"xx"}, new String[] {null});
		allowRelativeLinks.select("xx", isOn);
		allowRelativeLinks.addActionListener(FormEvent.ONCLICK);
		
		allowCoachEdit = uifactory.addCheckboxesHorizontal("allowCoachEdit", "allowCoachEdit", formLayout, new String[] {"xx"}, new String[] {null});
		allowCoachEdit.select("xx", choachesAllowedToEdit);
		allowCoachEdit.addActionListener(FormEvent.ONCLICK);
	}
}

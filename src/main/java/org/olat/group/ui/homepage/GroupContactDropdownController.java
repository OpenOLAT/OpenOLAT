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
package org.olat.group.ui.homepage;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

/**
 * 
 * Initial Date:  Aug 19, 2009 <br>
 * @author twuersch, www.frentix.com
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class GroupContactDropdownController extends FormBasicController {
	
	public GroupContactDropdownController(UserRequest ureq, WindowControl control) {
		super(ureq, control);
		initForm(ureq);
	}

	public SingleSelection destinationDropdown;

	@Override
	protected void doDispose() {
		// Nothing to do here.
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String[] keys = new String[] {"form.to.owners", "form.to.participants", "form.to.all"};
		String[] values = new String[] {translate("form.to.owners"), translate("form.to.participants"), translate("form.to.all")};
		destinationDropdown = uifactory.addDropdownSingleselect("form.to", "form.to", formLayout, keys, values, null);
		destinationDropdown.select("form.to.owners", true);
		destinationDropdown.addActionListener(this, FormEvent.ONCHANGE);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		// Nothing to do here.
	}
}

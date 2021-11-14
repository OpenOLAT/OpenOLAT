/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes.projectbroker;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.nodes.ProjectBrokerCourseNode;
import org.olat.modules.ModuleConfiguration;

/**
 * 
 * @author guretzki
 */

public class ModulesFormController extends FormBasicController {

	private ModuleConfiguration config;
	private MultipleSelectionElement selectionDropbox;
	private MultipleSelectionElement selectionReturnbox;
	private static final String[] keys = new String[] { "form.modules.enabled.yes" };
	private static final String[] values = new String[] { "" };
	/**
	 * Modules selection form.
	 * @param name
	 * @param config
	 */
	public ModulesFormController(UserRequest ureq, WindowControl wControl, ModuleConfiguration config) {
		super(ureq, wControl);
		this.config = config;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//create form elements
		final Boolean dropboxEnabled = (Boolean)config.get(ProjectBrokerCourseNode.CONF_DROPBOX_ENABLED);
		selectionDropbox = uifactory.addCheckboxesHorizontal("dropbox", "form.modules.dropbox", formLayout, keys, values);
		selectionDropbox.select(keys[0], dropboxEnabled);
		selectionDropbox.addActionListener(FormEvent.ONCLICK);

		Boolean returnboxEnabled = (Boolean)config.get(ProjectBrokerCourseNode.CONF_RETURNBOX_ENABLED);
		if (returnboxEnabled == null) {
			returnboxEnabled = Boolean.TRUE;
		}
		selectionReturnbox = uifactory.addCheckboxesHorizontal("returnbox", "form.modules.returnbox", formLayout, keys, values);
		selectionReturnbox.select(keys[0], returnboxEnabled);
		selectionReturnbox.addActionListener(FormEvent.ONCLICK);
	}
	
	@Override
	protected void formInnerEvent (UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof SelectionElement) {
			fireEvent (ureq, new Event(source.getName()+":"+((SelectionElement)source).isSelected(0)));
		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
		
}

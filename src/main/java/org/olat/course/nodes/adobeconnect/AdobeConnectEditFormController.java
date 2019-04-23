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
package org.olat.course.nodes.adobeconnect;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.AdobeConnectCourseNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.adobeconnect.ui.AdobeConnectRunController;

/**
 * 
 * Initial date: 1 mars 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AdobeConnectEditFormController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };
	
	private MultipleSelectionElement guestAllowedEl;
	//private MultipleSelectionElement guestAllowedStartEl;
	
	private ModuleConfiguration config;

	public AdobeConnectEditFormController(UserRequest ureq, WindowControl wControl,
			AdobeConnectCourseNode courseNode) {
		super(ureq, wControl, Util.createPackageTranslator(AdobeConnectRunController.class, ureq.getLocale()));
		config = courseNode.getModuleConfiguration();
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("pane.tab.vcconfig");
		setFormContextHelp("Communication and Collaboration#_openmeeting");
		
		String[] guestValues = new String[] { translate("vc.access.open") };
		guestAllowedEl = uifactory.addCheckboxesHorizontal("guest.allowed", formLayout, onKeys, guestValues);
		if(!config.getBooleanSafe(AdobeConnectEditController.GUEST_ACCESS_ALLOWED, false)) {
			guestAllowedEl.select(onKeys[0], true);
		}
		/*
		String[] guestStartValues = new String[] { translate("vc.access.start") };
		guestAllowedStartEl = uifactory.addCheckboxesHorizontal("moderator.start.meeting", formLayout, onKeys, guestStartValues);
		if(config.getBooleanSafe(AdobeConnectEditController.MODERATOR_START_MEETING, true)) {
			guestAllowedStartEl.select(onKeys[0], true);
		}
		*/
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);

	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		config.setBooleanEntry(AdobeConnectEditController.GUEST_ACCESS_ALLOWED, !guestAllowedEl.isSelected(0));
		//config.setBooleanEntry(AdobeConnectEditController.MODERATOR_START_MEETING, guestAllowedStartEl.isSelected(0));
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}
}

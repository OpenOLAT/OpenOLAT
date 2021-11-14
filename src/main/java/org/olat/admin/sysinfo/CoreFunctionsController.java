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
package org.olat.admin.sysinfo;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.webdav.WebDAVModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.restapi.RestModule;

/**
 * 
 * Initial date: 19.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CoreFunctionsController extends FormBasicController {
	
	/**
	 * @param ureq
	 * @param wControl
	 */
	public CoreFunctionsController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "coreinfo");
		

		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		//server informations
		FormLayoutContainer serverCont = FormLayoutContainer.createDefaultFormLayout("functions", getTranslator());
		formLayout.add(serverCont);
		formLayout.add("functions", serverCont);

		MultipleSelectionElement clusterEl
			= uifactory.addCheckboxesHorizontal("webdav", "core.webdav", serverCont, new String[]{"xx"}, new String[]{""});
		clusterEl.setEnabled(false);
		clusterEl.select("xx", CoreSpringFactory.getImpl(WebDAVModule.class).isEnabled());

		MultipleSelectionElement jsMathEl
			= uifactory.addCheckboxesHorizontal("jsmath", "core.jsMath", serverCont, new String[]{"xx"}, new String[]{""});
		jsMathEl.setEnabled(false);
		jsMathEl.select("xx", Boolean.TRUE);
		
		MultipleSelectionElement restEl
		= uifactory.addCheckboxesHorizontal("restapi", "core.restapi", serverCont, new String[]{"xx"}, new String[]{""});
		restEl.setEnabled(false);
		RestModule restModule = CoreSpringFactory.getImpl(RestModule.class);
		restEl.select("xx", restModule.isEnabled());
	
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
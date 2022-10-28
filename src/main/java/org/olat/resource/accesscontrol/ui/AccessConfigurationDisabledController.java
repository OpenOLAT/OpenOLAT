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
package org.olat.resource.accesscontrol.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryService;

/**
 * 
 * Initial date: 17 Oct 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AccessConfigurationDisabledController extends FormBasicController {

	private final String helpUrl;

	public AccessConfigurationDisabledController(UserRequest ureq, WindowControl wControl, String helpUrl) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.helpUrl = helpUrl;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("offers.title");
		setFormContextHelp(helpUrl);
		
		String confPage = velocity_root + "/configuration_disabled.html";
		FormLayoutContainer confContainer = FormLayoutContainer.createCustomFormLayout("offers", getTranslator(), confPage);
		confContainer.setRootForm(mainForm);
		formLayout.add(confContainer);
		
		FormLink addFormLink = uifactory.addFormLink("create.offer", confContainer, Link.BUTTON);
		addFormLink.setIconRightCSS("o_icon o_icon_caret");
		addFormLink.setEnabled(false);
		
		uifactory.addStaticTextElement("message", null, translate("create.offer.disabled"), confContainer);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

}

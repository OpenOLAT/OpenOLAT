/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.user;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;

/**
 * 
 * Initial date: 15 Aug 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public abstract class UserInfoController extends FormBasicController {

	private final UserInfoProfileConfig profileConfig;
	private final UserInfoProfile profile;

	public UserInfoController(UserRequest ureq, WindowControl wControl, UserInfoProfileConfig profileConfig, UserInfoProfile profile) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.profileConfig = profileConfig;
		this.profile = profile;
	}
	
	public UserInfoController(UserRequest ureq, WindowControl wControl, Form mainForm, UserInfoProfileConfig profileConfig, UserInfoProfile profile) {
		super(ureq, wControl, LAYOUT_BAREBONE, null, mainForm);
		this.profileConfig = profileConfig;
		this.profile = profile;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String infoPage = Util.getPackageVelocityRoot(UserInfoController.class) + "/user_info.html";
		FormLayoutContainer infoCont = FormLayoutContainer.createCustomFormLayout("itemsCont", getTranslator(), infoPage);
		infoCont.setRootForm(mainForm);
		formLayout.add(infoCont);
		
		UserInfoProfileController profileCtrl = new UserInfoProfileController(ureq, getWindowControl(), profileConfig, profile);
		listenTo(profileCtrl);
		infoCont.put("profile", profileCtrl.getInitialComponent());
		
		FormLayoutContainer itemsCont = createItemsContainer();
		itemsCont.setRootForm(mainForm);
		infoCont.add("items", itemsCont);
		
		initFormItems(itemsCont, listener, ureq);
	}

	/*
	 * This method can be overridden to implement super fancy layouts.
	 */
	protected FormLayoutContainer createItemsContainer() {
		String itemsPage = Util.getPackageVelocityRoot(UserInfoController.class) + "/user_info_items.html";
		return FormLayoutContainer.createCustomFormLayout("itemsCont", getTranslator(), itemsPage);
	}
	
	@SuppressWarnings("unused") 
	protected void initFormItems(FormLayoutContainer itemsCont, Controller listener, UserRequest ureq) {
		// Add your items
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// May be overridden.
	}

}

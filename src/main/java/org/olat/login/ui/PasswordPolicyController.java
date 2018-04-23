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
package org.olat.login.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.login.LoginModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 16 avr. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PasswordPolicyController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };
	
	private SingleSelection historyEl;
	private MultipleSelectionElement changeOnceEl;
	
	private TextElement maxAgeEl;
	private TextElement maxAgeAuthorEl;
	private TextElement maxAgeGroupManagerEl;
	private TextElement maxAgePoolManagerEl;
	private TextElement maxAgeUserManagerEl;
	private TextElement maxAgeLearnResourceManagerEl;
	private TextElement maxAgeAdministratorEl;
	
	@Autowired
	private LoginModule loginModule;
	
	public PasswordPolicyController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(LoginModule.class, ureq.getLocale(), getTranslator()));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		setFormTitle("password.policy.title");
		setFormDescription("max.age.description");

		String[] onValues = new String[] { translate("on") };
		changeOnceEl = uifactory.addCheckboxesHorizontal("change.once", "change.once", formLayout, onKeys, onValues);
		if(loginModule.isPasswordChangeOnce()) {
			changeOnceEl.select(onKeys[0], true);
		}
		
		String selectedVal = Integer.toString(loginModule.getPasswordHistory());
		boolean hasVal = false;
		String[] historyKeys = new String[] { "0", "1", "2", "5", "10", "15" };
		for(String historyKey:historyKeys) {
			if(selectedVal.equals(historyKey)) {
				hasVal = true;
			}
		}
		String[] historyValues = new String[] { translate("disable.history"), translate("password.after","1"), translate("password.after","2"), translate("password.after","5"), translate("password.after","10"), translate("password.after","15")};
		if(!hasVal) {
			historyKeys = append(historyKeys, selectedVal);
			historyValues = append(historyValues, selectedVal);
		}
		historyEl = uifactory.addDropdownSingleselect("password.history", "password.history", formLayout, historyKeys, historyValues, null);
		historyEl.select(selectedVal, true);

		String maxAge = toMaxAgeAsString(loginModule.getPasswordMaxAge());
		maxAgeEl = uifactory.addTextElement("max.age", "max.age", 5, maxAge, formLayout);
		maxAgeEl.setExampleKey("max.age.hint", null);
		
		String maxAgeAuthor = toMaxAgeAsString(loginModule.getPasswordMaxAgeAuthor());
		maxAgeAuthorEl = uifactory.addTextElement("max.age.author", "max.age.author", 5, maxAgeAuthor, formLayout);
		
		String maxAgeGroupManager = toMaxAgeAsString(loginModule.getPasswordMaxAgeGroupManager());
		maxAgeGroupManagerEl = uifactory.addTextElement("max.age.groupmanager", "max.age.groupmanager", 5, maxAgeGroupManager, formLayout);
		
		String maxAgePoolManager = toMaxAgeAsString(loginModule.getPasswordMaxAgePoolManager());
		maxAgePoolManagerEl = uifactory.addTextElement("max.age.poolmanager", "max.age.poolmanager", 5, maxAgePoolManager, formLayout);

		String maxAgeUserManager = toMaxAgeAsString(loginModule.getPasswordMaxAgeUserManager());
		maxAgeUserManagerEl = uifactory.addTextElement("max.age.usermanager", "max.age.usermanager", 5, maxAgeUserManager, formLayout);

		String maxAgeLearnResourceManager = toMaxAgeAsString(loginModule.getPasswordMaxAgeLearnResourceManager());
		maxAgeLearnResourceManagerEl = uifactory.addTextElement("max.age.learnresourcemanager", "max.age.learnresourcemanager", 5, maxAgeLearnResourceManager, formLayout);

		String maxAgeAdministrator = toMaxAgeAsString(loginModule.getPasswordMaxAgeAdministrator());
		maxAgeAdministratorEl = uifactory.addTextElement("max.age.administrator", "max.age.administrator", 5, maxAgeAdministrator, formLayout);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}
	
	private String[] append(String[] array, String val) {
		String[] newArray = new String[array.length + 1];
		System.arraycopy(array, 0, newArray, 0, array.length);
		newArray[array.length] = val;
		return newArray;
	}
	
	private String toMaxAgeAsString(int maxAge) {
		if(maxAge < 0) {
			return "";
		}
		if(maxAge == 0) {
			return "";
		}
		int ageInDays = maxAge / (24 * 60 * 60);
		return  Integer.toString(ageInDays);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= validateMaxAgeEl(maxAgeEl);
		allOk &= validateMaxAgeEl(maxAgeAuthorEl);
		allOk &= validateMaxAgeEl(maxAgeGroupManagerEl);
		allOk &= validateMaxAgeEl(maxAgePoolManagerEl);
		allOk &= validateMaxAgeEl(maxAgeUserManagerEl);
		allOk &= validateMaxAgeEl(maxAgeLearnResourceManagerEl);
		allOk &= validateMaxAgeEl(maxAgeAdministratorEl);
		
		historyEl.clearError();
		if(!historyEl.isOneSelected()) {
			historyEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}
	
	private boolean validateMaxAgeEl(TextElement el) {
		boolean allOk = true;
		
		el.clearError();
		if(StringHelper.containsNonWhitespace(el.getValue())
				&& !StringHelper.isLong(el.getValue())) {
			el.setErrorKey("form.error.nointeger", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		loginModule.setPasswordChangeOnce(changeOnceEl.isAtLeastSelected(1));
		
		int history = Integer.parseInt(historyEl.getSelectedKey());
		loginModule.setPasswordHistory(history);
		
		loginModule.setPasswordMaxAge(getMaxAge(maxAgeEl));
		loginModule.setPasswordMaxAgeAuthor(getMaxAge(maxAgeAuthorEl));
		loginModule.setPasswordMaxAgeGroupManager(getMaxAge(maxAgeGroupManagerEl));
		loginModule.setPasswordMaxAgePoolManager(getMaxAge(maxAgePoolManagerEl));
		loginModule.setPasswordMaxAgeUserManager(getMaxAge(maxAgeUserManagerEl));
		loginModule.setPasswordMaxAgeLearnResourceManager(getMaxAge(maxAgeLearnResourceManagerEl));
		loginModule.setPasswordMaxAgeAdministrator(getMaxAge(maxAgeAdministratorEl));
	}
	
	private int getMaxAge(TextElement el) {
		if(StringHelper.containsNonWhitespace(el.getValue())
				&& StringHelper.isLong(el.getValue())) {
			int ageInDay = Integer.parseInt(el.getValue());
			return ageInDay * 24 * 60 * 60;//convert in seconds
		}
		return 0;
	}
}

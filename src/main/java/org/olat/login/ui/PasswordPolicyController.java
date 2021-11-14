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

import static org.olat.login.ui.LoginUIFactory.validateInteger;

import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
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
	
	private MultipleSelectionElement changeOnceEl;
	
	private TextElement validUntilGuiEl;
	private TextElement validUntilRestEl;
	private TextElement maxAgeEl;
	private TextElement maxAgeAuthorEl;
	private TextElement maxAgeGroupManagerEl;
	private TextElement maxAgePoolManagerEl;
	private TextElement maxAgeUserManagerEl;
	private TextElement maxAgeRolesManagerEl;
	private TextElement maxAgeLearnResourceManagerEl;
	private TextElement maxAgeCurriculumnManagerEl;
	private TextElement maxAgeLectureManagerEl;
	private TextElement maxAgeQualityManagerEl;
	private TextElement maxAgeLineManagerEl;
	private TextElement maxAgePrincipalEl;
	private TextElement maxAgeAdministratorEl;
	private TextElement maxAgeSysAdminEl;
	
	@Autowired
	private LoginModule loginModule;
	
	public PasswordPolicyController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(LoginModule.class, ureq.getLocale(), getTranslator()));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		setFormDescription("max.age.description");
		
		validUntilGuiEl = uifactory.addTextElement("password.change.valid.until.gui", 20, loginModule.getValidUntilHoursGui().toString(), formLayout);
		validUntilGuiEl.setMandatory(true);
		validUntilRestEl = uifactory.addTextElement("password.change.valid.until.rest", 20, loginModule.getValidUntilHoursRest().toString(), formLayout);
		validUntilRestEl.setMandatory(true);

		String[] onValues = new String[] { translate("on") };
		changeOnceEl = uifactory.addCheckboxesHorizontal("change.once", "change.once", formLayout, onKeys, onValues);
		if(loginModule.isPasswordChangeOnce()) {
			changeOnceEl.select(onKeys[0], true);
		}

		String maxAge = toMaxAgeAsString(loginModule.getPasswordMaxAge());
		maxAgeEl = uifactory.addTextElement("max.age", "max.age", 5, maxAge, formLayout);
		maxAgeEl.setExampleKey("max.age.hint", null);
		
		String maxAgeAuthor = toMaxAgeAsString(loginModule.getPasswordMaxAgeFor(OrganisationRoles.author));
		maxAgeAuthorEl = uifactory.addTextElement("max.age.author", "max.age.author", 5, maxAgeAuthor, formLayout);
		
		String maxAgeGroupManager = toMaxAgeAsString(loginModule.getPasswordMaxAgeFor(OrganisationRoles.groupmanager));
		maxAgeGroupManagerEl = uifactory.addTextElement("max.age.groupmanager", "max.age.groupmanager", 5, maxAgeGroupManager, formLayout);
		
		String maxAgePoolManager = toMaxAgeAsString(loginModule.getPasswordMaxAgeFor(OrganisationRoles.poolmanager));
		maxAgePoolManagerEl = uifactory.addTextElement("max.age.poolmanager", "max.age.poolmanager", 5, maxAgePoolManager, formLayout);

		String maxAgeUserManager = toMaxAgeAsString(loginModule.getPasswordMaxAgeFor(OrganisationRoles.usermanager));
		maxAgeUserManagerEl = uifactory.addTextElement("max.age.usermanager", "max.age.usermanager", 5, maxAgeUserManager, formLayout);

		String maxAgeRolesManager = toMaxAgeAsString(loginModule.getPasswordMaxAgeFor(OrganisationRoles.rolesmanager));
		maxAgeRolesManagerEl = uifactory.addTextElement("max.age.rolesmanager", "max.age.rolesmanager", 5, maxAgeRolesManager, formLayout);

		String maxAgeLearnResourceManager = toMaxAgeAsString(loginModule.getPasswordMaxAgeFor(OrganisationRoles.learnresourcemanager));
		maxAgeLearnResourceManagerEl = uifactory.addTextElement("max.age.learnresourcemanager", "max.age.learnresourcemanager", 5, maxAgeLearnResourceManager, formLayout);
		
		String maxAgeCurriculumManager = toMaxAgeAsString(loginModule.getPasswordMaxAgeFor(OrganisationRoles.curriculummanager));
		maxAgeCurriculumnManagerEl = uifactory.addTextElement("max.age.curriculummanager", "max.age.curriculummanager", 5, maxAgeCurriculumManager, formLayout);
	
		String maxAgeLectureManager = toMaxAgeAsString(loginModule.getPasswordMaxAgeFor(OrganisationRoles.lecturemanager));
		maxAgeLectureManagerEl = uifactory.addTextElement("max.age.lecturemanager", "max.age.lecturemanager", 5, maxAgeLectureManager, formLayout);
		
		String maxAgeQualityManager = toMaxAgeAsString(loginModule.getPasswordMaxAgeFor(OrganisationRoles.qualitymanager));
		maxAgeQualityManagerEl = uifactory.addTextElement("max.age.qualitymanager", "max.age.qualitymanager", 5, maxAgeQualityManager, formLayout);
		
		String maxAgeLineManager = toMaxAgeAsString(loginModule.getPasswordMaxAgeFor(OrganisationRoles.linemanager));
		maxAgeLineManagerEl = uifactory.addTextElement("max.age.linemanager", "max.age.linemanager", 5, maxAgeLineManager, formLayout);

		String maxAgePrincipal = toMaxAgeAsString(loginModule.getPasswordMaxAgeFor(OrganisationRoles.principal));
		maxAgePrincipalEl = uifactory.addTextElement("max.age.principal", "max.age.principal", 5, maxAgePrincipal, formLayout);
		
		String maxAgeAdministrator = toMaxAgeAsString(loginModule.getPasswordMaxAgeFor(OrganisationRoles.administrator));
		maxAgeAdministratorEl = uifactory.addTextElement("max.age.administrator", "max.age.administrator", 5, maxAgeAdministrator, formLayout);
		
		String maxAgeSysAdmin = toMaxAgeAsString(loginModule.getPasswordMaxAgeFor(OrganisationRoles.sysadmin));
		maxAgeSysAdminEl = uifactory.addTextElement("max.age.sysadmin", "max.age.sysadmin", 5, maxAgeSysAdmin, formLayout);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
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
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		allOk &= validateInteger(validUntilGuiEl, 1);
		allOk &= validateInteger(validUntilRestEl, 1);
		allOk &= validateMaxAgeEl(maxAgeEl);
		allOk &= validateMaxAgeEl(maxAgeAuthorEl);
		allOk &= validateMaxAgeEl(maxAgeGroupManagerEl);
		allOk &= validateMaxAgeEl(maxAgePoolManagerEl);
		allOk &= validateMaxAgeEl(maxAgeUserManagerEl);
		allOk &= validateMaxAgeEl(maxAgeRolesManagerEl);
		allOk &= validateMaxAgeEl(maxAgeLearnResourceManagerEl);
		allOk &= validateMaxAgeEl(maxAgeCurriculumnManagerEl);
		allOk &= validateMaxAgeEl(maxAgeQualityManagerEl);
		allOk &= validateMaxAgeEl(maxAgeLectureManagerEl);
		allOk &= validateMaxAgeEl(maxAgeLineManagerEl);
		allOk &= validateMaxAgeEl(maxAgePrincipalEl);
		allOk &= validateMaxAgeEl(maxAgeAdministratorEl);
		allOk &= validateMaxAgeEl(maxAgeSysAdminEl);
		
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
		
		Integer validUntilHoursGui = Integer.parseInt(validUntilGuiEl.getValue());
		loginModule.setValidUntilHoursGui(validUntilHoursGui);
		Integer validUntilHoursRest = Integer.parseInt(validUntilRestEl.getValue());
		loginModule.setValidUntilHoursRest(validUntilHoursRest);
		
		loginModule.setPasswordMaxAge(getMaxAge(maxAgeEl));
		loginModule.setPasswordMaxAgeFor(OrganisationRoles.author, getMaxAge(maxAgeAuthorEl));
		loginModule.setPasswordMaxAgeFor(OrganisationRoles.groupmanager, getMaxAge(maxAgeGroupManagerEl));
		loginModule.setPasswordMaxAgeFor(OrganisationRoles.poolmanager, getMaxAge(maxAgePoolManagerEl));
		loginModule.setPasswordMaxAgeFor(OrganisationRoles.usermanager, getMaxAge(maxAgeUserManagerEl));
		loginModule.setPasswordMaxAgeFor(OrganisationRoles.rolesmanager, getMaxAge(maxAgeRolesManagerEl));
		loginModule.setPasswordMaxAgeFor(OrganisationRoles.learnresourcemanager, getMaxAge(maxAgeLearnResourceManagerEl));
		loginModule.setPasswordMaxAgeFor(OrganisationRoles.curriculummanager, getMaxAge(maxAgeCurriculumnManagerEl));
		loginModule.setPasswordMaxAgeFor(OrganisationRoles.lecturemanager, getMaxAge(maxAgeLectureManagerEl));
		loginModule.setPasswordMaxAgeFor(OrganisationRoles.qualitymanager, getMaxAge(maxAgeQualityManagerEl));
		loginModule.setPasswordMaxAgeFor(OrganisationRoles.linemanager, getMaxAge(maxAgeLineManagerEl));
		loginModule.setPasswordMaxAgeFor(OrganisationRoles.principal, getMaxAge(maxAgePrincipalEl));
		loginModule.setPasswordMaxAgeFor(OrganisationRoles.administrator, getMaxAge(maxAgeAdministratorEl));
		loginModule.setPasswordMaxAgeFor(OrganisationRoles.sysadmin, getMaxAge(maxAgeSysAdminEl));
	}
	
	private int getMaxAge(TextElement el) {
		if(StringHelper.containsNonWhitespace(el.getValue())
				&& StringHelper.isLong(el.getValue())) {
			int ageInDay = Integer.parseInt(el.getValue());
			return ageInDay * 24 * 60 * 60;//convert in hours
		}
		return 0;
	}
}

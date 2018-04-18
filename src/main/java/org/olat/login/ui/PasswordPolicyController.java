package org.olat.login.ui;

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
		super(ureq, wControl, LAYOUT_BAREBONE);
		setTranslator(Util.createPackageTranslator(LoginModule.class, ureq.getLocale(), getTranslator()));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		FormLayoutContainer policyCont = FormLayoutContainer.createDefaultFormLayout("passwordPolicy", getTranslator());
		policyCont.setFormTitle(translate("password.policy.title"));
		formLayout.add(policyCont);
		
		String[] onValues = new String[] { "" };
		changeOnceEl = uifactory.addCheckboxesHorizontal("change.once", "change.once", policyCont, onKeys, onValues);
		if(loginModule.isPasswordChangeOnce()) {
			changeOnceEl.select(onKeys[0], true);
		}

		FormLayoutContainer ageCont = FormLayoutContainer.createDefaultFormLayout("passwordAges", getTranslator());
		ageCont.setFormTitle(translate("max.age.title"));
		ageCont.setFormDescription(translate("max.age.description"));
		formLayout.add(ageCont);

		String maxAge = toMaxAgeAsString(loginModule.getPasswordMaxAge());
		maxAgeEl = uifactory.addTextElement("max.age", "max.age", 5, maxAge, ageCont);
		maxAgeEl.setExampleKey("max.age.hint", null);
		
		String maxAgeAuthor = toMaxAgeAsString(loginModule.getPasswordMaxAgeAuthor());
		maxAgeAuthorEl = uifactory.addTextElement("max.age.author", "max.age.author", 5, maxAgeAuthor, ageCont);
		maxAgeAuthorEl.setExampleKey("max.age.hint", null);
		
		String maxAgeGroupManager = toMaxAgeAsString(loginModule.getPasswordMaxAgeGroupManager());
		maxAgeGroupManagerEl = uifactory.addTextElement("max.age.groupmanager", "max.age.groupmanager", 5, maxAgeGroupManager, ageCont);
		maxAgeGroupManagerEl.setExampleKey("max.age.hint", null);
		
		String maxAgePoolManager = toMaxAgeAsString(loginModule.getPasswordMaxAgePoolManager());
		maxAgePoolManagerEl = uifactory.addTextElement("max.age.poolmanager", "max.age.poolmanager", 5, maxAgePoolManager, ageCont);
		maxAgePoolManagerEl.setExampleKey("max.age.hint", null);

		String maxAgeUserManager = toMaxAgeAsString(loginModule.getPasswordMaxAgeUserManager());
		maxAgeUserManagerEl = uifactory.addTextElement("max.age.usermanager", "max.age.usermanager", 5, maxAgeUserManager, ageCont);
		maxAgeUserManagerEl.setExampleKey("max.age.hint", null);

		String maxAgeLearnResourceManager = toMaxAgeAsString(loginModule.getPasswordMaxAgeLearnResourceManager());
		maxAgeLearnResourceManagerEl = uifactory.addTextElement("max.age.learnresourcemanager", "max.age.learnresourcemanager", 5, maxAgeLearnResourceManager, ageCont);
		maxAgeLearnResourceManagerEl.setExampleKey("max.age.hint", null);

		String maxAgeAdministrator = toMaxAgeAsString(loginModule.getPasswordMaxAgeAdministrator());
		maxAgeAdministratorEl = uifactory.addTextElement("max.age.administrator", "max.age.administrator", 5, maxAgeAdministrator, ageCont);
		maxAgeAdministratorEl.setExampleKey("max.age.hint", null);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		ageCont.add(buttonsCont);
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
		return allOk;
	}
	
	private boolean validateMaxAgeEl(TextElement el) {
		boolean allOk = true;
		
		el.clearError();
		if(StringHelper.containsNonWhitespace(el.getValue())
				&& !StringHelper.isLong(el.getValue())) {
			el.setErrorKey("", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		loginModule.setPasswordChangeOnce(changeOnceEl.isAtLeastSelected(1));
		
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

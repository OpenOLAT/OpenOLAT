package org.olat.group.ui;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.group.BusinessGroupModule;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class BusinessGroupModuleAdminController extends FormBasicController {
	
	private MultipleSelectionElement allowEl;
	
	private final BusinessGroupModule module;
	private String[] onKeys = new String[]{"user","author"};
	
	public BusinessGroupModuleAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		module = CoreSpringFactory.getImpl(BusinessGroupModule.class);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("module.admin.title");
		setFormDescription("module.admin.desc");
		
		String[] values = new String[]{
				translate("user.allow.create"),
				translate("author.allow.create")
		};
		allowEl = uifactory.addCheckboxesVertical("module.admin.allow.create", formLayout, onKeys, values, null, 1);
		allowEl.select("user", module.isUserAllowedCreate());
		allowEl.select("author", module.isAuthorAllowedCreate());
		
		FormLayoutContainer buttonsContainer = FormLayoutContainer.createButtonLayout("module.buttons", getTranslator());
		buttonsContainer.setRootForm(mainForm);
		formLayout.add(buttonsContainer);
		uifactory.addFormSubmitButton("ok", "ok", formLayout);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		module.setUserAllowedCreate(allowEl.isSelected(0));
		module.setAuthorAllowedCreate(allowEl.isSelected(1));
	}
}

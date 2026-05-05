/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.category;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;

import org.olat.modules.selectus.ui.category.PositionCategoryController.CategoriesType;

/**
 * 
 * Initial date: 29 nov. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmDisableCategoriesController extends FormBasicController {
	
	private final CategoriesType type;
	
	public ConfirmDisableCategoriesController(UserRequest ureq, WindowControl wControl, CategoriesType type) {
		super(ureq, wControl, "confirm_disable");
		this.type = type;
		initForm(ureq);
	}
	
	public CategoriesType getType() {
		return type;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String msg = (type == CategoriesType.position) ? translate("confirm.disable.system.tags") : translate("confirm.disable.position.tags");
		formLayout.contextPut("msg", msg);
		uifactory.addFormSubmitButton("disable.categories", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}	
}

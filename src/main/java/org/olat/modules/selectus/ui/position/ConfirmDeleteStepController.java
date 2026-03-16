/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.position;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;

import org.olat.modules.selectus.ui.position.model.EditStepRow;

/**
 * 
 * Initial date: 14 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmDeleteStepController extends FormBasicController {
	
	private final EditStepRow row;
	
	public ConfirmDeleteStepController(UserRequest ureq, WindowControl wControl, EditStepRow row) {
		super(ureq, wControl, "confirm_delete_step");
		this.row = row;
		initForm(ureq);
	}
	
	public EditStepRow getRow() {
		return row;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			String message = translate("confirm.delete.step.text", StringHelper.escapeHtml(row.getCustomName(getLocale())));
			layoutCont.contextPut("message", message);
		}
		uifactory.addFormSubmitButton("delete", "delete", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}
}

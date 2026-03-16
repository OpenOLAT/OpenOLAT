/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.reference;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;

import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.ui.PositionController;

/**
 * 
 * Initial date: 25 mai 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmDeclineConsentController extends FormBasicController {
	
	private TextElement commentEl;
	
	private final Reference reference;
	
	public ConfirmDeclineConsentController(UserRequest ureq, WindowControl wControl, Reference reference) {
		super(ureq, wControl, "confirm_decline_consent", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.reference = reference;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String message = "";
		if(reference.getReferenceType() == ReferenceType.expert) {
			message = translate("reference.confirm.decline.expert.text");
		} else if(reference.getReferenceType() == ReferenceType.recommendation) {
			message = translate("reference.confirm.decline.recommendation.text");
		}
		formLayout.contextPut("msg", message);
		
		commentEl = uifactory.addTextAreaElement("reference.decline.comment", "reference.decline.comment", 32000, 6, 60, false, false, false, null, formLayout);
		
		uifactory.addFormSubmitButton("reference.decline.button.short", formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}
	
	public String getComment() {
		return commentEl.getValue();
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

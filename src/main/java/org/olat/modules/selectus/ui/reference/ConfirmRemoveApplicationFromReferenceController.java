/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.reference;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.ui.PositionController;

/**
 * 
 * Initial date: 7 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmRemoveApplicationFromReferenceController extends FormBasicController {
	
	private Reference reference;
	private ReferenceToApplicationRow referenceToApplication;

	@Autowired
	private DB dbInstance;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public ConfirmRemoveApplicationFromReferenceController(UserRequest ureq, WindowControl wControl,
			Reference reference, ReferenceToApplicationRow referenceToApplication) {
		super(ureq, wControl, "confirm_remove_app_from_ref", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.referenceToApplication = referenceToApplication;
		this.reference = reference;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String buttonI18nKey = "remove";
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			String arg = salutationGenerator.getFullname(referenceToApplication.getApplication(), getLocale());
			layoutCont.contextPut("message", translate("reference.management.confirm.remove.application.from.reference.alt.text", arg));
		}

		uifactory.addFormSubmitButton("remove", buttonI18nKey, formLayout);
		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		referenceToApplication.setDeleted(true);
		if(referenceToApplication.getReferenceToApplication() != null) {
			recruitingService.deleteReferenceToApplications(reference, referenceToApplication.getApplication());
			dbInstance.commit();
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
}

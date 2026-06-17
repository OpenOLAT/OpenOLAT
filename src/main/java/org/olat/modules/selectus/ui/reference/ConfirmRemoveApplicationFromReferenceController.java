/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
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

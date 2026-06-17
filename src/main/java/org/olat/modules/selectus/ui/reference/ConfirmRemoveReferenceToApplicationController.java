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
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.ui.PositionController;

/**
 * 
 * Initial date: 7 mars 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfirmRemoveReferenceToApplicationController extends FormBasicController {
	
	private Reference reference;
	private Application application;
	private final boolean lastApplication;
	private final int applicationsLeft;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RecruitingService recruitingService;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;
	
	public ConfirmRemoveReferenceToApplicationController(UserRequest ureq, WindowControl wControl,
			Reference reference, Application application, int applicationsLeft) {
		super(ureq, wControl, "confirm_remove_ref_to_app", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.application = application;
		this.reference = reference;
		lastApplication = applicationsLeft == 0;
		this.applicationsLeft = applicationsLeft;
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String buttonI18nKey = "remove";
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			
			String i18nKey;
			if(lastApplication) {
				i18nKey = "reference.management.confirm.delete.comparative.expert.text";
				buttonI18nKey = "delete";
			} else {
				if(applicationsLeft == 1) {
					i18nKey = "reference.management.confirm.remove.application.from.reference.text.singular";
				} else {
					i18nKey = "reference.management.confirm.remove.application.from.reference.text.plural";
				}
			}
			
			String[] args = new String[] {
				salutationGenerator.getFullname(reference, getLocale()),
				Long.toString(applicationsLeft)
			};
			layoutCont.contextPut("message", translate(i18nKey, args));
			layoutCont.contextPut("messageCss", lastApplication ? "o_error" : "o_warning");
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
		if(lastApplication) {
			recruitingService.deleteReference(reference);
		} else {
			recruitingService.deleteReferenceToApplications(reference, application);
		}
		dbInstance.commit();
		
		if(lastApplication) {
			fireEvent(ureq, Event.CLOSE_EVENT);
		} else {
			fireEvent(ureq, Event.DONE_EVENT);
		}
	}
}

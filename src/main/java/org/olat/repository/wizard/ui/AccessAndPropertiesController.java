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
package org.olat.repository.wizard.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Organisation;
import org.olat.core.util.Util;
import org.olat.course.editor.ChooseNodeController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryAllowToLeaveOptions;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.author.AuthoringEditAccessAndBookingController;
import org.olat.repository.ui.author.AuthoringEditAuthorAccessController;
import org.olat.repository.wizard.AccessAndProperties;
import org.olat.resource.accesscontrol.ui.AccessConfigurationController;
/**
 * @author fkiefer
 */
public class AccessAndPropertiesController extends StepFormBasicController {

	public static final String RUN_CONTEXT_KEY = "accessAndProperties";

	private RepositoryEntry entry;

	private final AuthoringEditAuthorAccessController authorAccessCtrl;
	private final AuthoringEditAccessAndBookingController accessAndBookingCtrl;
	
	public AccessAndPropertiesController(UserRequest ureq, WindowControl control, Form rootForm, StepsRunContext runContext) {
		super(ureq, control, rootForm, runContext, LAYOUT_BAREBONE, null);
		Translator translator = Util.createPackageTranslator(
				Util.createPackageTranslator(RepositoryService.class, AuthoringEditAccessAndBookingController.class, getLocale()),
				Util.createPackageTranslator(ChooseNodeController.class, AccessConfigurationController.class, getLocale()), getLocale());
		setTranslator(translator);
		entry = (RepositoryEntry) getFromRunContext("repoEntry");
		
		accessAndBookingCtrl = new AuthoringEditAccessAndBookingController(ureq, getWindowControl(), entry, rootForm);
		listenTo(accessAndBookingCtrl);
		
		authorAccessCtrl = new AuthoringEditAuthorAccessController(ureq, getWindowControl(), entry, rootForm);
		listenTo(authorAccessCtrl);

		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {			
		formLayout.add(accessAndBookingCtrl.getInitialFormItem());
		formLayout.add(authorAccessCtrl.getInitialFormItem());
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= accessAndBookingCtrl.validateFormLogic(ureq);
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean bookable = accessAndBookingCtrl.isBookable();
		boolean accessGuests = accessAndBookingCtrl.isGuests();
		boolean accessAllUsers = accessAndBookingCtrl.isAllUsers();
		List<Organisation> organisations = accessAndBookingCtrl.getSelectedOrganisations();
		RepositoryEntryAllowToLeaveOptions setting = accessAndBookingCtrl.getSelectedLeaveSetting();
		RepositoryEntryStatusEnum accessStatus = accessAndBookingCtrl.getEntryStatus();
		
		AccessAndProperties accessProperties = new AccessAndProperties(entry,
				setting, accessStatus, bookable, accessAllUsers, accessGuests, organisations);
		
		accessProperties.setCanCopy(authorAccessCtrl.canCopy());
		accessProperties.setCanDownload(authorAccessCtrl.canDownload());
		accessProperties.setCanReference(authorAccessCtrl.canReference());
		accessProperties.setOfferAccess(accessAndBookingCtrl.getOfferAccess());
		accessProperties.setDeletedOffer(accessAndBookingCtrl.getDeletedOffers());
		accessProperties.setConfirmationEmail(accessAndBookingCtrl.isSendConfirmationEmail());
					
		addToRunContext(RUN_CONTEXT_KEY, accessProperties);	
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
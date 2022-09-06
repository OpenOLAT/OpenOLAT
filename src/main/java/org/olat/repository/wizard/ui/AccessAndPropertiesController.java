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

import java.util.Collection;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Organisation;
import org.olat.core.util.Util;
import org.olat.course.editor.ChooseNodeController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.repository.ui.author.AuthoringEditAccessShareController;
import org.olat.repository.ui.author.AuthoringEditAccessShareController.PublicVisibleEvent;
import org.olat.repository.ui.author.AuthoringEditAccessShareController.StatusEvent;
import org.olat.repository.wizard.AccessAndProperties;
import org.olat.resource.accesscontrol.CatalogInfo;
import org.olat.resource.accesscontrol.ui.AccessConfigurationController;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * @author fkiefer
 */
public class AccessAndPropertiesController extends StepFormBasicController {

	public static final String RUN_CONTEXT_KEY = "accessAndProperties";

	private RepositoryEntry entry;

	private final AuthoringEditAccessShareController accessShareCtrl;
	private AccessConfigurationController accessOffersCtrl;
	
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryHandlerFactory handlerFactory;
	
	public AccessAndPropertiesController(UserRequest ureq, WindowControl control, Form rootForm, StepsRunContext runContext) {
		super(ureq, control, rootForm, runContext, LAYOUT_BAREBONE, null);
		Translator translator = Util.createPackageTranslator(
				Util.createPackageTranslator(RepositoryService.class, AuthoringEditAccessShareController.class, getLocale()),
				Util.createPackageTranslator(ChooseNodeController.class, AccessConfigurationController.class, getLocale()), getLocale());
		setTranslator(translator);
		entry = (RepositoryEntry) getFromRunContext("repoEntry");
		
		accessShareCtrl = new AuthoringEditAccessShareController(ureq, getWindowControl(), entry, rootForm);
		listenTo(accessShareCtrl);
		
		boolean guestSupported = handlerFactory.getRepositoryHandler(entry).supportsGuest(entry);
		Collection<Organisation> defaultOfferOrganisations = repositoryService.getOrganisations(entry);
		CatalogInfo catalogInfo = new CatalogInfo(true, false, null, null, null, null);
		boolean managedBookings = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.bookings);
		accessOffersCtrl = new AccessConfigurationController(ureq, getWindowControl(), rootForm,
				entry.getOlatResource(), entry.getDisplayname(), true, true, guestSupported, true,
				defaultOfferOrganisations, catalogInfo, false, managedBookings, "manual_user/course_create/Access_configuration#offer");
		accessOffersCtrl.setReStatus(RepositoryEntryStatusEnum.preparation);
		listenTo(accessOffersCtrl);
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.add(accessShareCtrl.getInitialFormItem());
		formLayout.add(accessOffersCtrl.getInitialFormItem());
		accessOffersCtrl.getInitialFormItem().setVisible(entry.isPublicVisible());
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == accessShareCtrl) {
			if (event instanceof PublicVisibleEvent) {
				PublicVisibleEvent pvw = (PublicVisibleEvent)event;
				accessOffersCtrl.getInitialFormItem().setVisible(pvw.isPublicVisible());
				if (pvw.isPublicVisible()) {
					accessOffersCtrl.setDefaultOfferOrganisations(accessShareCtrl.getSelectedOrganisations());
				}
			} else if (event instanceof StatusEvent) {
				StatusEvent se = (StatusEvent)event;
				accessOffersCtrl.setReStatus(se.getStatus());
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		allOk &= accessShareCtrl.validateFormLogic(ureq);
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		AccessAndProperties accessProperties = new AccessAndProperties();
		accessProperties.setRepositoryEntry(entry);
		accessProperties.setStatus(accessShareCtrl.getEntryStatus());
		accessProperties.setPublicVisible(accessShareCtrl.isPublicVisible());
		accessProperties.setSetting(accessShareCtrl.getSelectedLeaveSetting());
		accessProperties.setCanCopy(accessShareCtrl.canCopy());
		accessProperties.setCanDownload(accessShareCtrl.canDownload());
		accessProperties.setCanReference(accessShareCtrl.canReference());
		accessProperties.setOrganisations(accessShareCtrl.getSelectedOrganisations());
		if (accessProperties.isPublicVisible()) {
			accessProperties.setOfferAccess(accessOffersCtrl.getOfferAccess());
			accessProperties.setOpenAccess(accessOffersCtrl.getOpenAccessOffers());
			accessProperties.setGuestOffer(accessOffersCtrl.getGuestOffer());
		}
		
		addToRunContext(RUN_CONTEXT_KEY, accessProperties);	
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
}
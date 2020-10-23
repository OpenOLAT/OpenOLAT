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
package org.olat.modules.contacttracing.ui;

import java.util.List;
import java.util.function.Predicate;

import org.olat.NewControllerFactory;
import org.olat.admin.landingpages.model.Rules;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.WindowManager;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.helpers.Settings;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.HistoryPoint;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.prefs.Preferences;
import org.olat.modules.contacttracing.ContactTracingLocation;
import org.olat.modules.contacttracing.ContactTracingModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 22.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingRegistrationInternalWrapperController extends BasicController {

	public static final Event REGISTER_WITH_ACCOUNT_EVENT = new Event("register_with_account");
	public static final Event REGISTER_ANONYMOUS_EVENT = new Event("register_anonymous");

	private final ContactTracingLocation location;
	private final VelocityContainer mainVC;
	private final boolean skipSelection;

	private ContactTracingRegistrationFormController formController;
	private ContactTracingRegistrationFormController anonymousFormController;
	private ContactTracingRegistrationConfirmationController confirmationController;
	private ContactTracingRegistrationSelectionController selectionController;

	@Autowired
	private ContactTracingModule contactTracingModule;

	public ContactTracingRegistrationInternalWrapperController(UserRequest ureq, WindowControl wControl, ContactTracingLocation location, boolean skipSelection) {
		super(ureq, wControl);

		this.location = location;
		this.mainVC = createVelocityContainer("contact_tracing_registration_wrapper");
		this.skipSelection = skipSelection;

		UserSession userSession = ureq.getUserSession();

		// Open form directly if
		// 	-> Coming from log in screen
		//	-> Guest registration is deactivated
		//	-> Guest registration for logged in users is deactivated
		if (skipSelection || !location.isAccessibleByGuests() || (userSession.isAuthenticated() && !contactTracingModule.isAnonymousRegistrationForRegisteredUsersAllowed())) {
			openForm(ureq);
		} else {
			openSelection(ureq);
		}

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// Nothing to do here
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == selectionController) {
			if (event == ContactTracingRegistrationExternalWrapperController.REGISTER_ANONYMOUS_EVENT) {
				openAnonymousForm(ureq);
			} else if (event == ContactTracingRegistrationExternalWrapperController.REGISTER_WITH_ACCOUNT_EVENT) {
				openForm(ureq);
			} else if (event == Event.CANCELLED_EVENT) {
				doRedirect(ureq);
			}
		} else if (source == formController) {
			if (event == Event.DONE_EVENT) {
				openConfirmation(ureq);
			} else if (event == Event.CANCELLED_EVENT) {
				formController.doDispose();
				doRedirect(ureq);
			}
		} else if (source == confirmationController) {
			if (event == Event.CLOSE_EVENT) {
				doRedirect(ureq);
			}
		} else if (source == anonymousFormController) {
			if (event == Event.DONE_EVENT) {
				openConfirmation(ureq);
			} else if (event == Event.CANCELLED_EVENT) {
				doRedirect(ureq);
			}
		}
	}
	
	private void doRedirect(UserRequest ureq) {
		String businessPath = getLandingPage(ureq);
		String redirectURL = new StringBuilder()
				.append(Settings.getServerContextPathURI())
				.append("/auth/")
				.append(businessPath)
				.toString();
		MediaResource redirect = new RedirectMediaResource(redirectURL);
		ureq.getDispatchResult().setResultingMediaResource(redirect);
	}
	
	private String getLandingPage(UserRequest ureq) {
		Predicate<HistoryPoint> filter =  point -> {
			List<ContextEntry> entries = point.getEntries();
			if(entries == null || entries.isEmpty()) {
				return false;
			}
			String resType = entries.get(0).getOLATResourceable().getResourceableTypeName();
			return NewControllerFactory.getInstance().canResume(resType) && !"ContactTracing".equals(resType);
		};
		HistoryPoint point = ureq.getUserSession().getLastHistoryPoint(filter);
		
		String businessPath = null;
		if(point != null && StringHelper.containsNonWhitespace(point.getBusinessPath())) {
			String path = point.getBusinessPath();
			List<ContextEntry> ceList = BusinessControlFactory.getInstance().createCEListFromString(path);
			businessPath = BusinessControlFactory.getInstance().getBusinessPathAsURIFromCEList(ceList);
		}
		
		if(!StringHelper.containsNonWhitespace(businessPath)) {
			Preferences prefs =  ureq.getUserSession().getGuiPreferences();
			String landingPage = (String)prefs.get(WindowManager.class, "landing-page");
			if(StringHelper.containsNonWhitespace(landingPage)) {
				businessPath = Rules.cleanUpLandingPath(landingPage);
			}
		}
		
		if(!StringHelper.containsNonWhitespace(businessPath)) {
			businessPath = "RepositoryEntry/0";
		}
		return businessPath;
	}

	private void openSelection(UserRequest ureq) {
		if (selectionController == null) {
			selectionController = new ContactTracingRegistrationSelectionController(ureq, getWindowControl(), location);
			listenTo(selectionController);
		}

		mainVC.put("selection", selectionController.getInitialComponent());
		mainVC.setDirty(true);
	}

	private void openForm(UserRequest ureq) {
		if (formController == null) {
			formController = new ContactTracingRegistrationFormController(ureq, getWindowControl(), location);
			listenTo(formController);
		}

		mainVC.put("form", formController.getInitialComponent());
		mainVC.setDirty(true);
	}

	private void openAnonymousForm(UserRequest ureq) {
		if (anonymousFormController == null) {
			anonymousFormController = new ContactTracingRegistrationFormController(ureq, getWindowControl(), location, true);
			listenTo(anonymousFormController);
		}

		mainVC.put("form", anonymousFormController.getInitialComponent());
		mainVC.setDirty(true);
	}

	private void openConfirmation(UserRequest ureq) {
		if (confirmationController == null) {
			confirmationController = new ContactTracingRegistrationConfirmationController(ureq, getWindowControl());
			listenTo(confirmationController);
		}

		mainVC.put("confirmation", confirmationController.getInitialComponent());
		mainVC.setDirty(true);
	}

	@Override
	protected void doDispose() {
		// Nothing to dispose
	}
}

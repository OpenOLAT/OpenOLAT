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

import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.RedirectMediaResource;
import org.olat.core.helpers.Settings;
import org.olat.modules.contacttracing.ContactTracingLocation;
import org.olat.modules.contacttracing.ContactTracingRegistration;
import org.olat.modules.contacttracing.manager.ContactTracingManagerImpl;

/**
 * Initial date: 22.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingRegistrationExternalWrapperController extends BasicController {

	public static final Event REGISTER_WITH_ACCOUNT_EVENT = new Event("register_with_account");
	public static final Event REGISTER_ANONYMOUS_EVENT = new Event("register_anonymous");

	private final ContactTracingLocation location;
	private final VelocityContainer mainVC;

	private ContactTracingRegistrationSelectionController selectionController;
	private ContactTracingRegistrationFormController formController;
	private ContactTracingRegistrationConfirmationController confirmationController;

	private ContactTracingRegistration registration;

	public ContactTracingRegistrationExternalWrapperController(UserRequest ureq, WindowControl wControl, ContactTracingLocation location) {
		super(ureq, wControl);

		this.location = location;
		this.mainVC = createVelocityContainer("contact_tracing_registration_wrapper");

		openSelection(ureq);

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		// Nothing to do here
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == selectionController) {
			if (event == REGISTER_WITH_ACCOUNT_EVENT) {
				String redirectURL = new StringBuilder()
						.append(Settings.getServerContextPathURI())
						.append("/auth/")
						.append(ContactTracingManagerImpl.CONTACT_TRACING_CONTEXT_KEY)
						.append("/")
						.append(location.getKey())
						.append("/")
						.append(ContactTracingManagerImpl.CONTACT_TRACING_SELECTION_KEY)
						.append("/")
						.append("1")
						.toString();

				MediaResource redirect = new RedirectMediaResource(redirectURL);
				ureq.getDispatchResult().setResultingMediaResource(redirect);
			} else if (event == REGISTER_ANONYMOUS_EVENT) {
				openForm(ureq);
			} else if (event == Event.CANCELLED_EVENT) {
				doRedirectDefault(ureq);
			}
		} else if (source == formController) {
			if (event == Event.DONE_EVENT) {
				registration = formController.getRegistration();
				openConfirmation(ureq);
			} else if (event == Event.CANCELLED_EVENT) {
				doRedirectDefault(ureq);
			}
		} else if (source == confirmationController) {
			if (event == Event.CLOSE_EVENT) {
				doRedirectDefault(ureq);
			}
		}
	}
	
	private void doRedirectDefault(UserRequest ureq) {
		String redirectURL = new StringBuilder()
				.append(Settings.getServerContextPathURI())
				.append(DispatcherModule.PATH_AUTHENTICATED)
				.toString();
		MediaResource redirect = new RedirectMediaResource(redirectURL);
		ureq.getDispatchResult().setResultingMediaResource(redirect);
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

	private void openConfirmation(UserRequest ureq) {
		if (confirmationController == null) {
			confirmationController = new ContactTracingRegistrationConfirmationController(ureq, getWindowControl(), location, registration);
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

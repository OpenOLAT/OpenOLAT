/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */
package org.olat.group.ui.main;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.login.SupportsAfterLoginInterceptor;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.model.ResourceReservation;

/**
 * 
 * <h3>Description:</h3>
 * <p>
 * Resume to the last business path. The controller use the preferences
 * of the user to resume automatically, ask to resume with a popup window
 * or ignore the feature completely.
 * <p>
 * Initial Date:  12 jan. 2011 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class PendingEnrollmentController extends FormBasicController implements SupportsAfterLoginInterceptor {

	private final ACService acService;
	private final List<ResourceReservation> reservations;
	private final List<ResourceReservation> acceptedReservations = new ArrayList<ResourceReservation>();
	private final List<ResourceReservation> rejectedReservations = new ArrayList<ResourceReservation>();
	
	
	public PendingEnrollmentController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "accept_reservations");
		
		acService = CoreSpringFactory.getImpl(ACService.class);
		reservations = acService.getReservations(getIdentity());
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("reservations", reservations);
			layoutCont.contextPut("acceptedReservations", acceptedReservations);
			layoutCont.contextPut("acceptedReservations", acceptedReservations);
		}
		
		for(ResourceReservation reservation:reservations) {
			FormLink acceptLink = uifactory.addFormLink("accept_" + reservation.getKey(), "accept", null, formLayout, Link.BUTTON);
			acceptLink.setUserObject(reservation);
			FormLink rejectLink = uifactory.addFormLink("reject_" + reservation.getKey(), "reject", null, formLayout, Link.BUTTON);
			acceptLink.setUserObject(reservation);
			formLayout.add(acceptLink.getName(), acceptLink);
			formLayout.add(rejectLink.getName(), rejectLink);
		}

		// Button layout
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("submit", "ok", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	@Override
	public boolean isInterceptionRequired(UserRequest ureq) {
		return !reservations.isEmpty();
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink && ((FormLink)source).getUserObject() instanceof ResourceReservation) {
			ResourceReservation reservation = (ResourceReservation)((FormLink)source).getUserObject();
			if(source.getName().startsWith("accept_")) {
				acceptedReservations.add(reservation);
				rejectedReservations.remove(reservation);
			} else if (source.getName().startsWith("reject_")) {
				acceptedReservations.remove(reservation);
				rejectedReservations.add(reservation);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		for(ResourceReservation reservation:reservations) {
			if(acceptedReservations.contains(reservation)) {
				acService.acceptReservationToResource(getIdentity(), reservation);
			} else if(rejectedReservations.contains(reservation)) {
				acService.removeReservation(reservation);
			}
		}
		fireEvent (ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent (ureq, Event.CANCELLED_EVENT);
	}
}

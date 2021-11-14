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

import java.util.Date;

import org.olat.commons.calendar.CalendarUtils;
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
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.contacttracing.ContactTracingLocation;
import org.olat.modules.contacttracing.ContactTracingRegistration;
import org.olat.modules.immunityproof.ImmunityProof;
import org.olat.modules.immunityproof.ImmunityProofModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 21.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingRegistrationConfirmationController extends FormBasicController {

    private final ContactTracingLocation location;
    private final ContactTracingRegistration registration;

    private FormLink closeLink;
    
    @Autowired
    private ImmunityProofModule immunityProofModule;

    public ContactTracingRegistrationConfirmationController(UserRequest ureq, WindowControl wControl, ContactTracingLocation location, ContactTracingRegistration registration) {
        super(ureq, wControl, "contact_tracing_registration_confirmation");

        setTranslator(Util.createPackageTranslator(ImmunityProof.class, getLocale(), getTranslator()));
        
        this.location = location;
        this.registration = registration;

        initForm(ureq);
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        closeLink = uifactory.addFormLink("closeButton",  "contact.tracing.registration.close", null ,formLayout, Link.BUTTON);

        initContext((FormLayoutContainer) formLayout);
    }

    private void initContext(FormLayoutContainer container) {
        container.contextPut("registrationSuccessful", translate("contact.tracing.registration.successful"));
        container.contextPut("registrationMessage", translate("contact.tracing.registration.successful.message"));

        // Registration
        container.contextPut("nickName", registration.getNickName());
        container.contextPut("name", ContactTracingHelper.getName(registration));

        // Date
        Date startDate = registration.getStartDate();
        Date endDate = registration.getEndDate();
        boolean sameDay = CalendarUtils.isSameDay(startDate, endDate);

        container.contextPut("sameDay", sameDay);
        container.contextPut("startDate", StringHelper.formatLocaleDate(startDate.getTime(), getLocale()));
        container.contextPut("startTime",  StringHelper.formatLocaleTime(startDate, getLocale()));
        container.contextPut("endDate",  StringHelper.formatLocaleDate(endDate.getTime(), getLocale()));
        container.contextPut("endTime",  StringHelper.formatLocaleTime(endDate, getLocale()));

        // Location
        container.contextPut("reference", location.getReference());
        container.contextPut("title", location.getTitle());
        container.contextPut("building", location.getBuilding());
        container.contextPut("room", location.getRoom());
        container.contextPut("sector", location.getSector());
        container.contextPut("table", location.getTable());
        container.contextPut("seatNumber",registration.getSeatNumber());

        // Icon
        container.contextPut("confirmationIcon", "<i class='o_icon o_icon_check'></i>");
        
        // Immunity proof
        container.contextPut("certificateActive", immunityProofModule.isEnabled() && registration.getImmunityProofLevel() != null);
        if (immunityProofModule.isEnabled() && registration.getImmunityProofLevel() != null) {
	        container.contextPut("certificateState", registration.getImmunityProofLevel());
	        container.contextPut("certificateStateText", translate("immunity.proof." + registration.getImmunityProofLevel().toString()));
        }
    }

    @Override
    protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
        if (source == closeLink) {
            fireEvent(ureq, Event.CLOSE_EVENT);
        }
    }

    @Override
    protected void formOK(UserRequest ureq) {
        // Nothing to do here
    }
}

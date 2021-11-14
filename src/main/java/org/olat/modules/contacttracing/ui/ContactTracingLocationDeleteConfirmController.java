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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.TranslatorHelper;
import org.olat.core.util.StringHelper;
import org.olat.modules.contacttracing.ContactTracingLocation;
import org.olat.modules.contacttracing.ContactTracingManager;
import org.olat.modules.contacttracing.ContactTracingSearchParams;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 15.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingLocationDeleteConfirmController extends FormBasicController {

    private static final String[] ON_KEYS = new String[] {"on"};

    private List<ContactTracingLocation> deleteLocations;
    private long registrationsCount;

    private StaticTextElement locationListEl;
    private TextElement confirmRegistrationsEl;
    private MultipleSelectionElement confirmCheckBoxEl;

    @Autowired
    private ContactTracingManager contactTracingManager;

    public ContactTracingLocationDeleteConfirmController(UserRequest ureq, WindowControl wControl, List<ContactTracingLocation> deleteLocations) {
        super(ureq, wControl, "contact_tracing_confirm_delete");

        this.deleteLocations = deleteLocations;

        initForm(ureq);
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        FormLayoutContainer confirmationForm = FormLayoutContainer.createDefaultFormLayout("confirmationForm", getTranslator());
        confirmationForm.setRootForm(mainForm);
        formLayout.add(confirmationForm);

        locationListEl = uifactory.addStaticTextElement("contact.tracing.locations", null, confirmationForm);
        confirmRegistrationsEl = uifactory.addTextElement("contact.tracing.cols.registrations", -1, null, confirmationForm);
        confirmRegistrationsEl.setMandatory(true);
        confirmCheckBoxEl = uifactory.addCheckboxesHorizontal("contact.tracing.location.delete.confirm.label", confirmationForm, ON_KEYS, TranslatorHelper.translateAll(getTranslator(), new String[] {"contact.tracing.location.delete.confirm"}));
        confirmCheckBoxEl.setMandatory(true);

        FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("buttonLayout", getTranslator());
        buttonLayout.setRootForm(mainForm);
        confirmationForm.add(buttonLayout);

        uifactory.addFormCancelButton("contact.tracing.location.delete.cancel", buttonLayout, ureq, getWindowControl());
        uifactory.addFormSubmitButton("contact.tracing.location.delete.submit", buttonLayout);


        // Load data
        registrationsCount = 0l;

        StringBuilder locationListValue = new StringBuilder();
        ContactTracingSearchParams searchParams = new ContactTracingSearchParams();

        for (ContactTracingLocation location : deleteLocations) {
            searchParams.setLocation(location);
            long locationRegistrations = contactTracingManager.getRegistrationsCount(searchParams);
            registrationsCount += locationRegistrations;

            locationListValue
                    .append(location.getTitle())
                    .append("<ul>")
                    .append("<li>")
                    .append(locationRegistrations).append(" ").append(translate("contact.tracing.location.delete.list.registration" + (locationRegistrations != 1 ? "s" : "")))
                    .append("</li>")
                    .append("</ul>");
        }

        locationListEl.setValue(locationListValue.toString());

        ((FormLayoutContainer) formLayout).contextPut("numberOfRegistrations", new String[] {String.valueOf(registrationsCount)});
    }

    @Override
    protected boolean validateFormLogic(UserRequest ureq) {
        boolean allOk = super.validateFormLogic(ureq);

        allOk &= validateRegistrations(confirmRegistrationsEl);

        if (!confirmCheckBoxEl.isSelected(0)) {
            allOk = false;
            confirmCheckBoxEl.setErrorKey("form.mandatory.hover", null);
        } else {
            confirmCheckBoxEl.clearError();
        }

        return allOk;
    }

    private boolean validateRegistrations(TextElement el) {
        el.clearError();
        boolean allOk = validateFormItem(el);
        if(el.isEnabled() && el.isVisible()) {
            String val = el.getValue();
            if (StringHelper.containsNonWhitespace(val)) {
                try {
                    int number = Integer.parseInt(val);
                    if (number != registrationsCount) {
                        el.setErrorKey("contact.tracing.location.delete.count.error", null);
                        allOk = false;
                    }
                } catch (NumberFormatException e) {
                    el.setErrorKey("contact.tracing.location.delete.count.error", null);
                    allOk = false;
                }
            } else if (el.isMandatory()) {
                el.setErrorKey("form.mandatory.hover", null);
                allOk = false;
            }
        }
        return allOk;
    }

    @Override
    protected void formOK(UserRequest ureq) {
        contactTracingManager.deleteLocations(deleteLocations);

        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void formCancelled(UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }
}

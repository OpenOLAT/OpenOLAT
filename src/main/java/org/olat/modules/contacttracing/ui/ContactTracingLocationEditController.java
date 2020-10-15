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

import org.olat.core.gui.*;
import org.olat.core.gui.components.form.flexible.*;
import org.olat.core.gui.components.form.flexible.elements.*;
import org.olat.core.gui.components.form.flexible.impl.*;
import org.olat.core.gui.control.*;
import org.olat.core.gui.translator.*;
import org.olat.modules.contacttracing.*;
import org.springframework.beans.factory.annotation.*;

/**
 * Initial date: 12.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingLocationEditController extends FormBasicController {

    private static final String[] ON_KEYS = new String[]{"on"};
    private static final String[] GUEST_VALUES = new String[]{"contact.tracing.location.allowed"};
    private static final String[] QR_VALUES = new String[]{"contact.tracing.location.custom.qr.id"};

    private ContactTracingLocation location;

    private TextElement referenceEl;
    private TextElement titleEl;
    private TextElement roomEl;
    private TextElement buildingEl;
    private TextElement qrIdEl;
    private MultipleSelectionElement customQrIdEl;
    private MultipleSelectionElement guestsAllowedEl;

    @Autowired
    private ContactTracingManager contactTracingManager;

    public ContactTracingLocationEditController(UserRequest ureq, WindowControl wControl, ContactTracingLocation location) {
        super(ureq, wControl);

        // Save location
        this.location = location;

        initForm(ureq);
        loadData();
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        // Add input fields
        referenceEl = uifactory.addTextElement("contact.tracing.cols.reference", 255, null, formLayout);
        referenceEl.setNotEmptyCheck("contact.tracing.required");
        referenceEl.addActionListener(FormEvent.ONCHANGE);
        referenceEl.setMandatory(true);
        titleEl = uifactory.addTextElement("contact.tracing.cols.title", 255, null, formLayout);
        titleEl.setNotEmptyCheck("contact.tracing.required");
        titleEl.addActionListener(FormEvent.ONCHANGE);
        titleEl.setMandatory(true);
        roomEl = uifactory.addTextElement("contact.tracing.cols.room", 255, null, formLayout);
        roomEl.setNotEmptyCheck("contact.tracing.required");
        roomEl.addActionListener(FormEvent.ONCHANGE);
        roomEl.setMandatory(true);
        buildingEl = uifactory.addTextElement("contact.tracing.cols.building", 255, null, formLayout);
        buildingEl.setNotEmptyCheck("contact.tracing.required");
        buildingEl.addActionListener(FormEvent.ONCHANGE);
        buildingEl.setMandatory(true);
        customQrIdEl = uifactory.addCheckboxesHorizontal("contact.tracing.cols.qr.id", formLayout, ON_KEYS, TranslatorHelper.translateAll(getTranslator(), QR_VALUES));
        customQrIdEl.addActionListener(FormEvent.ONCHANGE);
        qrIdEl = uifactory.addTextElement("qr.id.element", null, 255, null, formLayout);
        qrIdEl.setNotEmptyCheck("contact.tracing.required");
        qrIdEl.setExampleKey("noTransOnlyParam", new String[]{""});
        qrIdEl.addActionListener(FormEvent.ONCHANGE);
        qrIdEl.setMandatory(true);
        guestsAllowedEl = uifactory.addCheckboxesHorizontal("contact.tracing.cols.guest", formLayout, ON_KEYS, TranslatorHelper.translateAll(getTranslator(), GUEST_VALUES));

        // Add button layout
        FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("location.edit.buttons", getTranslator());
        buttonLayout.setRootForm(mainForm);
        formLayout.add(buttonLayout);

        // Add buttons
        uifactory.addFormSubmitButton("contact.tracing.location.submit", buttonLayout);
        uifactory.addFormCancelButton("contact.tracing.location.cancel", buttonLayout, ureq, getWindowControl());
    }

    private void loadData() {
        if (location != null) {
            referenceEl.setValue(location.getReference());
            titleEl.setValue(location.getTitle());
            roomEl.setValue(location.getRoom());
            buildingEl.setValue(location.getBuilding());
            guestsAllowedEl.select(ON_KEYS[0], location.isAccessibleByGuests());
            qrIdEl.setValue(location.getQrId());

            // Check whether QR ID is custom
            if (location.getQrId().equals(generateQrId())) {
                qrIdEl.setVisible(false);
                customQrIdEl.select(ON_KEYS[0], false);
            } else{
                qrIdEl.setVisible(true);
                customQrIdEl.select(ON_KEYS[0], true);
            }
        } else {
            qrIdEl.setVisible(false);
        }
    }

    @Override
    protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
        if (source == customQrIdEl) {
            if (customQrIdEl.isSelected(0)) {
                qrIdEl.setVisible(true);
            } else {
                qrIdEl.setVisible(false);
                generateQrId();
            }
        } else if (source == qrIdEl) {
            qrIdExists(qrIdEl.getValue());
        } else if (!customQrIdEl.isSelected(0)) {
            generateQrId();
        }
    }

    private String generateQrId() {
        String qrId = new StringBuilder()
                .append(buildingEl.getValue())
                .append("-")
                .append(roomEl.getValue())
                .toString()
                .replaceAll("\\s+","");

        return qrIdExists(qrId) ? "error" : qrId;
    }

    private boolean qrIdExists(String qrId) {
        qrIdEl.setValue(qrId);
        qrIdEl.setExampleKey("noTransOnlyParam", new String[]{qrIdEl.getValue()});

        if(contactTracingManager.qrIdExists(qrId)) {
            // Check whether it is the QR ID of the current location
            if (!(location != null && location.getQrId().equals(qrId))) {
                customQrIdEl.select(ON_KEYS[0], true);
                qrIdEl.setVisible(true);
                qrIdEl.setErrorKey("contact.tracing.location.qr.id.exists", null);

                return true;
            }
        }
        qrIdEl.clearError();
        return false;
    }

    @Override
    protected boolean validateFormLogic(UserRequest ureq) {
        boolean allOk =  super.validateFormLogic(ureq);

        allOk &= validateFormItem(referenceEl);
        allOk &= validateFormItem(titleEl);
        allOk &= validateFormItem(roomEl);
        allOk &= validateFormItem(buildingEl);
        allOk &= validateFormItem(qrIdEl);

        allOk &= !qrIdExists(qrIdEl.getValue());

        return allOk;
    }

    @Override
    protected void formOK(UserRequest ureq) {
        if (location == null) {
            contactTracingManager.createLocation(
                    referenceEl.getValue(),
                    titleEl.getValue(),
                    roomEl.getValue(),
                    buildingEl.getValue(),
                    qrIdEl.getValue(),
                    guestsAllowedEl.isSelected(0));
        } else {
            location.setReference(referenceEl.getValue());
            location.setTitle(titleEl.getValue());
            location.setRoom(roomEl.getValue());
            location.setBuildiung(buildingEl.getValue());
            location.setQrId(qrIdEl.getValue());
            location.setAccessibleByGuests(guestsAllowedEl.isSelected(0));

            contactTracingManager.updateLocation(location);
        }

        fireEvent(ureq, Event.DONE_EVENT);
    }

    @Override
    protected void formCancelled(UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    @Override
    protected void doDispose() {

    }
}

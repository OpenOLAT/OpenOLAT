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

import java.util.Collections;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.TranslatorHelper;
import org.olat.modules.contacttracing.ContactTracingDispatcher;
import org.olat.modules.contacttracing.ContactTracingLocation;
import org.olat.modules.contacttracing.ContactTracingManager;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Initial date: 12.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingLocationEditController extends FormBasicController {

    private static final String[] ON_KEYS = new String[]{"on"};
    private static final String[] GUEST_VALUES = new String[]{"contact.tracing.location.allowed"};
    private static final String[] QR_ID_VALUES = new String[]{"contact.tracing.location.custom.qr.id"};
    private static final String[] QR_TEXT_VALUES = new String[]{"contact.tracing.location.custom.qr.text"};

    private ContactTracingLocation location;
    private boolean saveLocation;

    private TextElement referenceEl;
    private TextElement titleEl;
    private TextElement roomEl;
    private TextElement buildingEl;
    private TextElement qrIdEl;
    private RichTextElement qrTextEl;
    private MultipleSelectionElement customQrIdEl;
    private MultipleSelectionElement customQrTextEl;
    private MultipleSelectionElement guestsAllowedEl;
    private FormLink generatePdfPreviewLink;

    private ContactTracingPDFController pdfController;

    @Autowired
    private ContactTracingManager contactTracingManager;

    public ContactTracingLocationEditController(UserRequest ureq, WindowControl wControl, ContactTracingLocation location) {
        super(ureq, wControl, "contact_tracing_location_edit");

        // Save location
        this.location = location;
        this.saveLocation = location != null;

        initForm(ureq);
        loadData();
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        FormLayoutContainer editForm = FormLayoutContainer.createDefaultFormLayout("editForm", getTranslator());
        editForm.setRootForm(mainForm);
        formLayout.add(editForm);

        // Add input fields
        // TODO Better solution for the ONCHANGE event?
        referenceEl = uifactory.addTextElement("contact.tracing.cols.reference", 255, null, editForm);
        referenceEl.setNotEmptyCheck("contact.tracing.required");
        referenceEl.addActionListener(FormEvent.ONCHANGE);
        referenceEl.setMandatory(true);
        titleEl = uifactory.addTextElement("contact.tracing.cols.title", 255, null, editForm);
        titleEl.setNotEmptyCheck("contact.tracing.required");
        titleEl.addActionListener(FormEvent.ONCHANGE);
        titleEl.setMandatory(true);
        roomEl = uifactory.addTextElement("contact.tracing.cols.room", 255, null, editForm);
        roomEl.setNotEmptyCheck("contact.tracing.required");
        roomEl.addActionListener(FormEvent.ONCHANGE);
        roomEl.setMandatory(true);
        buildingEl = uifactory.addTextElement("contact.tracing.cols.building", 255, null, editForm);
        buildingEl.setNotEmptyCheck("contact.tracing.required");
        buildingEl.addActionListener(FormEvent.ONCHANGE);
        buildingEl.setMandatory(true);
        customQrIdEl = uifactory.addCheckboxesHorizontal("contact.tracing.cols.qr.id", editForm, ON_KEYS, TranslatorHelper.translateAll(getTranslator(), QR_ID_VALUES));
        customQrIdEl.addActionListener(FormEvent.ONCHANGE);
        qrIdEl = uifactory.addTextElement("qr.id.element", null, 255, null, editForm);
        qrIdEl.setNotEmptyCheck("contact.tracing.required");
        qrIdEl.setExampleKey("noTransOnlyParam", new String[]{""});
        qrIdEl.addActionListener(FormEvent.ONCHANGE);
        qrIdEl.setMandatory(true);
        customQrTextEl = uifactory.addCheckboxesHorizontal("contact.tracing.cols.qr.text", editForm, ON_KEYS, TranslatorHelper.translateAll(getTranslator(), QR_TEXT_VALUES));
        customQrTextEl.addActionListener(FormEvent.ONCHANGE);
        qrTextEl = uifactory.addRichTextElementForStringDataCompact("qr.text.element", null, null, -1, -1, null, editForm, ureq.getUserSession(), getWindowControl());
        qrTextEl.addActionListener(FormEvent.ONCHANGE);
        guestsAllowedEl = uifactory.addCheckboxesHorizontal("contact.tracing.cols.guest", editForm, ON_KEYS, TranslatorHelper.translateAll(getTranslator(), GUEST_VALUES));

        // Add button layout
        FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("location.edit.buttons", getTranslator());
        buttonLayout.setRootForm(mainForm);
        editForm.add(buttonLayout);

        // Add buttons
        uifactory.addFormSubmitButton("contact.tracing.location.submit", buttonLayout);
        uifactory.addFormCancelButton("contact.tracing.location.cancel", buttonLayout, ureq, getWindowControl());
        generatePdfPreviewLink = uifactory.addFormLink("contact.tracing.location.pdf.preview", buttonLayout, Link.BUTTON);
    }

    private void loadData() {
        if (location != null) {
            referenceEl.setValue(location.getReference());
            titleEl.setValue(location.getTitle());
            roomEl.setValue(location.getRoom());
            buildingEl.setValue(location.getBuilding());
            guestsAllowedEl.select(ON_KEYS[0], location.isAccessibleByGuests());
            qrIdEl.setValue(location.getQrId());
            qrTextEl.setValue(location.getQrText());

            // Check whether QR ID is custom
            if (location.getQrId().equals(generateQrId())) {
                qrIdEl.setVisible(false);
                customQrIdEl.select(ON_KEYS[0], false);
            } else{
                qrIdEl.setVisible(true);
                customQrIdEl.select(ON_KEYS[0], true);
            }

            // Check whether custom Qr text exists
            if (location.getQrText() != null) {
                qrTextEl.setVisible(true);
                customQrTextEl.select(ON_KEYS[0], true);
            } else {
                customQrTextEl.select(ON_KEYS[0], false);
            }

        } else {
            qrIdEl.setVisible(false);
            qrTextEl.setVisible(false);
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
                updatePdfPreview(ureq);
            }
        } else if (source == qrIdEl) {
            qrIdExists(qrIdEl.getValue());
            updatePdfPreview(ureq);
        } else if (source == customQrTextEl) {
            qrTextEl.setVisible(customQrTextEl.isSelected(0));
            updatePdfPreview(ureq);
        } else if (source == qrTextEl) {
            updatePdfPreview(ureq);
        } else if (source == generatePdfPreviewLink) {
            generatePdfPreview(ureq);
        } else if (!customQrIdEl.isSelected(0)) {
            // Generate QR ID automatically
            generateQrId();
            updatePdfPreview(ureq);
        }
    }

    private void generatePdfPreview(UserRequest ureq) {
        if (validateFormLogic(ureq)) {
            location = saveLocation();
            pdfController = new ContactTracingPDFController(ureq, getWindowControl(), Collections.singletonList(location));
            flc.put("pdfPreview", pdfController.getInitialComponent());
            generatePdfPreviewLink.setI18nKey("contact.tracing.location.pdf.preview.update");
        }
    }

    private void updatePdfPreview(UserRequest ureq) {
        if (location != null && pdfController != null) {
            generatePdfPreview(ureq);
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
        qrIdEl.setExampleKey("noTransOnlyParam", new String[]{ContactTracingDispatcher.getMeetingUrl(qrId)});

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
        allOk &= validateFormItem(qrTextEl);

        allOk &= !qrIdExists(qrIdEl.getValue());

        return allOk;
    }

    @Override
    protected void formOK(UserRequest ureq) {
        location = saveLocation();
        saveLocation = true;

        fireEvent(ureq, Event.DONE_EVENT);
    }

    private ContactTracingLocation saveLocation() {
        if (location == null) {
            return contactTracingManager.createLocation(
                    referenceEl.getValue(),
                    titleEl.getValue(),
                    roomEl.getValue(),
                    buildingEl.getValue(),
                    qrIdEl.getValue(),
                    qrTextEl.getValue(),
                    guestsAllowedEl.isSelected(0));
        } else {
            location.setReference(referenceEl.getValue());
            location.setTitle(titleEl.getValue());
            location.setRoom(roomEl.getValue());
            location.setBuildiung(buildingEl.getValue());
            location.setQrId(qrIdEl.getValue());
            location.setQrText(qrTextEl.getValue());
            location.setAccessibleByGuests(guestsAllowedEl.isSelected(0));

            return contactTracingManager.updateLocation(location);
        }
    }

    @Override
    protected void formCancelled(UserRequest ureq) {
        fireEvent(ureq, Event.CANCELLED_EVENT);
    }

    @Override
    protected void doDispose() {
        // In case a preview has been generated for a new location but the form has been cancelled
        if (!saveLocation && location != null) {
            contactTracingManager.deleteLocations(Collections.singletonList(location));
        }
    }
}

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
import java.util.Random;

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
import org.olat.core.util.StringHelper;
import org.olat.modules.contacttracing.ContactTracingDispatcher;
import org.olat.modules.contacttracing.ContactTracingLocation;
import org.olat.modules.contacttracing.ContactTracingManager;
import org.olat.modules.contacttracing.model.ContactTracingLocationImpl;
import org.springframework.beans.factory.annotation.Autowired;


/**
 * Initial date: 12.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingLocationEditController extends FormBasicController {

    private static final String[] ON_KEYS = new String[]{"on"};
    private static final String[] GUEST_VALUES = new String[]{"contact.tracing.location.allowed"};
    private static final String[] QR_TEXT_VALUES = new String[]{"contact.tracing.location.custom.qr.text"};

    private ContactTracingLocation location;

    private TextElement referenceEl;
    private TextElement titleEl;
    private TextElement buildingEl;
    private TextElement roomEl;
    private TextElement sectorEl;
    private TextElement tableEl;
    private TextElement qrIdEl;
    private RichTextElement qrTextEl;
    private MultipleSelectionElement customQrTextEl;
    private MultipleSelectionElement guestsAllowedEl;
    private FormLink generatePdfPreviewLink;
    private FormLink generateNumericIdentifierLink;
    private FormLink generateHumanReadableIdentifierLink;

    private ContactTracingPDFController pdfController;

    @Autowired
    private ContactTracingManager contactTracingManager;

    public ContactTracingLocationEditController(UserRequest ureq, WindowControl wControl, ContactTracingLocation location) {
        super(ureq, wControl, "contact_tracing_location_edit");

        // Save location
        this.location = location;

        initForm(ureq);
        loadData();
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        FormLayoutContainer editForm = FormLayoutContainer.createDefaultFormLayout("editForm", getTranslator());
        editForm.setRootForm(mainForm);
        formLayout.add(editForm);

        // Add input fields
        referenceEl = uifactory.addTextElement("contact.tracing.cols.reference", 255, null, editForm);
        titleEl = uifactory.addTextElement("contact.tracing.cols.title", 255, null, editForm);
        buildingEl = uifactory.addTextElement("contact.tracing.cols.building", 255, null, editForm);
        roomEl = uifactory.addTextElement("contact.tracing.cols.room", 255, null, editForm);
        sectorEl = uifactory.addTextElement("contact.tracing.cols.sector", 255, null, editForm);
        tableEl = uifactory.addTextElement("contact.tracing.cols.table", 255, null, editForm);

        qrIdEl = uifactory.addTextElement("contact.tracing.cols.qr.id", 255, null, editForm);
        qrIdEl.setNotEmptyCheck("contact.tracing.required");
        qrIdEl.setExampleKey("noTransOnlyParam", new String[]{""});
        qrIdEl.addActionListener(FormEvent.ONCHANGE);
        qrIdEl.setMandatory(true);
        FormLayoutContainer qrCodeButtons = FormLayoutContainer.createButtonLayout("qrCodeButtons", getTranslator());
        qrCodeButtons.setRootForm(mainForm);
        editForm.add(qrCodeButtons);
        generateNumericIdentifierLink = uifactory.addFormLink("contact.tracing.location.edit.generate.numeric.identifier", qrCodeButtons, Link.BUTTON);
        generateHumanReadableIdentifierLink = uifactory.addFormLink("contact.tracing.location.edit.generate.human.readable.identifier", qrCodeButtons, Link.BUTTON);


        customQrTextEl = uifactory.addCheckboxesHorizontal("contact.tracing.cols.qr.text", editForm, ON_KEYS, TranslatorHelper.translateAll(getTranslator(), QR_TEXT_VALUES));
        customQrTextEl.addActionListener(FormEvent.ONCHANGE);
        qrTextEl = uifactory.addRichTextElementForStringDataCompact("qr.text.element", null, null, -1, -1, null, editForm, ureq.getUserSession(), getWindowControl());
        qrTextEl.getEditorConfiguration().disableImageAndMovie();
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
            buildingEl.setValue(location.getBuilding());
            roomEl.setValue(location.getRoom());
            sectorEl.setValue(location.getSector());
            tableEl.setValue(location.getTable());
            guestsAllowedEl.select(ON_KEYS[0], location.isAccessibleByGuests());
            qrIdEl.setValue(location.getQrId());
            qrIdEl.setHelpTextKey("contact.tracing.location.edit.qr.id.message", null);
            qrIdEl.setExampleKey("noTransOnlyParam", new String[]{ContactTracingDispatcher.getRegistrationUrl(location.getQrId())});
            qrTextEl.setValue(location.getQrText());

            // Check whether custom Qr text exists
            if (StringHelper.containsNonWhitespace(location.getQrText())) {
                qrTextEl.setVisible(true);
                customQrTextEl.select(ON_KEYS[0], true);
            } else {
                qrTextEl.setVisible(false);
                customQrTextEl.select(ON_KEYS[0], false);
            }

        } else {
            qrTextEl.setVisible(false);
            guestsAllowedEl.select(ON_KEYS[0], true);
            generateNumericQrID();
        }
    }

    @Override
    protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
        if (source == generateNumericIdentifierLink) {
            generateNumericQrID();
            updatePdfPreview(ureq);
        } else if (source == generateHumanReadableIdentifierLink) {
            generateHumanReadableID();
            updatePdfPreview(ureq);
        } else if (source == generatePdfPreviewLink) {
            generatePdfPreview(ureq);
        } else if (source == customQrTextEl) {
            qrTextEl.setVisible(customQrTextEl.isSelected(0));
        }
    }

    private void generatePdfPreview(UserRequest ureq) {
        if (validateFormLogic(ureq)) {
            ContactTracingLocation previewlocation = generatePdfPreviewLocation();
            pdfController = new ContactTracingPDFController(ureq, getWindowControl(), Collections.singletonList(previewlocation));
            flc.put("pdfPreview", pdfController.getInitialComponent());
            generatePdfPreviewLink.setI18nKey("contact.tracing.location.pdf.preview.update");
        }
    }

    private void updatePdfPreview(UserRequest ureq) {
        if (pdfController != null) {
            generatePdfPreview(ureq);
        }
    }

    private ContactTracingLocation generatePdfPreviewLocation() {
        // This location is never persisted
        ContactTracingLocation previewLocation = new ContactTracingLocationImpl();

        previewLocation.setReference(referenceEl.getValue());
        previewLocation.setTitle(titleEl.getValue());
        previewLocation.setBuilding(buildingEl.getValue());
        previewLocation.setRoom(roomEl.getValue());
        previewLocation.setSector(sectorEl.getValue());
        previewLocation.setTable(tableEl.getValue());
        previewLocation.setQrId(qrIdEl.getValue());
        previewLocation.setQrText(StringHelper.containsNonWhitespace(qrTextEl.getValue()) ? qrTextEl.getValue() : null);
        previewLocation.setAccessibleByGuests(guestsAllowedEl.isSelected(0));

        return previewLocation;
    }

    private void generateNumericQrID() {
        Random generator = new Random();
        String qrId;

        do {
            qrId = String.valueOf(generator.nextInt(80000) + 10000);
        } while (qrIdExists(qrId, true));
    }

    private void generateHumanReadableID() {
        // Try with reference
        if (qrIdExists(transformStringToIdentifier(referenceEl.getValue()), true)) {
            StringBuilder qrIdBuilder = new StringBuilder();

            // Try with building-room-sector-table
            qrIdBuilder.append(transformStringToIdentifier(buildingEl.getValue()))
                    .append(buildingEl.getValue().length() > 0 ? "-" : "")
                    .append(transformStringToIdentifier(roomEl.getValue()))
                    .append(roomEl.getValue().length() > 0 ? "-" : "")
                    .append(transformStringToIdentifier(sectorEl.getValue()))
                    .append(sectorEl.getValue().length() > 0 ? "-" : "")
                    .append(transformStringToIdentifier(tableEl.getValue()));

            // Replace last _ if existing
            String qrId = qrIdBuilder.toString();
            if (qrId.endsWith("-")) {
                qrId = qrId.substring(0, qrId.length() - 1);
            }

            if (qrIdExists(qrId, true)) {
                // Try with table
                if (qrIdExists(transformStringToIdentifier(titleEl.getValue()), true)) {
                    showWarning("contact.tracing.location.edit.generate.human.readable.error");
                }
            }
        }
    }

    private String transformStringToIdentifier(String identifier) {
        if (!StringHelper.containsNonWhitespace(identifier)) {
            return "";
        }

        identifier = StringHelper.transformDisplayNameToFileSystemName(identifier);
        if (identifier.equals("_")) {
            return "";
        }

        return identifier;
    }

    private boolean qrIdExists(String qrId, boolean checkGeneratedId) {
        // Return true if an empty qrId was provided
        if (!StringHelper.containsNonWhitespace(qrId) && checkGeneratedId) {
            return true;
        }
        // Check if qrIdEl is empty
        if (validateFormItem(qrIdEl) || checkGeneratedId) {
            if (contactTracingManager.qrIdExists(qrId)) {
                // Check whether it is the QR ID of the current location
                if (!(location != null && location.getQrId().equals(qrId))) {
                    qrIdEl.setErrorKey("contact.tracing.location.qr.id.exists", null);
                    return true;
                }
            }
            // If qrId is not used yet
            qrIdEl.setValue(qrId);
            qrIdEl.setExampleKey("noTransOnlyParam", new String[]{ContactTracingDispatcher.getRegistrationUrl(qrId)});
        }

        validateFormItem(qrIdEl);
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

        allOk &= !qrIdExists(qrIdEl.getValue(), false);

        return allOk;
    }

    @Override
    protected void formOK(UserRequest ureq) {
        location = saveLocation();

        fireEvent(ureq, Event.DONE_EVENT);
    }

    private ContactTracingLocation saveLocation() {
        if (location == null) {
            return contactTracingManager.createLocation(
                    referenceEl.getValue(),
                    titleEl.getValue(),
                    buildingEl.getValue(),
                    roomEl.getValue(),
                    sectorEl.getValue(),
                    tableEl.getValue(),
                    qrIdEl.getValue(),
                    StringHelper.containsNonWhitespace(qrTextEl.getValue()) ? qrTextEl.getValue() : null,
                    guestsAllowedEl.isSelected(0));
        } else {
            location.setReference(referenceEl.getValue());
            location.setTitle(titleEl.getValue());
            location.setBuilding(buildingEl.getValue());
            location.setRoom(roomEl.getValue());
            location.setSector(sectorEl.getValue());
            location.setTable(tableEl.getValue());
            location.setQrId(qrIdEl.getValue());
            location.setQrText(StringHelper.containsNonWhitespace(qrTextEl.getValue()) ? qrTextEl.getValue() : null);
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
        // Nothing to dispose
    }
}

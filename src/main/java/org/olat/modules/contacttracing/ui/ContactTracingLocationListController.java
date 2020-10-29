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


import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.services.pdf.PdfService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.gui.media.MediaResource;
import org.olat.course.editor.overview.YesNoCellRenderer;
import org.olat.modules.contacttracing.ContactTracingLocation;
import org.olat.modules.contacttracing.ContactTracingManager;
import org.olat.modules.contacttracing.ui.ContactTracingLocationTableModel.ContactTracingLocationCols;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 12.10.20<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ContactTracingLocationListController extends FormBasicController {

    private static final int PAGE_SIZE = 20;
    private static final String QR_CODE_GENERATE_CMD = "qr_code_generate_cmd";
    private static final String EDIT_CMD = "edit_cmd";
    private static final String DELETE_CMD = "delete_cmd";

    private FlexiTableElement tableEl;
    private ContactTracingLocationTableModel tableModel;

    private FormLink generateQrCodeSelectedLocationsLink;
    private FormLink deleteSelectedLocationsLink;
    private FormLink generateQrCodeCalloutLink;
    private FormLink editLocationCalloutLink;
    private FormLink deleteCalloutLink;
    private FormLink addLocationLink;
    private FormLink importLocationsLink;

    private CloseableModalController cmc;
    private CloseableCalloutWindowController calloutWindowController;
    private ContactTracingLocationEditController editLocationController;
    private ContactTracingLocationEditController addLocationController;
    private ContactTracingLocationDeleteConfirmController deleteConfirmController;
    private StepsMainRunController importLocationsStepController;

    @Autowired
    private ContactTracingManager contactTracingManager;
    @Autowired
    private PdfService pdfService;

    public ContactTracingLocationListController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl, "contact_tracing_location_list");

        initForm(ureq);
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        // Columns
        DefaultFlexiColumnModel referenceColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.reference);
        DefaultFlexiColumnModel titleColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.title);
        DefaultFlexiColumnModel roomColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.room);
        DefaultFlexiColumnModel buildingColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.building);
        DefaultFlexiColumnModel qrIdColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.qrId);
        DefaultFlexiColumnModel registrationsColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.registrations);

        // Url column
        DefaultFlexiColumnModel urlColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.url);
        urlColumn.setDefaultVisible(false);

        // Key column
        DefaultFlexiColumnModel keyColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.key);
        keyColumn.setDefaultVisible(false);

        // QR text column
        DefaultFlexiColumnModel qrTextColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.qrText);
        TextFlexiCellRenderer textFlexiCellRenderer = new TextFlexiCellRenderer(EscapeMode.antisamy);
        qrTextColumn.setDefaultVisible(false);
        qrTextColumn.setCellRenderer(textFlexiCellRenderer);
        
        // Sector column
        DefaultFlexiColumnModel sectorColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.sector);
        sectorColumn.setDefaultVisible(false);
        
        // Table column
        DefaultFlexiColumnModel tableColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.table);
        tableColumn.setDefaultVisible(false);

        // Seat number column
        DefaultFlexiColumnModel seatNumberColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.seatNumber);
        seatNumberColumn.setCellRenderer(new YesNoCellRenderer(getTranslator()));
        seatNumberColumn.setDefaultVisible(false);
        
        // Guest column
        DefaultFlexiColumnModel guestColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.guest);
        guestColumn.setCellRenderer(new YesNoCellRenderer(getTranslator()));
       
        // Actions column
        DefaultFlexiColumnModel actionsColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.settings);
        actionsColumn.setIconHeader(ContactTracingLocationCols.settings.iconHeader());

        // Columns model
        FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
        columnModel.addFlexiColumnModel(keyColumn);
        columnModel.addFlexiColumnModel(referenceColumn);
        columnModel.addFlexiColumnModel(titleColumn);
        columnModel.addFlexiColumnModel(buildingColumn);
        columnModel.addFlexiColumnModel(roomColumn);
        columnModel.addFlexiColumnModel(sectorColumn);
        columnModel.addFlexiColumnModel(tableColumn);
        columnModel.addFlexiColumnModel(seatNumberColumn);
        columnModel.addFlexiColumnModel(qrIdColumn);
        columnModel.addFlexiColumnModel(urlColumn);
        columnModel.addFlexiColumnModel(qrTextColumn);
        columnModel.addFlexiColumnModel(guestColumn);
        columnModel.addFlexiColumnModel(registrationsColumn);
        columnModel.addFlexiColumnModel(actionsColumn);

        // Table model
        tableModel = new ContactTracingLocationTableModel(columnModel, contactTracingManager.getLocationsWithRegistrations(), getLocale());

        // Table element
        tableEl = uifactory.addTableElement(getWindowControl(), "locationsTable", tableModel, getTranslator(), formLayout);
        tableEl.setPageSize(PAGE_SIZE);
        tableEl.setExportEnabled(true);
        tableEl.setSearchEnabled(true);
        tableEl.setMultiSelect(true);
        tableEl.setSelectAllEnable(true);
        tableEl.setShowAllRowsEnabled(true);
        tableEl.setEmtpyTableMessageKey("contact.tracing.location.table.empty");
        tableEl.setAndLoadPersistedPreferences(ureq, ContactTracingLocationListController.class.getCanonicalName());

        // Create link to add a new location and import locations
        addLocationLink = uifactory.addFormLink("createNewLocation", "contact.tracing.location.add", null, formLayout, Link.BUTTON);
        addLocationLink.setIconLeftCSS("o_icon o_icon_fw o_icon_add");
        importLocationsLink = uifactory.addFormLink("importLocations", "contact.tracing.locations.import", null, formLayout, Link.BUTTON);
        importLocationsLink.setIconLeftCSS("o_icon o_icon-fw o_icon_import");

        // Create links to the batch buttons of the table
        generateQrCodeSelectedLocationsLink = uifactory.addFormLink("contact.tracing.location.qr.generate", formLayout, Link.BUTTON);
        generateQrCodeSelectedLocationsLink.setIconLeftCSS("o_icon o_icon_qrcode");
        deleteSelectedLocationsLink = uifactory.addFormLink("contact.tracing.location.delete", formLayout, Link.BUTTON);
        deleteSelectedLocationsLink.setIconLeftCSS("o_icon o_icon_delete_item");

        // Add batch buttons to table
        tableEl.addBatchButton(generateQrCodeSelectedLocationsLink);
        tableEl.addBatchButton(deleteSelectedLocationsLink);
    }

    private void reloadData() {
        tableModel.setObjects(contactTracingManager.getLocationsWithRegistrations());
        tableEl.reset();
    }

    @Override
    protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
        if (source == addLocationLink) {
            addLocationController = new ContactTracingLocationEditController(ureq, getWindowControl(), null);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), addLocationController.getInitialComponent(), true, translate("contact.tracing.location.create.title"), true, true);
            listenTo(addLocationController);
            listenTo(cmc);
            cmc.activate();
        } else if (source == importLocationsLink) {
        	// Create context wrapper (used to transfer data from step to step)
            ContactTracingStepContextWrapper contextWrapper = new ContactTracingStepContextWrapper();

            // Create first step and finish callback
            Step importStep = new ContactTracingLocationImportStep1(ureq, contextWrapper);
            FinishedCallback finish = new FinishedCallback();
            CancelCallback cancel = new CancelCallback();

            // Create step controller
            importLocationsStepController = new StepsMainRunController(ureq, getWindowControl(), importStep, finish, cancel, translate("contact.tracing.locations.import"), null);
            listenTo(importLocationsStepController);
            getWindowControl().pushAsModalDialog(importLocationsStepController.getInitialComponent());
        } else if (source == generateQrCodeSelectedLocationsLink) {
            // Collect selected locations
            List<ContactTracingLocation> generatePDFLocations = new ArrayList<>();
            tableEl.getMultiSelectedIndex().forEach(index -> generatePDFLocations.add(tableModel.getObject(index)));

            // Generate QR code PDF
            generateQrCodePDF(ureq, generatePDFLocations);
        } else if (source == deleteSelectedLocationsLink) {
            // Get selected locations
            List<ContactTracingLocation> deleteList = new ArrayList<>();
            tableEl.getMultiSelectedIndex().forEach(i -> deleteList.add(tableModel.getObject(i)));

            // Set up the cmc
            if (deleteList.size() > 0) {
                deleteConfirmController = new ContactTracingLocationDeleteConfirmController(ureq, getWindowControl(), deleteList);
                cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteConfirmController.getInitialComponent(), true, translate("contact.tracing.location.delete.title"), true, true);
                listenTo(deleteConfirmController);
                listenTo(cmc);
                cmc.activate();
            }
        } else if (source == generateQrCodeCalloutLink) {
            // First close the callout
            cleanUp();

            // Extract the location from the clicked link
            ContactTracingLocation location = (ContactTracingLocation) editLocationCalloutLink.getUserObject();
            generateQrCodePDF(ureq, Collections.singletonList(location));
        } else if (source == editLocationCalloutLink) {
            // First close the callout
            cleanUp();

            // Set up the cmc
            ContactTracingLocation location = (ContactTracingLocation) editLocationCalloutLink.getUserObject();
            editLocationController = new ContactTracingLocationEditController(ureq, getWindowControl(), location);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), editLocationController.getInitialComponent(), true, translate("contact.tracing.location.edit.title"), true, true);
            listenTo(editLocationController);
            listenTo(cmc);
            cmc.activate();
        } else if (source == deleteCalloutLink) {
            // First close the callout
            cleanUp();

            // Set up the cmc
            ContactTracingLocation deleteLocation = (ContactTracingLocation) editLocationCalloutLink.getUserObject();
            List<ContactTracingLocation> deleteList = Collections.singletonList(deleteLocation);
            deleteConfirmController = new ContactTracingLocationDeleteConfirmController(ureq, getWindowControl(), deleteList);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteConfirmController.getInitialComponent(), true, translate("contact.tracing.location.delete.title"), true, true);
            listenTo(deleteConfirmController);
            listenTo(cmc);
            cmc.activate();
        } else if (source instanceof FormLink) {
            FormLink link = (FormLink) source;

            if (link.getCmd().equals(ContactTracingLocationTableModel.ACTIONS_CMD)) {
                openActions(ureq, link);
            }
        } else if (event instanceof FlexiTableSearchEvent) {
            String searchString = ((FlexiTableSearchEvent) event).getSearch();
            tableModel.filter(searchString, null);
            tableEl.reset();
        }
    }

    @Override
    protected void event(UserRequest ureq, Controller source, Event event) {
        if (source == addLocationController) {
            if (event == Event.DONE_EVENT) {
                reloadData();
            }
            cleanUp();
        } else if (source == editLocationController) {
            if (event == Event.DONE_EVENT) {
                reloadData();
            }
            cleanUp();
        } else if (source == deleteConfirmController) {
            if (event == Event.DONE_EVENT) {
                reloadData();
            }
            cleanUp();
        } else if (source == importLocationsStepController) {
            if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
                // Close the dialog
                getWindowControl().pop();

                // Remove steps controller
                removeAsListenerAndDispose(importLocationsStepController);
                importLocationsStepController = null;

                // Reload form
                reloadData();
            }
        } else if (source == cmc) {
            cleanUp();
        }
    }

    private void openActions(UserRequest ureq, FormLink link) {
        // Extract location
        ContactTracingLocation location = (ContactTracingLocation) link.getUserObject();

        // Generate links
        if (generateQrCodeCalloutLink == null) {
            generateQrCodeCalloutLink = uifactory.addFormLink("generateQrCode", QR_CODE_GENERATE_CMD, "contact.tracing.location.qr.generate", null, flc, Link.LINK);
            generateQrCodeCalloutLink.setIconLeftCSS("o_icon o_icon_fw o_icon_qrcode");
        }
        if (editLocationCalloutLink == null) {
            editLocationCalloutLink = uifactory.addFormLink("editLocation", EDIT_CMD, "contact.tracing.location.edit", null, flc, Link.LINK);
            editLocationCalloutLink.setIconLeftCSS("o_icon o_icon_fw o_icon_edit");
        }
        if (deleteCalloutLink == null) {
            deleteCalloutLink = uifactory.addFormLink("deleteLocation", DELETE_CMD, "contact.tracing.location.delete", null, flc, Link.LINK);
            deleteCalloutLink.setIconLeftCSS("o_icon o_icon_fw o_icon_delete_item");
        }

        // Add loaction as user object to links
        generateQrCodeCalloutLink.setUserObject(location);
        editLocationCalloutLink.setUserObject(location);
        deleteCalloutLink.setUserObject(location);

        // Create velocity container for callout
        CalloutSettings settings = new CalloutSettings(true);
        VelocityContainer locationToolsContainer = createVelocityContainer("contact_tracing_location_tools");

        // Add links to velocity container
        locationToolsContainer.put("generateQrCode", generateQrCodeCalloutLink.getComponent());
        locationToolsContainer.put("editLocation", editLocationCalloutLink.getComponent());
        locationToolsContainer.put("deleteLocation", deleteCalloutLink.getComponent());

        calloutWindowController = new CloseableCalloutWindowController(ureq, getWindowControl(), locationToolsContainer, link.getFormDispatchId(), "", true, "", settings);
        listenTo(calloutWindowController);
        calloutWindowController.activate();
    }

    private void generateQrCodePDF(UserRequest ureq, List<ContactTracingLocation> locations) {
        if (locations == null || locations.isEmpty()) {
            showWarning("contact.tracing.location.qr.selection.empty.warning");
            return;
        }
        MediaResource pdf = pdfService.convert("Contact Tracing Locations.pdf", getIdentity(), new ContactTracingPDFControllerCreator(locations), getWindowControl());
        ureq.getDispatchResult().setResultingMediaResource(pdf);
    }

    private void cleanUp() {
        if (cmc != null && cmc.isCloseable()) {
            cmc.deactivate();
        }
        if (calloutWindowController != null && calloutWindowController.isCloseable()) {
            calloutWindowController.deactivate();
        }

        removeAsListenerAndDispose(cmc);
        removeAsListenerAndDispose(calloutWindowController);
        removeAsListenerAndDispose(addLocationController);
        removeAsListenerAndDispose(editLocationController);

        cmc = null;
        calloutWindowController = null;
        addLocationController = null;
        editLocationController = null;
    }

    @Override
    protected void formOK(UserRequest ureq) {

    }

    @Override
    protected void doDispose() {

    }
    
    private class FinishedCallback implements StepRunnerCallback {
        @Override
        public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
        	// Get context wrapper
        	ContactTracingStepContextWrapper contextWrapper = (ContactTracingStepContextWrapper) runContext.get("data");
        	List<ContactTracingLocation> locationList = contextWrapper.getLocations();
        	
        	// Return if no locations available
        	if (locationList == null || locationList.isEmpty()) {
				return StepsMainRunController.DONE_UNCHANGED;
			}
        	
        	Date creationDate = new Date();
        	for (ContactTracingLocation location : locationList) {
        		location.setCreationDate(creationDate);
                location.setLastModified(creationDate);
				contactTracingManager.saveLocation(location);
			}
        	
            // Fire event
            return StepsMainRunController.DONE_MODIFIED;
        }
    }
    
    private static class CancelCallback implements StepRunnerCallback {
        @Override
        public Step execute(UserRequest ureq, WindowControl wControl, StepsRunContext runContext) {
            return Step.NOSTEP;
        }
    }
}

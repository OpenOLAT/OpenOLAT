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
import java.util.List;

import org.olat.core.gui.UserRequest;
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
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
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

    private CloseableModalController cmc;
    private CloseableCalloutWindowController calloutWindowController;
    private ContactTracingLocationEditController editLocationController;
    private ContactTracingLocationEditController addLocationController;
    private ContactTracingLocationDeleteConfirmController deleteConfirmController;

    @Autowired
    private ContactTracingManager contactTracingManager;

    public ContactTracingLocationListController(UserRequest ureq, WindowControl wControl) {
        super(ureq, wControl, "contact_tracing_locations");

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

        // Guest column
        DefaultFlexiColumnModel guestColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.guest);
        guestColumn.setCellRenderer(new YesNoCellRenderer(getTranslator()));
        // Actinos column
        DefaultFlexiColumnModel actionsColumn = new DefaultFlexiColumnModel(ContactTracingLocationCols.settings);
        actionsColumn.setIconHeader(ContactTracingLocationCols.settings.iconHeader());

        // Columns model
        FlexiTableColumnModel columnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
        columnModel.addFlexiColumnModel(referenceColumn);
        columnModel.addFlexiColumnModel(titleColumn);
        columnModel.addFlexiColumnModel(roomColumn);
        columnModel.addFlexiColumnModel(buildingColumn);
        columnModel.addFlexiColumnModel(qrIdColumn);
        columnModel.addFlexiColumnModel(guestColumn);
        columnModel.addFlexiColumnModel(registrationsColumn);
        columnModel.addFlexiColumnModel(actionsColumn);

        // Table model
        tableModel = new ContactTracingLocationTableModel(columnModel, contactTracingManager.getLocations(), contactTracingManager);

        // Table element
        tableEl = uifactory.addTableElement(getWindowControl(), "locationsTable", tableModel, getTranslator(), formLayout);
        tableEl.setPageSize(PAGE_SIZE);
        tableEl.setExportEnabled(true);
        tableEl.setSearchEnabled(true);
        tableEl.setMultiSelect(true);
        tableEl.setSelectAllEnable(true);
        tableEl.setShowAllRowsEnabled(true);

        // Create link to add a new location
        addLocationLink = uifactory.addFormLink("createNewLocation", "contact.tracing.location.add", null, formLayout, Link.BUTTON);
        addLocationLink.setIconLeftCSS("o_icon o_icon_add");

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
        tableModel.setObjects(contactTracingManager.getLocations());
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
        } else if (source == generateQrCodeSelectedLocationsLink) {
            // TODO Batch QR
        } else if (source == deleteSelectedLocationsLink) {
            // Get selected locations
            List<ContactTracingLocation> deleteList = new ArrayList<>();
            tableEl.getMultiSelectedIndex().forEach(i -> deleteList.add(tableModel.getObject(i)));

            // Set up the cmc
            deleteConfirmController = new ContactTracingLocationDeleteConfirmController(ureq, getWindowControl(), deleteList);
            cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteConfirmController.getInitialComponent(), true, translate("contact.tracing.location.delete.title"), true, true);
            listenTo(deleteConfirmController);
            listenTo(cmc);
            cmc.activate();
        } else if (source == generateQrCodeCalloutLink) {
            // TODO QR
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
}
